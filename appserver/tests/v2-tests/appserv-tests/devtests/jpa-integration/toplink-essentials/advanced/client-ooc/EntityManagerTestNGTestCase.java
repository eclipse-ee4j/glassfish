/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package oracle.toplink.essentials.testing.tests.cmp3.advanced;

import oracle.toplink.essentials.testing.framework.TestNGTestCase;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Address;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Employee;
import oracle.toplink.essentials.testing.models.cmp3.advanced.ModelExamples;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import java.util.List;

/**
 * TestNG test case(s) for testing entity manager operations using the
 * TopLink implementation of the Java Persistence API.
 * The base class TestNGTestCase provides the EntiyManagerFactory setup.
 */
public class EntityManagerTestNGTestCase extends TestNGTestCase {
    private static Integer employeeId;

    @Test
    public void createEmployee() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee = ModelExamples.employeeExample1();
            em.persist(employee);
            employeeId = employee.getId();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    @Test(dependsOnMethods = { "createEmployee" })
    public void testReadEmployee() {
        Employee employee = createEntityManager().find(Employee.class, employeeId);
        Assert.assertTrue(employee.getId() == employeeId, "Error reading Employee");
    }

    @Test
    public void testNamedNativeQueryOnAddress() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Address address1 = ModelExamples.addressExample1();
            em.persist(address1);
            Address address2 = ModelExamples.addressExample2();
            em.persist(address2);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        Query query = em.createNamedQuery("findAllSQLAddresses");
        List addresses = query.getResultList();
        Assert.assertTrue(addresses != null, "Error executing named native query 'findSQLAllAddresses'");
    }

    @Test(dependsOnMethods = { "testReadEmployee" })
    public void testNamedQueryOnEmployee() {
        Query query = createEntityManager().createNamedQuery("findAllEmployeesByFirstName");
        query.setParameter("firstname", "Brady");
        Employee employee = (Employee) query.getSingleResult();
        Assert.assertTrue(employee != null, "Error executing named query 'findAllEmployeesByFirstName'");
    }

    @Test(dependsOnMethods = { "testNamedQueryOnEmployee" })
    public void testUpdateEmployee() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee employee = em.find(Employee.class, employeeId);
            employee.setSalary(50000);
            em.merge(employee);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        clearCache();
        Employee newEmployee = em.find(Employee.class, employeeId);
        Assert.assertTrue(newEmployee.getSalary() == 50000, "Error updating Employee");
    }

    @Test
    public void testRefreshNotManagedEmployee() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee emp = new Employee();
            emp.setFirstName("NotManaged");
            em.refresh(emp);
            Assert.fail("entityManager.refresh(notManagedObject) didn't throw exception");
        } catch (IllegalArgumentException illegalArgumentException) {
            // expected behaviour
        } catch (RuntimeException e ) {
            throw e;
        } finally {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    @Test
    public void testRefreshRemovedEmployee() {
        // find an existing or create a new Employee
        String firstName = "testRefreshRemovedEmployee";
        Employee emp;
        EntityManager em = createEntityManager();
        List result = em.createQuery("SELECT OBJECT(e) FROM Employee e WHERE e.firstName = '"+firstName+"'").getResultList();
        if(!result.isEmpty()) {
            emp = (Employee)result.get(0);
        } else {
            emp = new Employee();
            emp.setFirstName(firstName);
            // persist the Employee
            em.getTransaction().begin();
            try {
                em.persist(emp);
                em.getTransaction().commit();
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
                throw e;
            }
        }

        em.getTransaction().begin();
        try{
            emp = em.find(Employee.class, emp.getId());

            // delete the Employee from the db
            em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();

            // refresh the Employee - should fail with EntityNotFoundException
            em.refresh(emp);
            Assert.fail("entityManager.refresh(removedObject) didn't throw exception");
        } catch (EntityNotFoundException entityNotFoundException) {
            // expected behaviour
        } finally {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

     @Test(dependsOnMethods = { "testUpdateEmployee" })
     public void testDeleteEmployee() {
         EntityManager em = createEntityManager();
         em.getTransaction().begin();
         try {
             em.remove(em.find(Employee.class, employeeId));
             em.getTransaction().commit();
         } catch (RuntimeException e) {
             if (em.getTransaction().isActive()){
                 em.getTransaction().rollback();
             }
             em.close();
             throw e;
         }
         Assert.assertTrue(em.find(Employee.class, employeeId) == null, "Error deleting Employee");
     }

}
