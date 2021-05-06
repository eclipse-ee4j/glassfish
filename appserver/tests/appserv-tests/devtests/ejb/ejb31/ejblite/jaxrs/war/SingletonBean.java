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

@Singleton
public class SingletonBean {

    public boolean interceptorWasHere;

    private StatelessBean stateless;
    private StatelessBean stateless2;

    @PostConstruct
    private void init() {

        System.out.println("In SingletonBean:init() ");

        try {
            InitialContext ic = new InitialContext();

            // Lookup simple form of portable JNDI name
             stateless = (StatelessBean)
                ic.lookup("java:module/StatelessBean");

             stateless.hello();

            // Lookup fully-qualified for of portable JNDI name
            stateless2 = (StatelessBean)
                ic.lookup("java:module/StatelessBean!com.acme.StatelessBean");

        } catch(NamingException ne) {
            throw new EJBException(ne);
        }
    }

    public void hello() {
        System.out.println("In SingletonBean:hello()");
        stateless.hello();
        stateless2.hello();
    }

    public void assertInterceptorBinding() {
        if( !interceptorWasHere ) {
            throw new EJBException("interceptor was not here");
        }
    }

    @PreDestroy
    private void destroy() {
        System.out.println("In SingletonBean:destroy()");
    }


}
