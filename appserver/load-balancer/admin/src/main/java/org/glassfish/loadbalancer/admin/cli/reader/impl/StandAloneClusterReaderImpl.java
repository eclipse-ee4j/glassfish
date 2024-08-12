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
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HealthChecker;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerRef;

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
 * data for a stand alone server (reverse proxy).
 *
 * @author Kshitiz Saxena
 */
public class StandAloneClusterReaderImpl implements ClusterReader {

    public StandAloneClusterReaderImpl(Domain domain, ApplicationRegistry appRegistry, ServerRef ref)
            throws LbReaderException {
        _domain = domain;
        _appRegistry = appRegistry;
        _serverRef = ref;
        _server = domain.getServerNamed(_serverRef.getRef());
    }

    public StandAloneClusterReaderImpl(Domain domain, ApplicationRegistry appRegistry, Server server)
            throws LbReaderException {
        _domain = domain;
        _appRegistry = appRegistry;
        _server = server;
    }

    @Override
    public String getName() {
        return _server.getName();
    }

    @Override
    public InstanceReader[] getInstances() throws LbReaderException {
        InstanceReader[] readers = new InstanceReader[1];
        if(_serverRef != null){
            readers[0] = new InstanceReaderImpl(_domain, _serverRef);
        } else {
            readers[0] = new InstanceReaderImpl(_domain, _server);
        }
        return readers;
    }

    @Override
    public HealthCheckerReader getHealthChecker() throws LbReaderException {
        HealthChecker bean = null;
        if(_serverRef != null){
            bean = _serverRef.getHealthChecker();
        }
        if (bean == null) {
            return HealthCheckerReaderImpl.getDefaultHealthChecker();
        } else {
            HealthCheckerReader reader = new HealthCheckerReaderImpl(bean);
            return reader;
        }
    }

    @Override
    public WebModuleReader[] getWebModules() throws LbReaderException {
        List<ApplicationRef> refs = _server.getApplicationRef();

        return ClusterReaderHelper.getWebModules(_domain, _appRegistry, refs,
                _server.getName());
    }

    @Override
    public String getLbPolicy() {
        return null;
    }

    @Override
    public String getLbPolicyModule() {
        return null;
    }

    @Override
    public void accept(Visitor v) throws Exception {
        if (v instanceof ClusterVisitor) {
            ClusterVisitor cv = (ClusterVisitor) v;
            cv.visit(this);
        }
    }
    // ---- VARIABLE(S) - PRIVATE --------------------------
    private Domain _domain = null;
    private ApplicationRegistry _appRegistry = null;
    private ServerRef _serverRef = null;
    private Server _server = null;
}
