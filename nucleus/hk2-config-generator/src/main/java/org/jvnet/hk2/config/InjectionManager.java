/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.api.MultiException;

/**
 * InjectionManager is responsible for injecting resources into a component. Injection targets are identified by the
 * injection resolver type attribute.
 *
 * @author Jerome Dochez
 */
@SuppressWarnings("unchecked")
public class InjectionManager {

    /**
     * Initializes the component by performing injection.
     *
     * @param component component instance to inject
     * @param onBehalfOf the inhabitant to do injection on behalf of
     * @param targets the injection resolvers to resolve all injection points
     * @throws ComponentException if injection failed for some reason.
     */
    public void inject(Object component, InjectionResolver... targets) {
        syncDoInject(component, component.getClass(), targets);
    }

    /**
     * Initializes the component by performing injection.
     *
     * @param component component instance to inject
     * @param onBehalfOf the inhabitant to do injection on behalf of
     * @param es the ExecutorService to use in order to handle the work load
     * @param targets the injection resolvers to resolve all injection points
     * @throws ComponentException if injection failed for some reason.
     */
    public void inject(Object component, ExecutorService es, InjectionResolver... targets) {
        try {
            syncDoInject(component, component.getClass(), targets);
        } catch (Exception e) {
            // We do this to bolster debugging
            if (e instanceof MultiException multiException) {
                throw multiException;
            }

            throw new MultiException(e);
        }
    }

    protected static class InjectContext {
        public final Object component;

        public final ExecutorService executorService;
        public final InjectionResolver[] targets;

        public InjectContext(final Object component, final ExecutorService es, final InjectionResolver[] targets) {
            this.component = component;
            this.executorService = es;
            this.targets = targets;
        }
    }

    /**
     * Initializes the component by performing injection.
     *
     * @param component component instance to inject
     * @param type component class
     * @param targets the injection resolvers to resolve all injection points
     * @throws ComponentException if injection failed for some reason.
     */
    public void inject(Object component, Class<?> type, InjectionResolver... targets) {
        syncDoInject(component, type, targets);
    }

    /**
     * Initializes the component by performing injection.
     *
     * @param component component instance to inject
     * @param onBehalfOf the inhabitant to do injection on behalf of
     * @param type component class
     * @param targets the injection resolvers to resolve all injection points
     * @throws ComponentException if injection failed for some reason.
     */
    protected void syncDoInject(Object component, Class<?> type, InjectionResolver... targets) {

        try {
            Class<?> currentClass = type;

            while (currentClass != null && Object.class != currentClass) {
                // get the list of the instances variable
                for (Field field : currentClass.getDeclaredFields()) {

                    Annotation nonOptionalAnnotation = null;
                    boolean injected = false;
                    for (InjectionResolver target : targets) {
                        Annotation inject = field.getAnnotation(target.type);
                        if (inject == null)
                            continue;

                        Type genericType = field.getGenericType();
                        Class<?> fieldType = field.getType();

                        try {
                            Object value = target.getValue(component, field, genericType, fieldType);
                            if (value != null) {
                                field.setAccessible(true);
                                field.set(component, value);
                                injected = true;
                                break;
                            } else {
                                if (!target.isOptional(field, inject)) {
                                    nonOptionalAnnotation = inject;
                                }
                            }
                        } catch (Exception ex) {
                            error_injectionException(target, inject, field, ex);
                        }
                    }

                    // Exhausted all injection managers,
                    if (!injected && nonOptionalAnnotation != null) {
                        throw new UnsatisfiedDependencyException(field, nonOptionalAnnotation);
                    }
                }

                for (Method method : currentClass.getDeclaredMethods()) {
                    for (InjectionResolver target : targets) {
                        Annotation inject = method.getAnnotation(target.type);
                        if (inject == null)
                            continue;

                        Method setter = target.getSetterMethod(method, inject);
                        if (setter.getReturnType() != void.class) {
                            if (Collection.class.isAssignableFrom(setter.getReturnType())) {
                                injectCollection(component, setter, target.getValue(component, method, null, setter.getReturnType()));
                                continue;
                            }

                            error_InjectMethodIsNotVoid(method);
                        }

                        Class<?>[] paramTypes = setter.getParameterTypes();
                        Type[] genericParamTypes = setter.getGenericParameterTypes();

                        if (allowInjection(method, paramTypes)) {
                            try {
                                if (1 == paramTypes.length) {
                                    Object value = target.getValue(component, method, genericParamTypes[0], paramTypes[0]);
                                    if (value != null) {
                                        setter.setAccessible(true);
                                        setter.invoke(component, value);
                                    } else {
                                        if (!target.isOptional(method, inject)) {
                                            throw new UnsatisfiedDependencyException(method, inject);
                                        }
                                    }
                                } else {
                                    // multi params
                                    setter.setAccessible(true);

                                    Type gparamType[] = setter.getGenericParameterTypes();

                                    Object params[] = new Object[paramTypes.length];
                                    for (int i = 0; i < paramTypes.length; i++) {
                                        Object value = target.getValue(component, method, gparamType[i], paramTypes[i]);
                                        if (value != null) {
                                            params[i] = value;
                                        } else {
                                            if (!target.isOptional(method, inject)) {
                                                throw new UnsatisfiedDependencyException(method, inject);
                                            }
                                        }
                                    }

                                    setter.invoke(component, params);
                                }
                            } catch (IllegalAccessException | InvocationTargetException | RuntimeException e) {
                                error_injectionException(target, inject, setter, e);
                            }
                        }
                    }
                }

                currentClass = currentClass.getSuperclass();
            }
        } catch (final LinkageError e) {
            // Reflection could trigger additional classloading and resolution, so it can cause linkage error.
            // report more information to assist diagnosis.
            // can't trust component.toString() as the object could be in an inconsistent state.
            throw new LinkageError("injection failed on " + type + " from " + type.getClassLoader(), e);
        }
    }

