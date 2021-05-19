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

package com.sun.s1asdev.ejb.ee.txcheckpoint.simpletx.ejb;

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

    private static final String LOCAL_CHILD_SUFFIX = "_childLocal";

    private transient String message;

    private int activateCount;
    private int passivateCount;

    private SessionContext              sessionCtx;
    private String                      sfsbName;

    public void ejbCreate(String sfsbName) {
        this.sfsbName = sfsbName;
    }

    public String getName() {
        return this.sfsbName;
    }

    public String getTxName() {
        return this.sfsbName;
    }

    public void setSessionContext(SessionContext sc) {
        this.sessionCtx = sc;
    }

    public int getActivateCount() {
        return activateCount;
    }

    public int getPassivateCount() {
        return passivateCount;
    }

    public void ejbRemove() {}

    public void ejbActivate() {
        activateCount++;
    }

    public void ejbPassivate() {
        passivateCount++;
    }

}
