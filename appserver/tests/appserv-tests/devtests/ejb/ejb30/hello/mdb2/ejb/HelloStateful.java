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

package com.sun.s1asdev.ejb.ejb30.hello.mdb2;

import javax.ejb.Stateful;
import javax.ejb.Local;
import javax.ejb.Remove;
import jakarta.annotation.PreDestroy;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.ejb.SessionContext;

@Local({Hello2.class})
@Stateful public class HelloStateful implements Hello2, javax.ejb.SessionBean {

    private String msg;

    public void hello(String s) {
        msg = s;
        System.out.println("HelloStateful: " + s);
    }

    @Remove public void removeMethod() {
        System.out.println("Business method marked with @Remove called in " +
                           msg);
    }
    /*
    @PreDestroy public void myPreDestroyMethod() {
        System.out.println("PRE-DESTROY callback received in " + msg);        
    }
    */

    public void setSessionContext(javax.ejb.SessionContext sc) {
        System.out.println("In HelloStateful:setSessionContext");
        try {
            SessionContext sc2 = (SessionContext) 
                new InitialContext().lookup("java:comp/EJBContext");
            System.out.println("Got SessionContext via java:comp/env/EJBContext");

        } catch(Exception e) {
            throw new EJBException(e);
        }
    }

    public void ejbActivate() {}
    public void ejbPassivate() {}

    public void ejbRemove() {
        System.out.println("In HelloStateful:ejbRemove");
    }
}
