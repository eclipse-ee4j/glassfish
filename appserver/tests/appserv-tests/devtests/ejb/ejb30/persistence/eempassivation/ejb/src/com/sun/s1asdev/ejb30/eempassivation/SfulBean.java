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

package com.sun.s1asdev.ejb30.eempassivation;

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
@EJB(name = "ejb/SfulBean", beanInterface = com.sun.s1asdev.ejb30.eempassivation.SfulDelegate.class)
public class SfulBean implements Sful {
    private String name;

    private @EJB
    Sless sless;

    private @PersistenceContext(unitName = "lib/ejb-ejb30-persistence-eempassivation-par1.jar#em", type = PersistenceContextType.EXTENDED)
    EntityManager extendedEM;

    private SfulDelegate delegate;

    private Map<String, Boolean> testResultsMap = new HashMap<String, Boolean>();

    public void setName(String name) {
        this.name = name;
        try {
            String lookupName = "java:comp/env/ejb/SfulBean";
            InitialContext initCtx = new InitialContext();
            delegate = (SfulDelegate) initCtx.lookup(lookupName);

            System.out.println("**EEM Delegate: " + extendedEM.getDelegate().getClass().getName());
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    public Map<String, Boolean> doTests(String prefix) {
        performTests(prefix, testResultsMap);

        return testResultsMap;
    }

    public void createSfulDelegates() {
        for (int i = 0; i < 640; i++) {
            SfulDelegate sfulTemp = sless.createSfulDelegate();
        }
        sleepForSeconds(10);
    }

    private void performTests(String prefix, Map<String, Boolean> map) {
        String personName = prefix + name;
        Person person = new Person(personName);
        person.data = "data: " + personName;
        String delegateName = "delgname_" + personName;
        String delegateData = "delgdata: " + personName;
        delegate.create(delegateName, delegateData);
        Person dPerson = extendedEM.find(Person.class, delegateName);
        extendedEM.persist(person);
        Person foundPerson = delegate.find(personName);
        boolean delegateRemovedMe = delegate.remove(personName);
        boolean removedDelegate = removePerson(delegateName);

        map.put(prefix + "findDelegateCreatedPerson", (dPerson != null));
        map.put(prefix + "delegateFoundMe", (foundPerson != null));
        map.put(prefix + "delegateRemovedMe", delegateRemovedMe);
        map.put(prefix + "removedDelegate", removedDelegate);
        map.put(prefix + "passCount" + delegate.getPassivationCount(), true);
        map.put(prefix + "actCount" + delegate.getActivationCount(), true);
    }

    Person findPerson() {
        Person p = extendedEM.find(Person.class, name);
        System.out.println("Found " + p);
        return p;
    }

    boolean removePerson(String personName) {
        Person p = extendedEM.find(Person.class, personName);
        boolean removed = false;
        if (p != null) {
            extendedEM.remove(p);
            removed = true;
        }
        return removed;
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
