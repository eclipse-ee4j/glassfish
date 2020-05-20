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
 * ManagedConnectionFactoryProperties.java
 *
 * Created on September 27, 2000, 3:01 PM
 */

package com.sun.enterprise.tools.verifier.tests.connector.managed;

import java.util.*;
import java.lang.reflect.Method;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.connector.ConnectorCheck;

import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.OutboundResourceAdapter;

/**
 * Test that the class declared implementing the jakarta.resource.spi.ManagedConnectionFactory
 * interface implements the properties declared under the config-property
 * xml tag under the followind requirements :
 *      - Provide a getter and setter method ala JavaBeans
 *      - Properties should be either bound or constrained
 *      - PropertyListener registration/unregistration methods are public
 *
 * @author  Jerome Dochez
 * @version 
 */
public class ManagedConnectionFactoryProperties 
    extends ManagedConnectionFactoryTest
    implements ConnectorCheck 
{
    /** <p>
     * Test that the class declared implementing the jakarta.resource.spi.ManagedConnectionFactory
     * interface implements the properties declared under the config-property
     * xml tag under the followind requirements :
     *      - Provide a getter and setter method ala JavaBeans
     *      - Properties should be either bound or constrained
     *      - PropertyListener registration/unregistration methods are public
     * </p>
     *
     * @paramm descriptor deployment descriptor for the rar file
     * @return result object containing the result of the individual test
     * performed
     */
    public Result check(ConnectorDescriptor descriptor) {

        Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        // test NA for inboundRA
        if(!descriptor.getOutBoundDefined())
        {
          result.addNaDetails(smh.getLocalString
              ("tests.componentNameConstructor",
               "For [ {0} ]",
               new Object[] {compName.toString()}));
          result.notApplicable(smh.getLocalString
              ("com.sun.enterprise.tools.verifier.tests.connector.managed.notApplicableForInboundRA",
               "Resource Adapter does not provide outbound communication"));
          return result;
        }
        boolean oneFailed=false;
	OutboundResourceAdapter outboundRA =
	    descriptor.getOutboundResourceAdapter();

	Set connDefs = outboundRA.getConnectionDefs();
	Iterator iter = connDefs.iterator();
	while(iter.hasNext()) {
	    
	    ConnectionDefDescriptor connDefDesc = (ConnectionDefDescriptor)
		iter.next();
	    Set configProperties = connDefDesc.getConfigProperties();
	    if (!configProperties.isEmpty()) {
		Iterator propIterator = configProperties.iterator();
		Class mcf = testManagedConnectionFactoryImpl(descriptor, result);
		if (mcf == null) {
		    // not much we can do without the class, the superclass should have
		    // set the error code now, just abandon
		    return result;
		}
		while (propIterator.hasNext()) {
		    EnvironmentProperty ep = (EnvironmentProperty) propIterator.next();
		    
		    // Set method first
		    String propertyName = Character.toUpperCase(ep.getName().charAt(0)) + ep.getName().substring(1);
		    String setMethodName = "set" + propertyName;
		    Class[] parmTypes = new Class[] { ep.getValueType() };
		    Method m = getMethod(mcf, setMethodName, parmTypes);
		    if (m!=null) {
			result.addGoodDetails(smh.getLocalString
					      ("tests.componentNameConstructor",
					       "For [ {0} ]",
					       new Object[] {compName.toString()}));
			result.addGoodDetails(smh.getLocalString(getClass().getName() + ".passed", 
								 "Found a JavaBeans compliant accessor method [ {0} ] for the config-property [ {1} ]",
								 new Object[] {  m, ep.getName()}));               
		    } else {
			oneFailed=true;
			result.addErrorDetails(smh.getLocalString
					       ("tests.componentNameConstructor",
						"For [ {0} ]",
						new Object[] {compName.toString()}));
			result.addErrorDetails(smh.getLocalString
					       (getClass().getName() + ".failed", 
						"Error: There is no JavaBeans compliant accessor method [ {0} ] implemented in [ {1} ] for the config-property [ {2} ]",
						new Object[] {  "public void "+ setMethodName+"("+ep.getValueType().getName()+")", 
								    mcf.getName(), 
								    ep.getName()}));                      
		    }
		    String getMethodName = "get" + propertyName;
		    m = getMethod(mcf, getMethodName, null);
		    if (m!=null) {			
			result.addGoodDetails(smh.getLocalString
					      ("tests.componentNameConstructor",
					       "For [ {0} ]",
					       new Object[] {compName.toString()}));
			result.addGoodDetails(smh.getLocalString(getClass().getName() + ".passed", 
								 "Found a JavaBeans compliant accessor method [ {0} ] for the config-property [ {1} ]",
								 new Object[] {  m, ep.getName()}));   
		    } else {
			oneFailed=true;
			result.addErrorDetails(smh.getLocalString
					       ("tests.componentNameConstructor",
						"For [ {0} ]",
						new Object[] {compName.toString()}));
			result.addErrorDetails(smh.getLocalString
					       (getClass().getName() + ".failed", 
						"Error: There is no JavaBeans compliant accessor method [ {0} ] implemented in [ {1} ] for the config-property [ {2} ]",
						new Object[] {  "public " + ep.getValueType().getName() + " " + getMethodName, 
								    mcf.getName(), 
								    ep.getName()}));                     
		    }                                
		}            
	    }
	}
        if (oneFailed) {
            result.setStatus(Result.FAILED);
        } else {
            result.setStatus(Result.PASSED);
        }
        return result;
    }
}
