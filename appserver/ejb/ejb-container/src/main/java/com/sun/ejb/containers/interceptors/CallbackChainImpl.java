/*
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.containers.interceptors;

import java.lang.reflect.Method;

/**
 * @author Mahesh Kannan
 *         Date: Mar 10, 2008
 */
class CallbackChainImpl {

    protected CallbackInterceptor[] interceptors;
    protected int size;
    private Method method = null;

    CallbackChainImpl(CallbackInterceptor[] interceptors) {
        this.interceptors = interceptors;
        this.size = (interceptors == null) ? 0 : interceptors.length;

        // set invocation method if there is one on the bean class
        if (size > 0 && interceptors[size-1].isBeanCallback()) {
            method = interceptors[size-1].method;
        }
    }

    public Object invokeNext(int index, CallbackInvocationContext invContext)
            throws Throwable {

        invContext.method = method;
        Object result = null;
        if (index < size) {
            result = interceptors[index].intercept(invContext);
        } else {
            invContext.invokeSpecial();
        }

        return result;
    }

    public String toString() {
        StringBuilder bldr = new StringBuilder("CallbackInterceptorChainImpl");
        for (CallbackInterceptor inter : interceptors) {
            bldr.append("\n\t\t").append(inter);
        }

        return bldr.toString();
    }

    /**
     * Prepend an interceptor to an existing callback chain.
     * @param interceptor
     */
    public void prependInterceptor(CallbackInterceptor interceptor) {

        size++;

        CallbackInterceptor[] newArray = new CallbackInterceptor[size];
        newArray[0] = interceptor;
        for(int i = 1; i < size; i++) {
            newArray[i] = interceptors[i - 1];
        }

        interceptors = newArray;
    }
}
