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
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids: JSR109_WS_55; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: The port-component-link element links a port-component-ref to a 
 *   specific port-component required to be made available by a service reference. 
 *   The value of a port-component-link must be the port-component-name of a port-component 
 *   in the same module or another module in the same application unit. The syntax for 
 *   specification follows the syntax defined for ejb-link in the EJB 2.0 specification.
 */

public class PortComponentLinkValidCheck  extends WSClientTest implements WSClientCheck {
    ComponentNameConstructor compName;

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (ServiceReferenceDescriptor descriptor) {

	Result result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();

        boolean pass = true;

        Collection ports = descriptor.getPortsInfo();

        for (Iterator it=ports.iterator(); it.hasNext();) {
            ServiceRefPortInfo ref = (ServiceRefPortInfo)it.next();

            // check if this test is applicable first
            if (!ref.hasPortComponentLinkName()) { 
              //result.notapplicable, since port-comp-link does not exist in port-comp-ref
              result.addNaDetails(smh.getLocalString
                     ("tests.componentNameConstructor", "For [ {0} ]",
                      new Object[] {compName.toString()}));
              result.notApplicable(smh.getLocalString
                 ( getClass().getName() + ".notapp",
                 "Not applicable since port-comp-link does not exist in port-comp-ref [{0}].",
                  new Object[] {ref.getName()}));
               } 

               else if (ref.getPortComponentLink() != null) {
               pass = true; 
               } 
               else if (!isLinkValid(ref)) {
                     //result.fail ref.getName(), ref.getPortComponentLinkName()
                       result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                       "For [ {0} ]", new Object[] {compName.toString()}));
                       result.failed(smh.getLocalString (getClass().getName() + ".failed",
                       "Invalid port-component-link [{0}] in WebService client [{1}].",
                        new Object[] {ref.getPortComponentLinkName(),compName.toString()}));
                        pass = false;
                }  
              
        }
        if (pass) {
              //result.pass
              result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
              result.passed(smh.getLocalString (getClass().getName() + ".passed",
             "All port-component-link(s) in this service reference are valid."));
        }
        return result;
    }

   private boolean isLinkValid(ServiceRefPortInfo ref) {
   boolean pass = true;

   WebServiceEndpoint port = null;

      String linkName = ref.getPortComponentLinkName();
// == get the application
     Application application =
                 ref.getServiceReference().getBundleDescriptor().getApplication();

      if(  (linkName != null) && (linkName.length() > 0) && (application != null) )    { 
         int hashIndex = linkName.indexOf('#');
//         boolean absoluteLink = (hashIndex != -1);
         // Resolve <module>#<port-component-name> style link
         String relativeModuleUri = linkName.substring(0, hashIndex);
         String portName = linkName.substring(hashIndex + 1);
// == get bundle(s)
         Set webBundles = application.getBundleDescriptors(WebBundleDescriptor.class);
         Set ejbBundles = application.getBundleDescriptors(EjbBundleDescriptor.class);
// ==
         // iterate through the ejb jars in this J2EE Application
         Iterator ejbBundlesIterator = ejbBundles.iterator();
         EjbBundleDescriptor ejbBundle = null;
// == while...
         while (ejbBundlesIterator.hasNext()) {
         ejbBundle = (EjbBundleDescriptor)ejbBundlesIterator.next();
//         if (Verifier.getEarFile() != null){
           try {
              String archiveuri = ejbBundle.getModuleDescriptor().getArchiveUri();
              if ( relativeModuleUri.equals(archiveuri) ) {
              LinkedList<EjbBundleDescriptor> bundles = new LinkedList<EjbBundleDescriptor>();
                 bundles.addFirst(ejbBundle);
                 for(Iterator iter = bundles.iterator(); iter.hasNext();) {
                    BundleDescriptor next = (BundleDescriptor) iter.next();
                    port = next.getWebServiceEndpointByName(portName);
                    if( port != null ) {
                       pass = true;     
                       break;
                    }
                 }
              }
            }catch(Exception e) {}
//          }
          } // while block

         // iterate through the wars in this J2EE Application
         Iterator webBundlesIterator = webBundles.iterator();
         WebBundleDescriptor webBundle = null;
// == while...
         while (webBundlesIterator.hasNext()) {
         webBundle = (WebBundleDescriptor)webBundlesIterator.next();
//         if (Verifier.getEarFile() != null){
           try {
              String archiveuri = webBundle.getModuleDescriptor().getArchiveUri();
              if ( relativeModuleUri.equals(archiveuri) ) {
              LinkedList<WebBundleDescriptor> bundles = new LinkedList<WebBundleDescriptor>();
                 bundles.addFirst(webBundle);
                 for(Iterator iter = bundles.iterator(); iter.hasNext();) {
                    BundleDescriptor next = (BundleDescriptor) iter.next();
                    port = next.getWebServiceEndpointByName(portName);
                    if( port != null ) {
                       pass = true;    
                       break;
                    }
                 }
              }
            }catch(Exception e) {}
//          }
          } // while block
       } // 
       if ( port == null)
          pass = false;
     return pass; 

    } // end of method 

 }