    protected class InjectClass implements Runnable {

        private final Class<?> classType;
        private final InjectContext injectionContext;

        public InjectClass(final Class<?> type, final InjectContext ic) {
            this.classType = type;
            this.injectionContext = ic;
        }

        @Override
        public void run() {
            WorkManager workManager = new WorkManager(injectionContext.executorService, 2);
            workManager.execute(new InjectMethods(this));
            workManager.execute(new InjectFields(this));
            workManager.awaitCompletion();

            new InjectMethods(this).run();
            new InjectFields(this).run();
        }

    }

    protected class InjectFields implements Runnable {
        private final InjectClass iClass;

        public InjectFields(InjectClass iClass) {
            this.iClass = iClass;
        }

        @Override
        public void run() {
            ArrayList<Runnable> tasks = new ArrayList<Runnable>();
            for (Field field : iClass.classType.getDeclaredFields()) {
                for (InjectionResolver target : iClass.injectionContext.targets) {
                    Annotation inject = field.getAnnotation(target.type);
                    if (inject != null) {
                        tasks.add(new InjectField(iClass, field, inject, target));
                    }
                }
            }

            WorkManager workManager = new WorkManager(iClass.injectionContext.executorService, tasks.size());
            workManager.executeAll(tasks);
            workManager.awaitCompletion();
        }
    }

    protected class InjectMethods implements Runnable {
        private final InjectClass iClass;

        public InjectMethods(InjectClass iClass) {
            this.iClass = iClass;
        }

        @Override
        public void run() {
            ArrayList<Runnable> tasks = new ArrayList<Runnable>();
            for (Method method : iClass.classType.getDeclaredMethods()) {
                for (InjectionResolver target : iClass.injectionContext.targets) {
                    Annotation inject = method.getAnnotation(target.type);
                    if (inject != null) {
                        tasks.add(new InjectMethod(iClass, method, inject, target));
                    }
                }
            }

            WorkManager workManager = new WorkManager(iClass.injectionContext.executorService, tasks.size());
            workManager.executeAll(tasks);
            workManager.awaitCompletion();
        }
    }

    protected class InjectField implements Runnable {
        private final InjectContext ic;
        private final Field field;
        private final Annotation inject;
        private final InjectionResolver target;

        public InjectField(InjectClass iClass, Field field, Annotation inject, InjectionResolver target) {
            this.ic = iClass.injectionContext;
            this.field = field;
            this.inject = inject;
            this.target = target;
        }

