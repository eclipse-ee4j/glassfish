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
import java.util.Set;
import jakarta.ejb.FinderException;


public interface League extends  jakarta.ejb.EJBObject   {

    public String getLeagueId() throws RemoteException;
    public String getName() throws RemoteException;
    public String getSport() throws RemoteException;
    public Collection getTeams() throws RemoteException;

    public Team getRemoteTeamByCity(String city) throws FinderException,
                                                        RemoteException;

    public Set getRemoteTeamsOfThisLeague() throws FinderException,
                                                    RemoteException;

    public Collection getRemotePlayersFromLeague() throws FinderException,
                                                   RemoteException;

//    public void addTeam(Team team) throws RemoteException;
//    public void dropTeam(Team team) throws RemoteException;

//    public Set getCitiesOfThisLeague() throws FinderException;
//    public String getTeamsNameByCity(String city) throws FinderException;
}
