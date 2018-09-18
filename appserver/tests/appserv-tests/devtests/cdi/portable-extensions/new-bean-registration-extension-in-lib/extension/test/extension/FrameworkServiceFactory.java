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


/**
 * A simple Service Factory class that provides the ability to obtain/get 
 * references to a service implementation (obtained from a service registry) 
 * and also provides a mechanism to unget or return a service after its usage
 * is completed.
 * 
 * @author Sivakumar Thyagarajan
 */
public class FrameworkServiceFactory {
    private static final boolean DEBUG_ENABLED = false;

    /**
     * Get a reference to the service of the provided <code>Type</code>
     */
    public static Object getService(final Type type, final FrameworkService fs){
        debug("getService " + type + " FS:" + fs);
        Object instance = lookupService(type, fs.waitTimeout());
        
        //If the service is marked as dynamic, when a method is invoked on a 
        //a service proxy, an attempt is made to get a reference to the service 
        //and then the method is invoked on the newly obtained service.
        //This scheme should work for statless and/or idempotent service 
        //implementations that have a dynamic lifecycle that is not linked to
        //the service consumer [service dynamism]
        if (fs.dynamic()) {
            InvocationHandler proxyInvHndlr = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                    debug("Method " + method + " called on proxy");
                    //Always lookup the service and invoke the method in this.
                    return method.invoke(lookupService(type, fs.waitTimeout()), args);
                }
            };
            instance =  Proxy.newProxyInstance(
                                Thread.currentThread().getContextClassLoader(), 
                                new Class[]{(Class)type}, 
                                proxyInvHndlr); 
        }
        return instance;
    }

    //NOTE:hard-coded service instantiation for this test, 
    //but ideally should get the
    //service implementation from the framework's service registry
    private static Object lookupService(Type type, int waitTimeout) {
        String clazzName = ((Class)type).getName() + "Impl";
        System.out.println("LOADING " + clazzName);
        try {
            return FrameworkServiceFactory.class.getClassLoader().loadClass(clazzName).newInstance();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Unget the service
     */
    public static void ungetService(Object serviceInstance, 
            Type type, FrameworkService frameworkService){
        //unget the service instance from the service registry
    }
    
    private static void debug(String string) {
        if(DEBUG_ENABLED)
            System.out.println("ServiceFactory:: " + string);
    }
    

}
