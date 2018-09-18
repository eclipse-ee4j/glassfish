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

import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.tools.verifier.tests.web.*;
import org.glassfish.web.deployment.runtime.*;

//<addition author="irfan@sun.com" [bug/rfe]-id="4711198" >
/* Changed the result messages to reflect consistency between the result messages generated 
 * for the EJB test cases for SunONE specific deployment descriptors*/
//</addition>

public class ASJSPConfig extends WebTest implements WebCheck{

public Result check(WebBundleDescriptor descriptor) {


	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean oneFailed = false;
        boolean notApp = false;
 
        try{
            JspConfig jspConfig = ((SunWebAppImpl)descriptor.getSunDescriptor()).getJspConfig();
            WebProperty[] webProps=null;
            if(jspConfig !=null)
                webProps = jspConfig.getWebProperty();
            if (jspConfig!=null && webProps!=null && webProps.length > 0){
                if(ASWebProperty.checkWebProperties(webProps,result ,descriptor, this )){
                    oneFailed=true;
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                                (getClass().getName() + ".failed",
                                "FAILED [AS-WEB sun-web-app] jsp-config - Atleast one name/value pair is not valid in [ {0} ].",
                                new Object[] {descriptor.getName()}));
                }
            }else{
                notApp=true;
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "NOT APPLICABLE [AS-WEB sun-web-app] jsp-config element not defined in  the web archive [ {0} ].",
				  new Object[] {descriptor.getName()}));
            }
            if (oneFailed){
                result.setStatus(Result.FAILED);
            }else if(notApp){
                result.setStatus(Result.NOT_APPLICABLE);
            }else{
                result.setStatus(Result.PASSED);
                addGoodDetails(result, compName);
                result.passed
		    (smh.getLocalString
                    (getClass().getName() + ".passed",
                    "PASSED [AS-WEB sun-web-app] jsp-config elements are valid within the web archive [ {0} ].",
                    new Object[] {descriptor.getName()} ));
            }
        }catch(Exception ex){
            oneFailed = true;
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                (getClass().getName() + ".failed1",
                    "FAILED [AS-WEB csun-web-app] could not create the jsp-config object"));
        
        }
        return result;
    }

}

