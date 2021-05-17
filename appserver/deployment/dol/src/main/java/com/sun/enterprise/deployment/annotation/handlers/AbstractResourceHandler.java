/*
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

import com.sun.enterprise.deployment.annotation.context.EjbBundleContext;
import com.sun.enterprise.deployment.annotation.context.EjbsContext;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.context.WebComponentsContext;

import com.sun.enterprise.deployment.EjbBundleDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * This is an abstract class encapsulate generic behaviour of resource
 * annotation.
 * Concrete subclass handlers need to implement the following:
 *     public Class&lt;? extends Annotation&gt; getAnnotationType();
 *     protected HandlerProcessingResult processAnnotation(
 *             AnnotationInfo ainfo,
 *             ResourceContainerContext[] rcContexts)
 *             throws AnnotationProcessorException;
 * It may also need to override the following if other annotations
 * need to be processed prior to given annotation:
 *     public Class&lt;? extends Annotation&gt;[] getTypeDependencies();
 *
 * @author Shing Wai Chan
 */
public abstract class AbstractResourceHandler extends AbstractHandler {
    /**
     * Process Annotation with given ResourceContainerContexts.
     * @param ainfo
     * @param rcContexts
     */
    protected abstract HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo,
            ResourceContainerContext[] rcContexts)
            throws AnnotationProcessorException;

    /**
     * Process a particular annotation which type is the same as the
     * one returned by @see getAnnotationType(). All information
     * pertinent to the annotation and its context is encapsulated
     * in the passed AnnotationInfo instance.
     *
     * @param ainfo the annotation information
     */
    public HandlerProcessingResult processAnnotation(AnnotationInfo ainfo)
            throws AnnotationProcessorException {

        AnnotatedElementHandler aeHandler = ainfo.getProcessingContext().getHandler();
        if (aeHandler instanceof EjbBundleContext) {
            EjbBundleContext ejbBundleContext = (EjbBundleContext)aeHandler;
            aeHandler = ejbBundleContext.createContextForEjb();
            if (aeHandler == null) {
                aeHandler = ejbBundleContext.createContextForEjbInterceptor();
            }

            // If it's still null and we're in an ejb-jar, use the EjbBundleContext.
            // This way we process dependencies on any classes (other than ejbs ,
            // interceptors , and their super-classes) that have annotations in case
            // we need the info for managed classes we wouldn't normally know about
            // (e.g. 299 classes).   In a .war, those are already processed during the
            // .war annotation scanning.

            EjbBundleDescriptor bundleDesc = ejbBundleContext.getDescriptor();
            RootDeploymentDescriptor enclosingBundle = bundleDesc.getModuleDescriptor().getDescriptor();

            boolean ejbJar = enclosingBundle instanceof EjbBundleDescriptor;

            if( (aeHandler == null) && ejbJar ) {
                aeHandler = ejbBundleContext;
            }


        }
        // WebBundleContext is a ResourceContainerContext.

        if (aeHandler == null) {
            // not an ejb, interceptor in ejbBundle
            return getInvalidAnnotatedElementHandlerResult(
                ainfo.getProcessingContext().getHandler(), ainfo);
        }
        ResourceContainerContext[] rcContexts = null;
        if (aeHandler instanceof EjbsContext) {
            EjbsContext ejbsContext = (EjbsContext)aeHandler;
            rcContexts = (ResourceContainerContext[])ejbsContext.getEjbContexts();
        } else if (aeHandler instanceof WebComponentsContext) {
            WebComponentsContext webCompsContext = (WebComponentsContext)aeHandler;
            rcContexts = (ResourceContainerContext[])webCompsContext.getWebComponentContexts();
        } else if (aeHandler instanceof ResourceContainerContext) {
            rcContexts = new ResourceContainerContext[] {
                    (ResourceContainerContext)aeHandler };
        } else {
            return getInvalidAnnotatedElementHandlerResult(aeHandler, ainfo);
        }

        return processAnnotation(ainfo, rcContexts);
    }

    public Class<? extends Annotation>[] getTypeDependencies() {
        return getEjbAndWebAnnotationTypes();
    }


    protected boolean isAEjbComponentClass(Annotation[] annotations) {
        Class<? extends Annotation> ejbAnnotations[] = getEjbAnnotationTypes();
        for (Annotation annotation : annotations) {
            for (Class<? extends Annotation> ejbAnnotation : ejbAnnotations) {
                if (ejbAnnotation.equals(annotation.annotationType())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isAWebComponentClass(Annotation[] annotations) {
        Class<? extends Annotation> webAnnotations[] = getWebAnnotationTypes();
        for (Annotation annotation : annotations) {
            for (Class<? extends Annotation> webAnnotation : webAnnotations) {
                if (webAnnotation.equals(annotation.annotationType())) {
                    return true;
                }
            }
        }
        return false;
    }

    // validate methods that are annotated with @PostConstruct and @PreDestroy
    // to conform the spec
    protected void validateAnnotatedLifecycleMethod(Method method) {
        Class[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 1) {
            throw new IllegalArgumentException(localStrings.getLocalString("lifecycle_method_invalid_param_size", "The lifecycle method [{0}] must not have more than one parameter", method.getName()));
        }

        if (parameterTypes.length == 0) {
            Class[] exceptionTypes = method.getExceptionTypes();
            for (Class exception : exceptionTypes) {
                 if (!RuntimeException.class.isAssignableFrom(exception)) {
                     throw new IllegalArgumentException(localStrings.getLocalString("lifecycle_method_no_checked_exception", "The lifecycle method [{0}] must not throw a checked exception", method.getName()));
                 }
            }
            Class returnType = method.getReturnType();
            if (!returnType.equals(Void.TYPE)) {
                throw new IllegalArgumentException(localStrings.getLocalString("lifecycle_method_return_type_void", "The return type of the lifecycle method [{0}] must be void", method.getName()));
            }
        }
    }
}
