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

package com.sun.s1asdev.ejb.ejb30.interceptors.bindings;

import java.util.List;
import java.util.LinkedList;

import jakarta.interceptor.InvocationContext;
import jakarta.ejb.EJBException;

public class Common {

    static final String INTERCEPTORS_PROP = "interceptors";
    static final String NOTHING_METHOD = "nothing";

    static void aroundInvokeCalled(InvocationContext ctx, String id) {

        List<String> interceptors = (List<String>)
            ctx.getContextData().get(INTERCEPTORS_PROP);

        if( interceptors == null ) {
            interceptors = new LinkedList<String>();
            ctx.getContextData().put(INTERCEPTORS_PROP, interceptors);
        }

        interceptors.add(id);

    }

    static void checkResults(InvocationContext ctx) {

        String methodName = ctx.getMethod().getName();

        List<String> expected = null;

        if( !methodName.equals("nothing") ) {

            expected = new LinkedList<String>();

            String methodNameUpper = methodName.toUpperCase();

            for( char nextChar : methodNameUpper.toCharArray() ) {
                expected.add(nextChar + "");
            }
        }

        List<String> actual = (List<String>)
            ctx.getContextData().get(INTERCEPTORS_PROP);

        String msg = "Expected " + expected + " for method " +
            ctx.getMethod() + " actual = " + actual;

        if( (expected == null) && (actual == null) ) {
            System.out.println("Successful interceptor chain : " + msg);
        } else if( (expected == null) || (actual == null) ) {
            throw new EJBException(msg);
        } else if( !expected.equals(actual) ) {
            throw new EJBException(msg);
        } else {
            System.out.println("Successful interceptor chain : " + msg);
        }
    }

}
