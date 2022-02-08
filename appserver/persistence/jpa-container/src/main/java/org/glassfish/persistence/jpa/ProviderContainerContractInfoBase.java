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

package org.glassfish.persistence.jpa;

import static org.glassfish.persistence.common.PersistenceHelper.lookupNonTxResource;
import static org.glassfish.persistence.common.PersistenceHelper.lookupPMResource;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.glassfish.api.deployment.DeploymentContext;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;

/**
 * Convenience base class for implementing ProviderContainerContractInfo.
 *
 * @author Mitesh Meswani
 */
public abstract class ProviderContainerContractInfoBase implements ProviderContainerContractInfo {

    private ConnectorRuntime connectorRuntime;
    private DeploymentContext context;

    public ProviderContainerContractInfoBase(ConnectorRuntime connectorRuntime) {
        // This ctor is currently called only by ACC impl of
        // ProviderContainerContractInfo which which will not deal with app/module
        // scoped resources
        this.connectorRuntime = connectorRuntime;
    }

    public ProviderContainerContractInfoBase(ConnectorRuntime connectorRuntime, DeploymentContext context) {
        this(connectorRuntime);
        this.context = context;
    }

    @Override
    public DataSource lookupDataSource(String dataSourceName) throws NamingException {
        return DataSource.class.cast(lookupPMResource(connectorRuntime, context, dataSourceName));
    }

    @Override
    public DataSource lookupNonTxDataSource(String dataSourceName) throws NamingException {
        return DataSource.class.cast(lookupNonTxResource(connectorRuntime, context, dataSourceName));
    }

    @Override
    public String getDefaultDataSourceName() {
        return DEFAULT_DS_NAME;
    }

    @Override
    public boolean isWeavingEnabled() {
        return true;
    }

}
