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

// AccountManagerImpl.java
import org.omg.PortableServer.*;

import java.util.*;

public class AccountManagerImpl extends Bank.AccountManagerPOA {

  public java.util.Hashtable getAccounts () {
    return _accounts;
  }

  public Bank.Account get (java.lang.String arg0) {
    return (Bank.Account) _accounts.get(arg0);
  }

  public synchronized Bank.Account create (Bank.AccountData arg0) {
    // Lookup the account in the account dictionary.
    Bank.Account account = (Bank.Account) _accounts.get(arg0.getName());
    // If there was no account in the dictionary, create one.
    if(account == null) {
      // Create the account implementation, given the balance.
      AccountImpl accountServant = new AccountImpl(arg0);
      try {
        // Activate it on the default POA which is root POA for this servant
        account = Bank.AccountHelper.narrow(_default_POA().servant_to_reference(accountServant));
      } catch (Exception e) {
        e.printStackTrace();
      }
      // Print out the new account.
      System.out.println("Created " + arg0.getName() + "'s account: " + account);
      // Save the account in the account dictionary.
      _accounts.put(arg0.getName(), account);
    }
    // Return the account.
    return account;
  }
  private Hashtable _accounts = new Hashtable();
}

