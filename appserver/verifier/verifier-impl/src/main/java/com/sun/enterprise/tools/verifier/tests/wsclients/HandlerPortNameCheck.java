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
import java.util.*;
import com.sun.enterprise.tools.verifier.tests.*;
import java.lang.reflect.*;

/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids:  JSR109_WS_56; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: Handler Port Names are valid
 */

// portnames : verify that all the portnames exist in the WebService
public class HandlerPortNameCheck extends WSClientTest implements WSClientCheck {

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (ServiceReferenceDescriptor descriptor) {

	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        if (descriptor.hasHandlers()) {
           Collection allPortNames = getAllPortNamesInService(descriptor);
           List handlerChain = descriptor.getHandlerChain();
           for (Iterator it = handlerChain.iterator(); it.hasNext();) {
               Collection c = ((WebServiceHandler)it.next()).getPortNames();
               Collection invalid = getInvalidHandlerPortNames(c,allPortNames);
               if (invalid.size() > 0) {
                  //result.fail
                  result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
                  result.failed(smh.getLocalString
                    (getClass().getName() + ".failed",
                     "The Port Name(s) in the Handler Chain are invalid."));
               }
               else {
                  //result.pass
                  result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
                  result.passed(smh.getLocalString (getClass().getName()+ ".passed",
                  "Port Name(s) in the Handler-Chain are valid."));

               }
           }
        }
        else {
         // result.NotApplicable
         result.addNaDetails(smh.getLocalString
            ("tests.componentNameConstructor", "For [ {0} ]", new Object[] {compName.toString()}));
         result.notApplicable(smh.getLocalString (getClass().getName() + ".notapp",
           "Not Applicable since No handlers defined in this WebService."));
        }

        return result;
    }

   private Collection getAllPortNamesInService(ServiceReferenceDescriptor descriptor) {

       Collection endPoints = descriptor.getPortsInfo();
       Vector<String>  ret = new Vector<String>();
       for (Iterator it = endPoints.iterator(); it.hasNext();) {
           ret.add(((ServiceRefPortInfo)it.next()).getPortComponentLinkName());
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

