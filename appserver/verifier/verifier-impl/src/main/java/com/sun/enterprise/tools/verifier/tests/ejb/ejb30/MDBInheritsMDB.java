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

package com.sun.enterprise.tools.verifier.tests.ejb.ejb30;

import com.sun.enterprise.tools.verifier.Result;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;

import java.util.Set;

/**
 * A message-driven bean class must not have a superclass that is itself a 
 * message-driven bean class.
 * 
 * @author Vikas Awasthi
 */
public class MDBInheritsMDB extends MessageBeanTest {
    
    public Result check(EjbMessageBeanDescriptor descriptor) {
        try {
            ClassLoader cl = getVerifierContext().getClassLoader();
            Class ejbCls = Class.forName(descriptor.getEjbClassName(), false, cl);
            Set<EjbDescriptor> descrptors =
                                descriptor.getEjbBundleDescriptor().getEjbs();
            for (EjbDescriptor ejbDescriptor : descrptors) {
                if(!(ejbDescriptor instanceof EjbMessageBeanDescriptor))
                    continue;
                if(descriptor.getEjbClassName().equals(ejbDescriptor.getEjbClassName()))
                    continue;
                Class mdbCls = null;
                try {
                    mdbCls = Class.forName(ejbDescriptor.getEjbClassName(), false, cl);
                } catch (ClassNotFoundException e) {
                    continue; // ignore as this error will be caught by other tests
                }
                if(mdbCls.isAssignableFrom(ejbCls)) {
                    addErrorDetails(result, compName);
                    result.failed(
                            smh.getLocalString(getClass().getName()+".failed",
                            "Message bean [ {0} ] inherits other message bean [ {1} ]",
                            new Object[] {ejbCls.getName(), mdbCls.getName()}));
                }

            }
        } catch (ClassNotFoundException e) {
            //ignore as this error will be caught by other tests
            logger.fine(descriptor.getEjbClassName() + " Not found");
        }

        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(
                    smh.getLocalString(getClass().getName()+".passed",
                            "Valid Message bean [ {0} ]",
                            new Object[] {descriptor.getEjbClassName()}));
        }
        
        return result;
    }
}
