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

package com.sun.s1asdev.ejb31.timer.nonpersistenttimer;

import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.*;


public class FooEJB extends TimerStuffImpl implements SessionBean, TimedObject {
    private SessionContext sc;

    public FooEJB(){
    }

    public void ejbTimeout(Timer t) {
        checkCallerSecurityAccess("ejbTimeout", false);

        try {
            System.out.println("In FooEJB::ejbTimeout --> " + t.getInfo());
            if (t.isPersistent())
                throw new RuntimeException("FooEJB::ejbTimeout -> "
                       + t.getInfo() + " is PERSISTENT!!!");
        } catch(RuntimeException e) {
            System.out.println("got exception while calling getInfo");
            throw e;
        }

        try {
            handleEjbTimeout(t);
        } catch(RuntimeException re) {
            throw re;
        } catch(Exception e) {
            System.out.println("handleEjbTimeout threw exception");
            e.printStackTrace();
        }

    }

    public void setSessionContext(SessionContext sc) {
        this.sc = sc;
        setContext(sc);

        System.out.println("In FooEJB::setSessionContext");

        checkCallerSecurityAccess("setSessionContext", false);

        getTimerService("setSessionContext", false);
        doTimerStuff("setSessionContext", false);
    }

    public void ejbCreate() throws EJBException {
        System.out.println("In ejbnonpersistenttimer.Foo::ejbCreate !!");
        setupJmsConnection();
        checkGetSetRollbackOnly("ejbCreate", false);
        checkCallerSecurityAccess("ejbCreate", false);

        getTimerService("ejbCreate", true);
        doTimerStuff("ejbCreate", false);
    }

    public void ejbRemove() throws EJBException {
        System.out.println("In FooEJB::ejbRemove");
        checkCallerSecurityAccess("ejbRemove", false);
        checkGetSetRollbackOnly("ejbRemove", false);
        getTimerService("ejbRemove", true);
        doTimerStuff("ejbRemove", false);
        cleanup();
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }



}
