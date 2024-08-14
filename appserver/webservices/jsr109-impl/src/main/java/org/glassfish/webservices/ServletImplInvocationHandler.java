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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * InvocationHandler used to delegate calls to JAXRPC servlet impls
 * that aren't subtypes of their associated Service Endpoint Interface.
 *
 * @author Kenneth Saks
 */
public class ServletImplInvocationHandler implements InvocationHandler {

    private static final Logger logger = LogUtils.getLogger();

    private final Object servletImplDelegate;
    private final Class servletImplClass;

    public ServletImplInvocationHandler(Object delegate) {
        servletImplDelegate = delegate;
        servletImplClass    = delegate.getClass();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {

        // NOTE : be careful with "args" parameter.  It is null
        //        if method signature has 0 arguments.

        Class methodClass = method.getDeclaringClass();
        if( methodClass == java.lang.Object.class )  {
            return invokeJavaObjectMethod(this, method, args);
        }

        Object returnValue = null;

        try {
            // Since impl class isn't subtype of SEI, we need to do a
            // method lookup to get method object to use for invocation.
            Method implMethod = servletImplClass.getMethod(method.getName(), method.getParameterTypes());
            returnValue = implMethod.invoke(servletImplDelegate, args);
        } catch(InvocationTargetException ite) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, LogUtils.EXCEPTION_THROWN, ite);
            }
            throw ite.getCause();
        } catch(Throwable t) {
            logger.log(Level.INFO, LogUtils.ERROR_INVOKING_SERVLETIMPL, t);
            throw t;
        }

        return returnValue;
    }


    private Object invokeJavaObjectMethod(InvocationHandler handler, Method method, Object[] args) throws Throwable {
        Object returnValue = null;

        // Can only be one of :
        //     boolean java.lang.Object.equals(Object)
        //     int     java.lang.Object.hashCode()
        //     String  java.lang.Object.toString()
        //
        // Optimize by comparing as few characters as possible.

        switch( method.getName().charAt(0) ) {
            case 'e' :
                Object other = Proxy.isProxyClass(args[0].getClass()) ?
                    Proxy.getInvocationHandler(args[0]) : args[0];
                returnValue = Boolean.valueOf(handler.equals(other));
                break;
            case 'h' :
                returnValue = Integer.valueOf(handler.hashCode());
                break;
            case 't' :
                returnValue = handler.toString();
                break;
            default :
                throw new Throwable("Object method " + method.getName() + "not found");
        }

        return returnValue;
    }

}
