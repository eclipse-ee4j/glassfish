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

package com.sun.enterprise.tools.verifier.tests.ejb.messagebean;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.MethodUtils;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Message listener methods must not be declared as final or static.
 * 
 * @author Vikas Awasthi
 */
public class MessageListenerMethodModifiers extends MessageBeanTest {

    public Result check(EjbMessageBeanDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = 
                            getVerifierContext().getComponentNameConstructor();

        ClassLoader cl = getVerifierContext().getClassLoader();
        try {
            Class intfCls = Class.forName(descriptor.getMessageListenerType(), false, cl);
            Class ejbCls = Class.forName(descriptor.getEjbClassName(), false, cl);
            Method[] intfMethods = intfCls.getMethods();
            for (Method method : intfMethods) {
                for (Method ejbMethod : ejbCls.getMethods()) {
                    // if matching method is found then check the assertion
                    if (MethodUtils.methodEquals(ejbMethod, method)) {
                        if(Modifier.isFinal(ejbMethod.getModifiers()) ||
                                Modifier.isStatic(ejbMethod.getModifiers())) {
                            addErrorDetails(result, compName);
                            result.failed(smh.getLocalString
                                    (getClass().getName() + ".failed",
                                            "Wrong method [ {0} ]",
                                            new Object[]{ejbMethod}));
                        }
                        break;
                    }
                }// another test will report failure if listener method is not found
            }
        } catch (ClassNotFoundException e) {} // will be caught in other tests

        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (getClass().getName() + ".passed",
                            "Valid message listener method(s)."));
        }
        return result;
    }
}
