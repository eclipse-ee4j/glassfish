/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.interceptors.intro;

import java.lang.reflect.Method;

import jakarta.interceptor.InvocationContext;
import jakarta.interceptor.AroundInvoke;

public class ArgumentsVerifier {

    @AroundInvoke
    public Object interceptCall(InvocationContext ctx)
           throws MyBadException, Exception
    {
        Method method = ctx.getMethod();
        String methodName = method.getName();
        Object[] params = ctx.getParameters();


        if (methodName.equals("concatAndReverse")) {
            String arg1 = (String) params[0];
            String arg2 = (String) params[1];

            if ("null".equalsIgnoreCase(arg1)) {
                params[0] = null;
            }
            if ("null".equalsIgnoreCase(arg2)) {
                params[1] = null;
            }
        } else if (methodName.equals("plus")) {
            params = new Object[] {new Byte((byte) 6), new Short((short) 6), new Integer(6)};
        } else {
            params = new Object[] {new Byte((byte) 88), new Short((short) 6)};
        }

        try {
            ctx.setParameters(params);
        } catch (IllegalArgumentException illArgEx) {
            throw new MyBadException("Invalid type as argument", illArgEx);
        }
        return ctx.proceed();
    }

}
