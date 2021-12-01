/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018, 2019 Payara Foundation and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.ext.ExceptionMapper;

import jakarta.annotation.Priority;
import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.inject.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InstanceBinding;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.spi.ComponentProvider;
import org.glassfish.jersey.server.spi.internal.ResourceMethodInvocationHandlerProvider;

import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.data.ModuleInfo;

import com.sun.ejb.containers.BaseContainer;
import com.sun.ejb.containers.EjbContainerUtil;
import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;

/**
 * EJB component provider.
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak
 */
@Priority(300)
public final class EjbComponentProvider implements ComponentProvider, ResourceMethodInvocationHandlerProvider {

    private static final Logger LOGGER = Logger.getLogger(EjbComponentProvider.class.getName());

    private InitialContext initialContext;
    private final List<String> libNames = new CopyOnWriteArrayList<>();

    private boolean ejbInterceptorRegistered = false;

    /**
     * HK2 factory to provide EJB components obtained via JNDI lookup.
     */
    private static class EjbFactory<T> implements Supplier<T> {

        final InitialContext ctx;
        final Class<T> clazz;
        final String beanName;
        final EjbComponentProvider ejbProvider;

        @SuppressWarnings("unchecked")
        @Override
        public T get() {
            try {
                return (T) lookup(ctx, clazz, beanName, ejbProvider);
            } catch (NamingException ex) {
                Logger.getLogger(ApplicationHandler.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        private static <T> String getBeanName(final Class<T> clazz) {
            final Stateless stateless = clazz.getAnnotation(Stateless.class);
            if (stateless != null) {
                if (stateless.name().isEmpty()) {
                    return clazz.getSimpleName();
                }
                return stateless.name();
            }
            final jakarta.ejb.Singleton singleton = clazz.getAnnotation(jakarta.ejb.Singleton.class);
            if (singleton != null) {
                if (singleton.name().isEmpty()) {
                    return clazz.getSimpleName();
                }
                return singleton.name();
            }
            return clazz.getSimpleName();
        }

        public EjbFactory(Class<T> rawType, InitialContext ctx, EjbComponentProvider ejbProvider) {
            this.clazz = rawType;
            this.ctx = ctx;
            this.ejbProvider = ejbProvider;
            this.beanName = getBeanName(rawType);
        }
    }

    /**
     * Annotations to determine EJB components.
     */
    private static final Set<String> EjbComponentAnnotations = Collections.unmodifiableSet(new HashSet<String>() {{
        add("jakarta.ejb.Stateful");
        add("jakarta.ejb.Stateless");
        add("jakarta.ejb.Singleton");
    }});

    private InjectionManager injectionManager = null;

    // ComponentProvider
    @Override
    public void initialize(final InjectionManager injectionManager) {
        this.injectionManager = injectionManager;

        InstanceBinding<EjbComponentProvider> descriptor = Bindings.service(EjbComponentProvider.this)
                .to(ResourceMethodInvocationHandlerProvider.class);
        this.injectionManager.register(descriptor);
    }

    private ApplicationInfo getApplicationInfo(EjbContainerUtil ejbUtil) throws NamingException {
        ApplicationRegistry appRegistry = ejbUtil.getServices().getService(ApplicationRegistry.class);
        Applications applications = ejbUtil.getServices().getService(Applications.class);
        String appNamePrefix = (String) initialContext.lookup("java:app/AppName");
        Set<String> appNames = appRegistry.getAllApplicationNames();
        Set<String> disabledApps = new TreeSet<>();
        for (String appName : appNames) {
            if (appName.startsWith(appNamePrefix)) {
                Application appDesc = applications.getApplication(appName);
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
        Iterator<String> it = disabledApps.iterator();
        String lastDisabledApp = null;
        while (it.hasNext()) {
            lastDisabledApp = it.next();
        }
        if (lastDisabledApp != null) {
            return ejbUtil.getDeployment().get(lastDisabledApp);
        }

        throw new NamingException("Application Information Not Found");
    }

    private void registerEjbInterceptor(Class<?> component) {
        try {
            final Object interceptor = new EjbComponentInterceptor(injectionManager);
            initialContext = getInitialContext();
            final EjbContainerUtil ejbUtil = EjbContainerUtilImpl.getInstance();
            final ApplicationInfo appInfo = getApplicationInfo(ejbUtil);
            for (ModuleInfo moduleInfo : appInfo.getModuleInfos()) {
                final String jarName = moduleInfo.getName();
                if (jarName.endsWith(".jar") || jarName.endsWith(".war")) {
                    final String moduleName = jarName.substring(0, jarName.length() - 4);
                    final Object bundleDescriptor = moduleInfo.getMetaData(EjbBundleDescriptorImpl.class.getName());
                    if (bundleDescriptor instanceof EjbBundleDescriptorImpl) {
                        final Collection<EjbDescriptor> ejbs = ((EjbBundleDescriptorImpl) bundleDescriptor).getEjbs();

                        for (final EjbDescriptor ejb : ejbs) {
                            final BaseContainer ejbContainer = EjbContainerUtilImpl.getInstance().getContainer(ejb.getUniqueId());
                            if (ejbContainer.getEJBClass() != component) {
                                continue;
                            }
                            libNames.add(moduleName);
                            try {
                                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                                    @Override
                                    public Object run() throws Exception {
                                        final Method registerInterceptorMethod =
                                                BaseContainer.class
                                                        .getDeclaredMethod("registerSystemInterceptor", java.lang.Object.class);
                                        registerInterceptorMethod.setAccessible(true);

                                        registerInterceptorMethod.invoke(ejbContainer, interceptor);
                                        return null;
                                    }
                                });
                            } catch (PrivilegedActionException pae) {
                                final Throwable cause = pae.getCause();
                                LOGGER.log(Level.WARNING,
                                    "Could not bind EJB intercetor for class " + ejb.getEjbClassName() + ".", cause);
                            }
                        }
                    }
                }
            }

            final Object interceptorBinder = initialContext.lookup("java:org.glassfish.ejb.container.interceptor_binding_spi");
            // Some implementations of InitialContext return null instead of
            // throwing NamingException if there is no Object associated with
            // the name
            if (interceptorBinder == null) {
                throw new IllegalStateException(
                    "The EJB interceptor binding API is not available. JAX-RS EJB integration can not be supported.");
            }

            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    @Override
                    public Object run() throws Exception {
                        Method interceptorBinderMethod = interceptorBinder.getClass()
                                .getMethod("registerInterceptor", java.lang.Object.class);

                        interceptorBinderMethod.invoke(interceptorBinder, interceptor);
                        EjbComponentProvider.this.ejbInterceptorRegistered = true;
                        LOGGER.log(Level.CONFIG,
                            "The Jersey EJB interceptor is bound. JAX-RS EJB integration support is enabled.");
                        return null;
                    }
                });
            } catch (PrivilegedActionException pae) {
                throw new IllegalStateException(
                    "Error when configuring to use the EJB interceptor binding API. JAX-RS EJB integration can not be supported.",
                    pae.getCause());
            }

        } catch (NamingException ex) {
            throw new IllegalStateException(
                "The EJB interceptor binding API is not available. JAX-RS EJB integration can not be supported.", ex);
        } catch (LinkageError ex) {
            throw new IllegalStateException(
                "Linkage error when configuring to use the EJB interceptor binding API. JAX-RS EJB integration can not be supported.",
                ex);
        }
    }

