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
 *   @assertion_ids:  JSR109_WS_19; JSR109_WS_22; JSR109_WS_23; JSR109_WS_33; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: Service Implementations using a stateless session bean must be 
 *   defined in the ejb-jar.xml deployment descriptor file using the session element.
 *
 *   The developer declares the implementation of the Web service using 
 *   the service-impl-bean element of the deployment descriptor.
 *
 *   For a stateless session bean implementation, the ejb-link element associates the 
 *   port-component with a session element in the ejb-jar.xml. The ejb-link element may 
 *   not refer to a session element defined in another module.
 *
 *   The service-impl-bean element defines the Web service implementation. A service 
 *   implementation can be an EJB bean class or JAX-RPC web component.
 */

public class ServiceImplBeanLinkCheck extends WSTest implements WSCheck {

    public boolean resolveComponentLink(WebServiceEndpoint desc, Result result) {
        boolean resolved = false;
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        if( desc.implementedByEjbComponent()) {
            EjbBundleDescriptor ejbBundle = (EjbBundleDescriptor)desc.getBundleDescriptor();
            if( ejbBundle.hasEjbByName(desc.getEjbLink())) {
                EjbDescriptor ejb = ejbBundle.getEjbByName(desc.getEjbLink());
                if (ejb != null) {
                    resolved = true;
                    //result.pass , ejb-link resolved
                     result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
                     result.passed(smh.getLocalString (getClass().getName() + ".passed",
                    "[{0}] link of service-impl-bean element resolved successfully.",
                           new Object[] {desc.getEjbLink()}));

                 }
                 else {
                   //result.fail, ejb-link could not be resolved
                    result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
                    result.failed(smh.getLocalString (getClass().getName() + ".failed",
                    "Could not resolve [{0}] link of service-impl-bean element.",
                  new Object[] {desc.getEjbLink()}));

                 }
            }
        } else if( desc.implementedByWebComponent()) {
            WebBundleDescriptor webBundle = (WebBundleDescriptor)desc.getBundleDescriptor();
            WebComponentDescriptor webComponent =
                (WebComponentDescriptor) webBundle.
                  getWebComponentByCanonicalName(desc.getWebComponentLink());
            if( webComponent != null && webComponent.isServlet()) {
                resolved = true;
                //result.pass servlet-link resolved
                result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                         "For [ {0} ]", new Object[] {compName.toString()}));
                result.passed(smh.getLocalString (getClass().getName() + ".passed",
                 "[{0}] link of service-impl-bean element resolved successfully.",
                new Object[] {desc.getWebComponentLink()}));
            }
            else {
                   //result.fail, servlet-link could not be resolved
                   result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                          "For [ {0} ]", new Object[] {compName.toString()}));
                   result.failed(smh.getLocalString (getClass().getName() + ".failed",
                   "Could not resolve [{0}] link of service-impl-bean element.",
                   new Object[] {desc.getWebComponentLink()}));
            }
        }
        return resolved;
    }

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint wsdescriptor) {

	Result result = getInitializedResult();
        boolean pass = resolveComponentLink(wsdescriptor, result);
        return result;
    }
 }

