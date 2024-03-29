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

/**
 * Basic interfaced for annotation processing warnings and errors
 *
 * @author Jerome Dochez
 */
public interface ErrorHandler {

    /**
     * Receive notication of a fine error message
     *
     * @param ape The warning information
     * @throws any exception to stop the annotation processing
     */
    void fine(AnnotationProcessorException ape) throws AnnotationProcessorException;

    /**
     * Receive notification of a warning
     *
     * @param ape The warning information
     * @throws any exception to stop the annotation processing
     */
    void warning(AnnotationProcessorException ape) throws AnnotationProcessorException;

    /**
     * Receive notification of an error
     *
     * @param ape The error information
     * @throws amy exception to stop the annotation processing
     */
    void error(AnnotationProcessorException ape) throws AnnotationProcessorException;

}
