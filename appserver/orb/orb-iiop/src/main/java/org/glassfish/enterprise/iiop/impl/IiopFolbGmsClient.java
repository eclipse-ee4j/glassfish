/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.enterprise.iiop.impl;

import com.sun.corba.ee.impl.folb.GroupInfoServiceBase;
import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.corba.ee.spi.folb.GroupInfoServiceObserver;
import com.sun.corba.ee.spi.folb.SocketInfo;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.ee.cms.core.CallBack;
import com.sun.enterprise.ee.cms.core.FailureNotificationSignal;
import com.sun.enterprise.ee.cms.core.JoinedAndReadyNotificationSignal;
import com.sun.enterprise.ee.cms.core.PlannedShutdownSignal;
import com.sun.enterprise.ee.cms.core.Signal;
import com.sun.enterprise.ee.cms.core.SignalReleaseException;

import java.lang.System.Logger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.config.support.GlassFishConfigBean;
import org.glassfish.config.support.PropertyResolver;
import org.glassfish.gms.bootstrap.GMSAdapter;
import org.glassfish.gms.bootstrap.GMSAdapterService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.orb.admin.config.IiopListener;
import org.glassfish.orb.admin.config.IiopService;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;


/**
 * @author Harold Carr
 */
class IiopFolbGmsClient implements CallBack {

    private static final Logger LOG = System.getLogger(IiopFolbGmsClient.class.getName());

    private ServiceLocator services;
    private Domain domain;
    private Server myServer;
    private Nodes nodes;
    private GMSAdapterService gmsAdapterService;
    private GMSAdapter gmsAdapter;
    private Map<String, ClusterInstanceInfo> currentMembers;
    private GroupInfoService gis;

    IiopFolbGmsClient(ServiceLocator services) {
        LOG.log(DEBUG, "IiopFolbGmsClient: constructor: services {0}", services);
        this.services = services;

        gmsAdapterService = services.getService(GMSAdapterService.class);

        try {
            if (gmsAdapterService == null) {
                return;
            }

            gmsAdapter = gmsAdapterService.getGMSAdapter();
            LOG.log(DEBUG, "IiopFolbGmsClient: gmsAdapter {0}", gmsAdapter);

            if (gmsAdapter == null) {
                LOG.log(DEBUG, "IiopFolbGmsClient: gmsAdapterService is null");
                gis = new GroupInfoServiceNoGMSImpl();
            } else {
                domain = services.getService(Domain.class);
                LOG.log(DEBUG, "IiopFolbGmsClient: domain {0}", domain);

                Servers servers = services.getService(Servers.class);
                LOG.log(DEBUG, "IiopFolbGmsClient: servers {0}", servers);

                nodes = services.getService(Nodes.class);
                LOG.log(DEBUG, "IiopFolbGmsClient: nodes {0}", nodes);

                String instanceName = gmsAdapter.getModule().getInstanceName();
                LOG.log(DEBUG, "IiopFolbGmsClient: instanceName {0}", instanceName);

                myServer = servers.getServer(instanceName);
                LOG.log(DEBUG, "IiopFolbGmsClient: myServer {0}", myServer);

                gis = new GroupInfoServiceGMSImpl();
                LOG.log(DEBUG, "IiopFolbGmsClient: IIOP GIS created");

                currentMembers = getAllClusterInstanceInfo();
                LOG.log(DEBUG, "IiopFolbGmsClient: currentMembers = ", currentMembers);

                LOG.log(DEBUG, "iiop instance info = " + getIIOPEndpoints());

                gmsAdapter.registerFailureNotificationListener(this);
                gmsAdapter.registerJoinedAndReadyNotificationListener(this);
                gmsAdapter.registerPlannedShutdownListener(this);

                LOG.log(DEBUG, "IiopFolbGmsClient: GMS action factories added");
            }

        } catch (Exception e) {
            LOG.log(ERROR, "Initialization failed.", e);
        } finally {
            LOG.log(DEBUG, "IiopFolbGmsClient: gmsAdapter {0}", gmsAdapter);
        }
    }

    GroupInfoService getGroupInfoService() {
        return gis ;
    }

    boolean isGMSAvailable() {
        return gmsAdapter != null ;
    }

    ////////////////////////////////////////////////////
    //
    // Action
    //

