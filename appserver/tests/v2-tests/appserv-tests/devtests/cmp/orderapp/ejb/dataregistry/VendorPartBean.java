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


public abstract class VendorPartBean implements EntityBean {

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

    public abstract String getDescription();
    public abstract void setDescription(String description);

    public abstract double getPrice();
    public abstract void setPrice(double price);

    public abstract LocalPart getPart();
    public abstract void setPart(LocalPart part);

    public abstract LocalVendor getVendor();
    public abstract void setVendor(LocalVendor vendor);

    public Object ejbCreate(String description, double price, LocalPart part)
        throws CreateException {

        setDescription(description);
        setPrice(price);

        return null;
    }

    public void ejbPostCreate(String description, double price, LocalPart part)
        throws CreateException {

        setPart(part);
    }

    public abstract Double ejbSelectAvgPrice() throws FinderException;

    public abstract Double ejbSelectTotalPricePerVendor(int vendorId) throws FinderException;

    public Double ejbHomeGetAvgPrice() throws FinderException {
        return ejbSelectAvgPrice();
    }

    public Double ejbHomeGetTotalPricePerVendor(int vendorId) throws FinderException {
        return ejbSelectTotalPricePerVendor(vendorId);
    }
}
