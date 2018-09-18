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
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids:  JSR109_WS_48; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: If the wsdl-file is not specified in the deployment descriptor, the 
 *   jaxrpc-mapping-file must not be specified.
 */

public class MappingFileOnlyIfWSDLFileCheck extends WSClientTest implements WSClientCheck {

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (ServiceReferenceDescriptor descriptor) {

	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        if (!descriptor.hasWsdlFile()) {
            if (descriptor.hasMappingFile()) {
               // result.fail, mapping file specified without WSDL
                result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                     "For [ {0} ]", new Object[] {compName.toString()}));
                result.failed(smh.getLocalString (getClass().getName() + ".failed",
                "Mapping file specified for this webservice [{0}] without a corresponding WSDL file.",
                      new Object[] {compName.toString()}));
            }
            else {
              // result.pass
              result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
              result.passed(smh.getLocalString (getClass().getName() + ".passed",
             "No Mapping file specified since there is no WSDL file."));

            }
        }
        else {
         // not applicable
         // NOTE: the other way around things are being checked in another test
         result.addNaDetails(smh.getLocalString
                     ("tests.componentNameConstructor", "For [ {0} ]",
                      new Object[] {compName.toString()}));
         result.notApplicable(smh.getLocalString
                 ( getClass().getName() + ".notapp",
                 "Not Applicable since there is a WSDL file specified."));
 
        }

        return result;
    }
 }

