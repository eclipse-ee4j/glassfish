/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.admin.cli.CLICommand;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * A local Monitor Command (this will call the remote 'monitor' command). The reason for having to implement this as
 * local is to interpret the options --interval and --filename(TBD) options.
 *
 * @author Prashanth
 * @author Bill Shannon
 */
@Service(name = "monitor")
@PerLookup
public class MonitorCommand extends CLICommand {
    @Param(optional = true, defaultValue = "30")
    private int interval = 30; // default 30 seconds
    @Param
    private String type;
    @Param(optional = true)
    private String filter;
    @Param(optional = true)
    private File fileName;
    @Param(primary = true, optional = true)
    private String target; // XXX - not currently used
    private static final LocalStringsImpl strings = new LocalStringsImpl(MonitorCommand.class);

    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {
        // Based on interval, loop the subject to print the output
        Timer timer = new Timer();
        try {
            MonitorTask monitorTask = new MonitorTask(timer, getRemoteArgs(), programOpts, env, type, filter, fileName);
            timer.scheduleAtFixedRate(monitorTask, 0, (long) interval * 1000);
            boolean done = false;
            final BufferedReader in = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));

            while (!done) {
                String str = "";

                if (monitorTask.allOK == null)
                    str = ""; // not ready yet
                else if (monitorTask.allOK == false)
                    str = "Q";
                else if (System.in.available() > 0)
                    str = in.readLine();

                if (str == null || str.equals("q") || str.equals("Q")) {
                    timer.cancel();
                    done = true;
                    String exceptionMessage = monitorTask.getExceptionMessage();
                    if (exceptionMessage != null) {
                        throw new CommandException(exceptionMessage);
                    }
                } else if (str.equals("h") || str.equals("H")) {
                    monitorTask.displayDetails();
                }
            }
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                // ignore
            }
        } catch (Exception e) {
            timer.cancel();
            throw new CommandException(strings.get("monitorCommand.errorRemote", e.getMessage()));
        }
        return 0;
    }

    private String[] getRemoteArgs() {
        List<String> list = new ArrayList<String>(5);
        list.add("monitor");

        if (ok(type)) {
            list.add("--type");
            list.add(type);
        }
        if (ok(filter)) {
            list.add("--filter");
            list.add(filter);
        }
        return list.toArray(new String[list.size()]);
    }
}
