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

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.TagLibDescriptor;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.deployment.WebBundleDescriptor;

/**
 *
 */
public class TagLibPublicID extends WebTest implements WebCheck {

    public Result check(WebBundleDescriptor descriptor) {

        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        String acceptablePubidLiterals[] = {
            "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN" ,
            "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN" };

        String acceptableURLs[] = {"http://java.sun.com/j2ee/dtds/web-jsptaglibrary_1_1.dtd",
                                   "http://java.sun.com/dtd/web-jsptaglibrary_1_2.dtd"};
        VerifierTestContext context = getVerifierContext();
        TagLibDescriptor tlds[] = context.getTagLibDescriptors();

        addGoodDetails(result, compName);
        result.passed(smh.getLocalString
                (getClass().getName() + ".passed",
                        "Test passed successfully"));

        if (tlds != null && tlds.length !=0) {
            boolean oneFailed = false;
            // iterate over all the tag lib descriptors present in war file
            for (int i=0;i<tlds.length;i++) {
                String publicID = tlds[i].getPublicID();
                String systemID = tlds[i].getSystemID();
                if (publicID==null) continue;
                boolean match = false;
                for (int k=0;k<acceptablePubidLiterals.length;k++) {
                    if (publicID.compareTo(acceptablePubidLiterals[k])==0 && systemID.compareTo(acceptableURLs[k])==0) {
                        match=true;
                        addGoodDetails(result, compName);
                        result.passed
                                (smh.getLocalString
                                (getClass().getName() + ".passed1",
                                        "The deployment descriptor [ {0} ] has the proper PubidLiteral: [ {1} ] and sytemID: [ {2} ]",
                                        new Object[] {tlds[i].getUri(), acceptablePubidLiterals[k], acceptableURLs[k]}));
                        break;
                    }
                }

                if (!match) {
                    oneFailed=true;
                    addErrorDetails(result, compName);
                    result.addErrorDetails
                            (smh.getLocalString
                            (getClass().getName() + ".failed",
                                    "The deployment descriptor for [ {0} ] does not have an expected PubidLiteral or SystemID",
                                    new Object[] {tlds[i].getUri()}));

                }
            }
            if(oneFailed)
                result.setStatus(Result.FAILED);
            return result;

        }
        return result;
    }
}
