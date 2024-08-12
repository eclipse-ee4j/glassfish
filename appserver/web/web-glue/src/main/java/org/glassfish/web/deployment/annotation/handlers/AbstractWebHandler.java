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

package org.glassfish.web.deployment.annotation.handlers;

import com.sun.enterprise.deployment.annotation.context.WebBundleContext;
import com.sun.enterprise.deployment.annotation.context.WebComponentContext;
import com.sun.enterprise.deployment.annotation.context.WebComponentsContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractHandler;

import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;

/**
 * This is an abstract class for Web annotation handler.
 * Concrete subclass handlers need to implement the following methods:
 *     public Class&lt;? extends Annotation&gt; getAnnotationType();
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
abstract class AbstractWebHandler extends AbstractHandler {
    /**
     * Process Annotation with given WebCompContexts.
     * @param ainfo
     * @param webCompContexts
     * @return HandlerProcessingResult
     */
    protected abstract HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, WebComponentContext[] webCompContexts)
            throws AnnotationProcessorException;

    /**
     * Process Annotation with given WebBundleContext.
     * @param ainfo
     * @param webBundleContext
     * @return HandlerProcessingResult
     */
    protected abstract HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, WebBundleContext webBundleContext)
            throws AnnotationProcessorException;

    /**
     * Process a particular annotation which type is the same as the
     * one returned by @see getAnnotationType(). All information
     * pertinent to the annotation and its context is encapsulated
     * in the passed AnnotationInfo instance.
     * This is a method in interface AnnotationHandler.
     *
     * @param ainfo the annotation information
     */
    @Override
    public HandlerProcessingResult processAnnotation(AnnotationInfo ainfo)
            throws AnnotationProcessorException {

        AnnotatedElementHandler aeHandler = ainfo.getProcessingContext().getHandler();
        if (aeHandler instanceof WebBundleContext) {
            WebBundleContext webBundleContext = (WebBundleContext)aeHandler;
            AnnotatedElementHandler aeh = webBundleContext.createContextForWeb();
            if (aeh != null) {
                aeHandler = aeh;
            }
        }

        //no inheritance

        HandlerProcessingResult procResult = null;
        if (aeHandler instanceof WebComponentContext) {
            procResult = processAnnotation(ainfo,
                new WebComponentContext[] { (WebComponentContext)aeHandler });
        } else if (aeHandler instanceof WebComponentsContext) {
            WebComponentsContext webCompsContext = (WebComponentsContext)aeHandler;
            procResult = processAnnotation(ainfo, webCompsContext.getWebComponentContexts());
        } else if (aeHandler instanceof WebBundleContext) {
            WebBundleContext webBundleContext = (WebBundleContext)aeHandler;
            procResult = processAnnotation(ainfo, webBundleContext);
        } else {
            return getInvalidAnnotatedElementHandlerResult(aeHandler, ainfo);
        }

        return procResult;
    }
}
