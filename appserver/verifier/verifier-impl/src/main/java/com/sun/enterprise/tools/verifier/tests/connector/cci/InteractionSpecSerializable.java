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
 * InteractionSpecSerializable.java
 *
 * Created on November 7, 2000, 5:05 PM
 */

package com.sun.enterprise.tools.verifier.tests.connector.cci;

import java.io.File;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorCheck;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorTest;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;

/**
 * Test whether the implementation of the InteractionSpec interface 
 * also implements the Serializable interface
 *
 * @author  Jerome Dochez
 * @version 
 */
public class InteractionSpecSerializable     
    extends ConnectionFactoryTest 
    implements ConnectorCheck 
{
    /** <p>
     * Test if the jakarta.resource.cci.InterfactionSpec implementation provided
     * in the rar file also implements the java.io.Serializable interface
     * </p>
     *
     * @paramm descriptor deployment descriptor for the rar file
     * @return result object containing the result of the individual test
     * performed
     */
    public Result check(ConnectorDescriptor descriptor) {
                
        Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
        if (isCCIImplemented(descriptor, result)) {
            //File jarFile = Verifier.getJarFile(descriptor.getModuleDescriptor().getArchiveUri());
//            File f=Verifier.getArchiveFile(descriptor.getModuleDescriptor().getArchiveUri());
            Class is = findImplementorOf(descriptor, "jakarta.resource.cci.InteractionSpec");
            if (is !=null) {
                testImplementationOf(is, "java.io.Serializable", result);
                return result;
            } else {
		result.addNaDetails(smh.getLocalString
				  ("tests.componentNameConstructor",
				   "For [ {0} ]",
				   new Object[] {compName.toString()}));
                result.notApplicable(smh.getLocalString
    	            ("com.sun.enterprise.tools.verifier.tests.connector.cci.InteractionSpecJavaBeansCompliance.nonexist",
                    "Error: While the CCI interfaces are implemented, the jakarta.resource.cci.InteractionSpec is not"));         
                return result;            
            }
        } else {
	    result.addNaDetails(smh.getLocalString
				  ("tests.componentNameConstructor",
				   "For [ {0} ]",
				   new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
    	        ("com.sun.enterprise.tools.verifier.tests.connector.cci.InteractionExistence.notapp",
                 "NotApplicable : The CCI interfaces do not seem to be implemented by this resource adapter"));                    
        }
        return result;
    }
}
