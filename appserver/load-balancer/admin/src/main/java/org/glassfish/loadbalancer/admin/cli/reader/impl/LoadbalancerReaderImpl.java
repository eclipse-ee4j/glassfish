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

package org.glassfish.loadbalancer.admin.cli.reader.impl;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterRef;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Ref;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerRef;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.loadbalancer.admin.cli.LbLogUtil;
import org.glassfish.loadbalancer.admin.cli.reader.api.ClusterReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.LbReaderException;
import org.glassfish.loadbalancer.admin.cli.reader.api.LoadbalancerReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.PropertyReader;
import org.glassfish.loadbalancer.admin.cli.transform.LoadbalancerVisitor;
import org.glassfish.loadbalancer.admin.cli.transform.Visitor;
import org.glassfish.loadbalancer.config.LbConfig;

/**
 * Reader class to get information about load balancer configuration.
 *
 * @author Kshitiz Saxena
 */
public class LoadbalancerReaderImpl implements LoadbalancerReader {

    //--- CTORS-------
    public LoadbalancerReaderImpl(Domain domain, ApplicationRegistry appRegistry, Set<String> clusters, Properties properties) {
        _domain = domain;
        _appRegistry = appRegistry;
        _clusters = clusters;
        _properties = properties;
    }

    public LoadbalancerReaderImpl(Domain domain, ApplicationRegistry appRegistry, LbConfig lbConfig) {
        _domain = domain;
        _appRegistry = appRegistry;
        _lbConfig = lbConfig;
    }

    //--- READER IMPLEMENTATION -----
    /**
     * Returns properties of the load balancer.
     * For example response-timeout-in-seconds, reload-poll-interval-in-seconds
     * and https-routing etc.
     *
     * @return PropertyReader[]     array of properties
     */
    @Override
    public PropertyReader[] getProperties() throws LbReaderException {
        if(_lbConfig != null){
            return PropertyReaderImpl.getPropertyReaders(_lbConfig);
        } else {
            return PropertyReaderImpl.getPropertyReaders(_properties);
        }
    }

    /**
     * Returns the cluster info that are load balanced by this LB.
     *
     * @return ClusterReader        array of cluster readers
     */
    @Override
    public ClusterReader[] getClusters() throws LbReaderException {
        if (_lbConfig != null) {
            return getClustersDataFromLBConfig();
        } else if (_clusters != null) {
            return getClustersData();
        } else {
            String msg = LbLogUtil.getStringManager().getString("NoConfigOrCluster");
            throw new LbReaderException(msg);
        }
    }

    public ClusterReader[] getClustersData() throws LbReaderException {
        ClusterReader[] cls = new ClusterReader[_clusters.size()];
        Iterator<String> iter = _clusters.iterator();
        int i = 0;
        boolean isFirstServer = false;
        while (iter.hasNext()) {
            String name = iter.next();
            boolean isServer = _domain.isServer(name);
            if (i == 0) {
                isFirstServer = isServer;
            } else {
                //Mix of standalone instances and clusters is not allowed
                if (isFirstServer^isServer) {
                    String msg = LbLogUtil.getStringManager().getString("MixofServerAndClusterNotSupported");
                    throw new LbReaderException(msg);
                }
            }
            if (isServer) {
                Server server = _domain.getServerNamed(name);
                //An instance within cluster is not allowed
                if(server.getCluster() != null){
                    String msg = LbLogUtil.getStringManager().getString("ServerPartofClusterNotSupported", name);
                    throw new LbReaderException(msg);
                }
                cls[i++] = new StandAloneClusterReaderImpl(_domain, _appRegistry, server);
            } else {
                Cluster cluster = _domain.getClusterNamed(name);
                if(cluster == null){
                    String msg = LbLogUtil.getStringManager().getString("ClusterorInstanceNotFound", name);
                    throw new LbReaderException(msg);
                }
                cls[i++] = new ClusterReaderImpl(_domain, _appRegistry, cluster);
            }
        }
        return cls;
    }

    public ClusterReader[] getClustersDataFromLBConfig() throws LbReaderException {
        List<Ref> serverOrClusters = _lbConfig.getClusterRefOrServerRef();
        ClusterReader[] cls = new ClusterReader[serverOrClusters.size()];
        Iterator<Ref> iter = serverOrClusters.iterator();
        int i = 0;
        while (iter.hasNext()) {
            Ref ref = iter.next();
            if (ref instanceof ServerRef) {
                cls[i++] = new StandAloneClusterReaderImpl(_domain,
                        _appRegistry, (ServerRef) ref);

            } else if (ref instanceof ClusterRef) {
                cls[i++] = new ClusterReaderImpl(_domain, _appRegistry,
                        (ClusterRef) ref);
            } else {
                String msg = LbLogUtil.getStringManager().getString("UnableToDetermineType", ref.getRef());
                throw new LbReaderException(msg);
            }
        }
        return cls;
    }

    /**
     * Returns the name of the load balancer
     *
     * @return String               name of the LB
     */
    @Override
    public String getName() throws LbReaderException {
        if (_lbConfig != null) {
            return _lbConfig.getName();
        }
        return null;
    }

    // --- VISITOR IMPLEMENTATION ---
    @Override
    public void accept(Visitor v) throws Exception {
        if (v instanceof LoadbalancerVisitor) {
            LoadbalancerVisitor cv = (LoadbalancerVisitor) v;
            cv.visit(this);
        }
    }

    @Override
    public LbConfig getLbConfig() {
        return _lbConfig;
    }

    // --- PRIVATE VARS -----
    private LbConfig _lbConfig = null;
    private Domain _domain = null;
    private ApplicationRegistry _appRegistry = null;
    private Set<String> _clusters = null;
    private Properties _properties = null;
}
