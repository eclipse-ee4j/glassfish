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

package org.glassfish.webservices;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.Invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * This extends InvokerImpl - the difference is this creates
 * a Map of methods from class to proxy class
 */
public class EjbInvokerImpl extends InvokerImpl {

    private final HashMap<Method,Method> methodMap = new HashMap<Method,Method>();

    public EjbInvokerImpl(Class endpointImpl, Invoker core,
            Object inv, WebServiceContextImpl w) {
        super(core, inv, w);

        Class proxyClass = invokeObject.getClass();
        for(Method x : endpointImpl.getMethods()) {
            try {
                Method mappedMethod =
                    proxyClass.getMethod(x.getName(), x.getParameterTypes());
                methodMap.put(x, mappedMethod);
            } catch (NoSuchMethodException noex) {
                // We do not take any action because these may be excluded @WebMethods
                // or EJB business methods that are not @WebMethods etc
            }
        }
    }

    /**
     * Here is where we actually call the endpoint method
     */
    public Object invoke(Packet p, Method m, Object... args )
                                throws InvocationTargetException, IllegalAccessException {
        Method mappedMethod = methodMap.get(m);
        if(mappedMethod != null)
            return(super.invoke(p, mappedMethod,  args));
        throw new IllegalAccessException("Unable to find invocation method for "+m+". Map="+methodMap);
    }
}
