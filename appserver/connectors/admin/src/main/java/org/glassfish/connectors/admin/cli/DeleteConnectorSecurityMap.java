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

package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.connectors.config.SecurityMap;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Delete Connector Work Security Map
 *
 */
@ExecuteOn(RuntimeType.ALL)
@Service(name="delete-connector-security-map")
@PerLookup
@I18n("delete.connector.security.map")
public class DeleteConnectorSecurityMap extends ConnectorSecurityMap implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DeleteConnectorSecurityMap.class);

    @Param(name="poolname")
    private String poolName;

    @Param(name="mapname", primary=true)
    private String mapName;

    @Param(optional = true, obsolete = true)
    private String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Inject
    private Domain domain;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        Collection<ConnectorConnectionPool> ccPools =  domain.getResources().getResources(ConnectorConnectionPool.class);
        // ensure we already have this resource
        if (!isResourceExists(ccPools)) {
            report.setMessage(localStrings.getLocalString(
                    "delete.connector.security.map.notFound",
                    "A security map named {0} for connector connection pool {1} does not exist.",
                    mapName, poolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {

            final ConnectorConnectionPool pool = getPool(poolName, ccPools);
            // delete connector-security-map
            ConfigSupport.apply(new SingleConfigCode<ConnectorConnectionPool>() {

                public Object run(ConnectorConnectionPool param) throws PropertyVetoException,
                        TransactionFailure {

                    final List<SecurityMap> securityMaps = param.getSecurityMap();
                    if (securityMaps != null) {
                        for (SecurityMap map : securityMaps) {
                            if (map.getName().equals(mapName)) {
                                param.getSecurityMap().remove(map);
                                break;
                            }
                        }
                    }

                    return param;
                }
            }, pool);
        } catch (TransactionFailure tfe) {
            Logger.getLogger(DeleteConnectorSecurityMap.class.getName()).log(Level.SEVERE,
                    "delete-connector-security-map failed", tfe);
            report.setMessage(localStrings.getLocalString(
                    "delete.connector.security.map.fail",
                    "Unable to delete security map {0} for connector connection pool {1}",
                    mapName, poolName) + " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
            return;
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private boolean isResourceExists(Collection<ConnectorConnectionPool> ccPools) {
        for (ConnectorConnectionPool resource : ccPools) {
            if (resource.getName().equals(poolName)) {
                for (SecurityMap sm : resource.getSecurityMap()) {
                    if (sm.getName().equals(mapName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
