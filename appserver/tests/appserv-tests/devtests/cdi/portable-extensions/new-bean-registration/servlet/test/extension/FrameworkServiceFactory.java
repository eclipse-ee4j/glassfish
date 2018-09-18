/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.extension;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import test.fwk.FrameworkService;
import test.fwk.SomeFwkServiceImpl;
import test.fwk.SomeFwkServiceInterface;

public class FrameworkServiceFactory {
    public static Object getService(final Type type, final FrameworkService fs){
        //NOTE:hard-coded for this test, but ideally should get the
        //service implementation from the service registry
        SomeFwkServiceInterface instance = 
            (SomeFwkServiceInterface) lookupService(type, fs.waitTimeout());
        
        if (fs.dynamic()) {
            InvocationHandler ih = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                    System.out.println("****************** Method " + method + " called on proxy");
                    return method.invoke(lookupService(type, fs.waitTimeout()), args);
                }
            };
            instance = (SomeFwkServiceInterface) 
            Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), 
                    new Class[]{SomeFwkServiceInterface.class}, ih); 
        }
        return instance;
    }
    
    private static Object lookupService(Type type, int waitTimeout) {
        if (type.equals(SomeFwkServiceInterface.class)){ 
            return new SomeFwkServiceImpl("test");
        }
        return null;
    }

    public static void ungetService(Object serviceInstance){
        //unget the service instance from the service registry
    }

}
