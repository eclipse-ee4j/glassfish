/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018, 2019 Payara Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.gf.ejb.internal;

import com.sun.ejb.containers.BaseContainer;
import com.sun.ejb.containers.EjbContainerUtil;
import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.ejb.containers.InternalInterceptorBindingImpl;
import com.sun.ejb.containers.InternalInterceptorBindingNamingProxy;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;

import jakarta.annotation.Priority;
import jakarta.ejb.Singleton;
import jakarta.ejb.Stateful;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.ext.ExceptionMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.data.ModuleInfo;
import org.glassfish.jersey.inject.hk2.AbstractBinder;
import org.glassfish.jersey.inject.hk2.Bindings;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.spi.ComponentProvider;
import org.glassfish.jersey.server.spi.internal.ResourceMethodInvocationHandlerProvider;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP;
import static org.glassfish.jersey.gf.ejb.internal.EjbClassUtilities.getRemoteAndLocalIfaces;

/**
 * EJB component provider.
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak
 */
@Priority(300)
public final class EjbComponentProvider implements ComponentProvider, ResourceMethodInvocationHandlerProvider {

    private static final Logger LOG = Logger.getLogger(EjbComponentProvider.class.getName());
    private static final String JNDI_JAVA_APP_NAME = JNDI_CTX_JAVA_APP + "AppName";

    /**Annotations to determine EJB components. */
    private static final Set<String> EJB_ANNOTATIONS = Stream.of(Stateful.class, Stateless.class, Singleton.class)
        .map(Class::getName).collect(Collectors.toUnmodifiableSet());

    private final List<String> moduleNames = new CopyOnWriteArrayList<>();

    private InitialContext initialContext;
    private InjectionManager injectionManager;
    private boolean ejbInterceptorRegistered;

    @Override
    public void initialize(final InjectionManager injectionManager) {
        this.injectionManager = injectionManager;
        this.injectionManager.register(
            Bindings.service(EjbComponentProvider.this)
                    .to(ResourceMethodInvocationHandlerProvider.class));
    }


    @Override
    public boolean bind(final Class<?> component, final Set<Class<?>> providerContracts) {
        LOG.log(Level.FINE, "Class, {0}, is being checked with Jersey EJB component provider.", component);
        if (injectionManager == null) {
            throw new IllegalStateException("EJB component provider has not been initialized properly.");
        }
        if (!isEjbComponent(component)) {
            return false;
        }
        if (!ejbInterceptorRegistered) {
            registerEjbInterceptor(component);
        }
        registerBinding(component, providerContracts);
        LOG.log(Level.CONFIG, "Class, {0}, has been bound by Jersey EJB component provider.", component);
        return true;
    }


    @Override
    public void done() {
        injectionManager.register(new EjbComponentBinder());
    }


    @Override
    public InvocationHandler create(Invocable method) {
        final Class<?> resourceClass = method.getHandler().getHandlerClass();
        if (resourceClass == null || !isEjbComponent(resourceClass)) {
            return null;
        }
        final Method handlingMethod = method.getDefinitionMethod();
        for (Class<?> iFace : getRemoteAndLocalIfaces(resourceClass)) {
            try {
                final Method iFaceMethod = iFace.getDeclaredMethod(handlingMethod.getName(), handlingMethod.getParameterTypes());
                if (iFaceMethod != null) {
                    return (target, ignored, args) -> iFaceMethod.invoke(target, args);
                }
            } catch (NoSuchMethodException | SecurityException ex) {
                logLookupException(handlingMethod, resourceClass, iFace, ex);
            }
        }
        return null;
    }


    /**
     * @return list of application's modules.
     */
    public List<String> getModuleNames() {
        return List.copyOf(moduleNames);
    }


