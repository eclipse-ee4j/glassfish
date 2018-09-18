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

package com.sun.enterprise.tools.verifier.tests.appclient;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import java.lang.ClassLoader;

/** 
 * AppClient callback handler test.
 * The AppClient provider may provide a JAAS callback handler.
 */
public class AppClientCallbackHandler extends AppClientTest implements AppClientCheck { 

      

    /** 
     * AppClient name test.
     * The AppClient provider must assign a display-name to each AppClient module
     *
     * 
     * @param descriptor the app-client deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(ApplicationClientDescriptor descriptor) {
	Result result = getInitializedResult();
ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
	String callbackHandler = descriptor.getCallbackHandler();

	if(callbackHandler == null) {
		result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));	
		result.passed(smh.getLocalString(getClass().getName() + ".passed", 
						 "AppClient callback handler is not specified"));
	}
	else if (callbackHandler.length() > 0) 
	{
	    try {
	        VerifierTestContext context = getVerifierContext();
		ClassLoader jcl = context.getClassLoader();
            Class c = Class.forName(callbackHandler, false, getVerifierContext().getClassLoader());
	        Object obj = c.newInstance();
	        if( obj instanceof javax.security.auth.callback.CallbackHandler) {
		    result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));	
		    result.passed(smh.getLocalString(getClass().getName() + ".passed1", "AppClient callback handler is : [ {0} ] and is loadable", new Object [] {callbackHandler}));
	        } else {
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString(getClass().getName() + ".failed", "Error: AppClient callback handler is not loadable."));
	        }
	    } catch (Exception e) {
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString(getClass().getName() + ".failed", "Error: AppClient callback handler is not loadable."));
	    }
	} else {
		// it's blank, test should not pass
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString(getClass().getName() + ".failed1", "Error: AppClient callback handler must not be blank."));
	} 
	return result;
    }
}
