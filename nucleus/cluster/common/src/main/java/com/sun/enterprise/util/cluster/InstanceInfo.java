/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util.cluster;

import com.sun.enterprise.admin.util.InstanceCommandExecutor;
import com.sun.enterprise.admin.util.InstanceStateService;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.universal.Duration;
import com.sun.enterprise.util.ColumnFormatter;
import com.sun.enterprise.util.StringUtils;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.InstanceCommandResult;
import org.glassfish.api.admin.InstanceState;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * Used to format instance state info in a standard way.
 * It also does internet work
 * @author byron Nevins
 */
public final class InstanceInfo {

    public InstanceInfo(ServiceLocator habitat, Server svr, int port0, String host0, String cluster0,
            Logger logger0, int timeout0, ActionReport report, InstanceStateService stateService) {
        if (svr == null )
//            if (svr == null || host0 == null)
            throw new NullPointerException("null arguments");

        this.habitat = habitat;
        this.svr = svr;
        name = svr.getName();
        port = port0;
        if (host0 == null)
            host="not yet assigned";
        else
            host = host0;
        logger = logger0;
        timeoutInMsec = timeout0;
        this.report = report;
        this.stateService = stateService;

        if (!StringUtils.ok(cluster0))
            cluster = null;
        else
            cluster = cluster0;
        future = pingInstance();
    }

    @Override
    public final String toString() {
        String cl = "";

        if (cluster != null)
            cl = ", cluster: " + getCluster();

        return "name: " + getName()
                + ", host: " + getHost()
                + ", port: " + getPort()
                + cl
                + ", uptime: " + uptime
                + ", pid: " + pid;

    }

    public final String getDisplayCluster() {
        return cluster == null ? NO_CLUSTER : cluster;
    }

    public final String getCluster() {
        return cluster;
    }

    public final String getHost() {
        return host;
    }

    public final int getPort() {
        return port;
    }

    public final String getName() {
        return name;
    }

    public final long getUptime() {
        if (uptime == -1) {
            getFutureResult();
        }
        return uptime;
    }

    public final int getPid() {
        if (pid < 0) {
            getFutureResult();
        }
        return pid;
    }

    public final String getDisplayState() {
        StringBuilder display = new StringBuilder();
        display.append(isRunning() ?
            InstanceState.StateType.RUNNING.getDisplayString() :
            InstanceState.StateType.NOT_RUNNING.getDisplayString());

        if (ssState == InstanceState.StateType.RESTART_REQUIRED) {
            if (isRunning()) {
                display.append("; ").append(InstanceState.StateType.RESTART_REQUIRED.getDisplayString());
            }
            List<String> failedCmds = stateService.getFailedCommands(name);
            if (!failedCmds.isEmpty()) {
                StringBuilder list = new StringBuilder();
                for (String z : failedCmds) list.append(z).append("; ");
                display.append(" [pending config changes are: ").append(list).append("]");
            }
        }
        return display.toString();
    }

    public final String getState() {
        if (state == null) {
            getFutureResult();
        }
        return state;
    }

    public final boolean isRunning() {
        if (state == null) {
            getFutureResult();
        }
        return running;
    }

