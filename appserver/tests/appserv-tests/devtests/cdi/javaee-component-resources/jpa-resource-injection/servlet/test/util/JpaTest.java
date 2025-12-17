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

package test.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;

import test.entity.Department;
import test.entity.Employee;

import static java.lang.System.Logger;
import static java.lang.System.Logger.Level.INFO;

public class JpaTest {
    
    private static Logger LOGGER = System.getLogger("JpaTest");

    private EntityManager em;
    private UserTransaction utx;

    private static Department deptRef[] = new Department[2];
    private static Employee empRef[] = new Employee[5];

    public JpaTest() {
    }

    public JpaTest(EntityManager em, UserTransaction utx) {
        this.em = em;
        this.utx = utx;
    }

    public boolean lazyLoadingInit() {
        boolean status = false;
        LOGGER.log(INFO, "-----lazeLoadingInit()---------");
        try {
            deptRef[0] = new Department(1, "Engineering");
            deptRef[1] = new Department(2, "Marketing");
            
            utx.begin();
            em.joinTransaction();
            for (int i = 0; i < 2; i++) {
                em.persist(deptRef[i]);
            }
            utx.commit();

            empRef[0] = new Employee(1, "Alan", "Frechette", deptRef[0]);
            empRef[1] = new Employee(2, "Arthur", "Wesley", deptRef[0]);
            empRef[2] = new Employee(3, "Abe", "White", deptRef[0]);
            empRef[3] = new Employee(4, "Paul", "Hinz", deptRef[1]);
            empRef[4] = new Employee(5, "Carla", "Calrson", deptRef[1]);
            
            utx.begin();
            em.joinTransaction();
            for (int i = 0; i < 5; i++) {
                em.persist(empRef[i]);
            }
            utx.commit();
            
            status = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        LOGGER.log(INFO, "-----status = " + status + "---------");
        return status;
    }

    public boolean lazyLoadingByQuery(String fName) {

        boolean status = true;
        LOGGER.log(INFO, "------------lazyLoadingByQuery -----------");
        LOGGER.log(INFO, "fName = " + fName);
        Query query = em.createQuery("SELECT e FROM Employee e WHERE e.firstName like :firstName")
                        .setParameter("firstName", fName);
        
        Employee emp = (Employee) query.getSingleResult();

        LOGGER.log(INFO, "queried: emp.firstName=" + emp.getFirstName());

        try {
            // 1. get Department before loading
            Department deptBL = emp.getDepartmentNoWeaving();
            LOGGER.log(INFO, "1. before loading: deptBL=" + deptBL);
            
            String deptNameBL = null;
            if (deptBL != null) {
                deptNameBL = deptBL.getName();
                LOGGER.log(INFO, "deptBL is not null, but should be null! deptNameBL=" + deptNameBL);
                
                status = false;
                
                LOGGER.log(INFO, "Setting status to false");
            }

            // 2. loading
            String deptName = emp.getDepartment().getName();
            LOGGER.log(INFO, "2. loading, deptName = " + deptName);

            // 3. get Department after loading
            Department deptAL = emp.getDepartmentNoWeaving();
            LOGGER.log(INFO, "3. after loading: deptAL=" + deptAL);
            String deptNameAL = deptAL.getName();
            LOGGER.log(INFO, "deptNameAL=" + deptNameAL);
            
            // assert deptAL != null
            // assert deptAL.getName == deptName;
            if (deptAL == null || deptNameAL != deptName) {
                status = false;
            }
        } catch (Exception ex) {
            status = false;
            ex.printStackTrace();
        }
        
        LOGGER.log(INFO, "-----status = " + status + "---------");
        return status;
    }
    
    public boolean lazyLoadingByFind(int employeeID) {

        boolean status = true;
        LOGGER.log(INFO, "------------lazyLoadingAfterFind -----------");
        LOGGER.log(INFO, "employeeID = " + employeeID);
        Employee emp = em.find(Employee.class, employeeID);

        LOGGER.log(INFO, "found: emp.id=" + emp.getId());

        try {
            // 1. get Department before loading
            Department deptBL = emp.getDepartmentNoWeaving();
            LOGGER.log(INFO, "1. before loading: deptBL=" + deptBL);
            String deptNameBL = null;
            if (deptBL != null) {
                deptNameBL = deptBL.getName();
                LOGGER.log(INFO, "deptNameBL=" + deptNameBL);
            }
            
            // assert deptBL == null;
            if (deptBL != null) {
                status = false;
            }

            // 2. loading
            String deptName = emp.getDepartment().getName();
            LOGGER.log(INFO, "2. loading, deptName = " + deptName);

            // 3. get Department after loading
            Department deptAL = emp.getDepartmentNoWeaving();
            LOGGER.log(INFO, "3. after loading: deptAL=" + deptAL);
            String deptNameAL = deptAL.getName();
            LOGGER.log(INFO, "deptNameAL=" + deptNameAL);
            
            // assert deptAL != null
            // assert deptAL.getName == deptName;
            if (deptAL == null || deptNameAL != deptName) {
                status = false;
            }
        } catch (Exception ex) {
            status = false;
            ex.printStackTrace();
        }

        LOGGER.log(INFO, "-----status = " + status + "---------");
        return status;
    }

}
