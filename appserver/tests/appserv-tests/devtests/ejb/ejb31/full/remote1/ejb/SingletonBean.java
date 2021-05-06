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

package com.acme;

import jakarta.ejb.*;
import jakarta.annotation.*;

import javax.naming.*;

@Singleton
@Startup
public class SingletonBean {

    @EJB Hello hello;
    @EJB HelloHome helloHome;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");

        try {
            HelloRemote hr = helloHome.create();
            System.out.println("HellohelloRemote.hello() says " + hr.hello());

            System.out.println("Hello.hello() says " + hello.hello());

            InitialContext ic = new InitialContext();

            SessionContext ctx = (SessionContext)
                ic.lookup("java:module/env/sesCtx");

            SingletonBean me = (SingletonBean)
                ic.lookup("java:app/ejb-ejb31-full-remote1-ejb/SingletonBean");

            SingletonBean meToo = (SingletonBean)
                ic.lookup("java:app/ejb-ejb31-full-remote1-ejb/SingletonBean!com.acme.SingletonBean");

            Hello m1 = (Hello) ic.lookup("java:module/env/M1");

            HelloHome m2 = (HelloHome) ctx.lookup("java:module/M2");

            Hello a1 = (Hello) ctx.lookup("java:app/env/A1");

            HelloHome a2 = (HelloHome) ic.lookup("java:app/A2");

            try {
                ic.lookup("java:comp/env/C1");
                throw new EJBException("Expected exception accessing private component environment entry of HelloBean");
            } catch(NamingException e) {
                System.out.println("Successfully did *not* find HelloBean private component environment dependency");
            }

            try {
                ic.lookup("java:comp/C1");
                throw new EJBException("Expected exception accessing private component environment entry of HelloBean");
            } catch(NamingException e) {
                System.out.println("Successfully did *not* find HelloBean private component environment dependency");
            }

            System.out.println("My AppName = " +
                               ctx.lookup("java:app/AppName"));

            System.out.println("My ModuleName = " +
                               ctx.lookup("java:module/ModuleName"));



        } catch(Exception e) {
            throw new EJBException("singleton init error" , e);
        }

    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