    private void registerEjbInterceptor(final Class<?> component) {
        try {
            initialContext = getInitialContext();
            final EjbComponentInterceptor interceptor = new EjbComponentInterceptor(injectionManager);
            final EjbContainerUtil ejbUtil = EjbContainerUtilImpl.getInstance();
            final ApplicationInfo appInfo = getApplicationInfo(ejbUtil);
            for (ModuleInfo moduleInfo : appInfo.getModuleInfos()) {
                final String fileName = moduleInfo.getName();
                if (isProbableEjbModule(fileName)) {
                    final String moduleName = fileName.substring(0, fileName.length() - 4);
                    final Object bundleDescriptor = moduleInfo.getMetaData(EjbBundleDescriptorImpl.class.getName());
                    if (bundleDescriptor instanceof EjbBundleDescriptorImpl) {
                        final Collection<EjbDescriptor> ejbs = ((EjbBundleDescriptorImpl) bundleDescriptor).getEjbs();
                        for (final EjbDescriptor ejb : ejbs) {
                            final BaseContainer ejbContainer = EjbContainerUtilImpl.getInstance().getContainer(ejb.getUniqueId());
                            if (ejbContainer.getEJBClass() != component) {
                                continue;
                            }
                            moduleNames.add(moduleName);
                            ejbContainer.registerSystemInterceptor(interceptor);
                        }
                    }
                }
            }

            final InternalInterceptorBindingImpl interceptorBinder = (InternalInterceptorBindingImpl) initialContext
                .lookup(InternalInterceptorBindingNamingProxy.INTERCEPTOR_BINDING);
            // Some implementations of InitialContext return null instead of throwing
            // NamingException if there is no Object associated with the name
            if (interceptorBinder == null) {
                throw new IllegalStateException(
                    "The EJB interceptor binding API is not available. Jersey EJB integration can not be supported.");
            }

            interceptorBinder.registerInterceptor(interceptor);
            this.ejbInterceptorRegistered = true;
            LOG.log(Level.CONFIG, "The Jersey EJB interceptor is bound. Jersey EJB integration support is enabled.");
        } catch (NamingException ex) {
            throw new IllegalStateException(
                "The EJB interceptor binding API is not available. Jersey EJB integration can not be supported.", ex);
        } catch (LinkageError ex) {
            throw new IllegalStateException("Linkage error when configuring to use the EJB interceptor binding API."
                + " Jersey EJB integration can not be supported.", ex);
        }
    }


    private boolean isProbableEjbModule(final String jarName) {
        return jarName.endsWith(".jar") || jarName.endsWith(".war");
    }


    // Jersey's API is inconsistent here, missing generics
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerBinding(final Class<?> component, final Set<Class<?>> providerContracts) {
        final EjbSupplier ejbSupplier = new EjbSupplier<>(component, initialContext, this);
        final Binding binding = Bindings.supplier(ejbSupplier).to(component).to(providerContracts);
        injectionManager.register(binding);
    }


    private ApplicationInfo getApplicationInfo(final EjbContainerUtil ejbUtil) throws NamingException {
        final ApplicationRegistry appRegistry = ejbUtil.getServices().getService(ApplicationRegistry.class);
        final Applications applications = ejbUtil.getServices().getService(Applications.class);
        final String appNamePrefix = (String) initialContext.lookup(JNDI_JAVA_APP_NAME);
        final Set<String> appNames = appRegistry.getAllApplicationNames();
        final TreeSet<String> disabledApps = new TreeSet<>();
        for (final String appName : appNames) {
            if (appName.startsWith(appNamePrefix)) {
                final Application appDesc = applications.getApplication(appName);
                if (appDesc != null && !ejbUtil.getDeployment().isAppEnabled(appDesc)) {
                    // skip disabled version of the app
                    disabledApps.add(appName);
                } else {
                    return ejbUtil.getDeployment().get(appName);
                }
            }
        }

        // grab the latest one, there is no way to make
        // sure which one the user is actually enabling,
        // so use the best case, i.e. upgrade
        final String lastDisabledApp = disabledApps.last();
        if (lastDisabledApp != null) {
            return ejbUtil.getDeployment().get(lastDisabledApp);
        }
        throw new NamingException("Application Information Not Found");
    }


    private boolean isEjbComponent(Class<?> component) {
        for (Annotation a : component.getAnnotations()) {
            if (EJB_ANNOTATIONS.contains(a.annotationType().getName())) {
                return true;
            }
        }
        return false;
    }


    private void logLookupException(final Method method, final Class<?> component, Class<?> iFace, Exception ex) {
        LOG.log(Level.WARNING, MessageFormat.format(
            "Exception thrown when trying to lookup actual handling method, {0}, for EJB type, {1}, using interface {2}.",
            method, component, iFace), ex);
    }


    private static InitialContext getInitialContext() {
        try {
            return new InitialContext();
        } catch (Exception ex) {
            throw new IllegalStateException("InitialContext not found. Jersey EJB support is not available.", ex);
        }
    }


    private static final class EjbComponentBinder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(EjbExceptionMapper.class).to(ExceptionMapper.class).in(jakarta.inject.Singleton.class);
        }
    }
}
