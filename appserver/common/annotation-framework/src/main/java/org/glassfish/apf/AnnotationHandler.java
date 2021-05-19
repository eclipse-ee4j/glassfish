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

import org.jvnet.hk2.annotations.Contract;

import java.lang.annotation.Annotation;

/**
 * This interface defines the contract for annotation handlers
 * and the annotation processing engine. Each annotation handler
 * is registered for a particular annotation type and will be
 * called by the engine when such annotation type is encountered.
 *
 * The AnnotationHandler is a stateless object, no state should
 * be stored, instead users should use the ProcessingContext.
 *
 * Annotation can be defined or processed in random orders on a
 * particular type, however, a particular annotation may need
 * other annotation to be processed before itself in order to be
 * processed successfully. An annotation type can indicate through
 * the @see getAnnotations() method which annotation types should
 * be processed before itself.
 *
 * Each implementation of this interface must specify the annotation that it can handle using
 * {@link AnnotationHandlerFor} annotation.
 *
 * @author Jerome Dochez
 */
@Contract
public interface AnnotationHandler {
    public final static String ANNOTATION_HANDLER_METADATA = "AnnotationHandlerFor";

    /**
     * @return the annotation type this annotation handler is handling
     */
    public Class<? extends Annotation> getAnnotationType();

    /**
     * Process a particular annotation which type is the same as the
     * one returned by @see getAnnotationType(). All information
     * pertinent to the annotation and its context is encapsulated
     * in the passed AnnotationInfo instance.
     *
     * @param element the annotation information
     */
    public HandlerProcessingResult processAnnotation(AnnotationInfo element)
        throws AnnotationProcessorException;

    /**
     * @return an array of annotation types this annotation handler would
     * require to be processed (if present) before it processes it's own
     * annotation type.
     */
    public Class<? extends Annotation>[] getTypeDependencies();

}
