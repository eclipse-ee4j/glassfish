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

package org.glassfish.apf.context;

import java.util.Stack;
import java.lang.reflect.AnnotatedElement;
import java.lang.annotation.ElementType;

import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.ProcessingContext;

/**
 * Convenient superclass implementation of Context objects responsible
 * for holding references to the DOL objects
 *
 * @author Jerome Dochez
 */
public class AnnotationContext implements AnnotatedElementHandler {

    ProcessingContext processingContext;

    /** Creates a new instance of AnnotationContext */
    public AnnotationContext() {
    }

    public void setProcessingContext(ProcessingContext processingContext) {
        this.processingContext = processingContext;
    }

    public ProcessingContext getProcessingContext() {
        return processingContext;
    }

    public void startElement(ElementType type, AnnotatedElement element)
        throws AnnotationProcessorException {
    }

    public void endElement(ElementType type, AnnotatedElement element)
        throws AnnotationProcessorException {
    }


}
