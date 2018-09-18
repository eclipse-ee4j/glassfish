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
import com.sun.enterprise.tools.verifier.tests.ejb.MethodUtils;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;

import java.lang.reflect.Method;

/**
 * The message driven bean class must implement the message listener interface 
 * or the methods of the message listener interface.
 * 
 * @author Vikas Awasthi
 */
public class MDBImplementsListenerMethods extends MessageBeanTest {
    
    public Result check(EjbMessageBeanDescriptor descriptor) {

        try {
            ClassLoader cl = getVerifierContext().getClassLoader();
            Class intfCls = Class.forName(descriptor.getMessageListenerType(), false, cl);
            Class ejbCls = Class.forName(descriptor.getEjbClassName(), false, cl);
            
            if(!intfCls.isAssignableFrom(ejbCls)) {
                Method[] methods = intfCls.getMethods();
                for (Method method : methods) {
                    boolean foundOne = false;
                    for (Method ejbMethod : ejbCls.getMethods()) {
                        if(MethodUtils.methodEquals(ejbMethod, method)) {
                            foundOne = true;
                            break;
                        }
                    }
                    if(!foundOne) {
                        addErrorDetails(result, compName);
                        result.failed(
                                smh.getLocalString(getClass().getName()+".failed",
                                "Message bean [ {0} ] neither implements listener " +
                                "interface [ {1} ] nor implements listener " +
                                "interface method [ {2} ]",
                                new Object[] {ejbCls.getSimpleName(), intfCls.getName(), method}));
                    }
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
