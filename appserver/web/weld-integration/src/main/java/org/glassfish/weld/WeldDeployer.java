/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.weld;

import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.web.ServletFilterMapping;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpSessionListener;
import jakarta.servlet.jsp.tagext.JspTag;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.api.invocation.ApplicationEnvironment;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.cdi.CDILoggerInfo;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.javaee.core.deployment.ApplicationHolder;
import org.glassfish.web.deployment.descriptor.AppListenerDescriptorImpl;
import org.glassfish.web.deployment.descriptor.ServletFilterDescriptor;
import org.glassfish.web.deployment.descriptor.ServletFilterMappingDescriptor;
import org.glassfish.weld.connector.WeldUtils;
import org.glassfish.weld.services.CDIServiceImpl;
import org.glassfish.weld.services.EjbServicesImpl;
import org.glassfish.weld.services.InjectionServicesImpl;
import org.glassfish.weld.services.NonModuleInjectionServices;
import org.glassfish.weld.services.ProxyServicesImpl;
import org.glassfish.weld.services.SecurityServicesImpl;
import org.glassfish.weld.services.TransactionServicesImpl;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.EEModuleDescriptor;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.helpers.EEModuleDescriptorImpl;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.injection.spi.InjectionServices;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.EjbSupport;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.security.spi.SecurityServices;
import org.jboss.weld.serialization.spi.ProxyServices;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.CONFIG;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.glassfish.api.invocation.ComponentInvocation.ComponentInvocationType.SERVLET_INVOCATION;
import static org.glassfish.cdi.CDILoggerInfo.ADDING_INJECTION_SERVICES;
import static org.glassfish.cdi.CDILoggerInfo.JMS_MESSAGElISTENER_AVAILABLE;
import static org.glassfish.cdi.CDILoggerInfo.MDB_PIT_EVENT;
import static org.glassfish.cdi.CDILoggerInfo.WELD_BOOTSTRAP_SHUTDOWN_EXCEPTION;
import static org.glassfish.internal.deployment.Deployment.APPLICATION_DISABLED;
import static org.glassfish.internal.deployment.Deployment.APPLICATION_LOADED;
import static org.glassfish.internal.deployment.Deployment.APPLICATION_STOPPED;
import static org.glassfish.internal.deployment.Deployment.APPLICATION_UNLOADED;
import static org.glassfish.weld.connector.WeldUtils.BDAType.JAR;
import static org.glassfish.weld.connector.WeldUtils.BDAType.RAR;
import static org.glassfish.weld.connector.WeldUtils.BDAType.WAR;
import static org.jboss.weld.bootstrap.api.Environments.SERVLET;
import static org.jboss.weld.bootstrap.spi.EEModuleDescriptor.ModuleType.CONNECTOR;
import static org.jboss.weld.bootstrap.spi.EEModuleDescriptor.ModuleType.EJB_JAR;
import static org.jboss.weld.bootstrap.spi.EEModuleDescriptor.ModuleType.WEB;
import static org.jboss.weld.manager.BeanManagerLookupService.lookupBeanManager;

