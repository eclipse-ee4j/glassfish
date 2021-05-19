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

package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.use_nonjtadatasource.ejb;

import java.util.Map;

import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.SynchronizationType;


@Stateful
public class SfulBean {
    @PersistenceContext(unitName="lib/unsynchpc_use_nonjtadatasource-par.jar#em",
            type=PersistenceContextType.EXTENDED,
            synchronization=SynchronizationType.UNSYNCHRONIZED)
    EntityManager em;

    public Person testUsingNonJTADataSource(Map<String, Boolean> resultMap) {
        Person p = em.find(Person.class, 1);
        System.out.println("I am in testUsingNonJTADataSource, and person name is " + p.getName());
        resultMap.put("equalsCurrentName", p.getName().equals("currentName"));
        return p;
    }

    public boolean testRollBackDoesNotClearUnsynchPC(Person person) {
         return (em.contains(person) && person.getName() != "newName");
    }
}