    // ComponentProvider
    @SuppressWarnings("unchecked")
    @Override
    public boolean bind(Class<?> component, Set<Class<?>> providerContracts) {
        LOGGER.log(Level.FINE, "Class, {0}, is being checked with Jersey EJB component provider.", component);
        if (injectionManager == null) {
            throw new IllegalStateException("EJB component provider has not been initialized properly.");
        }

        if (!isEjbComponent(component)) {
            return false;
        }

        if (!ejbInterceptorRegistered) {
            registerEjbInterceptor(component);
        }

        Binding binding = Bindings.supplier(new EjbFactory(component, initialContext, EjbComponentProvider.this))
                .to(component)
                .to(providerContracts);
        injectionManager.register(binding);

        LOGGER.log(Level.CONFIG, "Class, {0}, has been bound by Jersey EJB component provider.", component);
        return true;
    }

    @Override
    public void done() {
        registerEjbExceptionMapper();
    }

    private void registerEjbExceptionMapper() {
        injectionManager.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(EjbExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class);
            }
        });
    }

    private boolean isEjbComponent(Class<?> component) {
        for (Annotation a : component.getAnnotations()) {
            if (EjbComponentAnnotations.contains(a.annotationType().getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public InvocationHandler create(Invocable method) {

        final Class<?> resourceClass = method.getHandler().getHandlerClass();

        if (resourceClass == null || !isEjbComponent(resourceClass)) {
            return null;
        }

        final Method handlingMethod = method.getDefinitionMethod();

        for (Class iFace : remoteAndLocalIfaces(resourceClass)) {
            try {
                final Method iFaceMethod = iFace.getDeclaredMethod(handlingMethod.getName(), handlingMethod.getParameterTypes());
                if (iFaceMethod != null) {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(Object target, Method ignored, Object[] args)
                                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                            return iFaceMethod.invoke(target, args);
                        }
                    };
                }
            } catch (NoSuchMethodException | SecurityException ex) {
                logLookupException(handlingMethod, resourceClass, iFace, ex);
            }
        }
        return null;
    }

    private void logLookupException(final Method method, final Class<?> component, Class<?> iFace, Exception ex) {
        LOGGER.log(Level.WARNING, MessageFormat.format(
            "Exception thrown when trying to lookup actual handling method, {0}, for EJB type, {1}, using interface {2}.",
            method, component, iFace), ex);
    }

    private static List<Class> remoteAndLocalIfaces(final Class<?> resourceClass) {
        final List<Class> allLocalOrRemoteIfaces = new LinkedList<>();
        if (resourceClass.isAnnotationPresent(Remote.class)) {
            allLocalOrRemoteIfaces.addAll(Arrays.asList(resourceClass.getAnnotation(Remote.class).value()));
        }
        if (resourceClass.isAnnotationPresent(Local.class)) {
            allLocalOrRemoteIfaces.addAll(Arrays.asList(resourceClass.getAnnotation(Local.class).value()));
        }
        for (Class<?> i : resourceClass.getInterfaces()) {
            if (i.isAnnotationPresent(Remote.class) || i.isAnnotationPresent(Local.class)) {
                allLocalOrRemoteIfaces.add(i);
            }
        }
        if (allLocalOrRemoteIfaces.isEmpty()) {
            for (Class<?> i : resourceClass.getInterfaces()) {
                if (isAcceptableLocalInterface(i)) {
                    allLocalOrRemoteIfaces.add(i);
                }
            }
        }
        return allLocalOrRemoteIfaces;
    }

    private static boolean isAcceptableLocalInterface(final Class<?> iface) {
        if ("jakarta.ejb".equals(iface.getPackage().getName())) {
            return false;
        }
        return !Serializable.class.equals(iface) && !Externalizable.class.equals(iface);
    }

    private static InitialContext getInitialContext() {
        try {
            return new InitialContext();
        } catch (Exception ex) {
            throw new IllegalStateException("InitialContext not found. JAX-RS EJB support is not available.", ex);
        }
    }

    private static Object lookup(InitialContext ic, Class<?> rawType, String name, EjbComponentProvider provider)
            throws NamingException {
        try {
            return lookupSimpleForm(ic, rawType, name, provider);
        } catch (NamingException ex) {
            LOGGER.log(Level.WARNING, "An instance of EJB class, " + rawType.getName()
                + ", could not be looked up using simple form name. Attempting to look up using the fully-qualified form name.",
                ex);
            return lookupFullyQualifiedForm(ic, rawType, name, provider);
        }
    }

    private static Object lookupSimpleForm(
            InitialContext ic,
            Class<?> rawType,
            String name,
            EjbComponentProvider provider) throws NamingException {
        if (provider.libNames.isEmpty()) {
            String jndiName = "java:module/" + name;
            return ic.lookup(jndiName);
        } else {
            NamingException ne = null;
            for (String moduleName : provider.libNames) {
                String jndiName = "java:app/" + moduleName + "/" + name;
                Object result;
                try {
                    result = ic.lookup(jndiName);
                    if (result != null && isLookupInstanceValid(rawType, result)) {
                        return result;
                    }
                } catch (NamingException e) {
                    ne = e;
                }
            }
            throw (ne != null) ? ne : new NamingException();
        }
    }

    private static Object lookupFullyQualifiedForm(
            InitialContext ic,
            Class<?> rawType,
            String name,
            EjbComponentProvider provider) throws NamingException {
        if (provider.libNames.isEmpty()) {
            String jndiName = "java:module/" + name + "!" + rawType.getName();
            return ic.lookup(jndiName);
        } else {
            NamingException ne = null;
            for (String moduleName : provider.libNames) {
                String jndiName = "java:app/" + moduleName + "/" + name + "!" + rawType.getName();
                Object result;
                try {
                    result = ic.lookup(jndiName);
                    if (result != null && isLookupInstanceValid(rawType, result)) {
                        return result;
                    }
                } catch (NamingException e) {
                    ne = e;
                }
            }
            throw (ne != null) ? ne : new NamingException();
        }
    }

    private static boolean isLookupInstanceValid(Class<?> rawType, Object result){
        return rawType.isInstance(result)
                                || remoteAndLocalIfaces(rawType)
                                        .stream()
                                        .filter(iface -> iface.isInstance(result))
                                        .findAny()
                                        .isPresent();
    }
}
