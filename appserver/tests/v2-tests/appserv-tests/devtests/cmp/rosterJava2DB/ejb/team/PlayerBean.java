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

public abstract class PlayerBean implements EntityBean {

    private EntityContext context;

    // Access methods for persistent fields

    public abstract String getPlayerId();
    public abstract void setPlayerId(String id);

    public abstract String getName();
    public abstract void setName(String name);

    public abstract String getPosition();
    public abstract void setPosition(String position);

    public abstract double getSalary();
    public abstract void setSalary(double salary);

    // Access methods for relationship fields

    public abstract Collection getTeams();
    public abstract void setTeams(Collection teams);

    // Select methods

    public abstract Collection ejbSelectLeagues(LocalPlayer player)
        throws FinderException;

    public abstract Collection ejbSelectSports(LocalPlayer player)
        throws FinderException;



    // Business methods

    public Collection getLeagues() throws FinderException {

         LocalPlayer player =
             (team.LocalPlayer)context.getEJBLocalObject();
         return ejbSelectLeagues(player);
    }

    public Collection getSports() throws FinderException {

         LocalPlayer player =
             (team.LocalPlayer)context.getEJBLocalObject();
         return ejbSelectSports(player);
    }

    // EntityBean  methods

    public String ejbCreate (String id, String name, String position,
        double salary) throws CreateException {

        Debug.print("PlayerBean ejbCreate");
        setPlayerId(id);
        setName(name);
        setPosition(position);
        setSalary(salary);
        return null;
    }

    public void ejbPostCreate (String id, String name, String position,
        double salary) throws CreateException { }

    public void setEntityContext(EntityContext ctx) {
        context = ctx;
    }

    public void unsetEntityContext() {
        context = null;
    }

    public void ejbRemove() {
        Debug.print("PlayerBean ejbRemove");
    }

    public void ejbLoad() {
        Debug.print("PlayerBean ejbLoad");
    }

    public void ejbStore() {
        Debug.print("PlayerBean ejbStore");
    }

    public void ejbPassivate() { }

    public void ejbActivate() { }

} // PlayerBean class
