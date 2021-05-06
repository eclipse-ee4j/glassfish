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

package com.sun.s1asdev.ejb.sfsb.stress.ejb;

import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import jakarta.transaction.UserTransaction;

import java.rmi.RemoteException;

public class StressSFSBBean
    implements SessionBean
{
    private SessionContext              sessionCtx;
    private Context                     initialCtx;
    private String                      name;

    public void ejbCreate(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean hasSameName(String name) {
        return  this.name.equals(name);
    }

    public void ping() {
    }

    public boolean doWork(long millis) {
        return true;
    }

    public void setSessionContext(SessionContext sc) {
        this.sessionCtx = sc;
        try {
            this.initialCtx = new InitialContext();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void ejbRemove() {}

    public void ejbActivate() {
        //System.out.println ("In SFSB.ejbActivate() for name -> " + sfsbName);
    }

    public void ejbPassivate() {
        //System.out.println ("####In SFSB.ejbPassivate() for: " + sfsbName);
    }

    /*
    private boolean lookupEntityHome() {
        boolean status = false;
        try {
            Object homeRef = initialCtx.lookup("java:comp/env/ejb/SimpleEntityHome");
            this.entityHome = (SimpleEntityHome)
                PortableRemoteObject.narrow(homeRef, SimpleEntityHome.class);

            status = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return status;
    }

    private boolean lookupEntityLocalHome() {
        boolean status = false;
        try {
            Object homeRef = envSubCtx.lookup("SimpleEntityLocalHome");
            this.entityLocalHome = (SimpleEntityLocalHome) homeRef;

            status = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return status;
    }
    */
}
