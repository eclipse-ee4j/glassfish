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

public class OrderBean implements Serializable {

    private String id;
    private CustomerBean customer;
    private LineItemBean[] lineItems;
    private BigDecimal total;
    private AddressBean address;

    public OrderBean() {
    }

    public OrderBean( AddressBean address, CustomerBean customer, String id,
        LineItemBean[] lineItems, BigDecimal total) {

        this.id = id;
        this.customer = customer;
        this.total = total;
        this.lineItems = lineItems;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CustomerBean getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerBean customer) {
        this.customer = customer;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LineItemBean[] getLineItems() {
        return lineItems;
    }

    public void setLineItems(LineItemBean[] lineItems) {
        this.lineItems = lineItems;
    }

    public AddressBean getAddress() {
        return address;
    }

    public void setAddress(AddressBean address) {
        this.address = address;
    }
}
