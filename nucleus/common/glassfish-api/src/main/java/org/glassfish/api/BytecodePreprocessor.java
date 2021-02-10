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

package org.glassfish.api;

import java.util.Hashtable;

import org.jvnet.hk2.annotations.Contract;

/**
 * Third party tool vendors may implement this interface to provide code instrumentation to the application server.
 */
@Contract
public interface BytecodePreprocessor {

    /**
     * Initialize the profiler instance. This method should be called exactly once before any calls to preprocess.
     *
     * @param parameters Initialization parameters.
     * @return true if initialization succeeded.
     */
    boolean initialize(Hashtable parameters);

    /**
     * This function profiler-enables the given class. This method should not be called until the initialization method has
     * completed. It is thread- safe.
     *
     * @param classname The name of the class to process. Used for efficient filtering.
     * @param classBytes Actual contents of class to process
     * @return The instrumented class bytes.
     */
    byte[] preprocess(String classname, byte[] classBytes);

}
