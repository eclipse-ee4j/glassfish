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
 *   @assertion_ids: JSR109_WS_27; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: The urlpattern of the servlet-mapping must be an exact match pattern 
 *   (i.e. it must not contain an asterisk (?*?)).
 */

public class ServletUrlPatternExactCheck extends WSTest implements WSCheck {

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint desc) {

	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        boolean pass = true;

        if (desc.implementedByWebComponent()) {
            WebBundleDescriptor webBundle = (WebBundleDescriptor)desc.getBundleDescriptor();
            WebComponentDescriptor webComponent =
                (WebComponentDescriptor) webBundle.
                getWebComponentByCanonicalName(desc.getWebComponentLink());
            if(webComponent != null && webComponent.isServlet()) {
                Enumeration en = webComponent.getUrlPatterns();
                while(en.hasMoreElements()) {
                  String pattern =(String) en.nextElement();
                  if (pattern.indexOf("*") == -1) {
                     // result.pass
                      result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
                      result.passed(smh.getLocalString (getClass().getName() + ".passed",
                      "The urlpattern for this servlet-mapping [{0}] is exact.",
                      new Object[] {pattern}));
                     
                  }
                  else {
                     // result.fail
                     result.addErrorDetails(smh.getLocalString
                                  ("tests.componentNameConstructor", "For [ {0} ]",
                                   new Object[] {compName.toString()}));
                     result.failed(smh.getLocalString (getClass().getName() + ".failed",
                     "The urlpattern for this servlet-mapping [{0}] contains '*' and is not exact.",
                     new Object[] {pattern}));

                     pass = false;
                  }
                  // we assume there is one servlet-mapping specified, 
                  // this check is done in another test
                  break;
                }
            }
         }
         else {
           //result.notapp
            result.addNaDetails(smh.getLocalString
                     ("tests.componentNameConstructor", "For [ {0} ]",
                      new Object[] {compName.toString()}));
            result.notApplicable(smh.getLocalString
                 (getClass().getName() + ".notapp",
                 "Not Applicable since this not a JAX-RPC Service Endpoint."));
         }

        return result;
    }
 }

