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

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.ConnectionProbe;
import org.glassfish.grizzly.IOEvent;
import org.glassfish.grizzly.Transport;

/**
 *
 * @author oleksiys
 */
public class ConnectionMonitor implements ConnectionProbe {
    private final GrizzlyMonitoring grizzlyMonitoring;
    private final String monitoringId;

    public ConnectionMonitor(GrizzlyMonitoring grizzlyMonitoring,
            String monitoringId, Transport transport) {
        this.grizzlyMonitoring = grizzlyMonitoring;
        this.monitoringId = monitoringId;
    }

    @Override
    public void onAcceptEvent(final Connection serverConnection,
            final Connection clientConnection) {
        final Object peerAddress = clientConnection.getPeerAddress();

        grizzlyMonitoring.getConnectionQueueProbeProvider().connectionAcceptedEvent(
                monitoringId, clientConnection.hashCode(),
                peerAddress.toString());
    }

    @Override
    public void onConnectEvent(final Connection connection) {
        grizzlyMonitoring.getConnectionQueueProbeProvider().connectionConnectedEvent(
                monitoringId, connection.hashCode(),
                connection.getPeerAddress().toString());
    }

    @Override
    public void onCloseEvent(Connection connection) {
        grizzlyMonitoring.getConnectionQueueProbeProvider().connectionClosedEvent(
                monitoringId, connection.hashCode());
    }

    @Override
    public void onBindEvent(Connection connection) {
    }

    @Override
    public void onReadEvent(Connection connection, Buffer data, int size) {
    }

    @Override
    public void onWriteEvent(Connection connection, Buffer data, long size) {
    }

    @Override
    public void onErrorEvent(Connection connection, Throwable error) {
    }

    @Override
    public void onIOEventReadyEvent(Connection connection, IOEvent ioEvent) {
    }

    @Override
    public void onIOEventEnableEvent(Connection connection, IOEvent ioEvent) {
    }

    @Override
    public void onIOEventDisableEvent(Connection connection, IOEvent ioEvent) {
    }

}
