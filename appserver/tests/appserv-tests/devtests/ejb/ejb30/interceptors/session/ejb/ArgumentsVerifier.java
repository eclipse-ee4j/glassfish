/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import java.lang.reflect.Method;
import java.lang.StringBuilder;
import java.util.stream.Collectors;
import java.util.Arrays;

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
        System.out.println("**++**Args: " + toString(params));
        if (methodName.equals("addIntInt")) {
            params[0] = new Integer(10);
            params[1] = new Integer(20);
        } else if (methodName.equals("setInt")) {
            params[0] = new Long(10);
        } else if (methodName.equals("setLong")) {
            params[0] = new Integer(10);
        } else if (methodName.equals("addLongLong")) {
            params[0] = new Integer(10);
            params[0] = new Long(10);
        } else if (methodName.equals("setFoo")) {
            params[0] = new SubFoo();
        } else if (methodName.equals("setBar")) {
            // Should fail
            params[0] = new SubFoo();
        } else if (methodName.equals("emptyArgs")) {
            params = new Object[1];
            params[0] = new Long(45);
        } else if (methodName.equals("objectArgs")) {
            params = new Object[0];
        }

        try {
            ctx.setParameters(params);
        } catch (IllegalArgumentException illArgEx) {
            throw new MyBadException("Invalid type as argument", illArgEx);
        }
        Object retVal = ctx.proceed();

        if (methodName.equals("setIntInt")) {
            Integer ii = (Integer) retVal;
            if (! ii.equals(new Integer(30))) {
                throw new WrongResultException("Wrong result. expected 20. Got: " + ii);
            }
        }

        return retVal;
    }

    private String toString(Object[] objects) {
        if (objects == null) {
            return null;
        }

        return "[\n" + Arrays.stream(objects).map(o -> o == null ? "null" : (o.toString() + ": " + o.getClass()))
            .collect(Collectors.joining("\n")) + "\n]";
    }

}
