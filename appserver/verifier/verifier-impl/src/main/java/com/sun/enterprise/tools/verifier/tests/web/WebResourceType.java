/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.tools.verifier.tests.web;

import com.sun.enterprise.tools.verifier.tests.web.WebTest;
import java.util.*;
import java.util.logging.Level;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;


/** 
 * The web application resource-type element specifies the Java class type of 
 * the data source.
 */
public class WebResourceType extends WebTest implements WebCheck { 

    /**
     * The web application resource-type element specifies the Java class type of 
     * the data source.
     *
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor.getResourceReferences().hasMoreElements()) {
	    boolean oneFailed = false;
	    boolean foundIt = false;
	    // get the errorpage's in this .war
	    for (Enumeration e = descriptor.getResourceReferences() ; e.hasMoreElements() ;) {
		foundIt = false;
		ResourceReferenceDescriptor next = (ResourceReferenceDescriptor) e.nextElement();
		String resType = next.getType();
		logger.log(Level.FINE, "servlet resType: " + resType);
		if ((resType.equals("javax.sql.DataSource")) ||
		    (resType.equals("jakarta.jms.QueueConnectionFactory")) ||
		    (resType.equals("jakarta.jms.TopicConnectionFactory")) ||
		    (resType.equals("javax.mail.Session")) ||
		    (resType.equals("java.net.URL"))) { 
		    foundIt = true;
		} else {
		    foundIt = false;
                    String specVerStr = descriptor.getSpecVersion();
                    double specVer = 0;
                    specVer = (Double.valueOf(specVerStr)).doubleValue();
                    if (Double.compare(specVer, 2.4) >= 0) {
                     // with J2EE 1.4, resource-ref can be any userdefined types
                     foundIt = true;
                    }
		}
		if (foundIt) {
		    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addGoodDetails(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "The resource-type [ {0} ] element specifies the Java class type of the data source within web application [ {1} ]",
					   new Object[] {resType, descriptor.getName()}));
		} else {
		    if (!oneFailed) {
			oneFailed = true;
		    }
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addErrorDetails(smh.getLocalString
					   (getClass().getName() + ".failed",
					    "Error: The resource-type [ {0} ] element does not specify valid Java class type of the data source within web application [ {1} ]",
					    new Object[] {resType, descriptor.getName()}));
		}
	    }
	    if (oneFailed) {
		result.setStatus(Result.FAILED);
	    } else {
		result.setStatus(Result.PASSED);
	    }
	} else {
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no resource-type elements within the web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}

	return result;
    }
}
