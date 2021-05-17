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

import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;


/**
 * Provides notification when the annotation processor is visiting a
 * new AnnotatedElement.
 *
 * @author Jerome Dochez
 */
public interface AnnotatedElementHandler {

    /**
     * Before annotations for an annotated element are processed, the
     * startElement is called with the annotated element value and its type
     *
     * @param type the annotated element type (class, field, method...)
     * @param element the annotated element we are starting to visit.
     *
     * @exception AnnotationProcessorException;
     */
    public void startElement(ElementType type, AnnotatedElement element)
        throws AnnotationProcessorException;

    /**
     * After annotations for an annotated element are processed, the
     * endElement is called with the annotated element value and its type
     *
     * @param type the annotated element type (class, field, method...)
     * @param element the annotated element we are done visiting.
     *
     * @exception AnnotationProcessorException;
     */
    public void endElement(ElementType type, AnnotatedElement element)
        throws AnnotationProcessorException;

}
