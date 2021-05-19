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

package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.disallowed_methods.ejb;

import jakarta.persistence.*;

@Entity
@Table(name = "EJB32_PERSISTENCE_CONTEXT_PERSON")
public class Person implements java.io.Serializable {

    @Id
    private int id;

    private String name;

    public Person() {
    }

    public Person(String name) {
        this.name = name;
    }

    public Person(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Person: {" + "(name = " + name + ")}";
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
