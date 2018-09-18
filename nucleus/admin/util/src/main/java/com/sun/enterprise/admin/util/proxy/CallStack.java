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
import java.util.Iterator;
import java.util.Stack;

/**
 * Represents a call stack. 
 */
public class CallStack {

    private Stack callStack = new Stack();

    /** Creates a new instance of CallStack */
    public CallStack() {
    }

    public int getStackSize() {
        return callStack.size();
    }

    public void beginCall(Method m, Object[] args) {
        Call call = new Call(m, args);
        beginCall(call);
    }

    public void beginCall(Call call) {
        callStack.push(call);
    }

    public void endCall() {
        Call call = (Call)callStack.pop();
        if (!call.getState().isFinished()) {
            call.setState(CallState.SUCCESS);
        }
    }

    public void endCallWithError(Throwable th) {
        Call call = (Call)callStack.peek();
        call.setState(CallState.FAILED);
        call.setFailureReason(th);
        endCall();
    }

    public Call getActiveCall() {
        return (Call)callStack.peek();
    }

    public Iterator getCallStack() {
        return callStack.iterator();
    }

}