    private void getFutureResult() {
        try {
            InstanceCommandResult r = future.get(timeoutInMsec, TimeUnit.MILLISECONDS);
            InstanceCommandExecutor res = (InstanceCommandExecutor) r.getInstanceCommand();
            String instanceLocation = res.getCommandOutput();
            // Remove the pesky \n out
            instanceLocation = (instanceLocation == null) ? "" : instanceLocation.trim();

            if ((!instanceLocation.endsWith(res.getServer().getName()))
                    || (res.getReport().getActionExitCode() != ActionReport.ExitCode.SUCCESS)) {
                uptime = -1;
                state = NOT_RUNNING;
                running = false;
                pid = -1;
                ssState = stateService.setState(name, InstanceState.StateType.NOT_RUNNING, false);
            }
            else {
                Properties props = res.getReport().getTopMessagePart().getProps();
                String uptimeStr = props.getProperty("Uptime");
                String pidstr = props.getProperty("Pid");
                String restartstr = props.getProperty("Restart-Required");

                try {
                    pid = Integer.parseInt(pidstr);
                }
                catch(Exception e) {
                    // no I don't want to use the enclosing catch...
                    pid = -1;
                }
                ssState = stateService.setState(name,
                        Boolean.parseBoolean(restartstr) ?
                            InstanceState.StateType.RESTART_REQUIRED :
                            InstanceState.StateType.RUNNING,
                        false);
                uptime = Long.parseLong(uptimeStr);
                state = formatTime(uptime);
                running = true;
            }
        }
        catch (Exception e) {
            uptime = -1;
            state = NOT_RUNNING;
            ssState = stateService.setState(name, InstanceState.StateType.NOT_RUNNING, false);
            running = false;
            pid = -1;
        }
    }
    /////////////////////////////////////////////////////////////////////////
    ////////  static formatting stuff below   ///////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    public static String format(List<InstanceInfo> infos) {
        String headings[] = {NAME, HOST, PORT, PID, CLUSTER, STATE};
        ColumnFormatter cf = new ColumnFormatter(headings);
        for (InstanceInfo info : infos) {
            cf.addRow(new Object[]{
                        info.getName(),
                        info.getHost(),
                        info.getPort(),
                        formatPid(info),
                        info.getDisplayCluster(),
                        info.getDisplayState()
                    });
        }
        return cf.toString();
    }

    public static String formatBrief(List<InstanceInfo> infos) {
        ColumnFormatter cf = new ColumnFormatter();
        for (InstanceInfo info : infos) {
            cf.addRow(new Object[]{
                        info.getName(),
                        info.getDisplayState()
                    });
        }
        return cf.toString();
    }

    private static String formatPid(InstanceInfo info) {
        int pid = info.getPid();

        return pid < 0 ? "--" : "" + pid;
    }

    // TODO what about security????
    private Future<InstanceCommandResult> pingInstance() {
        try {
            ActionReport aReport = report.addSubActionsReport();
            InstanceCommandResult aResult = new InstanceCommandResult();
            ParameterMap map = new ParameterMap();
            map.set("type", "terse");
            InstanceCommandExecutor ice =
                    new InstanceCommandExecutor(habitat, "__locations", FailurePolicy.Error, FailurePolicy.Error,
                            svr, host, port, logger, map, aReport, aResult);
            ice.setConnectTimeout(timeoutInMsec);
            return stateService.submitJob(svr, ice, aResult);
            /*
            String ret = rac.executeCommand(map).trim();

            if (ret == null || (!ret.endsWith("/" + name)))
            return -1;
            running = true;
            String uptimeStr = rac.getAttributes().get("Uptime_value");
            return Long.parseLong(uptimeStr);
             */
        }
        catch (CommandException ex) {
            running = false;
            return null;
        }
    }

    private String formatTime(long uptime) {
        return Strings.get("instanceinfo.uptime", new Duration(uptime));
    }
    private final ServiceLocator habitat;
    private final String host;
    private final int port;
    private final String name;
    private long uptime = -1;
    private String state = null;
    private InstanceState.StateType ssState;
    private final String cluster;
    private final Logger logger;
    private final int timeoutInMsec;
    private Future<InstanceCommandResult> future;
    private final ActionReport report;
    private final InstanceStateService stateService;
    private final Server svr;
    private int pid;
    private boolean running;
    private static final String NOT_RUNNING = Strings.get("ListInstances.NotRunning");
    private static final String NAME = Strings.get("ListInstances.name");
    private static final String HOST = Strings.get("ListInstances.host");
    private static final String PORT = Strings.get("ListInstances.port");
    private static final String PID = Strings.get("ListInstances.pid");
    private static final String STATE = Strings.get("ListInstances.state");
    private static final String CLUSTER = Strings.get("ListInstances.cluster");
    private static final String NO_CLUSTER = "---";
}
