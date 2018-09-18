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

package com.sun.enterprise.tools.verifier.tests.ejb.homeintf;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Vector;

/** 
 * Superclass for all Home methods tests.
 *
 * @author Jerome Dochez
 * @version
 */
abstract public class HomeMethodTest extends EjbTest  {  

    /** Method tells the name of the home interface class that called this test
     */
    abstract protected String getHomeInterfaceName(EjbDescriptor descriptor);
    abstract protected String getSuperInterface();
    
    /** <p>
     * run an individual home method test
     * </p>
     * @param descriptor the deployment descriptor for the entity bean
     * @param result the result object
     * @param m the mehtod to test
     */    
  
    abstract protected void runIndividualHomeMethodTest( Method m,EjbDescriptor descriptor, Result result);
    
    /** 
     * 
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

 	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if(getHomeInterfaceName(descriptor) == null || "".equals(getHomeInterfaceName(descriptor))){
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                       ("com.sun.enterprise.tools.verifier.tests.ejb.localinterfaceonly.notapp",
                        "Not Applicable because, EJB [ {0} ] has Local Interfaces only.",
                                          new Object[] {descriptor.getEjbClassName()}));

	    return result;
	}

	if (!(descriptor instanceof EjbSessionDescriptor) &&
	    !(descriptor instanceof EjbEntityDescriptor)) {
        addNaDetails(result, compName);
	    result.notApplicable(smh.getLocalString
				 ("com.sun.enterprise.tools.verifier.tests.ejb.homeintf.HomeMethodTest.notApplicable1",
				  "Test apply only to session or entity beans."));
	    return result;                
        }
        boolean homeMethodFound = false;
        
	try {	  
	    // retrieve the remote interface methods
	    ClassLoader jcl = getVerifierContext().getClassLoader();
	    Class homeInterfaceClass = Class.forName(getClassName(descriptor), false, jcl);
            
            Vector<Method> v = new Vector<Method>(); 

            while (homeInterfaceClass != null && 
                    !homeInterfaceClass.getName().equals(getSuperInterface()) &&
                    !homeInterfaceClass.getName().equals("java.lang.Object")) {
	        Method [] homeInterfaceMethods = homeInterfaceClass.getDeclaredMethods();
                for (int i=0;i<homeInterfaceMethods.length;i++) {
                    v.add(homeInterfaceMethods[i]);
                }
                homeInterfaceClass = homeInterfaceClass.getSuperclass();
            }
                
            
	    Iterator iterator = v.iterator();
            while (iterator.hasNext()) {
                Method method = (Method) iterator.next();
                String methodName = method.getName();
                if (methodName.startsWith("create") || methodName.startsWith("find") || 
                    methodName.startsWith("remove")) 
                    continue;
                
                Method m = getMethod(javax.ejb.EJBHome.class, methodName, 
                                     method.getParameterTypes());
                if (m!=null) {
                    // this is an EJBHome method...
                    continue;
                } 
            
                homeMethodFound = true;
 
		// if (!runIndividualHomeMethodTest( method,descriptor, result)) 
		//  oneFailed = true;
		runIndividualHomeMethodTest( method,descriptor, result);
		if (result.getStatus() == Result.FAILED) 
		    break;
                
	    }
	} catch (ClassNotFoundException e) {
	    Verifier.debug(e);
	    addErrorDetails(result, compName);
		result.failed(smh.getLocalString
			  ("com.sun.enterprise.tools.verifier.tests.ejb.homeintf.HomeMethodTest.failedException",
			   "Error: Home interface [ {0} ] does not exist or is not loadable within bean [ {1} ]",
			   new Object[] {getClassName(descriptor),descriptor.getName()}));
	}

        if (!homeMethodFound) {
	    addNaDetails(result, compName);
	    result.notApplicable(smh.getLocalString
			  ("com.sun.enterprise.tools.verifier.tests.ejb.homeintf.HomeMethodTest.notApplicable2",
			   "Home interface [ {0} ] does not define any home methods",
			   new Object[] {getClassName(descriptor)}));
        } //else {
    	  //  result.setStatus(oneFailed?result.FAILED:Result.PASSED);
	// }
	return result;
    }

    private String getClassName(EjbDescriptor descriptor) {
	return getHomeInterfaceName(descriptor);
    } 
}
