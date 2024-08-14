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

import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.annotation.context.EjbContext;
import com.sun.enterprise.deployment.annotation.context.EjbInterceptorContext;

import jakarta.interceptor.AroundConstruct;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling jakarta.intercept.AroundConstruct
 *
 */
@Service
@AnnotationHandlerFor(AroundConstruct.class)
public class AroundConstructHandler extends AbstractAttributeHandler {

    public AroundConstructHandler() {
    }

    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            EjbContext[] ejbContexts) throws AnnotationProcessorException {

        logger.log(Level.WARNING, "Bean class should not define AroundConstruct interceptor method");
        return getDefaultProcessedResult();
    }

    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            EjbInterceptorContext ejbInterceptorContext)
            throws AnnotationProcessorException {

        EjbInterceptor ejbInterceptor =  ejbInterceptorContext.getDescriptor();
        ejbInterceptor.addAroundConstructDescriptor(
            getAroundConstructDescriptor(ainfo));
        return getDefaultProcessedResult();
    }

    private LifecycleCallbackDescriptor getAroundConstructDescriptor(
            AnnotationInfo ainfo) {
        Method annotatedMethod = (Method) ainfo.getAnnotatedElement();
        LifecycleCallbackDescriptor aroundConstruct =
                new LifecycleCallbackDescriptor();
        aroundConstruct.setLifecycleCallbackClass(annotatedMethod.getDeclaringClass().getName());
        aroundConstruct.setLifecycleCallbackMethod(annotatedMethod.getName());
        return aroundConstruct;
    }

    /**
     * @return an array of annotation types this annotation handler would
     * require to be processed (if present) before it processes it's own
     * annotation type.
     */
    public Class<? extends Annotation>[] getTypeDependencies() {
        return getEjbAnnotationTypes();
    }

    protected boolean isDelegatee() {
        return true;
    }
}
