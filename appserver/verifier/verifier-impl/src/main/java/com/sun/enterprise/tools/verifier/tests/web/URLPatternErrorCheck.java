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

import com.sun.enterprise.tools.verifier.tests.web.WebTest;
import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import org.glassfish.deployment.common.Descriptor;

/** 
 * The content of the url-pattern element follows the rules specified in 
 * section 10 of the servlet spec.
 * In this test, we only check for failure conditions. 
 * In URLPatternWarningCheck class we check for warnings.
 */
public class URLPatternErrorCheck extends URLPattern { 

    protected void checkUrlPatternAndSetResult(String urlPattern, Descriptor descriptor, Result result, ComponentNameConstructor compName){
     if (urlPattern != null && 
               (urlPattern.startsWith("/") || (urlPattern.startsWith("*.") && urlPattern.length()!=2))){
        result.passed(smh.getLocalString
                              ("tests.componentNameConstructor",
    	                       "For [ {0} ]",
    	                       new Object[] {compName.toString()}));
    	result.addGoodDetails (smh.getLocalString
    		                (getClass().getName() + ".passed",
    		                 "url-pattern [ {0} ] within [ {1} ] follows the rules specified in servlet specification",
    		                 new Object[] {urlPattern, descriptor.getName()}));
     } else {
	oneFailed=true;
    	result.failed(smh.getLocalString
    			       ("tests.componentNameConstructor",
    				"For [ {0} ]",
    				new Object[] {compName.toString()}));
    	result.addErrorDetails (smh.getLocalString
    				     (getClass().getName() + ".failed",
    				      "Error: Content of the url-pattern element [ {0} ] does not follow the rules specified in servlet specification within [ {1} ]",
    				      new Object[] {urlPattern, descriptor.getName()}));
     }
    }
}
