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
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static @EJB StatelessInterface sless;
    List rows;
    Iterator i;

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    public static String testSuiteID;

    public static void main(String[] args) {

     if(args.length==1) {
         testSuiteID=args[0];
            }
       System.out.println("The TestSuite ID : " + testSuiteID);
       System.out.println("The args length is : " + args.length);
        stat.addDescription("ejb3_slsb_persistence");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb3_slsb_persistenceID");
    }

    public Client(String[] args) {}

    public void doTest() {
        try{

            try {
                //--setup test: persist all entities
                System.out.println("Client: invoking stateful setup");
                sless.setUp();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence cleanUp", stat.PASS);
            } catch(Exception e) {
                System.out.println("Client: Error in setUp");
                e.printStackTrace();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence setup", stat.FAIL);
            }

            try {
                //--addOrders test: add some orders through customer
                System.out.println("Client: getting customer orders");
                Collection c = sless.getCustomerOrders(1);
                if(c==null){
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getCustomerOrders:relationshipTest",
                            stat.FAIL);
                    System.out.println("Client: got NULL Orders");
                } else {
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getCustomerOrders:relationshipTest",
                            stat.PASS);
                    System.out.println("Client: got Orders of class:"+c.getClass().getName());
                    i=c.iterator();
                    while(i.hasNext())
                        System.out.println((OrderEntity)i.next());
                }
            } catch(Exception e) {
                System.out.println("Client: Error in getCustomerOrders:relationshipTest");
                e.printStackTrace();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getCustomerOrders:relationshipTest", stat.FAIL);
            }

            try{
                //--getCustomers test: get customer by name and city
                rows=sless.getCustomers("Alice", "Santa Clara");
                if(rows == null){
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getCustomers", stat.FAIL);
                } else {
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getCustomers", stat.PASS);
                    System.out.println("Client: Got Rows. Listing...");
                    i=rows.iterator();
                    while(i.hasNext())
                        System.out.println((CustomerEntity)i.next());
                }
            } catch(Exception e) {
                System.out.println("Client: Error in getCustomers");
                e.printStackTrace();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getCustomers", stat.FAIL);
            }

            try{
                //--getAllCustomers test: get all customers
                rows=sless.getAllCustomers();
                if(rows == null){
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllCustomers", stat.FAIL);
                } else {
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllCustomers", stat.PASS);
                    System.out.println("Client: Got allCustomer rows. Listing...");
                    i=rows.iterator();
                    while(i.hasNext())
                        System.out.println((CustomerEntity)i.next());
                }
            } catch(Exception e) {
                System.out.println("Client: Error in getAllCustomers");
                e.printStackTrace();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllCustomers", stat.FAIL);
            }

            try{
                //--getAllItemsByName test
                rows=sless.getAllItemsByName();
                if(rows == null){
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllItemsByName", stat.FAIL);
                } else {
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllItemsByName", stat.PASS);
                    System.out.println("Client: Got allItemsByName rows. Listing...");
                    i=rows.iterator();
                    while(i.hasNext())
                        System.out.println((ItemEntity)i.next());
                }
            } catch(Exception e) {
                System.out.println("Client: Error in getAllItemsByName");
                e.printStackTrace();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllItemsByName", stat.FAIL);
            }

            try{
                //--getAllOrdersByItem test
                rows=sless.getAllItemsByName();
                if(rows == null){
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllOrdersByItem",
                            stat.FAIL);
                } else {
                    stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllOrdersByItem",
                            stat.PASS);
                    System.out.println("Client: Got AllOrdersByItem rows. Listing...");
                    i=rows.iterator();
                    while(i.hasNext())
                        System.out.println((ItemEntity)i.next());
                }
            } catch(Exception e) {
                System.out.println("Client: Error in getAllOrdersByItem");
                e.printStackTrace();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence getAllOrdersByItem", stat.FAIL);
            }

            try{
                //--cleanup test: remove all persisted entities
                System.out.println("Cleanup: DELETING ROWS...");
                sless.cleanUp();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence cleanUp", stat.PASS);
            } catch(Exception e) {
                System.out.println("Client: Error in cleanUp");
                e.printStackTrace();
                stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence cleanUp", stat.FAIL);
            }

            //System.out.println("Client: Inserted and deleted row "+
            //        "through 3.0 persistence entity");
            //System.out.println(sless.getMessage());
            //stat.addStatus("ejb3_slsb_persistence sfsb_persistent_insert", stat.PASS);
        } catch(Throwable e) {
            System.out.println("Client: Unexpected Error,check server.log");
            e.printStackTrace();
            stat.addStatus(testSuiteID+""+"ejb3_slsb_persistence ALLTESTS", stat.FAIL);
        }
        return;
    }
}
