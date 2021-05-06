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

package client;


import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;

import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import request.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static OrderRequest orderRequest;

    public static void main(String[] args) {

        try {
            stat.addDescription("OrderApp");
            Context initial = new InitialContext();

            Object objref = initial.lookup("java:comp/env/ejb/Request");
            RequestHome home = (RequestHome)PortableRemoteObject.narrow(objref,
                    RequestHome.class);

            Request request = home.create();

            createData(request);
            printData(request);
            stat.addStatus("ejbclient OrderApp", stat.PASS);

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
            stat.addStatus("ejbclient OrderApp", stat.FAIL);
        }
        stat.printSummary("OrderApp");
    }

    private static void createData(Request request) {
        try {
            request.createPart(new PartRequest("1234-5678-01", 1, "ABC PART",
                    new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null));
            request.createPart(new PartRequest("9876-4321-02", 2, "DEF PART",
                    new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null));
            request.createPart(new PartRequest("5456-6789-03", 3, "GHI PART",
                    new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null));
            request.createPart(new PartRequest("ABCD-XYZW-FF", 5, "XYZ PART",
                    new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null));
            request.createPart(new PartRequest("SDFG-ERTY-BN", 7, "BOM PART",
                    new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null));

            request.addPartToBillOfMaterial(new BomRequest("SDFG-ERTY-BN", 7,
                    "1234-5678-01", 1));
            request.addPartToBillOfMaterial(new BomRequest("SDFG-ERTY-BN", 7,
                    "9876-4321-02", 2));
            request.addPartToBillOfMaterial(new BomRequest("SDFG-ERTY-BN", 7,
                    "5456-6789-03", 3));
            request.addPartToBillOfMaterial(new BomRequest("SDFG-ERTY-BN", 7,
                    "ABCD-XYZW-FF", 5));

            request.createVendor(new VendorRequest(100, "WidgetCorp",
                    "111 Main St., Anytown, KY 99999", "Mr. Jones",
                    "888-777-9999"));
            request.createVendor(new VendorRequest(200, "Gadget, Inc.",
                    "123 State St., Sometown, MI 88888", "Mrs. Smith",
                    "866-345-6789"));

            request.createVendorPart(new VendorPartRequest("1234-5678-01", 1,
                    "PART1", 100.00, 100));
            request.createVendorPart(new VendorPartRequest("9876-4321-02", 2,
                    "PART2", 10.44, 200));
            request.createVendorPart(new VendorPartRequest("5456-6789-03", 3,
                    "PART3", 76.23, 200));
            request.createVendorPart(new VendorPartRequest("ABCD-XYZW-FF", 5,
                    "PART4", 55.19, 100));
            request.createVendorPart(new VendorPartRequest("SDFG-ERTY-BN", 7,
                    "PART5", 345.87, 100));

            Integer orderId = new Integer(1111);
            request.createOrder(new OrderRequest(orderId, 'N', 10,
                    "333 New Court, NewCity, CA 90000"));
            request.addLineItem(new LineItemRequest(orderId, "1234-5678-01", 1, 3));
            request.addLineItem(new LineItemRequest(orderId, "9876-4321-02", 2, 5));
            request.addLineItem(new LineItemRequest(orderId, "ABCD-XYZW-FF", 5, 7));

            orderId = new Integer(4312);
            request.createOrder(new OrderRequest(orderId, 'N', 0,
                    "333 New Court, NewCity, CA 90000"));
            request.addLineItem(new LineItemRequest(orderId, "SDFG-ERTY-BN", 7, 1));
            request.addLineItem(new LineItemRequest(orderId, "ABCD-XYZW-FF", 5, 3));
            request.addLineItem(new LineItemRequest(orderId, "1234-5678-01", 1, 15));

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
            stat.addStatus("ejbclient OrderApp", stat.FAIL);
        }
    }

    private static MessageFormat mf = new MessageFormat(": {0, number, $#,##0.##}");

    private static void printData(Request request) {
        try {

            BomRequest bomRequest = new BomRequest("SDFG-ERTY-BN", 7, null, 0);
            double price = request.getBillOfMaterialPrice(bomRequest);
            System.out.println("Cost of Bill of Material for PN "
                    + bomRequest.bomPartNumber + " Rev: "
                    + bomRequest.bomRevision
                    + mf.format(new Object[] {new Double(price)}));

            printCostOfOrders(request);

            System.out.println("\nAdding 5% discount");
            request.adjustOrderDiscount(5);
            printCostOfOrders(request);

            System.out.println("\nRemoving 7% discount");
            request.adjustOrderDiscount(-7);
            printCostOfOrders(request);

            java.lang.Double price0 = request.getAvgPrice();
            if (price0 == null) {
                System.out.println("\nNo parts found");
            } else {
                System.out.println("\nAverage price of all parts"
                        + mf.format(new Object[] {price0}));
            }

            VendorRequest vendorRequest = new VendorRequest(100, null, null, null, null);
            price0 = request.getTotalPricePerVendor(vendorRequest);
            if (price0 == null) {
                System.out.println("\nNo parts found for Vendor "
                        + vendorRequest.vendorId);
            } else {
                System.out.println("\nTotal price of parts for Vendor "
                        + vendorRequest.vendorId + ""
                        + mf.format(new Object[] {price0}));
            }

            System.out.println("\nOrdered list of vendors for order 1111");
            System.out.println(request.reportVendorsByOrder(new Integer(1111)));

            System.out.println("Counting all line items");
            int count = request.countAllItems();
            System.out.println("Found " + count + " line items");

            System.out.println("\nRemoving Order");
            request.removeOrder(new Integer(4312));
            count = request.countAllItems();
            System.out.println("Found " + count + " line items");

            Collection names = request.locateVendorsByPartialName("I");
            System.out.println("\nFound " + names.size()
                    + " out of 2 vendors with 'I' in the name:");
            for (Iterator it = names.iterator(); it.hasNext();) {
                System.out.println(it.next());
            }

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
            stat.addStatus("ejbclient OrderApp", stat.FAIL);
        }
    }

    private static void printCostOfOrders(Request request)
            throws java.rmi.RemoteException {

        Integer orderId = new Integer(1111);
        double price = request.getOrderPrice(orderId);
        System.out.println("Cost of Order " + orderId
                + mf.format(new Object[] {new Double(price)}));

        orderId = new Integer(4312);
        price = request.getOrderPrice(orderId);
        System.out.println("Cost of Order " + orderId
                + mf.format(new Object[] {new Double(price)}));

    }

}
