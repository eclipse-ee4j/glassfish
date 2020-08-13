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
 * TransactionSupport.java
 *
 * Created on September 20, 2000, 9:29 AM
 */

package com.sun.enterprise.tools.verifier.tests.connector;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;

/**
 * Verify that the Transaction Support for the resource adapter is of an
 * acceptable value
 *
 * @author  Jerome Dochez
 * @version 
 */
public class TransactionSupport extends ConnectorTest implements ConnectorCheck {

    private final String[] acceptableValues = {
        ConnectorTagNames.DD_NO_TRANSACTION,
        ConnectorTagNames.DD_LOCAL_TRANSACTION,
        ConnectorTagNames.DD_XA_TRANSACTION };
                    
    /** 
     * <p>
     * Verifier test implementation. Check for the transaction-support 
     * deployment field which should be one of the acceptable values :
     *          NoTransaction
     *          LocalTransaction
     *          XATransaction
     * </p>
     *
     * @param <code>ConnectorDescritor</code>The deployment descriptor for
     * the connector.
     * @return <code>Result</code> Code execution result
     */
    public Result check(ConnectorDescriptor descriptor) {
        
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
        String connectorTransactionSupport =
        descriptor.getOutboundResourceAdapter().getTransSupport();
        
        // No transaction support specified, this is an error
        if (connectorTransactionSupport==null) {
	    result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
	    result.failed(smh.getLocalString
			  (getClass().getName() + ".nonexist",
			   "Error: No Transaction support specified for resource adapter",
			   new Object[] {connectorTransactionSupport}));        
            return result;
        }
        
        // let's loop over all acceptable values to check the declared one is valid
        for (int i=0;i<acceptableValues.length;i++) {
            if (connectorTransactionSupport.equals(acceptableValues[i])) {
                    
                // Test passed, we found an acceptable value
	       result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));	
		result.passed(smh.getLocalString
	            (getClass().getName() + ".passed",
                    "Transaction support [ {0} ] for resource adapter is supported",
	            new Object[] {connectorTransactionSupport}));
               return result;
            }     
        }
        
        // If we end up here, we haven't found an acceptable transaction support
	result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	result.failed(smh.getLocalString
	       (getClass().getName() + ".failed",
                "Error: Deployment descriptor transaction-support [ {0} ] for resource adapter is not valid",
		new Object[] {connectorTransactionSupport}));        
        return result;
    }
}
