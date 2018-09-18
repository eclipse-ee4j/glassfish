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

/*
 * CheckConfigPropertyName.java
 *
 * Created on October 2, 2000, 11:11 AM
 */

package com.sun.enterprise.tools.verifier.tests.connector;

import java.util.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.tools.verifier.Result;

/**
 *
 * @author  Jerome Dochez
 * @version 
 */
public class CheckConfigPropertyName extends ConnectorTest implements ConnectorCheck {

    
    /** <p>
     * Properties names defined in the resource adapter config-propery should
     * be unique per resource adapter
     * </p>
     *
     * @paramm descriptor deployment descriptor for the rar file
     * @return result object containing the result of the individual test
     * performed
     */
    public Result check(ConnectorDescriptor descriptor) {
        
        Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        Set properties = descriptor.getConfigProperties();
        Iterator iterator = properties.iterator();
        // let's add the propery name
        HashSet<String> hs = new HashSet<String>();
        while (iterator.hasNext()) {
            EnvironmentProperty ep = (EnvironmentProperty) iterator.next();
            if (hs.add(ep.getName())==false) {
                // duplicate name...
                result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString(getClass().getName() + ".failed",
                "Error: More than one propery has a duplicate name [ {0} ] in the deployment descriptors",
		new Object[] {ep.getName()}));                     
                return result;
            }            
        }
        // success
        result.addGoodDetails(smh.getLocalString
			      ("tests.componentNameConstructor",
			       "For [ {0} ]",
			       new Object[] {compName.toString()}));	
	result.passed(smh.getLocalString(getClass().getName() + ".passed",
					 "There are no config properties with a duplicate name"));                     
        return result;
        
    }
}
