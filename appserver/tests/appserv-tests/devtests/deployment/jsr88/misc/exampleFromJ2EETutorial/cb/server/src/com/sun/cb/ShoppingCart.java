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
import com.sun.cb.RetailPriceList;
import com.sun.cb.RetailPriceItem;

public class ShoppingCart {
  ArrayList items = null;
  BigDecimal total = new BigDecimal("0.00");
  int numberOfItems = 0;

  public ShoppingCart(RetailPriceList rpl) {
      items = new ArrayList();

      for(Iterator i = rpl.getItems().iterator(); i.hasNext(); ) {
        RetailPriceItem item = (RetailPriceItem) i.next();
        ShoppingCartItem sci = new ShoppingCartItem(item, new BigDecimal("0.0"), new BigDecimal("0.00"));
        items.add(sci);
        numberOfItems++;
      }
  }

  public synchronized void add (ShoppingCartItem item) {
    items.add(item);
    total = total.add(item.getPrice()).setScale(2);
    numberOfItems++;
  }

  public synchronized int getNumberOfItems() {
    return numberOfItems;
  }

  public synchronized ArrayList getItems() {
      return items;
  }

  protected void finalize() throws Throwable {
      items.clear();
  }

  public synchronized BigDecimal getTotal() {
    return total;
  }


  public synchronized void clear() {
      numberOfItems = 0;
      total = new BigDecimal("0.00");
      items.clear();
  }
}

