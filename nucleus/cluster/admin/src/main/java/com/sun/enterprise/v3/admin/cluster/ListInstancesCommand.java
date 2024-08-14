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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.admin.util.InstanceStateService;
import com.sun.enterprise.admin.util.RemoteInstanceCommandHelper;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.cluster.InstanceInfo;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.InstanceState;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ReferenceContainer;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.v3.admin.cluster.Constants.NONE;

/**
 * AdminCommand to list all instances and their states
 *
 * This is so clumsy & hard to remember I leave it here as a comment:
 * @Inject(name = ServerEnvironment.DEFAULT_INSTANCE_NAME)
 * @author Byron Nevins
 */
@org.glassfish.api.admin.ExecuteOn(RuntimeType.DAS)
@Service(name = "list-instances")
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.instances.command")
@PerLookup
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.GET,
        path="list-instances",
        description="List Cluster Instances",
        params={
            @RestParam(name="id", value="$parent")
        }),
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.GET,
        path="list-instances",
        description="List Instances")
})
public class ListInstancesCommand implements AdminCommand {

    @Inject
    private ServiceLocator habitat;
    @Inject
    private Domain domain;
    @Inject
    private ServerEnvironment env;
    @Inject
    private Servers allServers;
    @Inject
    InstanceStateService stateService;
    @Param(optional = true, defaultValue = "false", name = "long", shortName = "l")
    private boolean long_opt;
    @Param(optional = true, defaultValue = "60000")
    private String timeoutmsec;
    @Param(optional = true, defaultValue = "false")
    private boolean standaloneonly;
    @Param(optional = true, defaultValue = "false")
    private boolean nostatus;

    // We are setting the whichTarget to an empty String because FindBugs - LL
    // does not understand that HK2 is going to set it to a Sting for us later.
    // This garbage empty String will be replaced by HK2 soon...
    @Param(optional = true, primary = true, defaultValue = "domain")
    String whichTarget = "";

    private List<InstanceInfo> infos = new LinkedList<InstanceInfo>();
    private List<Server> serverList;
    private ActionReport report;
    private ActionReport.MessagePart top = null;
    private static final String EOL = "\n";

    @Override
    public void execute(AdminCommandContext context) {
        // setup
        int timeoutInMsec;
        try {
            timeoutInMsec = Integer.parseInt(timeoutmsec);
        }
        catch (Exception e) {
            timeoutInMsec = 60000;
        }

        report = context.getActionReport();
        top = report.getTopMessagePart();

        Logger logger = context.getLogger();

        if (!validateParams()) {
            return;
        }

        serverList = createServerList();

        if (serverList == null) {
            fail(Strings.get("list.instances.badTarget", whichTarget ));
            return;
        }
        // Require that we be a DAS
        if (!env.isDas()) {
            String msg = Strings.get("list.instances.onlyRunsOnDas");
            logger.warning(msg);
            fail(msg);
            return;
        }

        if (nostatus) {
            noStatus(serverList);
        }
        else {
            yesStatus(serverList, timeoutInMsec, logger);
        }

        report.setActionExitCode(ExitCode.SUCCESS);
    }

