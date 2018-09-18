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
*   @assertion_ids: JSR109_WS_15; JSR109_WS_30;
*   @test_Strategy:
*   @class.testArgs: Additional arguments (if any) to be passed when execing the client
*   @testDescription: The developer is responsible for packaging, either by containment or
*   reference, the WSDL file, Service Endpoint Interface class, Service Implementation Bean
*   class, and their dependent classes, JAX-RPC mapping file along with a Web services
*   deployment descriptor in a J2EE module.
*
*   jaxrpc-mapping-file The file name is a relative path within the module.
*/
public class MappingFileCheck extends WSTest implements WSCheck {

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint descriptor) {

        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String mappingFile = descriptor.getWebService().getMappingFileUri();
        if (getVerifierContext().getSchemaVersion().compareTo("1.1") > 0) {
            if (mappingFile != null) {
                addWarningDetails(result, compName);
                result.warning(smh.getLocalString (getClass().getName() + ".warning",
                        "The mapping file as specified in location [ {0} ] is not required.",
                        new Object[] {mappingFile}));
                return result;
            }
        } else {
            InputStream deploymentEntry=null;
            try {
                String uri = getAbstractArchiveUri(descriptor);
                try {
                    FileArchive arch = new FileArchive();
                    arch.open(uri);
                    deploymentEntry = arch.getEntry(mappingFile);
                }catch (IOException e) { throw e;}
                if (deploymentEntry == null) {
                    //result.fail, mapping file does not exist at that location
                    result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                            "For [ {0} ]", new Object[] {compName.toString()}));
                    result.failed(smh.getLocalString (getClass().getName() + ".failed",
                            "The mapping file does not exist at the specified location [{0}] in the archive.",
                            new Object[] {mappingFile}));

                }
            }catch (Exception e) {
                result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                        "For [ {0} ]", new Object[] {compName.toString()}));
                result.failed(smh.getLocalString (getClass().getName() + ".failed",
                        "The mapping file does not exist at the specified location [{0}] in the archive.",
                        new Object[] {mappingFile}));
            }
            finally {
                try {
                    if (deploymentEntry != null)
                        deploymentEntry.close();
                }catch(IOException e) {}
            }
        }
        if(result.getStatus() != Result.FAILED || result.getStatus() != Result.WARNING) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString (getClass().getName() + ".passed",
                    "mapping file requirements are satisfied"));
        }
        return result;
    }
}
