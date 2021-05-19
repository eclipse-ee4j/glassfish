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

package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;


//Default is @Local
@Stateless
public class SlessEJB
        implements Sless
{
    public String sayHello() {
            return "Hello";
    }

    public double computeMidPoint(int min, int max)
            throws SwapArgumentsException
    {
            if (min > max) {
                    throw new SwapArgumentsException("("+min+", "+max+")");
            }
            return (min*1.0+max)/2.0;
    }

    @Interceptors(ArgumentsVerifier.class)
    public void setFoo(Foo foo) {
    }

    @Interceptors(ArgumentsVerifier.class)
    public void setBar(Bar bar) {
    }

    @Interceptors(ArgumentsVerifier.class)
    public void emptyArgs() {
    }

    @Interceptors(ArgumentsVerifier.class)
    public void objectArgs(Object obj) {
    }

    @Interceptors(ArgumentsVerifier.class)
    public void setInt(int val)
        throws MyBadException {
    }

    @Interceptors(ArgumentsVerifier.class)
    public int addIntInt(int i, int j)
        throws WrongResultException, MyBadException {
        return i + j;
    }

    @Interceptors(ArgumentsVerifier.class)
    public void setLong(long obj)
        throws MyBadException {
    }

    @Interceptors(ArgumentsVerifier.class)
    public long addLongLong(long obj, long k)
        throws MyBadException {
        return obj + k;
    }

}


