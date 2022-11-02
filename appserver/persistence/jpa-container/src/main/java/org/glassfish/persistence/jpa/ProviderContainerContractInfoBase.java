/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.persistence.jpa;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.naming.SimpleJndiName;

import static org.glassfish.persistence.common.PersistenceHelper.lookupNonTxResource;
import static org.glassfish.persistence.common.PersistenceHelper.lookupPMResource;

/**
 * Convenience base class for implementing ProviderContainerContractInfo.
 *
 * @author Mitesh Meswani
 */
public abstract class ProviderContainerContractInfoBase implements ProviderContainerContractInfo {

    private final ConnectorRuntime connectorRuntime;
    private final DeploymentContext context;

    public ProviderContainerContractInfoBase(ConnectorRuntime connectorRuntime) {
        // This ctor is currently called only by ACC impl of
        // ProviderContainerContractInfo which which will not deal with app/module
        // scoped resources
        this.connectorRuntime = connectorRuntime;
        this.context = null;
    }

    public ProviderContainerContractInfoBase(ConnectorRuntime connectorRuntime, DeploymentContext context) {
        this.connectorRuntime = connectorRuntime;
        this.context = context;
    }

    @Override
    public DataSource lookupDataSource(SimpleJndiName dataSourceName) throws NamingException {
        return lookupPMResource(connectorRuntime, context, dataSourceName);
    }

    @Override
    public DataSource lookupNonTxDataSource(SimpleJndiName dataSourceName) throws NamingException {
        return lookupNonTxResource(connectorRuntime, context, dataSourceName);
    }

    @Override
    public SimpleJndiName getDefaultDataSourceName() {
        return DEFAULT_DS_NAME;
    }

    @Override
    public boolean isWeavingEnabled() {
        return true;
    }

}
