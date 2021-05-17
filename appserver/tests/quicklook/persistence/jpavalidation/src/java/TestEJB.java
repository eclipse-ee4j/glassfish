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

import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import java.util.*;

@Stateless
public class TestEJB {

    @PersistenceContext EntityManager em;

    public TestEJB() {
    }

    // initData
    public boolean test1() {
        boolean pass= false;

    Employee e1 = new Employee(1, "emp1", 1000);
    Employee e2 = new Employee(2, "emp2", 2000);
    Employee e3 = new Employee(3, "emp3", 3000);
    Employee e4 = new Employee(4, "emp4", 4000);
    Project p1 = new Project(1, "proj1");
    Collection<Employee> employees = new ArrayList<Employee>();
    employees.add(e1);
    employees.add(e2);
    employees.add(e3);
    employees.add(e4);
    p1.setEmployees(employees);

    // Persist Cascade without long name
    try {
      System.out.println("1. Persisting project....");
      em.persist(p1);
          em.flush();
      pass = true;
        } catch(Throwable e){
      e.printStackTrace();
        }
        return pass;
    }

    // persist Employee with a long name
    public boolean test2() {
        boolean pass= false;
    Employee e5 = new Employee(5, "myLongName5", 5000);
    try {
      System.out.println("2. Persisting employee with long name....");
      em.persist(e5);
          em.flush();
      System.out.println("Error: not get BV ex for persist");
    } catch(jakarta.validation.ConstraintViolationException ex){
      System.out.println("Expected BV Ex");
      pass= true;
      String msg = ex.getMessage();
      System.out.println("msg="+msg);
    } catch(Throwable e){
      System.out.println("Unexpected Ex");
      e.printStackTrace();
        }
        return pass;
    }


    // update Employee with a long name
    public boolean test3() {
        boolean pass= false;
    try {
      System.out.println("3. Updating employee with long name....");
      Employee e = em.find(Employee.class, 3);
      e.setName("myLongName3");
      em.flush();
      System.out.println("Error: not get BV ex for update");
    } catch (jakarta.validation.ConstraintViolationException ex) {
      System.out.println("Expected BV Ex");
      pass= true;
      String msg = ex.getMessage();
      System.out.println("msg="+msg);
    } catch(Throwable e){
      System.out.println("Unexpected Ex");
      e.printStackTrace();
        }
        return pass;
    }

    // remove Employee with a long name
    public boolean test4() {
        boolean pass= false;
        try {
      System.out.println("4. Removing employee with long name....");
      Employee e = em.find(Employee.class, 1);
      e.setName("myLongName1");
      em.remove(e);
      em.flush();
      System.out.println("OK: not get BV ex for remove");
      pass= true;
    } catch (jakarta.validation.ConstraintViolationException ex) {
      System.out.println("BV Ex");
      String msg = ex.getMessage();
      System.out.println("msg="+msg);
    } catch(Throwable e){
      System.out.println("Unexpected Ex");
      e.printStackTrace();
        }
        return pass;
    }

    // verify previous operations
    public boolean test5() {
        boolean pass= false;
        boolean bvsize = true;
    try {
      System.out.println("5. Verifying employee ....");
      Employee emp = null;
      Query q= em.createQuery("SELECT e FROM Employee e");
      List result = q.getResultList();
      int size = result.size();
      for (int i = 0 ; i < size ; i++) {
        emp = (Employee) result.get(i);
        String name = emp.getName();
        System.out.println("i=" + i + ", name=" + name);
        if (name.length() > 5) {
          bvsize = false;
        }
      }
      System.out.println("size =" +size+", bvsize="+bvsize);
      if (size == 3 && bvsize){
        pass = true;
      }
    } catch(Throwable e){
      System.out.println("Unexpected Ex");
      e.printStackTrace();
        }
        return pass;
    }


}




