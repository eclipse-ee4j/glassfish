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

package com.sun.s1asdev.ejb.ejb30.persistence.context;

import jakarta.persistence.*;

@Entity
@NamedQuery(name="findPersonByName",
            query="SELECT OBJECT(p) FROM Person p WHERE p.name LIKE :pName")
@SqlResultSetMapping(name="PersonSqlMapping",
                     entities=@EntityResult(entityClass=Person.class))
@Table(name="EJB30_PERSISTENCE_CONTEXT_PERSON")

public class Person implements java.io.Serializable {

    @Id String name;

    public Person(){
    }

    public Person(String name){
        this.name = name;
    }

    @Override public String toString(){
        return "Person: {"+"(name = "+name+")}";
    }

    public String getName() {
        return name;
    }
}

