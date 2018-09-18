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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.ejbfindermethod;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/** 
 * Note that the ejbFind<METHOD> names and parameter signatures do not 
 * provide the container tools with sufficient information for automatically 
 * generating the implementation of the finder methods for methods other than 
 * ejbFindByPrimaryKey. Therefore, the bean provider is responsible for 
 * providing a description of each finder method. The entity bean Deployer 
 * uses container tools to generate the implementation of the finder methods 
 * based in the description supplied by the bean provider. The Enterprise 
 * JavaBeans architecture does not specify the format of the finder method 
 * description.
 */
public class EjbFinderMethodDescription extends EjbTest implements EjbCheck { 


    /**
     * Note that the ejbFind<METHOD> names and parameter signatures do not 
     * provide the container tools with sufficient information for automatically 
     * generating the implementation of the finder methods for methods other than 
     * ejbFindByPrimaryKey. Therefore, the bean provider is responsible for 
     * providing a description of each finder method. The entity bean Deployer 
     * uses container tools to generate the implementation of the finder methods 
     * based in the description supplied by the bean provider. The Enterprise 
     * JavaBeans architecture does not specify the format of the finder method 
     * description.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();

	// Stub test class placeholder
	// fill in guts/logic - pass/fail accordingly in future
	result.setStatus(Result.NOT_IMPLEMENTED);
	result.addNaDetails
	    (smh.getLocalString
	     (getClass().getName() + ".notImplemented",
	      "No static testing done - yet."));
	return result;
    }
}
