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

package org.glassfish.apf;

import org.glassfish.apf.context.AnnotationContext;
import org.glassfish.api.deployment.archive.ReadableArchive;

/**
 * This interface defines the context for the annotation procesing
 * handler. There is only one context instance per AnnotationProcessor
 * invocation.
 *
 * @author Jerome Dochez
 */
public interface ProcessingContext {

    /**
     * Returns the AnnotationProcessor instance this context is associated with.
     * @return annotation processor instance
     */
    AnnotationProcessor getProcessor();

    /**
     * Returns the Scanner implementation which is responsible for providing
     * access to all the .class files the processing tool needs to scan.
     * @return scanner instance
     */
    Scanner getProcessingInput();

    /**
     * Returns the module archive that can be used to load files/resources,
     * that assist in the processing of annotations. Using the ClassLoader is
     * preferred, but not all files can be loaded by it and this can be handy
     * in those cases.
     *
     * @return module archive
     */
    ReadableArchive getArchive();

    void setArchive(ReadableArchive archive);

    /**
     * Sets the Scanner implementation which is responsible for accessing
     * all the .class files the AnnotationProcessor should process.
     */
    void setProcessingInput(Scanner scanner);

    /**
     * Push a new handler on the stack of handlers. This handler will receive
     * all the AnnotedElementHandler events until it is removed from the stack
     * with a popHandler() call.
     *
     * @param handler the new events handler.
     */
    void pushHandler(AnnotationContext handler);

    /**
     * Return the current handler (if any) receving all the annotated elements
     * start and stop events.
     *
     * @return the top handler
     */
    AnnotatedElementHandler getHandler();

    /**
     * Removes the top handler
     *
     * @return the removed handler
     */
    AnnotatedElementHandler popHandler();

    /**
     * Sets the ErrorHandler instance for all errors/warnings that may be raised
     * during the annotation processing.
     *
     * @param errorHandler the annotation handler
     */
    void setErrorHandler(ErrorHandler errorHandler);

    /**
     * Return the error handler for this processing context.
     *
     * @return the error handler
     */
    ErrorHandler getErrorHandler();

}
