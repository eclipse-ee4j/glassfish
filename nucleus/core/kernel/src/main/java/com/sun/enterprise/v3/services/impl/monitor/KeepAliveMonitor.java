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

package com.sun.enterprise.v3.services.impl.monitor;

import com.sun.enterprise.v3.services.impl.monitor.stats.KeepAliveStatsProvider;

import java.io.IOException;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.http.KeepAlive;
import org.glassfish.grizzly.http.KeepAliveProbe;

/**
 *
 * @author oleksiys
 */
public class KeepAliveMonitor implements KeepAliveProbe {
    private final GrizzlyMonitoring grizzlyMonitoring;
    private final String monitoringId;

    public KeepAliveMonitor(GrizzlyMonitoring grizzlyMonitoring,
            String monitoringId, KeepAlive config) {
        this.grizzlyMonitoring = grizzlyMonitoring;
        this.monitoringId = monitoringId;

        if (grizzlyMonitoring != null) {
            final KeepAliveStatsProvider statsProvider =
                    grizzlyMonitoring.getKeepAliveStatsProvider(monitoringId);
            statsProvider.setStatsObject(config);
            statsProvider.reset();
        }
    }

    @Override
    public void onConnectionAcceptEvent(final Connection connection) {
        grizzlyMonitoring.getKeepAliveProbeProvider().incrementCountConnectionsEvent(monitoringId);
        connection.addCloseListener(new Connection.CloseListener() {

            @Override
            public void onClosed(final Connection connection, Connection.CloseType closeType) throws IOException {
                grizzlyMonitoring.getKeepAliveProbeProvider().decrementCountConnectionsEvent(monitoringId);
            }

        });
    }

    @Override
    public void onHitEvent(Connection connection, int requestNumber) {
        grizzlyMonitoring.getKeepAliveProbeProvider().incrementCountHitsEvent(monitoringId);
    }

    @Override
    public void onRefuseEvent(Connection connection) {
        grizzlyMonitoring.getKeepAliveProbeProvider().incrementCountRefusalsEvent(monitoringId);
    }

    @Override
    public void onTimeoutEvent(Connection connection) {
        grizzlyMonitoring.getKeepAliveProbeProvider().incrementCountTimeoutsEvent(monitoringId);
    }

}
