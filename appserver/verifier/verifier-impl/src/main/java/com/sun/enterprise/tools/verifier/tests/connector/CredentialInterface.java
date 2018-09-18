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
 * CredentialInterface.java
 *
 * Created on October 3, 2000, 2:42 PM
 */

package com.sun.enterprise.tools.verifier.tests.connector;

import java.util.*;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.AuthMechanism;

/**
 * All Credential interface declared in the DD should be one of the 
 * allowed interface
 *
 * @author  Jerome Dochez
 * @version 
 */
public class CredentialInterface extends ConnectorTest implements ConnectorCheck {


    private static String[] allowedInterfaces = new String[] {
                              "javax.resource.spi.security.PasswordCredential",
                              "org.ietf.jgss.GSSCredential",
                              "javax.resource.spi.security.GenericCredential" };

    /** <p>
     * All Credential interface declared in the DD should be one of the 
     * allowed interface
     * </p>
     *
     * @paramm descriptor deployment descriptor for the rar file
     * @return result object containing the result of the individual test
     * performed
     */
    public Result check(ConnectorDescriptor descriptor) {
        boolean oneFailed = false;
        Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        if(!descriptor.getOutBoundDefined())
        {
          result.addNaDetails(smh.getLocalString
              ("tests.componentNameConstructor",
               "For [ {0} ]",
               new Object[] {compName.toString()}));
          result.notApplicable(smh.getLocalString
              ("com.sun.enterprise.tools.verifier.tests.connector.managed.notApplicableForInboundRA",
               "Resource Adapter does not provide outbound communication"));
          return result;
        }
        Set mechanisms = 
        descriptor.getOutboundResourceAdapter().getAuthMechanisms();
        if (mechanisms.isEmpty()) {
            // passed
	    result.addGoodDetails(smh.getLocalString
				  ("tests.componentNameConstructor",
				   "For [ {0} ]",
				   new Object[] {compName.toString()}));	
	    result.passed(smh.getLocalString
    	        ("com.sun.enterprise.tools.verifier.tests.connector.AuthMechType.nonexist",
                 "No authentication mechanism defined for this resource adapater"));
            return result;
        }
        Iterator mechIterator = mechanisms.iterator();
        while (mechIterator.hasNext()) {
            AuthMechanism am = (AuthMechanism) mechIterator.next();
            String credInterface = am.getCredentialInterface();
            boolean allowedInterface = false;            
            if (credInterface!=null) { 
                for (int i=0;i<allowedInterfaces.length;i++) {
                    if (credInterface.equals(allowedInterfaces[i])) {
                        allowedInterface = true;
                        break;
                    }
                }
            }
            if (!allowedInterface || credInterface == null) {
                // failed
                oneFailed = true;
        	result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString
    	            ("com.sun.enterprise.tools.verifier.tests.connector.CredentialInterface.failed",
                    "Authentication mechanism credential interface [ {0} ] defined in the credential-interface tag is not allowed",
                    new Object[] {credInterface}));
            }
        }
        if (!oneFailed) {
	    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));	
		result.passed(smh.getLocalString
    	        ("com.sun.enterprise.tools.verifier.tests.connector.CredentialInterface.passed",
                 "All defined authorization mechanism credential interfaces are allowed"));
        }
        return result;        
    }
}
