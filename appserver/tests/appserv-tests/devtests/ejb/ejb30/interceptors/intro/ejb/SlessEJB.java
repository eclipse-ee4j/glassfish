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

import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;


@Stateless
public class SlessEJB
        implements Sless
{
    public String sayHello() {
            return "Hello";
    }

    @Interceptors(ArgumentsVerifier.class)
    public String concatAndReverse(String one, String two) {
        String str = null;
        if (one != null) {
            str = one;
        }
        if (two != null) {
            str = str + two;
        }

        String result = null;
        if (str != null) {
            result = "";
            int len = str.length()-1;
            for (int i=str.length()-1; i>=0; i--) {
                result += str.charAt(i);
            }
        }

        return result;
    }

    @Interceptors(ArgumentsVerifier.class)
    public double plus(short s, int ival, long lval) {
        return s + ival + lval;
    }

    @Interceptors(ArgumentsVerifier.class)
    public boolean isGreaterShort(Number one, Number two) {
        return one.shortValue() > two.shortValue();
    }

}


