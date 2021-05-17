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

public class ShoppingCartItem {
    RetailPriceItem item;
    BigDecimal pounds;
    BigDecimal price;


    public ShoppingCartItem() {}

    public ShoppingCartItem(RetailPriceItem item, BigDecimal pounds, BigDecimal price) {
        this.item = item;
        this.pounds = pounds;
        this.price = price;
    }

    public void setItem(RetailPriceItem item) {
        this.item = item;
    }

    public void setPounds(BigDecimal pounds) {
        this.pounds=pounds;
    }

    public void setPrice(BigDecimal price) {
        this.price=price;
    }

    public RetailPriceItem getItem() {
        return item;
    }


    public BigDecimal getPounds() {
        return pounds;
    }

    public BigDecimal getPrice() {
        return price;
    }
}










