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

package com.sun.enterprise.tools.verifier.tests.wsclients;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import java.util.*;

/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids: JSR109_WS_57; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: The service-endpoint-interface element defines a fully qualified Java class 
 *   that represents the Service Endpoint Interface of a WSDL port.
 */

public class PortCompRefSEIClassCheck  extends WSClientTest implements WSClientCheck {
    ComponentNameConstructor compName;

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (ServiceReferenceDescriptor descriptor) {

	Result result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();

        boolean pass = true;

        Collection ports = descriptor.getPortsInfo();

        for (Iterator it=ports.iterator(); it.hasNext();) {
            ServiceRefPortInfo ref = (ServiceRefPortInfo)it.next();
            if (!loadSEIClass(ref,result)) {
               //result.fail ref.getName(), ref.getServiceEndpointInterface
               result.addErrorDetails(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
               result.failed(smh.getLocalString
                   (getClass().getName() + ".failed",
                    "Error: Service Endpoint Interface class [ {0} ]  not found.",
                    new Object[] {ref.getServiceEndpointInterface()}));
               
               pass = false;
            }
            else {
              //result.pass
              result.addGoodDetails(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
              result.passed(smh.getLocalString (getClass().getName() + ".passed",
              "Service Endpoint Interface class [ {0} ]  found.", 
              new Object[] {ref.getServiceEndpointInterface()}));

            }
        }

        return result;
    }

   private boolean loadSEIClass(ServiceRefPortInfo ref, Result result) {

     boolean pass = true;

     if (ref.hasServiceEndpointInterface()) {
        try {
              Class.forName(ref.getServiceEndpointInterface(), false, getVerifierContext().getClassLoader());
           } catch (ClassNotFoundException e) {
               Verifier.debug(e);
               pass = false;
           }
     }
     else {
       //result.not applicable (SEI not specified)
       result.addNaDetails(smh.getLocalString
                     ("tests.componentNameConstructor", "For [ {0} ]",
                      new Object[] {compName.toString()}));
       result.notApplicable(smh.getLocalString
                 ( getClass().getName() + ".notapp",
                 "Not applicable since Service reference does not specify an SEI."));

     }
    return pass;
   }
 }

