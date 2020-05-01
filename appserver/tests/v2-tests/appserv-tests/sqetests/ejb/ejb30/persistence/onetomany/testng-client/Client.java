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

package pe.ejb.ejb30.persistence.toplinksample.client;

import java.io.*;
import java.util.*;
import jakarta.ejb.*;
import javax.naming.*;
import java.rmi.*;
import pe.ejb.ejb30.persistence.toplinksample.ejb.*;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;


public class Client {

    private static @EJB StatelessInterface sless;
    List rows;
    Iterator i;

    public static void main(String[] args) {
        org.testng.TestNG testng = new org.testng.TestNG();
        testng.setTestClasses(
            new Class[] { pe.ejb.ejb30.persistence.toplinksample.client.Client.class } );
        testng.run();
    }

    @Configuration(beforeTestClass = true)
    public void setup() throws Exception {
        System.out.println("Client: invoking stateful setup");
        sless.setUp();
    }

    @Configuration(afterTestClass = true)
    public void cleanup() throws Exception {
        System.out.println("Cleanup: DELETING ROWS...");
        sless.cleanUp();
    }

    @Test
    public void testGetCustomerOrders() throws Exception {
        System.out.println("Client: getting customer orders");
        Collection coll = sless.getCustomerOrders(1);
        if (coll != null) {
            for (Iterator iterator=coll.iterator(); iterator.hasNext();)
                System.out.println((OrderEntity)iterator.next());
        }
        Assert.assertTrue((coll != null), "Got customers orders");
    }

    @Test
    public void testGetCustomerByName() throws Exception {
        System.out.println("Get customer by name and address");
        List rows = sless.getCustomers("Alice", "Santa Clara");
        if (rows != null) {
            for (Iterator iterator = rows.iterator(); iterator.hasNext();)
                System.out.println((CustomerEntity)iterator.next());
        }

        Assert.assertTrue((rows != null), "Got customers");
    }

    @Test
    public void testGetAllCustomers() throws Exception {
        System.out.println("Get all customers");
        List rows = sless.getAllCustomers();
        if (rows != null) {
            for (Iterator iterator = rows.iterator(); iterator.hasNext();)
                System.out.println((CustomerEntity)iterator.next());
        }

        Assert.assertTrue((rows != null), "Got all customers");
    }

    @Test
    public void testGetAllItemsByName() throws Exception {
        System.out.println("Get all items by name");
        List rows = sless.getAllItemsByName();

        if (rows != null) {
            for (Iterator iterator = rows.iterator(); iterator.hasNext();)
                System.out.println((ItemEntity)iterator.next());
        }
        Assert.assertTrue((rows != null), "Got all item by name");
    }

    @Test
    public void testGetAllOrdersByItem() throws Exception {
        System.out.println("Get all orders by item");
        List rows = sless.getAllOrdersByItem();

        if (rows != null) {
            for (Iterator iterator = rows.iterator(); iterator.hasNext();)
                System.out.println((ItemEntity)iterator.next());
        }
        Assert.assertTrue((rows != null), "Got all orders by item");
    }
}
