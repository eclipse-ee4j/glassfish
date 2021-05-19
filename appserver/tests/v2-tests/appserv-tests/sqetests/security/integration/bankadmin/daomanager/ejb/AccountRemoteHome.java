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

package com.sun.s1peqe.security.integration.bankadmin.daomanager;

import java.util.Collection;
import jakarta.ejb.CreateException;
import java.rmi.RemoteException;
import jakarta.ejb.EJBHome;
import java.util.Date;
import jakarta.ejb.FinderException;

public interface AccountRemoteHome extends jakarta.ejb.EJBHome
{
    public AccountRemote createAccount (AccountDataObject dao)
    throws jakarta.ejb.CreateException,RemoteException;
        public AccountRemote findByPrimaryKey(String id) throws jakarta.ejb.FinderException,RemoteException;

}
