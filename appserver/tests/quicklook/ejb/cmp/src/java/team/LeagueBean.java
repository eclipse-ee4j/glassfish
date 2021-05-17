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

package team;

import java.util.*;
import jakarta.ejb.*;
import javax.naming.*;
import util.Debug;

public abstract class LeagueBean implements EntityBean {

    private EntityContext context;

    // Access methods for persistent fields

    public abstract String getLeagueId();
    public abstract void setLeagueId(String id);

    public abstract String getName();
    public abstract void setName(String name);

    public abstract String getSport();
    public abstract void setSport(String sport);


    // Access methods for relationship fields

    public abstract Collection getTeams();
    public abstract void setTeams(Collection teams);

    // Select methods

    public abstract Set ejbSelectTeamsCity(LocalLeague league)
        throws FinderException;

    public abstract LocalTeam ejbSelectTeamByCity(String city)
        throws FinderException;

    public abstract String ejbSelectTeamsNameByCity(String city)
        throws FinderException;


    public abstract Set ejbSelectPlayersByLeague(LocalLeague league)
        throws FinderException;

    // Business methods

    public Set getCitiesOfThisLeague() throws FinderException {

         LocalLeague league =
             (team.LocalLeague)context.getEJBLocalObject();

         return ejbSelectTeamsCity(league);
    }


    public LocalTeam getTeamByCity(String city) throws FinderException {

        return ejbSelectTeamByCity(city);
    }

    public String getTeamsNameByCity(String city) throws FinderException {

        return ejbSelectTeamsNameByCity(city);
    }


    public Set getPlayersFromLeague() throws FinderException{

        LocalLeague league = (team.LocalLeague)context.getEJBLocalObject();

        return ejbSelectPlayersByLeague(league);
    }

    public void addTeam(LocalTeam team) {

        Debug.print("TeamBean addTeam");
        try {
            Collection teams = getTeams();
            teams.add(team);
        } catch (Exception ex) {
ex.printStackTrace();
            throw new EJBException(ex.getMessage());
        }
    }

    public void dropTeam(LocalTeam team) {

        Debug.print("TeamBean dropTeam");
        try {
            Collection teams = getTeams();
            teams.remove(team);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    // EntityBean  methods

    public String ejbCreate (String id, String name, String sport)
        throws CreateException {

        Debug.print("LeagueBean ejbCreate");
        setLeagueId(id);
        setName(name);
        setSport(sport);
        return null;
    }

    public void ejbPostCreate (String id, String name, String sport)
        throws CreateException { }

    public void setEntityContext(EntityContext ctx) {
        context = ctx;
    }

    public void unsetEntityContext() {
        context = null;
    }

    public void ejbRemove() {
        Debug.print("LeagueBean ejbRemove");
    }

    public void ejbLoad() {
        Debug.print("LeagueBean ejbLoad");
    }

    public void ejbStore() {
        Debug.print("LeagueBean ejbStore");
    }

    public void ejbPassivate() { }

    public void ejbActivate() { }

} // LeagueBean class
