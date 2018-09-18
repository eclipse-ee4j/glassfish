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
 * DefaultConnectionManagerSerializable.java
 *
 * Created on September 26, 2000, 6:53 PM
 */

package com.sun.enterprise.tools.verifier.tests.connector;

import java.io.File;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;


/**
 * Test if the default implementation of the javax.resource.spi.ConnectionManager
 * provided implements the java.io.Serializable interface
 *
 * @author  Jerome Dochez
 * @version 
 */
public class DefaultConnectionManagerSerializable extends ConnectorTest implements ConnectorCheck {


    /** <p>
     * Test if the default implementation of the javax.resource.spi.ConnectionManager
     * provided implements the java.io.Serializable interface
     * </p>
     *
     * @paramm descriptor deployment descriptor for the rar file
     * @return result object containing the result of the individual test
     * performed
     */
    public Result check(ConnectorDescriptor descriptor) {
        
        Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        if(!descriptor.getOutBoundDefined())
        {
          result.addNaDetails(smh.getLocalString
              ("tests.componentNameConstructor",
               "For [ {0} ]",
               new Object[] {compName.toString()}));
          result.notApplicable(smh.getLocalString
              ("com.sun.enterprise.tools.verifier.tests.connector.managed.notApplicableForInboundRA",
               "Resource Adapter does not provide outbound communication"));
          return result;
        }
        
        // let's get first the the default implementation of the ConnectionManager
        //File jarFile = Verifier.getJarFile(descriptor.getModuleDescriptor().getArchiveUri());
//        File f=Verifier.getArchiveFile(descriptor.getModuleDescriptor().getArchiveUri());
        Class c = findImplementorOf(descriptor, "javax.resource.spi.ConnectionManager");
        
        if (c!=null) {
            // We found it, let's see if it implements the right interface
            testImplementationOf(c, "java.io.Serializable", result);
            return result;
        }
        // ooppss, no implementation of the default Connection Manager
        result.addErrorDetails(smh.getLocalString
			       ("tests.componentNameConstructor",
				"For [ {0} ]",
				new Object[] {compName.toString()}));
	result.failed(smh.getLocalString
		      ("com.sun.enterprise.tools.verifier.tests.connector.DefaultConnectionManagerExistence.failed", 
		       "Error: There is no default implementation of the [ {0} ] provided",
		       new Object[] {"javax.resource.spi.ConnectionManager"}));                            
        return result;
    }
}
