/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.webservices.transport.tcp;

import com.sun.xml.ws.transport.tcp.server.IncomeMessageProcessor;
import com.sun.xml.ws.transport.tcp.server.TCPMessageListener;
import com.sun.xml.ws.transport.tcp.server.WSTCPConnector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Properties;

/**
 *
 * @author oleksiys
 */
public class Connector implements WSTCPConnector {
    private final String host;
    private final int port;
    private final TCPMessageListener listener;

    private final Properties properties;

    private final IncomeMessageProcessor processor;

    public Connector(String host, int port, TCPMessageListener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;

        properties = new Properties();

        processor = IncomeMessageProcessor.registerListener(port, listener, properties);
    }

    public void listen() throws IOException {
    }

    public void process(ByteBuffer buffer, SocketChannel channel) throws IOException {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader oldClassLoader = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(processor.getClass().getClassLoader());
            processor.process(buffer, channel);
        } finally {
            currentThread.setContextClassLoader(oldClassLoader);
        }
    }

    public void notifyConnectionClosed(SocketChannel channel) {
        processor.notifyClosed(channel);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
    }

    public TCPMessageListener getListener() {
        return listener;
    }

    public void setListener(TCPMessageListener arg0) {
    }

    public void setFrameSize(int frameSize) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getFrameSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() {
        IncomeMessageProcessor.releaseListener(port);
    }
}
