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


public class ASResourceRefName extends WebTest implements WebCheck {

    public Result check(WebBundleDescriptor descriptor) {
        String resrefName;
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
//Start Bugid:4703107
        ResourcePrincipal resPrincipal;
//End Bugid:4703107
        boolean oneFailed = false;
        boolean notApp = false;
        try{
            Set<ResourceReferenceDescriptor> resRefs = descriptor.getResourceReferenceDescriptors();
            if (resRefs != null && resRefs.size() > 0) {
                for (ResourceReferenceDescriptor resRef : resRefs) {
                    resrefName = resRef.getName();
                    if (validResRefName(resrefName,descriptor)) {
                        addGoodDetails(result, compName);
                        result.passed(smh.getLocalString
                                (getClass().getName() + ".passed",
                                        "PASSED [AS-WEB sun-web-app] resource-ref name [ {0} ] properly defined in the war file.",
                                        new Object[] {resrefName}));

                    }
                    else {
                        oneFailed = true;
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString
                                (getClass().getName() + ".failed",
                                        "FAILED [AS-WEB sun-web-app] resource-ref name [ {0} ] is not valid, either empty or not defined in web.xml.",
                                        new Object[] {resrefName}));
                    }
                    //Start Bugid:4703107
                    resPrincipal = resRef.getResourcePrincipal();
                    if(resPrincipal != null){
                        boolean defResourcePrincipalValid = true;
                        String defaultname = resPrincipal.getName();
                        String defaultpassword = resPrincipal.getPassword();
                        if((defaultname == null)||(defaultname.length() == 0)){
                            oneFailed=true;
                            defResourcePrincipalValid = false;
                            addErrorDetails(result, compName);
                            result.failed(smh.getLocalString
                                    (getClass().getName() + ".failed2",
                                            "FAILED [AS-WEB resource-ref] name field in DefaultResourcePrincipal of ResourceRef [ {0} ] is not specified or is an empty string.",
                                            new Object[] {resrefName}));
                        }
                        if((defaultpassword == null)||(defaultpassword.length() == 0)){
                            oneFailed=true;
                            defResourcePrincipalValid = false;
                            addErrorDetails(result, compName);
                            result.failed(smh.getLocalString
                                    (getClass().getName() + ".failed3",
                                            "FAILED [AS-WEB resource-ref] password field in DefaultResourcePrincipal of ResourceRef [ {0} ] is not specified or is an empty string.",
                                            new Object[] {resrefName}));
                        }
                        if(defResourcePrincipalValid){
                            addGoodDetails(result, compName);
                            result.passed(smh.getLocalString
                                    (getClass().getName() + ".passed3",
                                            "PASSED [AS-WEB resource-ref]  DefaultResourcePrincipal of ResourceRef [ {0} ] properly defined",
                                            new Object[] {resrefName}));
                        }
                    }
                    //End Bugid:4703107
                }
            } else {
                notApp = true;
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString
                        (getClass().getName() + ".notApplicable",
                                "NOT APPLICABLE [AS-WEB sun-web-app] resource-ref element not defined in the web archive [ {0} ].",
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
                                "PASSED [AS-WEB sun-web-app] resource-ref element(s) are valid within the web archive [ {0} ] .",
                                new Object[] {descriptor.getName()} ));
            }
        }catch(Exception ex){
            oneFailed=true;
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failed4", "FAILED [AS-WEB resource-env-ref] Could not create the resource-ref"));

        }
        return result;
    }

    boolean validResRefName(String name,WebBundleDescriptor descriptor){
        boolean valid =true;
        if(name !=null && name.length()!=0) {
            try{
                descriptor.getResourceReferenceByName(name);
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
