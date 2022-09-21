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

import com.sun.enterprise.deployment.annotation.context.ComponentContext;
import com.sun.enterprise.deployment.annotation.context.EjbBundleContext;
import com.sun.enterprise.deployment.annotation.context.EjbContext;
import com.sun.enterprise.deployment.annotation.context.EjbsContext;
import com.sun.enterprise.deployment.annotation.context.WebBundleContext;
import com.sun.enterprise.deployment.annotation.context.WebComponentContext;
import com.sun.enterprise.deployment.annotation.context.WebComponentsContext;

import java.lang.annotation.ElementType;
import java.util.logging.Level;

import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;

/**
 * This is an abstract class encapsulate generic behaviour of annotation
 * handler applying on Ejb and WebComponent Class. It will get the corresponding
 * EjbDescriptors or WebComponentDescriptor associated to the annotation on
 * the given Class and then pass it to underlying processAnnotation method.
 * Concrete subclass handlers need to implement the following:
 *     public Class&lt;? extends Annotation&gt; getAnnotationType();
 *     protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
 *             EjbContext[] ejbContexts) throws AnnotationProcessorException;
 *     protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
 *             WebComponentContext[] webCompContexts)
 *             throws AnnotationProcessorException;
 *     protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
 *             WebBundleContext webBundleContext)
 *             throws AnnotationProcessorException;
 * It may also need to override the following if other annotations
 * need to be processed prior to given annotation:
 *     public Class&lt;? extends Annotation&gt;[] getTypeDependencies();
 *
 * @author Shing Wai Chan
 */
public abstract class AbstractCommonAttributeHandler extends AbstractHandler {

    /**
     * Process Annotation with given EjbContexts.
     *
     * @param ainfo
     * @param ejbContexts
     * @return HandlerProcessingResult
     */
    protected abstract HandlerProcessingResult processAnnotation(
        AnnotationInfo ainfo,
        EjbContext[] ejbContexts)
            throws AnnotationProcessorException;

    /**
     * Process Annotation with given WebCompContexts.
     *
     * @param ainfo
     * @param webCompContexts
     * @return HandlerProcessingResult
     */
    protected abstract HandlerProcessingResult processAnnotation(
        AnnotationInfo ainfo,
        WebComponentContext[] webCompContexts)
            throws AnnotationProcessorException;

    /**
     * Process Annotation with given WebBundleContext.
     *
     * @param ainfo
     * @param webBundleContext
     * @return HandlerProcessingResult
     */
    protected abstract HandlerProcessingResult processAnnotation(
        AnnotationInfo ainfo,
        WebBundleContext webBundleContext)
            throws AnnotationProcessorException;


    /**
     * Process a particular annotation which type is the same as the
     * one returned by @see getAnnotationType(). All information
     * pertinent to the annotation and its context is encapsulated
     * in the passed AnnotationInfo instance.
     *
     * @param ainfo the annotation information
     */
    @Override
    public HandlerProcessingResult processAnnotation(AnnotationInfo ainfo) throws AnnotationProcessorException {
        AnnotatedElementHandler aeHandler = ainfo.getProcessingContext().getHandler();
        if (aeHandler instanceof EjbBundleContext) {
            EjbBundleContext ejbBundleContext = (EjbBundleContext) aeHandler;
            aeHandler = ejbBundleContext.createContextForEjb();
        } else if (aeHandler instanceof WebBundleContext) {
            WebBundleContext webBundleContext = (WebBundleContext) aeHandler;
            aeHandler = webBundleContext.createContextForWeb();
            if (aeHandler == null) {
                // no such web comp, use webBundleContext
                aeHandler = ainfo.getProcessingContext().getHandler();
            }
        }

        if (aeHandler == null) {
            // no such ejb
            return getInvalidAnnotatedElementHandlerResult(ainfo.getProcessingContext().getHandler(), ainfo);
        }

        if (!supportTypeInheritance() && ElementType.TYPE.equals(ainfo.getElementType())
            && aeHandler instanceof ComponentContext) {
            ComponentContext context = (ComponentContext) aeHandler;
            Class<?> clazz = (Class<?>) ainfo.getAnnotatedElement();
            if (!clazz.getName().equals(context.getComponentClassName())) {
                if (logger.isLoggable(Level.WARNING)) {
                    log(Level.WARNING, ainfo,
                        I18N.getLocalString("enterprise.deployment.annotation.handlers.typeinhernotsupp",
                            "The annotation symbol inheritance is not supported."));
                }
                return getDefaultProcessedResult();
            }
        }

        if (aeHandler instanceof EjbContext) {
            return processAnnotation(ainfo, new EjbContext[] {(EjbContext) aeHandler});
        } else if (aeHandler instanceof EjbsContext) {
            EjbsContext ejbsContext = (EjbsContext) aeHandler;
            return processAnnotation(ainfo, ejbsContext.getEjbContexts());
        } else if (aeHandler instanceof WebComponentContext) {
            return processAnnotation(ainfo, new WebComponentContext[] {(WebComponentContext) aeHandler});
        } else if (aeHandler instanceof WebComponentsContext) {
            WebComponentsContext webCompsContext = (WebComponentsContext) aeHandler;
            return processAnnotation(ainfo, webCompsContext.getWebComponentContexts());
        } else if (aeHandler instanceof WebBundleContext) {
            WebBundleContext webBundleContext = (WebBundleContext) aeHandler;
            return processAnnotation(ainfo, webBundleContext);
        } else {
            return getInvalidAnnotatedElementHandlerResult(aeHandler, ainfo);
        }
    }


    /**
     * This indicates whether the annotation type should be processed for
     * type level in super-class.
     *
     * @return false
     */
    protected boolean supportTypeInheritance() {
        return false;
    }
}
