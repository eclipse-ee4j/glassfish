/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.container.common.impl.util;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT_ENV;

import com.sun.enterprise.container.common.spi.CDIService;
import com.sun.enterprise.container.common.spi.ManagedBeanManager;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.InjectionCapable;
import com.sun.enterprise.deployment.InjectionInfo;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import jakarta.inject.Inject;
import java.lang.System.Logger;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import javax.naming.NamingException;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Implementation of InjectionManager.
 *
 * @author Kenneth Saks
 */
@Service
public class InjectionManagerImpl implements InjectionManager, PostConstruct {

    private static final Logger LOG = System.getLogger(InjectionManagerImpl.class.getName());

    @Inject
    private ComponentEnvManager componentEnvManager;

    @Inject
    private InvocationManager invocationManager;

    @Inject
    private GlassfishNamingManager glassfishNamingManager;

    @Inject
    private ServiceLocator serviceLocator;

    @Inject
    private ProcessEnvironment processEnvironment;

    @Override
    public void postConstruct() {
        // When in the server, register in JNDI to allow container code without
        // compile-time dependency on GlassFish to use injection services.
        // We know GlassFishNaming manager is available because it's an injected field.

        if (processEnvironment.getProcessType().isServer()) {
            try {
                glassfishNamingManager.publishObject(
                    new SimpleJndiName(InjectionManager.class.getName()),
                    this, true);
            } catch (NamingException ne) {
                throw new RuntimeException(ne);
            }
        }

    }

    @Override
    public void injectInstance(Object instance) throws InjectionException {
        injectInstance(instance, true);
    }

    @Override
    public void injectInstance(Object instance, boolean invokePostConstruct) throws InjectionException {
        ComponentInvocation currentInvocation = invocationManager.getCurrentInvocation();
        if (currentInvocation == null) {
            throw new InjectionException("Null invocation context");
        }

        JndiNameEnvironment componentEnv = componentEnvManager.getJndiNameEnvironment(currentInvocation.getComponentId());
        if (componentEnv == null) {
            throw new InjectionException("No descriptor registered for current invocation: " + currentInvocation);
        }

        inject(instance.getClass(), instance, componentEnv, null, invokePostConstruct);
    }

    @Override
    public void injectInstance(Object instance, JndiNameEnvironment componentEnv) throws InjectionException {
        inject(instance.getClass(), instance, componentEnv, null, true);
    }

    @Override
    public void injectInstance(Object instance, JndiNameEnvironment componentEnv, boolean invokePostConstruct) throws InjectionException {
        inject(instance.getClass(), instance, componentEnv, null, invokePostConstruct);
    }

    @Override
    public void injectInstance(Object instance, SimpleJndiName jndiName, boolean invokePostConstruct) throws InjectionException {
        ComponentInvocation currentInvocation = invocationManager.getCurrentInvocation();
        if (currentInvocation == null) {
            throw new InjectionException("Null invocation context");
        }

        String componentId = jndiName == null ? null : jndiName.toString();
        JndiNameEnvironment componentEnv = componentEnvManager.getJndiNameEnvironment(componentId);
        if (componentEnv == null) {
            throw new InjectionException("No descriptor registered for componentId: " + jndiName);
        }

        inject(instance.getClass(), instance, componentEnv, componentId, invokePostConstruct);
    }

    @Override
    public void injectClass(Class<?> clazz, SimpleJndiName jndiName, boolean invokePostConstruct) throws InjectionException {
        ComponentInvocation currentInvocation = invocationManager.getCurrentInvocation();
        if (currentInvocation == null) {
            throw new InjectionException("Null invocation context");
        }

        String componentId = jndiName == null ? null : jndiName.toString();
        JndiNameEnvironment componentEnv = componentEnvManager.getJndiNameEnvironment(componentId);
        if (componentEnv == null) {
            throw new InjectionException("No descriptor registered for jndiName: " + jndiName);
        }

        injectClass(clazz, componentEnv, invokePostConstruct);
    }

