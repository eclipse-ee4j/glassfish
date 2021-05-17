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

import java.lang.reflect.AnnotatedElement;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;

import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * Instances encapsulate all information necessary for an AnnotationHandler
 * to process an annotation. In particular, instances of this class provide
 * access to :
 *
 * <p>
 * <li> the Annotation instance
 * <li> the ProcessingContext of the tool
 * <li> the AnnotatedElement which is a reference to the annotation element
 * (Type, Method...).
 * </p>
 *
 * @see java.lang.annotation.Annotation, java.lang.reflect.AnnotatedElement
 *
 * @author Jerome Dochez
 *
 */
public class AnnotationInfo {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(AnnotationInfo.class);

    // the annotated element
    final private AnnotatedElement annotatedElement;

    // the annotation
    final private Annotation annotation;

    // the processing context
    final private ProcessingContext context;

    // the element type
    final private ElementType type;

    /**
     * Creates a new instance of AnnotationInfo with all the information
     * necessary to process an annotation.
     *
     * @param context the annotation processor processing context
     * @param element the annotated element
     * @param annotation the annotation
     */
    public AnnotationInfo(ProcessingContext context, AnnotatedElement element,
            Annotation annotation, ElementType type) {

        this.context = context;
        this.annotatedElement = element;
        this.annotation = annotation;
        this.type = type;
    }

    /**
     * @return the annotated element instance
     */
    public AnnotatedElement getAnnotatedElement() {
        return annotatedElement;
    }

    /**
     * @return the annotation instance
     */
    public Annotation getAnnotation() {

        return annotation;
    }

    /**
     * @return the processing context
     */
    public ProcessingContext getProcessingContext() {
        return context;
    }

    /**
     * @return the annotated element ElementType
     */
    public ElementType getElementType() {
        return type;
    }

    public String toString() {
        return localStrings.getLocalString("annotatedinfo.string", "annotation [{0}] on annotated element [{1}] of type [{2}]", annotation, annotatedElement, type);
    }
}
