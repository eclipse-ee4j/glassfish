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
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Set;


/**
 * Dependent value class must be public and not abstract and must be serializable
 *
 * @author  Jerome Dochez
 * @version 
 */
public class DependentValueClassModifiers extends CmpFieldTest {

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
        String fieldName = persistentField.getName();
        String getMethodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        Method getMethod = getMethod(c, getMethodName, null);
        if (getMethod != null) {        
            Class returnType = getMethod.getReturnType();
            // check if this is a reference to a primitive or an array of primitive type
            if (returnType.isArray()) {
                returnType = returnType.getComponentType();
            }
            if (returnType.isPrimitive()) {
		result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                result.addGoodDetails(smh.getLocalString(
                    "com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CMPTest.DependentValueClassModifiers.notApplicable",
                    "Field [ {0} ] is not a dependent value class reference",
                    new Object[] {fieldName}));        
                return true;
            }
	    if (returnType.isInterface()) {
		result.addGoodDetails(smh.getLocalString
				      ("tests.componentNameConstructor",
				       "For [ {0} ]",
				       new Object[] {compName.toString()}));
                result.addGoodDetails(smh.getLocalString(
							 "com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CMPTest.DependentValueClassModifiers.notApplicable",
							 "Field [ {0} ] is not a dependent value class reference",
							 new Object[] {fieldName}));        
                return true;
            }
	    if (returnType.toString().startsWith("class java.")) {
		result.addGoodDetails(smh.getLocalString
				      ("tests.componentNameConstructor",
				       "For [ {0} ]",
				       new Object[] {compName.toString()}));
                result.addGoodDetails(smh.getLocalString(
							 "com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CMPTest.DependentValueClassModifiers.notApplicable",
							 "Field [ {0} ] is not a dependent value class reference",
							 new Object[] {fieldName}));        
                return true;
            }
             // it must be a reference to a bean's home or remote interface
            EjbBundleDescriptorImpl bundle = ((EjbDescriptor) entity).getEjbBundleDescriptor();
            if ((isValidInterface(returnType, bundle.getEjbs(),MethodDescriptor.EJB_REMOTE)) ||
		(isValidInterface(returnType, bundle.getEjbs(),MethodDescriptor.EJB_LOCAL))) {
		result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.addGoodDetails(smh.getLocalString(
							 "com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CMPTest.DependentValueClassModifiers.notApplicable",
							 "Field [ {0} ] is not a dependent value class reference",
							 new Object[] {fieldName}));        
		return true;
	    }
      
            // this is a reference to a dependent value class
            int modifiers = returnType.getModifiers();
	    if (Modifier.isPublic(modifiers) && 
		Modifier.isAbstract(modifiers) == false && 
		EjbUtils.isValidSerializableType(returnType)) {
		result.addGoodDetails(smh.getLocalString
				      ("tests.componentNameConstructor",
				       "For [ {0} ]",
				       new Object[] {compName.toString()}));
		result.addGoodDetails(smh.getLocalString(
				       "com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CMPTest.DependentValueClassModifiers.passed",
				       "Dependent value class [ {0} ] reference by cmp field [ {1} ] is public, not abstract and serializable",
				       new Object[] {returnType.getName(), fieldName}));        
		return true;
            } else {
		result.addWarningDetails(smh.getLocalString
				      ("tests.componentNameConstructor",
				       "For [ {0} ]",
				       new Object[] {compName.toString()}));
                result.addWarningDetails(smh.getLocalString(
				       "com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CMPTest.DependentValueClassModifiers.failed",
				       "Verifier cannot find out if [ {0} ] is a Dependent value class (reference by cmp field [ {1} ]) ",
				       new Object[] {returnType.getName(), fieldName})); 
		return false;
            }
        } else {
	    result.addErrorDetails(smh.getLocalString
		  	          ("tests.componentNameConstructor",
				   "For [ {0} ]",
				   new Object[] {compName.toString()}));
            result.addErrorDetails(smh.getLocalString
				   ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CMPTest.isAccessorDeclared.failed2",
				    "Error : Cannot find accessor [ {0} ] method for [ {1} ] field ",
		 new Object[] {getMethodName, fieldName}));       
            return false;
        }
    }
    
    private boolean isValidInterface(Class fieldType, Set<EjbDescriptor> entities, String interfaceType) {
        
        if (entities==null)
            return false;
        
        Iterator<EjbDescriptor> iterator = entities.iterator();
	if (interfaceType.equals(MethodDescriptor.EJB_REMOTE)) {
	    while (iterator.hasNext()) {
		EjbDescriptor entity = iterator.next();
		if (fieldType.getName().equals(entity.getHomeClassName()) ||
		    fieldType.getName().equals(entity.getRemoteClassName()))
		    return true;
	    }
	}
 	if (interfaceType.equals(MethodDescriptor.EJB_LOCAL)) {
	    while (iterator.hasNext()) {
		EjbDescriptor entity = iterator.next();
		if (fieldType.getName().equals(entity.getLocalHomeClassName()) ||
		    fieldType.getName().equals(entity.getLocalClassName()))
		    return true;
	    } 
	}
	return false;
    }
}
