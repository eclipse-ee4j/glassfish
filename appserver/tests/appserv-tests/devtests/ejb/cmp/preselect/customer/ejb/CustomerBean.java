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
import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;

public abstract class CustomerBean implements EntityBean {

    private EntityContext context;

    // Access methods for persistent fields
    public abstract String getId();
    public abstract void setId(String id);

    public abstract String getName();
    public abstract void setName(String name);

    // Access methods for relationship fields
    public abstract Collection getItems();
    public abstract void setItems(Collection items);

    // Select methods
    public abstract Collection ejbSelectItemsLessThan100(CustomerLocal customer)
        throws FinderException;

    // Business methods
    public int getItemsForLess() throws FinderException, RemoteException {
         int count = 0;
         CustomerLocal customer =
             (CustomerLocal)context.getEJBLocalObject();
         try {
           InitialContext initial = new InitialContext();
           Object objref = initial.lookup("java:comp/env/ejb/SimpleItem");
           ItemLocalHome itemHome =
             (ItemLocalHome)PortableRemoteObject.narrow(objref,
                                                        ItemLocalHome.class);
           ItemLocal item = itemHome.create("Dark Chocolate", "1", 20.00);
           getItems().add(item);

           item = itemHome.create("Milk Chocolate", "2", 30.00);
           getItems().add(item);

           item = itemHome.create("White Chocolate", "3", 40.00);
           getItems().add(item);

           System.out.println("calling ejbSelectItemsLessThan100...");
           Collection items = ejbSelectItemsLessThan100(customer);

           if (items != null) {

               for (Iterator iterator = items.iterator(); iterator.hasNext();) {
                   System.out.println("Item.price < 100 : " +
                                      ((ItemLocal)iterator.next()).getId());
                   count++;
               }
           }
           return count;

         } catch (Exception e) {
             throw new EJBException(e.getMessage());
         }
    }

    // EntityBean  methods
    public String ejbCreate (String id, String name) throws CreateException {

        System.out.print("CustomerBean ejbCreate");
        setId(id);
        setName(name);
        return null;
    }

    public void ejbPostCreate (String id, String name) throws CreateException { }

    public void setEntityContext(EntityContext ctx) {
        context = ctx;
    }

    public void unsetEntityContext() {
        context = null;
    }

    public void ejbRemove() {
        System.out.print("CustomerBean ejbRemove");
    }

    public void ejbLoad() {
        System.out.print("CustomerBean ejbLoad");
    }

    public void ejbStore() {
        System.out.print("CustomerBean ejbStore");


    }

    public void ejbPassivate() { }

    public void ejbActivate() { }
}
