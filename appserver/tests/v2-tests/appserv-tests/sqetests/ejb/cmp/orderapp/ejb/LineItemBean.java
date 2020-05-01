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

package dataregistry;

import jakarta.ejb.*;


public abstract class LineItemBean implements EntityBean {

    private EntityContext context;


    /**
     * @see EntityBean#setEntityContext(EntityContext)
     */
    public void setEntityContext(EntityContext aContext) {
        context=aContext;
    }


    /**
     * @see EntityBean#ejbActivate()
     */
    public void ejbActivate() {

    }


    /**
     * @see EntityBean#ejbPassivate()
     */
    public void ejbPassivate() {

    }


    /**
     * @see EntityBean#ejbRemove()
     */
    public void ejbRemove() {

    }


    /**
     * @see EntityBean#unsetEntityContext()
     */
    public void unsetEntityContext() {
        context=null;
    }


    /**
     * @see EntityBean#ejbLoad()
     */
    public void ejbLoad() {

    }


    /**
     * @see EntityBean#ejbStore()
     */
    public void ejbStore() {

    }

    public abstract Integer getOrderId();
    public abstract void setOrderId(Integer orderId);

    public abstract int getItemId();
    public abstract void setItemId(int itemId);

    public abstract int getQuantity();
    public abstract void setQuantity(int quantity);

    public abstract LocalVendorPart getVendorPart();
    public abstract void setVendorPart(LocalVendorPart vendorPart);

    public abstract LocalOrder getOrder();
    public abstract void setOrder(LocalOrder order);

    public LineItemKey ejbCreate(LocalOrder order, int quantity,
            LocalVendorPart vendorPart) throws CreateException {

        setOrderId(order.getOrderId());
        setItemId(order.getNextId());
        setQuantity(quantity);

        return null;
    }

    public void ejbPostCreate(LocalOrder order, int quantity,
            LocalVendorPart vendorPart) throws CreateException {

        setVendorPart(vendorPart);

        // This assignment is not necessary if the CMP container
        // treats setOrderId() as a relationship assignment.
        setOrder(order);
    }

}
