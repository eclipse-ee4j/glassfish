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

package com.sun.enterprise.tools.verifier.tests.ejb.session;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

/** 
 * Session Bean Transaction demarcation type test.  
 * For bean managed session beans, it doesn't make sense to have 
 * container transactions.
 */
public class TransactionTypeNullForContainerTX extends EjbTest implements EjbCheck { 


    /** 
     * Session Bean Transaction demarcation type test.  
     * For bean managed session beans, it doesn't make sense to have 
     * container transactions.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor instanceof EjbSessionDescriptor) {
	    String transactionType = descriptor.getTransactionType();
	    if (EjbDescriptor.BEAN_TRANSACTION_TYPE.equals(transactionType)) {
		// taken from DOL - remember that for bean managed session beans, 
		// it doesn't make sense to have container transactions
		// you'll have to enforce this in the object model somewhere, 
		// and in the UI
                try {
		    if (descriptor.getMethodContainerTransactions().size() > 0) {
		        // shouldn't have container transaction for bean managed session 
		        // since container transaction is not null, it's defined, we fail
		        // test
		        result.addErrorDetails(smh.getLocalString
					       ("tests.componentNameConstructor",
						"For [ {0} ]",
						new Object[] {compName.toString()}));
			result.failed(smh.getLocalString
				      (getClass().getName() + ".failed",
				       "Error: Session Beans [ {0} ] with [ {1} ] managed \n" +
				       "transaction demarcation should not have container \n" +
				       "transactions defined.",
				       new Object[] {descriptor.getName(),transactionType}));
		    } else {
		        // container transaction is null, not defined, which is correct
		        // shouldn't have container transaction for bean managed session 
		        result.addGoodDetails(smh.getLocalString
					      ("tests.componentNameConstructor",
					       "For [ {0} ]",
					       new Object[] {compName.toString()}));
			result.passed(smh.getLocalString
				      (getClass().getName() + ".passed",
				       "This session bean [ {0} ] is [ {1} ] managed and correctly declares no container transactions.",
				       new Object[] {descriptor.getName(),transactionType}));
		    }
		    return result;
                } catch (NullPointerException e) {
		    // container transaction is null, not defined, which is correct
		    // shouldn't have container transaction for bean managed session 
		    result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
		    result.passed(smh.getLocalString
				  (getClass().getName() + ".passed",
				   "This session bean [ {0} ] is [ {1} ] managed and correctly declares no container transactions.",
				   new Object[] {descriptor.getName(),transactionType}));
		    return result;
		}
		
	    } else {
		// not bean/container managed, but is a session/entity bean
		// (i.e it's CONTAINER_TRANSACTION_TYPE)
		result.addNaDetails(smh.getLocalString
				    ("tests.componentNameConstructor",
				     "For [ {0} ]",
				     new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable1",
				      "Session bean [ {0} ], expected [ {1} ] managed, but called with [ {2} ] managed.",
				      new Object[] {descriptor.getName(),EjbDescriptor.BEAN_TRANSACTION_TYPE, EjbDescriptor.CONTAINER_TRANSACTION_TYPE}));
		return result;
	    }
	} else {
	    result.addNaDetails(smh.getLocalString
				("tests.componentNameConstructor",
				 "For [ {0} ]",
				 new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "[ {0} ] expected {1} \n bean, but called with {2} bean.",
				  new Object[] {getClass(),"Session","Entity"}));
	    return result;
	} 
    }
}
