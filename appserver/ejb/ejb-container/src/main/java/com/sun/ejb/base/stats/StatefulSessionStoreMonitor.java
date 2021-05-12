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

/**
 * An instance of this class is used by the StatefulContainer to update monitoring
 *  data. There is once instance of this class per StatefulEJBContainer
 *
 * @author Mahesh Kannan
 */
public class StatefulSessionStoreMonitor {

    private StatefulSessionStoreStatsImpl statsImpl;

    void setDelegate(StatefulSessionStoreStatsImpl delegate) {
        this.statsImpl = delegate;
    }

    final void appendStats(StringBuffer sbuf) {
        if (statsImpl != null) {
            statsImpl.appendStats(sbuf);
        }
    }

    //The following methods are called from StatefulSessionContainer
    //
    public final boolean isMonitoringOn() {
        return (statsImpl != null);
    }

    public final void incrementActivationCount(boolean success) {
        StatefulSessionStoreStatsImpl delegate = statsImpl;
        if (delegate != null) {
            delegate.incrementActivationCount(success);
        }
    }

    public final void incrementPassivationCount(boolean success) {
        StatefulSessionStoreStatsImpl delegate = statsImpl;
        if (delegate != null) {
            delegate.incrementPassivationCount(success);
        }
    }

    public final void setActivationSize(long val) {
        StatefulSessionStoreStatsImpl delegate = statsImpl;
        if (delegate != null) {
            delegate.setActivationSize(val);
        }
    }

    public final void setActivationTime(long val) {
        StatefulSessionStoreStatsImpl delegate = statsImpl;
        if (delegate != null) {
            delegate.setActivationTime(val);
        }
    }

    public final void setPassivationSize(long val) {
        StatefulSessionStoreStatsImpl delegate = statsImpl;
        if (delegate != null) {
            delegate.setPassivationSize(val);
        }
    }

    public final void setPassivationTime(long val) {
        StatefulSessionStoreStatsImpl delegate = statsImpl;
        if (delegate != null) {
            delegate.setPassivationTime(val);
        }
    }

    public final void incrementExpiredSessionsRemoved(long val) {
        StatefulSessionStoreStatsImpl delegate = statsImpl;
        if (delegate != null) {
            delegate.incrementExpiredSessionCountVal(val);
        }
    }

    public void incrementCheckpointCount(boolean success) {
        throw new RuntimeException("Checkpoint operation not allowed on non-HA store");
    }

    public void setCheckpointSize(long val) {
        throw new RuntimeException("Checkpoint operation not allowed on non-HA store");
    }

    public void setCheckpointTime(long val) {
        throw new RuntimeException("Checkpoint operation not allowed on non-HA store");
    }

    //The following methods are maintained for backward compatibility
    //Called from LruSessionCache
    public int getNumExpiredSessionsRemoved() {
        StatefulSessionStoreStatsImpl delegate = statsImpl;
        return (delegate != null)
            ? delegate.getNumExpiredSessionCount()
                : 0;
    }

    public int getNumPassivationErrors() {
        StatefulSessionStoreStatsImpl delegate = statsImpl;
        return (delegate != null)
            ? delegate.getNumPassivationErrorCount()
                : 0;
    }

    public int getNumPassivations() {
        StatefulSessionStoreStatsImpl delegate = statsImpl;
        return (delegate != null)
            ? delegate.getNumPassivationCount()
                : 0;
    }

    public int getNumPassivationSuccess() {
        StatefulSessionStoreStatsImpl delegate = statsImpl;
        return (delegate != null)
            ? delegate.getNumPassivationSuccessCount()
                : 0;
    }

}
