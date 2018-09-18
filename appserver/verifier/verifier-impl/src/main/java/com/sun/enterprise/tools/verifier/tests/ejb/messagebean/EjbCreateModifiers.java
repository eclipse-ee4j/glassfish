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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


/**
 * Verify that message beans implement the ejbCreate method with a void return
 * type
 *
 * @author  Jerome Dochez
 * @version 
 */
public class EjbCreateModifiers extends MessageBeanTest {

    /**
     * <p>
     * @return the name of the method on the message bean this test applies to
     * </p>
     */
    protected String getMethodName() {
        return "ejbCreate";
    }
    
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
        Class mbc = loadMessageBeanClass(descriptor, result);
        if (mbc!=null) {
            Method m = getMethod(mbc, getMethodName(),null);
            if (m!=null) {
                int modifiers = m.getModifiers();
                if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) {
                    result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));	
			
		    result.passed(smh.getLocalString
    		        ("com.sun.enterprise.tools.verifier.tests.ejb.messagebean.EjbCreateModifiers.passed",
                        "Message-Drive bean [ {0} ] provide an {1} implementation declared public and not static or final",
	                new Object[] {(descriptor).getEjbClassName(), getMethodName()}));                    
                } else {
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
		        ("com.sun.enterprise.tools.verifier.tests.ejb.messagebean.EjbCreateModifiers.failed",
                        "Error: Message-Drive bean [ {0} ] {1} implementation is either not public or is static or final",
	                new Object[] {(descriptor).getEjbClassName(), getMethodName()}));                       
                } 
            } else {
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString
		    ("com.sun.enterprise.tools.verifier.tests.ejb.messagebean.EjbCreateExists.failed",
                    "Error: Message-Drive bean [ {0} ] does not implement an {1} with no arguments",
	            new Object[] {(descriptor).getEjbClassName(), getMethodName()}));                       
            }                
        }         
        return result;
    }
}
