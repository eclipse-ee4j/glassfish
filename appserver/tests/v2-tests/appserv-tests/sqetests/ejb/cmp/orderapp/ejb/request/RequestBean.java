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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import jakarta.ejb.*;
import javax.naming.*;

import dataregistry.*;


public class RequestBean implements SessionBean {

    private SessionContext context;

    private LocalLineItemHome lineItemHome = null;

    private LocalOrderHome orderHome = null;

    private LocalPartHome partHome = null;

    private LocalVendorHome vendorHome = null;

    private LocalVendorPartHome vendorPartHome = null;


    /**
     * @see SessionBean#setSessionContext(SessionContext)
     */
    public void setSessionContext(SessionContext aContext) {
        context=aContext;
    }


    /**
     * @see SessionBean#ejbActivate()
     */
    public void ejbActivate() {

    }


    /**
     * @see SessionBean#ejbPassivate()
     */
    public void ejbPassivate() {

    }


    /**
     * @see SessionBean#ejbRemove()
     */
    public void ejbRemove() {

    }


    /**
     * See section 7.10.3 of the EJB 2.0 specification
     */
    public void ejbCreate() {
        try {
            lineItemHome   = lookupLineItem();
            orderHome      = lookupOrder();
            partHome       = lookupPart();
            vendorHome     = lookupVendor();
            vendorPartHome = lookupVendorPart();
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    public void createPart(PartRequest partRequest) {
        try {
            LocalPart part = partHome.create(partRequest.partNumber,
                    partRequest.revision, partRequest.description,
                    partRequest.revisionDate, partRequest.specification,
                    partRequest.drawing);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    public void addPartToBillOfMaterial(BomRequest bomRequest) {
        try {
            PartKey bomkey = new PartKey();
            bomkey.partNumber = bomRequest.bomPartNumber;
            bomkey.revision = bomRequest.bomRevision;

            LocalPart bom = partHome.findByPrimaryKey(bomkey);

            PartKey pkey = new PartKey();
            pkey.partNumber = bomRequest.partNumber;
            pkey.revision = bomRequest.revision;

            LocalPart part = partHome.findByPrimaryKey(pkey);
            part.setBomPart(bom);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    public void createVendor(VendorRequest vendorRequest) {
        try {
            LocalVendor vendor = vendorHome.create(vendorRequest.vendorId,
                    vendorRequest.name, vendorRequest.address,
                    vendorRequest.contact, vendorRequest.phone);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    public void createVendorPart(VendorPartRequest vendorPartRequest) {
        try {
            PartKey pkey = new PartKey();
            pkey.partNumber = vendorPartRequest.partNumber;
            pkey.revision = vendorPartRequest.revision;

            LocalPart part = partHome.findByPrimaryKey(pkey);
            LocalVendorPart vendorPart = vendorPartHome.create(
                    vendorPartRequest.description, vendorPartRequest.price,
                    part);

            VendorKey vkey = new VendorKey();
            vkey.vendorId = vendorPartRequest.vendorId;

            LocalVendor vendor = vendorHome.findByPrimaryKey(vkey);
            vendor.addVendorPart(vendorPart);

        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    public void createOrder(OrderRequest orderRequest) {
        try {
            LocalOrder order = orderHome.create(orderRequest.orderId,
                    orderRequest.status, orderRequest.discount,
                    orderRequest.shipmentInfo);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    public void addLineItem(LineItemRequest lineItemRequest) {
        try {
            LocalOrder order = orderHome.findByPrimaryKey(lineItemRequest.orderId);

            PartKey pkey = new PartKey();
            pkey.partNumber = lineItemRequest.partNumber;
            pkey.revision = lineItemRequest.revision;

            LocalPart part = partHome.findByPrimaryKey(pkey);

            LocalLineItem lineItem = lineItemHome.create(order, lineItemRequest.quantity,
                    part.getVendorPart());
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    public double getBillOfMaterialPrice(BomRequest bomRequest) {
        double price = 0.0;
        try {
            PartKey bomkey = new PartKey();
            bomkey.partNumber = bomRequest.bomPartNumber;
            bomkey.revision = bomRequest.bomRevision;

            LocalPart bom = partHome.findByPrimaryKey(bomkey);
            Collection parts = bom.getParts();
            for (Iterator iterator = parts.iterator(); iterator.hasNext();) {
                LocalPart part = (LocalPart)iterator.next();
                LocalVendorPart vendorPart = part.getVendorPart();
                price += vendorPart.getPrice();
            }

        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }

        return price;
    }

    public double getOrderPrice(Integer orderId) {
        double price = 0.0;
        try {
            LocalOrder order = orderHome.findByPrimaryKey(orderId);
            price = order.calculateAmmount();

        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }

        return price;
    }

    public void adjustOrderDiscount(int adjustment) {
        orderHome.adjustDiscount(adjustment);
    }

    public Double getAvgPrice() {
        return vendorPartHome.getAvgPrice();
    }

    public Double getTotalPricePerVendor(VendorRequest vendorRequest) {
        return vendorPartHome.getTotalPricePerVendor(vendorRequest.vendorId);
    }

    public Collection locateVendorsByPartialName(String name) {

        Collection names = new ArrayList();
        try {
            Collection vendors = vendorHome.findByPartialName(name);
            for (Iterator iterator = vendors.iterator(); iterator.hasNext();) {
                LocalVendor vendor = (LocalVendor)iterator.next();
                names.add(vendor.getName());
            }

        } catch (FinderException e) {
        }

        return names;
    }

    public int countAllItems() {
        int count = 0;
        try {
            count = lineItemHome.findAll().size();
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }

        return count;
    }

    public void removeOrder(Integer orderId) {
        try {
            orderHome.remove(orderId);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    public String reportVendorsByOrder(Integer orderId) {
        StringBuffer report = new StringBuffer();
        try {
            Collection vendors = vendorHome.findByOrder(orderId);
            for (Iterator iterator = vendors.iterator(); iterator.hasNext();) {
                LocalVendor vendor = (LocalVendor)iterator.next();
                report.append(vendor.getVendorId()).append(' ')
                      .append(vendor.getName()).append(' ')
                      .append(vendor.getContact()).append('\n');
            }

        } catch (FinderException e) {
            throw new EJBException(e.getMessage());
        }

        return report.toString();
    }

    private LocalLineItemHome lookupLineItem() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/SimpleLineItem");

        return (LocalLineItemHome) objref;
    }

    private LocalOrderHome lookupOrder() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/SimpleOrder");

        return (LocalOrderHome) objref;
    }

    private LocalPartHome lookupPart() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/SimplePart");

        return (LocalPartHome) objref;
    }

    private LocalVendorHome lookupVendor() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/SimpleVendor");

        return (LocalVendorHome) objref;
    }

    private LocalVendorPartHome lookupVendorPart() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/SimpleVendorPart");

        return (LocalVendorPartHome) objref;
    }

}
