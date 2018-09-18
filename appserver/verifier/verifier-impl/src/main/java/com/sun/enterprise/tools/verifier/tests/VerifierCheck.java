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

/*
 * VerifierTest.java
 *
 * Created on September 20, 2000, 3:39 PM
 */

package com.sun.enterprise.tools.verifier.tests;

import com.sun.enterprise.tools.verifier.Result;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.tools.verifier.VerifierTestContext;

/**
 * <p>
 * All tests to be run inside the verifier harness must implement this interface
 * defining the contract between the harness and individual tests
 * <p>
 *
 * @author  Jerome Dochez
 * @version 
 */
public interface VerifierCheck {
    
    /**
     * <p>
     * run an individual test in the Verifier harness
     * </p>
     *
     * @param descriptor deployment descriptor for the archive
     * @return result object containing the result of the individual test
     * performed
     */
    public Result check(Descriptor descriptor);
    public VerifierTestContext getVerifierContext();
    public void setVerifierContext(VerifierTestContext context);
}
