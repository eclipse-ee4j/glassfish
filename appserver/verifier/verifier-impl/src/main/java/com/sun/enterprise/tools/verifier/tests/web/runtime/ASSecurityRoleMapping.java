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


import com.sun.enterprise.tools.verifier.tests.web.WebTest;
import com.sun.enterprise.tools.verifier.tests.web.WebCheck;
import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.runtime.common.*;

//<addition author="irfan@sun.com" [bug/rfe]-id="4711198" >
/* Changed the result messages to reflect consistency between the result messages generated 
 * for the EJB test cases for SunONE specific deployment descriptors*/
//</addition>

public class ASSecurityRoleMapping extends WebTest implements WebCheck {



    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
        String roleName;
        List<PrincipalNameDescriptor> prinNames;
        List<String> grpNames;
	boolean oneFailed = false;
        
        try{
        SecurityRoleMapping[] secRoleMapp = (descriptor.getSunDescriptor()).getSecurityRoleMapping();
	if (secRoleMapp !=null && secRoleMapp.length > 0) {
	    for (int rep=0; rep<secRoleMapp.length; rep++ ) {
                roleName=secRoleMapp[rep].getRoleName();
                prinNames=secRoleMapp[rep].getPrincipalNames();
                grpNames=secRoleMapp[rep].getGroupNames();

                if(validRoleName(roleName,descriptor)){
                    addGoodDetails(result, compName);
                    result.passed(smh.getLocalString
			(getClass().getName() + ".passed",
			"PASSED [AS-WEB security-role-mapping] role-name [ {0} ] properly defined in the war file.",
			new Object[] {roleName}));

                }else{
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
					   (getClass().getName() + ".failed",
					    "FAILED [AS-WEB security-role-mapping] role-name [ {0} ] is not valid, either empty or not defined in web.xml.",
					    new Object[] {roleName}));
                  oneFailed = true;

                }
                if (prinNames !=null && prinNames.size() > 0){
                    String prinName;
                    for (int rep1=0; rep1<prinNames.size(); rep1++ ) {
                      // <addition> srini@sun.com Bug : 4699658
                      prinName = prinNames.get(rep1).getName().trim();
                      // </addition>
                      if(prinName !=null && ! "".equals(prinName)){
                          addGoodDetails(result, compName);
                          result.passed(smh.getLocalString
					  (getClass().getName() + ".passed1",
					   "PASSED [AS-WEB security-role-mapping] principal-name [ {0} ] properly defined in the war file.",
					   new Object[] {prinName}));
                      }else{
                         addErrorDetails(result, compName);
                         result.failed(smh.getLocalString
                         		(getClass().getName() + ".failed1",
					    "FAILED [AS-WEB security-role-mapping] principal-name [ {0} ] cannot be empty string.",
					    new Object[] {prinName}));
                      oneFailed = true;

                      }
                    }
                }
                if (grpNames !=null && grpNames.size() > 0) {
                  String grpName;
                  for (int rep1=0; rep1<grpNames.size(); rep1++ ) {
                      // <addition> srini@sun.com Bug : 4699658
                      grpName =grpNames.get(rep1).trim();
                      // </addition>
                      if(grpName !=null && ! "".equals(grpName)){
                      addGoodDetails(result, compName);
                      result.passed(smh.getLocalString
					  (getClass().getName() + ".passed2",
					   "PASSED [AS-WEB security-role-mapping] group-name [ {0} ] properly defined in the war file.",
					   new Object[] {grpName}));

                      }else{

                      addErrorDetails(result, compName);
                      result.failed(smh.getLocalString
					   (getClass().getName() + ".failed2",
					    "FAILED [AS-WEB security-role-mapping] group-name [ {0} ] cannot be an empty string.",
					    new Object[] {grpName}));
                      oneFailed = true;

                      }
                    }
                }

	      }
         } else {
             addNaDetails(result, compName);
             result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "NOT APPLICABLE [AS-WEB sun-web-app] security-role-mapping element not defined in the web archive [ {0} ].",
				  new Object[] {descriptor.getName()}));
	    return result;
	}

	if (oneFailed){
		result.setStatus(Result.FAILED);
        } else {
                result.setStatus(Result.PASSED);
                addGoodDetails(result, compName);
		result.passed
		    (smh.getLocalString
		     (getClass().getName() + ".passed3",
		      "PASSED [AS-WEB sun-web-app] security-role-mapping element(s) are valid within the web archive [ {0} ].",
                            new Object[] {descriptor.getName()} ));
        }
        }catch(Exception ex){
            oneFailed = true;
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
				(getClass().getName() + ".failed3",
				"FAILED [AS-WEB security-role-mapping] could not create the security-role-mapping object"));
            
        }
	return result;
    }
    boolean validRoleName(String roleName, WebBundleDescriptor descriptor){
          boolean valid=false;
          if (roleName != null && roleName.length() != 0) {
              Enumeration roles = descriptor.getSecurityRoles();
                    // test the sec roles in this .war
                    while (roles!=null && roles.hasMoreElements()) {
                        SecurityRoleDescriptor roleDesc = (SecurityRoleDescriptor) roles.nextElement();
                        String thisRoleName = roleDesc.getName();
			if (roleName.equals(thisRoleName)) {
                            valid = true;
                            break;
                        }
                    }
                    // to-do vkv#
                    //## roles related to application also needs to be checked, although present application
                    //##descriptor dont have seperate sec roles data-structure, so leaving it for time

          }
          return valid;
    }
}

