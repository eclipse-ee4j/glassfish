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

package com.sun.s1asdev.ejb30.eemsfsbpassivation;

import java.util.Map;
import java.util.HashMap;
import javax.naming.InitialContext;
import jakarta.ejb.Stateful;
import jakarta.ejb.EJB;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.EJBException;

@Stateful
public class SfulBean implements Sful {
    private String name;

    private @PersistenceContext(unitName = "lib/ejb-ejb30-persistence-eemsfsbpassivation-par1.jar#em", type = PersistenceContextType.EXTENDED)
    EntityManager extendedEM;

    private Map<String, Boolean> testResultsMap = new HashMap<String, Boolean>();

    public void setName(String name) {
        this.name = name;
        try {
            String lookupName = "java:comp/env/ejb/SfulBean";
            InitialContext initCtx = new InitialContext();

            Person person = new Person(name);
            person.data = "data: " + name;
            extendedEM.persist(person);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    public Map<String, Boolean> doTests(String prefix) {
        Person p = extendedEM.find(Person.class, name);
        testResultsMap.put("find" + prefix + "Activate", (p != null));

        return testResultsMap;
    }

    private void sleepForSeconds(int time) {
        try {
            while (time-- > 0) {
                // System.out.println("Sleeping... " + time + " seconds to
                // go...");
                Thread.currentThread().sleep(1000);
            }
        } catch (InterruptedException inEx) {
        }
    }
}
