/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.app.persistence.inject.factory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;

import java.io.PrintWriter;

public class JakartaPersistenceTest {

    private EntityManager entityManager;
    private UserTransaction transaction;
    PrintWriter out;
    private static Department departments[] = new Department[2];
    private static Employee employees[] = new Employee[5];

    public JakartaPersistenceTest() {
    }

    public JakartaPersistenceTest(EntityManager em, UserTransaction utx, PrintWriter out) {
        this.entityManager = em;
        this.transaction = utx;
        this.out = out;
    }

    protected boolean lazyLoadingInit() {
        boolean status = false;
        out.println("-----lazeLoadingInit()---------");
        try {
            departments[0] = new Department(1, "Engineering");
            departments[1] = new Department(2, "Marketing");

            transaction.begin();
            entityManager.joinTransaction();
            for (int i = 0; i < 2; i++) {
                entityManager.persist(departments[i]);
            }
            transaction.commit();

            employees[0] = new Employee(1, "Alan", "Frechette", departments[0]);
            employees[1] = new Employee(2, "Arthur", "Wesley", departments[0]);
            employees[2] = new Employee(3, "Abe", "White", departments[0]);
            employees[3] = new Employee(4, "Paul", "Hinz", departments[1]);
            employees[4] = new Employee(5, "Carla", "Calrson", departments[1]);

            transaction.begin();
            entityManager.joinTransaction();
            for (int i = 0; i < 5; i++) {
                entityManager.persist(employees[i]);
            }
            transaction.commit();

            status = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        out.println("-----status = " + status + "---------");
        return status;
    }

    public boolean lazyLoadingByFind(int employeeID) {

        boolean status = true;
        out.println("------------lazyLoadingAfterFind -----------");
        out.println("employeeID = " + employeeID);
        Employee emp = entityManager.find(Employee.class, employeeID);

        out.println("found: emp.id=" + emp.getId());

        try {
            // 1. get Department before loading
            Department deptBL = emp.getDepartmentNoWeaving();
            out.println("1. before loading: deptBL=" + deptBL);
            String deptNameBL = null;
            if (deptBL != null) {
                deptNameBL = deptBL.getName();
                out.println("deptNameBL=" + deptNameBL);
            }
            // assert deptBL == null;
            if (deptBL != null) {
                status = false;
            }

            // 2. loading
            String deptName = emp.getDepartment().getName();
            out.println("2. loading, deptName = " + deptName);

            // 3. get Department after loading
            Department deptAL = emp.getDepartmentNoWeaving();
            out.println("3. after loading: deptAL=" + deptAL);
            String deptNameAL = deptAL.getName();
            System.out.println("deptNameAL=" + deptNameAL);
            // assert deptAL != null
            // assert deptAL.getName == deptName;
            if (deptAL == null || deptNameAL != deptName) {
                status = false;
            }
        } catch (Exception ex) {
            status = false;
            ex.printStackTrace();
        }

        out.println("-----status = " + status + "---------");
        return status;
    }

    public boolean lazyLoadingByQuery(String fName) {

        boolean status = true;
        out.println("------------lazyLoadingByQuery -----------");
        out.println("fName = " + fName);
        Query query = entityManager.createQuery("SELECT e FROM Employee e WHERE e.firstName like :firstName").setParameter("firstName", fName);
        ;
        Employee emp = (Employee) query.getSingleResult();

        out.println("queried: emp.firstName=" + emp.getFirstName());

        try {
            // 1. get Department before loading
            Department deptBL = emp.getDepartmentNoWeaving();
            out.println("1. before loading: deptBL=" + deptBL);
            String deptNameBL = null;
            if (deptBL != null) {
                deptNameBL = deptBL.getName();
                out.println("deptNameBL=" + deptNameBL);
            }
            // assert deptBL == null;
            if (deptBL != null) {
                status = false;
            }

            // 2. loading
            String deptName = emp.getDepartment().getName();
            System.out.println("2. loading, deptName = " + deptName);

            // 3. get Department after loading
            Department deptAL = emp.getDepartmentNoWeaving();
            out.println("3. after loading: deptAL=" + deptAL);
            String deptNameAL = deptAL.getName();
            out.println("deptNameAL=" + deptNameAL);
            // assert deptAL != null
            // assert deptAL.getName == deptName;
            if (deptAL == null || deptNameAL != deptName) {
                status = false;
            }
        } catch (Exception ex) {
            status = false;
            ex.printStackTrace();
        }
        out.println("-----status = " + status + "---------");
        return status;
    }

}
