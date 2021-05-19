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

package com.sun.s1asdev.ejb.txprop.cmttimeout;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBException;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import com.sun.s1asdev.ejb.txprop.cmttimeout.HelloHome;
import com.sun.s1asdev.ejb.txprop.cmttimeout.Hello;

public class FooBean
    implements SessionBean
{

    private SessionContext sc;

    public FooBean() {}

    public void ejbCreate() throws RemoteException {
        System.out.println("In FooBean::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
        this.sc = sc;
    }

    public boolean invokeMethod(int timeout)  {
        System.out.println("in FooBean::callHello()");

        boolean marked = sc.getRollbackOnly();
        try {
            Context ic = new InitialContext();

            Object objref = ic.lookup("java:comp/env/ejb/hello");

            HelloHome  home = (HelloHome)PortableRemoteObject.narrow
                (objref, HelloHome.class);

            Hello hr = home.create();

            marked = sc.getRollbackOnly();
            System.out.println("Before invocation ctx.getRollbackOnly(): "
                    + marked);

            hr.compute(timeout);

            marked = sc.getRollbackOnly();
            System.out.println("After invocation ctx.getRollbackOnly(): "
                    + marked);
            System.out.println("ctx.getRollbackOnly() = " + marked);
        } catch(Exception e) {
            System.out.println("Exception during invocation: " + e);
        }
        return marked;
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