    @Override
    public void injectClass(Class<?> clazz, JndiNameEnvironment componentEnv) throws InjectionException {
        injectClass(clazz, componentEnv, true);
    }

    @Override
    public void injectClass(Class<?> clazz, JndiNameEnvironment componentEnv, boolean invokePostConstruct) throws InjectionException {
        inject(clazz, null, componentEnv, null, invokePostConstruct);
    }

    @Override
    public void invokeInstancePreDestroy(Object instance, JndiNameEnvironment componentEnv) throws InjectionException {
        invokePreDestroy(instance.getClass(), instance, componentEnv);
    }

    @Override
    public void invokeInstancePostConstruct(Object instance, JndiNameEnvironment componentEnv) throws InjectionException {
        invokePostConstruct(instance.getClass(), instance, componentEnv);
    }

    @Override
    public void invokeInstancePreDestroy(Object instance) throws InjectionException {
        invokeInstancePreDestroy(instance, true);
    }

    @Override
    public void invokeInstancePreDestroy(Object instance, boolean validate) throws InjectionException {
        ComponentInvocation currentInvocation = invocationManager.getCurrentInvocation();
        LOG.log(DEBUG, "invokeInstancePreDestroy(instance={0}, validate={1}); invocation={2}", instance, validate,
            currentInvocation);

        // if ComponentInv is null and validate is true, throw InjectionException;
        // if component JndiNameEnvironment is null and validate is true, throw InjectionException;
        // if validate is false, the above 2 null conditions are basically ignored,
        // except that when fine logging is enabled, fine-log a message.
        if (currentInvocation == null) {
            if (validate) {
                throw new InjectionException("Null invocation context");
            }
            return;
        }
        String componentId = currentInvocation.getComponentId();
        JndiNameEnvironment componentEnv = componentEnvManager.getJndiNameEnvironment(componentId);
        if (componentEnv == null) {
            if (validate) {
                throw new InjectionException("No descriptor registered for current invocation: " + currentInvocation);
            }
            return;
        }

        invokePreDestroy(instance.getClass(), instance, componentEnv);
    }

    @Override
    public void invokeClassPreDestroy(Class clazz, JndiNameEnvironment componentEnv) throws InjectionException {
        invokePreDestroy(clazz, null, componentEnv);
    }

    /**
     * Create a managed object for the given class. The object will be injected and any PostConstruct methods will be
     * called. The returned object can be cast to the clazz type but is not necessarily a direct reference to the managed
     * instance. All invocations on the returned object should be on its public methods.
     *
     * It is the responsibility of the caller to destroy the returned object by calling destroyManagedObject(Object
     * managedObject).
     *
     * @param clazz Class to be instantiated
     * @return managed object
     * @throws InjectionException
     */
    @Override
    public <T> T createManagedObject(Class<T> clazz) throws InjectionException {
        LOG.log(DEBUG, "createManagedObject(clazz={0})", clazz);
        T managedObject = null;

        try {
            ManagedBeanManager managedBeanManager = serviceLocator.getService(ManagedBeanManager.class);
            CDIService cdiService = serviceLocator.getService(CDIService.class);

            if (cdiService != null && cdiService.isCurrentModuleCDIEnabled()) {

                // Create , inject, and call PostConstruct via managed bean manager
                managedObject = managedBeanManager.createManagedBean(clazz);
            } else {

                // Not in a CDI-enabled module so just instantiate using new and perform injection
                managedObject = clazz.getConstructor().newInstance();

                // Inject and call PostConstruct
                injectInstance(managedObject);
            }
        } catch (Exception e) {
            throw new InjectionException("Error creating managed object for class: " + clazz, e);
        }

        return managedObject;
    }

