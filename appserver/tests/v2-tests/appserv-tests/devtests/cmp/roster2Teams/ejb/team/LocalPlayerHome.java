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

public interface LocalPlayerHome extends EJBLocalHome {

    public LocalPlayer create (String id, String name, String position,
        double salary)
        throws CreateException;

    public LocalPlayer findByPrimaryKey (String id)
        throws FinderException;

    public Collection findByPosition(String position)
        throws FinderException;

    public Collection findByHigherSalary(String name)
        throws FinderException;

    public Collection findBySalaryRange(double low, double high)
        throws FinderException;

    public Collection findByLeague(LocalLeague league)
        throws FinderException;

    public Collection findBySport(String sport)
        throws FinderException;

    public Collection findByCity(String city)
        throws FinderException;

    public Collection findAll()
        throws FinderException;

    public Collection findNotOnTeam()
        throws FinderException;

    public Collection findByPositionAndName(String position,
        String name) throws FinderException;

    public Collection findByTest (String parm1, String parm2, String parm3)
        throws FinderException;

    public Collection findByPositionsGoalkeeperOrDefender()
        throws FinderException;

    public Collection findByNameEndingWithON()
        throws FinderException;

    public Collection findByNullName()
        throws FinderException;

    public Collection findByTeam(LocalTeam team)
        throws FinderException;

    public Collection findBySalarayWithArithmeticFunctionABS(double salaray)
        throws FinderException;

    public Collection findBySalarayWithArithmeticFunctionSQRT(double salaray)
        throws FinderException;

}
