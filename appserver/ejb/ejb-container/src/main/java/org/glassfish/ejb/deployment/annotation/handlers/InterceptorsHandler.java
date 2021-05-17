/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.deployment.annotation.handlers;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import jakarta.ejb.PostActivate;
import jakarta.ejb.PrePassivate;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.Interceptors;

import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.annotation.context.EjbContext;
import com.sun.enterprise.deployment.annotation.context.EjbInterceptorContext;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.apf.ProcessingContext;
import org.glassfish.apf.impl.ComponentDefinition;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.InterceptorBindingDescriptor;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType;

/**
 * This handler is responsible for handling jakarta.ejb.Interceptors
 *
 */
@Service
@AnnotationHandlerFor(Interceptors.class)
public class InterceptorsHandler extends AbstractAttributeHandler {

    public InterceptorsHandler() {
    }

    protected boolean supportTypeInheritance() {
        return true;
    }

    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            EjbContext[] ejbContexts) throws AnnotationProcessorException {

        Interceptors interceptors = (Interceptors) ainfo.getAnnotation();


        EjbBundleDescriptorImpl ejbBundle =
            ((EjbDescriptor)ejbContexts[0].getDescriptor()).
                getEjbBundleDescriptor();

        // Process each of the interceptor classes.
        for(Class interceptor : interceptors.value()) {
            processInterceptorClass(interceptor, ejbBundle, ainfo);
        }

        for(EjbContext next : ejbContexts) {

            EjbDescriptor ejbDescriptor = (EjbDescriptor) next.getDescriptor();

            // Create binding information.
            InterceptorBindingDescriptor binding =
                new InterceptorBindingDescriptor();

            binding.setEjbName(ejbDescriptor.getName());

            for(Class interceptor : interceptors.value()) {
                binding.appendInterceptorClass(interceptor.getName());
            }

            if(ElementType.METHOD.equals(ainfo.getElementType())) {
                Method m = (Method) ainfo.getAnnotatedElement();
                MethodDescriptor md =
                    new MethodDescriptor(m, MethodDescriptor.EJB_BEAN);
                binding.setBusinessMethod(md);
            } else if(ElementType.CONSTRUCTOR.equals(ainfo.getElementType())) {
                                Constructor c = (Constructor) ainfo.getAnnotatedElement();
                Class cl = c.getDeclaringClass();
                Class[] ctorParamTypes = c.getParameterTypes();
                String[] parameterClassNames = (new MethodDescriptor()).getParameterClassNamesFor(null, ctorParamTypes);
                MethodDescriptor md = new MethodDescriptor(cl.getSimpleName(), null,
                        parameterClassNames, MethodDescriptor.EJB_BEAN);
                binding.setBusinessMethod(md);
            }

            // All binding information processed from annotations should go
            // before the binding information processed from the descriptors.
            // Since descriptors are processed first, always place the binding
            // info at the front.  The binding information from the descriptor
            // is ordered, but there is no prescribed order in which the
            // annotations are processed, so all that matters is that it's
            // before the descriptor bindings and that the descriptor binding
            // order is preserved.
            ejbBundle.prependInterceptorBinding(binding);
        }

        return getDefaultProcessedResult();
    }

    private void processInterceptorClass(Class interceptorClass,
            EjbBundleDescriptorImpl ejbBundle, AnnotationInfo ainfo)
        throws AnnotationProcessorException {

        Set<LifecycleCallbackDescriptor> aroundInvokeDescriptors =
            new HashSet<LifecycleCallbackDescriptor>();
        Set<LifecycleCallbackDescriptor> aroundTimeoutDescriptors =
            new HashSet<LifecycleCallbackDescriptor>();
        Set<LifecycleCallbackDescriptor> postActivateDescriptors =
            new HashSet<LifecycleCallbackDescriptor>();
        Set<LifecycleCallbackDescriptor> prePassivateDescriptors =
            new HashSet<LifecycleCallbackDescriptor>();

        ComponentDefinition cdef = new ComponentDefinition(interceptorClass);
        for(Method m : cdef.getMethods()) {
            if( m.getAnnotation(AroundInvoke.class) != null ) {
                aroundInvokeDescriptors.add(getLifecycleCallbackDescriptor(m));
            }
            if( m.getAnnotation(AroundTimeout.class) != null ) {
                aroundTimeoutDescriptors.add(getLifecycleCallbackDescriptor(m));
            }
            if( m.getAnnotation(PostActivate.class) != null ) {
                postActivateDescriptors.add(getLifecycleCallbackDescriptor(m));
            }
            if( m.getAnnotation(PrePassivate.class) != null ) {
                prePassivateDescriptors.add(getLifecycleCallbackDescriptor(m));
            }
        }

        EjbInterceptor interceptor =
            ejbBundle.getInterceptorByClassName(interceptorClass.getName());
        if (interceptor == null) {
            interceptor = new EjbInterceptor();
            interceptor.setInterceptorClassName(interceptorClass.getName());
            // Add interceptor to the set of all interceptors in the ejb-jar
            ejbBundle.addInterceptor(interceptor);
        }

        if (aroundInvokeDescriptors.size() > 0) {
            interceptor.addAroundInvokeDescriptors(aroundInvokeDescriptors);
        }

        if (aroundTimeoutDescriptors.size() > 0) {
            interceptor.addAroundTimeoutDescriptors(aroundTimeoutDescriptors);
        }

        if (postActivateDescriptors.size() > 0) {
            interceptor.addCallbackDescriptors(CallbackType.POST_ACTIVATE,
                postActivateDescriptors);
        }

        if (prePassivateDescriptors.size() > 0) {
            interceptor.addCallbackDescriptors(CallbackType.PRE_PASSIVATE,
                prePassivateDescriptors);
        }

        // process resource related annotations
        EjbInterceptorContext ejbInterceptorContext =
            new EjbInterceptorContext(interceptor);
        ProcessingContext procContext = ainfo.getProcessingContext();
        procContext.pushHandler(ejbInterceptorContext);
        procContext.getProcessor().process(
            procContext, new Class[] { interceptorClass });
        return;
    }

    /**
     * @return an array of annotation types this annotation handler would
     * require to be processed (if present) before it processes it's own
     * annotation type.
     */
    public Class<? extends Annotation>[] getTypeDependencies() {
        return getEjbAnnotationTypes();
    }

    private LifecycleCallbackDescriptor getLifecycleCallbackDescriptor(Method m) {
        LifecycleCallbackDescriptor lccDesc = new LifecycleCallbackDescriptor();
        lccDesc.setLifecycleCallbackClass(m.getDeclaringClass().getName());
        lccDesc.setLifecycleCallbackMethod(m.getName());
        return lccDesc;
    }
}
