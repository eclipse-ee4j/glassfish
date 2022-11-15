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

package org.glassfish.persistence.common;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.ResourceInfo;


/**
 * Contains helper methods for persistence module
 *
 * @author Mitesh Meswani
 */
public class PersistenceHelper {

    public static DataSource lookupNonTxResource(ConnectorRuntime connectorRuntime, DeploymentContext ctx, SimpleJndiName dataSourceName) throws NamingException {
        return connectorRuntime.lookupNonTxResource(getResourceInfo(ctx, dataSourceName), true);
    }

    public static DataSource lookupPMResource(ConnectorRuntime connectorRuntime, DeploymentContext ctx, SimpleJndiName dataSourceName) throws NamingException {
        return DataSource.class.cast(connectorRuntime.lookupPMResource(getResourceInfo(ctx, dataSourceName), true));
    }

    private static ResourceInfo getResourceInfo(DeploymentContext ctx, SimpleJndiName dataSourceName) {
        if (dataSourceName.isJavaApp()) {
            String applicationName = ctx.getCommandParameters(OpsParams.class).name();
            return new ResourceInfo(dataSourceName, applicationName);
        }
        return new ResourceInfo(dataSourceName);
    }
}
