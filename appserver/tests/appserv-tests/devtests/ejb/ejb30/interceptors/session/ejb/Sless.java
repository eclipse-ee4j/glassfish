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

import jakarta.ejb.Local;

@Local
public interface Sless
{
    public String sayHello();

    public double computeMidPoint(int min, int max)
            throws SwapArgumentsException;

    public void setFoo(Foo foo)
        throws MyBadException;

    public void setBar(Bar bar)
        throws MyBadException;

    public void emptyArgs()
        throws MyBadException;

    public void objectArgs(Object obj)
        throws MyBadException;

    public void setInt(int val)
        throws MyBadException;

    public int addIntInt(int i, int j)
        throws WrongResultException, MyBadException;

    public void setLong(long obj)
        throws MyBadException;

    public long addLongLong(long obj, long k)
        throws MyBadException;

}

class Foo implements java.io.Serializable  {}

class SubFoo extends Foo {}

class Bar implements java.io.Serializable  {}

