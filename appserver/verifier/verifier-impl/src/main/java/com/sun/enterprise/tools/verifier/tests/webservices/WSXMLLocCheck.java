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
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deploy.shared.FileArchive;

import java.io.*;

/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids: JSR109_WS_16; JSR109_WS_17; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: The Web services deployment descriptor location within the EJB-JAR file 
 *   is META-INF/webservices.xml.
 *
 *   A Web services deployment descriptor is located in a WAR at WEB-INF/webservices.xml.
 */

public class WSXMLLocCheck extends WSTest implements WSCheck {

    // webservices.xml
    private String ejbWSXmlLoc = "META-INF/webservices.xml";
    private String jaxrpcWSXmlLoc = "WEB-INF/webservices.xml";

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint descriptor) {

	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

//        boolean pass = true;

//        File f = Verifier.getArchiveFile(descriptor.getBundleDescriptor().
//                 getModuleDescriptor().getArchiveUri());
//        JarFile jarFile = null;
        InputStream deploymentEntry=null;

        try {
//             if (f == null) {
              String uri = getAbstractArchiveUri(descriptor);
//              try {
                 FileArchive arch = new FileArchive();
                 arch.open(uri);
                 if (descriptor.implementedByEjbComponent()) {
                    deploymentEntry = arch.getEntry(ejbWSXmlLoc);
                 }
                 else if (descriptor.implementedByWebComponent()) {
                    deploymentEntry = arch.getEntry(jaxrpcWSXmlLoc);
                 }
                 else {
                    throw new Exception("Niether implemented by EJB nor by WEB Component");
                 }
//               }catch (IOException e) { throw e;}
//             }
//             else {
//
//               jarFile = new JarFile(f);
//               ZipEntry deploymentEntry1 = null;
//               if (descriptor.implementedByEjbComponent()) {
//                   deploymentEntry1 = jarFile.getEntry(ejbWSXmlLoc);
//               }
//               else if (descriptor.implementedByWebComponent()) {
//                   deploymentEntry1 = jarFile.getEntry(jaxrpcWSXmlLoc);
//               }
//               else {
//                    throw new Exception("Niether implemented by EJB nor by WEB Component");
//               }
//               deploymentEntry = jarFile.getInputStream(deploymentEntry1);
//            }
            if (deploymentEntry != null) {
              // webservices XML exists 
              // result.pass
              result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
              result.passed(smh.getLocalString (getClass().getName() + ".passed",
                          "The webservices.xml file for [{0}] is located at the correct place.",
                           new Object[] {compName.toString()}));

            }
            else {
             // ws xml is does not exist
             //result.fail
             result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
             result.failed(smh.getLocalString (getClass().getName() + ".failed",
               "The webservices.xml file for [{0}] is not located in WEB-INF/META-INF directory as applicable.",
                new Object[] {compName.toString()}));

//             pass = false;
            }
        }catch (Exception e) {
            //result.fail
            result.addErrorDetails(smh.getLocalString
               ("com.sun.enterprise.tools.verifier.tests.webservices.Error",
                "Error: Unexpected error occurred [ {0} ]",
                new Object[] {e.getMessage()}));
//            pass = false;
        }
        finally {

           try {
           if (deploymentEntry != null)
               deploymentEntry.close();
           }catch (IOException e) {}
        }

        return result;
    }
 }

