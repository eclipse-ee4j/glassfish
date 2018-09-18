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

package org.glassfish.persistence.common;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.resourcebase.resources.api.ResourceInfo;

import javax.naming.NamingException;
import javax.sql.DataSource;


/**
 * Contains helper methods for persistence module
 * @author Mitesh Meswani
 */
public class PersistenceHelper {

    public static DataSource lookupNonTxResource(ConnectorRuntime connectorRuntime, DeploymentContext ctx, String dataSourceName) throws NamingException {
        return DataSource.class.cast(connectorRuntime.lookupNonTxResource(getResourceInfo(ctx, dataSourceName), true));
    }

    public static DataSource lookupPMResource(ConnectorRuntime connectorRuntime, DeploymentContext ctx, String dataSourceName) throws NamingException {
        return DataSource.class.cast(connectorRuntime.lookupPMResource(getResourceInfo(ctx, dataSourceName), true));
    }

    private static ResourceInfo getResourceInfo(DeploymentContext ctx, String dataSourceName) {
        ResourceInfo resourceInfo;
        if(dataSourceName.startsWith("java:app") /* || jndiName.startsWith("java:module") // Use of module scoped resources from JPA still needs to be speced out*/){
            String applicationName  = ctx.getCommandParameters(OpsParams.class).name();
            resourceInfo = new ResourceInfo(dataSourceName, applicationName);
        }else{
            resourceInfo = new ResourceInfo(dataSourceName);
        }
        return resourceInfo;
    }
}
