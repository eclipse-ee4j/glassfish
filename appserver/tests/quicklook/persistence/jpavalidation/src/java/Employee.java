/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package myapp;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.util.Collection;

@Entity
@Table(name="BV_EMPL")
public class Employee {

    private int         id;
    private String  name;
    private long salary;
    private Collection<Project> projects;


    // ===========================================================
    // constructor
    public Employee() {}

    public Employee(int id, String name, long salary) {
        this.id = id;
        this.name = name;
        this.salary = salary;
    }

    // ===========================================================
    // getters and setters for the state fields
    @Id
    @Column(name="ID")
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    @Size(max = 5)
    @Column(length=20, name="NAME")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Column(name="SALARY")
    public long getSalary() {
        return salary;
    }
    public void setSalary(long salary) {
        this.salary = salary;
    }

    // ===========================================================
    // relationship
    @ManyToMany(mappedBy = "employees", cascade = CascadeType.ALL)
    public Collection<Project> getProjects() {
        return projects;
    }
    public void setProjects(Collection<Project> projects) {
        this.projects = projects;
    }

    // ===========================================================
    @PrePersist
    void m1() {
        System.out.println("PrePersist m1() called for Employee" + this);
    }

    @PreUpdate
    void m2() {
        System.out.println("PreUpdate m2() called for Employee" + this);
    }

    @PreRemove
    void m3() {
        System.out.println("PreRemove m3() called for Employee" + this);
    }

    // ===========================================================
    @Override
    public String toString() {
        return "Employee {Id:" +  id + " name:" + name + "}";
    }

}



