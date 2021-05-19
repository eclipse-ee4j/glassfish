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

/*
 * Order.java
 *
 * Created on February 23, 2005, 8:35 PM
 */

package com.acme;
import jakarta.persistence.*;
import static jakarta.persistence.GeneratorType.*;
import static jakarta.persistence.AccessType.*;

/**
 *
 * @author ss141213
 */
@Entity
public class Order {
    private Long id;
    private int version;
    private int itemId;
    private int quantity;
    private Customer customer;
    @Id(generate=AUTO)
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @Version
    protected int getVersion() {
        return version;
    }
    protected void setVersion(int version) {
        this.version = version;
    }
    @Basic
    public int getItemId() {
        return itemId;
    }
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    @Basic public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    @ManyToOne public Customer getCustomer() {
        return customer;
    }
    public void setCustomer(Customer cust) {
        this.customer = cust;
    }
}
