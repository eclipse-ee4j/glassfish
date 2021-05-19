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

package request;

import java.rmi.RemoteException;
import java.util.Collection;

import jakarta.ejb.*;


public interface Request extends EJBObject {

    public void createPart(PartRequest partRequest) throws RemoteException;

    public void addPartToBillOfMaterial(BomRequest bomRequest) throws RemoteException;

    public void createVendor(VendorRequest vendorRequest) throws RemoteException;

    public void createVendorPart(VendorPartRequest vendorPartRequest) throws RemoteException;

    public void createOrder(OrderRequest orderRequest) throws RemoteException;

    public void addLineItem(LineItemRequest lineItemRequest) throws RemoteException;

    public double getBillOfMaterialPrice(BomRequest bomRequest) throws RemoteException;

    public Double getAvgPrice() throws RemoteException;

    public Double getTotalPricePerVendor(VendorRequest vendorRequest) throws RemoteException;

    public double getOrderPrice(Integer orderId) throws RemoteException;

    public void adjustOrderDiscount(int adjustment) throws RemoteException;

    public Collection locateVendorsByPartialName(String name) throws RemoteException;

    public String reportVendorsByOrder(Integer orderId) throws RemoteException;

    public int countAllItems() throws RemoteException;

    public void removeOrder(Integer orderId) throws RemoteException;

}

