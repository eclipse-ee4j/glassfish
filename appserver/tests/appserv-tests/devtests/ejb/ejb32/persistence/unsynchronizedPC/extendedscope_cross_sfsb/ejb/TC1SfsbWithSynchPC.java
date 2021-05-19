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

import jakarta.ejb.Init;
import jakarta.ejb.LocalHome;
import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.SynchronizationType;

import com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.extendedscope_cross_sfsb.ejb.Person;


@Stateful
@LocalHome(TC1FinderHome.class)
public class TC1SfsbWithSynchPC {
    @PersistenceContext(unitName="lib/unsyncpc_extendedscope_cross_sfsb-par.jar#em",
            synchronization = SynchronizationType.SYNCHRONIZED,
            type = PersistenceContextType.EXTENDED)
    EntityManager em;

    //@Override
    public Person findPerson(String name) {
        Person p = em.find(Person.class, name);
        System.out.print("Find persion " + p);
        return p;
    }

    @Init
    public void createFinder() {

    }

}
