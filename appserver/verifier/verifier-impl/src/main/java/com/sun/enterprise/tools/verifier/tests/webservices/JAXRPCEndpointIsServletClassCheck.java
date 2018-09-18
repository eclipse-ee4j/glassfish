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
 *   @assertion_ids:  JSR109_WS_20; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: Service Implementations using a JAX-RPC Service Endpoint must be defined 
 *   in the web.xml deployment descriptor file using the servlet-class element.
 */
public class JAXRPCEndpointIsServletClassCheck extends WSTest implements WSCheck {

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint wsdescriptor) {

	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        ClassLoader loader = getVerifierContext().getClassLoader();

        if (wsdescriptor.implementedByWebComponent()) {
            WebBundleDescriptor webBundle = (WebBundleDescriptor)wsdescriptor.getBundleDescriptor();
            WebComponentDescriptor webComponent =
                (WebComponentDescriptor) webBundle.
                  getWebComponentByCanonicalName(wsdescriptor.getWebComponentLink());
            if( webComponent != null && webComponent.isServlet()) {
                String servletClass = wsdescriptor.getWebComponentImpl().
                                      getWebComponentImplementation();
                if ((servletClass == null) || (!wsdescriptor.getWebComponentImpl().isServlet())) {
                     //result.fail, webcomponentimpl for webservice is not servlet
                      result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
                      result.failed(smh.getLocalString
                      ("com.sun.enterprise.tools.verifier.tests.webservices.failed", "[{0}]",
                       new Object[] {"The WebComponent implementation for this JAX-RPC endpoint is not a servlet"}));

                }
                else {
                     // try to load the servlet class
                  try {
                    Class cl = Class.forName(servletClass, false, getVerifierContext().getClassLoader());
                      //result.pass
                      result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
                      result.passed(smh.getLocalString (
                          "com.sun.enterprise.tools.verifier.tests.webservices.clpassed", 
                          "The [{0}] Class [{1}] exists and was loaded successfully.",
                           new Object[] {"Servlet Class", servletClass}));

                  }catch (ClassNotFoundException e) {
                      //result.fail could not find servlet class
                      result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                            "For [ {0} ]", new Object[] {compName.toString()}));
                      result.failed(smh.getLocalString (
                            "com.sun.enterprise.tools.verifier.tests.webservices.clfailed",
                            "The [{0}] Class [{1}] could not be Loaded",
                             new Object[] {"Servlet Class", servletClass}));

                  }
                }
            }
            else {
                //result.fail, servlet-link could not be resolved
                result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
                result.failed(smh.getLocalString
                  ("com.sun.enterprise.tools.verifier.tests.webservices.failed", "[{0}]",
                  new Object[] {"The servlet-link for this JAX-RPC Endpoint could not be resolved"}));
 
            }
        }
        else {
          //result.notapplicable
          result.addNaDetails(smh.getLocalString
                     ("tests.componentNameConstructor", "For [ {0} ]",
                      new Object[] {compName.toString()}));
          result.notApplicable(smh.getLocalString
                 ("com.sun.enterprise.tools.verifier.tests.webservices.notapp",
                 "[{0}]", new Object[] {"Not Applicable since this is NOT a JAX-RPC Service Endpoint"}));

        }

        return result;
    }
 }

