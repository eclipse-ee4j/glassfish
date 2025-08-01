/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.glassfish.main.jul.handler.Syslog.SyslogLevel;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.glassfish.main.jul.record.MessageResolver;

import static org.glassfish.main.jul.handler.SyslogHandlerProperty.BUFFER_CAPACITY;
import static org.glassfish.main.jul.handler.SyslogHandlerProperty.BUFFER_TIMEOUT;
import static org.glassfish.main.jul.handler.SyslogHandlerProperty.ENABLED;
import static org.glassfish.main.jul.handler.SyslogHandlerProperty.ENCODING;
import static org.glassfish.main.jul.handler.SyslogHandlerProperty.HOST;
import static org.glassfish.main.jul.handler.SyslogHandlerProperty.LEVEL;
import static org.glassfish.main.jul.handler.SyslogHandlerProperty.PORT;

/**
 * @author cmott 2009
 * @author David Matejcek 2022
 */
public class SyslogHandler extends Handler {
    private static final MessageResolver MSG_RESOLVER = new MessageResolver();

    private final LogRecordBuffer pendingRecords;

    private final Syslog syslog;
    private LoggingPump pump;

    public SyslogHandler() {
        final HandlerConfigurationHelper helper = HandlerConfigurationHelper.forHandlerClass(getClass());
        if (!helper.getBoolean(ENABLED, true)) {
            syslog = null;
            pump = null;
            pendingRecords = null;
            return;
        }
        setLevel(helper.getLevel(LEVEL, Level.WARNING));
        setFormatter(helper.getFormatter(SimpleFormatter.class));
        setFilter(helper.getFilter());

        final int bufferCapacity = helper.getInteger(BUFFER_CAPACITY, 5000);
        final int bufferTimeout = helper.getInteger(BUFFER_TIMEOUT, 300);
        pendingRecords = new LogRecordBuffer(bufferCapacity, bufferTimeout);

        final String host = helper.getString(HOST, getLocalHost());
        final int port = helper.getInteger(PORT, 514);
        final Charset encoding = helper.getCharset(ENCODING, StandardCharsets.UTF_8);
        syslog = new Syslog(host, port, encoding);

        pump = new LoggingPump("SyslogHandler log pump", this.pendingRecords);
        pump.start();
    }


    @Override
    public void publish(final LogRecord record) {
        if (pump == null || record == null || record.getLevel().intValue() < getLevel().intValue()) {
            return;
        }
        pendingRecords.add(MSG_RESOLVER.resolve(record));
    }


    @Override
    public void flush() {
        // nothing to do
    }


    @Override
    public synchronized void close() {
        if (pump != null && pump.isAlive()) {
            pump.interrupt();
            pump = null;
        }
    }


    private void log(final GlassFishLogRecord record) {
        if (syslog == null) {
            return;
        }
        final SyslogLevel syslogLevel = SyslogLevel.of(record.getLevel());
        final StringBuilder sb = new StringBuilder();
        sb.append("GlassFish");
        sb.append('[').append(ProcessHandle.current().pid()).append(']');
        sb.append(": <").append(syslogLevel.name()).append('>');
        sb.append(' ').append(getFormatter().formatMessage(record));
        syslog.log(syslogLevel, sb.toString());
    }


    private static String getLocalHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            throw new IllegalStateException("Could not initialize the SyslogHandler's connection.", e);
        }
    }

    private final class LoggingPump extends LoggingPumpThread {

        private LoggingPump(final String threadName, final LogRecordBuffer buffer) {
            super(threadName, buffer);
        }


        @Override
        protected int getFlushFrequency() {
            return 1;
        }


        @Override
        protected boolean logRecord(final GlassFishLogRecord record) {
            if (record == null) {
                return false;
            }
            log(record);
            return true;
        }


        @Override
        protected void flushOutput() {
            flush();
        }
    }
}

