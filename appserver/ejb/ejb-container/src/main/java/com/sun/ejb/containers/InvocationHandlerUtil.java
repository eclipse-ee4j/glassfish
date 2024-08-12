/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ejb.EJBException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;

public final class InvocationHandlerUtil {

    InvocationHandlerUtil() {}

    public static Object invokeJavaObjectMethod(InvocationHandler handler,
                                         Method method, Object[] args)
        throws EJBException {

        Object returnValue = null;

        // Can only be one of :
        //     boolean java.lang.Object.equals(Object)
        //     int     java.lang.Object.hashCode()
        //     String  java.lang.Object.toString()
        //
        // Optimize by comparing as few characters as possible.

        switch( method.getName().charAt(0) ) {
            case 'e' :
                boolean result = false;
                if (args[0] != null) {
                    Object other = Proxy.isProxyClass(args[0].getClass()) ?
                            Proxy.getInvocationHandler(args[0]) : args[0];
                            result = handler.equals(other);
                }
                returnValue = result;
                break;
            case 'h' :
                returnValue = handler.hashCode();
                break;
            case 't' :
                returnValue = handler.toString();
                break;
            default :
                throw new EJBException(method.getName());
        }

        return returnValue;
    }

    public static boolean isDeclaredException(Throwable t,
                                       Class[] declaredExceptions)
    {
        boolean declaredException = false;

        for(int i = 0; i < declaredExceptions.length; i++) {
            Class next = declaredExceptions[i];
            if( next.isAssignableFrom(t.getClass()) ) {
                declaredException = true;
                break;
            }
        }

        return declaredException;
    }

    public static void throwLocalException(Throwable t,
                                    Class[] declaredExceptions)
        throws Throwable
    {
        Throwable toThrow;

        if( (t instanceof java.lang.RuntimeException) ||
            (isDeclaredException(t, declaredExceptions)) ) {
            toThrow = t;
        } else {
            toThrow = new EJBException(t.getMessage());
            toThrow.initCause(t);
        }

        throw toThrow;

    }

    public static void throwRemoteException(Throwable t,
                                     Class[] declaredExceptions)
        throws Throwable
    {
        Throwable toThrow;

        if( (t instanceof java.lang.RuntimeException) ||
            (t instanceof java.rmi.RemoteException) ||
            (isDeclaredException(t, declaredExceptions)) ) {
            toThrow = t;
        } else {
            toThrow = new RemoteException(t.getMessage(), t);
        }

        throw toThrow;
    }
}
