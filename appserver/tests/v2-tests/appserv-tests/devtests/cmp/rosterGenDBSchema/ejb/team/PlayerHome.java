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

public interface PlayerHome extends  jakarta.ejb.EJBHome   {

    public Player create (String id, String name, String position,
        double salary)
        throws CreateException, RemoteException;

    public Player findByPrimaryKey (String id)
        throws FinderException, RemoteException;

    public Collection findByPosition(String position)
        throws FinderException, RemoteException;

    public Collection findByHigherSalary(String name)
        throws FinderException, RemoteException;

    public Collection findBySalaryRange(double low, double high)
        throws FinderException, RemoteException;

//    public Collection findByLeague(League league)
//        throws FinderException, RemoteException;

    public Collection findBySport(String sport)
        throws FinderException, RemoteException;

    public Collection findByCity(String city)
        throws FinderException, RemoteException;

    public Collection findAll()
        throws FinderException, RemoteException;

    public Collection findNotOnTeam()
        throws FinderException, RemoteException;

    public Collection findByPositionAndName(String position,
        String name) throws FinderException, RemoteException;

    public Collection findByTest (String parm1, String parm2, String parm3)
        throws FinderException, RemoteException;



}
