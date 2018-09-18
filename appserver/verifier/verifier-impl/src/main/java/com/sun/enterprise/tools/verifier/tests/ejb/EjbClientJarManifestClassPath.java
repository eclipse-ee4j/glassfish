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

package com.sun.enterprise.tools.verifier.tests.ejb;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/** 
 * The EJB specification does not specify whether the ejb-jar file should 
 * include the classes that are in the ejb-client JAR by copy or by reference. 
 * If the by-copy approach is used, the producer simply includes all the class 
 * files in the ejb-client JAR file also in the ejb-jar file. If the 
 * by-reference approach is used, the ejb-jar file producer does not duplicate 
 * the content of the ejb-client JAR file in the ejb-jar file, but instead uses
 * a Manifest Class-Path entry in the ejb-jar file to specify that the ejb-jar 
 * file depends on the ejb-client JAR at runtime.
 */
public class EjbClientJarManifestClassPath extends EjbTest implements EjbCheck { 




    /** 
     * The EJB specification does not specify whether the ejb-jar file should 
     * include the classes that are in the ejb-client JAR by copy or by reference. 
     * If the by-copy approach is used, the producer simply includes all the class 
     * files in the ejb-client JAR file also in the ejb-jar file. If the 
     * by-reference approach is used, the ejb-jar file producer does not duplicate 
     * the content of the ejb-client JAR file in the ejb-jar file, but instead uses
     * a Manifest Class-Path entry in the ejb-jar file to specify that the ejb-jar 
     * file depends on the ejb-client JAR at runtime.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	// Stub test class placeholder
	// fill in guts/logic - pass/fail accordingly in future
	// once DOL returns proper value
	result.setStatus(Result.NOT_IMPLEMENTED);
	result.addNaDetails(smh.getLocalString
			    ("tests.componentNameConstructor",
			     "For [ {0} ]",
			     new Object[] {compName.toString()}));
	result.addNaDetails
	    (smh.getLocalString
	     (getClass().getName() + ".notImplemented",
	      "No static testing done - yet."));
	return result;

    }
}
