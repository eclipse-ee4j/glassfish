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

package com.sun.enterprise.admin.util.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A proxy class
 */
public class ProxyClass implements InvocationHandler {

    private static InheritableThreadLocal callStackHolder = new InheritableThreadLocal() {
        protected synchronized Object initialValue() {
            return new CallStack();
        }
    };

    private static Logger _logger = getLogger();

    private Object delegate;
    private Interceptor interceptor;

    /** Creates a new instance of Proxy */
    public ProxyClass(Object handler, Interceptor interceptor) {
        delegate = handler;
        this.interceptor = interceptor;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Call call = new Call(method, args);
        CallStack callStack = (CallStack) callStackHolder.get();
        callStack.beginCall(call);
        try {
            interceptor.preInvoke(callStack);
        } catch (Throwable t) {
            _logger.log(Level.FINE, "Preinvoke failed for MBeanServer interceptor [{0}].", t.getMessage());
            _logger.log(Level.FINEST, "Preinvoke exception for MBeanServer interceptor.", t);
        }
        Object result = null;
        boolean success = true;
        Throwable failReason = null;
        try {
            result = method.invoke(delegate, args);
        } catch (InvocationTargetException ite) {
            success = false;
            failReason = ite.getTargetException();
            throw failReason;
        } catch (Throwable t) {
            success = false;
            failReason = t;
            throw failReason;
        } finally {
            if (!success) {
                call.setState(CallState.FAILED);
                call.setFailureReason(failReason);
            }
            call.setResult(result);

            if (!(call.getState().isFailed()))
                call.setState(CallState.SUCCESS);

            try {
                interceptor.postInvoke(callStack);
            } catch (Throwable t) {
                _logger.log(Level.FINE, "Postinvoke failed for MBeanServer interceptor [{0}].", t.getMessage());
                _logger.log(Level.FINEST, "Postinvoke exception for MBeanServer interceptor.", t);
            }
            callStack.endCall();
        }
        return result;
    }

    private static Logger getLogger() {
        String loggerName = System.getProperty("com.sun.aas.admin.logger.name");
        if (loggerName == null) {
            loggerName = "global";
        }
        return Logger.getLogger(loggerName);
    }
}
