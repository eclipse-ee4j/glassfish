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

package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.txscope_synctype_mismatch.ejb;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.SynchronizationType;

@Stateless
public class TC2SlsbWithUnsynchPC implements Tester{

    @PersistenceContext(unitName="lib/unsyncpc_txscope_synctype_mismatch-par.jar#em",
            synchronization = SynchronizationType.UNSYNCHRONIZED)
    EntityManager em;

    @EJB(beanName = "SlsbWithSynchPC")
    Finder finder;

    @Override
    public boolean doTest() {
        try {
            //FIXME: a workaround to initiate JavaEETransactionImpl.txEntityManagerMap,
            //so that the current PC will be associated with the current TX.
            em.find(Person.class, "Tom");

            System.out.println("I am in TC2SlsbWithUnsynchPC.doTest");
            //expect exception thrown from finder.findPerson
            finder.findPerson("Tom");
            System.out.println("method TC2SlsbWithUnsynchPC.findPerson ends with no exception thrown");
            return false;
        } catch(EJBException ejbEx) {
            System.out.println("method TC2SlsbWithUnsynchPC.findPerson ends with EJBException thrown");
            //expect EJBException
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("method TC2SlsbWithUnsynchPC.findPerson ends with unexpected exception thrown");
            //unexpected Exception
            return false;
        }
    }


}
