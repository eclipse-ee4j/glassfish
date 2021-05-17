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

package com.sun.s1asdev.ejb.ejbflush;

import javax.naming.*;
import jakarta.ejb.*;

public class TestBean implements SessionBean {

    private  A1LocalHome a1Home = null;
    private  A2LocalHome a2Home = null;
    private  A1Local a1bean = null;
    private  A2Local a2bean = null;

    // SessionBean methods

    public void ejbCreate() throws CreateException {
        System.out.println("TestBean ejbCreate");
        try {
            a1Home = lookupA1();
            a2Home = lookupA2();

        } catch (NamingException ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void ejbActivate() {
        System.out.println("TestBean ejbActivate");
    }

    public void ejbPassivate() {
            a1Home = null;
            a2Home = null;
    }

    public void ejbRemove() {}
    public void setSessionContext(SessionContext sc) {}

    private A1LocalHome lookupA1() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/A1FL");
        return (A1LocalHome) objref;
    }

    private A2LocalHome lookupA2() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/A2FL");
        return (A2LocalHome) objref;
    }

    String msg = "Update succeeded with flush enabled";

    public void testA1() {
        try {
           a1bean = a1Home.create("A1");
        } catch (CreateException e) {
           throw new RuntimeException (e.getMessage(), e);
        }

//PG->       a1bean.setName("A12345678901234567890");
    }

    public void testA2() {
        try {
            a2bean = a2Home.create("A2");
        } catch (CreateException e) {
            throw new RuntimeException (e.getMessage());
        }
//PG->        a2bean.setName("A12345678901234567890");
    }

    public void testA1WithFlush() {
        boolean success = true;
        try {
            a1bean = a1Home.create("B1");
            a1bean.setNameWithFlush("A12345678901234567890");
            success = false;
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        if (!success)
            throw new RuntimeException(msg);
    }

    public void testA2WithFlush() {
        boolean success = true;
        try {
            a2bean = a2Home.create("B2");
            a2bean.setNameWithFlush("A12345678901234567890");
            success = false;
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        if (!success)
            throw new RuntimeException(msg);
    }

}
