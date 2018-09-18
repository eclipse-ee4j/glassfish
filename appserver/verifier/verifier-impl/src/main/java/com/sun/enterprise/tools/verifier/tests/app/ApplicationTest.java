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

package com.sun.enterprise.tools.verifier.tests.app;


import com.sun.enterprise.tools.verifier.tests.VerifierTest;
import com.sun.enterprise.tools.verifier.tests.VerifierCheck;
import com.sun.enterprise.tools.verifier.Result;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.Application;

import java.io.File;

/**
 * Superclass for all application tests, contains common services.
 *
 * @author  Jerome Dochez
 * @version 
 */
public abstract class ApplicationTest extends VerifierTest implements VerifierCheck, AppCheck
{
        
    /**
     * <p>
     * run an individual test against the deployment descriptor for the 
     * archive the verifier is performing compliance tests against.
     * </p>
     *
     * @paramm descriptor deployment descriptor for the archive
     * @return result object containing the result of the individual test
     * performed
     */    
    public Result check(Descriptor descriptor) {
        return check((Application) descriptor);
    }
   
    /**
     * <p>
     * all connector tests should implement this method. it run an individual
     * test against the resource adapter deployment descriptor. 
     * </p>
     *
     * @paramm descriptor deployment descriptor for the archive file
     * @return result object containing the result of the individual test
     * performed
     */    
    public abstract Result check(Application descriptor);     

    

    protected String getAbstractArchiveUri(Application desc) {

     String archBase = getVerifierContext().getAbstractArchive().
                       getURI().toString();
     return archBase;
    }

    
}
