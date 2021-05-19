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

/*
 * JpaServlet.java
 */

package web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.*;

import jakarta.persistence.*;
import jakarta.transaction.*;
import jakarta.annotation.*;

import entity.*;

import util.*;

public class JpaServlet extends ServletUtil {

    @PersistenceUnit(name = "myemf", unitName="pu1")
    private EntityManagerFactory emf;
    private EntityManager em;
    private @Resource UserTransaction utx;


    private final int customerId = 1;
    private final String customerName = "Joe Smith";
    private final int order1Id = 100;
    private final int order2Id = 200;
    private final String order1Addr = "123 Main St. Anytown, US";
    private final String order2Addr = "567 1st St. Random City, USA";


    public boolean processParams(HttpServletRequest request) {
      try {
        if (request.getParameter("case") != null) {
           tc = request.getParameter("case");
        }
        return true;
     } catch(Exception ex) {
        System.err.println("Exception when processing the request params");
        ex.printStackTrace();
        return false;
     }

    }

    public EntityManager getEntityManager()
    {
        return  emf.createEntityManager();
    }

    public boolean testInsert() {
        boolean result = false;

        try {
        utx.begin();
        //out.println("createEM  EntityManager=" + em);
        em = getEntityManager();
        // Create new customer
        Customer customer0 = new Customer();
        customer0.setId(customerId);
        customer0.setName(customerName);

        // Persist the customer
        em.persist(customer0);

        // Create 2 orders
        Order order1 = new Order();
        order1.setId(order1Id);
        order1.setAddress(order1Addr);

        Order order2 = new Order();
        order2.setId(order2Id);
        order2.setAddress(order2Addr);

        // Associate orders with the customer. The association
        // must be set on both sides of the relationship: on the
        // customer side for the orders to be persisted when
        // transaction commits, and on the order side because it
        // is the owning side.
        customer0.getOrders().add(order1);
        order1.setCustomer(customer0);

        customer0.getOrders().add(order2);
        order2.setCustomer(customer0);
        utx.commit();
        result = true;
     }catch(Exception ex){
        ex.printStackTrace();
      } finally {
        if (em != null) {
           em.close();
        }
      }
      return result;
    }


    public boolean testDelete() {
      boolean result = false;
      try {
        utx.begin();
        em = getEntityManager();
        Customer c = findCustomer(customerName);
        Customer c0 = em.merge(c);
        em.remove(c0);
        utx.commit();
        result = true;
      } catch(Exception ex){
        ex.printStackTrace();
      } finally {
        if (em != null) {
           em.close();
        }
      }
      return result;
    }

     public boolean verifyInsert() {
        boolean result = false;

        try {
        em = getEntityManager();
        System.out.println("Customer name in verifyInsert:"+customerName);
        Customer c = findCustomer(customerName);

        Collection<Order> orders = c.getOrders();
        if (orders == null || orders.size() != 2) {
            throw new RuntimeException("Unexpected number of orders: "
                    + ((orders == null)? "null" : "" + orders.size()));
        }
        result = true;
        } finally {
        if (em != null) {
           em.close();
           }
        }
        return result;
    }


    public boolean verifyDelete() {
      boolean result = false;
      try {
      em = getEntityManager();
      Query q = em.createQuery("select c from Customer c");
      List results = q.getResultList();
      if (results == null || results.size() != 0) {
        throw new RuntimeException("Unexpected number of customers after delete");
      }

      q = em.createQuery("select o from Order o");
      results = q.getResultList();

      if (results == null || results.size() != 0) {
        throw new RuntimeException("Unexpected number of orders after delete");
      }
      result = true;
     } finally {
        if (em != null) {
           em.close();
           }
     }
      return result;
    }

    public Customer findCustomer(String name) {
      Query q = em.createQuery("select c from Customer c where c.name = :name");
      q.setParameter("name", name);
      Customer c = (Customer)q.getSingleResult();
      return c;
    }

}
