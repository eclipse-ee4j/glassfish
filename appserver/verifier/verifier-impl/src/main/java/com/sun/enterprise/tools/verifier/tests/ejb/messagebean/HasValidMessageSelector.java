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

package com.sun.enterprise.tools.verifier.tests.ejb.messagebean;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;


/**
 * Verify that message beans message-selector is valid
 *
 * @author  Jerome Dochez
 * @version 
 */
public class HasValidMessageSelector extends MessageBeanTest {

    /** 
     * Run a verifier test against an individual declared message
     * drive bean component
     * 
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbMessageBeanDescriptor descriptor) {
        
        Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String messageSelector = descriptor.getJmsMessageSelector();
        if (messageSelector != null) {
            try {
                // TODO(Sahoo): Fix me
                // I don't see IAMJmsUtil.class in v3 yet, so currently
                // this test does nothing.
//                IASJmsUtil.validateJMSSelector(messageSelector);
//        	result.addGoodDetails(smh.getLocalString
//				       ("tests.componentNameConstructor",
//					"For [ {0} ]",
//					new Object[] {compName.toString()}));
//		result.passed(smh.getLocalString
//	            ("com.sun.enterprise.tools.verifier.tests.ejb.messagebean.HasValidMessageSelector.failed",
//                    "Message-driven bean [ {0} ] defines a valid message selector",
//                    new Object[] {descriptor.getName()}));
            } catch (Exception e) {
        	result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString
	            ("com.sun.enterprise.tools.verifier.tests.ejb.messagebean.HasValidMessageSelector.failed",
                    "Error : Message-driven bean [ {0} ] defines an invalid message selector",
                    new Object[] {descriptor.getName()}));
            }           
        } else {
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
		("com.sun.enterprise.tools.verifier.tests.ejb.messagebean.HasValidMessageSelector.notApplicable",
                 "Message-driven bean [ {0} ] does not define a message selector",
                new Object[] {descriptor.getName()}));            
        }
        return result;
    }
}
