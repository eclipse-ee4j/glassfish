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

import java.text.MessageFormat;

/**
 * Exception that denotes a warning or error condition in the
 * annotation procesing tool
 *
 * @author Jerome Dochez
 */
public final class AnnotationProcessorException extends Exception {

    private static final long serialVersionUID = 1L;

    // TODO if this class is meant for serialization, make sure all its constituents are serializable.
    private final transient AnnotationInfo annotationInfo;

    private boolean isFatal;


    /**
     * Creats a new annotation exception
     *
     * @param message describing the exception cause
     */
    public AnnotationProcessorException(String message) {
        super(message);
        this.annotationInfo = null;
    }


    /**
     * Creates a new annotation exception
     *
     * @param message describing the exception cause
     * @param annotationInfo gives information about the annotation and
     *            the annotated element which caused the exception
     */
    public AnnotationProcessorException(String message, AnnotationInfo annotationInfo) {
        super(message);
        this.annotationInfo = annotationInfo;
    }


    /**
     * Creates a new annotation exception
     *
     * @param message describing the exception cause
     * @param annotationInfo gives information about the annotation and
     *            the annotated element which caused the exception
     * @param cause
     */
    public AnnotationProcessorException(String message, AnnotationInfo annotationInfo, Throwable cause) {
        super(message, cause);
        this.annotationInfo = annotationInfo;
    }


    /**
     * Return information about the annotation and annotated element
     * which caused the exception or null if it is not available.
     *
     * @return the annotation info instance
     */
    public AnnotationInfo getAnnotationInfo() {
        return annotationInfo;
    }


    @Override
    public String getMessage() {
        if (annotationInfo == null) {
            return super.getMessage();
        }
        return MessageFormat.format("{0}. Related annotation information: {1}", super.getMessage(), annotationInfo);
    }


    /**
     * @return true if this exception was considered by the sender as being
     *         fatal to the annotations processing(i.e. it should stop).
     */
    public boolean isFatal() {
        return isFatal;
    }


    /**
     * Sets wether is exception is considered as fatal to the annotation processing.
     *
     * @param fatal true if the annotation processing should stop
     */
    public void setFatal(boolean fatal) {
        this.isFatal = fatal;
    }
}
