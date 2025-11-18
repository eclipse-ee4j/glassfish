/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.servermgmt.cli;

import com.sun.enterprise.admin.cli.Environment;
import com.sun.enterprise.admin.cli.ProgramOptions;
import com.sun.enterprise.admin.cli.remote.RemoteCLICommand;

import java.io.File;
import java.lang.System.Logger;
import java.util.Timer;
import java.util.TimerTask;

import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;

import static java.lang.System.Logger.Level.INFO;

public class MonitorTask extends TimerTask {
    private static final Logger LOG = System.getLogger(MonitorTask.class.getName());
    private static final LocalStringsImpl strings = new LocalStringsImpl(MonitorTask.class);

    private String type = null;
    private String filter = null;
    private Timer timer = null;
    private String[] remoteArgs;
    private String exceptionMessage = null;
    private RemoteCLICommand cmd;
    private static final int NUM_ROWS = 25;
    private int counter = 0;
    volatile Boolean allOK = null;

    public MonitorTask(final Timer timer, final String[] remoteArgs, ProgramOptions programOpts, Environment env, final String type,
            final String filter, final File fileName) throws CommandException, CommandValidationException {
        this.timer = timer;
        if ((type != null) && (type.length() > 0)) {
            this.type = type;
        }
        if ((filter != null) && (filter.length() > 0)) {
            this.filter = filter;
        }
        this.remoteArgs = remoteArgs;
        cmd = new RemoteCLICommand(remoteArgs[0], programOpts, env);
        displayHeader(type);

    }

    void displayHeader(String type) {
        // print title
        String title = "";
        if ("servlet".equals(type)) {
            title = String.format("%1$-10s %2$-10s %3$-10s", "aslc", "mslc", "tslc");
        } else if ("httplistener".equals(type)) {
            title = String.format("%1$-4s %2$-4s %3$-6s %4$-4s", "ec", "mt", "pt", "rc");
        } else if ("jvm".equals(type)) {
            title = String.format("%1$45s", "JVM Monitoring");
            LOG.log(INFO, title);
            // row title
            title = null;
            if (filter != null) {
                if (("heapmemory".equals(filter)) || ("nonheapmemory".equals(filter))) {
                    title = String.format("%1$-10s %2$-10s %3$-10s %4$-10s", "init", "used", "committed", "max");
                }
            }
            if (title == null) {
                // default jvm stats
                title = String.format("%1$-35s %2$-40s", "UpTime(ms)", "Heap and NonHeap Memory(bytes)");
                LOG.log(INFO, title);
                title = String.format("%1$-25s %2$-10s %3$-10s %4$-10s %5$-10s %6$-10s", strings.get("monitor.jvm.current"),
                        strings.get("monitor.jvm.min"), strings.get("monitor.jvm.max"), strings.get("monitor.jvm.low"),
                        strings.get("monitor.jvm.high"), strings.get("monitor.jvm.count"));
            }
        } else if ("webmodule".equals(type)) {
            title = String.format("%1$-5s %2$-5s %3$-5s %4$-5s %5$-5s %6$-5s %7$-5s %8$-8s %9$-10s %10$-5s", "asc", "ast", "rst", "st",
                    "ajlc", "mjlc", "tjlc", "aslc", "mslc", "tslc");
        }
        LOG.log(INFO, title);
    }

    void cancelMonitorTask() {
        timer.cancel();
    }

    @Override
    public void run() {
        try {
            cmd.execute(remoteArgs);
            allOK = true;
            if (counter == NUM_ROWS) {
                displayHeader(type);
                counter = 0;
            }
            counter++;
        } catch (Exception e) {
            //LOG.severe(
            //strings.get("monitorCommand.errorRemote", e.getMessage()));
            allOK = false;
            cancelMonitorTask();
            exceptionMessage = e.getMessage();
        }
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void displayDetails() {
        String details = "";
        if ("servlet".equals(type)) {
            details = strings.get("commands.monitor.servlet_detail");
        } else if ("httplistener".equals(type)) {
            details = strings.get("commands.monitor.httplistener_detail");
        } else if ("jvm".equals(type)) {
            //no details
        } else if ("webmodule".equals(type)) {
            details = strings.get("commands.monitor.webmodule_virtual_server_detail");
        }
        LOG.log(INFO, details);
    }
}