    /**
     * Create a managed object for the given class. The object will be injected and if invokePostConstruct is true,
     * any @PostConstruct methods on the instance's class(and super-classes) will be invoked after injection. The returned
     * object can be cast to the clazz type but is not necessarily a direct reference to the managed instance. All
     * invocations on the returned object should be on its public methods.
     *
     * It is the responsibility of the caller to destroy the returned object by calling destroyManagedObject(Object
     * managedObject).
     *
     * @param clazz Class to be instantiated
     * @param invokePostConstruct if true, invoke any @PostConstruct methods on the instance's class(and super-classes)
     * after injection.
     * @return managed object
     * @throws InjectionException
     */
    @Override
    public <T> T createManagedObject(Class<T> clazz, boolean invokePostConstruct) throws InjectionException {
        LOG.log(DEBUG, "createManagedObject(clazz={0}, invokePostConstruct={1})", clazz, invokePostConstruct);
        T managedObject = null;

        try {
            ManagedBeanManager managedBeanMgr = serviceLocator.getService(ManagedBeanManager.class);
            CDIService cdiService = serviceLocator.getService(CDIService.class);

            if (cdiService != null && cdiService.isCurrentModuleCDIEnabled()) {

                // Create , inject, and call PostConstruct (if necessary) via managed bean manager
                managedObject = managedBeanMgr.createManagedBean(clazz, invokePostConstruct);

            } else {
                // Not in a CDI-enabled module and not annoated with @ManagedBean, so
                // just instantiate using new and perform injection
                managedObject = clazz.getConstructor().newInstance();

                // Inject and call PostConstruct if necessary
                injectInstance(managedObject, invokePostConstruct);
            }

        } catch (Exception e) {
            throw new InjectionException("Error creating managed object for class: " + clazz, e);
        }

        return managedObject;
    }

    /**
     * Destroy a managed object that was created via createManagedObject. Any PreDestroy methods will be called.
     *
     * @param managedObject
     * @throws InjectionException
     */
    @Override
    public void destroyManagedObject(Object managedObject) throws InjectionException {

        destroyManagedObject(managedObject, true);
    }

    /**
     * Destroy a managed object that was created via createManagedObject. Any PreDestroy methods will be called.
     *
     * @param managedObject
     * @param validate if false, do nothing if the instance is not registered
     * @throws InjectionException
     */
    @Override
    public void destroyManagedObject(Object managedObject, boolean validate) throws InjectionException {
        LOG.log(DEBUG, "destroyManagedObject(managedObject={0}, validate={1})", managedObject, validate);

        ManagedBeanManager managedBeanManager = serviceLocator.getService(ManagedBeanManager.class);
        CDIService cdiService = serviceLocator.getService(CDIService.class);

        if (cdiService != null && cdiService.isCurrentModuleCDIEnabled()) {

            // If CDI-enabled always delegate to managed bean manager
            managedBeanManager.destroyManagedBean(managedObject, validate);

        } else {
            // Ask managed bean manager.
            if (managedBeanManager.isManagedBean(managedObject)) {
                managedBeanManager.destroyManagedBean(managedObject, validate);
            } else {
                invokeInstancePreDestroy(managedObject, validate);
            }
        }

    }

    /**
     * @param instance Target instance for injection, or null if injection is class-based. Any error encountered during any
     * portion of injection is propagated immediately.
     */
    @Override
    public <T> void inject(final Class<? extends T> clazz, final T instance, JndiNameEnvironment envDescriptor, String componentId,
        boolean invokePostConstruct) throws InjectionException {
        LOG.log(DEBUG, "inject(clazz={0}, instance={1}, envDescriptor, componentId={2}, invokePostConstruct={3})",
            clazz, instance, componentId, invokePostConstruct);
        LinkedList<Method> postConstructMethods = new LinkedList<>();

        Class<?> nextClass = clazz;

        // Process each class in the inheritance hierarchy, starting with
        // the most derived class and ignoring java.lang.Object.
        while (nextClass != Object.class && nextClass != null) {

            InjectionInfo injInfo = envDescriptor.getInjectionInfoByClass(nextClass);
            if (!injInfo.getInjectionResources().isEmpty()) {
                _inject(nextClass, instance, componentId, injInfo.getInjectionResources());
            }

            if (invokePostConstruct) {
                if (injInfo.getPostConstructMethodName() != null) {

                    Method postConstructMethod = getPostConstructMethod(injInfo, nextClass);

                    // Delay calling post construct methods until all
                    // dependency injection within the hierarchy has been
                    // completed. Then, invoke the methods starting from
                    // the least-derived class downward.
                    postConstructMethods.addFirst(postConstructMethod);
                }
            }
            nextClass = nextClass.getSuperclass();
        }

        for (Method postConstructMethod : postConstructMethods) {
            invokeLifecycleMethod(postConstructMethod, instance);
        }
    }

