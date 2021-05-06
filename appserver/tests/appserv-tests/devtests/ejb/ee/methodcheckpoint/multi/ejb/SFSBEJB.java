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

import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import jakarta.transaction.UserTransaction;

import java.rmi.RemoteException;

public class SFSBEJB
    implements SessionBean
{

    private SessionContext              sessionCtx;

    private String                      accountHolderName;
    private transient int                balance;
    private int                                checkpointedBalance;

    public void ejbCreate(String accountHolderName, int balance) {
        this.accountHolderName = accountHolderName;
        this.balance = balance;
    }

    public void setSessionContext(SessionContext sc) {
        this.sessionCtx = sc;
    }

    public void ejbRemove() {}

    public void ejbActivate() {
        balance = checkpointedBalance;
    }

    public void ejbPassivate() {
        checkpointedBalance = balance;
    }

    public String getAccountHolderName() {
        return this.accountHolderName;
    }

    public int getBalance() {
        return balance;
    }

    public void incrementBalance(int val) {
        balance += val;
    }

    public int getCheckpointedBalance() {
        return checkpointedBalance;
    }

    public void nonTxNonCheckpointedMethod() {
    }

    public void nonTxCheckpointedMethod() {
    }

    public void txNonCheckpointedMethod() {
    }

    public void txCheckpointedMethod() {
    }

}
