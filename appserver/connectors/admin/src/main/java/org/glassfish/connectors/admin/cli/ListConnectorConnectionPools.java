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
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * List Connector Connection Pools command
 *
 */
@TargetType(value={CommandTarget.DAS,CommandTarget.DOMAIN, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTERED_INSTANCE })
@Service(name="list-connector-connection-pools")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn(value={RuntimeType.DAS})
@I18n("list.connector.connection.pools")
@RestEndpoints({
    @RestEndpoint(configBean=Resources.class,
        opType=RestEndpoint.OpType.GET,
        path="list-connector-connection-pools",
        description="list-admin-objects")
})
public class ListConnectorConnectionPools implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListConnectorConnectionPools.class);

    @Inject
    private Domain domain;

    @Param(primary = true, optional = true, alias = "targetName", obsolete = true)
    private String target ;


    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        try {
            Collection<ConnectorConnectionPool> connPools = domain.getResources().getResources(ConnectorConnectionPool.class);
            for (ConnectorConnectionPool pool : connPools) {
                final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(pool.getName());
            }
        } catch (Exception e) {
            Logger.getLogger(ListConnectorConnectionPools.class.getName()).log(Level.SEVERE,
                    "Something went wrong in list-connector-connection-pools", e);
            report.setMessage(localStrings.getLocalString("list.connector.connection.pools.failed",
                    "List connector connection pools failed"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
