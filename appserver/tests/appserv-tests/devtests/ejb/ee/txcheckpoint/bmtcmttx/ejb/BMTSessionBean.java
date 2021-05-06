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

public class BMTSessionBean
    implements SessionBean
{

    private static final String LOCAL_CHILD_SUFFIX = "_childLocal";

    private transient String message;

    private int activateCount;
    private int passivateCount;
    private int        count;

    private SessionContext              sessionCtx;
    private String                      sfsbName;
    private UserTransaction                userTx;

    private CMTSessionHome cmtHome;
    private CMTSession cmtSfsb;

    private int MAX_SIZE = 6;
    private CMTSession[]    cmtSessions = new CMTSession[MAX_SIZE];

    public void ejbCreate(String sfsbName) {
        System.out.println ("In SFSB.ejbCreate() for name -> " + sfsbName);
        this.sfsbName = sfsbName;

        try {
            InitialContext ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/CMTSessionHome");
            cmtHome = (CMTSessionHome)PortableRemoteObject.narrow(
                objref, CMTSessionHome.class);
            cmtSfsb = (CMTSession) cmtHome.create(sfsbName);

            for (int i=0; i<MAX_SIZE; i++) {
                cmtSessions[i] = (CMTSession) cmtHome.create(sfsbName + i);
            }
        } catch (Exception ex) {
            throw new EJBException(ex.toString());
        }
    }

    public String getName() {
        System.out.println("In getName() for " + sfsbName);
        return this.sfsbName;
    }

    public void setSessionContext(SessionContext sc) {
        this.sessionCtx = sc;
    }

    public void startTx() {
        try {
            userTx = sessionCtx.getUserTransaction();
            userTx.begin();
        } catch (Exception ex) {
            throw new EJBException(ex.toString());
        }
    }

    public void commitTx() {
        try {
            userTx.commit();
        } catch (Exception ex) {
            throw new EJBException(ex.toString());
        }
    }

    public void rollbackTx() {
        try {
            userTx.rollback();
        } catch (Exception ex) {
            throw new EJBException(ex.toString());
        }
    }

    public void incrementCount() {
        count++;
    }

    public void accessCMTBean()
        throws java.rmi.RemoteException
    {
        cmtSfsb.incrementCount();
        for (int i=0; i<MAX_SIZE; i++) {
            cmtSessions[i].incrementCount();
        }
    }

    public CMTSession getCMTSession() {
        return cmtSfsb;
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