    private void noStatus(List<Server> serverList) {
        if (serverList.size() < 1) {
            report.setMessage(NONE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        boolean firstServer = true;
        Properties extraProps = new Properties();
        List instanceList = new ArrayList();

        for (Server server : serverList) {
            boolean clustered = server.getCluster() != null;

            if (standaloneonly && clustered) {
                continue;
            }

            String name = server.getName();

            if (notDas(name)) {
                if (firstServer) {
                    firstServer = false;
                }
                else {
                    sb.append(EOL);
                }

                sb.append(name);
                top.addProperty(name, "");
                HashMap<String, Object> insDetails = new HashMap<String, Object>();
                insDetails.put("name", name);
                instanceList.add(insDetails);
            }
        }
        extraProps.put("instanceList", instanceList);
        report.setMessage(sb.toString());
        report.setExtraProperties(extraProps);
    }

    private boolean notDas(String name) {
        return !SystemPropertyConstants.DAS_SERVER_NAME.equals(name);
    }

    private void yesStatus(List<Server> serverList, int timeoutInMsec, Logger logger) {
        // Gather a list of InstanceInfo -- one per instance in domain.xml
        RemoteInstanceCommandHelper helper = new RemoteInstanceCommandHelper(habitat);

        for (Server server : serverList) {
            boolean clustered = server.getCluster() != null;
            int port = helper.getAdminPort(server);
            String host = server.getAdminHost();

            if (standaloneonly && clustered) {
                continue;
            }

            String name = server.getName();

            if (name == null) {
                continue;   // can this happen?!?
            }

            Cluster cluster = domain.getClusterForInstance(name);
            String clusterName = (cluster != null) ? cluster.getName() : null;
            // skip DAS
            if (notDas(name)) {
                ActionReport tReport = habitat.getService(ActionReport.class, "html");
                InstanceInfo ii = new InstanceInfo(
                        habitat,
                        server,
                        port,
                        host,
                        clusterName,
                        logger,
                        timeoutInMsec,
                        tReport,
                        stateService);
                infos.add(ii);
            }
        }
        if (infos.size() < 1) {
            report.setMessage(NONE);
            return;
        }

        Properties extraProps = new Properties();
        List instanceList = new ArrayList();

        for (InstanceInfo ii : infos) {
            String name = ii.getName();
            String value = (ii.isRunning()) ? InstanceState.StateType.RUNNING.getDescription()
                    : InstanceState.StateType.NOT_RUNNING.getDescription();
            InstanceState.StateType state = (ii.isRunning())
                    ? (stateService.setState(name, InstanceState.StateType.RUNNING, false))
                    : (stateService.setState(name, InstanceState.StateType.NOT_RUNNING, false));
            List<String> failedCmds = stateService.getFailedCommands(name);
            if (state == InstanceState.StateType.RESTART_REQUIRED) {
                if (ii.isRunning()) {
                    //value += (";" + InstanceState.StateType.RESTART_REQUIRED.getDescription());
                    value = InstanceState.StateType.RESTART_REQUIRED.getDescription();
                }
            }

            HashMap<String, Object> insDetails = new HashMap<String, Object>();
            insDetails.put("name", name);
            insDetails.put("status", value);
            if (state == InstanceState.StateType.RESTART_REQUIRED) {
                insDetails.put("restartReasons", failedCmds);
            }
            if (ii.isRunning()) {
                insDetails.put("uptime", ii.getUptime());
            }
            instanceList.add(insDetails);
        }
        extraProps.put("instanceList", instanceList);
        report.setExtraProperties(extraProps);

        if (long_opt) {
            report.setMessage(InstanceInfo.format(infos));
        }
        else {
            report.setMessage(InstanceInfo.formatBrief(infos));
        }
    }

    /*
     * return null means the whichTarget is garbage
     * return empty list means the whichTarget was an empty cluster
     */
    private List<Server> createServerList() {
        // 1. no whichTarget specified
        if (!StringUtils.ok(whichTarget))
            return allServers.getServer();

        ReferenceContainer rc = domain.getReferenceContainerNamed(whichTarget);
        // 2. Not a server or a cluster. Could be a config or a Node
        if (rc == null) {
            return getServersForNodeOrConfig();
        }
        else if (rc.isServer()) {
            List<Server> l = new LinkedList<Server>();
            l.add((Server) rc);
            return l;
        }
        else if (rc.isCluster()) { // can't be anything else currently! (June 2010)
            Cluster cluster = (Cluster) rc;
            return cluster.getInstances();
        }
        else {
            return null;
        }
    }

    private List<Server> getServersForNodeOrConfig() {
        if (whichTarget == null)
            throw new NullPointerException("impossible!");

        List<Server> list = getServersForNode();

        if (list == null) {
            list = getServersForConfig();
        }

        return list;
    }

    private List<Server> getServersForNode() {
        if (whichTarget == null) // FindBugs can't figure out that our caller already checked.
            throw new NullPointerException("impossible!");

        boolean foundNode = false;
        Nodes nodes = domain.getNodes();

        if (nodes != null) {
            List<Node> nodeList = nodes.getNode();
            if (nodeList != null) {
                for (Node node : nodeList) {
                    if (whichTarget.equals(node.getName())) {
                        foundNode = true;
                        break;
                    }
                }
            }
        }
        if (!foundNode) {
            return null;
        }
        else {
            return domain.getInstancesOnNode(whichTarget);
        }
    }

    private List<Server> getServersForConfig() {
        Config config = domain.getConfigNamed(whichTarget);

        if (config == null) {
            return null;
        }

        List<ReferenceContainer> rcs = domain.getReferenceContainersOf(config);
        List<Server> servers = new LinkedList<Server>();

        for (ReferenceContainer rc : rcs) {
            if (rc.isServer()) {
                servers.add((Server) rc);
            }
        }

        return servers;
    }

    /*
     * false means error
     */
    private boolean validateParams() {
        // another sort of weird scenario is that if the whichTarget is set to "domain",
        // that means ALL instances in the domains.  To make life easier -- we just
        //set the whichTarget to zilch to signal all instances in domain

        if ("domain".equals(whichTarget)) {
            whichTarget = null;
        }

        // standaloneonly AND a whichTarget are mutually exclusive
        if (standaloneonly && StringUtils.ok(whichTarget)) {
            fail(Strings.get("list.instances.targetWithStandaloneOnly"));
            return false;
        }

        // long_opt is not allowed with nostatus.
        // It could be allowed in the future if desired but the table code needs
        // to change.
        if (long_opt && nostatus) {
            fail(Strings.get("list.instances.longAndNoStatus"));
            return false;
        }

        // details details details!
        // if the whichTarget is the weird screwy "server" then fail.
        // TODO - we *could* show DAS status in the future but it's stupid
        // since this command ONLY runs on DAS -- it is obviously running!!

        if (!notDas(whichTarget)) {
            fail(Strings.get("list.instances.serverTarget"));
            return false;
        }
        return true;
    }

    // avoid ugly boilerplate...
    private void fail(String s) {
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        report.setMessage(s);
    }
}
