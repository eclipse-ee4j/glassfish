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

package team;

import java.util.Collection;
import java.rmi.RemoteException;
import jakarta.ejb.CreateException;
import jakarta.ejb.FinderException;

public interface LeagueHome extends  jakarta.ejb.EJBHome   {

    public League create (String id, String name, String sport)
        throws CreateException, RemoteException;

    public League findByPrimaryKey (String id)
        throws FinderException, RemoteException;

    public Collection findAll()
        throws FinderException, RemoteException;

    public League findByName(String name)
        throws FinderException, RemoteException;

}
