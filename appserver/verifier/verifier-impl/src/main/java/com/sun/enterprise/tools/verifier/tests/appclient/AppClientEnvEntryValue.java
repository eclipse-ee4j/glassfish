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
 * If the Bean Provider provides a value for an environment entry using the 
 * env-entry-value element, the value can be changed later by the Application 
 * Assembler or Deployer. The value must be a string that is valid for the 
 * constructor of the specified type that takes a single String parameter.
 */
public class AppClientEnvEntryValue extends AppClientTest implements AppClientCheck { 


    /** 
     * If the Bean Provider provides a value for an environment entry using the 
     * env-entry-value element, the value can be changed later by the Application 
     * Assembler or Deployer. The value must be a string that is valid for the 
     * constructor of the specified type that takes a single String parameter.
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
            int oneEnvValue = 0;
            int oneNA = 0;
	    // The value must be a string that is valid for the
	    // constructor of the specified type that takes a single String parameter
	    for (Iterator itr2 = descriptor.getEnvironmentProperties().iterator(); 
		 itr2.hasNext();) {
                oneEnvValue++;
		EnvironmentProperty nextEnvironmentProperty = 
		    (EnvironmentProperty) itr2.next();
                if ((nextEnvironmentProperty.getValue() != null) && (nextEnvironmentProperty.getValue().length() > 0)) {
		    if (nextEnvironmentProperty.getType().equals("java.lang.String"))  {
			// don't need to do anything in this case, since any string results
			// in a valid object creation
			try {
			    new String(nextEnvironmentProperty.getValue());
			} catch (Exception e) {
			    if (debug) {
				e.printStackTrace();
			    }
			    oneFailed = true;
			}
		    } else if (nextEnvironmentProperty.getType().equals("java.lang.Character"))  {
			try {
			    if (nextEnvironmentProperty.getValue().length() == 1) {
				char c = (nextEnvironmentProperty.getValue()).charAt(0);
				new Character(c);
			    }
			    else oneFailed = true;
			} catch (Exception e) {
			    if (debug) {
				e.printStackTrace();
			    }
			    oneFailed = true;
			}
		    } else if (nextEnvironmentProperty.getType().equals("java.lang.Integer")) {
			try {
			    new Integer(nextEnvironmentProperty.getValue());
			} catch (NumberFormatException e) {
			    if (debug) {
				e.printStackTrace();
			    }
			    oneFailed = true;
			}
		    } else if  (nextEnvironmentProperty.getType().equals("java.lang.Boolean")) {
			// don't need to do anything in this case, since any string results
			// in a valid object creation
			try {
			    new Boolean(nextEnvironmentProperty.getValue());
			} catch (Exception e) {
			    if (debug) {
				e.printStackTrace();
			    }
			    oneFailed = true;
			}
		    } else if  (nextEnvironmentProperty.getType().equals("java.lang.Double")) {
			try {
			    new Double(nextEnvironmentProperty.getValue());
			} catch (NumberFormatException e) {
			    if (debug) {
				e.printStackTrace();
			    }
			    oneFailed = true;
			}
		    } else if  (nextEnvironmentProperty.getType().equals("java.lang.Byte")) {
			try {
			    new Byte(nextEnvironmentProperty.getValue());
			} catch (NumberFormatException e) {
			    if (debug) {
				e.printStackTrace();
			    }
			    oneFailed = true;
			}
		    } else if  (nextEnvironmentProperty.getType().equals("java.lang.Short")) {
			try {
			    new Short(nextEnvironmentProperty.getValue());
			} catch (NumberFormatException e) {
			    if (debug) {
				e.printStackTrace();
			    }
			    oneFailed = true;
			}
		    } else if  (nextEnvironmentProperty.getType().equals("java.lang.Long")) {
			try {
			    new Long(nextEnvironmentProperty.getValue());
			} catch (NumberFormatException e) {
			    if (debug) {
				e.printStackTrace();
			    }
			    oneFailed = true;
			}
		    } else if  (nextEnvironmentProperty.getType().equals("java.lang.Float")) {
			try {
			    new Float(nextEnvironmentProperty.getValue());
			} catch (NumberFormatException e) {
			    if (debug) {
				e.printStackTrace();
			    }
			    oneFailed = true;
			}
		    } else {
			oneFailed = true;
		    }
		    if (oneFailed) {
			result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
			result.addErrorDetails
			    (smh.getLocalString
			     (getClass().getName() + ".failed",
			      "Error: Environment entry value [ {0} ] does not have valid value [ {1} ] for constructor of the specified type [ {2} ] that takes a single String parameter within application client [ {3} ]",
			      new Object[] {nextEnvironmentProperty.getName(),nextEnvironmentProperty.getValue(),nextEnvironmentProperty.getType(),descriptor.getName()}));
		    } else {
			result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
			result.addGoodDetails
			    (smh.getLocalString
			     (getClass().getName() + ".passed",
			      "Environment entry value [ {0} ] has valid value [ {1} ] for constructor of the specified type [ {2} ] that takes a single String parameter within application client [ {3} ]",
			      new Object[] {nextEnvironmentProperty.getName(),nextEnvironmentProperty.getValue(),nextEnvironmentProperty.getType(),descriptor.getName()}));
		    } 
                } else {
                    // maybe nextEnvironmentProperty.getValue is null 'cause we
                    // are not using nextEnvironmentProperty.getValue
                    // if that is the case, then test is N/A,
		    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                    result.addNaDetails(smh.getLocalString
                                        (getClass().getName() + ".notApplicable1",
                                         "Environment entry [ {0} ] initial value is not defined within application client [ {1} ]",
                                         new Object[] {nextEnvironmentProperty.getName(), descriptor.getName()}));
                    oneNA++;
                }
	    }
	    if (oneFailed){
		result.setStatus(Result.FAILED);
            } else if (oneNA == oneEnvValue) {
                result.setStatus(Result.NOT_APPLICABLE);
	    } else {
		result.setStatus(Result.PASSED);
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
