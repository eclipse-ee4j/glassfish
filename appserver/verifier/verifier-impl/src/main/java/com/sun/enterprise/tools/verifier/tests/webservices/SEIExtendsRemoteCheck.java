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

/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids: JSR109_WS_46; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: SEI must extend the java.rmi.Remote interface.
 */

public class SEIExtendsRemoteCheck  extends WSTest implements WSCheck {

    /**
     * @param descriptor the  WebService deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        // get hold of the SEI Class
        String s = descriptor.getServiceEndpointInterface();
        if (s == null) {
            // internal error, should never happen
            result.failed(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.webservices.Error",
                            "Error: Unexpected error occurred [ {0} ]",
                            new Object[] {"SEI Class Name Null"}));
            return result;

        }
        Class sei = null;
        try {
            sei = Class.forName(s, false, getVerifierContext().getClassLoader());
        } catch(ClassNotFoundException e) {
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.webservices.Error",
                            "Error: Unexpected error occurred [ {0} ]",
                            new Object[] {e.toString()}));

            return result;
        }
        if (!(getVerifierContext().getSchemaVersion().compareTo("1.1") > 0)) {
            if(!java.rmi.Remote.class.isAssignableFrom(sei)) {
                result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                        "For [ {0} ]", new Object[] {compName.toString()}));
                result.failed(smh.getLocalString(getClass().getName() + ".failed",
                        "SEI [{0}] does not extend the java.rmi.Remote interface.",
                        new Object[] {s}));
            }
        } else if (java.rmi.Remote.class.isAssignableFrom(sei)) {
            result.addWarningDetails(smh.getLocalString ("tests.componentNameConstructor",
                    "For [ {0} ]", new Object[] {compName.toString()}));
            result.warning(smh.getLocalString(getClass().getName() + ".warning",
                    "SEI [{0}] is not required to extend the java.rmi.Remote interface.",
                    new Object[] {s}));
        }
        if(result.getStatus() != Result.FAILED
                && result.getStatus() != Result.WARNING) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString(getClass().getName() + ".passed",
                    "Service Enpoint is defined properly"));
        }
        return result;
    }
}

