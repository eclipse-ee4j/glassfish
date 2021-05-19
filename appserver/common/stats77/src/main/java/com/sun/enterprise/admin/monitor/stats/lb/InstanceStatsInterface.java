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

package com.sun.enterprise.admin.monitor.stats.lb;

/**
 * This interface has all of the bean info accessor methods.
 */
public interface InstanceStatsInterface {
    java.lang.String getNumTotalRequests();

    java.lang.String getId();

    void setNumActiveRequests(java.lang.String value);

    void setNumTotalRequests(java.lang.String value);

    void setApplicationStatsNumErrorRequests(java.lang.String value);

    int removeApplicationStats(boolean value);

    void setApplicationStatsNumFailoverRequests(java.lang.String value);

    java.lang.String getNumActiveRequests();

    boolean[] getApplicationStats();

    void setApplicationStats(int index, boolean value);

    void setApplicationStatsNumActiveRequests(java.lang.String value);

    void setHealth(java.lang.String value);

    void setId(java.lang.String value);

    int sizeApplicationStats();

    java.lang.String getApplicationStatsId();

    void setApplicationStatsNumIdempotentUrlRequests(java.lang.String value);

    void setApplicationStatsNumTotalRequests(java.lang.String value);

    java.lang.String getApplicationStatsNumErrorRequests();

    java.lang.String getApplicationStatsMinResponseTime();

    int addApplicationStats(boolean value);

    void setApplicationStatsId(java.lang.String value);

    void setApplicationStatsAverageResponseTime(java.lang.String value);

    void setApplicationStatsMinResponseTime(java.lang.String value);

    void setApplicationStats(boolean[] value);

    java.util.List fetchApplicationStatsList();

    java.lang.String getApplicationStatsAverageResponseTime();

    java.lang.String getApplicationStatsNumIdempotentUrlRequests();

    java.lang.String getApplicationStatsMaxResponseTime();

    void setApplicationStatsMaxResponseTime(java.lang.String value);

    java.lang.String getApplicationStatsNumActiveRequests();

    java.lang.String getHealth();

    boolean isApplicationStats(int index);

    java.lang.String getApplicationStatsNumTotalRequests();

    java.lang.String getApplicationStatsNumFailoverRequests();

}
