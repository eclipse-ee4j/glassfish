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

/** 
 * AppClient name test.
 * The AppClient provider must assign a display-name to each AppClient module
 */
public class AppClientName extends AppClientTest implements AppClientCheck { 

      

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
        // String clientName = descriptor.getName();
        // get client xml display-name..
        // fix for bug # 4942905 - api changed in the DOL
        String clientName = descriptor.getDisplayName();


	if (clientName !=null && clientName.length() > 0) 
	    {
		// as long as it's not blank, test should pass
		result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));	
		result.passed(smh.getLocalString(getClass().getName() + ".passed", "AppClient display name is : [ {0} ]", new Object [] {clientName}));
	    } else {
		// it's blank, test should not pass
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString(getClass().getName() + ".failed", "Error: AppClient display name must not be blank."));
	    }
	return result;
    }
}
