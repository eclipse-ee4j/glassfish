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

package com.sun.enterprise.tools.verifier.tests.web;

import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

public abstract class URLPatternUnique extends WebTest implements WebCheck { 
    /** 
     * the url-pattern should be unique. Refer to bug#4903615 
	 * This test serves as a base class for three classes.
	 * It has a pure virtual function which has to be implemented in concrete subclasses.
     * Note: This right now reports WARNING, as it is not clear from spec
	 * if it should report a failure.
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {
        boolean na=true, warning=false;
        Result result = getInitializedResult();
        ComponentNameConstructor compName =
                getVerifierContext().getComponentNameConstructor();
        //this warning will be displayed depending on the final status.
        result.addWarningDetails(smh.getLocalString
                                                   ("tests.componentNameConstructor",
                                                        "For [ {0} ]",
                                                        new Object[] {compName.toString()}));

        //Assumes that DOL gives us the list of url-patterns including duplicates
        Set<String> urlPatterns=new HashSet<String>();
        for(Iterator iter=getUrlPatterns(descriptor).iterator();iter.hasNext();){
            na=false;
            String urlPattern=(String)iter.next();
            if(!urlPatterns.add(urlPattern)){
                    warning=true;
                    result.setStatus(Result.WARNING);
                    result.addWarningDetails(smh.getLocalString
                                                     (getClass().getName() + ".warning",
                                                      "url-pattern [ {0} ] already exists in web archive [ {1} ]",
                                                      new Object[] {urlPattern, descriptor.getName()}));
            }
        }

        if(na){
            result.setStatus(Result.NOT_APPLICABLE);
            result.addNaDetails(smh.getLocalString
                                               ("tests.componentNameConstructor",
                                                    "For [ {0} ]",
                                                    new Object[] {compName.toString()}));	    
            result.addNaDetails(smh.getLocalString
                                                     (getClass().getName() + ".notApplicable",
                                                    "There is no url-pattern element within the web archive [ {0} ]",
                                                    new Object[] {descriptor.getName()}));
        }else if(!warning) {
            result.passed(smh.getLocalString
                                               ("tests.componentNameConstructor",
                                                    "For [ {0} ]",
                                                    new Object[] {compName.toString()}));	    
            result.addGoodDetails(smh.getLocalString
                                                     (getClass().getName() + ".passed",
                                                    "All the url-patterns are unique within the web archive [ {0} ]",
                                                    new Object[] {descriptor.getName()}));
        }
        return result;
    }
    protected abstract Collection getUrlPatterns(WebBundleDescriptor descriptor);
}
