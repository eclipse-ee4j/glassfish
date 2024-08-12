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

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.annotation.context.EjbContext;

import jakarta.interceptor.ExcludeDefaultInterceptors;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.InterceptorBindingDescriptor;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling the
 * jakarta.ejb.ExcludeDefaultInterceptors annotation.
 *
 */
@Service
@AnnotationHandlerFor(ExcludeDefaultInterceptors.class)
public class ExcludeDefaultInterceptorsHandler
    extends AbstractAttributeHandler {

    public ExcludeDefaultInterceptorsHandler() {
    }

    protected boolean supportTypeInheritance() {
        return true;
    }

    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            EjbContext[] ejbContexts) throws AnnotationProcessorException {

         EjbBundleDescriptorImpl ejbBundle =
             ((EjbDescriptor)ejbContexts[0].getDescriptor()).
                 getEjbBundleDescriptor();

         for(EjbContext next : ejbContexts) {

            EjbDescriptor ejbDescriptor = (EjbDescriptor) next.getDescriptor();

            // Create binding information.
            InterceptorBindingDescriptor binding =
                new InterceptorBindingDescriptor();

            binding.setEjbName(ejbDescriptor.getName());

            binding.setExcludeDefaultInterceptors(true);

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

            ejbBundle.prependInterceptorBinding(binding);
        }

        return getDefaultProcessedResult();
    }

    /**
     * @return an array of annotation types this annotation handler would
     * require to be processed (if present) before it processes it's own
     * annotation type.
     */
    public Class<? extends Annotation>[] getTypeDependencies() {
        return getEjbAnnotationTypes();
    }
}
