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

package org.glassfish.webservices.transport.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.nio.NIOConnection;

/**
 *
 * @author Alexey Stashok
 */
public class WSTCPProtocolFilter extends BaseFilter {

    private static final Logger LOGGER = LogUtils.getLogger();

    private volatile Connector connector;

    private final Object sync = new Object();

    private static final V3Module module = new V3Module();

    @Override
    public NextAction handleRead(final FilterChainContext ctx) throws IOException {
        final NIOConnection connection = (NIOConnection) ctx.getConnection();

        if (connector == null) {
            synchronized (sync) {
                if (connector == null) {

                    final InetSocketAddress socketAddress = (InetSocketAddress) connection.getPeerAddress();
                    final String host = socketAddress.getHostName();
                    final int port = socketAddress.getPort();

                    LOGGER.log(Level.INFO, LogUtils.SOAPTCP_PROTOCOL_INITIALIZED, port);

                    connector = new Connector(host, port, module.getDelegate());
                }
            }
        }

        final Buffer buffer = ctx.getMessage();
        final ByteBuffer byteBuffer = buffer.toByteBuffer();

        final SocketChannel channel = (SocketChannel) connection.getChannel();
        connector.process(byteBuffer, channel);

        return ctx.getStopAction();
    }

    @Override
    public NextAction handleClose(final FilterChainContext ctx) throws IOException {
        final Connection connection = ctx.getConnection();
        final SelectionKey selectionKey = ((NIOConnection) connection).getSelectionKey();

        try {
            if (connector != null) {
                connector.notifyConnectionClosed((SocketChannel) selectionKey.channel());
            } else {
                synchronized (sync) {
                    if (connector != null) {
                        connector.notifyConnectionClosed((SocketChannel) selectionKey.channel());
                    }
                }
            }
        } catch (Exception e) {
        }

        return ctx.getInvokeAction();
    }
}
