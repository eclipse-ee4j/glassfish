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
 * data. There is once instance of this class per StatefulEJBContainer
 *
 * @author Mahesh Kannan
 */
public class HAStatefulSessionStoreMonitor extends StatefulSessionStoreMonitor {

    private HAStatefulSessionStoreStatsImpl haStatsImpl;

    protected void setDelegate(HAStatefulSessionStoreStatsImpl delegate) {
        this.haStatsImpl = delegate;
        super.setDelegate(delegate);
    }


    @Override
    public final void incrementCheckpointCount(boolean success) {
        HAStatefulSessionStoreStatsImpl delegate = haStatsImpl;
        if (delegate != null) {
            delegate.incrementCheckpointCount(success);
        }
    }


    @Override
    public final void setCheckpointSize(long val) {
        HAStatefulSessionStoreStatsImpl delegate = haStatsImpl;
        if (delegate != null) {
            delegate.setCheckpointSize(val);
        }
    }


    @Override
    public final void setCheckpointTime(long val) {
        HAStatefulSessionStoreStatsImpl delegate = haStatsImpl;
        if (delegate != null) {
            delegate.setCheckpointTime(val);
        }
    }
}
