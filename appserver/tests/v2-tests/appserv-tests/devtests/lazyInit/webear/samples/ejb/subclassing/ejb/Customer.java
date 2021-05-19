/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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


