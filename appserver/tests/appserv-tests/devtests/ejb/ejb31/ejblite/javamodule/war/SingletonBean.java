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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;

import jakarta.ejb.SessionContext;

@Singleton
@LocalBean
public class SingletonBean implements java.util.Observer {


    @EJB private  SingletonBean me;

    @EJB private  StatefulBean sf;

    @Resource private SessionContext sessionCtx;

    private SingletonBean sb2;
    private SingletonBean sb3;
    private SingletonBean sb4;
    private SingletonBean sb5;
    private StatelessBean slsb;
    private StatelessBean slsb2;
    private StatelessBean slsb3;
    private StatelessBean slsb4;
    private StatelessBean slsb5;

    public void update(java.util.Observable a,java.lang.Object b)  {}

    @PostConstruct
    private void init() {

        System.out.println("In SingletonBean:init() me = " + me);

        if( sessionCtx.getContextData() == null ) {
            throw new EJBException("null context data");
        }

        try {
            InitialContext ic = new InitialContext();

            // Lookup simple form of portable JNDI name
            StatelessBean stateless = (StatelessBean)
                ic.lookup("java:module/StatelessBean");

            stateless.hello();

            // Lookup fully-qualified form of portable JNDI name
            StatelessBean stateless2 = (StatelessBean)
                ic.lookup("java:module/StatelessBean!com.acme.StatelessBean");

            stateless2.hello();

            sb2 = (SingletonBean) ic.lookup("java:module/SingletonBean");
            sb3 = (SingletonBean) ic.lookup("java:module/SingletonBean!com.acme.SingletonBean");

            sb4 = (SingletonBean) ic.lookup("java:module/ES1");
            sb5 = (SingletonBean) ic.lookup("java:module/env/ES2");

            slsb = (StatelessBean) ic.lookup("java:module/StatelessBean");
            slsb2 = (StatelessBean) ic.lookup("java:app/ejb-ejb31-ejblite-javamodule-web/StatelessBean");
            slsb3 = (StatelessBean) ic.lookup("java:app/ejb-ejb31-ejblite-javamodule-web/StatelessBean!com.acme.StatelessBean");

            slsb4 = (StatelessBean) ic.lookup("java:app/EL1");
            slsb5 = (StatelessBean) ic.lookup("java:app/env/EL2");

            System.out.println("My AppName = " +
                               ic.lookup("java:app/AppName"));

            System.out.println("My ModuleName = " +
                               ic.lookup("java:module/ModuleName"));

        } catch(NamingException ne) {
            throw new EJBException(ne);
        }
    }

    public void hello() {
        System.out.println("In SingletonBean:hello()");
        if( sessionCtx.getContextData() == null ) {
            throw new EJBException("null context data");
        }

    }

    @PreDestroy
    private void destroy() {
        System.out.println("In SingletonBean:destroy()");
        if( sessionCtx.getContextData() == null ) {
            throw new EJBException("null context data");
        }

    }


}
