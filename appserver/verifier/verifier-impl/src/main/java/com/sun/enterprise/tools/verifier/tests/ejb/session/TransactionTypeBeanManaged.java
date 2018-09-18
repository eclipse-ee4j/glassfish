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
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;


/** 
 * Session Bean's bean-managed transaction demarcation test.  
 *
 *  The enterprise bean with bean-managed transaction demarcation must 
 *  be a Session bean.
 */
public class TransactionTypeBeanManaged extends EjbTest implements EjbCheck { 


    /** 
     * Session Bean's bean-managed transaction demarcation test.  
     *
     *  The enterprise bean with bean-managed transaction demarcation must 
     *  be a Session bean.
     *     
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	String transactionType = descriptor.getTransactionType();
	if (EjbSessionDescriptor.BEAN_TRANSACTION_TYPE.equals(transactionType)) {
	    if (descriptor instanceof EjbSessionDescriptor) {
		result.addGoodDetails(smh.getLocalString
				      ("tests.componentNameConstructor",
				       "For [ {0} ]",
				       new Object[] {compName.toString()}));
		result.passed(smh.getLocalString
			      (getClass().getName() + ".passed",
			       "[ {0} ] is valid transactionType within Session bean [ {1} ]",
			       new Object[] {transactionType, descriptor.getName()}));
	    } else if (descriptor instanceof EjbEntityDescriptor) {
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString
			      (getClass().getName() + ".failedException",
			       "Error: [ {0} ] is not valid transactionType within entity bean [ {1} ]",
			       new Object[] {transactionType, descriptor.getName()}));
	    } 
	    return result;
	} else if (EjbSessionDescriptor.CONTAINER_TRANSACTION_TYPE.equals(transactionType)) {
	    result.addNaDetails(smh.getLocalString
				("tests.componentNameConstructor",
				 "For [ {0} ]",
				 new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "Expected [ {0} ] transaction demarcation, but [ {1} ] bean has [ {2} ] transaction demarcation.",
				  new Object[] {EjbSessionDescriptor.BEAN_TRANSACTION_TYPE,descriptor.getName(),EjbSessionDescriptor.CONTAINER_TRANSACTION_TYPE}));
	    return result;
	} else {
	    result.addNaDetails(smh.getLocalString
				("tests.componentNameConstructor",
				 "For [ {0} ]",
				 new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "Expected [ {0} ] transaction demarcation, but [ {1} ] bean has [ {2} ] transaction demarcation.",
				  new Object[] {EjbSessionDescriptor.BEAN_TRANSACTION_TYPE,descriptor.getName(),transactionType}));
	    return result;
	} 
    }
}
