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

package samples.ejb.subclassing.ejb;

import java.rmi.RemoteException;

public interface Customer extends jakarta.ejb.EJBObject
{
  public String getLastName() throws RemoteException;

  public String getFirstName() throws RemoteException;

  public String getAddress1() throws RemoteException;

  public String getAddress2() throws RemoteException;

  public String getCity() throws RemoteException;

  public String getState() throws RemoteException;

  public String getZipCode() throws RemoteException;

  public String getSSN() throws RemoteException;

  public long getSavingsBalance() throws RemoteException;

  public long getCheckingBalance() throws RemoteException;

  public void doCredit(long amount, String accountType) throws RemoteException;

  public void doDebit(long amount, String accountType) throws RemoteException;

}


