/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationHandler;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.apf.ResultType;
import org.glassfish.apf.impl.AnnotationUtils;
import org.glassfish.apf.impl.HandlerProcessingResultImpl;
import org.glassfish.internal.deployment.AnnotationTypesProvider;
import org.jvnet.hk2.annotations.Optional;

/**
 * This is an abstract base class for Handlers.
 * Concrete subclass has to be annotated with {@link AnnotationHandlerFor} so that appropriate
 * metadata
 * can be generated statically. Concrete subclass has to also implement the following method:
 * public HandlerProcessingResult processAnnotation(AnnotationInfo ainfo)
 *
 * @author Shing Wai Chan
 */
public abstract class AbstractHandler implements AnnotationHandler {

    protected static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(AbstractHandler.class);
    protected static final Logger logger = AnnotationUtils.getLogger();

    @Inject
    @Named("EJB")
    @Optional
    protected AnnotationTypesProvider ejbProvider;

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return getClass().getAnnotation(AnnotationHandlerFor.class).value();
    }


    @Override
    public Class<? extends Annotation>[] getTypeDependencies() {
        return null;
    }

    // ----- end of implements AnnotationHandler -----


    /**
     * @return a default processed result
     */
    protected HandlerProcessingResult getDefaultProcessedResult() {
        return HandlerProcessingResultImpl.getDefaultResult(getAnnotationType(), ResultType.PROCESSED);
    }


    /**
     * @return a default failed result
     */
    protected HandlerProcessingResult getDefaultFailedResult() {
        return HandlerProcessingResultImpl.getDefaultResult(getAnnotationType(), ResultType.FAILED);
    }


    /**
     * @param aeHandler
     * @param ainfo
     * @return a result for invalid AnnotatedElementHandler
     */
    protected HandlerProcessingResult getInvalidAnnotatedElementHandlerResult(AnnotatedElementHandler aeHandler,
        AnnotationInfo ainfo) throws AnnotationProcessorException {
        if (logger.isLoggable(Level.FINE)) {
            log(Level.FINE, ainfo, I18N.getLocalString("enterprise.deployment.annotation.handlers.invalidaehandler",
                "Invalid annotation symbol found for this type of class."));
        }
        logger.log(Level.FINER, "Invalid AnnotatedElementHandler: {0}", aeHandler);
        return getDefaultProcessedResult();
    }


    protected void log(Level level, AnnotationInfo ainfo, String localizedMessage) throws AnnotationProcessorException {
        if (Level.SEVERE.equals(level)) {
            ainfo.getProcessingContext().getErrorHandler()
                .error(new AnnotationProcessorException(localizedMessage, ainfo));
        } else if (Level.WARNING.equals(level)) {
            ainfo.getProcessingContext().getErrorHandler()
                .warning(new AnnotationProcessorException(localizedMessage, ainfo));
        } else if (Level.FINE.equals(level)) {
            ainfo.getProcessingContext().getErrorHandler()
                .fine(new AnnotationProcessorException(localizedMessage, ainfo));
        } else if (ainfo != null) {
            ainfo.getProcessingContext().getProcessor().log(level, ainfo, localizedMessage);
        } else {
            logger.log(level, localizedMessage);
        }
    }


    protected String getInjectionMethodPropertyName(Method method, AnnotationInfo ainfo)
        throws AnnotationProcessorException {
        String methodName = method.getName();
        if (methodName.length() > 3 && methodName.startsWith("set")) {
            // Derive javabean property name.
            return methodName.substring(3, 4).toLowerCase(Locale.US) + methodName.substring(4);
        }
        throw new AnnotationProcessorException(
            I18N.getLocalString("enterprise.deployment.annotation.handlers.invalidinjectionmethodname",
                "Injection method name must start with \"set\""),
            ainfo);
    }


    /**
     * Check if given method is a valid injection method.
     * Throw Exception if it is not.
     *
     * @exception AnnotationProcessorException
     */
    protected void validateInjectionMethod(Method method, AnnotationInfo ainfo) throws AnnotationProcessorException {
        if (method.getParameterTypes().length != 1) {
            throw new AnnotationProcessorException(
                I18N.getLocalString("enterprise.deployment.annotation.handlers.invalidinjectionmethod",
                    "Injection on a method requires a JavaBeans setter method type with one parameter "),
                ainfo);

        }
        if (!void.class.equals(method.getReturnType())) {
            throw new AnnotationProcessorException(
                I18N.getLocalString("enterprise.deployment.annotation.handlers.injectionmethodmustreturnvoid",
                    "Injection on a method requires a void return type"),
                ainfo);
        }
    }


    protected HandlerProcessingResult getOverallProcessingResult(List<HandlerProcessingResult> resultList) {
        HandlerProcessingResult overallProcessingResult = null;
        for (HandlerProcessingResult result : resultList) {
            if (overallProcessingResult == null
                || (result.getOverallResult().compareTo(overallProcessingResult.getOverallResult()) > 0)) {
                overallProcessingResult = result;
            }
        }
        return overallProcessingResult;
    }


    /**
     * This is called by getTypeDependencies().
     *
     * @return an array of all ejb annotation types
     */
    protected Class<? extends Annotation>[] getEjbAnnotationTypes() {
        if (ejbProvider == null) {
            return new Class[0];
        }
        return ejbProvider.getAnnotationTypes();
    }


    /**
     * This is called by getTypeDependencies().
     *
     * @return an array of all ejb and web types annotation
     */
    protected Class<? extends Annotation>[] getEjbAndWebAnnotationTypes() {
        Class<? extends Annotation>[] weTypes = null;
        Class<? extends Annotation>[] ejbTypes = getEjbAnnotationTypes();
        Class<? extends Annotation>[] webTypes = getWebAnnotationTypes();
        if (ejbTypes.length > 0) {
            weTypes = new Class[ejbTypes.length + webTypes.length];
            System.arraycopy(ejbTypes, 0, weTypes, 0, ejbTypes.length);
            System.arraycopy(webTypes, 0, weTypes, ejbTypes.length, webTypes.length);
        } else {
            weTypes = webTypes;
        }

        return weTypes;
    }


    /**
     * This is called by getTypeDependencies().
     *
     * @return an array of all web types annotation
     */
    protected Class<? extends Annotation>[] getWebAnnotationTypes() {
        return new Class[] {jakarta.servlet.annotation.WebServlet.class};
    }


    /**
     * This is called by getTypeDependencies().
     *
     * @return an array of all connector type annotations
     */
    protected Class<? extends Annotation>[] getConnectorAnnotationTypes() {
        return new Class[] {jakarta.resource.spi.Connector.class};
    }
}
