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

import java.lang.reflect.Method;

/**
 * Represents a method call (or invocation)
 */
public class Call {

    private Method method;
    private Object[] arguments;
    private CallState callState = CallState.IN_PROCESS;
    private Throwable failureReason;
    private Object result;
    
    public Call(Method m, Object[] args) {
        method = m;
        arguments = args;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public CallState getState() {
        return callState;
    }

    public void setState(CallState state) {
        callState = state;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(Throwable reason) {
        failureReason = reason;
    }
}
