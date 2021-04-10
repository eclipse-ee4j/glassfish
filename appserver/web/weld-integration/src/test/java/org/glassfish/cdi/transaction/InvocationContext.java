/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.cdi.transaction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * User: paulparkinson Date: 12/12/12 Time: 1:12 PM
 */
public class InvocationContext implements jakarta.interceptor.InvocationContext {
    Method method;
    Exception exceptionFromProceed;
    TestInvocationContextTarget testInvocationContextTarget = new TestInvocationContextTarget();

    public InvocationContext(Method method, Exception exceptionFromProceed) {
        this.method = method;
        this.exceptionFromProceed = exceptionFromProceed;
    }

    public Object getTarget() {
        return testInvocationContextTarget;
    }

    class TestInvocationContextTarget {

    }

    public Object getTimer() {
        return null;
    }

    public Method getMethod() {
        return method;
    }

    public Constructor getConstructor() {
        return null;
    }

    public Object[] getParameters() {
        return new Object[0];
    }

    public void setParameters(Object[] params) {

    }

    public Map<String, Object> getContextData() {
        return null;
    }

    public Object proceed() throws Exception {
        if (exceptionFromProceed != null)
            throw exceptionFromProceed;
        return null;
    }
}
