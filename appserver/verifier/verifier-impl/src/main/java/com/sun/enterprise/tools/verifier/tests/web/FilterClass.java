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

package com.sun.enterprise.tools.verifier.tests.web;

import java.util.Enumeration;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;
import org.glassfish.web.deployment.descriptor.ServletFilterDescriptor;

/** 
 * Super class for all filter tests.
 * 
 * @author Jerome Dochez
 * @version 1.0
 */
public abstract class FilterClass extends WebTest implements WebCheck {

    /**
     * <p>
     * Run the verifier test against a declared individual filter class
     * </p>
     *
     * @param result is used to put the test results in
     * @param filterClass is the individual filter class object to test
     * @return true if the test pass
     */    
    abstract protected boolean runIndividualFilterTest(Result result, Class listenerClass);
    
    /** 
     * iterates over all declared filter in the archive file and 
     * delegates actual test on individual filter class to 
     * runIndividualFilterTest
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {
        
        Result result;
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        Enumeration filterEnum = descriptor.getServletFilterDescriptors().elements();
	if (filterEnum.hasMoreElements()) {
            boolean oneFailed = false;
	    // test the filters in this .war
            result = loadWarFile(descriptor);
	    while (filterEnum.hasMoreElements()) {
		ServletFilterDescriptor filter = (ServletFilterDescriptor) filterEnum.nextElement();
		Class filterClass = loadClass(result, filter.getClassName());
                                
                if (!runIndividualFilterTest(result, filterClass)) 
                    oneFailed=true;                
 	    }
	    if (oneFailed) {
		result.setStatus(Result.FAILED);
	    } else {
		result.setStatus(Result.PASSED);
	    }
	} else {
            result = getInitializedResult();
            result.setStatus(Result.NOT_APPLICABLE);
	    result.addNaDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));

	    result.notApplicable(smh.getLocalString
                ("com.sun.enterprise.tools.verifier.tests.web.FilterClass" + ".notApplicable",
		 "There are no filter components within the web archive [ {0} ]",
		 new Object[] {descriptor.getName()}));
	}

	return result;
    }
 }
