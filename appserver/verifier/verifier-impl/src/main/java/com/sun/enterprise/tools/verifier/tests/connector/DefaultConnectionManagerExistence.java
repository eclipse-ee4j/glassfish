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
 * DefaultConnectionManagerExistence.java
 *
 * Created on September 26, 2000, 3:56 PM
 */

package com.sun.enterprise.tools.verifier.tests.connector;

import java.io.File;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;



/**
 * Test if a default ConnectionManager has been provided in the rar file
 *
 * @author  Jerome Dochez
 * @version 
 */
public class DefaultConnectionManagerExistence 
    extends ConnectorTest 
    implements ConnectorCheck 
{

    /** <p>
     * all connector tests should implement this method. it run an individual
     * test against the resource adapter deployment descriptor.
     * </p>
     *
     * @paramm descriptor deployment descriptor for the rar file
     * @return result object containing the result of the individual test
     * performed
     */
    public Result check(ConnectorDescriptor descriptor) {
                
        Result result = getInitializedResult();
        ComponentNameConstructor compName = 
          getVerifierContext().getComponentNameConstructor();
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
	        
        //File jarFile = Verifier.getJarFile(descriptor.getModuleDescriptor().getArchiveUri());
//        File f=Verifier.getArchiveFile(descriptor.getModuleDescriptor().getArchiveUri());     
        findImplementorOf(descriptor, "javax.resource.spi.ConnectionManager", result);
        return result;
    }
}
