/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.cb;

import java.math.BigDecimal;
import java.util.*;

public class TestOrderCaller {
    public static void main(String[] args) {
        try {

            AddressBean address = new AddressBean("455 Apple Way",
               "Santa Clara", "CA", "95123");
            CustomerBean customer = new CustomerBean("Buzz",
               "Murphy", "247-5566", "buzz.murphy@clover.com");

            LineItemBean itemA = new LineItemBean("mocha", new BigDecimal("1.0"), new BigDecimal("9.50"));
            LineItemBean itemB = new LineItemBean("special blend", new BigDecimal("5.0"), new BigDecimal("8.00"));
            LineItemBean itemC = new LineItemBean("wakeup call", new BigDecimal("0.5"), new BigDecimal("10.00"));
            LineItemBean[] lineItems = {itemA, itemB, itemC};

            OrderBean order = new OrderBean(address, customer, "123", lineItems,
                new BigDecimal("55.67"));

            OrderCaller oc = new OrderCaller(args[0]);
            ConfirmationBean confirmation = oc.placeOrder(order);

            System.out.println(confirmation.getOrderId()  + " " +
                DateHelper.format(confirmation.getShippingDate(), "MM/dd/yy"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
