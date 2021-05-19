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

package roster;

import java.util.ArrayList;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.FinderException;
import jakarta.ejb.RemoveException;
import util.LeagueDetails;
import util.PlayerDetails;
import util.TeamDetails;
import java.util.Set;

public interface Roster extends EJBLocalObject {

    // Players

    public void createPlayer(PlayerDetails details)
        ;

    public void addPlayer(String playerId, String teamId)
        ;

    public void removePlayer(String playerId)
        ;

    public void dropPlayer(String playerId, String teamId)
        ;

    public PlayerDetails getPlayer(String playerId)
        ;

    public ArrayList getPlayersOfTeam(String teamId)
        ;

    public ArrayList getPlayersOfTeamCopy(String teamId)
        ;

    public ArrayList getPlayersByPosition(String position)
        ;

    public ArrayList getPlayersByHigherSalary(String name)
        ;

    public ArrayList getPlayersBySalaryRange(double low, double high)
        ;

    public ArrayList getPlayersByLeagueId(String leagueId)
        ;

    public ArrayList getPlayersBySport(String sport)
        ;

    public ArrayList getPlayersByCity(String city)
        ;

    public ArrayList getAllPlayers()
        ;

    public ArrayList getPlayersNotOnTeam()
        ;

    public ArrayList getPlayersByPositionAndName(String position,
        String name) ;

    public ArrayList getLeaguesOfPlayer(String playerId)
        ;

    public ArrayList getSportsOfPlayer(String playerId)
        ;

    public double getSalaryOfPlayerFromTeam(String teamID, String playerName)
        ;

    public ArrayList getPlayersOfLeague(String leagueId)
        ;


    public ArrayList getPlayersWithPositionsGoalkeeperOrDefender()
        ;

    public ArrayList getPlayersWithNameEndingWithON()
        ;

    public ArrayList getPlayersWithNullName()
        ;

    public ArrayList getPlayersWithTeam(String teamId)
        ;

    public ArrayList getPlayersWithSalaryUsingABS(double salary)
        ;

    public ArrayList getPlayersWithSalaryUsingSQRT(double salary)
        ;


    // Teams

    public ArrayList getTeamsOfLeague(String leagueId)
        ;

    public void createTeamInLeague(TeamDetails details, String leagueId)
        ;

    public void removeTeam(String teamId)
        ;

    public TeamDetails getTeam(String teamId)
        ;

    public ArrayList getTeamsByPlayerAndLeague(String playerKey,
                                               String leagueKey)
                                               ;

    public Set getCitiesOfLeague(String leagueKey) ;

    public TeamDetails getTeamOfLeagueByCity(String leagueKey, String city)
        ;

    public String getTeamsNameOfLeagueByCity(String leagueKey, String city)
        ;

    public  String getTeamNameVariations(String teamId) ;

    // Leagues

    public void createLeague(LeagueDetails details)
        ;

    public void removeLeague(String leagueId)
        ;

    public LeagueDetails getLeague(String leagueId)
        ;

    public LeagueDetails getLeagueByName(String name)
        ;

    // Test

    public ArrayList getPlayersByLeagueIdWithNULL(String leagueId)  ;

    public ArrayList testFinder(String parm1, String parm2, String parm3)
        ;

    public void cleanUp() throws FinderException, RemoveException;

}
