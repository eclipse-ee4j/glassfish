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

package com.sun.enterprise.tools.verifier.tests.ejb.session;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * The Bean Provider must assume that the content of transient fields may be 
 * lost between the ejbPassivate and ejbActivate notifications. Therefore, the
 * Bean Provider should not store in a transient field a reference to any of 
 * the following objects: SessionContext object; environment JNDI naming 
 * context and any its subcontexts; home and remote interfaces; and the 
 * UserTransaction interface. The restrictions on the use of transient fields 
 * ensure that Containers can use Java Serialization during passivation and 
 * activation.
 */
public class TransientFieldsSerialization extends EjbTest implements EjbCheck {



    /**
     * The Bean Provider must assume that the content of transient fields may be 
     * lost between the ejbPassivate and ejbActivate notifications. Therefore, the
     * Bean Provider should not store in a transient field a reference to any of 
     * the following objects: SessionContext object; environment JNDI naming 
     * context and any its subcontexts; home and remote interfaces; and the 
     * UserTransaction interface. The restrictions on the use of transient fields 
     * ensure that Containers can use Java Serialization during passivation and 
     * activation.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean isEjb30 = descriptor.getEjbBundleDescriptor()
                              .getSpecVersion().equalsIgnoreCase("3.0");

        if (descriptor instanceof EjbSessionDescriptor) {
            try {
                Class c = Class.forName(((EjbSessionDescriptor)descriptor).getEjbClassName(), false,
                                   getVerifierContext().getClassLoader());
                //  Bean Provider should not store in a transient field a reference to
                // any of the following objects: SessionContext object; environment
                // JNDI naming context and any its subcontexts; home and remote
                // interfaces; and the UserTransaction interface.
                Field [] fields = c.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    int modifiers = fields[i].getModifiers();
                    if (!Modifier.isTransient(modifiers)) {
                        continue;
                    } else {
                        Class fc = fields[i].getType();
                        // can't do anything with environment JNDI naming context and
                        // any its subcontexts
                        //sg133765: do we need to do something for business interface
                        if ((fc.getName().equals("jakarta.ejb.SessionContext")) ||
                                (fc.getName().equals("jakarta.transaction.UserTransaction")) ||
                                (fc.getName().equals(descriptor.getRemoteClassName())) ||
                                (fc.getName().equals(descriptor.getHomeClassName()))||
                                (fc.getName().equals(descriptor.getLocalClassName())) ||
                                (fc.getName().equals(descriptor.getLocalHomeClassName())) ||
                                (isEjb30 && fc.getName().equals("jakarta.ejb.EntityManager")) ||
                                (isEjb30 && fc.getName().equals("jakarta.ejb.EntityManagerFactory"))) {

                            result.failed(smh.getLocalString
                                    ("tests.componentNameConstructor",
                                            "For [ {0} ]",
                                            new Object[] {compName.toString()}));
                            result.addErrorDetails(smh.getLocalString
                                    (getClass().getName() + ".failed",
                                    "Error: Field [ {0} ] defined within" +
                                    " session bean class [ {1} ] is defined as transient. " +
                                    "Session bean fields should not store in a " +
                                    "transient field a reference to any of the following objects: " +
                                    "SessionContext object; environment JNDI naming context and any " +
                                    "its subcontexts; home and remote interfaces;" +
                                    " and the UserTransaction interface.",
                                            new Object[] {fields[i].getName(),
                                            ((EjbSessionDescriptor)descriptor).getEjbClassName()}));
                        }
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
                                "Error: [ {0} ] class not found.",
                                new Object[] {((EjbSessionDescriptor)descriptor).getEjbClassName()}));
            }
        }
        if(result.getStatus()!=Result.FAILED) {
            addGoodDetails(result, compName);
		    result.passed(smh.getLocalString
				  (getClass().getName() + ".passed",
				   "This session bean class has not stored in a " +
                    "transient field a reference to any of the following objects: " +
                    "SessionContext object; environment JNDI naming context and" +
                    " any its subcontexts; home and remote interfaces; and the " +
                    "UserTransaction interface."));
        }
        return result;
    }
}


