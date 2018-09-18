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

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbUtils;
import com.sun.enterprise.tools.verifier.tests.ejb.RmiIIOPUtils;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;


/**
 * Check that the field type for all declated cmp fields of the entity bean
 * are of an acceptable type :
 *      - Java Primitive Type
 *      - Java Serializable class
 *      - Reference to a bean's home or bean's remote interface
 * 
 * @author  Jerome Dochez
 * @version 1.0
 */
public class CmpFields extends CmpFieldTest {
 
    /**
     * run an individual verifier test of a declated cmp field of the class
     *
     * @param entity the descriptor for the entity bean containing the cmp-field    
     * @param f the descriptor for the declared cmp field
     * @param c the class owning the cmp field
     * @parma r the result object to use to put the test results in
     * 
     * @return true if the test passed
     */    

    protected boolean runIndividualCmpFieldTest(Descriptor entity, Descriptor persistentField, Class c, Result result) {
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String cmpFieldName = persistentField.getName();
        String getMethodName = "get" + Character.toUpperCase(cmpFieldName.charAt(0)) + cmpFieldName.substring(1);
        Method getMethod = getMethod(c, getMethodName, null);
        Class fieldType;

        if (getMethod != null) {
	    boolean run = false;
            // get the return type for the setMethod
            fieldType = getMethod.getReturnType();
	    EjbBundleDescriptorImpl bundle = ((EjbDescriptor) entity).getEjbBundleDescriptor();
	    if (!RmiIIOPUtils.isValidRmiIDLPrimitiveType(fieldType) &&
		!EjbUtils.isValidSerializableType(fieldType)) {
		// it must be a reference to a bean's home or local interface
		if (!isValidInterface(fieldType, bundle.getEjbs(),MethodDescriptor.EJB_REMOTE, result)) {
		     result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addErrorDetails(smh.getLocalString
			   (getClass().getName() + ".failed",
			   "Error : Invalid type assigned for container managed field [ {0} ] in bean [ {1} ]",
			    new Object[] {((Descriptor)persistentField).getName(),entity.getName()}));
		    return false;
		}
		if (!isValidInterface(fieldType, bundle.getEjbs(),MethodDescriptor.EJB_LOCAL, result)) {
		     result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addErrorDetails(smh.getLocalString
			      (getClass().getName() + ".failed",
			       	"Error : Invalid type assigned for container managed field [ {0} ] in bean [ {1} ]",
				new Object[] {((Descriptor)persistentField).getName(),entity.getName()}));
				return false;
		}
	    }   
	    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()})); 
	    result.addGoodDetails(smh.getLocalString
		   (getClass().getName() + ".passed",
		    "Valid type assigned for container managed field [ {0} ] in bean [ {1} ]",
		    new Object[] {((Descriptor)persistentField).getName(),entity.getName()}));
	    run = true;  
	    return run;
	    
        } else {
	    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
            result.addErrorDetails(smh.getLocalString
		("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CMPTest.isAccessorDeclared.failed2",
		 "Error : Cannot find accessor [ {0} ] method for [ {1} ] field ",
		 new Object[] {getMethodName , persistentField.getName()}));       
        }
        return false;
    }

    private boolean isValidInterface(Class fieldType, Set<EjbDescriptor> entities,String interfaceType, Result result) {
	try {  
        if (entities==null)
            return false;
        
        Iterator<EjbDescriptor> iterator = entities.iterator();
	if(interfaceType.equals(MethodDescriptor.EJB_REMOTE)) {
	    while (iterator.hasNext()) {
		EjbDescriptor entity = iterator.next();
		
		if (fieldType.getName().equals(entity.getHomeClassName()) ||
		    fieldType.getName().equals(entity.getRemoteClassName()))
		    return true;
	    }
	}
	if(interfaceType.equals(MethodDescriptor.EJB_LOCAL)) {
	    while (iterator.hasNext()) {
		EjbDescriptor entity = iterator.next();
		
		if (fieldType.getName().equals(entity.getLocalHomeClassName()) ||
		    fieldType.getName().equals(entity.getLocalClassName()))
		    return true;
	    }
	}
        return false;
	}catch(Throwable t) {
	    result.addErrorDetails(smh.getLocalString
			      (getClass().getName() + ".failed",
			       	"Error occured in accessing remote/local interface",
				new Object[] {}));
	    return false;
	}
    }
}
