/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collection;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getResourcesOfPool;

/**
 * Delete Connector Connection Pool Command
 *
 */
@ExecuteOn(RuntimeType.ALL)
@Service(name="delete-connector-connection-pool")
@PerLookup
@I18n("delete.connector.connection.pool")
public class DeleteConnectorConnectionPool implements AdminCommand {
    private static final Logger LOG = System.getLogger(DeleteConnectorConnectionPool.class.getName());
    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(DeleteConnectorConnectionPool.class);

    @Param(optional = true, obsolete = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;

    @Param(optional = true, defaultValue = "false")
    private Boolean cascade;

    @Param(name = "poolname", primary = true)
    private String poolname;

    @Inject
    private Domain domain;

    @Inject
    private IterableProvider<Server> servers;
    @Inject
    private IterableProvider<Cluster> clusters;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        LOG.log(Level.DEBUG, "execute(context={0}); poolname={1}, target={2}", context, poolname, target);
        final ActionReport report = context.getActionReport();
        if (poolname == null) {
            report.setMessage(I18N.getLocalString("delete.connector.connection.pool.noJndiName",
                            "No id defined for connector connection pool."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        final SimpleJndiName jndiName = new SimpleJndiName(poolname);
        // ensure we already have this resource
        if (domain.getResources().getResourceByName(ConnectorConnectionPool.class, jndiName) == null) {
            report.setMessage(I18N.getLocalString("delete.connector.connection.pool.notfound",
                "A connector connection pool named {0} does not exist.", poolname));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            // if cascade=true delete all the resources associated with this pool
            // if cascade=false don't delete this connection pool if a resource is referencing it
            int obj = deleteAssociatedResources(servers, clusters, domain.getResources(), cascade, jndiName);
            if (obj == ResourceStatus.FAILURE) {
                report.setMessage(I18N.getLocalString(
                    "delete.connector.connection.pool.pool_in_use",
                    "Some connector resources are referencing connection pool {0}. Use 'cascade' " +
                            "option to delete them as well.", poolname));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            // delete connector connection pool
            SingleConfigCode<Resources> configCode = param -> {
                ConnectorConnectionPool cp = domain.getResources().getResourceByName(ConnectorConnectionPool.class, jndiName);
                if (cp == null) {
                    return null;
                }
                return param.getResources().remove(cp);
            };
            if (ConfigSupport.apply(configCode, domain.getResources()) == null) {
                report.setMessage(I18N.getLocalString("delete.connector.connection.pool.notfound",
                                "A connector connection pool named {0} does not exist.", poolname));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

        } catch (TransactionFailure tfe) {
            LOG.log(Level.ERROR, "Something went wrong in delete-connector-connection-pool", tfe);
            report.setMessage(tfe.getMessage() != null ? tfe.getLocalizedMessage()
                : I18N.getLocalString("delete.connector.connection.pool.fail",
                    "Connector connection pool {0} delete failed ", poolname));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }

        report.setMessage(I18N.getLocalString("delete.connector.connection.pool.success",
                "Connector connection pool {0} deleted successfully", poolname));
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private int deleteAssociatedResources(final Iterable<Server> servers, final Iterable<Cluster> clusters,
        Resources resources, final boolean cascade, final SimpleJndiName poolName) throws TransactionFailure {
        if (cascade) {
            SingleConfigCode<Resources> configCode = param -> {
                Collection<BindableResource> referringResources = getResourcesOfPool(param, poolName);
                for (BindableResource referringResource : referringResources) {
                    // delete resource-refs
                    SimpleJndiName jndiName = SimpleJndiName.of(referringResource.getJndiName());
                    LOG.log(Level.INFO, "Deleting referring resource: {0}", jndiName);
                    deleteServerResourceRefs(servers, jndiName);
                    deleteClusterResourceRefs(clusters, jndiName);
                    param.getResources().remove(referringResource);
                }
                return true; // no-op
            };
            ConfigSupport.apply(configCode, resources);
        } else {
            Collection<BindableResource> referringResources = getResourcesOfPool(resources, poolName);
            if (!referringResources.isEmpty()) {
                return ResourceStatus.FAILURE;
            }
        }
        return ResourceStatus.SUCCESS;
    }


    // TODO duplicate code in JDBCConnectionPoolManager
    private void deleteServerResourceRefs(Iterable<Server> servers, final SimpleJndiName refName) throws TransactionFailure {
        if (servers != null) {
            for (Server server : servers) {
                server.deleteResourceRef(refName);
            }
        }
    }


    private void deleteClusterResourceRefs(Iterable<Cluster> clusters, final SimpleJndiName refName) throws TransactionFailure {
        if (clusters != null) {
            for (Cluster cluster : clusters) {
                cluster.deleteResourceRef(refName);
            }
        }
    }
}
