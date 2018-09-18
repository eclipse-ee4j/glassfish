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

package com.sun.enterprise.tools.verifier.tests.ejb.entity;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.util.Iterator;
import java.util.Vector;

/** 
 * The abstract schema name for every CMP bean within a jar file should be unique.
 *
 * @author Sheetal Vartak
 *
 */
public class UniqueAbstractSchemaName extends EjbTest implements EjbCheck { 


    /** 
     * The abstract schema name for every CMP bean within a jar file should be unique.
     *
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
	boolean oneFailed = false;
	String abstractSchema = null;

	if (descriptor instanceof EjbEntityDescriptor) {
	    if (((EjbEntityDescriptor)descriptor).getPersistenceType().equals(EjbEntityDescriptor.CONTAINER_PERSISTENCE)) {                
                
                if (((EjbCMPEntityDescriptor) descriptor).getCMPVersion()==EjbCMPEntityDescriptor.CMP_2_x) {
                    abstractSchema = ((EjbCMPEntityDescriptor)descriptor).getAbstractSchemaName();
                    if (abstractSchema==null) {
                        result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
                        result.failed(smh.getLocalString
                                        (getClass().getName() + ".failed2",
                                        "No Abstract Schema Name specified for a CMP 2.0 Entity Bean {0} ",
                                        new Object[] {descriptor.getName()}));                          
                        return result;
                    }
                }
            }
            if (abstractSchema ==null) {
                result.addNaDetails(smh.getLocalString
                    ("tests.componentNameConstructor",
                    "For [ {0} ]",
                    new Object[] {compName.toString()}));
                result.notApplicable(smh.getLocalString
                    (getClass().getName() + ".notApplicable",
                    "This test is only for CMP 2.0 beans. Abstract Schema Names should be unique within an ejb JAR file."));
                    return result;
	    }

	    EjbBundleDescriptorImpl bundle = descriptor.getEjbBundleDescriptor();
	    Iterator iterator = (bundle.getEjbs()).iterator();
	    Vector<String> schemaNames = new Vector<String>();
	    while(iterator.hasNext()) {
		EjbDescriptor entity = (EjbDescriptor) iterator.next();
		if (entity instanceof EjbEntityDescriptor) { 
		    if (!entity.equals(descriptor)) {
			if (((EjbEntityDescriptor)entity).getPersistenceType().equals(EjbEntityDescriptor.CONTAINER_PERSISTENCE)) {
			    schemaNames.addElement(((EjbCMPEntityDescriptor)entity).getAbstractSchemaName());
			} 
		    }
		}
	    }

	    for (int i = 0; i < schemaNames.size(); i++) {
		if (abstractSchema.equals(schemaNames.elementAt(i))) {
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.addErrorDetails
			(smh.getLocalString
			 (getClass().getName() + ".failed",
			  "Abstract Schema Names should be unique within an ejb JAR file. Abstract Schema Name [ {0} ] is not unique.",
			  new Object[] {abstractSchema}));
		    oneFailed = true;
		}
	    }
	    if (oneFailed == false) {
		result.addGoodDetails(smh.getLocalString
				      ("tests.componentNameConstructor",
				       "For [ {0} ]",
				       new Object[] {compName.toString()}));
		result.passed
		(smh.getLocalString
		 (getClass().getName() + ".passed",
		  "PASSED : Abstract Schema Names for all beans within the ejb JAR file are unique."));
	    }
	    else result.setStatus(Result.FAILED);
	    
	} else {
        addNaDetails(result, compName);        
        result.notApplicable(smh.getLocalString
            (getClass().getName() + ".notApplicable",
            "This test is only for CMP 2.0 beans. Abstract Schema Names should be unique within an ejb JAR file."));
    }
    return result;
    }
}
