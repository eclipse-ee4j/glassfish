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
import jakarta.ejb.*;

public interface LocalTeam extends EJBLocalObject {
    public String getTeamId();
    public String getName();
    public String getCity();
    public Collection getPlayers();
    public LocalLeague getLeague();

    public ArrayList getCopyOfPlayers();
    public void addPlayer(LocalPlayer player);
    public void dropPlayer(LocalPlayer player);
}
