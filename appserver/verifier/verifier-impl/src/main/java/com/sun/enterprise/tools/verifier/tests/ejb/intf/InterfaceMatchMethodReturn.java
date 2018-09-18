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

package com.sun.enterprise.tools.verifier.tests.ejb.intf;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.lang.reflect.Method;
import java.util.logging.Level;

/** 
 * Local or remote interface/business matching methods return type test.
 * Verify the following:
 *
 *   For each method defined in the local or remote interface, there must be a matching
 *   method in the enterprise Bean's class. The matching method must have:
 *   . The same return type.
 */
abstract public class InterfaceMatchMethodReturn extends InterfaceMethodTest { 
    /**
     * <p>
     * run an individual verifier test against a declared method of the 
     * local/remote interface.
     * </p>
     * 
     * @param descriptor the deployment descriptor for the bean
     * @param method the method to run the test on
     * @return true if the test passes
     */
    protected boolean runIndividualMethodTest(EjbDescriptor descriptor, Method method, Result result) {
        
        boolean businessMethodFound, returnMatch;
        ComponentNameConstructor compName = null;
        
        try {	  
            compName = getVerifierContext().getComponentNameConstructor();
            Class methodReturnType = method.getReturnType();
            // retrieve the EJB Class Methods
            ClassLoader jcl = getVerifierContext().getClassLoader();            
            Class EJBClass = Class.forName(descriptor.getEjbClassName(), false, jcl);
            
            // start do while loop here....
            do {
                Method [] businessMethods = EJBClass.getDeclaredMethods();
                
                // try and find the business method in the EJB Class Methods
                businessMethodFound = false;
                returnMatch = false;
                for (Method businessMethod : businessMethods) {
                    if (method.getName().equals(businessMethod.getName())) {
                        businessMethodFound = true;
                        Class businessMethodReturnType = businessMethod.getReturnType();
                        if (methodReturnType.equals(businessMethodReturnType)) {
                            returnMatch = true;
                            break;
                        } // if the bean & local/remote interface method return value matches
                    } else {
                        continue;
                        
                    } // if the bean & local/remote interface method match
                }  // for all the bean class business methods
                
                // now display the appropriate results for this particular business
                // method
                if (businessMethodFound && returnMatch) {
                    addGoodDetails(result, compName);
                    result.addGoodDetails(smh.getLocalString
                            (getClass().getName() + ".passed",
                            "The corresponding business method with a matching " +
                            "return type was found."));
                    return true;
                } else if(businessMethodFound && !returnMatch) {
                    logger.log(Level.FINE, getClass().getName() + ".debug1",
                            new Object[] {method.getDeclaringClass().getName(),method.getName()});
                    logger.log(Level.FINE, getClass().getName() + ".debug3",
                            new Object[] {method.getName()});
                    logger.log(Level.FINE, getClass().getName() + ".debug2");
                }
                
            } while (((EJBClass = EJBClass.getSuperclass()) != null) && (!(businessMethodFound && returnMatch)));
            
            
            if (!returnMatch) {
                addErrorDetails(result, compName);
                result.addErrorDetails(smh.getLocalString
                        (getClass().getName() + ".failed",
                        "Error: No corresponding business method with matching " +
                        "return type was found for method [ {0} ].",
                        new Object[] {method.getName()}));
            }
        } catch (ClassNotFoundException e) {
            Verifier.debug(e);
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failedException",
                    "Error: "+ getInterfaceType() +"interface [ {0} ] does not " +
                    "exist or is not loadable within bean [ {1} ]",
                    new Object[] {getClassName(descriptor),descriptor.getName()}));
        }
        
        return false;
    }
    
    private String getClassName(EjbDescriptor descriptor) {
        return getInterfaceName(descriptor);
    }
}
