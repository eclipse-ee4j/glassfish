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

/*
 * Rpa.java
 *
 * Created on May 15, 2003, 5:09 PM
 */

package com.sun.devtest.admin.synchronization.api.security.shopping;
import jakarta.ejb.EJBObject;
/**
 * Shopping Cart Stateful Session Bean. Just tests -Dj2eelogin.name
 *  -Dj2eelogin.password system properties.
 * @author  hsingh
 */
public interface RpaRemote extends EJBObject {

    public void addItem(java.lang.String item, int price) throws java.rmi.RemoteException;

    public void deleteItem(java.lang.String item) throws java.rmi.RemoteException;

    public double getTotalCost() throws java.rmi.RemoteException;

    public java.lang.String[] getItems() throws java.rmi.RemoteException;
}
