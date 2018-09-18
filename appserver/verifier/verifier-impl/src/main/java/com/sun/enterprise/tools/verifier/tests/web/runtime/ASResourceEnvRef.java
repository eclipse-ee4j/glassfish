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

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.tools.verifier.tests.web.*;

//<addition author="irfan@sun.com" [bug/rfe]-id="4711198" >
/* Changed the result messages to reflect consistency between the result messages generated 
 * for the EJB test cases for SunONE specific deployment descriptors*/
//</addition>

public class ASResourceEnvRef extends WebTest implements WebCheck {

    public Result check(WebBundleDescriptor descriptor) {

        String resName;
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean oneFailed = false;
        boolean notApp = false;
        try{
            Set<ResourceEnvReferenceDescriptor> envRefs= descriptor.getResourceEnvReferenceDescriptors();
        
            if (envRefs != null && envRefs.size() > 0) {

                for (ResourceEnvReferenceDescriptor envRef : envRefs) {
		        resName = envRef.getName();

                if (validResEnvRefName(resName,descriptor)) {
                    addGoodDetails(result, compName);
                    result.passed(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "PASSED [AS-WEB sun-web-app] resource-env-ref [ {0} ] properly defined in the war file.",
					   new Object[] {resName}));
                } else {
                    oneFailed = true;
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                                        (getClass().getName() + ".failed",
                                        "FAILED [AS-WEB sun-web-app] resource-env-ref name [ {0} ] is not valid, either empty or not defined in web.xml.",
                                        new Object[] {resName}));
                }

            }

        } else {
            notApp = true;
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "NOT APPLICABLE [AS-WEB sun-web-app] resource-env-ref element not defined in the web archive [ {0} ].",
				  new Object[] {descriptor.getName()}));
        }
        if (oneFailed) {
            result.setStatus(Result.FAILED);
        } else if(notApp) {
            result.setStatus(Result.NOT_APPLICABLE);
        }else {
            result.setStatus(Result.PASSED);
            addGoodDetails(result, compName);
            result.passed
		    (smh.getLocalString
		     (getClass().getName() + ".passed2",
		      "PASSED [AS-WEB sun-web-app] resource-env-ref element(s) are valid within the web archive [ {0} ].",
                      new Object[] {descriptor.getName()} ));
        }
        }catch(Exception ex){
            oneFailed=true;
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
               (getClass().getName() + ".failed2", "FAILED [AS-WEB resource-env-ref] Could not create the resource-env-ref"));
            
            
        }
	
            return result;
        }
    

     boolean validResEnvRefName(String name,WebBundleDescriptor descriptor){
        boolean valid =true;
        if(name !=null && name.length()!=0) {
            try{
              descriptor.getResourceEnvReferenceByName(name);
            }
            catch(IllegalArgumentException e){
            valid=false;
            }
        }  else{
         valid=false;

        }
        return valid;
    }

}
