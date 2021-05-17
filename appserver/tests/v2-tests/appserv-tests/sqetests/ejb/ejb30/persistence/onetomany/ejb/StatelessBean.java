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

package pe.ejb.ejb30.persistence.toplinksample.ejb;

import jakarta.ejb.*;
import jakarta.persistence.*;
import jakarta.annotation.*;
import javax.sql.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import pe.ejb.ejb30.persistence.toplinksample.ejb.*;
import java.util.*;

@Stateless
@Remote({StatelessInterface.class})
public class StatelessBean implements StatelessInterface {

    @Resource SessionContext sc;
    @PersistenceContext EntityManager em;

    CustomerEntity c1 = new CustomerEntity(1, "Alice", "Santa Clara");
    CustomerEntity c2 = new CustomerEntity(2, "Betty", "Sunnyvale");
    OrderEntity o1 = new OrderEntity(100, 1);
    OrderEntity o2 = new OrderEntity(101, 2);
    ItemEntity i1 = new ItemEntity(100, "Camcorder");
    ItemEntity i2 = new ItemEntity(101, "PlayStation");
    private String message = "hello";

    public void setUp() {
        System.out.println("StatelessBean:setUp:persisting...");
        em.flush();
        em.persist(c1);
        em.persist(c2);
        em.persist(o1);
        em.persist(o2);
        em.persist(i1);



        System.out.println("Stateless:setUp:Entities persisted");
    }

    public List getCustomers(String name, String city) {
        System.out.println("StatelessBean: getting customers...");
        List result = em.createNamedQuery("findAllCustomersWithLike")
            .setParameter("name", name)
            .setParameter("city", city)
            .getResultList();
        return result;
    }

    public List getAllCustomers() {
        System.out.println("StatelessBean: getting ALL customers...");
        List result = em.createNamedQuery("findAllCustomers").getResultList();
        return result;
    }

    public Collection getCustomerOrders(int custId) {
        System.out.println("StatelessBean:getCustomerOrders");
        CustomerEntity customer = em.find(CustomerEntity.class, new Integer(custId));
        if(customer == null){
            System.out.println("StatelessBean:getCustomerOrders:Customer not found!");
            return null;
        } else {
            OrderEntity order1 = em.find(OrderEntity.class, new Integer(100));
            OrderEntity order2 = em.find(OrderEntity.class, new Integer(101));
            ItemEntity item1 = em.find(ItemEntity.class, new Integer(101));
            order1.setItem(item1);
            //ArrayList al = new ArrayList();
            //al.add(o1);
            //al.add(o2);
            //c1.setOrders(al);
            customer.addOrder(order1);
            customer.addOrder(order2);
            //em.persist(customer);
            System.out.println("StatelessBean:getCustomerOrders:"+
                    "Customer found, returning orders");
            Collection<OrderEntity> coe = customer.getOrders();
            if(coe ==null) {
                System.out.println("StatelessBean:getCustomerOrders: "+
                    "NULL collection returned");
            } else {
                System.out.println("StatelessBean:getCustomerOrders: "+
                    "collection class returned:"+coe.getClass().getName());
            }

            return coe;
        }
    }

    public List getAllItemsByName() {
        System.out.println("StatelessBean: getting ALL customers...");
        List result = em.createNamedQuery("findAllItemsByName")
            .setParameter("1", "Camcorder")
            .getResultList();
        return result;
    }

    public List getAllOrdersByItem() {
        System.out.println("StatelessBean: getting ALL customers...");
        List result = em.createNamedQuery("findAllOrdersByItem")
            .setParameter("id", "1")
            .getResultList();
        return result;
    }

    public void cleanUp() {
        System.out.println("StatelessBean:cleanUp:");
        em.merge(c1);
        if(em.contains(c1)) em.remove(c1);
        em.merge(c2);
        if(em.contains(c2)) em.remove(c2);
        //em.merge(o1); // o1 will be removed as a result of cascade, after removing customer1
        //if(em.contains(o1)) em.remove(o1);
        //em.merge(o2); // o2 will be removed as a result of cascade, after removing customer1
        //if(em.contains(o2)) em.remove(o2);
        em.merge(i1);
        if(em.contains(i1)) em.remove(i1);
        em.merge(i2); // why isn't this throwing an exception?
        if(em.contains(i2)) em.remove(i2); // why isn't this throwing an exception?
        System.out.println("Stateless:cleanUp:Entities removed");
    }

    public void setMessage(String message) {
        System.out.println("Stateless:setMessage:" + message);
        this.message = message;
    }

    public String getMessage() { return message; }
}
