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

package com.sun.enterprise.tools.verifier.tests.web.spec22;

import com.sun.enterprise.tools.verifier.tests.web.WebTest;
import com.sun.enterprise.tools.verifier.tests.web.WebCheck;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;


/** 
 * Jsp file element contains the full path to Jsp file within web application
 * test.
 */
public class JspFileName extends WebTest implements WebCheck { 

    
    /**
     * Jsp file in Servlet 2.2 applications must start with a leading  /
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
    String specVersion = descriptor.getSpecVersion();
    Float specVer = new Float(specVersion);
	if (!descriptor.getJspDescriptors().isEmpty()) {
	    boolean oneFailed = false;
        int count = getNonRuntimeCountNodeSet("/web-app/servlet");
        for(int i=1;i<=count;i++){
            String jspFilename = getXPathValueForNonRuntime("/web-app/servlet["+i+"]/jsp-file");
            if(jspFilename!=null){
                if (jspFilename.startsWith("/")){
                    if(specVer.compareTo(new Float("2.3"))<0){
                        result.addGoodDetails(smh.getLocalString
                                ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
                        result.addGoodDetails(smh.getLocalString(getClass().getName() + ".passed",
                                " PASSED : Jsp Name [ {0} ] is valid",
                                new Object[] { jspFilename }));

                    }else{
                        result.addGoodDetails(smh.getLocalString("tests.componentNameConstructor",
                                "For [ {0} ]",
                                new Object[] {compName.toString()}));
                        result.addGoodDetails(smh.getLocalString(getClass().getName() + ".passed1",
                                " Jsp Name [ {0} ] is valid and starts with a leading '/'",
                                new Object[] { jspFilename }));

                    }
                }else{
                    if(specVer.compareTo(new Float("2.3"))<0){
                        result.addGoodDetails(smh.getLocalString("tests.componentNameConstructor",
                                "For [ {0} ]",
                                new Object[] {compName.toString()}));
                        result.addGoodDetails(smh.getLocalString(getClass().getName() + ".passed",
                                " PASSED Jsp Name [ {0} ] is valid",
                                new Object[] { jspFilename }));

                    }else{
                        result.addErrorDetails(smh.getLocalString
                                ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
                        result.addErrorDetails(smh.getLocalString(getClass().getName() + ".failed",
                                " Error : Jsp Name [ {0} ] in invalid as it does not start with a leading '/'",
                                new Object[] { jspFilename }));
                        oneFailed=true;
                    }
                }

            }
        }
        if (oneFailed) {
            result.setStatus(Result.FAILED);
        } else {
            result.setStatus(Result.PASSED);
        }
    } else {
        result.addNaDetails(smh.getLocalString
                ("tests.componentNameConstructor",
                        "For [ {0} ]",
                        new Object[] {compName.toString()}));
        result.notApplicable(smh.getLocalString
                (getClass().getName() + ".notApplicable",
                        "There are no Jsp components within the web archive [ {0} ]",
                        new Object[] {descriptor.getName()}));
    }
        return result;

    }
}
