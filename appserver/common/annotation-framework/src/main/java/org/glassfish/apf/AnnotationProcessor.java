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

package org.glassfish.apf;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.util.logging.Level;

/**
 * <p>
 * The annotation processor is the core engine to process annotations.
 * All the processing configuration (input classes, error handlers, etc...)
 * is provided by the ProcessingContext which can be either created from the
 * createContext method or through another mean. Once the ProcessingContext has
 * been initialized, it is passed to the process(ProcessingContext ctx) method which
 * triggers the annotation processing.
 * </p>
 *
 * <p>
 * Each class accessible from the ProcessingContext.getInputScanner instance, will be
 * scanned for annotations.
 * Each annotation will then be processed by invoking the corresponding AnnotationHandler
 * from its annotation type.
 * </p>
 *
 * <p>
 * The AnnotationProcessor can be configured by using the pushAnnotationHandler and
 * popAnnotationHandler which allow new AnnotationHandler instances to be registered and
 * unregistered for a particular annotation type.
 * </p>
 *
 * <p>
 * Even without reconfiguring the AnnotationProcessor instance with the above
 * configuration methods, the AnnotationProcessor implementation cannot guarantee
 * to be thread safe, therefore, it is encouraged the make instanciation cheap
 * and users should not use the same instance concurrently.
 * </p>
 *
 * @author Jerome Dochez
 */
public interface AnnotationProcessor {

    /**
     * Creates a new empty ProcessingContext instance which can be configured
     * before invoking the process() method.
     * @return an empty ProcessingContext
     */
    public ProcessingContext createContext();

    /**
     * Starts the annotation processing tool passing the processing context which
     * encapuslate all information necessary for the configuration of the tool.
     * @param ctx is the initialized processing context
     * @return the result of the annoations processing
     */
    public ProcessingResult process(ProcessingContext ctx) throws AnnotationProcessorException;

    /**
     * Process a set of classes from the parameter list rather than from the
     * processing context. This allow the annotation handlers to call be the
     * annotation processing tool when classes need to be processed in a
     * particular context rather than when they are picked up by the scanner.
     *
     * @param the processing context
     * @param the list of classes to process
     * @return the processing result for such classes
     * @throws AnnotationProcessorException if handlers fail to process
     * an annotation
     */
    public ProcessingResult process(ProcessingContext ctx, Class[] classes)
        throws AnnotationProcessorException;

    /**
     * Registers a new AnnotationHandler for a particular annotation type. New annotation handler
     * are pushed on a List of annotation handlers for that particular annotation type, the last
     * annotation handler to be registered will be invoked first and so on.
     * The annotation type handled by the AnnotationHandler instance is defined
     * by the getAnnotationType() method of the AnnotationHandler instance
     *
     * @param type the annotation type
     * @param handler the annotation handler instance
     */
    public void pushAnnotationHandler(AnnotationHandler handler);

    /**
     * @return the top annotation handler for a particular annotation type
     * @param type the annotation type
     */
    public AnnotationHandler getAnnotationHandler(Class<? extends Annotation> type);

    /**
     * Unregisters the last annotation handler registered for an annotation type.
     * @param type the annotation type.
     */
    public void popAnnotationHandler(Class<? extends Annotation> type);

    /**
     * @return the most recent AnnotatedElement being processed which type is of the
     * given ElementType or null if there is no such element in the stack of
     * processed annotation elements.
     */
    public AnnotatedElement getLastAnnotatedElement(ElementType type);

    /**
     * Log a message on the default logger
     */
    public void log(Level level, AnnotationInfo locator, String localizedMessage);
}
