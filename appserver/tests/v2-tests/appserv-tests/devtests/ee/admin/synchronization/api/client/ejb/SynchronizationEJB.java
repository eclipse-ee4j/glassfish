/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.admin.ee.synchronization.api.client;

import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import java.io.File;
import java.io.IOException;

import java.rmi.RemoteException;

import com.sun.enterprise.ee.synchronization.api.SynchronizationClient;
import com.sun.enterprise.ee.synchronization.api.SynchronizationFactory;
import com.sun.enterprise.ee.synchronization.SynchronizationException;

public class SynchronizationEJB
    implements SessionBean
{
    private SessionContext context;
    private Context initialCtx;

    public void ejbCreate() {
    }

    public boolean getFile(String instanceName, String sourceFile,
    String destLoc) {
    try {
            SynchronizationClient sc =
              SynchronizationFactory.createSynchronizationClient( instanceName);
            sc.connect();
            sc.get(sourceFile, destLoc);
            sc.disconnect();
            return true;
    } catch(Exception e) {
        e.printStackTrace();
        return false;
    }
    }

    public boolean putFile(String instanceName, String sourceFile,
    String destDir)  {
    try {
            SynchronizationClient sc =
              SynchronizationFactory.createSynchronizationClient( instanceName);
            sc.connect();
            String s = sc.put(sourceFile, destDir);
            sc.disconnect();
            System.out.println("Upload file at " + s);
            return true;
    } catch(Exception e) {
        e.printStackTrace();
        return false;
    }
    }

    public void setSessionContext(SessionContext sc) {
        this.context = sc;
        try {
            this.initialCtx = new InitialContext();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void ejbRemove() {}

    public void ejbActivate() {
        System.out.println ("In SFSB.ejbActivate() " );
    }

    public void ejbPassivate() {
        System.out.println ("In SFSB.ejbPassivate() ");
    }
}
