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

package com.sun.s1asdev.ejb.ee.ejb;

import jakarta.ejb.*;
import java.rmi.RemoteException;

public interface SFSB
    extends EJBObject
{

    public String getAccountHolderName()
        throws RemoteException;

    public int getBalance()
        throws RemoteException;

    public void incrementBalance(int val)
        throws RemoteException;

    public int getCheckpointedBalance()
        throws RemoteException;




    public void nonTxNonCheckpointedMethod()
        throws RemoteException;

    public void nonTxCheckpointedMethod()
        throws RemoteException;

    public void txNonCheckpointedMethod()
        throws RemoteException;

    public void txCheckpointedMethod()
        throws RemoteException;

}
