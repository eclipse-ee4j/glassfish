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

package com.sun.enterprise.tools.verifier.tests.ejb.intf.localintf;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.lang.reflect.Method;

/**
 * The throws clause of a home method on the local home interface may include 
 * additional application-level exceptions. It must not include the 
 * java.rmi.RemoteException.
 * 
 * @author Vikas Awasthi
 */
public class LocalInterfaceRemoteException extends EjbTest {

    public Result check(EjbDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        ClassLoader cl = getVerifierContext().getClassLoader();
        String localClassName = descriptor.getLocalHomeClassName();
        if (localClassName!=null) {
            try {
                Class localHome = Class.forName(localClassName, false, cl);
                Method[] methods = localHome.getMethods();
                for (int i = 0; i < methods.length; i++) {
                    if(containsRemote(methods[i].getExceptionTypes())) {
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString
                                (getClass().getName()+".failed",
                                "Method [ {0} ] throws a RemoteException.", 
                                new Object[]{methods[i]}));
                    }
                
                }
            } catch (ClassNotFoundException e) {
                Verifier.debug(e);
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                                (getClass().getName()+".failed1",
                                "LocalHome class [ {0} ] not found.", 
                                new Object[]{localClassName}));
            }
        }

        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                            "Valid LocalInterface."));
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
