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

import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterRef;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HealthChecker;
import com.sun.enterprise.config.serverbeans.ServerRef;

import java.util.Iterator;
import java.util.List;

import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.loadbalancer.admin.cli.reader.api.ClusterReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.HealthCheckerReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.InstanceReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.LbReaderException;
import org.glassfish.loadbalancer.admin.cli.reader.api.WebModuleReader;
import org.glassfish.loadbalancer.admin.cli.transform.ClusterVisitor;
import org.glassfish.loadbalancer.admin.cli.transform.Visitor;

/**
 * Impl class for ClusterReader. This provides loadbalancer
 * data for a cluster.
 *
 * @author Kshitiz Saxena
 */
public class ClusterReaderImpl implements ClusterReader {

    public ClusterReaderImpl(Domain domain, ApplicationRegistry appRegistry, Cluster cluster) {
        _domain = domain;
        _appRegistry = appRegistry;
        _cluster = cluster;
    }

    public ClusterReaderImpl(Domain domain, ApplicationRegistry appRegistry, ClusterRef clusterRef) {
        _domain = domain;
        _appRegistry = appRegistry;
        _clusterRef = clusterRef;
        _cluster = domain.getClusterNamed(clusterRef.getRef());
    }

    @Override
    public String getName() {
        return _cluster.getName();
    }

    @Override
    public InstanceReader[] getInstances() throws LbReaderException {

        List<ServerRef> servers = null;
        servers = _cluster.getServerRef();
        InstanceReader[] readers = null;

        if (servers != null) {
            readers = new InstanceReader[servers.size()];

            Iterator<ServerRef> serverIter = servers.iterator();
            int i = 0;
            while (serverIter.hasNext()) {
                readers[i++] = new InstanceReaderImpl(_domain,
                        serverIter.next());
            }
        }

        return readers;
    }

    @Override
    public HealthCheckerReader getHealthChecker() throws LbReaderException {

        if (_clusterRef == null) {
            return HealthCheckerReaderImpl.getDefaultHealthChecker();
        }

        HealthChecker bean = _clusterRef.getHealthChecker();
        if (bean == null) {
            return null;
        } else {
            HealthCheckerReader reader = new HealthCheckerReaderImpl(bean);
            return reader;
        }
    }

    @Override
    public WebModuleReader[] getWebModules() throws LbReaderException {

        List<ApplicationRef> refs = _cluster.getApplicationRef();
        return ClusterReaderHelper.getWebModules(_domain, _appRegistry, refs, _cluster.getName());
    }

    @Override
    public String getLbPolicy() {
        if (_clusterRef == null) {
            return defaultLBPolicy;
        }
        return _clusterRef.getLbPolicy();
    }

    @Override
    public String getLbPolicyModule() {
        if (_clusterRef == null) {
            return null;
        }
        return _clusterRef.getLbPolicyModule();
    }

    @Override
    public void accept(Visitor v) throws Exception{
        if(v instanceof ClusterVisitor){
        ClusterVisitor cv = (ClusterVisitor) v;
        cv.visit(this);
        }
    }
    // ---- VARIABLE(S) - PRIVATE --------------------------
    private Cluster _cluster = null;
    private ClusterRef _clusterRef = null;
    private Domain _domain = null;
    private ApplicationRegistry _appRegistry = null;
    private static final String defaultLBPolicy =
            "round-robin";
}
