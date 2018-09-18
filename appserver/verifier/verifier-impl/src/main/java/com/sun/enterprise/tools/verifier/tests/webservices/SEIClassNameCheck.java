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

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import java.util.*;
import com.sun.enterprise.tools.verifier.tests.*;
import java.lang.reflect.*;

/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids: JSR109_WS_15; JSR109_WS_24;
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: The developer is responsible for packaging, either by containment or 
 *   reference, the WSDL file, Service Endpoint Interface class, Service Implementation Bean 
 *   class, and their dependent classes, JAX-RPC mapping file along with a Web services 
 *   deployment descriptor in a J2EE module.
 *
 *   The developer must specify the fully qualified class name of the Service Endpoint 
 *   Interface in the service-endpoint-interface element.
 */

public class SEIClassNameCheck extends WSTest implements WSCheck {

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint wsdescriptor) {

	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        Class cl = loadSEIClass(wsdescriptor,result);
        if (cl == null) {
           result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
           result.failed(smh.getLocalString
                (getClass().getName() + ".failed",
                "The [{0}] Class [{1}] could not be Loaded.",
                 new Object[] {"SEI", wsdescriptor.getServiceEndpointInterface()}));

        }
        else {

            result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
            result.passed(smh.getLocalString (getClass().getName() + ".passed",
                          "The [{0}] Class [{1}] Loaded Successfully.",
                           new Object[] {"SEI",wsdescriptor.getServiceEndpointInterface()}));

        }

        return result;
    }
 }

