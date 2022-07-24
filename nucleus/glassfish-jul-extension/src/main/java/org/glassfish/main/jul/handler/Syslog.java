/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul.handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.logging.Level;

import org.glassfish.main.jul.tracing.GlassFishLoggingTracer;

/**
 * Send a message via syslog.
 *
 * @author cmott
 * @author David Matejcek
 */
// This code is taken from spy.jar and enhanced
class Syslog {

    private final InetAddress addr;
    private final int port;
    private final Charset encoding;

    /**
     * Log to a particular log host.
     *
     * @param host
     * @param port
     */
    Syslog(final String host, final int port, final Charset encoding) {
        try {
            addr = InetAddress.getByName(host);
        } catch (final UnknownHostException e) {
            throw new IllegalStateException("Initialization of Syslog failed.", e);
        }
        this.port = port;
        this.encoding = encoding;
    }


    /**
     * Send a log message.
     *
     * @param level
     * @param msg
     */
    void log(final SyslogLevel level, final String msg) {
        try (DatagramSocket datagramSocket = new DatagramSocket()) {
            final byte[] buf = msg.getBytes(encoding);
            final DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, port);
            datagramSocket.send(packet);
        } catch (final IOException e) {
            GlassFishLoggingTracer.error(getClass(), "Failed to send the log message: " + msg, e);
        }
    }


    enum SyslogLevel {

        err(3),
        warning(4),
        info(6),
        debug(7),
        ;

        private int code;

        SyslogLevel(final int code) {
            this.code = code;
        }


        public int code() {
            return this.code;
        }

        /**
         * @param level
         * @return never null
         */
        public static SyslogLevel of(final Level level) {
            if (level.intValue() <= Level.SEVERE.intValue()) {
                return err;
            } else if (level.intValue() <= Level.WARNING.intValue()) {
                return warning;
            } else if (level.intValue() <= Level.INFO.intValue()) {
                return info;
            } else {
                return debug;
            }
        }
    }
}
