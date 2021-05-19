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

package com.sun.s1asdev.ejb.classload.lifecycle;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBException;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import javax.xml.rpc.Service;

public class FooBean implements SessionBean {

    private SessionContext sc;

    public FooBean() {}

    public void ejbCreate() {
        System.out.println("In FooBean::ejbCreate()");
        try {
            InitialContext ic = new InitialContext();
            Service service = (Service) ic.lookup("java:comp/env/service/GoogleSearch_ejbCreate");
            System.out.println("Successfully looked up service");
        } catch(Exception e) {
            e.printStackTrace();
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(e);
            throw ejbEx;
        }
    }

    public void setSessionContext(SessionContext sc) {
        System.out.println("In FooBean::setSessionContext()");
        this.sc = sc;
    }

    public void callHello() {
        System.out.println("In FooBean::callHello()");
    }

    public void ejbRemove() {
        System.out.println("In FooBean::ejbRemove()");
        try {
            InitialContext ic = new InitialContext();
            Service service = (Service) ic.lookup("java:comp/env/service/GoogleSearch_ejbRemove");
            System.out.println("Successfully looked up service");
        } catch(Exception e) {
            e.printStackTrace();
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(e);
            throw ejbEx;
        }

    }

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
