/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.sfsb.passivateactivate.ejb;

import jakarta.ejb.*;
import java.rmi.RemoteException;

public interface SFSB
    extends EJBObject
{

    public String getName()
        throws RemoteException;

    public void createSFSBChild()
        throws RemoteException;

    public String getMessage()
        throws RemoteException;

    public boolean checkSessionContext()
        throws RemoteException;

    public boolean checkInitialContext()
        throws RemoteException;

    public boolean checkEntityHome()
        throws RemoteException;

    public boolean checkEntityLocalHome()
        throws RemoteException;

    public boolean checkEntityRemoteRef()
        throws RemoteException;

    public boolean checkEntityLocalRef()
        throws RemoteException;

    public boolean checkHomeHandle()
        throws RemoteException;

    public boolean checkHandle()
        throws RemoteException;

    public boolean checkUserTransaction()
        throws RemoteException;

    public boolean isOK(String name)
        throws RemoteException;

    public int getActivationCount()
        throws RemoteException;

    public int getPassivationCount()
        throws RemoteException;

    public void makeStateNonSerializable()
        throws RemoteException;

    public void sleepForSeconds(int sec)
        throws RemoteException;

    public void unusedMethod()
        throws RemoteException;
}
