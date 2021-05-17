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

package org.glassfish.apf.impl;

import org.glassfish.apf.ErrorHandler;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Default implementation of the ErrorHandler
 *
 * @author Jerome Dochez
 */
public class DefaultErrorHandler implements ErrorHandler {

    Logger logger;

    /**
     * Creates a new ErrorHandler with the default logger
     */
    public DefaultErrorHandler() {
        logger = AnnotationUtils.getLogger();
    }

    /**
     * Creates a new ErrorHandler with the provided looger;
     */
    public DefaultErrorHandler(Logger logger){
        this.logger = logger;
    }

    /**
     * Receive notication of a fine error message
     * @param ape The warning information
     * @throws any exception to stop the annotation processing
     */
    public void fine(AnnotationProcessorException ape) throws
            AnnotationProcessorException {

        if (logger.isLoggable(Level.FINE)){
            AnnotationInfo info = ape.getLocator();
            if (info==null){
                logger.fine(ape.getMessage());
            } else{
                logger.fine(AnnotationUtils.getLocalString(
                    "enterprise.deployment.annotation.error",
                    "{2}\n symbol: {0}\n location: {1}\n\n",
                    new Object[] { info.getElementType(), info.getAnnotatedElement(), ape.getMessage()}));
            }
        }

    }

    /**
     * Receive notification of a warning
     * @param ape The warning information
     * @throws any exception to stop the annotation processing
     */
    public void warning(AnnotationProcessorException ape) throws
            AnnotationProcessorException {

        if (logger.isLoggable(Level.WARNING)){
            AnnotationInfo info = ape.getLocator();
            if (info==null){
                logger.warning(ape.getMessage());
            } else{
                logger.warning(AnnotationUtils.getLocalString(
                    "enterprise.deployment.annotation.error",
                    "{2}\n symbol: {0}\n location: {1}\n\n",
                    new Object[] { info.getElementType(), info.getAnnotatedElement(), ape.getMessage()}));
            }
        }
    }

    /**
     * Receive notification of an error
     * @param ape The error information
     * @throws amy exception to stop the annotation processing
     */
    public void error(AnnotationProcessorException ape) throws
            AnnotationProcessorException {

        AnnotationInfo info = ape.getLocator();
        if (info==null){
            logger.severe(ape.getMessage());
        } else{
            logger.severe(AnnotationUtils.getLocalString(
                "enterprise.deployment.annotation.error",
                "{2}\n symbol: {0} location: {1}\n\n",
                new Object[] { info.getElementType(), info.getAnnotatedElement(), ape.getMessage()}));
        }
    }
}
