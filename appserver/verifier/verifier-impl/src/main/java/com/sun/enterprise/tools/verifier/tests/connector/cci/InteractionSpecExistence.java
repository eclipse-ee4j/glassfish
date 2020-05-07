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
 * InteractionSpecExistence.java
 *
 * Created on October 5, 2000, 10:32 AM
 */

package com.sun.enterprise.tools.verifier.tests.connector.cci;

import java.io.File;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorCheck;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorTest;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;

/**
 * Test if a jakarta.resource.cci.InterfactionSpec implementation has been provided in
 * the rar file
 *
 * @author  Jerome Dochez
 * @version 
 */
public class InteractionSpecExistence 
    extends ConnectionFactoryTest 
    implements ConnectorCheck 
{

    /** <p>
     * Test if a jakarta.resource.cci.InterfactionSpec implementation has been 
     * provided in the rar file
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
            findImplementorOf(descriptor, "jakarta.resource.cci.InteractionSpec", result);
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
