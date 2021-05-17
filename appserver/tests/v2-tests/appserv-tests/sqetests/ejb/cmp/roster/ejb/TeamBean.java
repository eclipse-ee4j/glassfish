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

package com.sun.s1peqe.ejb.cmp.roster.ejb;

import java.util.*;
import jakarta.ejb.*;
import javax.naming.*;
import com.sun.s1peqe.ejb.cmp.roster.util.Debug;
import com.sun.s1peqe.ejb.cmp.roster.util.PlayerDetails;

public abstract class TeamBean implements EntityBean {

    private EntityContext context;

    // Access methods for persistent fields

    public abstract String getTeamId();
    public abstract void setTeamId(String id);

    public abstract String getName();
    public abstract void setName(String name);

    public abstract String getCity();
    public abstract void setCity(String city);


    // Access methods for relationship fields

    public abstract Collection getPlayers();
    public abstract void setPlayers(Collection players);

    public abstract LocalLeague getLeague();
    public abstract void setLeague(LocalLeague league);

    // Business methods

    public ArrayList getCopyOfPlayers() {
        Debug.print("TeamBean getCopyOfPlayers");
        ArrayList playerList = new ArrayList();
        Collection players = getPlayers();

        Iterator i = players.iterator();
        while (i.hasNext()) {
            LocalPlayer player = (LocalPlayer) i.next();
            PlayerDetails details = new PlayerDetails(player.getPlayerId(),
                                                      player.getName(),
                                                      player.getPosition(),
                                                      0.00);
            playerList.add(details);
        }
        return playerList;
    }

    public void addPlayer(LocalPlayer player) {
        Debug.print("TeamBean addPlayer");
        try {
            Collection players = getPlayers();
            players.add(player);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void dropPlayer(LocalPlayer player) {
        Debug.print("TeamBean dropPlayer");
        try {
            Collection players = getPlayers();
            players.remove(player);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    // EntityBean  methods
    public String ejbCreate (String id, String name, String city)
        throws CreateException {

        Debug.print("TeamBean ejbCreate");
        setTeamId(id);
        setName(name);
        setCity(city);
        return null;
    }

    public void ejbPostCreate (String id, String name, String city)
        throws CreateException { }

    public void setEntityContext(EntityContext ctx) {
        context = ctx;
    }

    public void unsetEntityContext() {
        context = null;
    }

    public void ejbRemove() {
        Debug.print("TeamBean ejbRemove");
    }

    public void ejbLoad() {
        Debug.print("TeamBean ejbLoad");
    }

    public void ejbStore() {
        Debug.print("TeamBean ejbStore");
    }

    public void ejbPassivate() { }
    public void ejbActivate() { }
}