    @Override
    public void processNotification(final Signal signal) {
        try {
            LOG.log(INFO, "processNotification: signal {0}", signal);
            signal.acquire();
            LOG.log(DEBUG, "Signal from: {0}, member details: {1}",
                signal.getMemberToken(),
                signal.getMemberDetails().entrySet());

            if (signal instanceof PlannedShutdownSignal || signal instanceof FailureNotificationSignal) {
                removeMember(signal);
            } else if (signal instanceof JoinedAndReadyNotificationSignal) {
                addMember(signal);
            } else {
                LOG.log(ERROR, "IiopFolbGmsClient.handleSignal: unknown signal: {0}", signal);
            }
        } catch (Exception e) {
            LOG.log(ERROR, "Could not handle signal " + signal, e);
        } finally {
            try {
                signal.release();
            } catch (SignalReleaseException e) {
                LOG.log(ERROR, "signal.release failed.", e);
            }
        }
    }

    private void removeMember(final Signal signal) {
        String instanceName = signal.getMemberToken();
        try {
            LOG.log(DEBUG, "IiopFolbGmsClient.removeMember->: {0}", instanceName);

            synchronized (this) {
                if (currentMembers.get(instanceName) != null) {
                    currentMembers.remove(instanceName);

                    LOG.log(DEBUG, "IiopFolbGmsClient.removeMember: {0} removed - notifying listeners", instanceName);

                    gis.notifyObservers();

                    LOG.log(DEBUG, "IiopFolbGmsClient.removeMember: {0} - notification complete", instanceName);
                } else {
                    LOG.log(DEBUG, "IiopFolbGmsClient.removeMember: {0} not present: no action", instanceName);
                }
            }
        } finally {
            LOG.log(DEBUG, "IiopFolbGmsClient.removeMember<-: {0}", instanceName);
        }
    }


    private void addMember(final Signal signal) {
        final String instanceName = signal.getMemberToken();
        try {
            LOG.log(DEBUG, "IiopFolbGmsClient.addMember->: {0}", instanceName);

            synchronized (this) {
                if (currentMembers.get(instanceName) != null) {
                    LOG.log(DEBUG, "IiopFolbGmsClient.addMember: {0} already present: no action", instanceName);
                } else {
                    ClusterInstanceInfo clusterInstanceInfo = getClusterInstanceInfo(instanceName);

                    currentMembers.put(clusterInstanceInfo.name(), clusterInstanceInfo);

                    LOG.log(DEBUG, "IiopFolbGmsClient.addMember: {0} added - notifying listeners", instanceName);

                    gis.notifyObservers();

                    LOG.log(DEBUG, "IiopFolbGmsClient.addMember: {0} - notification complete", instanceName);
                }
            }
        } finally {
            LOG.log(DEBUG, "IiopFolbGmsClient.addMember<-: {0}", instanceName);
        }
    }


    private int resolvePort(Server server, IiopListener listener) {
        LOG.log(DEBUG, "resolvePort: server {0} listener {1}", server, listener);

        IiopListener ilRaw = GlassFishConfigBean.getRawView(listener);
        LOG.log(DEBUG, "resolvePort: ilRaw {0}", ilRaw);

        PropertyResolver pr = new PropertyResolver(domain, server.getName());
        LOG.log(DEBUG, "resolvePort: pr {0}", pr);

        String port = pr.getPropertyValue(ilRaw.getPort());
        LOG.log(DEBUG, "resolvePort: port {0}", port);

        return Integer.parseInt(port);
    }


    private ClusterInstanceInfo getClusterInstanceInfo(Server server, Config config, boolean assumeInstanceIsRunning) {
        if (server == null) {
            return null;
        }

        if (!assumeInstanceIsRunning) {
            if (!server.isListeningOnAdminPort()) {
                return null;
            }
        }

        LOG.log(DEBUG, "getClusterInstanceInfo: server {0}, config {1}", server, config);

        final String name = server.getName();
        LOG.log(DEBUG, "getClusterInstanceInfo: name {0}", name);

        final int weight = Integer.parseInt(server.getLbWeight());
        LOG.log(DEBUG, "getClusterInstanceInfo: weight {0}", weight);

        final String nodeName = server.getNodeRef();
        String hostName = nodeName;
        if (nodes != null) {
            Node node = nodes.getNode(nodeName);
            if (node != null) {
                if (node.isLocal()) {
                    try {
                        hostName = InetAddress.getLocalHost().getHostName();
                    } catch (UnknownHostException exc) {
                        LOG.log(DEBUG, "getClusterInstanceInfo: caught exception for localhost lookup {0}", exc);
                    }
                } else {
                    hostName = node.getNodeHost();
                }
            }
        }

        LOG.log(DEBUG, "getClusterInstanceInfo: host {0}", hostName);

        final IiopService iservice = config.getExtensionByType(IiopService.class);
        LOG.log(DEBUG, "getClusterInstanceInfo: iservice {0}", iservice);

        final List<IiopListener> listeners = iservice.getIiopListener();
        LOG.log(DEBUG, "getClusterInstanceInfo: listeners {0}", listeners);

        final List<SocketInfo> sinfos = new ArrayList<>();
        for (IiopListener il : listeners) {
            SocketInfo sinfo = new SocketInfo(il.getId(), hostName, resolvePort(server, il));
            sinfos.add(sinfo);
        }
        LOG.log(DEBUG, "getClusterInstanceInfo: sinfos {0}", sinfos);

        final ClusterInstanceInfo result = new ClusterInstanceInfo(name, weight, sinfos);
        LOG.log(DEBUG, "getClusterInstanceInfo: result {0}", result);

        return result;
    }


