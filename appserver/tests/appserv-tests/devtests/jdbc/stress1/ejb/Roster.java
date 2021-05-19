/*
 * Copyright (c) 2001, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.jdbc.stress1.ejb;

import java.util.*;
import jakarta.ejb.EJBObject;
import java.rmi.RemoteException;
import com.sun.s1asdev.jdbc.stress1.util.*;

public interface Roster extends EJBObject {

    // Players

    public void createPlayer(PlayerDetails details)
        throws RemoteException;

    public void addPlayer(String playerId, String teamId)
        throws RemoteException;

    public void removePlayer(String playerId)
        throws RemoteException;

    public void dropPlayer(String playerId, String teamId)
        throws RemoteException;

    public PlayerDetails getPlayer(String playerId)
        throws RemoteException;

    public ArrayList getPlayersOfTeam(String teamId)
        throws RemoteException;

    public ArrayList getPlayersOfTeamCopy(String teamId)
        throws RemoteException;

    public ArrayList getPlayersByPosition(String position)
        throws RemoteException;

    public ArrayList getPlayersByHigherSalary(String name)
        throws RemoteException;

    public ArrayList getPlayersBySalaryRange(double low, double high)
        throws RemoteException;

    public ArrayList getPlayersByLeagueId(String leagueId)
        throws RemoteException;

    public ArrayList getPlayersBySport(String sport)
        throws RemoteException;

    public ArrayList getPlayersByCity(String city)
        throws RemoteException;

    public ArrayList getAllPlayers()
        throws RemoteException;

    public ArrayList getPlayersNotOnTeam()
        throws RemoteException;

    public ArrayList getPlayersByPositionAndName(String position,
        String name) throws RemoteException;

    public ArrayList getLeaguesOfPlayer(String playerId)
        throws RemoteException;

    public ArrayList getSportsOfPlayer(String playerId)
        throws RemoteException;

    // Teams

    public ArrayList getTeamsOfLeague(String leagueId)
        throws RemoteException;

    public void createTeamInLeague(TeamDetails details, String leagueId)
        throws RemoteException;

    public void removeTeam(String teamId)
        throws RemoteException;

    public TeamDetails getTeam(String teamId)
        throws RemoteException;

    // Leagues

    public void createLeague(LeagueDetails details)
        throws RemoteException;

    public void removeLeague(String leagueId)
        throws RemoteException;

    public LeagueDetails getLeague(String leagueId)
        throws RemoteException;

    // Test

    public ArrayList testFinder(String parm1, String parm2, String parm3)
        throws RemoteException;
}
