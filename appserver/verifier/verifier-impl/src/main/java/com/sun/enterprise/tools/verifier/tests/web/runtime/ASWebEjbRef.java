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

package com.sun.enterprise.tools.verifier.tests.web.runtime;

import java.util.Set;

import com.sun.enterprise.tools.verifier.tests.web.WebTest;
import com.sun.enterprise.tools.verifier.tests.web.WebCheck;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.types.EjbReference;

//<addition author="irfan@sun.com" [bug/rfe]-id="4711198" >
/* Changed the result messages to reflect consistency between the result messages generated 
 * for the EJB test cases for SunONE specific deployment descriptors*/
//</addition>

public class ASWebEjbRef extends WebTest implements WebCheck {

    public Result check(WebBundleDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean oneFailed = false;
        String refName;
        Set<EjbReference> ejbRefs = descriptor.getEjbReferenceDescriptors();
        if (ejbRefs!=null && ejbRefs.size() > 0) {
            for (EjbReference ejbRef : ejbRefs) {
                refName = ejbRef.getName();
                if (validEjbRefName(refName,descriptor)) {
                    addGoodDetails(result, compName);
                    result.passed(smh.getLocalString
                            (getClass().getName() + ".passed",
                                    "PASSED [AS-WEB ejb-ref] ejb-ref-name [ {0} ] properly defined in the war file.",
                                    new Object[] {refName}));
                } else {
                    oneFailed = true;
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                            (getClass().getName() + ".failed",
                                    "FAILED [AS-WEB ejb-ref] ejb-ref-name [ {0} ] is not valid, either empty or not defined in web.xml.",
                                    new Object[] {refName}));
                }
            }
        } else {
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                    (getClass().getName() + ".notApplicable",
                            "NOT APPLICABLE [AS-WEB sun-web-app] ejb-ref element(s) not defined in the web archive [ {0} ].",
                            new Object[] {descriptor.getName()}));
            return result;
        }
        if (oneFailed)
        {
            result.setStatus(Result.FAILED);
        } else {
            addGoodDetails(result, compName);
            result.passed
                    (smh.getLocalString
                    (getClass().getName() + ".passed2",
                            "PASSED [AS-WEB sun-web-app] ejb-ref element(s) defined are valid within the web archive [ {0} ].",
                            new Object[] {descriptor.getName()} ));
        }
        return result;
    }

    boolean validEjbRefName(String name,WebBundleDescriptor descriptor){
        boolean valid =true;
        if(name !=null && name.length()!=0) {
            try{
                descriptor.getEjbReferenceByName(name);
            }
            catch(IllegalArgumentException e){
                valid=false;
            }
        }else{
            valid=false;
        }
        return valid;
    }
}
