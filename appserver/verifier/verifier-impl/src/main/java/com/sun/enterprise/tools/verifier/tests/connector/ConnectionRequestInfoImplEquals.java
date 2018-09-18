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
 * ConnectionRequestInfoImplEquals.java
 *
 * Created on September 27, 2000, 10:21 AM
 */

package com.sun.enterprise.tools.verifier.tests.connector;

import java.io.File;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;

/**
 * Test wether the implementatin of the ConnectionRequestInfo interface
 * properly overrides the equals method
 *
 * @author  Jerome Dochez
 * @version 
 */
public class ConnectionRequestInfoImplEquals extends ConnectorTest implements ConnectorCheck {


    /** <p>
     * Test wether the implementatin of the ConnectionRequestInfo interface
     * properly overrides the equals method.
     * </p>
     *
     * @paramm descriptor deployment descriptor for the rar file
     * @return result object containing the result of the individual test
     * performed
     */
    public Result check(ConnectorDescriptor descriptor) {
        
        Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
        // let's get first the the default implementation of the ConnectionManager
        //File jarFile = Verifier.getJarFile(descriptor.getModuleDescriptor().getArchiveUri());            
//        File f=Verifier.getArchiveFile(descriptor.getModuleDescriptor().getArchiveUri());
        Class c = findImplementorOf(descriptor, "javax.resource.spi.ConnectionRequestInfo");
        
        if (c == null) {
	    result.addNaDetails(smh.getLocalString
				  ("tests.componentNameConstructor",
				   "For [ {0} ]",
				   new Object[] {compName.toString()}));	
            result.notApplicable(smh.getLocalString
	    ("com.sun.enterprise.tools.verifier.tests.connector.ConnectorTest.optionalInterfaceMissing", 
            "Warning: There is no implementation of the optional [ {0} ] interface",
            new Object[] {"javax.resource.spi.ConnectionRequestInfo"}));  
        } else {
            // An implementation of the interface is provided, let's check the equals method
            checkMethodImpl(c, "equals", new Class[] {Object.class}, "public boolean equals(java.lang.Object)", result);                    
        }
        return result;
    }        
}
