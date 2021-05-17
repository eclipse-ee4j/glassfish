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

// AccountImpl.java

public class AccountImpl extends Bank.AccountPOA {

  public AccountImpl(Bank.AccountData data) {
    _name = data.getName();
    _balance = data.getBalance();
  }

  public String name () throws java.rmi.RemoteException {
    return _name;
  }

  public float getBalance () throws java.rmi.RemoteException {
    return _balance;
  }

  public void setBalance (float balance) throws java.rmi.RemoteException {
    _balance = balance;
  }

  private float _balance;
  private String _name;
}

