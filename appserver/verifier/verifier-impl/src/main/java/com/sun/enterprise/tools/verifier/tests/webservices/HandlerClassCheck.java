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

// Make sure we are able to load the Handler classes
/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids: JSR109_WS_29; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: handler-class Defines a fully qualified class name for the handler 
 *   implementation and implements javax.xml.rpc.handler.Handler.
 */
public class HandlerClassCheck extends WSTest implements WSCheck {

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint descriptor) {

	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean pass = true;

        if (descriptor.hasHandlers()) {
           List handlers = descriptor.getHandlers();
           for (Iterator it = handlers.iterator(); it.hasNext();) {
                String hClass = ((WebServiceHandler)it.next()).getHandlerClass();
                Class cl = null;
                try {
                  cl = Class.forName(hClass, false, getVerifierContext().getClassLoader());
                  if ((cl != null) && ((javax.xml.rpc.handler.Handler.class).isAssignableFrom(cl))) {
                    //result.pass
                    result.addGoodDetails(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]",
                                   new Object[] {compName.toString()}));
                    result.passed(smh.getLocalString (getClass().getName() + ".passed", 
             "The Handler Class [{0}] exists and implements the javax.xml.rpc.handler.Handler Interface.",
                     new Object[] {hClass}));

                  }
                  else {
                       //result.fail, handler class does not extend javax.xml.rpc.handler.Handler
                        result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                         "For [ {0} ]", new Object[] {compName.toString()}));
                        result.failed(smh.getLocalString (getClass().getName() + ".failed", 
                        "Handler Class [{0}] does not implement javax.xml.rpc.handler.Handler",
                        new Object[] {hClass}));
                        pass = false;

                  }
                }
                catch (ClassNotFoundException e) {
                 // result.fail, handler class not found
                 result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                 "For [ {0} ]", new Object[] {compName.toString()}));
                 result.failed(smh.getLocalString (
                 "com.sun.enterprise.tools.verifier.tests.webservices.clfailed",
                 "The [{0}] Class [{1}] could not be Loaded",
                  new Object[] {"Handler Class", hClass}));
                }
            }
          }
          else {
             result.addNaDetails(smh.getLocalString
                     ("tests.componentNameConstructor", "For [ {0} ]",
                      new Object[] {compName.toString()}));
             result.notApplicable(smh.getLocalString
                 ( getClass().getName() + ".notapp",
                 "Not Applicable since No handlers defined in this WebService"));

            }

        return result;
    }
 }

