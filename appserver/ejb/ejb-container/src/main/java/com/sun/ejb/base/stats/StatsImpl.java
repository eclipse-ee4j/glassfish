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

package com.sun.ejb.base.stats;

import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;

import java.util.logging.Logger;

import org.glassfish.j2ee.statistics.BoundedRangeStatistic;
import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.Statistic;
import org.glassfish.j2ee.statistics.Stats;

/**
 * Base class for all the StatsImpl. Also provides a couple
 *  of methods for MonitorListener
 *
 * @author Mahesh Kannan
 */
public abstract class StatsImpl implements Stats {
    protected static final Logger _logger = EjbContainerUtilImpl.getLogger();

    private GenericStatsImpl genericStatsDelegate;

    protected StatsImpl() {
    }

    protected void initialize(String statInterfaceName) {
        try {
            genericStatsDelegate =  new GenericStatsImpl(statInterfaceName, this);
        } catch(ClassNotFoundException cnfEx) {
            throw new RuntimeException(statInterfaceName + " not found", cnfEx);
        }
    }

    @Override
    public Statistic getStatistic(String statName) {
        return genericStatsDelegate.getStatistic(statName);
    }

    @Override
    public String[] getStatisticNames() {
        return genericStatsDelegate.getStatisticNames();
    }

    @Override
    public Statistic[] getStatistics() {
        return genericStatsDelegate.getStatistics();
    }

    public String statToString() {
        StringBuffer sbuf = new StringBuffer();
        Statistic[] stats = getStatistics();
        int sz = stats.length;
        for (int i = 0; i < sz; i++) {
            if (stats[i] instanceof CountStatistic) {
                CountStatistic stat = (CountStatistic) stats[i];
                sbuf.append(stat.getName()).append("=").append(stat.getCount()).append("; ");
            } else if (stats[i] instanceof BoundedRangeStatistic) {
                BoundedRangeStatistic stat = (BoundedRangeStatistic) stats[i];
                sbuf.append(stat.getName()).append("=").append(stat.getCurrent()).append("; ");
            } else {
                sbuf.append(stats[i].getName()).append("=?");
            }
        }

        return sbuf.toString();
    }

}
