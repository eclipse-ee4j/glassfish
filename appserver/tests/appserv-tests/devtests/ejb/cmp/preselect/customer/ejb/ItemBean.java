/*
 * Copyright (c) 2001, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.ejb.cmp.preselect.ejb;

import java.util.*;
import jakarta.ejb.*;
import javax.naming.*;

public abstract class ItemBean implements EntityBean {

    private EntityContext context;

    // Access methods for persistent fields
    public abstract String getId();
    public abstract void setId(String id);

    public abstract String getName();
    public abstract void setName(String name);

    public abstract double getPrice();
    public abstract void setPrice(double price);


    // Business methods
    public void modifyPrice(double newPrice) {
        setPrice(newPrice);
    }


    // EntityBean  methods
    public String ejbCreate (String id, String name, double price) throws CreateException {

        System.out.print("ItemBean ejbCreate");
        setId(id);
        setName(name);
        setPrice(price);
        return null;
    }

    public void ejbPostCreate (String id, String name, double price) throws CreateException { }

    public void setEntityContext(EntityContext ctx) {
        context = ctx;
    }

    public void unsetEntityContext() {
        context = null;
    }

    public void ejbRemove() {
        System.out.print("ItemBean ejbRemove");
    }

    public void ejbLoad() {
        System.out.print("ItemBean ejbLoad");
    }

    public void ejbStore() {
        System.out.print("ItemBean ejbStore");
        ItemLocal item = (ItemLocal)context.getEJBLocalObject();

        System.out.println("Item price less than $100 : " +
                           item.getId());
        System.out.println("Modifying its price to $200...");
        item.modifyPrice(200.00);
    }

    public void ejbPassivate() { }

    public void ejbActivate() { }
}
