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

package com.sun.s1asdev.ejb.ejb30.hello.session3;

import jakarta.ejb.Stateless;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.EntityManager;
import javax.naming.InitialContext;

@Stateless public class HelloEJB implements Hello {

    @PersistenceUnit
        private EntityManagerFactory emf1;

    @PersistenceUnit(name="myemf", unitName="foo")
        private EntityManagerFactory emf2;

    @PersistenceContext
        private EntityManager em1;

    @PersistenceContext(name="myem",
                        unitName="foo", type=PersistenceContextType.EXTENDED)
        private EntityManager em2;

    public void hello() {

        if( (emf1 != null) && (emf2 != null) && (em1 != null) &&
            (em2 != null) ) {

            try {
                InitialContext ic = new InitialContext();

                EntityManagerFactory lookupemf1 = (EntityManagerFactory)
                    ic.lookup("java:comp/env/com.sun.s1asdev.ejb.ejb30.hello.session3.HelloEJB/emf1");

                EntityManagerFactory lookupemf2 = (EntityManagerFactory)
                    ic.lookup("java:comp/env/myemf");

                EntityManager lookupem1 = (EntityManager)
                    ic.lookup("java:comp/env/com.sun.s1asdev.ejb.ejb30.hello.session3.HelloEJB/em1");

                EntityManager lookupem2 = (EntityManager)
                    ic.lookup("java:comp/env/myem");

            } catch(Exception e) {
                throw new jakarta.ejb.EJBException(e);
            }


            System.out.println("HelloEJB successful injection of EMF/EM references!");
        } else {
            throw new jakarta.ejb.EJBException("One or more EMF/EM references" +
                                             " was not injected in HelloEJB");
        }

        System.out.println("In HelloEJB::hello()");
    }

}
