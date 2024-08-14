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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.admin.util.InstanceStateService;
import com.sun.enterprise.admin.util.RemoteInstanceCommandHelper;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.cluster.InstanceInfo;

import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.InstanceState;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.config.ReferenceContainer;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.v3.admin.cluster.Constants.PARTIALLY_RUNNING;
import static com.sun.enterprise.v3.admin.cluster.Constants.PARTIALLY_RUNNING_DISPLAY;

/**
 *  This is a remote command that lists the clusters.
 * Usage: list-clusters

 * @author Bhakti Mehta
 * @author Byron Nevins
 */
@Service(name = "list-clusters")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.clusters.command")
@RestEndpoints({
    @RestEndpoint(configBean=Clusters.class,
        opType=RestEndpoint.OpType.GET,
        path="list-clusters",
        description="List Clusters")
})
public final class ListClustersCommand implements AdminCommand {

    @Inject
    private ServiceLocator habitat;
    @Inject
    Domain domain;
    @Inject
    InstanceStateService stateService;

    private static final String NONE = "Nothing to list.";
    private static final String EOL = "\n";

    @Param(optional = true, primary = true, defaultValue = "domain")
    String whichTarget;

    @Inject
    private Clusters allClusters;

    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        Logger logger = context.getLogger();
        ActionReport.MessagePart top = report.getTopMessagePart();

        List<Cluster> clusterList = null;
        //Fix for issue 13057 list-clusters doesn't take an operand
        //defaults to domain
        if (whichTarget.equals("domain" )) {
            Clusters clusters = domain.getClusters();
            clusterList = clusters.getCluster();
        } else {

            clusterList = createClusterList();

            if (clusterList == null) {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(Strings.get("list.instances.badTarget", whichTarget));
                return;
            }
        }
        StringBuilder sb = new StringBuilder();
        if (clusterList.size() < 1) {
            sb.append(NONE);
        }

        int timeoutInMsec = 2000;
        Map<String,ClusterInfo> clusterMap = new HashMap<String,ClusterInfo>();

        List<InstanceInfo> infos = new LinkedList<InstanceInfo>();
        for (Cluster cluster : clusterList) {
            String clusterName = cluster.getName();
            List<Server> servers = cluster.getInstances();

            if (servers.isEmpty()) {
                ClusterInfo ci = clusterMap.get(clusterName);
                if (ci == null ) {
                    ci = new ClusterInfo(clusterName);

                }
                ci.serversEmpty = true;
                clusterMap.put(clusterName,ci);
            }
            //Fix for issue 16273 create all InstanceInfos which will ping the instances
            //Then check the status for them
            for (Server server : servers) {
                String name = server.getName();

                if (name != null) {
                    ActionReport tReport = habitat.getService(ActionReport.class, "html");
                    InstanceInfo ii = new InstanceInfo(
                            habitat, server,
                            new RemoteInstanceCommandHelper(habitat).getAdminPort(server),
                            server.getAdminHost(),
                            clusterName, logger, timeoutInMsec, tReport, stateService);
                    infos.add(ii);
                }
            }
        }

        for(InstanceInfo ii : infos) {

            String clusterforInstance = ii.getCluster();
            ClusterInfo ci = clusterMap.get(clusterforInstance);
            if (ci == null ) {
                ci = new ClusterInfo(clusterforInstance);
            }
            ci.allInstancesRunning &= ii.isRunning();

            if (ii.isRunning()) {
                ci.atleastOneInstanceRunning = true;
            }

            clusterMap.put(clusterforInstance,ci);
        }

        //List the cluster and also the state
        //A cluster is a three-state entity and
        //list-cluster should return one of the following:

        //running (all instances running)
        //not running (no instance running)
        //partially running (at least 1 instance is not running)

        String display;
        String value ;
        for(ClusterInfo ci : clusterMap.values()) {

            if (ci.serversEmpty ||  !ci.atleastOneInstanceRunning) {
                display = InstanceState.StateType.NOT_RUNNING.getDisplayString();
                value = InstanceState.StateType.NOT_RUNNING.getDescription();
            }
            else if (ci.allInstancesRunning) {
                display = InstanceState.StateType.RUNNING.getDisplayString();
                value = InstanceState.StateType.RUNNING.getDescription();
            }
            else {
                display = PARTIALLY_RUNNING_DISPLAY;
                value = PARTIALLY_RUNNING;
            }
            sb.append(ci.getName()).append(display).append(EOL);
            top.addProperty(ci.getName(), value);

        }

        String output = sb.toString();
        //Fix for isue 12885
        report.setMessage(output.substring(0,output.length()-1 ));
    }

    /*
    * if target was junk then return all the clusters
    */
    private List<Cluster> createClusterList() {
        // 1. no whichTarget specified
        if (!StringUtils.ok(whichTarget))
            return allClusters.getCluster();

        ReferenceContainer rc = domain.getReferenceContainerNamed(whichTarget);
        // 2. Not a server or a cluster. Could be a config or a Node
        if (rc == null) {
            return getClustersForNodeOrConfig();
        }
        else if (rc.isServer()) {
            Server s =((Server) rc);
            List<Cluster> cl = new LinkedList<Cluster>();
            cl.add(s.getCluster());
            return  cl;
        }
        else if (rc.isCluster()) {
            Cluster cluster = (Cluster) rc;
            List<Cluster> cl = new LinkedList<Cluster>();
            cl.add(cluster);
            return cl;
        }
        else
            return null;
    }

     private List<Cluster> getClustersForNodeOrConfig() {
        if (whichTarget == null)
            throw new NullPointerException("impossible!");

        List<Cluster> list = getClustersForNode();

        if (list == null)
            list = getClustersForConfig();

        return list;
    }

     private List<Cluster> getClustersForNode() {
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
         if (!foundNode)
             return null;
         else
             return domain.getClustersOnNode(whichTarget);
    }

     private List<Cluster> getClustersForConfig() {
        Config config = domain.getConfigNamed(whichTarget);

        if (config == null)
            return null;

        List<ReferenceContainer> rcs = domain.getReferenceContainersOf(config);
        List<Cluster> clusters = new LinkedList<Cluster>();

        for (ReferenceContainer rc : rcs)
            if (rc.isCluster())
                clusters.add((Cluster) rc);

        return clusters;
    }

     private static class ClusterInfo {

        private boolean atleastOneInstanceRunning = false;
        private boolean allInstancesRunning = true;
        private boolean serversEmpty = false;
        private String name;

        public String getName() {
            return name;
        }

        private ClusterInfo (String name) {
            this.name = name;
        }

    }
}