@Service
public class WeldDeployer extends SimpleDeployer<WeldContainer, WeldApplicationContainer>
    implements PostConstruct, EventListener {

    private static final Logger LOG = CDILoggerInfo.getLogger();

    private static final String KEY_BUNDLE_DESCRIPTOR = BundleDescriptor.class.getName();
    public static final String WELD_EXTENSION = "org.glassfish.weld";
    public static final String WELD_DEPLOYMENT = "org.glassfish.weld.WeldDeployment";
    static final String WELD_BOOTSTRAP = "org.glassfish.weld.WeldBootstrap";
    private static final String WELD_CONTEXT_LISTENER = "org.glassfish.weld.WeldContextListener";

    // Note...this constant is also defined in org.apache.catalina.connector.AsyncContextImpl.  If it changes here it must
    // change there as well.  The reason it is duplicated is so that a dependency from web-core to gf-weld-connector
    // is not necessary.
    private static final String WELD_LISTENER = "org.jboss.weld.module.web.servlet.WeldInitialListener";
    private static final String WELD_TERMINATION_LISTENER = "org.jboss.weld.module.web.servlet.WeldTerminalListener";
    private static final String WELD_SHUTDOWN = "weld_shutdown";

    // This constant is used to indicate if bootstrap shutdown has been called or not.
    private static final String WELD_BOOTSTRAP_SHUTDOWN = "weld_bootstrap_shutdown";
    private static final String WELD_CONVERSATION_FILTER_CLASS = "org.jboss.weld.module.web.servlet.ConversationFilter";
    private static final String WELD_CONVERSATION_FILTER_NAME = "CDI Conversation Filter";

    @Inject
    private Events events;

    @Inject
    private ServiceLocator services;

    @Inject
    private ApplicationRegistry applicationRegistry;

    @Inject
    private InvocationManager invocationManager;

    @Inject
    private ArchiveFactory archiveFactory;

    private final Map<Application, WeldBootstrap> appToBootstrap = new HashMap<>();

    private final Map<BundleDescriptor, BeanDeploymentArchive> bundleToBeanDeploymentArchive = new HashMap<>();

    private static final Class<?>[] NON_CONTEXT_CLASSES = {
            Servlet.class,
            ServletContextListener.class,
            Filter.class,
            HttpSessionListener.class,
            ServletRequestListener.class,
            JspTag.class
            // TODO need to add more classes
    };

    static {
        try {
            ACLSingletonProvider.initializeSingletonProvider();
        } catch (RuntimeException ignore) {
            LOG.log(FINEST, "The singleton provider was already initialized, I am ignoring the exception.", ignore);
        }
    }

    @Override
    public MetaData getMetaData() {
        return new MetaData(true, null, new Class[] { Application.class });
    }

    @Override
    public void postConstruct() {
        events.register(this);
    }


    /**
     * Processing in this method is performed for each module that is in the process of being loaded
     * by the container.
     * This method will collect information from each archive (module) and produce
     * <code>BeanDeploymentArchive</code> information for each module.
     * <p>
     * The <code>BeanDeploymentArchive</code>s are stored in the <code>Deployment</code> (that will
     * eventually be handed off to <code>Weld</code>. Once this method is called for all modules
     * (and <code>BeanDeploymentArchive</code> information has been collected for all
     * <code>Weld</code> modules), a relationship structure is produced defining the accessiblity
     * rules for the <code>BeanDeploymentArchive</code>s.
     */
    @Override
    public WeldApplicationContainer load(WeldContainer container, DeploymentContext context) {

        DeployCommandParameters deployParams = context.getCommandParameters(DeployCommandParameters.class);
        ApplicationInfo appInfo = applicationRegistry.get(deployParams.name);

        ReadableArchive archive = context.getSource();

        // See if a WeldBootsrap has already been created - only want one per app.

        WeldBootstrap bootstrap = context.getTransientAppMetaData(WELD_BOOTSTRAP, WeldBootstrap.class);
        if (bootstrap == null) {
            bootstrap = new WeldBootstrap();
            Application app = context.getModuleMetaData(Application.class);
            appToBootstrap.put(app, bootstrap);

            // Stash the WeldBootstrap instance, so we may access the WeldManager later..
            context.addTransientAppMetaData(WELD_BOOTSTRAP, bootstrap);
            appInfo.addTransientAppMetaData(WELD_BOOTSTRAP, bootstrap);

            // Making sure that if WeldBootstrap is added, shutdown is set to false, as it is/would not have been called.
            appInfo.addTransientAppMetaData(WELD_BOOTSTRAP_SHUTDOWN, "false");
        }

        EjbBundleDescriptor ejbBundle = getEjbBundleFromContext(context);

        EjbServices ejbServices = null;

        Set<EjbDescriptor> ejbs = new HashSet<>();
        if (ejbBundle != null) {
            ejbs.addAll(ejbBundle.getEjbs());
            ejbServices = new EjbServicesImpl(services);
        }

        // Create a Deployment Collecting Information From The ReadableArchive (archive)
        DeploymentImpl deploymentImpl = context.getTransientAppMetaData(WELD_DEPLOYMENT, DeploymentImpl.class);
        if (deploymentImpl == null) {
            deploymentImpl = new DeploymentImpl(archive, ejbs, context, archiveFactory);

            // Add services
            deploymentImpl.getServices().add(TransactionServices.class, new TransactionServicesImpl(services));
            deploymentImpl.getServices().add(SecurityServices.class, new SecurityServicesImpl());
            deploymentImpl.getServices().add(ProxyServices.class, new ProxyServicesImpl(services));

            addWeldListenerToAllWars(context);
        } else {
            deploymentImpl.scanArchive(archive, ejbs, context);
        }
        deploymentImpl.addDeployedEjbs(ejbs);

        if (ejbBundle != null && !deploymentImpl.getServices().contains(EjbServices.class)) {
            // EJB Services is registered as a top-level service
            deploymentImpl.getServices().add(EjbServices.class, ejbServices);
        }

        BeanDeploymentArchive beanDeploymentArchive = deploymentImpl.getBeanDeploymentArchiveForArchive(archive.getName());
        if (beanDeploymentArchive == null || beanDeploymentArchive.getBeansXml().getBeanDiscoveryMode().equals(BeanDiscoveryMode.NONE)) {
            LOG.log(FINE, "The bean discovery mode was explicitly set to NONE in the beans.xml file"
                + " or the archive is not a CDI deployment archive.");
        } else {
            WebBundleDescriptor webBundleDescriptor = context.getModuleMetaData(WebBundleDescriptor.class);
            if (webBundleDescriptor != null) {
                webBundleDescriptor.setExtensionProperty(WELD_EXTENSION, "true");

                // Add the Weld Listener.  We have to do it here too in case addWeldListenerToAllWars wasn't
                // able to do it.
                webBundleDescriptor.addAppListenerDescriptorToFirst(new AppListenerDescriptorImpl(WELD_LISTENER));

                // Add Weld Context Listener - this listener will ensure the WeldELContextListener is used
                // for JSP's..
                webBundleDescriptor.addAppListenerDescriptor(new AppListenerDescriptorImpl(WELD_CONTEXT_LISTENER));

                // Weld 2.2.1.Final.  There is a tck test for this: org.jboss.cdi.tck.tests.context.session.listener.SessionContextHttpSessionListenerTest
                // This WeldTerminationListener must come after all application-defined listeners
                webBundleDescriptor.addAppListenerDescriptor(new AppListenerDescriptorImpl(WELD_TERMINATION_LISTENER));

                // Adding Weld ConverstationFilter if there is filterMapping for it and it doesn't exist already.
                // However, it will be applied only if web.xml has mapping for it.
                // Doing this here to make sure that its done only for CDI enabled web application
                for (ServletFilterMapping filterMapping : webBundleDescriptor.getServletFilterMappings()) {
                    String displayName = ((ServletFilterMappingDescriptor) filterMapping).getDisplayName();
                    if (WELD_CONVERSATION_FILTER_NAME.equals(displayName)) {
                        ServletFilterDescriptor filterDescriptor = new ServletFilterDescriptor();
                        filterDescriptor.setClassName(WELD_CONVERSATION_FILTER_CLASS);
                        filterDescriptor.setName(WELD_CONVERSATION_FILTER_NAME);
                        webBundleDescriptor.addServletFilter(filterDescriptor);
                        break;
                    }
                }
            }

            BundleDescriptor bundle = webBundleDescriptor == null ? ejbBundle : webBundleDescriptor;
            if (bundle != null) {
                // Register EE injection manager at the bean deployment archive level.
                // We use the generic InjectionService service to handle all EE-style
                // injection instead of the per-dependency-type InjectionPoint approach.
                // Each InjectionServicesImpl instance knows its associated GlassFish bundle.

                InjectionManager injectionManager = services.getService(InjectionManager.class);
                InjectionServices injectionServices = new InjectionServicesImpl(injectionManager, bundle, deploymentImpl);

                if (LOG.isLoggable(FINE)) {
                    LOG.log(FINE, ADDING_INJECTION_SERVICES, new Object[] { injectionServices, beanDeploymentArchive.getId() });
                }

                beanDeploymentArchive.getServices().add(InjectionServices.class, injectionServices);
                EEModuleDescriptor eeModuleDescriptor = getEEModuleDescriptor(beanDeploymentArchive);
                if (eeModuleDescriptor != null) {
                    beanDeploymentArchive.getServices().add(EEModuleDescriptor.class, eeModuleDescriptor);
                }

                // Relevant in WAR BDA - WEB-INF/lib BDA scenarios
                for (BeanDeploymentArchive subBeanDeploymentArchive : beanDeploymentArchive.getBeanDeploymentArchives()) {
                    if (LOG.isLoggable(FINE)) {
                        LOG.log(FINE, ADDING_INJECTION_SERVICES, new Object[] { injectionServices, subBeanDeploymentArchive.getId() });
                    }

                    subBeanDeploymentArchive.getServices().add(InjectionServices.class, injectionServices);
                    eeModuleDescriptor = getEEModuleDescriptor(beanDeploymentArchive);
                    if (eeModuleDescriptor != null) {
                        beanDeploymentArchive.getServices().add(EEModuleDescriptor.class, eeModuleDescriptor);
                    }
                }
                LOG.log(CONFIG,
                    "Adding pair bundle.class={0}, bundle.name={1} and archive.class={2}, archive.id={3}",
                new Object[] {bundle.getClass(), bundle.getName(), beanDeploymentArchive.getClass(),
                    beanDeploymentArchive.getId()});
                bundleToBeanDeploymentArchive.put(bundle, beanDeploymentArchive);
                appInfo.addTransientAppMetaData(KEY_BUNDLE_DESCRIPTOR, bundle);
            }
        }


        context.addTransientAppMetaData(WELD_DEPLOYMENT, deploymentImpl);
        appInfo.addTransientAppMetaData(WELD_DEPLOYMENT, deploymentImpl);

        return new WeldApplicationContainer();
    }


    /**
     * Specific stages of the Weld bootstrapping process will execute across different stages of the deployment process.
     * Weld deployment will happen when the load phase of the deployment process is complete. When all modules have been
     * loaded, a deployment graph is produced defining the accessibility relationships between
     * <code>BeanDeploymentArchive</code>s.
     */
    @Override
    public void event(Event<?> event) {
        LOG.log(FINEST, () -> "event(event.name=" + event.name() + ", event.hook=" + event.hook() + ")");
        if (event.is(APPLICATION_LOADED)) {
            ApplicationInfo appInfo = (ApplicationInfo) event.hook();
            WeldBootstrap bootstrap = appInfo.getTransientAppMetaData(WELD_BOOTSTRAP, WeldBootstrap.class);
            if (bootstrap == null) {
                return;
            }
            enable(appInfo, bootstrap);
        } else if (event.is(APPLICATION_STOPPED)
                || event.is(APPLICATION_UNLOADED)
                || event.is(APPLICATION_DISABLED)) {
            disable((ApplicationInfo) event.hook());
        }
    }

    @Override
    protected void generateArtifacts(DeploymentContext dc) throws DeploymentException {

    }

    @Override
    protected void cleanArtifacts(DeploymentContext dc) throws DeploymentException {

    }

    @Override
    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }

    public BeanDeploymentArchive getBeanDeploymentArchiveForBundle(BundleDescriptor bundle) {
        LOG.log(FINEST, "getBeanDeploymentArchiveForBundle(bundle.class={0})", bundle.getClass());
        return bundleToBeanDeploymentArchive.get(bundle);
    }

    public boolean isCdiEnabled(BundleDescriptor bundle) {
        return bundleToBeanDeploymentArchive.containsKey(bundle);
    }

    public WeldBootstrap getBootstrapForApp(Application app) {
        LOG.log(FINEST, "getBootstrapForApp(app.name={0})", app.getName());
        return appToBootstrap.get(app);
    }

    private void enable(ApplicationInfo appInfo, WeldBootstrap bootstrap) {
        LOG.log(INFO, () -> "Enabling Weld for " + appInfo);

        DeploymentImpl deploymentImpl = appInfo.getTransientAppMetaData(WELD_DEPLOYMENT, DeploymentImpl.class);

        deploymentImpl.buildDeploymentGraph();

        List<BeanDeploymentArchive> archives = deploymentImpl.getBeanDeploymentArchives();
        for (BeanDeploymentArchive archive : archives) {
            ResourceLoaderImpl loader = new ResourceLoaderImpl(
                ((BeanDeploymentArchiveImpl) archive).getModuleClassLoaderForBDA());
            archive.getServices().add(ResourceLoader.class, loader);
        }

        final InjectionManager injectionManager = services.getService(InjectionManager.class);
        addCdiServicesToNonModuleBdas(deploymentImpl.getLibJarRootBdas(), injectionManager);
        addCdiServicesToNonModuleBdas(deploymentImpl.getRarRootBdas(), injectionManager);

        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        invocationManager.pushAppEnvironment(new WeldApplicationEnvironment(appInfo));
        try {
            doBootstrapStartup(appInfo, bootstrap, deploymentImpl);
        } catch (Throwable t) {
            doBootstrapShutdown(appInfo);
            String msgPrefix = getDeploymentErrorMsgPrefix(t);
            throw new DeploymentException(msgPrefix + t.getMessage(), t);
        } finally {
            invocationManager.popAppEnvironment();

            // The TCL is originally the EAR classloader and is reset during Bean deployment to the
            // corresponding module classloader in BeanDeploymentArchiveImpl.getBeans
            // for Bean classloading to succeed.
            //
            // The TCL is reset to its old value here.
            Thread.currentThread().setContextClassLoader(originalClassLoader);
            deploymentComplete(deploymentImpl);
        }
    }

    private void disable(ApplicationInfo appInfo) {
        Application app = appInfo.getMetaData(Application.class);
        LOG.log(FINEST, () -> "Found application: " + app);
        if (app != null) {
            for (BundleDescriptor next : app.getBundleDescriptors()) {
                if (next instanceof EjbBundleDescriptor || next instanceof WebBundleDescriptor) {
                    bundleToBeanDeploymentArchive.remove(next);
                }
            }
            appToBootstrap.remove(app);
        }

        String shutdown = appInfo.getTransientAppMetaData(WELD_SHUTDOWN, String.class);
        if (Boolean.TRUE.equals(Boolean.valueOf(shutdown))) {
            return;
        }

        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(appInfo.getAppClassLoader());
        try {
            WeldBootstrap bootstrap = appInfo.getTransientAppMetaData(WELD_BOOTSTRAP, WeldBootstrap.class);
            LOG.log(FINEST, () -> "Found bootstrap: " + bootstrap);
            if (bootstrap != null) {
                LOG.log(INFO, () -> "Disabling Weld for " + appInfo);
                invocationManager.pushAppEnvironment(new WeldApplicationEnvironment(appInfo));
                try {
                    doBootstrapShutdown(appInfo);
                } catch (Exception e) {
                    LOG.log(WARNING, WELD_BOOTSTRAP_SHUTDOWN_EXCEPTION, e);
                } finally {
                    invocationManager.popAppEnvironment();
                }
                appInfo.addTransientAppMetaData(WELD_SHUTDOWN, "true");
            }
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }

        DeploymentImpl deploymentImpl = appInfo.getTransientAppMetaData(WELD_DEPLOYMENT, DeploymentImpl.class);
        if (deploymentImpl != null) {
            deploymentImpl.cleanup();
        }
    }

    private void doBootstrapStartup(ApplicationInfo appInfo, WeldBootstrap bootstrap, DeploymentImpl deploymentImpl) {
        ComponentInvocation componentInvocation = createComponentInvocation(appInfo);
        try {
            invocationManager.preInvoke(componentInvocation);
            Iterable<Metadata<Extension>> extensions = deploymentImpl.getExtensions();
            LOG.log(FINE, () -> "Starting extensions: " + extensions);
            bootstrap.startExtensions(extensions);
            bootstrap.startContainer(appInfo.getName(), SERVLET, deploymentImpl);

            if (!deploymentImpl.getBeanDeploymentArchives().isEmpty()) {
                enableEjbSupport(bootstrap, deploymentImpl);
            }

            bootstrap.startInitialization();
            fireProcessInjectionTargetEvents(bootstrap, deploymentImpl);
            bootstrap.deployBeans();

            bootstrap.validateBeans();
            bootstrap.endInitialization();
        } finally {
            invocationManager.postInvoke(componentInvocation);
        }
    }


    private ComponentInvocation createComponentInvocation(ApplicationInfo applicationInfo) {
        JndiNameEnvironment bundleDescriptor = applicationInfo.getTransientAppMetaData(KEY_BUNDLE_DESCRIPTOR,
            JndiNameEnvironment.class);
        String componentEnvId = DOLUtils.getComponentEnvId(bundleDescriptor);
        LOG.log(Level.FINE,
            () -> "Computed component env id=" + componentEnvId + " for application name=" + applicationInfo.getName());
        ComponentInvocation componentInvocation = new ComponentInvocation(componentEnvId, SERVLET_INVOCATION,
            applicationInfo, applicationInfo.getName(), DOLUtils.getModuleName(bundleDescriptor));

        componentInvocation.setJNDIEnvironment(bundleDescriptor);
        return componentInvocation;
    }


    /**
     * Install support for delegating some EJB tasks to the right bean archive.
     * Without this, when the a bean manager for the root bean archive is used, it will not
     * find the EJB definitions in a sub-archive, and will treat the bean as a normal CDI bean.
     * <p>
     * For EJB beans a few special rules have to be taken into account, and without applying these
     * rules CreateBeanAttributesTest#testBeanAttributesForSessionBean fails.
     */
    private void enableEjbSupport(WeldBootstrap bootstrap, DeploymentImpl deploymentImpl) {
        BeanDeploymentArchive rootArchive = deploymentImpl.getBeanDeploymentArchives().get(0);
        ServiceRegistry rootServices = bootstrap.getManager(rootArchive).getServices();

        EjbSupport originalEjbSupport = rootServices.get(EjbSupport.class);
        if (originalEjbSupport != null) {
            // We need to create a proxy instead of a simple wrapper, since EjbSupport references
            // the type "EnhancedAnnotatedType", which the Weld OSGi bundle doesn't export.
            WeldInvocationHandler handler = new WeldInvocationHandler(deploymentImpl, originalEjbSupport, bootstrap);
            EjbSupport proxyEjbSupport = (EjbSupport) Proxy.newProxyInstance(EjbSupport.class.getClassLoader(),
                    new Class[] { EjbSupport.class }, handler);
            rootServices.add(EjbSupport.class, proxyEjbSupport);
        }
    }

    private void deploymentComplete(DeploymentImpl deploymentImpl) {
        for (BeanDeploymentArchive oneBda : deploymentImpl.getBeanDeploymentArchives()) {
            ((BeanDeploymentArchiveImpl) oneBda).setDeploymentComplete(true);
        }
    }

    private void doBootstrapShutdown(ApplicationInfo appInfo) {
        WeldBootstrap bootstrap = appInfo.getTransientAppMetaData(WELD_BOOTSTRAP, WeldBootstrap.class);
        String bootstrapShutdown = appInfo.getTransientAppMetaData(WELD_BOOTSTRAP_SHUTDOWN, String.class);
        if (bootstrapShutdown == null || Boolean.valueOf(bootstrapShutdown).equals(Boolean.FALSE)) {
            bootstrap.shutdown();
            appInfo.addTransientAppMetaData(WELD_BOOTSTRAP_SHUTDOWN, "true");
        }
    }

    private String getDeploymentErrorMsgPrefix(Throwable t) {
        if (t instanceof jakarta.enterprise.inject.spi.DefinitionException) {
            return "CDI definition failure: ";
        }

        if (t instanceof jakarta.enterprise.inject.spi.DeploymentException) {
            return "CDI deployment failure: ";
        }

        Throwable cause = t.getCause();
        if (cause == t || cause == null) {
            return "CDI deployment failure: ";
        }

        return getDeploymentErrorMsgPrefix(cause);
    }

    /**
     * We are only firing ProcessInjectionTarget<X> for non-contextual EE
     * components and not using the InjectionTarget<X> from the event during
     * instance creation in {@link CDIServiceImpl}
     *
     * TODO weld would provide a better way to do this, otherwise we may need
     * TODO to store InjectionTarget<X> to be used in instance creation
     */
    private void fireProcessInjectionTargetEvents(WeldBootstrap bootstrap, DeploymentImpl impl) {
        List<BeanDeploymentArchive> bdaList = impl.getBeanDeploymentArchives();

        // Web-Profile and other lighter distributions would not ship the JMS
        // API and implementations. So, the weld-integration layer cannot
        // have a direct dependency on the JMS API
        final Class<?> messageListenerClass = loadMessageListenerClass();

        for (BeanDeploymentArchive bda : bdaList) {
            Collection<Class<?>> bdaClasses = ((BeanDeploymentArchiveImpl) bda).getBeanClassObjects();
            for (Class<?> bdaClazz : bdaClasses) {
                for (Class<?> nonClazz : NON_CONTEXT_CLASSES) {
                    if (nonClazz.isAssignableFrom(bdaClazz)) {
                        firePITEvent(bootstrap, bda, bdaClazz);
                    }
                }

                // For distributions that have the JMS API, an MDB is a valid
                // non-contextual EE component to which we have to fire ProcessInjectionTarget
                // events (see GLASSFISH-16730)
                if (messageListenerClass != null && messageListenerClass.isAssignableFrom(bdaClazz)) {
                    LOG.log(FINE, MDB_PIT_EVENT, bdaClazz);
                    firePITEvent(bootstrap, bda, bdaClazz);
                }
            }
        }
    }

    /**
     * @return {@link jakarta.jms.MessageListener} class (for full profile) or null (for web profile).
     */
    private Class<?> loadMessageListenerClass() {
        try {
            Class<?> messageListenerClass = Thread.currentThread().getContextClassLoader()
                .loadClass("jakarta.jms.MessageListener");
            LOG.log(FINE, JMS_MESSAGElISTENER_AVAILABLE);
            return messageListenerClass;
        } catch (ClassNotFoundException cnfe) {
            return null;
        }
    }

    private void firePITEvent(WeldBootstrap bootstrap, BeanDeploymentArchive bda, Class<?> bdaClazz) {
        // Fix for issue GLASSFISH-17464
        // The PIT event should not be fired for interfaces
        if (bdaClazz.isInterface()) {
            return;
        }

        AnnotatedType<?> annotatedType = bootstrap.getManager(bda).createAnnotatedType(bdaClazz);
        InjectionTarget<?> injectionTarget = bootstrap.getManager(bda).fireProcessInjectionTarget(annotatedType);

        ((BeanDeploymentArchiveImpl) bda).putInjectionTarget(annotatedType, injectionTarget);
    }


    private EEModuleDescriptor getEEModuleDescriptor(BeanDeploymentArchive beanDeploymentArchive) {
        if (!(beanDeploymentArchive instanceof BeanDeploymentArchiveImpl)) {
            return null;
        }

        WeldUtils.BDAType bdaType = ((BeanDeploymentArchiveImpl) beanDeploymentArchive).getBDAType();
        if (bdaType.equals(JAR)) {
            return new EEModuleDescriptorImpl(beanDeploymentArchive.getId(), EJB_JAR);
        }

        if (bdaType.equals(WAR)) {
            return new EEModuleDescriptorImpl(beanDeploymentArchive.getId(), WEB);
        }

        if (bdaType.equals(RAR)) {
            return new EEModuleDescriptorImpl(beanDeploymentArchive.getId(), CONNECTOR);
        }

        return null;
    }

    private void addWeldListenerToAllWars(DeploymentContext context) {
        // If there's at least 1 ejb jar then add the listener to all wars
        ApplicationHolder applicationHolder = context.getModuleMetaData(ApplicationHolder.class);
        if (applicationHolder != null) {
            if (applicationHolder.app.getBundleDescriptors(EjbBundleDescriptor.class).size() > 0) {
                Set<WebBundleDescriptor> webBundleDescriptors = applicationHolder.app.getBundleDescriptors(WebBundleDescriptor.class);
                for (WebBundleDescriptor oneWebBundleDescriptor : webBundleDescriptors) {
                    // Add the Weld Listener if it does not already exist..
                    // we have to do this regardless because the war may not be cdi-enabled but an ejb is.
                    oneWebBundleDescriptor.addAppListenerDescriptorToFirst(new AppListenerDescriptorImpl(WELD_LISTENER));
                    oneWebBundleDescriptor.addAppListenerDescriptor(new AppListenerDescriptorImpl(WELD_TERMINATION_LISTENER));
                }
            }
        }
    }

    private EjbBundleDescriptor getEjbBundleFromContext(DeploymentContext context) {
        EjbBundleDescriptor ejbBundle = context.getModuleMetaData(EjbBundleDescriptor.class);
        if (ejbBundle != null) {
            return ejbBundle;
        }
        WebBundleDescriptor webBundleDescriptor = context.getModuleMetaData(WebBundleDescriptor.class);
        if (webBundleDescriptor == null) {
            return null;
        }
        Collection<EjbBundleDescriptor> ejbBundles = webBundleDescriptor.getExtensionsDescriptors(EjbBundleDescriptor.class);
        if (ejbBundles.iterator().hasNext()) {
            return ejbBundles.iterator().next();
        }
        return null;
    }

    /**
     * Add the cdi services to a non-module bda (library or rar)
     */
    private void addCdiServicesToNonModuleBdas(Iterator<RootBeanDeploymentArchive> rootBdas, InjectionManager injectionMgr) {
        if (injectionMgr != null && rootBdas != null) {
            while (rootBdas.hasNext()) {
                RootBeanDeploymentArchive oneRootBda = rootBdas.next();
                addCdiServicesToBda(injectionMgr, oneRootBda);
                addCdiServicesToBda(injectionMgr, oneRootBda.getModuleBda());
            }
        }
    }

    private void addCdiServicesToBda(InjectionManager injectionManager, BeanDeploymentArchive beanDeploymentArchive) {
        beanDeploymentArchive.getServices().add(InjectionServices.class, new NonModuleInjectionServices(injectionManager));
    }


    private static class WeldInvocationHandler implements InvocationHandler {
        private final DeploymentImpl deploymentImpl;
        private final EjbSupport originalEjbSupport;
        private final WeldBootstrap bootstrap;

        private WeldInvocationHandler(DeploymentImpl deploymentImpl, EjbSupport originalEjbSupport,
            WeldBootstrap bootstrap) {
            this.deploymentImpl = deploymentImpl;
            this.originalEjbSupport = originalEjbSupport;
            this.bootstrap = bootstrap;
        }


        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("isEjb")) {
                EjbSupport targetEjbSupport = getTargetEjbSupport((Class<?>) args[0]);
                // Unlikely to be null, but let's check to be sure.
                if (targetEjbSupport != null) {
                    return method.invoke(targetEjbSupport, args);
                }
            } else if (method.getName().equals("createSessionBeanAttributes")) {
                Object enhancedAnnotated = args[0];

                Class<?> beanClass = (Class<?>) enhancedAnnotated.getClass().getMethod("getJavaClass")
                    .invoke(enhancedAnnotated);

                EjbSupport targetEjbSupport = getTargetEjbSupport(beanClass);
                if (targetEjbSupport != null) {
                    return method.invoke(targetEjbSupport, args);
                }
            }
            return method.invoke(originalEjbSupport, args);
        }


        private EjbSupport getTargetEjbSupport(Class<?> beanClass) {
            BeanDeploymentArchive ejbArchive = deploymentImpl.getBeanDeploymentArchive(beanClass);
            if (ejbArchive == null) {
                return null;
            }
            BeanManagerImpl ejbBeanManager = lookupBeanManager(beanClass, bootstrap.getManager(ejbArchive));
            return ejbBeanManager.getServices().get(EjbSupport.class);
        }
    }

    private static class WeldApplicationEnvironment implements ApplicationEnvironment {
        private final ApplicationInfo appInfo;

        private WeldApplicationEnvironment(ApplicationInfo appInfo) {
            this.appInfo = appInfo;
        }


        @Override
        public String getName() {
            return appInfo.getName();
        }
    }
}
