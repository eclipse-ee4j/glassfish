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

package com.sun.enterprise.server.logging;

import org.glassfish.config.support.TranslatedConfigView;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.ContractsProvided;
import jakarta.inject.Inject;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;

import com.sun.common.util.logging.BooleanLatch;

import jakarta.inject.Singleton;

import java.text.SimpleDateFormat;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.*;

/**
 * Created by IntelliJ IDEA.
 * User: cmott
 * Date: Mar 11, 2009
 * Time: 1:41:30 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
@Singleton
@ContractsProvided({SyslogHandler.class, java.util.logging.Handler.class})
public class SyslogHandler extends Handler implements PostConstruct, PreDestroy {

    @Inject
    ServerEnvironmentImpl env;

    private Syslog sysLogger;
    private Thread pump= null;
    private BooleanLatch done = new BooleanLatch();
    private BlockingQueue<LogRecord> pendingRecords = new ArrayBlockingQueue<LogRecord>(5000);
    private SimpleFormatter simpleFormatter = new SimpleFormatter();


    public void postConstruct() {

        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();

        Object obj = TranslatedConfigView.getTranslatedValue(manager.getProperty(cname + ".useSystemLogging"));
        // Added below 2 lines of code to avoid NPE as per the bug http://java.net/jira/browse/GLASSFISH-16162
        if(obj==null)
            return;
        String systemLogging = obj.toString();
        if (systemLogging.equals("false"))
            return;

        //set up the connection
        try {
            sysLogger = new Syslog("localhost");  //for now only write to this host
        } catch ( java.net.UnknownHostException e) {
                        LogFacade.LOGGING_LOGGER.log(Level.SEVERE, LogFacade.ERROR_INIT_SYSLOG, e);
                        return;
                    }

        // start the Queue consummer thread.
        pump = new Thread() {
            public void run() {
                try {
                    while (!done.isSignalled()) {
                        log();
                    }
                } catch (RuntimeException e) {

                }
            }
        };
        pump.start();

    }

    public void preDestroy() {
        if (LogFacade.LOGGING_LOGGER.isLoggable(Level.FINE)) {
            LogFacade.LOGGING_LOGGER.fine("SysLog Logger handler killed");
        }
    }

    /**
     * Retrieves the LogRecord from our Queue and store them in the file
     *
     */
    public void log() {

        LogRecord record;

        try {
            record = pendingRecords.take();
        } catch (InterruptedException e) {
            return;
        }
        Level level= record.getLevel();
        long millisec = record.getMillis();
        int syslogLevel = Syslog.INFO;
        String logLevel = "INFO";

        if (level.equals(Level.SEVERE)) {
            syslogLevel = Syslog.CRIT;
            logLevel = "CRIT";
        } else if (level.equals(Level.WARNING)){
            syslogLevel = Syslog.WARNING;
            logLevel = "WARNING";
        } else if(level.intValue() <= Level.FINE.intValue())   {
            syslogLevel = Syslog.DEBUG;
            logLevel = "DEBUG";
        }

        //format the message
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd HH:mm:ss");
        sb.append(formatter.format(millisec));
        sb.append(" [ ");
        sb.append(logLevel);
        sb.append(" glassfish ] ");
        String formattedMsg = simpleFormatter.formatMessage(record);
        sb.append(formattedMsg);
         //send message
        if (sysLogger != null) {
            sysLogger.log(Syslog.DAEMON, syslogLevel, sb.toString());
        }

    }

    /**
     * Publishes the logrecord storing it in our queue
     */
    public void publish( LogRecord record ) {
        if (pump == null)
            return;

        try {
            pendingRecords.add(record);
        } catch(IllegalStateException e) {
            // queue is full, start waiting.
            try {
                pendingRecords.put(record);
            } catch (InterruptedException e1) {
                // to bad, record is lost...
            }
        }
    }

    public void close() {

    }

    public void flush() {

    }
}

