/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "EMPLOYEE")
public class Employee implements java.io.Serializable {

    private int id;
    private String firstName;
    private String lastName;
    private Department department;

    public Employee() {
    }

    public Employee(int id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Employee(int id, String firstName, String lastName, Department department) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
    }

    // ===========================================================
    // getters and setters for the state fields
    
    @Id
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "FIRSTNAME")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Column(name = "LASTNAME")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // ===========================================================
    // getters and setters for the association fields
    // @ManyToOne
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENT_ID")
    public Department getDepartment() {
        return department;
    }

    @Transient
    public Department getDepartmentNoWeaving() {
        try {
            java.lang.reflect.Field f = Employee.class.getDeclaredField("department");
            return (Department) f.get(this);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Please change argument to getDeclaredField", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String toString() {
        return 
            "Employee id=" + getId() + 
            ", firstName=" + getFirstName() + 
            ", lastName=" + getLastName() + 
            ", department=" + getDepartment();
    }

}
