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

public class ASWebProperty extends WebTest implements WebCheck{


public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();

	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        boolean oneFailed = false;
        boolean notApp = false;
        
        try{
            WebProperty[] webProps = ((SunWebAppImpl)descriptor.getSunDescriptor()).getWebProperty();
            if (webProps.length > 0){
                oneFailed=checkWebProperties(webProps,result ,descriptor, this ) ;
            }else{
            notApp = true;
            addNaDetails(result, compName);
	    result.notApplicable(smh.getLocalString
                                 (getClass().getName() + ".notApplicable",
                                  "NOT APPLICABLE [AS-WEB sun-web-app] web property element not defined within the web archive [ {0} ].",
                                  new Object[] {descriptor.getName()}));
        }
        if (oneFailed) {
            result.setStatus(Result.FAILED);
        } else if(notApp) {
            result.setStatus(Result.NOT_APPLICABLE);
        }else {
            result.setStatus(Result.PASSED);
        }

        }catch(Exception ex){
            oneFailed = true;
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                (getClass().getName() + ".failed",
                    "FAILED [AS-WEB sun-web-app] could not create the web-property object"));
        }

	return result;
    }
   public static boolean checkWebProperties(WebProperty[] webProps, Result result ,WebBundleDescriptor descriptor, Object obj ) {
       String compName = result.getComponentName();
       String name;
       String value;
       boolean oneFailed = false;
       String[] names=null;
       if (webProps.length > 0){
           names=new String[webProps.length];
           for (int rep=0; rep<webProps.length; rep++ ){
               name = webProps[rep].getAttributeValue(WebProperty.NAME); //*************needs verification from ko]umar Sg
               value = webProps[rep].getAttributeValue(WebProperty.VALUE);
               names[rep]=name;
               if (name !=null && value !=null && name.length() != 0 && value.length() != 0){
                   //check if the name already exist in this web-prop
                   boolean isDuplicate=false;
                   for(int rep1=0;rep1<rep;rep1++)
                   {
                       if(name.equals(names[rep1])){
                           isDuplicate=true;
                            break;
                       }

                    }

                    if(!isDuplicate){
                        result.addGoodDetails(smh.getLocalString
                                                ("tests.componentNameConstructor",
                                                 "For [ {0} ]",
                                                 new Object[] {compName}));
                        result.passed(smh.getLocalString
					  (obj.getClass().getName() + ".passed",
					   "PASSED [AS-WEB property] Proper web property with name  [ {0} ] and value [ {1} ] defined.",
					   new Object[] {name, value}));
                    }else{
                    if (!oneFailed)
                        oneFailed = true;
                    result.addErrorDetails(smh.getLocalString
                                            ("tests.componentNameConstructor",
                                             "For [ {0} ]",
                                             new Object[] {compName}));
                    result.failed(smh.getLocalString
                                      (obj.getClass().getName() + ".failed2",
                                      "FAILED [AS-WEB property] name [ {0} ] and value [ {1} ], the name must be unique in the entire list of web property.",
                                      new Object[] {name, value}));
                    }

               }else{
                   if (!oneFailed)
                        oneFailed = true;
                   result.addErrorDetails(smh.getLocalString
                                            ("tests.componentNameConstructor",
                                             "For [ {0} ]",
                                             new Object[] {compName}));
                   result.failed(smh.getLocalString
                                      (obj.getClass().getName() + ".failed1",
                                      "FAILED [AS-WEB property] name [ {0} ] and value [ {1} ], attributes must be of finite length.",
                                      new Object[] {name, value}));
               }
            }

        }

        return oneFailed;

   }

}

