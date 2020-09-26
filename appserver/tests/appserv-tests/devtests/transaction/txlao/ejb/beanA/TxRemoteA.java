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

package com.sun.s1peqe.transaction.txlao.ejb.beanA;

import jakarta.ejb.EJBObject;
import java.rmi.RemoteException;

public interface TxRemoteA extends EJBObject{
    public boolean firstXAJDBCSecondNonXAJDBC() throws RemoteException ;
    public boolean firstNonXAJDBCSecondXAJDBC() throws RemoteException ;
    public boolean firstXAJDBCSecondXAJDBC() throws RemoteException ;
    public boolean firstNonXAJDBCSecondNonXAJDBC() throws RemoteException ;
    public boolean firstXAJMSSecondNonXAJDBC() throws RemoteException ;
    public boolean firstNonXAJDBCOnly() throws RemoteException ;
    public void cleanup() throws RemoteException;
    public boolean rollbackXAJDBCNonXAJDBC()throws RemoteException;
    public boolean rollbackNonXAJDBCXAJDBC() throws RemoteException;
    public boolean txCommit() throws RemoteException;
    public boolean txRollback() throws RemoteException;
}
