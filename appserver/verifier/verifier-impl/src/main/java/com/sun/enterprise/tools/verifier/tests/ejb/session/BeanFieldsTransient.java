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
 *  The Bean Provider should not declare the session bean fields in the session
 *  bean class as transient.
 *
 *  This is to allow the Container to swap out an instance's state through 
 *  techniques other than the Java Serialization protocol. For example, the 
 *  Container's Java Virtual Machine implementation may use a block of memory 
 *  to keep the instance's variables, and the Container swaps the whole memory 
 *  block to the disk instead of performing Java Serialization on the instance.
 */
public class BeanFieldsTransient extends EjbTest implements EjbCheck {


    /**
     *  The Bean Provider should not declare the session bean fields in the session
     *  bean class as transient.
     *
     *  This is to allow the Container to swap out an instance's state through 
     *  techniques other than the Java Serialization protocol. For example, the 
     *  Container's Java Virtual Machine implementation may use a block of memory 
     *  to keep the instance's variables, and the Container swaps the whole memory 
     *  block to the disk instead of performing Java Serialization on the instance.
     *   
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        if (descriptor instanceof EjbSessionDescriptor) {
            try {
                Class c = Class.forName(((EjbSessionDescriptor)descriptor).getEjbClassName(), false, getVerifierContext().getClassLoader());
                // fields should not be defined in the session bean class as transient.
                Field [] fields = c.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    int modifiers = fields[i].getModifiers();
                    if (!Modifier.isTransient(modifiers)) {
                        continue;
                    } else {
                        addWarningDetails(result, compName);
                        result.warning(smh.getLocalString
                                (getClass().getName() + ".warning",
                                        "Warning: Field [ {0} ] defined within session bean class [ {1} ] is defined as transient.  Session bean fields should not be defined in the session bean class as transient.",
                                        new Object[] {fields[i].getName(),((EjbSessionDescriptor)descriptor).getEjbClassName()}));
                    }
                }
            } catch (ClassNotFoundException e) {
                Verifier.debug(e);
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                        (getClass().getName() + ".failedException",
                                "Error: [ {0} ] class not found.",
                                new Object[] {((EjbSessionDescriptor)descriptor).getEjbClassName()}));
            } catch (Throwable t) { 
                addWarningDetails(result, compName);
                result.warning(smh.getLocalString
                        (getClass().getName() + ".warningException",
                        "Warning: [ {0} ] class encountered [ {1} ]. " +
                        "Cannot access fields of class [ {2} ] which is external to [ {3} ].",
                         new Object[] {(descriptor).getEjbClassName(),t.toString(),
                         t.getMessage(),
                         descriptor.getEjbBundleDescriptor().getModuleDescriptor().getArchiveUri()}));
            }
            return result;
        }

        if(result.getStatus()!=Result.WARNING){
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                            "The session bean class has defined all fields " +
                    "as non-transient fields."));

        }
        return result;
    }
}
