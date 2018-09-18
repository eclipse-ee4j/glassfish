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

package com.sun.ejb.containers;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * This class is used as a "proxy" or adapter between the remote business 
 * interface proxy and the EJBObjectInvocationHandler. An instance of this 
 * class is created for each remote business interface of a bean. All 
 * java.lang.Object methods are handled by this InvocationHandler itself 
 * while the business interface methods are delegated to the delegate 
 * (which is the EJBObjectInvocaionHandler). 
 *   
 * @author Kenneth Saks
 *
 */
public class EJBObjectInvocationHandlerDelegate
    implements InvocationHandler {

    private Class remoteBusinessIntfClass;
    private EJBObjectInvocationHandler delegate;
    
    EJBObjectInvocationHandlerDelegate(Class intfClass, 
                                       EJBObjectInvocationHandler delegate) {
        this.remoteBusinessIntfClass = intfClass;
        this.delegate = delegate;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) 
        throws Throwable {
        
        Class methodClass = method.getDeclaringClass();
        Object result = null;
        if( methodClass == java.lang.Object.class ) {
            result = InvocationHandlerUtil.invokeJavaObjectMethod
                (this, method, args);
        } else {
            result = delegate.invoke(remoteBusinessIntfClass, method, args);
        }
        
        return result;
    }
    
    

}
