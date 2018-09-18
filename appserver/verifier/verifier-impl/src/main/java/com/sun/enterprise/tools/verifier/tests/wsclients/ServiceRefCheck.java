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
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids:  JSR109_WS_49; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: The service-interface element declares the fully qualified class name of 
 *   the JAX-RPC Service interface the client depends on. In most cases the value will be 
 *   javax.xml.rpc.Service. A JAX-RPC generated Service Interface class may also be specified.
 */

public class ServiceRefCheck extends WSClientTest implements WSClientCheck {

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (ServiceReferenceDescriptor descriptor) {

	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean pass = true;

        if (descriptor.hasGenericServiceInterface()) {
           //result.pass , has generic service interface
           result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
           result.passed(smh.getLocalString (getClass().getName() + ".passed",
           "The JAX-RPC Service interface the client depends on is the Generic Service Interface."));

           
        }
        else if (descriptor.hasGeneratedServiceInterface()) {
           String intf = descriptor.getServiceInterface();
           try {
             Class cl = Class.forName(intf, false, getVerifierContext().getClassLoader());
               // result.pass
             result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
             result.passed(smh.getLocalString (getClass().getName() + ".passed1",
             "The JAX-RPC Service interface the client depends on is a Generated Service Interface [{0}].",
              new Object[] {intf}));

           }catch (ClassNotFoundException e) {
            //result.fail; Generated service interface class does not exist
            result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
            "For [ {0} ]", new Object[] {compName.toString()}));
            result.failed(smh.getLocalString (getClass().getName() + ".failed",
            "The JAX-RPC Service interface the client depends on [{0}] could not be loaded.",
            new Object[] {intf}));

            pass = false;
          }
        }
        else {
          //result.internal error, its neither type, or error in XML 
          result.addErrorDetails(smh.getLocalString
               ("com.sun.enterprise.tools.verifier.tests.webservices.Error",
                "Error: Unexpected error occurred [ {0} ]",
                new Object[] {"Service Interface Neither Generic nor Generated"}));
        }

        return result;
    }
 }

