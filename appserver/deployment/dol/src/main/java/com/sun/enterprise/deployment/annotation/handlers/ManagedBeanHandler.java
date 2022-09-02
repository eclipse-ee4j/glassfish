/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.annotation.handlers;

import com.sun.enterprise.deployment.InterceptorDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.ManagedBeanDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.annotation.context.ManagedBeanContext;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.util.TypeUtil;

import jakarta.annotation.ManagedBean;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.ExcludeClassInterceptors;
import jakarta.interceptor.Interceptors;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.apf.ProcessingContext;
import org.jvnet.hk2.annotations.Service;

@Service
@AnnotationHandlerFor(ManagedBean.class)
public class ManagedBeanHandler extends AbstractHandler {

    public ManagedBeanHandler() {
    }


    @Override
    public HandlerProcessingResult processAnnotation(AnnotationInfo element) throws AnnotationProcessorException {
        AnnotatedElementHandler aeHandler = element.getProcessingContext().getHandler();
        if (aeHandler instanceof ManagedBeanContext) {

            // Ignore @ManagedBean processing during ManagedBean class processing itself
            return getDefaultProcessedResult();
        }

        ManagedBeanDescriptor managedBeanDesc = new ManagedBeanDescriptor();

        ManagedBean resourceAn = (ManagedBean) element.getAnnotation();

        // name() is optional
        String logicalName = resourceAn.value();
        if (!logicalName.isEmpty()) {
            managedBeanDesc.setName(logicalName);
        }

        Class<?> managedBeanClass = (Class<?>) element.getAnnotatedElement();

        managedBeanDesc.setBeanClassName(managedBeanClass.getName());

        Class<?>[] classInterceptors = null;
        Map<AccessibleObject, Class<?>[]> methodLevelInterceptors = new HashMap<>();
        Map<String, InterceptorDescriptor> interceptorDescs = new HashMap<>();

        // For now, just process the jakarta.interceptor related annotations directly instead
        // of relying on the annotation framework.   All the existing jakarta.interceptor
        // handlers are very tightly coupled to ejb so it would be more work to abstract those
        // than to just process the annotations directly.  Also, do jakarta.interceptor
        // annotation processing reflectively to avoid dependency on jakarta.interceptor from
        // DOL module.

        Annotation interceptorsAnn = getClassAnnotation(managedBeanClass, Interceptors.class.getName());
        if (interceptorsAnn != null) {
            try {
                Method m = interceptorsAnn.annotationType().getDeclaredMethod("value");
                classInterceptors = (Class[]) m.invoke(interceptorsAnn);
            } catch (Exception e) {
                AnnotationProcessorException ape = new AnnotationProcessorException(e.getMessage(), element);
                ape.initCause(e);
                throw ape;
            }
        }

        Class<?> nextIntClass = managedBeanClass;
        while (nextIntClass != Object.class) {

            Method managedBeanAroundInvoke = getMethodForMethodAnnotation(nextIntClass, AroundInvoke.class.getName());
            if (managedBeanAroundInvoke != null
                && !methodOverridden(managedBeanAroundInvoke, nextIntClass, managedBeanClass)) {
                LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
                desc.setLifecycleCallbackClass(nextIntClass.getName());
                desc.setLifecycleCallbackMethod(managedBeanAroundInvoke.getName());
                managedBeanDesc.addAroundInvokeDescriptor(desc);
            }
            nextIntClass = nextIntClass.getSuperclass();
        }

        for (Method m : managedBeanClass.getMethods()) {
            processForAnnotations(element, m, methodLevelInterceptors, managedBeanDesc, managedBeanClass);
        }

        for (Constructor<?> c : managedBeanClass.getDeclaredConstructors()) {
            processForAnnotations(element, c, methodLevelInterceptors, managedBeanDesc, managedBeanClass);
        }

        if (aeHandler instanceof ResourceContainerContext) {
            ((ResourceContainerContext) aeHandler).addManagedBean(managedBeanDesc);

            // process managed bean class annotations
            ManagedBeanContext managedBeanContext = new ManagedBeanContext(managedBeanDesc);
            ProcessingContext procContext = element.getProcessingContext();
            procContext.pushHandler(managedBeanContext);
            procContext.getProcessor().process(procContext, new Class[] {managedBeanClass});
            List<InterceptorDescriptor> classInterceptorChain = new LinkedList<>();
            if (classInterceptors != null) {
                for (Class<?> i : classInterceptors) {
                    InterceptorDescriptor nextInterceptor = processInterceptor(i, managedBeanContext, procContext);
                    // Add interceptor to class-level chain
                    classInterceptorChain.add(nextInterceptor);
                    interceptorDescs.put(i.getName(), nextInterceptor);
                }
                managedBeanDesc.setClassInterceptorChain(classInterceptorChain);
            }

            for (Map.Entry<AccessibleObject, Class<?>[]> next : methodLevelInterceptors.entrySet()) {
                AccessibleObject o = next.getKey();
                Class<?>[] interceptors = next.getValue();
                boolean excludeClassInterceptors = getMethodAnnotation(o,
                    ExcludeClassInterceptors.class.getName()) != null;

                List<InterceptorDescriptor> methodInterceptorChain = excludeClassInterceptors
                    ? new LinkedList<>()
                    : new LinkedList<>(classInterceptorChain);

                for (Class<?> nextInterceptor : interceptors) {
                    InterceptorDescriptor interceptorDesc = interceptorDescs.get(nextInterceptor.getName());
                    if (interceptorDesc == null) {
                        interceptorDesc = processInterceptor(nextInterceptor, managedBeanContext, procContext);
                        interceptorDescs.put(nextInterceptor.getName(), interceptorDesc);
                    }
                    methodInterceptorChain.add(interceptorDesc);
                }

                MethodDescriptor mDesc = getMethodDescriptor(o, managedBeanClass);
                if (mDesc != null) {
                    managedBeanDesc.setMethodLevelInterceptorChain(mDesc, methodInterceptorChain);
                }
            }

        }
        return getDefaultProcessedResult();
    }


