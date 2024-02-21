/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.container.common.impl.managedbean;

import com.sun.enterprise.container.common.spi.CDIService;
import com.sun.enterprise.container.common.spi.CDIService.CDIInjectionContext;
import com.sun.enterprise.container.common.spi.InterceptorInvoker;
import com.sun.enterprise.container.common.spi.JavaEEInterceptorBuilder;
import com.sun.enterprise.container.common.spi.JavaEEInterceptorBuilderFactory;
import com.sun.enterprise.container.common.spi.ManagedBeanManager;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.container.common.spi.util.InterceptorInfo;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.InterceptorDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.ManagedBeanDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.NamingObjectProxy;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.PostStartupRunLevel;
import org.glassfish.internal.data.ApplicationInfo;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType.POST_CONSTRUCT;
import static com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType.PRE_DESTROY;
import static java.security.AccessController.doPrivileged;
import static java.util.Collections.synchronizedMap;
import static java.util.logging.Level.FINE;
import static org.glassfish.internal.deployment.Deployment.APPLICATION_LOADED;
import static org.glassfish.internal.deployment.Deployment.APPLICATION_UNLOADED;
import static org.glassfish.internal.deployment.Deployment.DEPLOYMENT_FAILURE;

/**
 */
@Service(name = "ManagedBeanManagerImpl")
@RunLevel(value = PostStartupRunLevel.VAL, mode = RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
public class ManagedBeanManagerImpl implements ManagedBeanManager, PostConstruct, EventListener {

    private static final Logger LOG = LogDomains.getLogger(ManagedBeanManagerImpl.class, LogDomains.CORE_LOGGER, false);

    @Inject
    private ComponentEnvManager compEnvManager;

    @Inject
    private InjectionManager injectionManager;

    @Inject
    private GlassfishNamingManager namingManager;

    @Inject
    private ServiceLocator serviceLocator;

    @Inject
    private Events events;

    @Inject
    private ProcessEnvironment processEnv;

    private ProcessType processType;

    // Keep track of contexts for managed bean instances instantiated via CDI
    private final Map<BundleDescriptor, Map<Object, CDIInjectionContext>> cdiManagedBeanInstanceMap = new HashMap<>();

    // Used to hold managed beans in app client container
    private final Map<String, NamingObjectProxy> appClientManagedBeans = new HashMap<>();

    @Override
    public void postConstruct() {
        events.register(this);
        processType = processEnv.getProcessType();
    }

    @Override
    public void event(Event<?> event) {
        if (event.is(APPLICATION_LOADED)) {
            ApplicationInfo info = (ApplicationInfo) event.hook();
            loadManagedBeans(info);
            registerAppLevelDependencies(info);
        } else if (event.is(APPLICATION_UNLOADED)) {
            ApplicationInfo info = (ApplicationInfo) event.hook();
            doCleanup(info.getMetaData(Application.class));
        } else if (event.is(DEPLOYMENT_FAILURE)) {
            DeploymentContext context = (DeploymentContext) event.hook();
            doCleanup(context.getModuleMetaData(Application.class));
        }
    }

    private void doCleanup(Application app) {
        if (app != null) {
            unloadManagedBeans(app);

            unregisterAppLevelDependencies(app);
        }
    }

    private void registerAppLevelDependencies(ApplicationInfo appInfo) {
        Application app = appInfo.getMetaData(Application.class);
        if (app == null) {
            return;
        }

        try {
            compEnvManager.bindToComponentNamespace(app);
        } catch (Exception e) {
            throw new RuntimeException("Error binding app-level env dependencies " + app.getAppName(), e);
        }

    }

    private void unregisterAppLevelDependencies(Application app) {
        if (app != null) {
            try {
                compEnvManager.unbindFromComponentNamespace(app);
            } catch (Exception e) {
                LOG.log(FINE, "Exception unbinding app objects", e);
            }
        }
    }

    private void loadManagedBeans(ApplicationInfo appInfo) {
        Application app = appInfo.getMetaData(Application.class);

        if (app == null) {
            return;
        }

        loadManagedBeans(app);
    }

    @Override
    public void loadManagedBeans(Application app) {
        CDIService cdiService = serviceLocator.getService(CDIService.class);

        for (BundleDescriptor bundle : app.getBundleDescriptors()) {
            if (!bundleEligible(bundle)) {
                continue;
            }

            boolean isCDIBundle = cdiService != null && cdiService.isCDIEnabled(bundle);

            for (ManagedBeanDescriptor managedBeanDescriptor : bundle.getManagedBeans()) {

                try {
                    Set<String> interceptorClasses = managedBeanDescriptor.getAllInterceptorClasses();
                    Class<?> targetClass = bundle.getClassLoader().loadClass(managedBeanDescriptor.getBeanClassName());

                    InterceptorInfo interceptorInfo = new InterceptorInfo();
                    interceptorInfo.setTargetClass(targetClass);
                    interceptorInfo.setInterceptorClassNames(interceptorClasses);
                    interceptorInfo.setAroundConstructInterceptors(managedBeanDescriptor.getAroundConstructCallbackInterceptors(targetClass,
                            getConstructor(targetClass, isCDIBundle)));
                    interceptorInfo.setPostConstructInterceptors(managedBeanDescriptor.getCallbackInterceptors(POST_CONSTRUCT));
                    interceptorInfo.setPreDestroyInterceptors(managedBeanDescriptor.getCallbackInterceptors(PRE_DESTROY));

                    if (managedBeanDescriptor.hasAroundInvokeMethod()) {
                        interceptorInfo.setHasTargetClassAroundInvoke(true);
                    }

                    Map<Method, List<InterceptorDescriptor>> interceptorChains = new HashMap<>();
                    for (Method targetMethod : targetClass.getMethods()) {
                        interceptorChains.put(targetMethod, managedBeanDescriptor.getAroundInvokeInterceptors(targetMethod));
                    }

                    interceptorInfo.setAroundInvokeInterceptorChains(interceptorChains);

                    // TODO can optimize this out for the non-JAXRS, non-application specified interceptor case
                    interceptorInfo.setSupportRuntimeDelegate(true);

                    JavaEEInterceptorBuilderFactory interceptorBuilderFactory = serviceLocator
                            .getService(JavaEEInterceptorBuilderFactory.class);

                    JavaEEInterceptorBuilder builder = interceptorBuilderFactory.createBuilder(interceptorInfo);

                    managedBeanDescriptor.setInterceptorBuilder(builder);

                    compEnvManager.bindToComponentNamespace(managedBeanDescriptor);

                    SimpleJndiName jndiName = managedBeanDescriptor.getGlobalJndiName();
                    ManagedBeanNamingProxy namingProxy = new ManagedBeanNamingProxy(managedBeanDescriptor, serviceLocator);

                    if (processType.isServer()) {
                        namingManager.publishObject(jndiName, namingProxy, true);
                    } else {
                        // Can't store them in server's global naming service so keep
                        // them in local map.
                        appClientManagedBeans.put(jndiName.toString(), namingProxy);
                    }

                } catch (Exception e) {
                    throw new RuntimeException("Error binding ManagedBean " + managedBeanDescriptor.getBeanClassName() + " with name = "
                            + managedBeanDescriptor.getName(), e);
                }
            }

            cdiManagedBeanInstanceMap.put(bundle, synchronizedMap(new HashMap<>()));
        }

    }

    private Constructor<?> getConstructor(Class<?> clz, boolean isCDIBundle) throws Exception {
        if (isCDIBundle) {
            for (Constructor<?> ctor : clz.getDeclaredConstructors()) {
                if (ctor.getAnnotation(jakarta.inject.Inject.class) != null) {
                    // @Inject constructor
                    return ctor;
                }
            }
        }

        // Not a CDI bundle or no @Inject constructor - use no-arg constructor
        return clz.getConstructor();
    }

    @Override
    public Object getManagedBean(String globalJndiName) throws Exception {
        NamingObjectProxy proxy = appClientManagedBeans.get(globalJndiName);
        if (proxy == null) {
            return null;
        }

        return proxy.create(new InitialContext());

    }

    /**
     * Apply a runtime interceptor instance to all managed beans in the given module
     *
     * @param interceptorInstance
     * @param bundle bundle descripto
     *
     */
    @Override
    public void registerRuntimeInterceptor(Object interceptorInstance, BundleDescriptor bundle) {
        for (ManagedBeanDescriptor managedBeanDescriptor : bundle.getManagedBeans()) {
            JavaEEInterceptorBuilder interceptorBuilder = (JavaEEInterceptorBuilder) managedBeanDescriptor.getInterceptorBuilder();

            interceptorBuilder.addRuntimeInterceptor(interceptorInstance);
        }
    }

    @Override
    public void unloadManagedBeans(Application app) {
        for (BundleDescriptor bundle : app.getBundleDescriptors()) {
            if (!bundleEligible(bundle)) {
                continue;
            }

            Map<Object, CDIInjectionContext> cdiInstances = cdiManagedBeanInstanceMap.remove(bundle);

            if (cdiInstances != null) {
                for (CDIInjectionContext cdiInjectionContext : cdiInstances.values()) {
                    try {
                        cdiInjectionContext.cleanup(true);
                    } catch (Exception e) {
                        LOG.log(FINE, "Exception during CDI cleanup for " + cdiInjectionContext, e);
                    }
                }
            }

            for (ManagedBeanDescriptor managedBeanDescriptor : bundle.getManagedBeans()) {
                for (Object instance : managedBeanDescriptor.getBeanInstances()) {

                    InterceptorInvoker invoker = (InterceptorInvoker) managedBeanDescriptor.getSupportingInfoForBeanInstance(instance);

                    try {
                        invoker.invokePreDestroy();
                    } catch (Exception e) {
                        LOG.log(FINE, "Managed bean " + managedBeanDescriptor.getBeanClassName() + " PreDestroy", e);
                    }
                }

                ComponentEnvManager compEnvManager = serviceLocator.getService(ComponentEnvManager.class);

                try {
                    compEnvManager.unbindFromComponentNamespace(managedBeanDescriptor);
                } catch (javax.naming.NamingException ne) {
                    LOG.log(FINE, "Managed bean " + managedBeanDescriptor.getBeanClassName() + " unbind", ne);
                }

                GlassfishNamingManager namingManager = serviceLocator.getService(GlassfishNamingManager.class);
                SimpleJndiName jndiName = managedBeanDescriptor.getGlobalJndiName();

                if (processType.isServer()) {
                    try {
                        namingManager.unpublishObject(jndiName);
                    } catch (NamingException ne) {
                        LOG.log(FINE, "Error unpubishing managed bean " + managedBeanDescriptor.getBeanClassName()
                            + " with jndi name " + jndiName, ne);
                    }
                } else {
                    appClientManagedBeans.remove(jndiName);
                }

                managedBeanDescriptor.clearAllBeanInstanceInfo();
            }
        }

    }

    private boolean bundleEligible(BundleDescriptor bundle) {
        if (processType.isServer()) {
            return bundle instanceof WebBundleDescriptor || bundle instanceof EjbBundleDescriptor;
        }

        if (processType == ProcessType.ACC) {
            return bundle instanceof ApplicationClientDescriptor;
        }

        return false;
    }

    @Override
    public <T> T createManagedBean(Class<T> managedBeanClass) throws Exception {
        ManagedBeanDescriptor managedBeanDesc = null;

        try {
            BundleDescriptor bundle = getBundle();
            managedBeanDesc = bundle.getManagedBeanByBeanClass(managedBeanClass.getName());
        } catch (Exception e) {
            // OK. Can mean that it's not annotated with @ManagedBean but CDI can handle it.
        }

        return createManagedBean(managedBeanDesc, managedBeanClass);
    }

    @Override
    public <T> T createManagedBean(Class<T> managedBeanClass, boolean invokePostConstruct) throws Exception {
        ManagedBeanDescriptor managedBeanDescriptor = null;

        try {
            managedBeanDescriptor = getBundle().getManagedBeanByBeanClass(managedBeanClass.getName());
        } catch (Exception e) {
            // OK. Can mean that it's not annotated with @ManagedBean but CDI can handle it.
        }

        return createManagedBean(managedBeanDescriptor, managedBeanClass, invokePostConstruct);
    }

    /**
     *
     * @param managedBeanDescriptor can be null if CDI enabled bundle.
     * @param managedBeanClass
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T createManagedBean(ManagedBeanDescriptor managedBeanDescriptor, Class<T> managedBeanClass) throws Exception {
        CDIService cdiService = serviceLocator.getService(CDIService.class);

        BundleDescriptor bundleDescriptor = null;

        if (managedBeanDescriptor == null) {
            bundleDescriptor = getBundle();
        } else {
            bundleDescriptor = managedBeanDescriptor.getBundleDescriptor();
        }

        if (bundleDescriptor == null) {
            throw new IllegalStateException("Class " + managedBeanClass + " is not a valid EE ManagedBean class");
        }

        T callerObject = null;

        if (cdiService != null && cdiService.isCDIEnabled(bundleDescriptor)) {

            // Have CDI create, inject, and call PostConstruct on managed bean

            CDIInjectionContext<T> cdiContext = cdiService.createManagedObject(managedBeanClass, bundleDescriptor);
            callerObject = cdiContext.getInstance();

            // Need to keep track of context in order to destroy properly
            Map<Object, CDIInjectionContext> bundleNonManagedObjs = cdiManagedBeanInstanceMap.get(bundleDescriptor);
            bundleNonManagedObjs.put(callerObject, cdiContext);

        } else {
            JavaEEInterceptorBuilder interceptorBuilder = (JavaEEInterceptorBuilder) managedBeanDescriptor.getInterceptorBuilder();

            InterceptorInvoker interceptorInvoker = interceptorBuilder.createInvoker(null);

            // This is the object passed back to the caller.
            callerObject = (T) interceptorInvoker.getProxy();

            // Inject interceptor instances
            for (Object interceptorInstance : interceptorInvoker.getInterceptorInstances()) {
                inject(interceptorInstance, managedBeanDescriptor);
            }

            interceptorInvoker.invokeAroundConstruct();

            // This is the managed bean class instance
            Object managedBean = interceptorInvoker.getTargetInstance();
            inject(managedBean, managedBeanDescriptor);
            interceptorInvoker.invokePostConstruct();

            managedBeanDescriptor.addBeanInstanceInfo(managedBean, interceptorInvoker);
        }

        return callerObject;
    }

    /**
     *
     * @param managedBeanDescriptor can be null if CDI enabled bundle.
     * @param managedBeanClass
     * @param invokePostConstruct
     * @return
     * @throws Exception
     */
    @Override
    public <T> T createManagedBean(ManagedBeanDescriptor managedBeanDescriptor, Class<T> managedBeanClass, boolean invokePostConstruct)
            throws Exception {
        CDIService cdiService = serviceLocator.getService(CDIService.class);

        BundleDescriptor bundleDescriptor = null;

        if (managedBeanDescriptor == null) {
            bundleDescriptor = getBundle();
        } else {
            bundleDescriptor = managedBeanDescriptor.getBundleDescriptor();
        }

        if (bundleDescriptor == null) {
            throw new IllegalStateException("Class " + managedBeanClass + " is not a valid EE ManagedBean class");
        }

        T callerObject = null;

        if ((cdiService != null) && cdiService.isCDIEnabled(bundleDescriptor)) {

            // Have CDI create, inject, and call PostConstruct (if desired) on managed bean

            CDIInjectionContext<T> cdiContext = cdiService.createManagedObject(managedBeanClass, bundleDescriptor, invokePostConstruct);
            callerObject = cdiContext.getInstance();
            // Need to keep track of context in order to destroy properly
            Map<Object, CDIInjectionContext> bundleNonManagedObjs = cdiManagedBeanInstanceMap.get(bundleDescriptor);
            bundleNonManagedObjs.put(callerObject, cdiContext);

        } else {
            JavaEEInterceptorBuilder interceptorBuilder = (JavaEEInterceptorBuilder) managedBeanDescriptor.getInterceptorBuilder();

            // This is the managed bean class instance
            T managedBean = managedBeanClass.getDeclaredConstructor().newInstance();

            InterceptorInvoker interceptorInvoker = interceptorBuilder.createInvoker(managedBean);

            // This is the object passed back to the caller.
            callerObject = (T) interceptorInvoker.getProxy();

            Object[] interceptorInstances = interceptorInvoker.getInterceptorInstances();

            inject(managedBean, managedBeanDescriptor);

            // Inject interceptor instances
            for (Object interceptorInstance : interceptorInstances) {
                inject(interceptorInstance, managedBeanDescriptor);
            }

            interceptorInvoker.invokePostConstruct();

            managedBeanDescriptor.addBeanInstanceInfo(managedBean, interceptorInvoker);
        }

        return callerObject;
    }

    @Override
    public boolean isManagedBean(Object object) {
        JavaEEInterceptorBuilderFactory interceptorBuilderFactory = serviceLocator.getService(JavaEEInterceptorBuilderFactory.class);
        if (interceptorBuilderFactory == null) {
            return false;
        }

        return interceptorBuilderFactory.isClientProxy(object);
    }

    private void inject(Object instance, ManagedBeanDescriptor managedBeanDesc) throws Exception {
        BundleDescriptor bundle = managedBeanDesc.getBundle();

        if (bundle instanceof EjbBundleDescriptor || bundle instanceof ApplicationClientDescriptor) {
            injectionManager.injectInstance(instance, managedBeanDesc.getGlobalJndiName(), false);
        } else {
            // Inject instances, but use injection invoker for PostConstruct
            injectionManager.injectInstance(instance, (JndiNameEnvironment) bundle, false);
        }
    }

    @Override
    public void destroyManagedBean(Object managedBean) {
        destroyManagedBean(managedBean, true);
    }

    @Override
    public void destroyManagedBean(Object managedBean, boolean validate) {
        BundleDescriptor bundle = getBundle();

        CDIService cdiService = serviceLocator.getService(CDIService.class);

        if (cdiService != null && cdiService.isCDIEnabled(bundle)) {
            Map<Object, CDIInjectionContext> bundleNonManagedObjs = cdiManagedBeanInstanceMap.get(bundle);

            // In a failure scenario it's possible that bundleNonManagedObjs is null
            if (bundleNonManagedObjs == null) {
                if (validate) {
                    throw new IllegalStateException(
                        "Unknown CDI-enabled managed bean " + managedBean + " of class " + managedBean.getClass());
                }
                LOG.log(FINE,
                    "Unknown CDI-enabled managed bean " + managedBean + " of class " + managedBean.getClass());
            } else {
                CDIInjectionContext context = bundleNonManagedObjs.remove(managedBean);
                if (context == null) {
                    if (validate) {
                        throw new IllegalStateException(
                            "Unknown CDI-enabled managed bean " + managedBean + " of class " + managedBean.getClass());
                    }
                    LOG.log(FINE,
                        "Unknown CDI-enabled managed bean " + managedBean + " of class " + managedBean.getClass());
                    return;
                }

                // Call PreDestroy and cleanup
                context.cleanup(true);
            }
        } else {
            Object managedBeanInstance = null;

            try {
                Field proxyField = managedBean.getClass().getDeclaredField("__ejb31_delegate");

                doPrivileged(new java.security.PrivilegedExceptionAction<Object>() {
                    @Override
                    public Object run() throws Exception {
                        if (!proxyField.trySetAccessible()) {
                            throw new InaccessibleObjectException("Unable to make accessible: " + proxyField);
                        }
                        return null;
                    }
                });
                Proxy proxy = (Proxy) proxyField.get(managedBean);
                InterceptorInvoker invoker = (InterceptorInvoker) Proxy.getInvocationHandler(proxy);
                managedBeanInstance = invoker.getTargetInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("invalid managed bean " + managedBean, e);
            }

            ManagedBeanDescriptor managedBeanDescriptor = bundle.getManagedBeanByBeanClass(managedBeanInstance.getClass().getName());

            if (managedBeanDescriptor == null) {
                throw new IllegalStateException("Could not retrieve managed bean descriptor for " + managedBean
                    + " of class " + managedBean.getClass());
            }

            InterceptorInvoker invoker = (InterceptorInvoker) managedBeanDescriptor.getSupportingInfoForBeanInstance(managedBeanInstance);

            try {
                invoker.invokePreDestroy();
            } catch (Exception e) {
                LOG.log(FINE, "Managed bean " + managedBeanDescriptor.getBeanClassName() + " PreDestroy", e);
            }

            managedBeanDescriptor.clearBeanInstanceInfo(managedBeanInstance);
        }
    }

    private BundleDescriptor getBundle() {
        JndiNameEnvironment jndiNameEnvironment = serviceLocator.getService(ComponentEnvManager.class).getCurrentJndiNameEnvironment();

        BundleDescriptor bundle = null;

        if (jndiNameEnvironment instanceof BundleDescriptor) {
            bundle = (BundleDescriptor) jndiNameEnvironment;
        } else if (jndiNameEnvironment instanceof EjbDescriptor) {
            bundle = (BundleDescriptor) ((EjbDescriptor) jndiNameEnvironment).getEjbBundleDescriptor().getModuleDescriptor()
                    .getDescriptor();
        }

        if (bundle == null) {
            throw new IllegalStateException("Invalid context for managed bean creation");
        }

        return bundle;
    }
}