    /**
     * @param instance Target instance for preDestroy, or null if class-based.
     */
    private void invokePreDestroy(final Class<?> clazz, final Object instance, JndiNameEnvironment envDescriptor) throws InjectionException {
        LOG.log(TRACE, "invokePreDestroy(clazz={0}, instance, envDescriptor.class={1})", clazz, envDescriptor.getClass());
        LinkedList<Method> preDestroyMethods = new LinkedList<>();

        Class<?> nextClass = clazz;

        // Process each class in the inheritance hierarchy, starting with
        // the most derived class and ignoring java.lang.Object.
        while ((nextClass != Object.class) && (nextClass != null)) {

            InjectionInfo injInfo = envDescriptor.getInjectionInfoByClass(nextClass);

            if (injInfo.getPreDestroyMethodName() != null) {

                Method preDestroyMethod = getPreDestroyMethod(injInfo, nextClass);

                // Invoke the preDestroy methods starting from
                // the least-derived class downward.
                preDestroyMethods.addFirst(preDestroyMethod);
            }

            nextClass = nextClass.getSuperclass();
        }

        for (Method preDestroyMethod : preDestroyMethods) {

            invokeLifecycleMethod(preDestroyMethod, instance);

        }

    }

    /**
     * @param instance Target instance for postConstruct, or null if class-based.
     */
    private void invokePostConstruct(final Class<?> clazz, final Object instance, JndiNameEnvironment envDescriptor)
            throws InjectionException {
        LinkedList<Method> postConstructMethods = new LinkedList<>();

        Class<?> nextClass = clazz;

        // Process each class in the inheritance hierarchy, starting with
        // the most derived class and ignoring java.lang.Object.
        while (nextClass != Object.class && nextClass != null) {

            InjectionInfo injInfo = envDescriptor.getInjectionInfoByClass(nextClass);

            if (injInfo.getPostConstructMethodName() != null) {

                Method postConstructMethod = getPostConstructMethod(injInfo, nextClass);

                // Invoke the postConstruct methods starting from
                // the least-derived class downward.
                postConstructMethods.addFirst(postConstructMethod);
            }

            nextClass = nextClass.getSuperclass();
        }

        for (Method postConstructMethod : postConstructMethods) {
            invokeLifecycleMethod(postConstructMethod, instance);
        }
    }

