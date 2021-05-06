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

import javax.naming.InitialContext;

import javax.management.j2ee.ManagementHome;

public class SFSB implements Hello {

    private EntityManager em;

    public void init() {
        System.out.println("In SFSB::init()");
        FooEntity fe = new FooEntity("BAR");
        em.persist(fe);
        System.out.println("Done SFSB::init()");
    }

    public String test(String value, int count) throws EJBException {
        System.out.println("In SFSB::test()");
        Query q = em.createQuery("SELECT f FROM FooEntity f WHERE f.name=:name");
        q.setParameter("name", value);
        java.util.List result = q.getResultList();
        if (result.size() != count)
            throw new EJBException("ERROR: Found " + result.size() + " FooEntity named " + value + ", not expected " + count);

        return "Found " + result.size() + " FooEntity named " + value;
    }

    public void testRemove() {
        System.out.println("In SFSB::testRemove()");
    }

    public void destroy() {
        System.out.println("In SFSB::destroy()");
        try {
            jakarta.transaction.TransactionSynchronizationRegistry r = (jakarta.transaction.TransactionSynchronizationRegistry)
                   new InitialContext().lookup("java:comp/TransactionSynchronizationRegistry");
            if (r.getTransactionStatus() != jakarta.transaction.Status.STATUS_ACTIVE) {
                throw new IllegalStateException("Transaction status is not STATUS_ACTIVE: " + r.getTransactionStatus());
            }
            FooEntity fe = new FooEntity("FOO");
            em.persist(fe);
            System.out.println("Done SFSB::destroy()");
        } catch(Exception e) {
            throw new EJBException(e);
        }

    }



}
