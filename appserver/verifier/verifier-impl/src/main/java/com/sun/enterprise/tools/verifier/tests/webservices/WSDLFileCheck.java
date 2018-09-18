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
 *   @assertion_ids: JSR109_WS_15; JSR109_WS_25;
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: The developer is responsible for packaging, either by containment or 
 *   reference, the WSDL file, Service Endpoint Interface class, Service Implementation Bean 
 *   class, and their dependent classes, JAX-RPC mapping file along with a Web services 
 *   deployment descriptor in a J2EE module.
 *
 *   The wsdl-file element specifies a location of the WSDL description of a set of Web 
 *   services. The location is relative to the root of the module and must be specified 
 *   by the developer.
 */

public class WSDLFileCheck extends WSTest implements WSCheck {

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

        // wsdl file
        String wsdlUri = descriptor.getWebService().getWsdlFileUri(); 

        try {
//             if (f == null) {
              String uri = getAbstractArchiveUri(descriptor);
//              try {
                 FileArchive arch = new FileArchive();
                 arch.open(uri);
                 deploymentEntry = arch.getEntry(wsdlUri);
//              }catch (IOException e) { throw e;}
//             }
//             else {
//
//               jarFile = new JarFile(f);
//               ZipEntry deploymentEntry1 =
//                   jarFile.getEntry(wsdlUri);
//               deploymentEntry = jarFile.getInputStream(deploymentEntry1);
//            }
            if (deploymentEntry == null) {
               //result.fail, 
                result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
                result.failed(smh.getLocalString (getClass().getName() + ".failed",
                "WSDL file does not exist in the archive at uri [{0}].",
                new Object[] {wsdlUri}));

//               pass = false;
            }
            else {
               //result.pass
               result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
               result.passed(smh.getLocalString (getClass().getName() + ".passed", 
                          "WSDL file exists in the archive at uri [{0}].",
                           new Object[] {wsdlUri}));

            }
        }catch (Exception e) {
          // result.fail
          result.addErrorDetails(smh.getLocalString
               ("com.sun.enterprise.tools.verifier.tests.webservices.Error",
                "Error: Unexpected error occurred [ {0} ]",
                new Object[] {e.getMessage()}));
//          pass = false;
        }
        finally {
           try {
           if (deploymentEntry != null)
               deploymentEntry.close();
           }catch(IOException e) {}
        }

        return result;
    }
 }