        @Override
        public void run() {
            Type genericType = field.getGenericType();
            Class<?> fieldType = field.getType();

            try {
                Object value = target.getValue(ic.component, field, genericType, fieldType);
                if (value != null) {
                    field.setAccessible(true);
                    field.set(ic.component, value);
                } else {
                    if (!target.isOptional(field, inject)) {
                        throw new UnsatisfiedDependencyException(field, inject);
                    }
                }
            } catch (MultiException e) {
                error_injectionException(target, inject, field, e);
            } catch (IllegalAccessException e) {
                error_injectionException(target, inject, field, e);
            } catch (RuntimeException e) {
                error_injectionException(target, inject, field, e);
            }
        }
    }

    protected class InjectMethod implements Runnable {
        private final InjectContext ic;
        private final Method method;
        private final Annotation inject;
        private final InjectionResolver target;

        public InjectMethod(InjectClass iClass, Method method, Annotation inject, InjectionResolver target) {
            this.ic = iClass.injectionContext;
            this.method = method;
            this.inject = inject;
            this.target = target;
        }

        @Override
        public void run() {
            Method setter = target.getSetterMethod(method, inject);
            if (void.class != setter.getReturnType()) {
                if (Collection.class.isAssignableFrom(setter.getReturnType())) {
                    injectCollection(ic.component, setter, target.getValue(ic.component, method, null, setter.getReturnType()));
                } else {
                    error_InjectMethodIsNotVoid(method);
                }
            }

            Class<?>[] paramTypes = setter.getParameterTypes();
            if (allowInjection(method, paramTypes)) {
                try {
                    if (paramTypes.length == 1) {
                        Object value = target.getValue(ic.component, method, null, paramTypes[0]);
                        if (value != null) {
                            setter.setAccessible(true);
                            setter.invoke(ic.component, value);
                        } else {
                            if (!target.isOptional(method, inject)) {
                                throw new UnsatisfiedDependencyException(method, inject);
                            }
                        }
                    } else {
                        // multi params
                        setter.setAccessible(true);

                        Type gparamType[] = setter.getGenericParameterTypes();
                        Object params[] = new Object[paramTypes.length];
                        for (int i = 0; i < paramTypes.length; i++) {
                            Object value = target.getValue(ic.component, method, gparamType[i], paramTypes[i]);
                            if (value != null) {
                                params[i] = value;
                            } else {
                                if (!target.isOptional(method, inject)) {
                                    throw new UnsatisfiedDependencyException(method, inject);
                                }
                            }
                        }

                        setter.invoke(ic.component, params);
                    }
                } catch (MultiException e) {
                    error_injectionException(target, inject, setter, e);
                } catch (IllegalAccessException e) {
                    error_injectionException(target, inject, setter, e);
                } catch (InvocationTargetException e) {
                    error_injectionException(target, inject, setter, e);
                } catch (RuntimeException e) {
                    error_injectionException(target, inject, setter, e);
                }
            }
        }
    }

    protected void error_injectionException(InjectionResolver target, Annotation inject, AnnotatedElement injectionPoint, Throwable e) {
        Logger.getAnonymousLogger().log(Level.FINE, "** Injection failure **", e);

        if (UnsatisfiedDependencyException.class.isInstance(e)) {
            if (injectionPoint == ((UnsatisfiedDependencyException) e).getUnsatisfiedElement()) {
                // no need to wrap again
                throw (UnsatisfiedDependencyException) e;
            }

            if (target.isOptional(injectionPoint, inject)) {
                return;
            } else {
                throw new UnsatisfiedDependencyException(injectionPoint, inject, e);
            }
        }

        if (null != e.getCause() && InvocationTargetException.class.isInstance(e)) {
            e = e.getCause();
        }

        throw new MultiException(e);
    }

    /**
     * jsr-330 rules are very forgiving.
     */
    protected boolean allowInjection(Method method, Class<?>[] paramTypes) {
        // let it all ride on black
        return true;
    }

    protected void error_InjectMethodIsNotVoid(Method method) {
        throw new MultiException(new IllegalStateException(
                "Injection failed on " + method.getName() + " : setter method is not declared with a void return type"));
    }

    private void injectCollection(Object component, Method method, Object value) {
        if (value == null) {
            return;
        }

        Collection<?> c = Collection.class.cast(value);

        @SuppressWarnings("rawtypes")
        Collection target = null;

        try {
            target = Collection.class.cast(method.invoke(component));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
            return;
        }

        target.addAll(c);
    }

}