    private void processForAnnotations(AnnotationInfo element, AccessibleObject o,
        Map<AccessibleObject, Class<?>[]> methodLevelInterceptors, ManagedBeanDescriptor managedBeanDesc,
        Class<?> managedBeanClass) throws AnnotationProcessorException {
        Annotation ann = getMethodAnnotation(o, Interceptors.class.getName());
        if (ann != null) {
            try {
                Method valueM = ann.annotationType().getDeclaredMethod("value");
                methodLevelInterceptors.put(o, (Class[]) valueM.invoke(ann));
            } catch (Exception e) {
                AnnotationProcessorException ape = new AnnotationProcessorException(e.getMessage(), element);
                ape.initCause(e);
                throw ape;
            }
        } else {
            // If the method or constructor excludes
            // class-level interceptors, explicitly set method-level to an empty list.
            boolean excludeClassInterceptors = getMethodAnnotation(o, ExcludeClassInterceptors.class.getName()) != null;
            if (excludeClassInterceptors) {
                MethodDescriptor mDesc = getMethodDescriptor(o, managedBeanClass);
                if (mDesc != null) {
                    managedBeanDesc.setMethodLevelInterceptorChain(mDesc, new LinkedList<InterceptorDescriptor>());
                }
            }
        }
    }


    private MethodDescriptor getMethodDescriptor(AccessibleObject o, Class<?> managedBeanClass) {
        MethodDescriptor mDesc = null;
        if (o instanceof Method) {
            mDesc = new MethodDescriptor((Method) o);
        } else if (o instanceof Constructor) {
            Class<?>[] ctorParamTypes = ((Constructor<?>) o).getParameterTypes();
            String[] parameterClassNames = (new MethodDescriptor()).getParameterClassNamesFor(null, ctorParamTypes);
            // MethodDescriptor.EJB_BEAN is just a tag, not to add a new one
            mDesc = new MethodDescriptor(managedBeanClass.getSimpleName(), null, parameterClassNames,
                MethodDescriptor.EJB_BEAN);
        }
        return mDesc;
    }


