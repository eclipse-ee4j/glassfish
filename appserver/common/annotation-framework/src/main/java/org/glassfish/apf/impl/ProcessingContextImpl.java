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

import org.glassfish.apf.Scanner;
import java.util.Stack;
import org.glassfish.apf.*;
import org.glassfish.apf.context.AnnotationContext;
import org.glassfish.api.deployment.archive.ReadableArchive;

/**
 * Minimal implementation of the ProcessingContext interface
 *
 * @author Jerome ochez
 */
class ProcessingContextImpl implements ProcessingContext {

    protected AnnotationProcessor processor;
    protected Stack<AnnotatedElementHandler> handlers = new Stack<AnnotatedElementHandler>();
    protected Scanner scanner;
    protected ReadableArchive archive;

    /** Creates a new instance of ProcessingContextHelper */
    ProcessingContextImpl(AnnotationProcessor processor) {
        this.processor = processor;
    }

    public AnnotationProcessor getProcessor() {
        return processor;
    }

    public ReadableArchive getArchive() {
        return archive;
    }

    public void setArchive(ReadableArchive archive) {
        this.archive = archive;
    }

    public void pushHandler(AnnotatedElementHandler handler) {
        if (handler instanceof AnnotationContext) {
            ((AnnotationContext) handler).setProcessingContext(this);
        }
        handlers.push(handler);
    }

    public AnnotatedElementHandler getHandler() {
        if (handlers.isEmpty())
            return null;

        return handlers.peek();
    }

    public AnnotatedElementHandler popHandler() {
        if (handlers.isEmpty())
            return null;

        return handlers.pop();
    }

    /**
     * @return the previously set ClientContext casted to the requestd
     * type if possible or throw an exception otherwise.
     */
    public <U extends AnnotatedElementHandler> U getHandler(Class<U> contextType)
        throws ClassCastException {

        if (handlers.isEmpty())
            return null;
        if (AnnotationUtils.shouldLog("handler")) {
            AnnotationUtils.getLogger().finer("Top handler is " + handlers.peek());
        }
        return contextType.cast(handlers.peek());
    }

    public Scanner getProcessingInput() {
        return scanner;
    }
    public void setProcessingInput(Scanner scanner) {
        this.scanner = scanner;
    }

    private ErrorHandler errorHandler = null;

    /**
     * Sets the error handler for this processing context.
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * @return the error handler for this processing context.
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}
