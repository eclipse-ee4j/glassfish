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

package com.sun.enterprise.tools.verifier.tests.appclient.elements;

import com.sun.enterprise.tools.verifier.tests.appclient.AppClientTest;
import com.sun.enterprise.tools.verifier.tests.appclient.AppClientCheck;
import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

/** 
 * The Bean Provider must declare all enterprise bean's references to the homes
 * of other enterprise beans as specified in section 14.3.2 of the Moscone spec.
 * Check for one within the same jar file, can't check outside of jar file.
 * Load/locate & check other bean's home/remote/bean, ensure they match with 
 * what the linking bean says they should be; check for pair of referencing and 
 * referenced beans exist.
 */
public class AppClientEjbReferencesElement extends AppClientTest implements AppClientCheck { 


    /** 
     * The Bean Provider must declare all enterprise bean's references to the homes
     * of other enterprise beans as specified in section 14.3.2 of the Moscone spec. 
     * Check for one within the same jar file, can't check outside of jar file.
     * Load/locate & check other bean's home/remote/bean, ensure they match with
     * what the linking bean says they should be; check for pair of referencing and
     * referenced beans exist.
     *
     * @param descriptor the Application client deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(ApplicationClientDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor instanceof ApplicationClientDescriptor) {	  
	    // RULE: References to other beans must be declared in the form of 
	    //       references to other beans homes as specified in section 
	    //       14.3.2 of the Moscone spec.

	    // check for one bean within the same jar file; can't check outside of 
	    // jar file.  need to load/locate and check other beans remote, home, bean
	    // match with the linking bean says they should be. i.e. check for pair
	    // of referencing & referenced bean exist, using reflection API
      
	    EjbReferenceDescriptor ejbReference;
	    Set references = descriptor.getEjbReferenceDescriptors();
	    Iterator iterator = references.iterator();
      
	    if (iterator.hasNext()) {
            boolean oneFailed = false;
		while (iterator.hasNext()) {
		    ejbReference = (EjbReferenceDescriptor) iterator.next();

            String homeClassName = ejbReference.getHomeClassName();
            if(homeClassName == null) {
                oneFailed = true;
                result.addErrorDetails(smh.getLocalString
                                        ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
                result.failed(smh.getLocalString
                                      (getClass().getName() + ".failed1",
                                       "Error: {0} class cannot be null.",
                                       new Object[] {"home"}));
            } else {
                try {
                    Class.forName(homeClassName, false, getVerifierContext().getClassLoader());
                } catch (ClassNotFoundException e) {
                    Verifier.debug(e);
                    oneFailed = true;
                    result.addErrorDetails(smh.getLocalString
                               ("tests.componentNameConstructor",
                                "For [ {0} ]",
                                new Object[] {compName.toString()}));
                    result.failed(smh.getLocalString
                          (getClass().getName() + ".failed",
                           "Error: [ {0} ] class [ {1} ] cannot be found within this jar [ {2} ].",
                           new Object[] {ejbReference.getName(), homeClassName, 
                                         descriptor.getModuleDescriptor().getArchiveUri()}));
                }
                result.addGoodDetails(smh.getLocalString
                                        ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
                result.passed(smh.getLocalString
                    (getClass().getName() + ".passed2",
                    "The referenced bean's home interface [ {0} ] exists and is loadable within [ {1} ].",
                    new Object[] {ejbReference.getHomeClassName(), 
                                  descriptor.getModuleDescriptor().getArchiveUri()}));
            }
            
            String remoteClassName = ejbReference.getEjbInterface();
            if(remoteClassName == null) {
                oneFailed = true;
                result.addErrorDetails(smh.getLocalString
                        ("tests.componentNameConstructor",
                        "For [ {0} ]",
                        new Object[] {compName.toString()}));
                result.failed(smh.getLocalString
                        (getClass().getName() + ".failed1",
                       "Error: {0} class cannot be null.",
                        new Object[] {"remote"}));
            } else {
                try {
                    Class.forName(remoteClassName, false, getVerifierContext().getClassLoader());
                } catch (ClassNotFoundException e) {
                    Verifier.debug(e);
                    oneFailed = true;
                    result.addErrorDetails(smh.getLocalString
                               ("tests.componentNameConstructor",
                                "For [ {0} ]",
                                new Object[] {compName.toString()}));
                    result.failed(smh.getLocalString
                          (getClass().getName() + ".failed",
                           "Error: [ {0} ] class [ {1} ] cannot be found within this jar [ {2} ].",
                           new Object[] {ejbReference.getName(), remoteClassName, 
                                         descriptor.getModuleDescriptor().getArchiveUri()}));
                }
                result.addGoodDetails(smh.getLocalString
                        ("tests.componentNameConstructor",
                        "For [ {0} ]",
                        new Object[] {compName.toString()}));
                result.passed(smh.getLocalString
                        (getClass().getName() + ".passed3",
                        "The referenced bean's remote interface [ {0} ] exists and is loadable within [ {1} ].",
                        new Object[] {ejbReference.getEjbInterface(), 
                                      descriptor.getModuleDescriptor().getArchiveUri()}));
            }
		}
        if (oneFailed) {
            result.setStatus(result.FAILED);
        } else {
            result.setStatus(result.PASSED);
        }
	    } else {
            result.addNaDetails(smh.getLocalString
                           ("tests.componentNameConstructor",
                        "For [ {0} ]",
                        new Object[] {compName.toString()}));
            result.notApplicable(smh.getLocalString
                         (getClass().getName() + ".notApplicable1",
                          "There are no ejb references to other beans within this application client [ {0} ]",
                          new Object[] {descriptor.getName()}));
	    }
 
	    return result;
	} else {
	    result.addNaDetails(smh.getLocalString
                    ("tests.componentNameConstructor",
                    "For [ {0} ]",
                    new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
                    (getClass().getName() + ".notApplicable",
                    "[ {0} ] not called with a application client.",
                    new Object[] {getClass()}));
	    return result;
	}    
    }
}    


