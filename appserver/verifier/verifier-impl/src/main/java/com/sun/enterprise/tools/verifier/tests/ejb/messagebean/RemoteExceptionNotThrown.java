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
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;

import java.lang.reflect.Method;

/**
 * Message listener methods should not throw java.rmi.RemoteException.
 * @author Vikas Awasthi
 */
public class RemoteExceptionNotThrown extends MessageBeanTest {

    public Result check(EjbMessageBeanDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        ClassLoader cl = getVerifierContext().getClassLoader();
        try {
            Method[] methods = descriptor.getMessageListenerInterfaceMethods(cl);
            for (int i = 0; i < methods.length; i++) {
                if(containsRemote(methods[i].getExceptionTypes())) {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                                    (getClass().getName()+".failed",
                                    "Method [ {0} ] throws RemoteException",
                                    new Object[] {methods[i]}));
                }
            }
        } catch (NoSuchMethodException e) {
            Verifier.debug(e);
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                            (getClass().getName()+".failed1",
                            "[ {0} ]", new Object[]{e.getMessage()}));
        }
        
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (getClass().getName() + ".passed",
                            "Valid message listener method(s)."));
        }
        return result;
    }
    
    /** returns true if one of the exceptions is RemoteException*/
    private boolean containsRemote(Class[] exceptions) {
        for (int i = 0; i < exceptions.length; i++) 
            if(exceptions[i].getName().equals("java.rmi.RemoteException"))
                return true;
        
        return false;
    }
}
