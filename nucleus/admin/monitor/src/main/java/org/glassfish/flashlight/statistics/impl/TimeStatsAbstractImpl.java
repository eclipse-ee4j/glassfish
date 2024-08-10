/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.flashlight.statistics.impl;

import java.util.concurrent.atomic.AtomicLong;

import org.glassfish.flashlight.datatree.impl.AbstractTreeNode;
import org.glassfish.flashlight.statistics.Average;
import org.glassfish.flashlight.statistics.TimeStats;
import org.glassfish.flashlight.statistics.factory.AverageFactory;

/**
 * @author Harpreet Singh
 */
public abstract class TimeStatsAbstractImpl extends AbstractTreeNode implements TimeStats {

    private Average average = AverageFactory.createAverage();

    private AtomicLong lastSampleTime = new AtomicLong(0);
    protected long startTime = 0;

    private ThreadLocalTimeStatData individualData = new ThreadLocalTimeStatData();

    private static class ThreadLocalTimeStatData extends ThreadLocal<TimeStatData> {

        private TimeStatData tsd;

        protected TimeStatData initialValue() {
            tsd = new TimeStatData();
            return tsd;
        }

        public TimeStatData get() {
            if (tsd == null)
                tsd = new TimeStatData();
            return tsd;
        }

    }

    protected static final String NEWLINE = System.getProperty("line.separator");

    public double getTime() {
        return average.getAverage();
    }

    abstract public void entry();

    abstract public void exit();

    protected void postEntry(long entryTime) {
        if (startTime == 0) {
            startTime = entryTime;
        }
        this.setLastSampleTime(entryTime);
        individualData.get().setEntryTime(entryTime);
    }

    public void postExit(long exitTime) {
        TimeStatData tsd = individualData.get();
        tsd.setExitTime(exitTime);
        average.addDataPoint(tsd.getTotalTime());
    }

    public long getMinimumTime() {
        return average.getMin();
    }

    public long getMaximumTime() {
        return average.getMax();
    }

    // only for testing purposes.
    public void setTime(long time) {
        //  System.err.println ("setTime only for Testing purposes");
        individualData.get().setTotalTime(time);
        average.addDataPoint(time);
    }

    public void setReset(boolean reset) {
        average.setReset();
        individualData.get().setReset();
    }

    public long getTimesCalled() {
        return average.getSize();
    }

    // Implementations for TimeStatistic
    public long getCount() {
        return getTimesCalled();
    }

    public long getMaxTime() {
        return getMaximumTime();
    }

    public long getMinTime() {
        return getMinimumTime();
    }

    public long getTotalTime() {
        return average.getTotal();
    }

    public long getLastSampleTime() {
        return this.lastSampleTime.get();
    }

    public long getStartTime() {
        return this.startTime;
    }

    private void setLastSampleTime(long time) {
        this.lastSampleTime.set(time);
    }

    private static class TimeStatData {
        private long entryTime = 0;
        private long exitTime = 0;
        private long totalTime = 0;

        public long getEntryTime() {
            return entryTime;
        }

        public void setEntryTime(long entryTime) {
            this.entryTime = entryTime;
        }

        public long getExitTime() {
            return exitTime;
        }

        public void setExitTime(long exitTime) {
            this.exitTime = exitTime;
        }

        public long getTotalTime() {
            totalTime = exitTime - entryTime;
            return totalTime;
        }

        public void setTotalTime(long totalTime) {
            this.totalTime = totalTime;
        }

        public void setReset() {
            entryTime = 0;
            exitTime = 0;
            totalTime = 0;
        }
    }
}
