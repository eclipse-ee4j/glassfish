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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.PersistentFieldInfo;

import java.lang.reflect.Method;

/**
 * EJB 2.0 Spec 9.4.11 Set Accessor method for primary key fields should not be 
 * exposed in the remote/local interface
 * 
 * @author  Jerome Dochez
 * @version 
 */
public class CmpFieldsAccessorExposition extends CMPTest {
    Result result = null;
    ComponentNameConstructor compName = null;

    /** 
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbCMPEntityDescriptor descriptor) {

	result = getInitializedResult();
        boolean oneFailed = false;
	compName = getVerifierContext().getComponentNameConstructor();
        
	if (descriptor.getRemoteClassName() != null && !((descriptor.getRemoteClassName()).equals(""))) 
	    oneFailed = commonToBothInterfaces(descriptor.getRemoteClassName(),descriptor); 
	if(oneFailed == false) {
	    if (descriptor.getLocalClassName() != null && !((descriptor.getLocalClassName()).equals(""))) 
		oneFailed = commonToBothInterfaces(descriptor.getLocalClassName(),descriptor); 
	}
	if (oneFailed) 
            result.setStatus(Result.WARNING);
        else 
            result.setStatus(Result.PASSED);
        return result;
    }

 /** 
     * This method is responsible for the logic of the test. It is called for both local and remote interfaces.
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @param remote for the Remote/Local interface of the Ejb. 
     * @return boolean the results for this assertion i.e if a test has failed or not
     */
  
    private boolean commonToBothInterfaces(String remote, EjbDescriptor descriptor) {
	boolean oneFailed = false;
	try { 
	   Class c = Class.forName(remote, false, getVerifierContext().getClassLoader());   
	    boolean foundAtLeastOne = false;
            
	    try {
		// Check first that pk fields set methods are mot part of the remote interface                
		PersistentFieldInfo[] pkFieldInfos = ((EjbCMPEntityDescriptor)descriptor).getPersistenceDescriptor().getPkeyFieldInfo();
		for (int i=0;i<pkFieldInfos.length;i++) {
		    foundAtLeastOne = true;
		    PersistentFieldInfo info = pkFieldInfos[i];
		    // check that setXXX is not part of the remote interface
		    String setMethodName = "set" + Character.toUpperCase(info.name.charAt(0)) + info.name.substring(1);                
		    Class parms[] = { info.type };
		    Method setMethod = getMethod(c, setMethodName, parms );        
		    if (setMethod != null) {
			// oopss
			result.addWarningDetails(smh.getLocalString
						 ("tests.componentNameConstructor",
						  "For [ {0} ]",
						  new Object[] {compName.toString()}));
			result.addWarningDetails(smh.getLocalString
			    ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmpFieldsAccessorExposition.failed",
			     "Error : Primary key field set accessor method [ {0} ] is exposed through the component interface [ {1} ]",
			     new Object[] {info.name,remote}));  
			oneFailed = true;
		    } else {
			result.addGoodDetails(smh.getLocalString
					      ("tests.componentNameConstructor",
					       "For [ {0} ]",
					       new Object[] {compName.toString()}));
			result.addGoodDetails(smh.getLocalString
			    ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmpFieldsAccessorExposition.passed",
			     "Primary key field set accessor method [ {0} ] is not exposed through the component interface [ {1} ]",
			     new Object[] {info.name,remote}));                    
		    }
		}
		if (foundAtLeastOne == false) {
		    result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
		    result.addGoodDetails(smh.getLocalString
		     ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmpFieldsAccessorExposition.notApplicable",
		      "No persistent fields found.",
		      new Object[] {})); 
		    return oneFailed;
		}
		
	    } catch (RuntimeException rt) {
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString
			      ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmpFieldsAccessorExposition.failedException1",
			   "Exception occured while trying to access Primary key info in PersistenceDescriptor.",
			   new Object[] {}));
	    }
	    return oneFailed;
	} catch (ClassNotFoundException e) {
	    Verifier.debug(e);
	    result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
	    result.failed(smh.getLocalString
			  ("com.sun.enterprise.tools.verifier.tests.ejb.EjbTest.failedException",
			   "Error: [ {0} ] class not found.",
			   new Object[] {descriptor.getEjbClassName()}));
	    oneFailed = true;
	    return oneFailed;
	    
	}            
    }       
}
