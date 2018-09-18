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
 *   @assertion_ids:  JSR109_WS_19; JSR109_WS_23; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: Service Implementations using a stateless session bean must be defined 
 *   in the ejb-jar.xml deployment descriptor file using the session element.
 *
 *   For a stateless session bean implementation, the ejb-link element 
 *   associates the port-component with a session element in the ejb-jar.xml. The ejb-link 
 *   element may not refer to a session element defined in another module.
 */

public class EJBEndpointIsSLSBCheck extends WSTest implements WSCheck {

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint wsdescriptor) {

	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        if (wsdescriptor.implementedByEjbComponent()) {
            EjbDescriptor ejbdesc = wsdescriptor.getEjbComponentImpl();

            if (ejbdesc == null) {

               result.addErrorDetails(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]",
                                   new Object[] {compName.toString()}));
                 result.failed(smh.getLocalString
                   (getClass().getName() + ".failed1",
                    "Service Implementation bean Could Not be Resolved from the ejb-link specified"));
               return result;
             }

            if (ejbdesc instanceof EjbSessionDescriptor) {
               EjbSessionDescriptor session = (EjbSessionDescriptor)ejbdesc;
               if (EjbSessionDescriptor.STATELESS.equals(session.getSessionType())) {
                   result.addGoodDetails(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]",
                                   new Object[] {compName.toString()}));
                   result.passed(smh.getLocalString
                   (getClass().getName() + ".passed",
                   "Service Implementation bean defined in ejb-jar.xml using {0} session element",                   new Object[] {"stateless"}));
               }
               else {
                 // result.fail, endpoint can be a stateful session bean
                 result.addErrorDetails(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]",
                                   new Object[] {compName.toString()}));
                 result.failed(smh.getLocalString
                   (getClass().getName() + ".failed",
                    "Service Implementation bean cannot be Stateful Session Bean"));
               }
            }
            else {
              // result.fail, service endpoint should be Session Bean
              result.addErrorDetails(smh.getLocalString
                     ("tests.componentNameConstructor", "For [ {0} ]",
                      new Object[] {compName.toString()}));
              result.failed(smh.getLocalString
                 (getClass().getName() + ".failed2",
                 "Service Implementation bean Should be a Session Bean"));
            }
  
        }
        else {

          // result.notapp
          result.addNaDetails(smh.getLocalString
                     ("tests.componentNameConstructor", "For [ {0} ]",
                      new Object[] {compName.toString()}));
          result.notApplicable(smh.getLocalString
                 (getClass().getName() + ".notapp",
                 "This is a JAX-RPC Service Endpoint"));
        }

        return result;
    }
 }

