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

package com.sun.enterprise.tools.verifier.tests.appclient.elements;

import com.sun.enterprise.tools.verifier.tests.appclient.AppClientTest;
import com.sun.enterprise.tools.verifier.tests.appclient.AppClientCheck;
import java.util.*;
import java.util.logging.Level;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;


/**
 * The application client resource-type element specifies the Java class type
 * of the data source.
 */
public class AppClientResourceType extends AppClientTest implements AppClientCheck {

    /**
     * The application client resource-type element specifies the Java class type
     * of the data source.
     *
     * @param descriptor the Application client deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(ApplicationClientDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (!descriptor.getResourceReferenceDescriptors().isEmpty()) {
	    boolean oneFailed = false;
	    boolean foundIt = false;
	    Set resources = descriptor.getResourceReferenceDescriptors();
	    Iterator itr = resources.iterator();
	    // get the res-ref's in this .ear
	    while(itr.hasNext()) {
		foundIt = false;
		ResourceReferenceDescriptor next = (ResourceReferenceDescriptor) itr.next();
		String resType = next.getType();
                logger.log(Level.FINE, "servlet resType: " + resType);
		if ((resType.equals("javax.sql.DataSource")) ||
		    (resType.equals("jakarta.jms.QueueConnectionFactory")) ||
		    (resType.equals("jakarta.jms.TopicConnectionFactory")) ||
		    (resType.equals("jakarta.jms.ConnectionFactory")) ||
		    (resType.equals("jakarta.mail.Session")) ||
		    (resType.equals("java.net.URL"))) {
		    foundIt = true;
		} else {
		    foundIt = false;
                    String specVerStr = descriptor.getSpecVersion();
                    double specVer = 0;
                    specVer = (Double.valueOf(specVerStr)).doubleValue();
                    if (Double.compare(specVer, 1.4) >= 0) {
                      // with J2EE 1.4, resource-ref can be any userdefined type
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
					   "The resource-type [ {0} ] element specifies the Java class type of the data source within application client [ {1} ]",
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
					    "Error: The resource-type [ {0} ] element does not specify valid Java class type of the data source within application client [ {1} ]",
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
				  "There are no resource-type elements within the application client [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}

	return result;
    }
}
