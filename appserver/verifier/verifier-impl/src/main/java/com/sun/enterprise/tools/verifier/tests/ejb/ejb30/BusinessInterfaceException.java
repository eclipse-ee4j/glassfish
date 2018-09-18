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
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * The methods of the business interface may declare arbitrary application 
 * exceptions. However, the methods of the business interface should not throw 
 * the java.rmi.RemoteException, even if the interface is a remote business 
 * interface or the bean class is annotated WebService or the method as 
 * WebMethod.
 * The methods of the business interface may only throw the 
 * java.rmi.RemoteException if the interface extends java.rmi.Remote.
 * 
 * @author Vikas Awasthi
 */
public class BusinessInterfaceException extends EjbTest {

    public Result check(EjbDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
        Set<String> localAndRemoteClassNames = descriptor.getLocalBusinessClassNames();
        localAndRemoteClassNames.addAll(descriptor.getRemoteBusinessClassNames());
        
        for (String localOrRemoteClass : localAndRemoteClassNames) 
            checkForRemoteException(localOrRemoteClass,result,compName);

        if(result.getStatus() != Result.WARNING) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (getClass().getName() + ".passed",
                            "Business interface(s) if any are valid."));
        }
        return result;
    }
    
    private void checkForRemoteException(String className, 
                                        Result result, 
                                        ComponentNameConstructor compName) {
        try {
            Class c = Class.forName(className, 
                                    false, 
                                    getVerifierContext().getClassLoader());
            // do not check further if the business interface extends java.rmi.Remote 
            if(java.rmi.Remote.class.isAssignableFrom(c))
                return;
            Method[] methods = c.getMethods();
            for (Method method : methods) {
                Class[] exceptions = method.getExceptionTypes();
                for (Class exception : exceptions) {
                    if(java.rmi.RemoteException.class.isAssignableFrom(exception)) {
                        result.getFaultLocation().setFaultyClassAndMethod(method);
                        addWarningDetails(result, compName);
                        result.warning(smh.getLocalString
                                        (getClass().getName() + ".warning",
                                        "java.rmi.RemoteException is thrown " +
                                        "in method [ {0} ] of business interface [ {1} ]",
                                        new Object[] {method.getName(), className}));
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            Verifier.debug(e);
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                            (getClass().getName() + ".failed1",
                            "[ {0} ] not found.",
                            new Object[] {className}));
        }
    }
}
