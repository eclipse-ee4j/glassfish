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
import jakarta.persistence.*;
import jakarta.annotation.*;
import org.omg.CORBA.ORB;

import javax.naming.InitialContext;

import javax.management.j2ee.ManagementHome;

@Singleton
@Remote(Hello.class)
    @EJB(name="mejb", beanInterface=javax.management.j2ee.ManagementHome.class, mappedName="ejb/mgmt/MEJB")
@Startup
public class SingletonBean {

    @Resource
    private ORB orb;

    @PersistenceContext
    private EntityManager em;

    /*Object returned from IIOP_OBJECT_FACTORY is still ior
    @EJB(mappedName="ejb/mgmt/MEJB")
    ManagementHome mHome1;
    */

    /* Object returned from IIOP_OBJECT_FACTORY is still ior
    @EJB(lookup="java:global/mejb/MEJBBean")
    ManagementHome mHome2;
    */

    /* Doesn't work b/c actual MEJB app Home interface is new glassfish
     * type, so actual type derived from field declaration is tacked
     * onto mappedName and results in NameNotFound
    @EJB(mappedName="java:global/mejb/MEJBBean")
    ManagementHome mHome2;
    */




    //MEJBHome mHome2;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
        System.out.println("orb = " + orb);
        if( orb == null ) {
            throw new EJBException("null ORB");
        }
        try {
            // same problem ManagementHome mHomeL = (ManagementHome) new InitialContext().lookup("java:comp/env/mejb");
            // same problem ManagementHome mHomeL2 = (ManagementHome) new InitialContext().lookup("java:global/mejb/MEJBBean");
            // System.out.println("mHomeL = " + mHomeL);
            // System.out.println("mHomeL2 = " + mHomeL2);
        } catch(Exception e) {
            throw new EJBException(e);
        }

        // System.out.println("mHome1 = " + mHome1);
        //        System.out.println("mHome2 = " + mHome2);

    }

    public String hello() {
        System.out.println("In SingletonBean::hello()");
        return "hello, world!\n";
    }

    public void test_Err_or(String s1, String s2) {
        throw new Error("test java.lang.Error");
    }

    @Asynchronous
    public void async() {
        FooEntity fe = new FooEntity("ASYNC");
            em.persist(fe);

    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
        try {
            jakarta.transaction.TransactionSynchronizationRegistry r = (jakarta.transaction.TransactionSynchronizationRegistry)
                   new InitialContext().lookup("java:comp/TransactionSynchronizationRegistry");
            if (r.getTransactionStatus() != jakarta.transaction.Status.STATUS_ACTIVE) {
                throw new IllegalStateException("Transaction status is not STATUS_ACTIVE: " + r.getTransactionStatus());
            }
            FooEntity fe = new FooEntity("FOO");
            em.persist(fe);
        } catch(Exception e) {
            throw new EJBException(e);
        }

    }



}
