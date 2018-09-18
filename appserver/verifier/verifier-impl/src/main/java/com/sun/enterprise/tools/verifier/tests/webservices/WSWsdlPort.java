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
import javax.xml.namespace.QName;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids: 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription:  
 *                     Requirement from Web Service for J2EE, Version 1.0
 *                     Section 7.1.2
 *                    " Port s QName. In addition to specifying the WSDL document, 
 *                     the developer must also specify the WSDL port QName in the 
 *                     wsdl-port element for each Port defined in the deployment descriptor.
 *                     Requirement from Web Service for J2EE, Version 1.0
 *                     7.1.5 Web Services Deployment Descriptor DTD
 *                    <!-- The port-component element associates a WSDL port with a 
 *                    Web service interface and implementation. It defines the name 
 *                    of the port as a component, optional description, optional display name, 
 *                    optional iconic representations, WSDL port QName, Service Endpoint Interface, 
 *                    Service Implementation Bean. Used in: webservices --> 
 *                    <!ELEMENT port-component (description?, display-name?, small-icon?, large-icon?, 
 *                    port-component-name, wsdl-port, service-endpoint-interface, service-impl-bean, handler*)>
 */

public class WSWsdlPort extends WSTest implements WSCheck {
    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint descriptor) {
        
	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean pass = true;
        try {

          javax.xml.namespace.QName wsdlport = descriptor.getWsdlPort();
          // check to see that wsdl-port is specified in the service endpoint.
          if ( wsdlport != null) { 
             // get the local part 
             String localpart = wsdlport.getLocalPart();
             // String namespaceuri = wsdlport.getNamespaceURI();

             if ( localpart == null || localpart.equals("") ) { 
                // Error: localpart is not specified
               pass = false;
             }
             
              //if ( namespaceuri == null || namespaceuri.equals("")) { 
              //pass = false;
              //}

          } else {
            // Error: wsdl-port is missing for this endpoint
            pass = false;
          }

          if (pass) {
              result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
              result.passed(smh.getLocalString (getClass().getName() + ".passed",
                          "The wsdl-port in the webservices.xml file for [{0}] is specified for the endpoint",
                           new Object[] {compName.toString()}));
            }
            else {
             result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
             result.failed(smh.getLocalString (getClass().getName() + ".failed",
               "The  wsdl-port in the webservices.xml file for [{0}] is not correctly specified for the endpoint",
                new Object[] {compName.toString()}));
            }
        }catch (Exception e) {
            //result.fail
            result.addErrorDetails(smh.getLocalString
               ("com.sun.enterprise.tools.verifier.tests.webservices.Error",
                "Error: Unexpected error occurred [ {0} ]",
                new Object[] {e.getMessage()}));
        }
        return result;
    }
}
