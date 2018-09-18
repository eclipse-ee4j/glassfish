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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.ejbfindbyprimarykey;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.lang.reflect.Method;

/**
 * Define ejbFindByPrimaryKey method name test.
 *
 *     Every entity enterprise Bean class must define the ejbFindByPrimaryKey
 *     method. The return type for this method must be the primary key type.
 *     (i.e. the ejbFindByPrimaryKey method must be a single-object finder).
 *
 */
public class EjbFindByPrimaryKeyName extends EjbTest implements EjbCheck {


    /**
     * Define ejbFindByPrimaryKey method name test.
     *
     *     Every entity enterprise Bean class must define the ejbFindByPrimaryKey
     *     method. The return type for this method must be the primary key type.
     *     (i.e. the ejbFindByPrimaryKey method must be a single-object finder).
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor instanceof EjbEntityDescriptor) {
	    String persistence = ((EjbEntityDescriptor)descriptor).getPersistenceType();
    	boolean ejbFindByPrimaryKeyMethodFound = false;
		boolean oneFailed = false;
		try {
		    // retrieve the EJB Class Methods
		    VerifierTestContext context = getVerifierContext();
            ClassLoader jcl = context.getClassLoader();
		    Class EJBClass = Class.forName(descriptor.getEjbClassName(), false, getVerifierContext().getClassLoader());
            // start do while loop here....
            do {
                Method [] ejbFinderMethods = EJBClass.getDeclaredMethods();
                for (int j = 0; j < ejbFinderMethods.length; ++j) {
                    if (ejbFinderMethods[j].getName().equals("ejbFindByPrimaryKey")) {
                        // Every entity enterprise Bean class must define the
                        // ejbFindByPrimaryKey method. The result type for this method must
                        // be the primary key type (i.e. the ejbFindByPrimaryKey method
                        // must be a single-object finder).
                        ejbFindByPrimaryKeyMethodFound = true;
                        result.addGoodDetails(smh.getLocalString
                                ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
                        result.addGoodDetails(smh.getLocalString
                                (getClass().getName() + ".debug1",
                                        "For EJB Class [ {0} ] Finder Method [ {1} ]",
                                        new Object[] {EJBClass.getName(),ejbFinderMethods[j].getName()}));
                        result.addGoodDetails(smh.getLocalString
                                (getClass().getName() + ".passed",
                                        "An [ {0} ] method was found.",
                                        new Object[] {ejbFinderMethods[j].getName()}));
                        break;
                    }
                }
            } while (((EJBClass = EJBClass.getSuperclass()) != null) && (!ejbFindByPrimaryKeyMethodFound));
            if (EjbEntityDescriptor.BEAN_PERSISTENCE.equals(persistence)&&ejbFindByPrimaryKeyMethodFound) {
                result.setStatus(result.PASSED);
                return result;
            } else {
                if(EjbEntityDescriptor.BEAN_PERSISTENCE.equals(persistence) && !ejbFindByPrimaryKeyMethodFound) {
                    oneFailed = true;
                    result.addErrorDetails(smh.getLocalString
                            ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                    result.addErrorDetails(smh.getLocalString
                            (getClass().getName() + ".debug3",
                                    "For EJB Class [ {0} ]",
                                    new Object[] {descriptor.getEjbClassName()}));
                    result.addErrorDetails(smh.getLocalString
                            (getClass().getName() + ".failed",
                                    "Error: No ejbFindByPrimaryKey method was found in bean class."));
                } else if(EjbEntityDescriptor.CONTAINER_PERSISTENCE.equals(persistence) && ejbFindByPrimaryKeyMethodFound) {
                    oneFailed = true;
                    result.addErrorDetails(smh.getLocalString
                            ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                    result.addErrorDetails(smh.getLocalString
                            (getClass().getName() + ".debug3",
                                    "For EJB Class [ {0} ]",
                                    new Object[] {descriptor.getEjbClassName()}));
                    result.addErrorDetails(smh.getLocalString
                            (getClass().getName() + ".failed1",
                                    "Error:  ejbFindByPrimaryKey method was found for CMP bean class."));
                } else {//(CONTAINER_PERSISTENCE.equals(persistence))
                    result.addNaDetails(smh.getLocalString
                            ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                    result.notApplicable(smh.getLocalString
                            (getClass().getName() + ".notApplicable2",
                                    "Expected persistence type [ {0} ], but bean [ {1} ] has persistence type [ {2} ]",
                                    new Object[] {EjbEntityDescriptor.BEAN_PERSISTENCE,descriptor.getName(),persistence}));
                    return result;
                }
            }
        } catch (ClassNotFoundException e) {
            Verifier.debug(e);
            result.addErrorDetails(smh.getLocalString
                    ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName.toString()}));
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failedException",
                            "Error: EJB Class [ {0} ] does not exist or is not loadable.",
                            new Object[] {descriptor.getEjbClassName()}));
            oneFailed = true;
        }
        if (oneFailed) {
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
                (getClass().getName() + ".notApplicable",
                        "[ {0} ] expected {1} bean, but called with {2} bean.",
                        new Object[] {getClass(),"Entity","Session"}));
        return result;
    }
    }
}
