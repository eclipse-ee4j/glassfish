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
import com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;

import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.RangeStatistic;

/**
 * Implementation of StatefulSessionStoreStats
 *  There is once instance of this class per StatefulEJBContainer
 *
 * @author Mahesh Kannan
 */
public class StatefulSessionStoreStatsImpl
    extends StatsImpl
    implements com.sun.enterprise.admin.monitor.stats.StatefulSessionStoreStats {

    private MonitorableSFSBStoreManager provider;

    private MutableBoundedRangeStatisticImpl currentSize;

    private MutableCountStatisticImpl activationCount;
    private MutableCountStatisticImpl activationSuccessCount;
    private MutableCountStatisticImpl activationErrorCount;

    private MutableCountStatisticImpl passivationCount;
    private MutableCountStatisticImpl passivationSuccessCount;
    private MutableCountStatisticImpl passivationErrorCount;

    private MutableCountStatisticImpl expiredSessionCount;

    private MutableAverageRangeStatisticImpl activationSize;
    private MutableAverageRangeStatisticImpl activationTime;

    private MutableAverageRangeStatisticImpl passivationSize;
    private MutableAverageRangeStatisticImpl passivationTime;

    private Object currentSizeLock = new Object();

    private Object activationCountLock = new Object();
    private Object activationSizeLock = new Object();
    private Object activationTimeLock = new Object();

    private Object passivationCountLock = new Object();
    private Object passivationSizeLock = new Object();
    private Object passivationTimeLock = new Object();

    private Object expiredSessionCountLock = new Object();

    private long activationCountVal;
    private long activationSuccessCountVal;
    private long activationErrorCountVal;

    private long passivationCountVal;
    private long passivationSuccessCountVal;
    private long passivationErrorCountVal;

    private long expiredSessionCountVal;

    public StatefulSessionStoreStatsImpl(MonitorableSFSBStoreManager provider) {
        this.provider = provider;
        initialize();
    }


    protected StatefulSessionStoreStatsImpl(MonitorableSFSBStoreManager provider, String intfName) {
        this.provider = provider;
    }

    protected void initialize() {

        long now = System.currentTimeMillis();

        synchronized (currentSizeLock) {
            currentSize = new MutableBoundedRangeStatisticImpl(
                new BoundedRangeStatisticImpl(0, 0, Long.MAX_VALUE,
                    Long.MAX_VALUE, 0, "CurrentSize",
                    "bytes", "Number of sessions in store", now, now)
                );
        }

        synchronized (activationCountLock) {
            activationCount = new MutableCountStatisticImpl(
                new CountStatisticImpl("ActivationCount"));
            activationSuccessCount = new MutableCountStatisticImpl(
                new CountStatisticImpl("ActivationSuccessCount"));
            activationErrorCount = new MutableCountStatisticImpl(
                new CountStatisticImpl("ActivationErrorCount"));
        }

        synchronized (activationSizeLock) {
            activationSize = new MutableAverageRangeStatisticImpl(
                new BoundedRangeStatisticImpl(0, 0, Long.MAX_VALUE,
                    Long.MAX_VALUE, 0, "ActivationSize",
                    "bytes", "Number of bytes activated", now, now)
                );
        }

        synchronized (passivationSizeLock) {
            passivationSize = new MutableAverageRangeStatisticImpl(
                new BoundedRangeStatisticImpl(0, 0, Long.MAX_VALUE,
                    Long.MAX_VALUE, 0, "PassivationSize",
                    "bytes", "Number of bytes passivated", now, now)
                );
        }

        synchronized (passivationCountLock) {
            passivationCount = new MutableCountStatisticImpl(
                new CountStatisticImpl("PassivationCount"));
            passivationSuccessCount = new MutableCountStatisticImpl(
                new CountStatisticImpl("PassivationSuccessCount"));
            passivationErrorCount = new MutableCountStatisticImpl(
                new CountStatisticImpl("PassivationErrorCount"));
        }

        synchronized (expiredSessionCountLock) {
            expiredSessionCount = new MutableCountStatisticImpl(
                new CountStatisticImpl("ExpiredSessionCount"));
        }

        synchronized (activationTimeLock) {
            activationTime = new MutableAverageRangeStatisticImpl(
                new BoundedRangeStatisticImpl(0, 0, Long.MAX_VALUE,
                    Long.MAX_VALUE, 0, "ActivationTime",
                    "millis", "Time spent on activation", now, now)
                );
        }

        synchronized (passivationTimeLock) {
            passivationTime = new MutableAverageRangeStatisticImpl(
                new BoundedRangeStatisticImpl(0, 0, Long.MAX_VALUE,
                    Long.MAX_VALUE, 0, "PassivationTime",
                    "millis", "Time spent on passivation", now, now)
                );
        }
    }

    /**
     * Returns the number of passivated / checkpointed sessions in the store
     */
    public RangeStatistic getCurrentSize() {
        synchronized (currentSizeLock) {
            currentSize.setCount(provider.getCurrentStoreSize());
            return (RangeStatistic) currentSize.unmodifiableView();
        }
    }

    /**
     * Returns the total number of sessions activated from the store
     */
    public CountStatistic getActivationCount() {
        synchronized (activationCountLock) {
            activationCount.setCount(activationCountVal);
            return (CountStatistic) activationCount.unmodifiableView();
        }
    }

    /**
     * Returns the total number of sessions successfully Activated from the store
     */
    public CountStatistic getActivationSuccessCount() {
        synchronized (activationCountLock) {
            activationSuccessCount.setCount(activationSuccessCountVal);
            return (CountStatistic) activationSuccessCount.unmodifiableView();
        }
    }

    /**
     * Returns the total number of sessions that couldn't be Activated from the store
     */
    public CountStatistic getActivationErrorCount() {
        synchronized (activationCountLock) {
            activationErrorCount.setCount(activationErrorCountVal);
            return (CountStatistic) activationErrorCount.unmodifiableView();
        }
    }

    /**
     * Returns the total number of sessions passivated using this store
     */
    public CountStatistic getPassivationCount() {
        synchronized (passivationCountLock) {
            passivationCount.setCount(passivationCountVal);
            return (CountStatistic) passivationSuccessCount.unmodifiableView();
        }
    }

    /**
     * Returns the total number of sessions successfully Passivated using the store
     */
    public CountStatistic getPassivationSuccessCount() {
        synchronized (passivationCountLock) {
            passivationSuccessCount.setCount(passivationSuccessCountVal);
            return (CountStatistic) passivationSuccessCount.unmodifiableView();
        }
    }

    /**
     * Returns the total number of sessions that couldn't be Passivated using the store
     */
    public CountStatistic getPassivationErrorCount() {
        synchronized (passivationCountLock) {
            passivationErrorCount.setCount(passivationErrorCountVal);
            return (CountStatistic) passivationSuccessCount.unmodifiableView();
        }
    }

    /**
     * Returns the total number of expired sessions that were removed by this store
     */
    public CountStatistic getExpiredSessionCount() {
        synchronized (expiredSessionCountLock) {
            expiredSessionCount.setCount(expiredSessionCountVal);
            return (CountStatistic) expiredSessionCount.unmodifiableView();

        }
    }

    /**
     * Returns the total number of bytes activated by this store including total, min, maximum
     */
    public AverageRangeStatistic getActivatedBeanSize() {
        synchronized (activationSizeLock) {
            return (AverageRangeStatistic) activationSize.unmodifiableView();
        }
    }

    /**
     * Returns the time spent on activating beans from the store including total, min, max
     */
    public AverageRangeStatistic getActivationTime() {
        synchronized (activationTimeLock) {
            return (AverageRangeStatistic) activationTime.unmodifiableView();
        }
    }

    /**
     * Returns the total number of bytes passivated by this store including total, min, maximum
     */
    public AverageRangeStatistic getPassivatedBeanSize() {
        synchronized (passivationSizeLock) {
            return (AverageRangeStatistic) passivationSize.unmodifiableView();
        }
    }

    /**
     * Returns the time spent on passivating beans to the store including total, min, max
     */
    public AverageRangeStatistic getPassivationTime() {
        synchronized (passivationTimeLock) {
            return (AverageRangeStatistic) passivationTime.unmodifiableView();
        }
    }

    //The following methods are called from StatefulSessionStoreMonitor
    //
    void incrementActivationCount(boolean success) {
        synchronized (activationCountLock) {
            activationCountVal++;
            if (success) {
                activationSuccessCountVal++;
            } else {
                activationErrorCountVal++;
            }
        }
    }

    void incrementPassivationCount(boolean success) {
        synchronized (passivationCountLock) {
            passivationCountVal++;
            if (success) {
                passivationSuccessCountVal++;
            } else {
                passivationErrorCountVal++;
            }
        }
    }

    void setActivationSize(long val) {
        synchronized (activationSizeLock) {
            activationSize.setCount(val);
        }
    }

    void setActivationTime(long val) {
        synchronized (activationTimeLock) {
            activationTime.setCount(val);
        }
    }

    void setPassivationSize(long val) {
        synchronized (passivationSizeLock) {
            passivationSize.setCount(val);
        }
    }

    void setPassivationTime(long val) {
        synchronized (passivationTimeLock) {
            passivationTime.setCount(val);
        }
    }

    void incrementExpiredSessionCountVal(long val) {
        synchronized (expiredSessionCountLock) {
            expiredSessionCountVal += val;
        }
    }

    protected void appendStats(StringBuffer sbuf) {
        sbuf.append("currentSize=").append(provider.getCurrentStoreSize())
        .append("; ")
        .append("ActivationCount=").append(activationCountVal)
        .append("; ")
        .append("ActivationSuccessCount=").append(activationSuccessCountVal)
        .append("; ")
        .append("ActivationErrorCount=").append(activationErrorCountVal)
        .append("; ")
        .append("PassivationCount=").append(passivationCountVal)
        .append("; ")
        .append("PassivationSuccessCount=").append(passivationSuccessCountVal)
        .append("; ")
        .append("PassivationErrorCount=").append(passivationErrorCountVal)
        .append("; ")
        .append("ExpiredSessionsRemoved=").append(expiredSessionCountVal)
        .append("; ");

        appendTimeStatistic(sbuf, "ActivationSize", activationSize);
        appendTimeStatistic(sbuf, "ActivationTime", activationTime);
        appendTimeStatistic(sbuf, "PassivationSize", passivationSize);
        appendTimeStatistic(sbuf, "PassivationTime", passivationTime);

    }

    protected static void appendTimeStatistic(StringBuffer sbuf, String name,
        MutableAverageRangeStatisticImpl stat)
    {
        sbuf.append(name).append("(")
        .append("min=").append(stat.getLowWaterMark()).append(", ")
        .append("max=").append(stat.getHighWaterMark()).append(", ")
        .append("avg=").append(stat.getAverage())
        .append("); ");
    }


    //Some of the store attributes are (unfortunately) exposed through
    //    the LruSessionCache too
    //Called from LruSessionCache -> StatefulSessionStoreMonitor
    int getNumExpiredSessionCount() {
        synchronized (expiredSessionCountLock) {
            return (int) expiredSessionCountVal;
        }
    }

    int getNumPassivationCount() {
        synchronized (passivationCountLock) {
            return (int) passivationCountVal;
        }
    }

    int getNumPassivationSuccessCount() {
        synchronized (passivationCountLock) {
            return (int) passivationSuccessCountVal;
        }
    }

    int getNumPassivationErrorCount() {
        synchronized (passivationCountLock) {
            return (int) passivationErrorCountVal;
        }
    }
}
