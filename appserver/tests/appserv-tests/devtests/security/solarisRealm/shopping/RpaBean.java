/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * ShoppingEJB.java
 *
 * Created on May 15, 2003, 5:16 PM
 */

package shopping;

import jakarta.ejb.SessionContext;
import jakarta.ejb.SessionBean;
import java.util.Vector;
import java.lang.String;
import java.util.Iterator;
import jakarta.ejb.EJBException;
import java.rmi.RemoteException;
/**
 *
 * @author  Harpreet
 * @version
 */

public class RpaBean implements SessionBean {

    private String shopper = "anonymous";
    private int totalPrice = 0;

    private int totalItems = 0;

    private Vector items;

    private Vector itemPrice;

    private SessionContext sc = null;

    /** Creates a new instance of ShoppingEJB */
    public void ejbCreate(String shopperName) {
        shopper = shopperName;
        items = new Vector();
        itemPrice = new Vector();
    }

    public void addItem(java.lang.String item, int price) throws EJBException,
        RemoteException{
        items.add(item);
        itemPrice.add(new Integer(price));
        totalItems++;
        totalPrice += price;
        System.out.println(" Shopping Cart: Shopper "+ shopper +" has bought "
            + item +" for price ="+ price +" .Total Items = "+totalItems +
            " .TotalPrice = " + totalPrice);

        System.out.println("Caller Princial = "+sc.getCallerPrincipal());
    }

    public void deleteItem(java.lang.String item) throws EJBException,
        RemoteException{
        int index = items.indexOf(item);
        items.remove(item);
        Integer price = (Integer) itemPrice.get(index);
        System.out.println("Shopping Cart: Removing item "+ item +" @price "+
            price.intValue());
        totalPrice -= price.shortValue();
        itemPrice.remove(index);
        System.out.println(" Shopping Cart: Shopper "+ shopper +"  .Total Items = "+totalItems +
            " .TotalPrice = " + totalPrice);
        System.out.println("Caller Princial = "+sc.getCallerPrincipal());
    }

    public double getTotalCost() throws EJBException{
        System.out.println("Caller Princial = "+sc.getCallerPrincipal());

        return totalPrice;
    }

    public String[] getItems() throws EJBException{
        System.out.println("Caller Princial = "+sc.getCallerPrincipal());

        Iterator it = items.iterator();
        int sz = items.size();
        String[] itemNames = new String[sz];
        for(int i=0; it.hasNext();){
            itemNames[i++] = new String( (String)it.next());
        }
        return itemNames;
    }

    public void ejbActivate() {
        System.out.println("In Rpa ejbActivate");
    }


    public void ejbPassivate() {
        System.out.println("In Rpa ejbPassivate");
    }


    public void ejbRemove()  {
        System.out.println("In Rpa ejbRemove");
    }


    public void setSessionContext(jakarta.ejb.SessionContext sessionContext) {
        sc = sessionContext;
    }

}
