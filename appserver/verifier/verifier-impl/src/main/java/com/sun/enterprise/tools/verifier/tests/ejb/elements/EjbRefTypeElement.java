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

package com.sun.enterprise.tools.verifier.tests.ejb.elements;

import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.XpathPrefixResolver;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.util.Iterator;

/** 
 * The ejb-ref-type element must be one of the following:
 *   Entity
 *   Session
 */
public class EjbRefTypeElement extends EjbTest implements EjbCheck { 

    // Logger to log messages
    /**
     * The ejb-ref-type element must be one of the following:
     *   Entity
     *   Session
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
	boolean failed = false;

	//The ejb-ref-type element must be one of the following:
	// Entity
	// Session
	if (!descriptor.getEjbReferenceDescriptors().isEmpty()) {
	    for (Iterator itr = descriptor.getEjbReferenceDescriptors()
                .iterator(); itr.hasNext();) {
		EjbReferenceDescriptor nextEjbReference = 
                (EjbReferenceDescriptor) itr.next();

                // Need to use XPath, because if DOL sees an inconsistent
                // ref-type, it gives a warning and changes the type
		//String ejbRefTypeStr = nextEjbReference.getType();
                String refStr = (nextEjbReference.isLocal()) ? 
                                "ejb-local-ref" : "ejb-ref";
                String beanType = (descriptor.getType()).toLowerCase();
                String xpathQuery = null;
                if (getVerifierContext().getDocument().getDoctype() != null) {
                   xpathQuery = "/ejb-jar/enterprise-beans/" +
                   beanType + "[ejb-name=\"" + descriptor.getName() 
                   +"\"]/" + refStr + "[ejb-ref-name=\"" 
                   + nextEjbReference.getName() + "\"]/ejb-ref-type";
                }
                else {
                   String prefix = XpathPrefixResolver.fakeXPrefix;
                   xpathQuery = prefix + ":" + "ejb-jar/" + 
                    prefix + ":" + "enterprise-beans/" +
                    prefix + ":" + beanType + 
                    "[" + prefix + ":ejb-name=\"" + descriptor.getName() 
                    +"\"]/" + 
                    prefix + ":" + refStr + "[" + prefix + ":ejb-ref-name=\"" 
                    + nextEjbReference.getName() + "\"]/" +
                    prefix + ":" + "ejb-ref-type";
                }
           
		String ejbRefTypeStr = getXPathValueForNonRuntime(xpathQuery);
                EjbDescriptor rdesc = (EjbDescriptor) nextEjbReference.getEjbDescriptor();

                /*if (rdesc == null) {
                   logger.log(Level.SEVERE, getClass().getName() + ".Warn",
                   new Object[] {nextEjbReference.getBeanClassName()});
                }*/

                // XPath queries seem to fail for XSD Descriptors
                if (ejbRefTypeStr == null) {
                   ejbRefTypeStr = nextEjbReference.getType();
                } 

	        if (!((ejbRefTypeStr.equals(EjbSessionDescriptor.TYPE)) ||
		        (ejbRefTypeStr.equals(EjbEntityDescriptor.TYPE)))) {
		      result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		      result.failed(smh.getLocalString
				  (getClass().getName() + ".failed",
				   "Error: ejb-ref-type [ {0} ] within \n bean [ {1} ] is not valid.  \n Must be [ {2} ] or [ {3} ]",
				   new Object[] {ejbRefTypeStr,descriptor.getName(),EjbEntityDescriptor.TYPE,EjbSessionDescriptor.TYPE}));
		      failed = true;
		  }
                  else if (rdesc != null ) {
                    String actualRefType = rdesc.getType();
                    if (!ejbRefTypeStr.equals(actualRefType)) {
                       result.addErrorDetails(smh.getLocalString
                                           ("tests.componentNameConstructor",
                                            "For [ {0} ]",
                                            new Object[] {compName.toString()}));
                        result.failed(smh.getLocalString
                           (getClass().getName() + ".failed2",
                            "Error: ejb-ref-type [ {0} ] was specifed for ejb-ref [ {1} ], within bean [ {2} ], when it should have been [ {3} ].",
                            new Object[] {ejbRefTypeStr, 
                            nextEjbReference.getName(), 
                            descriptor.getName(), actualRefType}));
                    failed = true;
                  }
	      }
            }
	} else {
	    result.addNaDetails(smh.getLocalString
				("tests.componentNameConstructor",
				 "For [ {0} ]",
				 new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no ejb references to other beans within this bean [ {0} ]",
				  new Object[] {descriptor.getName()}));
	    return result;
	}

	if (failed)
	    {
		result.setStatus(Result.FAILED);
	    } else {
		result.addGoodDetails(smh.getLocalString
				      ("tests.componentNameConstructor",
				       "For [ {0} ]",
				       new Object[] {compName.toString()}));
		result.passed
		    (smh.getLocalString
		     (getClass().getName() + ".passed",
		      "All ejb-ref-type elements are valid.  They are all [ {0} ] or [ {1} ] within this bean [ {2} ]",
		      new Object[] {EjbEntityDescriptor.TYPE,EjbSessionDescriptor.TYPE,descriptor.getName()}));
	    } 
	return result;

    }
}
