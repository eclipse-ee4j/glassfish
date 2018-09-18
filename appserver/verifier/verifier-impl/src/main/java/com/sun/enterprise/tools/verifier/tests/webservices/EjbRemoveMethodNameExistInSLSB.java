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

package com.sun.enterprise.tools.verifier.tests.webservices;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;

import java.lang.reflect.Method;

/*
 *   @class.setup_props: ;
 */

/*
 *   @testName: check
 *   @assertion_ids:  JSR109_WS_05; 
 *   @test_Strategy:
 *   @class.testArgs: 
 *   @testDescription: Service Implementation Bean(SLSB) must implement the ejbRemove() method which take no 
 *   arguments.
 *
 *   This is a requirement of the EJB container,but generally can be stubbed out with an empty implementations
 */

public class EjbRemoveMethodNameExistInSLSB extends WSTest implements WSCheck {

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint wsdescriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
	boolean foundFailure=false;
        if (wsdescriptor.implementedByEjbComponent()) {
            EjbDescriptor ejbdesc = wsdescriptor.getEjbComponentImpl();
            if (ejbdesc != null && (ejbdesc instanceof EjbSessionDescriptor)) {
                EjbSessionDescriptor descriptor = (EjbSessionDescriptor)ejbdesc;
                if (EjbSessionDescriptor.STATELESS.equals(descriptor.getSessionType())) {
                    try {
                        //VerifierTestContext context = getVerifierContext();
                        ClassLoader jcl = getVerifierContext().getClassLoader();
                        Class c = Class.forName(descriptor.getEjbClassName(), false, getVerifierContext().getClassLoader());
                        int foundAtLeastOne = 0;

                        do {
                            Method [] methods = c.getDeclaredMethods();
                            for (int i = 0; i < methods.length; i++) {
                                // The method name must be ejbRemove.
                                if (methods[i].getName().startsWith("ejbRemove")) {
                                    foundAtLeastOne++;
                            		result.addGoodDetails(smh.getLocalString
                                            ("tests.componentNameConstructor",
                                                    "For [ {0} ]",
                                                    new Object[] {compName.toString()}));
                                    result.addGoodDetails(smh.getLocalString
                                            (getClass().getName() + ".passed",
                                                    "[ {0} ] declares [ {1} ] method.",
                                                    new Object[] {descriptor.getEjbClassName(),methods[i].getName()}));
                                }
                            }
                        } while (((c = c.getSuperclass()) != null) && (foundAtLeastOne == 0));
                        if (foundAtLeastOne == 0){
                            foundFailure = true;
                            result.addErrorDetails(smh.getLocalString
                                    ("tests.componentNameConstructor",
                                            "For [ {0} ]",
                                            new Object[] {compName.toString()}));
                            result.failed(smh.getLocalString
                                    (getClass().getName() + ".failed",
                                            "Error: [ {0} ] does not properly declare at least one ejbRemove() method.  [ {1} ] is not a valid bean.",
                                            new Object[] {descriptor.getEjbClassName(),descriptor.getEjbClassName()}));
                        }
                    } catch (ClassNotFoundException e) {
                        Verifier.debug(e);
                        result.addErrorDetails(smh.getLocalString
                                ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
                        result.failed(smh.getLocalString
                                (getClass().getName() + ".failedException",
                                        "Error: [ {0} ] class not found.",
                                        new Object[] {descriptor.getEjbClassName()}));
                        return result;
                    }
                } else {
                    result.addNaDetails(smh.getLocalString
                            ("tests.componentNameConstructor", "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                    result.notApplicable(smh.getLocalString
                            (getClass().getName() + ".notApplicable",
                                    "NOT APPLICABLE :Service Implementation bean is not a stateless Session Bean"));
                    return result;
                }
            } else {
                result.addNaDetails(smh.getLocalString
                        ("tests.componentNameConstructor",
                                "For [ {0} ]",
                                new Object[] {compName.toString()}));
                result.notApplicable(smh.getLocalString
                        (getClass().getName() + ".notApplicable1",
                                "NOT APPLICABLE:Service Implementation bean is null or not a session bean descriptor "));
                return result;
            }

            if (foundFailure) {
                result.setStatus(result.FAILED);
            } else {
                result.setStatus(result.PASSED);
            }
            return result;

        } else {
            result.addNaDetails(smh.getLocalString
                    ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName.toString()}));
            result.notApplicable(smh.getLocalString
                    (getClass().getName() + ".notApplicable2",
                            "Not Applicable: Service Implementation bean is not implemented by Ejb."));
            return result;
        }
    }
}
