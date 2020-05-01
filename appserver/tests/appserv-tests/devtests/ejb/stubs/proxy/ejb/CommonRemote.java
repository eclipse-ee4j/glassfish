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

package com.sun.s1asdev.ejb.stubs.proxy;

import jakarta.ejb.*;
import java.rmi.RemoteException;

public interface CommonRemote extends EJBObject
{
    public static final int STATELESS = 0;
    public static final int STATEFUL = 1;
    public static final int BMP = 2;
    public static final int CMP = 3;

    void notSupported() throws RemoteException;
    void required() throws RemoteException;
    void requiresNew() throws RemoteException;
    void mandatory() throws RemoteException;
    void never() throws RemoteException;
    void supports() throws RemoteException;

    // test proxy for behavior when interface method is not defined on
    // bean class.
    void notImplemented() throws RemoteException;

    void testException1() throws Exception, RemoteException;

    // will throw ejb exception
    void testException2() throws RemoteException;

    // throws some checked exception
    void testException3() throws jakarta.ejb.FinderException, RemoteException;

    void testException4() throws jakarta.ejb.FinderException, RemoteException;


    void testPassByRef1(int a) throws RemoteException;
    void testPassByRef2(Helper1 helper1) throws RemoteException;
    void testPassByRef3(Helper2 helper2) throws RemoteException;
    void testPassByRef4(CommonRemote cr) throws RemoteException;
    Helper1 testPassByRef5() throws RemoteException;
    Helper2 testPassByRef6() throws RemoteException;
    CommonRemote testPassByRef7() throws RemoteException;
    int testPassByRef8() throws RemoteException;

}
