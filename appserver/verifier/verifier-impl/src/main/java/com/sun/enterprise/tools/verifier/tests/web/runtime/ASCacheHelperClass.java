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

import com.sun.enterprise.deployment.WebBundleDescriptor;

import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.web.WebCheck;
import com.sun.enterprise.tools.verifier.Result;
import org.glassfish.web.deployment.runtime.Cache;
import org.glassfish.web.deployment.runtime.CacheHelper;
import org.glassfish.web.deployment.runtime.SunWebAppImpl;
import org.glassfish.web.deployment.runtime.WebProperty;

//<addition author="irfan@sun.com" [bug/rfe]-id="4711198" >
/* Changed the result messages to reflect consistency between the result messages generated 
 * for the EJB test cases for SunONE specific deployment descriptors*/
//</addition>




public class ASCacheHelperClass extends ASCache implements WebCheck {
    public Result check(WebBundleDescriptor descriptor) {
        Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean oneFailed = false;
        boolean notApp = false;
        boolean oneWarning=false;
        boolean presentHelper=false;

        try{
            Cache cache = ((SunWebAppImpl)descriptor.getSunDescriptor()).getCache();
            CacheHelper[] helperClasses=null;
            CacheHelper helperClass=null;
            WebProperty[] webProps;
            String name=null;
            String classname=null;
            String[] names=null;
            //to-do vkv# check for class-name attribute.
            if (cache != null )
                helperClasses=cache.getCacheHelper();
            if (cache != null && helperClasses !=null && helperClasses.length > 0)
            {
                names=new String[helperClasses.length];             
                for(int rep=0;rep < helperClasses.length;rep++)
                {
                    helperClass=helperClasses[rep]; 
                    if(helperClass==null)
                        continue;
                    int i = rep+1;
                    name = getXPathValue("sun-web-app/cache/cache-helper["+i+"]/@name");
                    classname = getXPathValue("sun-web-app/cache/cache-helper["+i+"]/@class-name");
                    Class hClass=null;
                    names[rep]=name;

                    if (name != null && name.length() != 0) {
                        //check if the name already exist 
                        boolean isDuplicate=false;
                        for(int rep1=0;rep1<rep;rep1++)
                        {
                            if(name.equals(names[rep1]))
                            {
                                isDuplicate=true;
                                break;
                            }

                        }
                        if(isDuplicate)
                        {
                            oneFailed = true;
                            addErrorDetails(result, compName);
                            result.failed(smh.getLocalString
                                      (getClass().getName() + ".failed",
                                      "FAILED [AS-WEB cache-helper] name attribute [ {0} ], must be unique in the entire list of cache-helper.",
                                      new Object[] {name}));
                        }
                        else
                        {
                            if(classname!=null && classname.length()!=0) {
                                hClass = loadClass(result,classname);
                            }
                            if(hClass !=null) 
                                presentHelper=true ;
                            else
                                presentHelper=false ;
                          
                            if(!presentHelper)
                            {
                                addWarningDetails(result, compName);
                                result.warning(smh.getLocalString(
                                                    getClass().getName() + ".error",
                                                    "WARNING [AS-WEB cache-helper] " +
                                                    "name [ {0} ], class not present in the war file.",
                                                    new Object[] {name}));
                                oneWarning = true; 
                            }
                            else
                            {
                                addGoodDetails(result, compName);
                                result.passed(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "PASSED [AS-WEB cache-helper] name  [ {0} ], helper class is valid.",
					   new Object[] {name}));
                            }
                            
                        }
                    } else {
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString
                                      (getClass().getName() + ".failed1",
                                      "FAILED [AS-WEB cache-helper] name [ {0} ], either empty or null.",
                                      new Object[] {name}));
		        oneFailed = true;
                   
                    }
                    webProps=helperClass.getWebProperty();
                    if(ASWebProperty.checkWebProperties(webProps,result ,descriptor, this )){
                        oneFailed=true;
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString
                                (getClass().getName() + ".failed2",
                                "FAILED [AS-WEB cache-helper] Atleast one name/value pair is not valid in helper-class of [ {0} ].",
                                new Object[] {descriptor.getName()}));
                    }
                }//end of for
            }else
            {
                notApp = true;
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString
                (getClass().getName() + ".notApplicable",
                    "NOT APPLICABLE [AS-WEB cache-helper] There is no cache-helper element for the web application",
                        new Object[] {descriptor.getName()}));
            }
            if (oneFailed) {
            result.setStatus(Result.FAILED);
        } else if(oneWarning){
            result.setStatus(Result.WARNING);
        } else if(notApp) {
            result.setStatus(Result.NOT_APPLICABLE);
        }else {
            result.setStatus(Result.PASSED);
        }
	
    }catch(Exception ex){
    oneFailed = true;
    addErrorDetails(result, compName);
    result.failed(smh.getLocalString
                (getClass().getName() + ".failed3",
                    "FAILED [AS-WEB cache-helper] could not create the cache object"));
    }
        return result;
    }
} 
