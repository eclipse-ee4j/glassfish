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

/*
 * InteractionSpecJavaBeansCompliance.java
 *
 * Created on October 5, 2000, 5:02 PM
 */

package com.sun.enterprise.tools.verifier.tests.connector.cci;

import java.io.File;
import java.beans.*;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorCheck;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorTest;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;

/**
 *
 * @author  Jerome Dochez
 * @version 
 */
public class InteractionSpecJavaBeansCompliance extends ConnectionFactoryTest implements ConnectorCheck {


    /** <p>
     * all connector tests should implement this method. it run an individual
     * test against the resource adapter deployment descriptor.
     * </p>
     *
     * @paramm descriptor deployment descriptor for the rar file
     * @return result object containing the result of the individual test
     * performed
     */
    public Result check(ConnectorDescriptor descriptor) {
        
        boolean oneFailed=false;
        Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
        if (isCCIImplemented(descriptor, result)) {
            //File jarFile = Verifier.getJarFile(descriptor.getModuleDescriptor().getArchiveUri());
//            File f=Verifier.getArchiveFile(descriptor.getModuleDescriptor().getArchiveUri());
            Class mcf = findImplementorOf(descriptor, "jakarta.resource.cci.InteractionSpec");
            if (mcf != null) {
                try {
                    BeanInfo bi = Introspector.getBeanInfo(mcf, Object.class);
                    PropertyDescriptor[] properties = bi.getPropertyDescriptors();
                    for (int i=0; i<properties.length;i++) {
                        // each property should have a getter/setter
                        if (properties[i].getReadMethod()==null || 
                            properties[i].getWriteMethod()==null) {
                                // this is an error.
                                oneFailed=true;
                                result.addErrorDetails(smh.getLocalString
						       ("tests.componentNameConstructor",
							"For [ {0} ]",
							new Object[] {compName.toString()}));
				result.failed(smh.getLocalString
					      (getClass().getName() + ".failed",
					       "Error: The jakarta.resource.cci.InteractionSpec implementation [ {0} ] of the property [ {1} ] is not JavaBeans compliant",
					       new Object[] {mcf.getName(), properties[i].getName()} ));                                                                                
			}
                        if (!properties[i].isConstrained() && !properties[i].isBound()) {
                            oneFailed=true;
                            result.addErrorDetails(smh.getLocalString
						   ("tests.componentNameConstructor",
						    "For [ {0} ]",
						    new Object[] {compName.toString()}));
			    result.failed(smh.getLocalString
					  (getClass().getName() + ".failed2",
					   "Error: The property [ {0} ] of the jakarta.resource.cci.InteractionSpec implementation [ {1} ] is not bound or constrained",
					   new Object[] {properties[i].getName(), mcf.getName()} ));                                                                                
                        }
                    }
                } catch (IntrospectionException ie) {
		    result.addNaDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
                    result.notApplicable(smh.getLocalString
    	                (getClass().getName() + ".failed",
                        "Error: The jakarta.resource.cci.InteractionSpec implementation [ {0} ] is not JavaBeans compliant",
                        new Object[] {mcf.getName()} ));                                                
                    return result;
                }
                // now iterates over the properties and look for descrepencies
            } else {
		result.addNaDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
                result.notApplicable(smh.getLocalString
    	            (getClass().getName() + ".nonexist",
                    "Error: While the CCI interfaces are implemented, the jakarta.resource.cci.InteractionSpec is not"));         
                return result;
            }
                
        } else {
	    result.addNaDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
    	        ("com.sun.enterprise.tools.verifier.tests.connector.cci.InteractionExistence.notapp",
                 "NotApplicable : The CCI interfaces do not seem to be implemented by this resource adapter"));                    
            return result;            
        }                
        if (!oneFailed) {
            result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));	
		result.passed(smh.getLocalString
                (getClass().getName() + ".passed",
                "The jakarta.resource.cci.InteractionSpec implementation is JavaBeans compliant"));                     
        }
        return result;
    }
}