    /**
     * Internal injection operation. componentId is only specified if componentId-specific lookup operation should be used.
     */
    private void _inject(final Class<?> clazz, final Object instance, String componentId, List<InjectionCapable> injectableResources)
            throws InjectionException {
        LOG.log(TRACE, "_inject(clazz={0}, instance={1}, componentId={2}, injectableResources.size={0})", clazz,
            instance, componentId, injectableResources.size());

        for (InjectionCapable next : injectableResources) {

            try {

                SimpleJndiName lookupName = next.getComponentEnvName();
                if (!lookupName.hasJavaPrefix()) {
                    lookupName = new SimpleJndiName(JNDI_CTX_JAVA_COMPONENT_ENV + lookupName);
                }

                final Object value = componentId == null
                    ? glassfishNamingManager.lookup(lookupName)
                    : glassfishNamingManager.lookup(componentId, lookupName);

                // there still could be 2 injection on the same class, better
                // do a loop here
                for (InjectionTarget target : next.getInjectionTargets()) {

                    // if target class is not the class we are injecting
                    // we can just jump to the next target
                    if (!clazz.getName().equals(target.getClassName())) {
                        continue;
                    }

                    if (target.isFieldInjectable()) {
                        final Field f = getField(target, clazz);
                        if (Modifier.isStatic(f.getModifiers()) && instance != null) {
                            throw new InjectionException(
                                "Illegal use of static field on class that only supports instance-based injection: "
                                    + f);
                        }

                        if (instance == null && !Modifier.isStatic(f.getModifiers())) {
                            throw new InjectionException(MessageFormat.format(
                                "Injected field: {0} on Application Client class: {1} must be declared static", f,
                                clazz));
                        }

                        LOG.log(DEBUG, "Injecting dependency with logical name: {0} into field: {1} on class: {2}",
                            next.getComponentEnvName(), f, clazz);

                        // Wrap actual value insertion in doPrivileged to
                        // allow for private/protected field access.
                        if (System.getSecurityManager() != null) {
                            java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction() {
                                @Override
                                public java.lang.Object run() throws Exception {
                                    f.set(instance, value);
                                    return null;
                                }
                            });
                        } else {
                            f.set(instance, value);
                        }
                    } else if (target.isMethodInjectable()) {

                        final Method method = getMethod(next, target, clazz);

                        if (Modifier.isStatic(method.getModifiers()) && (instance != null)) {
                            throw new InjectionException(
                                "Illegal use of static method on class that only supports instance-based injection: "
                                    + method);
                        }

                        if (instance == null && !Modifier.isStatic(method.getModifiers())) {
                            throw new InjectionException(MessageFormat.format(
                                "Injected method: {0} on Application Client class: {1} must be declared static", method,
                                clazz));
                        }

                        LOG.log(DEBUG, "Injecting dependency with logical name: {0} into method: {1} on class: {2}",
                            next.getComponentEnvName(), method, clazz);

                        if (System.getSecurityManager() != null) {
                            // Wrap actual value insertion in doPrivileged to
                            // allow for private/protected field access.
                            java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction() {
                                @Override
                                public java.lang.Object run() throws Exception {
                                    method.invoke(instance, value);
                                    return null;
                                }
                            });
                        } else {
                            method.invoke(instance, value);
                        }

                    }
                }
            } catch (Throwable t) {
                Throwable cause = (t instanceof InvocationTargetException) ? ((InvocationTargetException) t).getCause() : t;
                String msg = MessageFormat.format("Exception attempting to inject {0} into {1}: {2}", next, clazz,
                    cause.getMessage());
                throw new InjectionException(msg, cause);
            }
        }
    }

    private void invokeLifecycleMethod(final Method lifecycleMethod, final Object instance) throws InjectionException {
        LOG.log(DEBUG, "Calling lifecycle method: {0} on class: {1}", lifecycleMethod,
            lifecycleMethod.getDeclaringClass());

        try {
            // Wrap actual value insertion in doPrivileged to
            // allow for private/protected field access.
            java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction() {
                @Override
                public java.lang.Object run() throws Exception {
                    if (!lifecycleMethod.trySetAccessible()) {
                        throw new InaccessibleObjectException("Unable to make accessible: " + lifecycleMethod);
                    }
                    lifecycleMethod.invoke(instance);
                    return null;
                }
            });
        } catch (Throwable t) {
            InjectionException ie = new InjectionException(
                "Exception attempting invoke lifecycle method: " + lifecycleMethod);
            Throwable cause = (t instanceof InvocationTargetException) ? ((InvocationTargetException) t).getCause() : t;
            ie.initCause(cause);
            throw ie;
        }
    }

    private Field getField(InjectionTarget target, Class resourceClass) throws Exception {

        Field f = target.getField();

        if (f == null) {
            try {
                // Check for the given field within the resourceClass only.
                // This does not include super-classes of this class.
                f = resourceClass.getDeclaredField(target.getFieldName());

                final Field finalF = f;
                java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction() {
                    @Override
                    public java.lang.Object run() throws Exception {
                        if (!finalF.trySetAccessible()) {
                            throw new InaccessibleObjectException("Unable to make accessible: " + finalF);
                        }
                        return null;
                    }
                });

            } catch (java.lang.NoSuchFieldException nsfe) {
            }

            if (f != null) {
                target.setField(f);
            }
        }

        if (f == null) {
            throw new Exception(MessageFormat.format("InjectionManager exception.  Field: {0} not found in class: {1}",
                target.getFieldName(), resourceClass));
        }

        return f;
    }

    private Method getMethod(InjectionCapable resource, InjectionTarget target, Class resourceClass) throws Exception {
        Method m = target.getMethod();

        if (m == null) {
            // Check for the method within the resourceClass only.
            // This does not include super-classses.
            for (Method next : resourceClass.getDeclaredMethods()) {
                // Overloading is not supported for setter injection
                // methods, so matching on method-name is sufficient.
                if (next.getName().equals(target.getMethodName())) {
                    m = next;
                    target.setMethod(m);

                    final Method finalM = m;
                    java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction() {
                        @Override
                        public java.lang.Object run() throws Exception {
                            if (!finalM.trySetAccessible()) {
                                throw new InaccessibleObjectException("Unable to make accessible: " + finalM);
                            }
                            return null;
                        }
                    });

                    break;
                }
            }
        }

        if (m == null) {
            throw new Exception(
                MessageFormat.format("InjectionManager exception.  Method: void {0} ({1}) not found in class: {2}",
                    target.getMethodName(), resource.getInjectResourceType(), resourceClass));
        }

        return m;
    }

    private Method getPostConstructMethod(InjectionInfo injInfo, Class resourceClass) throws InjectionException {

        Method m = injInfo.getPostConstructMethod();

        if (m == null) {
            String postConstructMethodName = injInfo.getPostConstructMethodName();

            // Check for the method within the resourceClass only.
            // This does not include super-classes.
            for (Method next : resourceClass.getDeclaredMethods()) {
                // InjectionManager only handles injection into PostConstruct
                // methods with no arguments.
                if (next.getName().equals(postConstructMethodName) && (next.getParameterTypes().length == 0)) {
                    m = next;
                    injInfo.setPostConstructMethod(m);
                    break;
                }
            }
        }

        if (m == null) {
            throw new InjectionException(
                MessageFormat.format("InjectionManager exception. PostConstruct method: {0} not found in class: {1}",
                    injInfo.getPostConstructMethodName(), injInfo.getClassName()));
        }

        return m;
    }

    private Method getPreDestroyMethod(InjectionInfo injInfo, Class resourceClass) throws InjectionException {
        Method preDestroyMethod = injInfo.getPreDestroyMethod();

        if (preDestroyMethod == null) {
            String preDestroyMethodName = injInfo.getPreDestroyMethodName();

            // Check for the method within the resourceClass only.
            // This does not include super-classses.
            for (Method next : resourceClass.getDeclaredMethods()) {
                // InjectionManager only handles injection into PreDestroy
                // methods with no arguments.
                if (next.getName().equals(preDestroyMethodName) && (next.getParameterTypes().length == 0)) {
                    preDestroyMethod = next;
                    injInfo.setPreDestroyMethod(preDestroyMethod);
                    break;
                }
            }
        }

        if (preDestroyMethod == null) {
            throw new InjectionException(
                MessageFormat.format("InjectionManager exception. PreDestroy method: {0} not found in class: {1}",
                    injInfo.getPreDestroyMethodName(), injInfo.getClassName()));
        }

        return preDestroyMethod;
    }

}
