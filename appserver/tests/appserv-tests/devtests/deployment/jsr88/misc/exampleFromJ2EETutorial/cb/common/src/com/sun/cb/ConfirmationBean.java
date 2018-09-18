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

public class ConfirmationBean implements Serializable {

    private String orderId;
    private Calendar shippingDate;

    public ConfirmationBean() { }

    public ConfirmationBean(String orderId, Calendar shippingDate) {

        this.orderId = orderId;
        this.shippingDate = shippingDate;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Calendar getShippingDate() {
        return shippingDate;
    }

    public void setShippingDate(Calendar shippingDate) {
        this.shippingDate = shippingDate;
    }
}
