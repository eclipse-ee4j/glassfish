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

package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.extendedscope_cross_sfsb.ejb;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.SynchronizationType;

@Stateful
public class TC1SfsbWithUnsynchPC implements Tester{
    @PersistenceContext(unitName="lib/unsyncpc_extendedscope_cross_sfsb-par.jar#em",
            synchronization = SynchronizationType.UNSYNCHRONIZED,
            type = PersistenceContextType.EXTENDED)
    EntityManager em;

    @EJB private TC1FinderHome finderHome;
    private TC1Finder finderLocalObject;

    public boolean doTest() {
        try {
            // Should throw EJBException here
            finderLocalObject = finderHome.createFinder();
            System.out.println("Method TC1FinderHome.createFinder invoked without exception thrown");
            finderLocalObject.findPerson("Tom");
            return false;
        } catch (EJBException ejbException) {
            System.out.println("Method TC1FinderHome.createFinder invoked with expected exception thrown");
            // Expected exception
            return true;
        } catch (Exception e) {
            System.out.println("Unexpected exception thrown");
            e.printStackTrace();
            // Unexpected exception
            return false;
        }

    }
}
