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

package com.sun.s1asdev.ejb.allowedmethods.ctxcheck;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBException;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;


public class DriverBean
    implements SessionBean
{

    private SessionContext sc;

    public DriverBean() {
    }

    public void ejbCreate() throws RemoteException {
        System.out.println("In DriverBean::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
        this.sc = sc;
    }

    public void localSlsbGetEJBObject() {
        try {
            Context ic = new InitialContext();
            HereLocalHome localHome = (HereLocalHome) ic.lookup("java:comp/env/ejb/HereLocal");

            HereLocal local = (HereLocal) localHome.create();
            local.doSomethingHere();
            local.accessEJBObject();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localSlsbGetEJBLocalObject() {
        try {
            Context ic = new InitialContext();
            HereLocalHome localHome = (HereLocalHome) ic.lookup("java:comp/env/ejb/HereLocal");

            HereLocal local = (HereLocal) localHome.create();

            local.doSomethingHere();
            local.accessEJBLocalObject();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localSlsbGetEJBLocalHome() {
        try {
            Context ic = new InitialContext();
            HereLocalHome localHome = (HereLocalHome) ic.lookup("java:comp/env/ejb/HereLocal");

            HereLocal local = (HereLocal) localHome.create();

            local.doSomethingHere();
            local.accessEJBLocalHome();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localSlsbGetEJBHome() {
        try {
            Context ic = new InitialContext();
            HereLocalHome localHome = (HereLocalHome) ic.lookup("java:comp/env/ejb/HereLocal");

            HereLocal local = (HereLocal) localHome.create();

            local.doSomethingHere();
            local.accessEJBHome();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localEntityGetEJBObject() {
        try {
            Context ic = new InitialContext();
            LocalEntityHome localHome = (LocalEntityHome) ic.lookup("java:comp/env/ejb/LocalEntity");

            localHome.create("5", "5");
            LocalEntity local = (LocalEntity) localHome.findByPrimaryKey("5");
            local.localEntityGetEJBObject();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localEntityGetEJBLocalObject() {
        try {
            Context ic = new InitialContext();
            LocalEntityHome localHome = (LocalEntityHome) ic.lookup("java:comp/env/ejb/LocalEntity");

            localHome.create("6", "6");
            LocalEntity local = (LocalEntity) localHome.findByPrimaryKey("6");
            local.localEntityGetEJBLocalObject();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localEntityGetEJBLocalHome() {
        try {
            Context ic = new InitialContext();
            LocalEntityHome localHome = (LocalEntityHome) ic.lookup("java:comp/env/ejb/LocalEntity");

            localHome.create("7", "7");
            LocalEntity local = (LocalEntity) localHome.findByPrimaryKey("7");
            local.localEntityGetEJBLocalHome();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localEntityGetEJBHome() {
        try {
            Context ic = new InitialContext();
            LocalEntityHome localHome = (LocalEntityHome) ic.lookup("java:comp/env/ejb/LocalEntity");

            localHome.create("8", "8");
            LocalEntity local = (LocalEntity) localHome.findByPrimaryKey("8");
            local.localEntityGetEJBHome();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}

}
