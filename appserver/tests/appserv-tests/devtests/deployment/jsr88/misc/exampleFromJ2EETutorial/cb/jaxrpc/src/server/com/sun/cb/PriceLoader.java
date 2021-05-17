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

import java.util.*;
import java.math.BigDecimal;
import java.lang.reflect.Array;

public final class PriceLoader {

    public static final PriceItemBean[] loadItems(String propsName) {

       ResourceBundle priceBundle =
           ResourceBundle.getBundle(propsName);

       Enumeration bundleKeys = priceBundle.getKeys();
       ArrayList keyList = new ArrayList();

       while (bundleKeys.hasMoreElements()) {
           String key = (String)bundleKeys.nextElement();
           String value  = priceBundle.getString(key);
           keyList.add(value);
       }

       PriceItemBean[] items =
           (PriceItemBean[])Array.newInstance(PriceItemBean.class, keyList.size());
       int k = 0;
       for (Iterator it=keyList.iterator(); it.hasNext(); ) {
           String s = (String)it.next();
           int commaIndex = s.indexOf(",");
           String name = s.substring(0, commaIndex).trim();
           String price = s.substring(commaIndex + 1, s.length()).trim();
           items[k] = new PriceItemBean(name, new BigDecimal(price));
           k++;
       }

       return items;
   }
}