    private InterceptorDescriptor processInterceptor(Class<?> interceptorClass, ManagedBeanContext managedBeanCtx,
        ProcessingContext procCtx) throws AnnotationProcessorException {
        InterceptorDescriptor interceptorDesc = new InterceptorDescriptor();
        interceptorDesc.setInterceptorClassName(interceptorClass.getName());

        // Redirect PostConstruct / PreDestroy methods to InterceptorDescriptor
        // during annotation processing
        managedBeanCtx.setInterceptorMode(interceptorDesc);

        // Process annotations on interceptor
        procCtx.pushHandler(managedBeanCtx);
        procCtx.getProcessor().process(procCtx, new Class[] {interceptorClass});

        managedBeanCtx.unsetInterceptorMode();

        Class<?> nextIntClass = interceptorClass;
        while (nextIntClass != Object.class) {
            Method interceptorAroundInvoke = getMethodForMethodAnnotation(nextIntClass, AroundInvoke.class.getName());
            if (interceptorAroundInvoke != null
                && !methodOverridden(interceptorAroundInvoke, nextIntClass, interceptorClass)) {

                LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
                desc.setLifecycleCallbackClass(nextIntClass.getName());
                desc.setLifecycleCallbackMethod(interceptorAroundInvoke.getName());
                interceptorDesc.addAroundInvokeDescriptor(desc);
            }

            Method interceptorAroundConstruct = getMethodForMethodAnnotation(nextIntClass, AroundConstruct.class.getName());
            if (interceptorAroundConstruct != null
                && !methodOverridden(interceptorAroundConstruct, nextIntClass, interceptorClass)) {

                LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
                desc.setLifecycleCallbackClass(nextIntClass.getName());
                desc.setLifecycleCallbackMethod(interceptorAroundConstruct.getName());
                interceptorDesc.addAroundConstructDescriptor(desc);
            }

            nextIntClass = nextIntClass.getSuperclass();
        }
        return interceptorDesc;
    }


    private Annotation getClassAnnotation(Class<?> c, String annotationClassName) {
        for (Annotation next : c.getDeclaredAnnotations()) {
            if (next.annotationType().getName().equals(annotationClassName)) {
                return next;
            }
        }
        return null;
    }


    private Method getMethodForMethodAnnotation(Class<?> c, String annotationClassName) {
        for (Method m : c.getDeclaredMethods()) {
            for (Annotation next : m.getDeclaredAnnotations()) {
                if (next.annotationType().getName().equals(annotationClassName)) {
                    return m;
                }
            }
        }
        return null;
    }


    private Annotation getMethodAnnotation(AccessibleObject o, String annotationClassName) {
        for (Annotation next : o.getDeclaredAnnotations()) {
            if (next.annotationType().getName().equals(annotationClassName)) {
                return next;
            }
        }
        return null;
    }


    private boolean methodOverridden(final Method m, final Class<?> declaringSuperClass, final Class<?> leafClass) {
        logger.log(Level.FINEST, "Checking if {0} of {1} overrides same method in {2}",
            new Object[] {m.getName(), leafClass, declaringSuperClass});
        if (Modifier.isPrivate(m.getModifiers())) {
            return false;
        }
        Class<?> nextClass = leafClass;
        while (nextClass != declaringSuperClass && nextClass != Object.class) {
            for (final Method nextMethod : nextClass.getDeclaredMethods()) {
                if (!Modifier.isPrivate(nextMethod.getModifiers()) && TypeUtil.sameMethodSignature(m, nextMethod)) {
                    logger.log(Level.FINEST, "Method {0} of {1} overrides same method in {2}",
                        new Object[] {m.getName(), leafClass, nextClass});
                    return true;
                }
            }
            nextClass = nextClass.getSuperclass();
        }
        return false;
    }
}
