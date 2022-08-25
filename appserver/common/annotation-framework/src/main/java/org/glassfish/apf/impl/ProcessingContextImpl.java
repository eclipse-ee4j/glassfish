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

package org.glassfish.apf.impl;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationProcessor;
import org.glassfish.apf.ErrorHandler;
import org.glassfish.apf.ProcessingContext;
import org.glassfish.apf.Scanner;
import org.glassfish.apf.context.AnnotationContext;
import org.glassfish.api.deployment.archive.ReadableArchive;

/**
 * Minimal implementation of the ProcessingContext interface
 *
 * @author Jerome ochez
 */
class ProcessingContextImpl implements ProcessingContext {
    private static final Logger LOG = AnnotationUtils.getLogger();

    protected AnnotationProcessor processor;
    protected Stack<AnnotatedElementHandler> handlers = new Stack<>();
    protected Scanner scanner;
    protected ReadableArchive archive;
    private ErrorHandler errorHandler;

    /** Creates a new instance of ProcessingContextHelper */
    ProcessingContextImpl(AnnotationProcessor processor) {
        this.processor = processor;
    }


    @Override
    public AnnotationProcessor getProcessor() {
        return processor;
    }


    @Override
    public ReadableArchive getArchive() {
        return archive;
    }


    @Override
    public void setArchive(ReadableArchive archive) {
        this.archive = archive;
    }


    @Override
    public void pushHandler(AnnotatedElementHandler handler) {
        if (handler instanceof AnnotationContext) {
            ((AnnotationContext) handler).setProcessingContext(this);
        }
        handlers.push(handler);
    }


    @Override
    public AnnotatedElementHandler getHandler() {
        if (handlers.isEmpty()) {
            return null;
        }

        return handlers.peek();
    }


    @Override
    public AnnotatedElementHandler popHandler() {
        if (handlers.isEmpty()) {
            return null;
        }

        return handlers.pop();
    }


    /**
     * @return the previously set ClientContext casted to the requestd
     *         type if possible or throw an exception otherwise.
     */
    @Override
    public <U extends AnnotatedElementHandler> U getHandler(Class<U> contextType) throws ClassCastException {
        if (handlers.isEmpty()) {
            return null;
        }
        LOG.log(Level.FINER, "Top handler is {0}", handlers.peek());
        return contextType.cast(handlers.peek());
    }


    @Override
    public Scanner getProcessingInput() {
        return scanner;
    }


    @Override
    public void setProcessingInput(Scanner scanner) {
        this.scanner = scanner;
    }


    /**
     * Sets the error handler for this processing context.
     */
    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }


    /**
     * @return the error handler for this processing context.
     */
    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}
