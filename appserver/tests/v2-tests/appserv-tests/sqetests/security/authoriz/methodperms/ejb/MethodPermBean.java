/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.security.authoriz.methodperms;

import java.io.Serializable;
import java.rmi.RemoteException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;

public class MethodPermBean implements SessionBean {
    private String str;
    private SessionContext sc;

    public MethodPermBean(){}

    public void ejbCreate(String str) throws RemoteException {

        System.out.println("In ejbCreate !!");
        this.str = str;

    }

    public String authorizedMethod() throws RemoteException {

        System.out.println("MethodPerm Bean - inside authorizedMethod() !");
        System.out.println("CALLER PRINCIPAL: " + sc.getCallerPrincipal());
        boolean mgr = sc.isCallerInRole("MGR");
        boolean admin = sc.isCallerInRole("ADMIN");
        boolean staff =  sc.isCallerInRole("STAFF");
        boolean emp = sc.isCallerInRole("EMP");
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("\nIs CallerInRole: MGR(shud be true) = " + mgr);
        sbuf.append("\nIs CallerInRole: ADMIN(shud b false) = " + admin);
        sbuf.append("\nIs CallerInRole: STAFF(shud b true) = " + staff);
        sbuf.append("\nIs CallerInRole: EMP(shud b true) = " + emp);
        System.out.println (sbuf.toString());
        if(mgr &&  !admin && staff && emp)
            return sbuf.toString();
        else
            throw new RemoteException("Caller in role failed ");

    }

    public String authorizedMethod(String s, int i) throws RemoteException {

        System.out.println("MethodPerm Bean - inside authorizedMethod(String s, int i)!!");
        //System.out.println("Is CallerInRole: " + sc.isCallerInRole("MGR"));
        //System.out.println("Is CallerInRole: " + sc.isCallerInRole("STAFF"));
        //System.out.println("Is CallerInRole: " + sc.isCallerInRole("EMP"));
        //System.out.println("Is CallerInRole: " + sc.isCallerInRole("ADMIN"));
        return str + " " + s + " " + i;

    }

    public String authorizedMethod(int i) throws RemoteException {

        System.out.println("MethodPerm Bean - inside authorizedMethod(int i)!!!");
        return str + " " + i;

    }

    public void unauthorizedMethod() throws RemoteException {

        System.out.println("MethodPerm Bean - inside unauthorized method!!!!");

    }

    public String sayGoodbye() throws RemoteException {

        System.out.println("MethodPerm Bean - inside sayGoodbye()!!!!!");
        return str + " sayGoodbye";

    }

    public void setSessionContext(SessionContext sc) {

        this.sc = sc;

    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
