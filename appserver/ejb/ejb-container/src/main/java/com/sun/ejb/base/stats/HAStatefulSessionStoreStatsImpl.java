/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.ejb.spi.stats.MonitorableSFSBStoreManager;
import com.sun.enterprise.admin.monitor.stats.AverageRangeStatistic;
import com.sun.enterprise.admin.monitor.stats.BoundedRangeStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableAverageRangeStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;

import org.glassfish.j2ee.statistics.CountStatistic;

/**
 * Implementation of StatefulSessionStoreStats
 * There is once instance of this class per StatefulEJBContainer
 *
 * @author Mahesh Kannan
 */
public class HAStatefulSessionStoreStatsImpl extends StatefulSessionStoreStatsImpl
    implements com.sun.enterprise.admin.monitor.stats.HAStatefulSessionStoreStats {

    private MutableCountStatisticImpl checkpointCount;
    private MutableCountStatisticImpl checkpointSuccessCount;
    private MutableCountStatisticImpl checkpointErrorCount;
    private MutableAverageRangeStatisticImpl checkpointSize;
    private MutableAverageRangeStatisticImpl checkpointTime;

    private final Object checkpointCountLock = new Object();
    private final Object checkpointSizeLock = new Object();
    private final Object checkpointTimeLock = new Object();

    private long checkpointCountVal;
    private long checkpointSuccessCountVal;
    private long checkpointErrorCountVal;

    public HAStatefulSessionStoreStatsImpl(MonitorableSFSBStoreManager provider) {
        super(provider, "com.sun.enterprise.admin.monitor.stats.HAStatefulSessionStoreStats");
        initialize();
    }


    @Override
    protected void initialize() {
        super.initialize();

        synchronized (checkpointCountLock) {
            checkpointCount = new MutableCountStatisticImpl(new CountStatisticImpl("CheckpointCount"));
            checkpointSuccessCount = new MutableCountStatisticImpl(new CountStatisticImpl("CheckpointSuccessCount"));
            checkpointErrorCount = new MutableCountStatisticImpl(new CountStatisticImpl("CheckpointErrorCount"));
        }

        synchronized (checkpointTimeLock) {
            checkpointTime = new MutableAverageRangeStatisticImpl(new BoundedRangeStatisticImpl(0, 0, 0, 0, 0,
                "CheckpointTime", "millis", "Time spent on checkpointing", 0, 0));
        }

        synchronized (checkpointSizeLock) {
            checkpointSize = new MutableAverageRangeStatisticImpl(new BoundedRangeStatisticImpl(0, 0, 0, 0, 0,
                "CheckpointSize", "millis", "Number of bytes checkpointed", 0, 0));
        }
    }


    /**
     * Returns the total number of sessions checkpointed into the store
     */
    @Override
    public CountStatistic getCheckpointCount() {
        synchronized (checkpointCountLock) {
            checkpointCount.setCount(checkpointCountVal);
            return (CountStatistic) checkpointCount.unmodifiableView();
        }
    }


    /**
     * Returns the total number of sessions successfully Checkpointed into the store
     */
    @Override
    public CountStatistic getCheckpointSuccessCount() {
        synchronized (checkpointCountLock) {
            checkpointSuccessCount.setCount(checkpointSuccessCountVal);
            return (CountStatistic) checkpointSuccessCount.unmodifiableView();
        }
    }


    /**
     * Returns the total number of sessions that couldn't be Checkpointed into the store
     */
    @Override
    public CountStatistic getCheckpointErrorCount() {
        synchronized (checkpointCountLock) {
            checkpointErrorCount.setCount(checkpointErrorCountVal);
            return (CountStatistic) checkpointErrorCount.unmodifiableView();
        }
    }


    /**
     * Returns the number of bytes checkpointed
     */
    @Override
    public AverageRangeStatistic getCheckpointedBeanSize() {
        synchronized (checkpointTimeLock) {
            return (AverageRangeStatistic) checkpointSize.unmodifiableView();
        }
    }


    /**
     * Returns the time spent on passivating beans to the store including total, min, max
     */
    @Override
    public AverageRangeStatistic getCheckpointTime() {
        synchronized (checkpointTimeLock) {
            return (AverageRangeStatistic) checkpointTime.unmodifiableView();
        }
    }


    public void incrementCheckpointCount(boolean success) {
        synchronized (checkpointCountLock) {
            checkpointCountVal++;
            if (success) {
                checkpointSuccessCountVal++;
            } else {
                checkpointErrorCountVal++;
            }
        }
    }


    public void setCheckpointSize(long val) {
        synchronized (checkpointSizeLock) {
            checkpointSize.setCount(val);
        }
    }


    public void setCheckpointTime(long val) {
        synchronized (checkpointTimeLock) {
            checkpointTime.setCount(val);
        }
    }


    @Override
    protected void appendStats(StringBuffer sbuf) {
        super.appendStats(sbuf);
        sbuf.append("CheckpointCount: ").append(checkpointCountVal).append("; ")
            .append("CheckpointSuccessCount: ").append(checkpointSuccessCountVal).append("; ")
            .append("CheckpointErrorCount: ").append(checkpointErrorCountVal).append("; ");
        appendTimeStatistic(sbuf, "CheckpointTime", checkpointTime);
    }
}
