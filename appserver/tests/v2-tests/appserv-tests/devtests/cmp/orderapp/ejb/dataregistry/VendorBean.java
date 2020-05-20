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

import java.util.Collection;

import jakarta.ejb.*;


public abstract class VendorBean implements EntityBean {

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

    public abstract int getVendorId();
    public abstract void setVendorId(int vendorId);

    public abstract String getName();
    public abstract void setName(String name);

    public abstract String getAddress();
    public abstract void setAddress(String address);

    public abstract String getContact();
    public abstract void setContact(String contact);

    public abstract String getPhone();
    public abstract void setPhone(String phone);

    public abstract Collection getVendorParts();
    public abstract void setVendorParts(Collection vendorParts);

    public VendorKey ejbCreate(int vendorId, String name, String address,
            String contact, String phone) throws CreateException {

        setVendorId(vendorId);
        setName(name);
        setAddress(address);
        setContact(contact);
        setPhone(phone);

        return null;
    }

    public void ejbPostCreate(int vendorId, String name, String address,
            String contact, String phone) throws CreateException {
    }

    public void addVendorPart(LocalVendorPart part) {
        getVendorParts().add(part);
    }
}
