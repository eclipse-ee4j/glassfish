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
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;

/**
 * Superclass for all the Message Bean tests
 *
 * @author  Jerome Dochez
 * @version 
 */
abstract public class MessageBeanTest extends EjbTest {
    
    /** 
     * Run a verifier test against an individual declared message
     * drive bean component
     * 
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */    
    abstract public Result check(EjbMessageBeanDescriptor descriptor);
    ComponentNameConstructor compName = null;

    /** 
     *
     * Container-managed persistent fields test, iterates over all declared
     * cmp fields and invoke the runIndividualCmpFieldTest nethod
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {
        
        if (descriptor instanceof EjbMessageBeanDescriptor) {
            return check((EjbMessageBeanDescriptor) descriptor);
        } else {
            Result result = getInitializedResult();
	    compName = getVerifierContext().getComponentNameConstructor(); 
	    result.addNaDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
		("com.sun.enterprise.tools.verifier.tests.ejb.messagebean.MessageBeanTest.notApplicable",
		 "Test apply only to message-driven Bean components"));
            return result;                                
        }
    }   
    
    /**
     * <p>
     * load the declared message bean class from the archive
     * </p>
     * 
     * @param descriptor deployment descriptor for the message bean
     * @param result result to use if failure
     * @return the message bean class
     */
    protected Class loadMessageBeanClass(EjbMessageBeanDescriptor descriptor, Result result) {
        try {
	    compName = getVerifierContext().getComponentNameConstructor();
            VerifierTestContext context = getVerifierContext();
	    ClassLoader jcl = context.getClassLoader();
            return Class.forName(descriptor.getEjbClassName(), false, getVerifierContext().getClassLoader());
        } catch (ClassNotFoundException e) {
            Verifier.debug(e);
	    result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
	    result.failed(smh.getLocalString
	       ("com.sun.enterprise.tools.verifier.tests.ejb.messagebean.classnotfoundexception",
               "Cannot load declared message-driven bean component [ {0} ]"));        
            return null;
        }
    }
}
