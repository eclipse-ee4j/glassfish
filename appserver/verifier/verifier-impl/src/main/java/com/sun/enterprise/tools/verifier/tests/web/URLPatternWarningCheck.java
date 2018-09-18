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
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import org.glassfish.deployment.common.Descriptor;

/** 
 * The content of the url-pattern element follows the rules specified in 
 * section 10 of the servlet spec.
 * In this test we check for warnings. See bug#4880426 for more details about the motivation.
 */
public class URLPatternWarningCheck extends URLPattern { 

    protected void checkUrlPatternAndSetResult(String urlPattern, Descriptor descriptor, Result result, ComponentNameConstructor compName){

     if(urlPattern==null) return;//URLPattern1 will take care of this condition. So don't do any thing. More over, the super class would have set NA status, so we don't do that either.

     int count = new StringTokenizer(urlPattern,"*", true).countTokens();
     // See bug#4880426
     if((count ==2 && !urlPattern.endsWith("/*") && !urlPattern.startsWith("*.")) // patterns like /abc*, but not /abc/*, /*, *.jsp or *.
         || (count > 2)) //patterns like *.*, *.j*p, /*.jsp, /**, but not *.jsp
     { 
	oneWarning=true;
        result.warning(smh.getLocalString
                                 ("tests.componentNameConstructor",
                                  "For [ {0} ]",
                                  new Object[] {compName.toString()}));
        result.addWarningDetails (smh.getLocalString
                                   (getClass().getName() + ".warning",
                                    "url-pattern [ {0} ] within [ {1} ] will be used for exact match only, although it contains a *",
                                    new Object[] {urlPattern, descriptor.getName()}));
     } else {
        result.passed(smh.getLocalString
                              ("tests.componentNameConstructor",
    	                       "For [ {0} ]",
    	                       new Object[] {compName.toString()}));
    	result.addGoodDetails (smh.getLocalString
    		                (getClass().getName() + ".passed",
    		                 "url-pattern [ {0} ] within [ {1} ] follows the rules specified in servlet spec",
    		                 new Object[] {urlPattern, descriptor.getName()}));
     } 
    }
}
