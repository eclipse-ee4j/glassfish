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

package com.sun.enterprise.tools.verifier.tests.app;

import com.sun.enterprise.tools.verifier.tests.app.ApplicationTest;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;


/** 
 * Application's name test.
 * The Application provider must assign a display name to each Application
 */
public class AppName extends ApplicationTest implements AppCheck { 


    /** 
     * Application's name test.
     * The Application provider must assign a display name to each Application
     *
     * @param descriptor the Application deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(Application descriptor) {

	Result result = getInitializedResult();

    
	String appName = descriptor.getName();

	if (appName != null && appName.length() > 0 ) {
	    // as long as it's not blank, test should pass
	    result.passed
		(smh.getLocalString
		 (getClass().getName() + ".passed", 
		  "Application display name is : [ {0} ]", 
		  new Object [] {appName}));
	} else {
	    // it's blank, test should not pass
	    
		result.failed
		(smh.getLocalString
		 (getClass().getName() + ".failed", 
		  "Error: Application display name cannot be blank."));
	} 
	return result;
    }
}
