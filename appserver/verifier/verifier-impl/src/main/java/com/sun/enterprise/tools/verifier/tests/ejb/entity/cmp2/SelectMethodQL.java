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
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.QueryDescriptor;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

/**
 * Select methods must be associated with an EJB QL query which includes a 
 * SELECT clause
 *
 * @author  Jerome Dochez
 * @version 
 */
public class SelectMethodQL extends SelectMethodTest {

    /**
     * <p>
     * run an individual test against a declared ejbSelect method
     * </p>
     * 
     * @param m is the ejbSelect method
     * @param descriptor is the entity declaring the ejbSelect
     * @param result is where to put the result
     * 
     * @return true if the test passes
     */
    protected boolean runIndividualSelectTest(Method m, EjbCMPEntityDescriptor descriptor, Result result) {
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        // We don't use getQueryFor to free ourselfves from classloader issues.
        Set set = descriptor.getPersistenceDescriptor().getQueriedMethods();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            MethodDescriptor queryMethod = (MethodDescriptor) iterator.next();
            if (queryMethod.getName().equals(m.getName())) {
                Class mParms[] = m.getParameterTypes();
	
                String queryParms[] = queryMethod.getParameterClassNames();
	
		if (queryParms != null) {
	
                if (queryParms.length == mParms.length) {
                    boolean same = true;
                    for (int i=0;i<mParms.length;i++) {
                        if (!mParms[i].getName().equals(queryParms[i]))
                            same=false;                    
                    }
                    if (same) {
                        QueryDescriptor qd = descriptor.getPersistenceDescriptor().getQueryFor(queryMethod);
                        String query = qd.getQuery();
                        if (query == null && qd.getSQL()==null) {
			    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                            result.addErrorDetails(smh.getLocalString
                                ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.SelectMethodQL.failed2",
                                "Error : [ {0} ] EJB-QL query and description are null",
		                new Object[] {m.getName()}));                                                    
                            return false;
                        } else {
                            if (query==null) {
				result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                                result.addGoodDetails(smh.getLocalString
            		            ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.SelectMethodQL.passed1",
                                    "Description for [ {0} ] is provided",
        		            new Object[] {m.getName()}));                                                       
                                return true;
                            }                                
                            if (query.toUpperCase().indexOf("SELECT")==-1) {
				result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                                result.addErrorDetails(smh.getLocalString
                                    ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.SelectMethodQL.failed2",
                                    "Error : EJB-QL query for method [ {0}  is null",
		                    new Object[] {m.getName()}));                                                    
                                return false;
                            } else {
				result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                                result.addGoodDetails(smh.getLocalString
            		            ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.SelectMethodQL.passed2",
                                    "EJB-QL query for [ {0} ] is correct",
        		            new Object[] {m.getName()}));                                                       
                                return true;
                            }
                        }                        
                    }
                }
		}
		else if (mParms.length == 0) {
	
		    result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
		    result.addGoodDetails(smh.getLocalString
					  ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.SelectMethodQL.passed3",
					   "No EJB-QL query found",
					   new Object[] {}));                                                       
		    return true;
		} else {
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.addErrorDetails(smh.getLocalString
					   ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.SelectMethodQL.failed2",
					    "Error : EJB-QL query for method [ {0}  is null",
					    new Object[] {m.getName()}));                                                    
		    return false;
		}
            }	    	    
        }
	
	result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
        result.addErrorDetails(smh.getLocalString
	    ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.SelectMethodQL.failed1",
            "Error : [ {0} ] does not have a XML query element associated",
	    new Object[] {m.getName()}));                                                    
        return false;            
    }
}
