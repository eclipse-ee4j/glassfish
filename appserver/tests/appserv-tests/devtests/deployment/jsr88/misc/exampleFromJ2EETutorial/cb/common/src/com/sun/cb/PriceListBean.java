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
import java.util.*;

public class PriceListBean implements Serializable {

    private Calendar startDate;
    private Calendar endDate;
    private PriceItemBean[] priceItems;

    public PriceListBean() {
    }

    public PriceListBean(Calendar startDate, Calendar endDate,
        PriceItemBean[] priceItems) {

        this.startDate = startDate;
        this.endDate = endDate;
        this.priceItems = priceItems;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar date) {
        this.startDate = date;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar date) {
        this.endDate = date;
    }

    public PriceItemBean[] getPriceItems() {
        return priceItems;
    }

    public void setPriceItems(PriceItemBean[] priceItems) {
        this.priceItems = priceItems;
    }

}
