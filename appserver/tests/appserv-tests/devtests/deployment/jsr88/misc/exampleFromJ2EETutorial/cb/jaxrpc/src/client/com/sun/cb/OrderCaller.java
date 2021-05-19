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
import javax.xml.rpc.Stub;

public class OrderCaller {

    private SupplierIF supplier;

    public OrderCaller(String endpoint) {

        try {
                    Stub stub = (Stub)(new Supplier_Impl().getSupplierIFPort());
            //stub = (SupplierIF_Stub)(new Supplier_Impl().getSupplierIFPort());
            stub._setProperty(
                javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY,
                endpoint);
            supplier = (SupplierIF) stub;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public ConfirmationBean placeOrder(OrderBean order) {

        ConfirmationBean result = null;
        try {
            result = supplier.placeOrder(order);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

} // class
