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

//import com.sun.enterprise.tools.common.dd.webapp.*;

//<addition author="irfan@sun.com" [bug/rfe]-id="4711198" >
/* Changed the result messages to reflect consistency between the result messages generated 
 * for the EJB test cases for SunONE specific deployment descriptors*/
//</addition>


public class ASCache extends WebTest implements WebCheck{

    public Result check(WebBundleDescriptor descriptor) {
        
	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean oneFailed = false;
        boolean notApp = false;
        String maxEntries=null;
        String timeout=null;
        int intMaxEntries;
        long longTimeout;
        String enabled=null;
        
        try{
        Cache cache = ((SunWebAppImpl)descriptor.getSunDescriptor()).getCache();
    
        if (cache != null)
        {
            maxEntries = cache.getAttributeValue(Cache.MAX_ENTRIES);
            timeout = cache.getAttributeValue(Cache.TIMEOUT_IN_SECONDS);
            enabled = cache.getAttributeValue(Cache.ENABLED);
            if (!(enabled.equalsIgnoreCase("true")) && !(enabled.equalsIgnoreCase("false")))
            {

            addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                    (getClass().getName() + ".failed3",
                    "FAILED [AS-WEB cache] enabled  [ {0} ], attribute must be a proper boolean  value. ",
                    new Object[] {enabled}));
                    oneFailed = true;
            }
            else
            {
            addGoodDetails(result, compName);
                result.passed(smh.getLocalString
                    (getClass().getName() + ".passed3",
                        "PASSED [AS-WEB cache] enabled  [ {0} ] defined properly.",
                         new Object[] {enabled}));
            }     
              

            boolean validMaxEntriesValue=true;
            boolean validTimeOutValue=true;
            try
            {
                if(maxEntries !=null && maxEntries.length() != 0)     // check maxEntries.length() != 0 because is IMPLIED att
                {
                    intMaxEntries=Integer.parseInt(maxEntries);
                    if(intMaxEntries >0 && intMaxEntries < Integer.MAX_VALUE)
                        validMaxEntriesValue=true;
                    else
                        validMaxEntriesValue=false;
                }
            }
            catch(NumberFormatException exception)
            {
                validMaxEntriesValue=false;
                if (!oneFailed)
                    oneFailed = true;
            }
            if(validMaxEntriesValue)
            {
                addGoodDetails(result, compName);
                result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                    "PASSED [AS-WEB cache] max-entries [ {0} ] defined properly.",
                    new Object[] {maxEntries}));
            }else
            {
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                    (getClass().getName() + ".failed",
                    "FAILED [AS-WEB cache] max-entries [ {0} ], attribute must be a proper integer value. "+
                        "Its range should be 1 to MAX_INT.",
                        new Object[] {maxEntries}));
                     oneFailed = true;
            }
            try
            {
                if(timeout  != null && timeout.length() != 0 ) // check timeout.length() != 0 because is IMPLIED att
                {
                    longTimeout=Long.parseLong(timeout);
                    if(longTimeout >= -1 && longTimeout <= Long.MAX_VALUE)
                        validTimeOutValue=true;
                    else
                        validTimeOutValue=false;
                }                 

            }catch(NumberFormatException exception) 
            {
                validTimeOutValue=false;
                    oneFailed = true;
            }
            if(validTimeOutValue)
            {
                addGoodDetails(result, compName);
                result.passed(smh.getLocalString
                    (getClass().getName() + ".passed1",
                        "PASSED [AS-WEB cache] timeout-in-seconds  [ {0} ] defined properly.",
                         new Object[] {timeout}));
            }
            else
            {
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                    (getClass().getName() + ".failed1",
                    "FAILED [AS-WEB cache] timeout-in-seconds value [ {0} ], attribute must be a proper long value. " +
                    "Its range should be between -1 and MAX_LONG.",
                    new Object[] {timeout}));
                    oneFailed = true;
            }
            
        }else
        {
            notApp = true;
            addNaDetails(result, compName);
	    result.notApplicable(smh.getLocalString
                (getClass().getName() + ".notApplicable",
                    "NOT APPLICABLE [AS-WEB cache] Element not defined for the web application.",
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
            oneFailed=true;
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failed2",
                    "FAILED [AS-WEB cache] Could not create the cache object"));
        }
	return result;
    }
}
