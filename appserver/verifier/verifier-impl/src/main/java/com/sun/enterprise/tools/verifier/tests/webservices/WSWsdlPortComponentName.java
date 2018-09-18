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

package com.sun.enterprise.tools.verifier.tests.webservices;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
/* 
 *   @class.setup_props: ; 
 */ 
/*  
 *   @testName: check  
 *   @assertion_ids: 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription:  
 *                     
 *     from Web Services for J2EE, Version 1.0
 *     Section 7.1.2
 *     The developer is responsible for providing the following information in the webservices.xml deployment descriptor:
 *     " Port s name. A logical name for the port must be specified by the developer using the port-component-name element.
 *     This name bears no relationship to the WSDL port name. 
 *     This name must be unique amongst all port component names in a module.
 *     - verify that port-component-name appears in the webservices.xml file
 *     - verify that the name is unique.  Note: current dol issue with uniqueness test
 *                                        it does not return non unique endpoints.
 */

public class WSWsdlPortComponentName extends WSTest implements WSCheck {

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint descriptor) {

	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean pass = true;
        String portcomponentname = null;
        try {

           portcomponentname = descriptor.getEndpointName();
           if ( portcomponentname == null || portcomponentname.equals("") ) 
              pass = false;

           if (pass) {
              result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
              result.passed(smh.getLocalString (getClass().getName() + ".passed",
                          "The webservices.xml file for [ {0} ] has the port-component-name element specified.",
                           new Object[] {compName.toString()}));
            }
            else {
             result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
             result.failed(smh.getLocalString (getClass().getName() + ".failed",
               "The webservices.xml file for [ {0} ] does not have the port-component-name element specified.",
                new Object[] {compName.toString()}));
            }
        }catch (Exception e) {
            //result.fail
            result.addErrorDetails(smh.getLocalString
               ("com.sun.enterprise.tools.verifier.tests.webservices.Error",
                "Error: Unexpected error occurred [ {0} ]",
                new Object[] {e.getMessage()}));
        }
        return result;
    }
}

