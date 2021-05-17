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

//
// This interface has all of the bean info accessor methods.
//

public interface InstanceStatsInterface {
    public java.lang.String getNumTotalRequests();

    public java.lang.String getId();

    public void setNumActiveRequests(java.lang.String value);

    public void setNumTotalRequests(java.lang.String value);

    public void setApplicationStatsNumErrorRequests(java.lang.String value);

    public int removeApplicationStats(boolean value);

    public void setApplicationStatsNumFailoverRequests(java.lang.String value);

    public java.lang.String getNumActiveRequests();

    public boolean[] getApplicationStats();

    public void setApplicationStats(int index, boolean value);

    public void setApplicationStatsNumActiveRequests(java.lang.String value);

    public void setHealth(java.lang.String value);

    public void setId(java.lang.String value);

    public int sizeApplicationStats();

    public java.lang.String getApplicationStatsId();

    public void setApplicationStatsNumIdempotentUrlRequests(java.lang.String value);

    public void setApplicationStatsNumTotalRequests(java.lang.String value);

    public java.lang.String getApplicationStatsNumErrorRequests();

    public java.lang.String getApplicationStatsMinResponseTime();

    public int addApplicationStats(boolean value);

    public void setApplicationStatsId(java.lang.String value);

    public void setApplicationStatsAverageResponseTime(java.lang.String value);

    public void setApplicationStatsMinResponseTime(java.lang.String value);

    public void setApplicationStats(boolean[] value);

    public java.util.List fetchApplicationStatsList();

    public java.lang.String getApplicationStatsAverageResponseTime();

    public java.lang.String getApplicationStatsNumIdempotentUrlRequests();

    public java.lang.String getApplicationStatsMaxResponseTime();

    public void setApplicationStatsMaxResponseTime(java.lang.String value);

    public java.lang.String getApplicationStatsNumActiveRequests();

    public java.lang.String getHealth();

    public boolean isApplicationStats(int index);

    public java.lang.String getApplicationStatsNumTotalRequests();

    public java.lang.String getApplicationStatsNumFailoverRequests();

}
