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

import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

/** 
 * The environment entry value type must be one of the following Java types:
 * String, Integer, Boolean, Double, Byte, Short, Long, and Float.
 */
public class AppClientEnvEntryValueType extends AppClientTest implements AppClientCheck { 



    /** 
     * The environment entry value type must be one of the following Java types:
     * String, Integer, Boolean, Double, Byte, Short, Long, and Float.
     *
     * @param descriptor the app-client deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(ApplicationClientDescriptor descriptor) {
	Result result = getInitializedResult();
ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	boolean oneFailed = false;
	if (!descriptor.getEnvironmentProperties().isEmpty()) {
	    // environment entry value type must be one of the following Java types:
	    // String, Integer, Boolean, Double, Byte, Short, Long, and Float.
	    for (Iterator itr2 = descriptor.getEnvironmentProperties().iterator(); 
		 itr2.hasNext();) {
		EnvironmentProperty nextEnvironmentProperty = 
		    (EnvironmentProperty) itr2.next();
                String envType = nextEnvironmentProperty.getType();
		if ((envType.equals("java.lang.String")) ||
		    (envType.equals("java.lang.Integer")) ||
		    (envType.equals("java.lang.Boolean")) ||
		    (envType.equals("java.lang.Double")) ||
		    (envType.equals("java.lang.Byte")) ||
		    (envType.equals("java.lang.Short")) ||
		    (envType.equals("java.lang.Long")) ||
		    (envType.equals("java.lang.Character")) ||
		    (envType.equals("java.lang.Float"))) {
		    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addGoodDetails
			(smh.getLocalString
			 (getClass().getName() + ".passed",
			  "Environment entry value [ {0} ] has valid value type [ {1} ] within application client [ {2} ]",
			  new Object[] {nextEnvironmentProperty.getName(),envType,descriptor.getName()}));
		} else {
		    oneFailed = true;
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addErrorDetails
			(smh.getLocalString
			 (getClass().getName() + ".failed",
			  "Error: Environment entry value [ {0} ] does not have valid value type [ {1} ] within application client [ {2} ]",
			  new Object[] {nextEnvironmentProperty.getName(),envType,descriptor.getName()}));
		} 
	    }
	    if (!oneFailed){
		result.setStatus(Result.PASSED);
	    } else {
		result.setStatus(Result.FAILED);
	    }
	} else {
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no environment entry elements defined within this application client [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}


	return result;

    }

}