    private Config getConfigForServer(Server server) {
        LOG.log(DEBUG, "getConfigForServer: server {0}", server);

        String configRef = server.getConfigRef();
        LOG.log(DEBUG, "getConfigForServer: configRef {0}", configRef);

        Configs configs = services.getService(Configs.class);
        LOG.log(DEBUG, "getConfigForServer: configs {0}", configs);

        Config config = configs.getConfigByName(configRef);
        LOG.log(DEBUG, "getConfigForServer: config {0}", config);

        return config;
    }


    // For addMember.
    private ClusterInstanceInfo getClusterInstanceInfo(String instanceName) {
        LOG.log(DEBUG, "getClusterInstanceInfo: instanceName {0}", instanceName);

        final Servers servers = services.getService(Servers.class);
        LOG.log(DEBUG, "getClusterInstanceInfo: servers {0}", servers);

        final Server server = servers.getServer(instanceName);
        LOG.log(DEBUG, "getClusterInstanceInfo: server {0}", server);

        final Config config = getConfigForServer(server);
        LOG.log(DEBUG, "getClusterInstanceInfo: servers {0}", servers);

        // assumeInstanceIsRunning is set to true since this is
        // coming from addMember, because shoal just told us that the instance is up.
        ClusterInstanceInfo result = getClusterInstanceInfo(server, config, true);
        LOG.log(DEBUG, "getClusterInstanceInfo: result {0}", result);

        return result;
    }


    private Map<String, ClusterInstanceInfo> getAllClusterInstanceInfo() {
        final Cluster myCluster = myServer.getCluster();
        LOG.log(DEBUG, "getAllClusterInstanceInfo: myCluster {0}", myCluster);

        final Config myConfig = getConfigForServer(myServer);
        LOG.log(DEBUG, "getAllClusterInstanceInfo: myConfig {0}", myConfig);

        final Map<String, ClusterInstanceInfo> result = new HashMap<>();

        // When myServer is DAS's situation, myCluster is null.
        // null check is needed.
        if (myCluster != null) {
            for (Server server : myCluster.getInstances()) {
                ClusterInstanceInfo cii = getClusterInstanceInfo(server, myConfig, false);
                if (cii != null) {
                    result.put(server.getName(), cii);
                }
            }
        }

        LOG.log(DEBUG, "getAllClusterInstanceInfo: result {0}", result);
        return result;
    }


    // return host:port,... string for all clear text ports in the cluster
    // instance info.
    public final String getIIOPEndpoints() {
        final Map<String, ClusterInstanceInfo> cinfos = getAllClusterInstanceInfo();
        final StringBuilder result = new StringBuilder();
        boolean first = true;
        for (ClusterInstanceInfo cinfo : cinfos.values()) {
            for (SocketInfo sinfo : cinfo.endpoints()) {
                if (!sinfo.type().startsWith("SSL")) {
                    if (first) {
                        first = false;
                    } else {
                        result.append(',');
                    }

                    result.append(sinfo.host()).append(':').append(sinfo.port());
                }
            }
        }
        return result.toString();
    }


    class GroupInfoServiceGMSImpl extends GroupInfoServiceBase {

        @Override
        public List<ClusterInstanceInfo> internalClusterInstanceInfo(List<String> endpoints) {
            LOG.log(DEBUG, "internalClusterInstanceInfo: currentMembers {0}", currentMembers);
            if (currentMembers == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(currentMembers.values());
        }
    }

    class GroupInfoServiceNoGMSImpl extends GroupInfoServiceGMSImpl {

        @Override
        public boolean addObserver(GroupInfoServiceObserver x) {
            throw new RuntimeException("SHOULD NOT BE CALLED");
        }


        @Override
        public void notifyObservers() {
            throw new RuntimeException("SHOULD NOT BE CALLED");
        }


        @Override
        public boolean shouldAddAddressesToNonReferenceFactory(String[] x) {
            throw new RuntimeException("SHOULD NOT BE CALLED");
        }


        @Override
        public boolean shouldAddMembershipLabel(String[] adapterName) {
            throw new RuntimeException("SHOULD NOT BE CALLED");
        }
    }
}
