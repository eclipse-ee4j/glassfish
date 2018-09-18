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
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deploy.shared.FileArchive;

import java.io.*;
import java.net.URL;

/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids:  JSR109_WS_50; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: The wsdl-file element specifies a location of the WSDL description of the 
 *   service. The location is relative to the root of the module. The WSDL description may be a 
 *   partial WSDL, but must at least include the portType and binding elements.
 */

public class WSDLFileCheck extends WSClientTest implements WSClientCheck {

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (ServiceReferenceDescriptor descriptor) {

        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        InputStream deploymentEntry=null;

        // wsdl file
        if (descriptor.hasWsdlFile()) {
            String wsdlUri = descriptor.getWsdlFileUri();
            URL url = null;
            try {
                url = new URL(wsdlUri);
            } catch(java.net.MalformedURLException e) {
                // don't care, will eventuall fail below
            }
            if (url != null) {
                if ("http".equals(url.getProtocol()) || "https".equals(url.getProtocol())) {
                    return result;
                }
            }
            try {
                String uri = getAbstractArchiveUri(descriptor);
                FileArchive arch = new FileArchive();
                arch.open(uri);
                deploymentEntry = arch.getEntry(wsdlUri);

                if (deploymentEntry == null) {
                    //result.fail,
                    result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                            "For [ {0} ]", new Object[] {compName.toString()}));
                    result.failed(smh.getLocalString (getClass().getName() + ".failed",
                            "WSDL file does not exist in the archive at uri [{0}].",
                            new Object[] {wsdlUri}));
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
            }
            finally {
                try {
                    if (deploymentEntry != null)
                        deploymentEntry.close();
                }catch (IOException e) {}
            }

        }
        else {
            //result.notapplicable since no wsdl specified
            result.addNaDetails(smh.getLocalString
                    ("tests.componentNameConstructor", "For [ {0} ]",
                            new Object[] {compName.toString()}));
            result.notApplicable(smh.getLocalString
                    ( getClass().getName() + ".notapp",
                            "Not applicable since Service Client does not have a WSDL file specified."));

        }

        return result;
    }
 }

