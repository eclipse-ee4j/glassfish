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

package org.glassfish.ejb.deployment.annotation.handlers;

import com.sun.enterprise.deployment.annotation.context.ComponentContext;
import com.sun.enterprise.deployment.annotation.context.EjbBundleContext;
import com.sun.enterprise.deployment.annotation.context.EjbContext;
import com.sun.enterprise.deployment.annotation.context.EjbInterceptorContext;
import com.sun.enterprise.deployment.annotation.context.EjbsContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractHandler;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.util.logging.Level;

import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;

/**
 * This is an abstract class encapsulate generic behaviour of annotation
 * handler applying on Ejb Class.  It will get the corresponding
 * EjbDescriptors associated to the annotation on the given Ejb Class
 * and then pass it to underlying processAnnotation method.
 * Concrete subclass handlers need to implement the following:
 *     public Class&lt;? extends Annotation&gt; getAnnotationType();
 *     protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
 *             EjbContext[] ejbContexts) throws AnnotationProcessorException;
 * It may also need to override the following:
 * a) if other annotations need to be processed prior to given annotation:
 *     public Class&lt;? extends Annotation&gt;[] getTypeDependencies();
 * b) if the given annotation can be processed while processing another
 *    annotation
 *     protected boolean isDelegatee();
 * c) if we need to process for interceptor
 *     protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
 *             EjbInterceptorContext ejbInterceptorContext)
 *             throws AnnotationProcessorException;
 * d) indicate the annotation support type inheritance
 *     protected boolean supportTypeIneritance();
 *
 * @author Shing Wai Chan
 */
public abstract class AbstractAttributeHandler extends AbstractHandler {

    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(AbstractAttributeHandler.class);

    /**
     * Process Annotation with given EjbContexts.
     * @param ainfo
     * @param ejbContexts
     * @return HandlerProcessingResult
     * @exception AnnotationProcessorException
     */
    protected abstract HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, EjbContext[] ejbContexts)
            throws AnnotationProcessorException;

    /**
     * Process Annotation with given InteceptorContext.
     * @param ainfo
     * @param ejbInterceptorContext
     * @return HandlerProcessingResult
     * @exception AnnotationProcessorException
     */
    protected HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, EjbInterceptorContext ejbInterceptorContext)
            throws AnnotationProcessorException {
        if (!isDelegatee()) {
            throw new UnsupportedOperationException();
        }
        return getDefaultProcessedResult();
    }

    /**
     * Process a particular annotation which type is the same as the
     * one returned by @see getAnnotationType(). All information
     * pertinent to the annotation and its context is encapsulated
     * in the passed AnnotationInfo instance.
     * This is a method in interface AnnotationHandler.
     *
     * @param ainfo the annotation information
     */
    public HandlerProcessingResult processAnnotation(AnnotationInfo ainfo)
            throws AnnotationProcessorException {

        AnnotatedElement ae = ainfo.getAnnotatedElement();
        Annotation annotation = ainfo.getAnnotation();

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("@process annotation " + annotation + " in " + ae);
        }

        AnnotatedElementHandler aeHandler = ainfo.getProcessingContext().getHandler();

        if (aeHandler instanceof EjbBundleContext) {
            EjbBundleContext ejbBundleContext = (EjbBundleContext)aeHandler;
            AnnotatedElementHandler aeh = ejbBundleContext.createContextForEjb();
            if (aeh != null) {
                aeHandler = aeh;
            } else {
                if (isDelegatee()) {
                    aeHandler = ejbBundleContext.createContextForEjbInterceptor();
                }
                if (aeHandler == null) {
                    return getInvalidAnnotatedElementHandlerResult(null, ainfo);
                }
            }
        }

        if (!supportTypeInheritance() &&
                ElementType.TYPE.equals(ainfo.getElementType()) &&
                aeHandler instanceof ComponentContext) {
            ComponentContext context = (ComponentContext)aeHandler;
            Class clazz = (Class)ainfo.getAnnotatedElement();
            if (!clazz.getName().equals(context.getComponentClassName())) {
                if (logger.isLoggable(Level.WARNING)) {
                    log(Level.WARNING, ainfo,
                        localStrings.getLocalString(
                        "enterprise.deployment.annotation.handlers.typeinhernotsupp",
                        "The annotation symbol inheritance is not supported."));
                }
                return getDefaultProcessedResult();
            }
        }

        EjbContext[] ejbContexts = null;
        EjbInterceptorContext ejbInterceptorContext = null;
        if (aeHandler instanceof EjbContext) {
            EjbContext ejbContext = (EjbContext)aeHandler;
            ejbContexts = new EjbContext[] { ejbContext };
        } else if (aeHandler instanceof EjbsContext) {
            ejbContexts = ((EjbsContext)aeHandler).getEjbContexts();
        } else if (isDelegatee() && aeHandler instanceof EjbInterceptorContext) {
            ejbInterceptorContext = (EjbInterceptorContext)aeHandler;
        } else {
            return getInvalidAnnotatedElementHandlerResult(aeHandler, ainfo);
        }

        HandlerProcessingResult procResult = null;

        if (ejbInterceptorContext != null) {
            procResult = processAnnotation(ainfo, ejbInterceptorContext);
        } else {
            procResult = processAnnotation(ainfo, ejbContexts);
        }

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("New annotation for " + annotation);
        }
        return procResult;
    }

    /**
     * This indicates whether the annotation can be processed by delegation
     * from the another annotation.
     */
    protected boolean isDelegatee() {
        return false;
    }

    /**
     * This indicates whether the annotation type should be processed for
     * type level in super-class.
     */
    protected boolean supportTypeInheritance() {
        return false;
    }
}
