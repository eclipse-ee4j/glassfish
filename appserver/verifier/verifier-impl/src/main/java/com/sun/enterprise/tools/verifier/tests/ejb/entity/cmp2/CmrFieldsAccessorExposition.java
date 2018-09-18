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
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.descriptor.CMRFieldInfo;
import org.glassfish.ejb.deployment.descriptor.RelationRoleDescriptor;

import java.lang.reflect.Method;

/**
 * EJB 2.0 Spec 9.4.11 CMR accessor methods for relationships
 *  between entity beans should not be exposed in the remote interface
 * 
 * @author  Jerome Dochez
 * @version 
 */
public class CmrFieldsAccessorExposition extends CmrFieldTest {

   /**
     * run an individual verifier test of a declated cmr field of the class
     *
     * @param entity the descriptor for the entity bean containing the cmp-field    
     * @param info the descriptor for the declared cmr field
     * @param c the class owning the cmp field
     * @parma r the result object to use to put the test results in
     * 
     * @return true if the test passed
     */            
    protected boolean runIndividualCmrTest(Descriptor descriptor, RelationRoleDescriptor rrd, Class c, Result result) {
        
        // check first if this is one-to-one or many-to-one relationship ...previous version of ejb specs
	//   if ((!rrd.getIsMany() && !rrd.getPartner().getIsMany()) ||
	//     (rrd.getIsMany() && !rrd.getPartner().getIsMany())) {                
	    //  }
        // everyone falls back and should be checked

        boolean pass = true;
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
      
        // should not have accessor methods exposed. 
       	if (((EjbDescriptor)descriptor).getRemoteClassName() != null && 
	    !((((EjbDescriptor)descriptor).getRemoteClassName()).equals(""))) {
	    String interfaceType = ((EjbDescriptor)descriptor).getRemoteClassName();
	    try {             
		CMRFieldInfo info = rrd.getCMRFieldInfo();
		Class remoteInterface = Class.forName(interfaceType, false, getVerifierContext().getClassLoader());
		String getMethodName = "get" + Character.toUpperCase(info.name.charAt(0)) + info.name.substring(1);        
		String setMethodName = "set" + Character.toUpperCase(info.name.charAt(0)) + info.name.substring(1);        
		
		Method getMethod = getMethod(remoteInterface, getMethodName, null);
		if (getMethod != null) {
            addErrorDetails(result, compName);
		    result.addErrorDetails(smh.getLocalString
		    ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmrFieldsAccessorExposition.failed",
		     "Error : CMR field {0} accessor method [ {1} ] is exposed through the component interface [ {2} ]",
		     new Object[] {"get", info.name, interfaceType}));     
		    pass = false;
		} else {
		     result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addGoodDetails(smh.getLocalString
			("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmrFieldsAccessorExposition.passed",
			 "CMR field {0} accessor method [ {1} ] is not exposed through the component interface [ {2} ]",
			 new Object[] {"get", info.name, interfaceType}));        
		    pass = true;           
		}
		
		Class parms[] = { info.type };
		Method setMethod = getMethod(remoteInterface, setMethodName, parms );        
		if (setMethod != null) {
            addErrorDetails(result, compName);
		    result.addErrorDetails(smh.getLocalString
		       ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmrFieldsAccessorExposition.failed",
		       "Error : CMR field {0} accessor method [ {1} ] is exposed through the component interface [ {2} ]",
			new Object[] {"set", info.name, interfaceType}));   
		    
		    pass = false;
		} else {
		     result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addGoodDetails(smh.getLocalString
			("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmrFieldsAccessorExposition.passed",
			 "CMR field [{0}] accessor method [ {1} ] is not exposed through the component interface [ {2} ]",
			 new Object[] {"set", info.name, interfaceType}));                    
		}  
		
	    } catch (Exception e) {
		Verifier.debug(e);
        addErrorDetails(result, compName);
		result.addErrorDetails(smh.getLocalString
			      ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmrFieldsAccessorExposition.failedException",
			       "Error:  [{0}] class not found or local interface not defined",
			       new Object[] {interfaceType}));
		pass = false;
	
	    }                 
	} 
        return pass;        
    }
}
