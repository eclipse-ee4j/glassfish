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
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.web.AppListenerDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.*;
/** 
 * Super class for all Listener tests.
 * 
 * @author Jerome Dochez
 * @version 1.0
 */
public abstract class ListenerClass extends WebTest implements WebCheck {
    
    /**
     * <p>
     * Run the verifier test against a declared individual listener class
     * </p>
     *
     * @param result is used to put the test results in
     * @param listenerClass is the individual listener class object to test
     * @return true if the test pass
     */    
    abstract protected boolean runIndividualListenerTest(Result result, Class listenerClass);
    
    /** 
     * Listener class must implement a no arg constructor.
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {
        
        AppListenerDescriptor listener = null;
        Enumeration listenerEnum;
        Result result;
        boolean oneFailed = false;
        Class listenerClass = null;   
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
	
	listenerEnum = descriptor.getAppListenerDescriptors().elements();
	if (listenerEnum.hasMoreElements()) {
            result = loadWarFile(descriptor);
            while (listenerEnum.hasMoreElements()) {
		listener = (AppListenerDescriptor)listenerEnum.nextElement();

                if (listener.getListener().equals(smh.getLocalString("JAXRPCContextListener","com.sun.xml.rpc.server.http.JAXRPCContextListener"))) {
	            result.addGoodDetails(smh.getLocalString
				("tests.componentNameConstructor",
				 "For [ {0} ]",
				 new Object[] {compName.toString()}));
                    result.passed(smh.getLocalString (getClass().getName() + ".passed1",
                    "Listener Class Name is [ {0} ], make sure it is available in classpath at runtime.", 
		       new Object[] {listener.getListener()}));
                  continue;
                }

                if ("".equals(listener.getListener())) {
	          result.addErrorDetails(smh.getLocalString
				("tests.componentNameConstructor",
				 "For [ {0} ]",
				 new Object[] {compName.toString()}));
                  result.failed(smh.getLocalString (getClass().getName() + ".failed",
                    "Empty or Null String specified for Listener Class Name in [ {0} ].", 
		       new Object[] {compName.toString()}));
                  oneFailed = true;
                  continue;
                }

                listenerClass = loadClass(result, listener.getListener());                
                if (!runIndividualListenerTest(result, listenerClass)) 
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
                ("com.sun.enterprise.tools.verifier.tests.web.ListenerClass" + ".notApplicable",
		 "There are no listener components within the web archive [ {0} ]",
		 new Object[] {descriptor.getName()}));
	}

	return result;
    }
}
