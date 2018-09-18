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

package com.sun.enterprise.tools.verifier.tests.web;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.XpathPrefixResolver;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;


/**
 *
 *
 */
public class SessionConfigTest extends WebTest implements WebCheck {


    /**
     * The session-config element defines the session parameters for this web application
     * The deployment descriptor instance file must not contain multiple elements of session-config.
     *
     * @param descriptor the Web deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */

    public Result check(WebBundleDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        //This test is not applicable for application based on Servlet Spec 2.3
        String prefix = XpathPrefixResolver.fakeXPrefix;
        String query = prefix + ":" + "web-app/" + prefix + ":" + "session-config";
        int count = getNonRuntimeCountNodeSet(query);
        
        if ( count == 0 || count == -1) {
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                    (getClass().getName() + ".notApplicable",
                    "Not Applicable: Servlet session-config element is not Specified."));
        } else if ( count  == 1 ) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed" ,
                    "The session-config element is specified correctly"));
        } else if ( count > 1 ) {
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failed",
                    "The deployment descriptor instance contains multiple elements of session-config element"));
        }
        return result;
    }
}
