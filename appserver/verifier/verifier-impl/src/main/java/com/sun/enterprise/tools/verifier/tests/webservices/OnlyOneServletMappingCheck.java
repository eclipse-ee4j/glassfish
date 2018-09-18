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

/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids: JSR109_WS_26; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: No more than one servlet mapping may be specified for a servlet that is 
 *   linked to by a port-component.
 */

public class OnlyOneServletMappingCheck extends WSTest implements WSCheck {

    /**
     * @param desc the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint desc) {

	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        if (desc.implementedByWebComponent()) {
            WebBundleDescriptor webBundle = (WebBundleDescriptor)desc.getBundleDescriptor();
            WebComponentDescriptor webComponent =
                (WebComponentDescriptor) webBundle.
                getWebComponentByCanonicalName(desc.getWebComponentLink());
            if(webComponent != null && webComponent.isServlet()) {
               int sz = getSize(webComponent.getUrlPatternsSet());
               if (sz == 0) {
                  //result.fail , no servlet-mapping for servlet linked to port-component
                  result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
                  result.failed(smh.getLocalString
                       (getClass().getName() + ".failed",
                       "Found [{0}] servlet mappings for the servlet linked to by this port-component.",
                       new Object[] {"0"}));
               }
               if (sz > 1) {
                  //result.fail , more than one servlet-mapping for servlet linked to port-component
                  result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
                  result.failed(smh.getLocalString
                       (getClass().getName() + ".failed",
                       "Found [{0}] servlet mappings for the servlet linked to by this port-component.",
                       new Object[] {Integer.toString(sz)}));
                } 
                else {
                  //result.pass , one servlet-mapping for servlet linked to port-component
                   result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
                   result.passed(smh.getLocalString (getClass().getName() + ".passed",
                   "Found only one servlet mapping for the servlet linked to by this port-component."));

                }
            }
         }
         else {

            result.addNaDetails(smh.getLocalString
                     ("tests.componentNameConstructor", "For [ {0} ]",
                      new Object[] {compName.toString()}));
            result.notApplicable(smh.getLocalString(getClass().getName() + ".notapp",
                 " Not applicable since this is Not a JAX-RPC Service Endpoint."));

         }

        return result;
    }

    /**
     * This is a hack, since descriptors from backend contain
     * an extra url pattern.
     * @param urlPatterns
     * @return
     */
    private int getSize(Set urlPatterns) {
        int size = urlPatterns.size();
        if (getVerifierContext().isAppserverMode()) //only if backend
            for (Object url : urlPatterns) {
                String urlPattern = (String)url;
                if(urlPattern.indexOf("__container") != -1)
                    size--;
            }
        return size;
    }
 }

