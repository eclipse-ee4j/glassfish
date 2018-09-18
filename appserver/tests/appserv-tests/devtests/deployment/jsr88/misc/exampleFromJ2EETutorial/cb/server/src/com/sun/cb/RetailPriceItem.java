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

import java.io.Serializable;
import java.math.BigDecimal;

public class RetailPriceItem implements Serializable {

    private String coffeeName;
    private BigDecimal wholesalePricePerPound;
    private BigDecimal retailPricePerPound;
    private String distributor;

    public RetailPriceItem() {

        this.coffeeName = null;
        this.wholesalePricePerPound = new BigDecimal("0.00");
        this.retailPricePerPound = new BigDecimal("0.00");
        this.distributor = null;
    }

    public RetailPriceItem(String coffeeName, BigDecimal wholesalePricePerPound, BigDecimal retailPricePerPound, String distributor) {

        this.coffeeName = coffeeName;
        this.wholesalePricePerPound = wholesalePricePerPound;
        this.retailPricePerPound = retailPricePerPound;
        this.distributor = distributor;
    }

    public String getCoffeeName() {
        return coffeeName;
    }

    public void setCoffeeName(String coffeeName) {
        this.coffeeName = coffeeName;
    }

    public BigDecimal getWholesalePricePerPound() {
        return wholesalePricePerPound;
    }

    public BigDecimal getRetailPricePerPound() {
        return retailPricePerPound;
    }

    public void setRetailPricePerPound(BigDecimal retailPricePerPound) {
        this.retailPricePerPound = retailPricePerPound;
    }

    public void setWholesalePricePerPound(BigDecimal wholesalePricePerPound) {
        this.wholesalePricePerPound = wholesalePricePerPound;
    }
    public String getDistributor() {
        return distributor;
    }

    public void setDistributor(String distributor) {
        this.distributor = distributor;
    }
}

