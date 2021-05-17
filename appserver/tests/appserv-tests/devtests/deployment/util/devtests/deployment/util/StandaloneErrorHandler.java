/*
 * Copyright (c) 2005, 2018 Oracle and/or its affiliates. All rights reserved.
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

package devtests.deployment.util;

import org.glassfish.apf.ErrorHandler;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.impl.AnnotationUtils;

/**
 * Standalone Implementation of ErrorHandler.
 *
 * @author Shing Wai Chan
 */
public class StandaloneErrorHandler implements ErrorHandler {
    /** Creates a new instance of StandaloneErrorHandler */
    public StandaloneErrorHandler() {
    }

    /**
     * Receive notication of a fine error message
     * @param ape The warning information
     * @throws any exception to stop the annotation processing
     */
    public void fine(AnnotationProcessorException ape) throws
            AnnotationProcessorException {
        AnnotationUtils.getLogger().fine("Fine : " + ape);
    }

    /**
     * Receive notification of a warning
     * @param ape The warning information
     * @throws any exception to stop the annotation processing
     */
    public void warning(AnnotationProcessorException ape) throws
            AnnotationProcessorException {
        AnnotationUtils.getLogger().warning("Warning : " + ape);
    }

    /**
     * Receive notification of an error
     * @param ape The error information
     * @throws any exception to stop the annotation processing
     */
    public void error(AnnotationProcessorException ape) throws
            AnnotationProcessorException {
        AnnotationUtils.getLogger().severe("Error : " + ape);
        throw ape;
    }
}
