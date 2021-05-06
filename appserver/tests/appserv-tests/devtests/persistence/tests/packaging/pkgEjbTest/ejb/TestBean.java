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

package ejb;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.Collection;
import java.util.List;

import entity.*;

@Stateless(name="ejb/Test")
public class TestBean implements Test {

    @PersistenceContext(unitName="pu1")
    private EntityManager em;

    public String testInsert() {

        // Create new customer
        Customer customer0 = new Customer();
        customer0.setId(1);
        customer0.setName("Joe Smith");

        // Persist the customer
        em.persist(customer0);

        // Create 2 orders
        Order order1 = new Order();
        order1.setId(100);
        order1.setAddress("123 Main St. Anytown, USA");

        Order order2 = new Order();
        order2.setId(200);
        order2.setAddress("567 1st St. Random City, USA");

        // Associate orders with the customer. The association
        // must be set on both sides of the relationship: on the
        // customer side for the orders to be persisted when
        // transaction commits, and on the order side because it
        // is the owning side.
        customer0.getOrders().add(order1);
        order1.setCustomer(customer0);

        customer0.getOrders().add(order2);
        order2.setCustomer(customer0);

        return "OK";
    }

    public String verifyInsert() {

        Customer c = findCustomer("Joe Smith");

        Collection<Order> orders = c.getOrders();
        if (orders == null || orders.size() != 2) {
            throw new RuntimeException("Unexpected number of orders: "
                    + ((orders == null)? "null" : "" + orders.size()));
        }

        return "OK";
    }

     public String testDelete(String name) {

        Customer c = findCustomer(name);

        // Merge the customer to the new persistence context
        Customer c0 = em.merge(c);

        // Delete all records.
        em.remove(c0);

        return "OK";
    }


    public String verifyDelete() {

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

        return "OK";
    }

    public Customer findCustomer(String name) {
        Query q = em.createQuery("select c from Customer c where c.name = :name");
        q.setParameter("name", name);
        Customer c = (Customer)q.getSingleResult();
        return c;
    }

}
