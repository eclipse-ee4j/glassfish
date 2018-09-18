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

import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.TimeStatistic;

import com.sun.ejb.spi.stats.MonitorableSFSBStoreManager;

import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableTimeStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.TimeStatisticImpl;

/**
 * An instance of this class is used by the StatefulContainer to update monitoring 
 *  data. There is once instance of this class per StatefulEJBContainer
 *
 * @author Mahesh Kannan
 */

public class HAStatefulSessionStoreMonitor
    extends StatefulSessionStoreMonitor
{

    private HAStatefulSessionStoreStatsImpl haStatsImpl;

    protected void setDelegate(HAStatefulSessionStoreStatsImpl delegate) {
	this.haStatsImpl = delegate;
	super.setDelegate(delegate);
    }

    public final void incrementCheckpointCount(boolean success) {
	HAStatefulSessionStoreStatsImpl delegate = haStatsImpl;
	if (delegate != null) {
	    delegate.incrementCheckpointCount(success);
	}
    }

    public final void setCheckpointSize(long val) {
	HAStatefulSessionStoreStatsImpl delegate = haStatsImpl;
	if (delegate != null) {
	    delegate.setCheckpointSize(val);
	}
    }

    public final void setCheckpointTime(long val) {
	HAStatefulSessionStoreStatsImpl delegate = haStatsImpl;
	if (delegate != null) {
	    delegate.setCheckpointTime(val);
	}
    }

}
