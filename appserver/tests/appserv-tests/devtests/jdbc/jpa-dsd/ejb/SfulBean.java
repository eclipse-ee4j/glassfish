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

package com.sun.s1asdev.ejb.ejb30.persistence.tx_propagation;

import java.util.Map;
import java.util.HashMap;

import javax.naming.InitialContext;

import jakarta.ejb.Stateful;
import jakarta.ejb.EJB;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityNotFoundException;
import jakarta.annotation.sql.*;

import jakarta.persistence.EntityManager;

import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.EJBException;

@DataSourceDefinition(name = "java:app/jdbc/xa",
                        className = "org.apache.derby.jdbc.ClientXADataSource",
                        minPoolSize = 0,
                        initialPoolSize = 0,
                        user = "dbuser",
                        password = "dbpassword",
                        databaseName = "testdb",
                        properties = {"connectionAttributes=;create=true"}

                )
@Stateful
@EJB(name="ejb/SfulBean",
        beanInterface=com.sun.s1asdev.ejb.ejb30.persistence.tx_propagation.SfulDelegate.class)

public class SfulBean
    implements Sful {

    private String name;

    private @EJB SfulDelegate delegate;

    private @PersistenceContext(unitName="lib/ejb-ejb30-persistence-tx_propagation-par1.jar#em",
                type=PersistenceContextType.EXTENDED)
            EntityManager extendedEM;

    public void setName(String name) {
        this.name = name;
        try {
            String lookupName = "java:comp/env/ejb/SfulBean";

            InitialContext initCtx = new InitialContext();
            delegate = (SfulDelegate) initCtx.lookup(lookupName);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    public Map<String, Boolean> doTests() {
        Person person = new Person(name);

        String delegateName = "delgname_" + name;
        String delegateData= "delgdata: " + name;
        delegate.create(delegateName, delegateData);

        Person dPerson = extendedEM.find(Person.class, delegateName);

        extendedEM.persist(person);
        Person foundPerson = delegate.find(name);


        Map<String, Boolean> map = new HashMap<String, Boolean>();
        map.put("findDelegateCreatedPerson", (dPerson != null));
        map.put("delegateFoundMe", (foundPerson != null));

        // See https://glassfish.dev.java.net/issues/show_bug.cgi?id=3867
        extendedEM.flush();

        return map;
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

}
