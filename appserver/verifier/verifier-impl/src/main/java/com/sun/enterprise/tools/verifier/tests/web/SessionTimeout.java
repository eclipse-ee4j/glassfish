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
import java.io.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;


/** 
 * The session-timeout element deinfes the default session timeout interval 
 * for all sessions created in this web application.  The units used must
 * be expressed in whole minutes.
 */
public class SessionTimeout extends WebTest implements WebCheck { 

    
    /** 
     * The session-timeout element deinfes the default session timeout interval 
     * for all sessions created in this web application.  The units used must
     * be expressed in whole minutes.
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	boolean na = false;
	boolean foundIt = false;
	Integer sessionTimeout = descriptor.getSessionConfig().getSessionTimeout();
	// tomcat doesn't throw exception to DOL if you pass "ten" to xml element,
	// it initializes session-timeout to -1, hence this check
	if (sessionTimeout.intValue() == -1 ) {
	    na = true;
	} else if (sessionTimeout.intValue() >= 0 ) {
	    foundIt = true;
	} else {
	    foundIt = false;
	}
   
	// always true until DOL lets something other than integer thru...
	if (na) {
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
			  (getClass().getName() + ".notApplicable",
			   "Not Applicable: Servlet session-timeout [ {0} ] element does not define the default session timeout interval.",
			   new Object[] {sessionTimeout.toString()}));
	} else if (foundIt) {
	    result.addGoodDetails(smh.getLocalString
				  ("tests.componentNameConstructor",
				   "For [ {0} ]",
				   new Object[] {compName.toString()}));	
	    result.passed(smh.getLocalString
			      (getClass().getName() + ".passed",
			   "Servlet session-timeout [ {0} ] element defines the default session timeout interval expressed in whole minutes.",
			   new Object[] {sessionTimeout.toString()}));
	} else {
	    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.failed(smh.getLocalString
			  (getClass().getName() + ".failed",
			   "Error: Servlet session-timeout [ {0} ] element does not define the default session timeout interval expressed in whole minutes.",
			   new Object[] {sessionTimeout.toString()}));
	}
	return result;
    }
}
