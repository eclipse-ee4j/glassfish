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

package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.rollback_notclear_unsynchPC.ejb;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.SynchronizationType;
import jakarta.transaction.UserTransaction;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class SlessBean implements Tester{
    @PersistenceContext(unitName="lib/unsynchpc_rollback_notclear_unsynchPC-par.jar#em",
            synchronization=SynchronizationType.UNSYNCHRONIZED)
    EntityManager em;

    @Override
    public boolean doTest() {
        System.out.println("I am in SlessBean.doTest");

        UserTransaction utx = null;
        try {
            utx = (UserTransaction)(new javax.naming.InitialContext()).lookup("java:comp/UserTransaction");
            utx.begin();

            Person person = new Person("Tom");
            em.persist(person);

            //Let unsynchronized PC join the transaction;
            em.joinTransaction();

            System.out.println("Does PC contain person before rollback: " + em.contains(person));
            utx.rollback();
            System.out.println("Does PC contain person after rollback: " + em.contains(person));

            return !(em.contains(person));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
