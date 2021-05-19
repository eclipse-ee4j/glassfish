/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.cdi_ejb_jpa;

//Simple TestBean to test CDI.
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

//This bean implements Serializable as it needs to be placed into a Stateful Bean
@Stateless
public class TestBean {
    @PersistenceContext()
    private EntityManager em;

    public void addPerson(String name) {
        Person p = new Person();
        p.setName(name);
        em.persist(p);
    }

    public Person getPerson(Long pid) {
        return em.find(Person.class, pid);
    }

    public void removePerson(Long pid) {
        Person p = getPerson(pid);
        if(p != null) {
            em.remove(p);
        }
    }

    public void removePerson(Person p) {
        em.remove(p);
    }
}
