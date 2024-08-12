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
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.types.ResourceReferenceContainer;
import com.sun.enterprise.deployment.util.DOLUtils;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.ResourceInfo;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Contains helper methods for persistence module
 *
 * @author Mitesh Meswani
 */
public class PersistenceHelper {

    private static final System.Logger LOGGER = System.getLogger(PersistenceHelper.class.getName());

    public static DataSource lookupNonTxResource(ConnectorRuntime connectorRuntime, DeploymentContext ctx, SimpleJndiName dataSourceName) throws NamingException {
        return connectorRuntime.lookupNonTxResource(getResourceInfo(ctx, dataSourceName), true);
    }

    public static DataSource lookupPMResource(ConnectorRuntime connectorRuntime, DeploymentContext ctx, SimpleJndiName dataSourceName) throws NamingException {
        return DataSource.class.cast(connectorRuntime.lookupPMResource(getResourceInfo(ctx, dataSourceName), true));
    }

    private static ResourceInfo getResourceInfo(DeploymentContext ctx, SimpleJndiName dataSourceName) {
        if (ctx != null) {
            // ctx can be null e.g. in appclient
            dataSourceName = translateResourceReference(ctx, dataSourceName);
        }
        if (dataSourceName.isJavaApp()) {
            String applicationName = ctx.getCommandParameters(OpsParams.class).name();
            return new ResourceInfo(dataSourceName, applicationName);
        }
        return new ResourceInfo(dataSourceName);
    }

    private static SimpleJndiName translateResourceReference(DeploymentContext ctx, SimpleJndiName dataSourceName) {
        final BundleDescriptor currentBundle = DOLUtils.getCurrentBundleForContext(ctx);
        if (currentBundle instanceof ResourceReferenceContainer) {
            ResourceReferenceContainer referenceContainer = (ResourceReferenceContainer)currentBundle;
            try {
                return referenceContainer.getResourceReferenceByName(dataSourceName.toString()).getJndiName();
            } catch (IllegalArgumentException e) {
                LOGGER.log(DEBUG, () -> "Datasource name " + dataSourceName + " is not a reference, will use it as a JNDI name. Error: " + e.getMessage(), e);
                return dataSourceName;
            }
        }
        return dataSourceName;

    }
}
