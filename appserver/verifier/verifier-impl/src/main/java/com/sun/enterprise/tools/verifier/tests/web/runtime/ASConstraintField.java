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
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.deployment.WebBundleDescriptor;

/** constraint-field *
 *    Attribute: name, scope, cache-on-match, cache-on-match-failure
 */

public class ASConstraintField extends WebTest implements WebCheck {

    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String value = null;
	int count = 0;
        boolean oneFailed = false;
        try{
            count = getCountNodeSet("sun-web-app/cache/cache-mapping/constraint-field");
            if (count>0){
                for(int i=1;i<=count;i++){
                    //name attribute
                    value = getXPathValue("sun-web-app/cache/cache-mapping/constraint-field["+i+"]/@name");
                    if (value==null || value.length()==0){
                        oneFailed = true;
                        result.addErrorDetails(smh.getLocalString
                            ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName.toString()}));
                        result.failed(smh.getLocalString
                            (getClass().getName() + ".failed",
                            "FAILED [AS-WEB constraint-field] :  name attribute is required",
                            new Object[] {descriptor.getName()}));
                    }else{
                        result.addGoodDetails(smh.getLocalString
                            ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName.toString()}));
                        result.passed(smh.getLocalString( getClass().getName() + ".passed",
                              "PASSED [AS-WEB constraint-field] : name attribute is {1}",
                              new Object[] {descriptor.getName(),value}));
                    }

                    //scope attribute
                    value = getXPathValue("sun-web-app/cache/cache-mapping/constraint-field["+i+"]/@scope");
                    if (value==null || value.length()==0){
                        result.addNaDetails(smh.getLocalString
	                    ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName.toString()}));
                        result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                            "NOT APPLICABLE [AS-WEB constraint-field] : scope attribute not defined"));
                    }else{
                        String scopeValue[] = {"context.attribute", "request.header", "request.parameter", "request.cookie", "request.attribute", "session.attribute" };
                        boolean found = false;
                        for (int j=0;j<scopeValue.length;j++){
                            if (scopeValue[j].compareTo(value) ==0){
                                found = true;
                            }
                        }
                        if (found){
                            result.addGoodDetails(smh.getLocalString
                                ("tests.componentNameConstructor",
                                "For [ {0} ]", new Object[] {compName.toString()}));
                            result.passed(smh.getLocalString( getClass().getName() + ".passed1",
                                "PASSED [AS-WEB constraint-field] : scope attribute is {1}",
                                new Object[] {descriptor.getName(),value}));
                        }else{
                            oneFailed = true;
                            result.addErrorDetails(smh.getLocalString
                                ("tests.componentNameConstructor",
                                "For [ {0} ]",
                                new Object[] {compName.toString()}));
                            result.failed(smh.getLocalString
                                (getClass().getName() + ".failed1",
                                "FAILED [AS-WEB constraint-field] :  scope attribute must be one of context.attribute, request.header, request.parameter, request.cookie, request.attribute, session.attribute",
                                new Object[] {descriptor.getName()}));
                        }
                    }
                    //cache-on-match % boolean "(yes | no | on | off | 1 | 0 | true | false)">
                    value = getXPathValue("sun-web-app/cache/cache-mapping/constraint-field["+i+"]/@cache-on-match");
                    if (value==null || value.length()==0){
                        result.addNaDetails(smh.getLocalString
	                    ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName.toString()}));
                        result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable1",
                            "NOT APPLICABLE [AS-WEB constraint-field] : cache-on-match attribute not defined"));
                    }else{
                        String cacheOnMatchValue[] = {"yes", "no", "on", "off", "1", "0", "true", "false" };
                        boolean foundCacheOnMatch = false;
                        for (int j=0;j<cacheOnMatchValue.length;j++){
                            if (cacheOnMatchValue[j].compareTo(value) ==0){
                                foundCacheOnMatch = true;
                            }
                        }
                        if (foundCacheOnMatch){
                            result.addGoodDetails(smh.getLocalString
                                ("tests.componentNameConstructor",
                                "For [ {0} ]", new Object[] {compName.toString()}));
                            result.passed(smh.getLocalString( getClass().getName() + ".passed2",
                                "PASSED [AS-WEB constraint-field] : cache-on-match attribute is {1}",
                                new Object[] {descriptor.getName(),value}));
                        }else{
                            oneFailed = true;
                            result.addErrorDetails(smh.getLocalString
                                ("tests.componentNameConstructor",
                                "For [ {0} ]",
                                new Object[] {compName.toString()}));
                            result.failed(smh.getLocalString
                                (getClass().getName() + ".failed2",
                                "FAILED [AS-WEB constraint-field] :  cache-on-match attribute must be one of yes, no, on, off, 1, 0, true, false",
                                new Object[] {descriptor.getName()}));
                        }
                    }

                    //cache-on-match-failure
                    value = getXPathValue("sun-web-app/cache/cache-mapping/constraint-field["+i+"]/@cache-on-match-failure");
                    if (value==null || value.length()==0){
                        result.addNaDetails(smh.getLocalString
	                    ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName.toString()}));
                        result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable2",
                            "NOT APPLICABLE [AS-WEB constraint-field] : cache-on-match-failure attribute not defined"));
                    }else{
                        String cacheOnMatchFailureValue[] = {"yes", "no", "on", "off", "1", "0", "true", "false" };
                        boolean found = false;
                        for (int j=0;j<cacheOnMatchFailureValue.length;j++){
                            if (cacheOnMatchFailureValue[j].compareTo(value) ==0){
                                found = true;
                            }
                        }
                        if (found){
                            result.addGoodDetails(smh.getLocalString
                                ("tests.componentNameConstructor",
                                "For [ {0} ]", new Object[] {compName.toString()}));
                            result.passed(smh.getLocalString( getClass().getName() + ".passed3",
                                "PASSED [AS-WEB constraint-field] : cache-on-match-failure attribute is {1}",
                                new Object[] {descriptor.getName(),value}));
                        }else{
                            oneFailed = true;
                            result.addErrorDetails(smh.getLocalString
                                ("tests.componentNameConstructor",
                                "For [ {0} ]",
                                new Object[] {compName.toString()}));
                            result.failed(smh.getLocalString
                                (getClass().getName() + ".failed3",
                                "FAILED [AS-WEB constraint-field] :  cache-on-match-failure attribute must be one of yes, no, on, off, 1, 0, true, false",
                                new Object[] {descriptor.getName()}));
                        }
                    }
                }
            }else{
                result.addNaDetails(smh.getLocalString
		    ("tests.componentNameConstructor",
		    "For [ {0} ]",
		    new Object[] {compName.toString()}));
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable3",
                    "NOT APPLICABLE [AS-WEB sun-web-app] : constraint-field Element not defined"));
            }
            if(oneFailed)
                result.setStatus(Result.FAILED);
        }catch(Exception ex){
            result.failed(smh.getLocalString
                (getClass().getName() + ".failed4",
                    "FAILED [AS-WEB sun-web-app] could not create the constraint-field object"));
        }
	return result;
    }

}
