/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.enterprise.iiop.util;
import org.glassfish.external.statistics.Statistic;

/**
 * This is the common base class for the collection of statistics
 * for the threadpool and orb connection manager.
 *
 * @author Pramod Gopinath
 */

public class ORBCommonStatsImpl {

    private GenericStatsImpl genericStatsDelegate;

    protected ORBCommonStatsImpl() {
    }

    protected void initialize(String statInterfaceName) {
        try{
           genericStatsDelegate =  new GenericStatsImpl(statInterfaceName, this);
        } catch(ClassNotFoundException cnfEx) {
            throw new RuntimeException(statInterfaceName + " not found", cnfEx);
        }
    }

    public Statistic getStatistic(String statName) {
        return genericStatsDelegate.getStatistic(statName);
    }

    public String[] getStatisticNames() {
        return genericStatsDelegate.getStatisticNames();
    }

    public Statistic[] getStatistics() {
        return genericStatsDelegate.getStatistics();
    }



}
