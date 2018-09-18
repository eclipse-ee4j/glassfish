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

// portnames : verify that all the portnames exist in the WebService
/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids:  JSR109_WS_45; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: Handler Port Name is a valid portname 
 */
public class HandlerPortNameCheck extends WSTest implements WSCheck {

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint descriptor) {

	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        boolean pass = true;

        if (descriptor.hasHandlers()) {
           Collection allPortNames = getAllPortNamesInService(descriptor);
           List<WebServiceHandlerChain> handlerChains = descriptor.getHandlerChain();
           for (WebServiceHandlerChain handlerChain : handlerChains) {
               Collection c = new HashSet();
               for (WebServiceHandler wsh : handlerChain.getHandlers()) {
                   c.addAll(wsh.getPortNames());
               }
               Collection invalid = getInvalidHandlerPortNames(c,allPortNames);
               if (invalid.size() > 0) {
                  //result.fail
                  result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
                 result.failed(smh.getLocalString
                  ("com.sun.enterprise.tools.verifier.tests.webservices.failed",
                   "[{0}]", new Object[] {"The Port Name(s) in the Handler Chain are invalid"}));

                  pass = false;
               }
               else {
                  //result.pass
                  result.addGoodDetails(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]",
                                   new Object[] {compName.toString()}));
                  result.passed(smh.getLocalString (
                  "com.sun.enterprise.tools.verifier.tests.webservices.passed", "[{0}]",
                  new Object[] {"Port Name(s) in the Handler-Chain are valid"}));

               }
           }
        }
        else {
         // result.NotApplicable
         result.addNaDetails(smh.getLocalString
            ("tests.componentNameConstructor", "For [ {0} ]",
             new Object[] {compName.toString()}));
         result.notApplicable(smh.getLocalString
          ("com.sun.enterprise.tools.verifier.tests.webservices.notapp",
           "[{0}]", new Object[] {"Not Applicable since No handlers defined in this WebService"}));

        }

        return result;
    }

   private Collection getAllPortNamesInService(WebServiceEndpoint descriptor) {

       Collection endPoints = descriptor.getWebService().getEndpoints();
       Vector<String> ret = new Vector<String>();
       for (Iterator it = endPoints.iterator(); it.hasNext();) {
           ret.add(((WebServiceEndpoint)it.next()).getEndpointName());
       }
    return ret;    
   }

   private Collection getInvalidHandlerPortNames(Collection hpNames, Collection allPortNames) {
       
      Vector<String> ret = new Vector<String>();
      for (Iterator it = hpNames.iterator(); it.hasNext();) {
          String currName = (String)it.next();
          if (!allPortNames.contains(currName)) 
              ret.add(currName);
      }
    return ret;
   }
 }

