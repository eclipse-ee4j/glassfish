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

package com.sun.enterprise.tools.verifier;

import org.glassfish.deployment.common.Descriptor;


/**
 * All classes responsible for performing compliance tests on an J2EE architecture
 * like web applications or connectors implement this interface to be called by
 * the verifier harness. Classes with such a responsibility are called managers
 * in the verifier harness.
 *
 * @author Jerome Dochez
 */
public interface JarCheck {

    /**
     * <p/>
     * Entry point to perform all tests pertinent to this implemented
     * architecture on a J2EE archive file (ear, war, rar...)
     * </p>
     *
     * @param descriptor descriptor instance for the J2EE
     *                   archive to test
     */
    void check(Descriptor descriptor) throws Exception;
    
}
