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

public class SupplierImpl implements SupplierIF {

    public ConfirmationBean placeOrder(OrderBean order) {

         Date tomorrow = com.sun.cb.DateHelper.addDays(new Date(), 1);
         ConfirmationBean confirmation =
             new ConfirmationBean(order.getId(),
                 DateHelper.dateToCalendar(tomorrow));
         return confirmation;
    }

    public PriceListBean getPriceList() {

       PriceListBean priceList = loadPrices();
       return priceList;
    }

    private PriceListBean loadPrices() {

       String propsName = "com.sun.cb.SupplierPrices";
       Date today = new Date();
       Date endDate = DateHelper.addDays(today, 30);

       PriceItemBean[] priceItems = PriceLoader.loadItems(propsName);
       PriceListBean priceList =
           new PriceListBean(DateHelper.dateToCalendar(today),
               DateHelper.dateToCalendar(endDate), priceItems);

       return priceList;
    }

} // class

