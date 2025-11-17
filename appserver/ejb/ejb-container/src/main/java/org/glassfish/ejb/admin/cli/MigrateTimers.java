/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.admin.cli;

import com.sun.ejb.containers.EJBTimerService;
import com.sun.ejb.containers.EjbContainerUtil;
import com.sun.enterprise.admin.util.ClusterOperationUtil;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.common.util.admin.ParameterMapExtractor;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;

@Service(name = "migrate-timers")
@PerLookup
@I18n("migrate.timers")
@org.glassfish.api.admin.ExecuteOn(value = {RuntimeType.INSTANCE}, ifNeverStarted = FailurePolicy.Error)
@TargetType(value = {CommandTarget.DAS, CommandTarget.CLUSTERED_INSTANCE})
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.POST,
        path="migrate-timers",
        description="Migrate Timers")
})
public class MigrateTimers implements AdminCommand {

    static StringManager localStrings = StringManager.getManager(MigrateTimers.class);

    private static final Logger logger =
        LogDomains.getLogger(MigrateTimers.class, LogDomains.EJB_LOGGER);

    @Param(name = "target", optional = true, alias="destination",
        defaultValue=SystemPropertyConstants.DAS_SERVER_NAME)
    public String target;

    private boolean needRedirect;

    @Param(name = "fromServer", primary = true, optional = false)
    public String fromServer;

    @Inject
    private EjbContainerUtil ejbContainerUtil;

    @Inject
    private Domain domain;

    @Inject
    Target targetUtil;

    @Inject
    private ServiceLocator habitat;

    /**
     * Executes the command
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        String error = validate();
        if (error != null) {
            report.setMessage(error);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            if (needRedirect) {
                needRedirect = false;
                ParameterMapExtractor mapExtractor = new ParameterMapExtractor(this);
                ParameterMap params = mapExtractor.extract();
                logger.info(localStrings.getString("migrate.timers.redirect",
                        target, params.toCommaSeparatedString()));

                ClusterOperationUtil.replicateCommand("migrate-timers",
                        FailurePolicy.Error, FailurePolicy.Error, FailurePolicy.Error,
                        Arrays.asList(new String[]{target}),
                        context, params, habitat);
                return;
            }

            int totalTimersMigrated = migrateTimers(fromServer);
            report.setMessage(localStrings.getString("migrate.timers.count",
                    totalTimersMigrated, fromServer, target));
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (Exception e) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    private String validate() {
        //verify fromServer is clusteredInstance
        Cluster fromServerCluster = targetUtil.getClusterForInstance(fromServer);
        if(fromServerCluster == null) {
            return localStrings.getString("migrate.timers.fromServerNotClusteredInstance", fromServer);
        }

        //verify fromServer is not running
        if (isServerRunning(fromServer)) {
            return localStrings.getString(
                    "migrate.timers.migrateFromServerStillRunning", fromServer);
        }

        //if destinationServer is not set, or set to DAS, pick a running instance
        //in the same cluster as fromServer
        if(target.equals(SystemPropertyConstants.DAS_SERVER_NAME)) {
            List<Server> instances = fromServerCluster.getInstances();
            for(Server instance : instances) {
                if(instance.isListeningOnAdminPort()) {
                    target = instance.getName();
                    needRedirect = true;
                }
            }
            //if destination is still DAS, that means no running server is available
            if(target.equals(SystemPropertyConstants.DAS_SERVER_NAME)) {
                return localStrings.getString("migrate.timers.noRunningInstanceToChoose",
                        target);
            }
        } else {
            //verify fromServer and destinationServer are in the same cluster, and
            //verify destination is a clustered instance.
            Cluster destinationServerCluster = targetUtil.getClusterForInstance(target);
            if (!fromServerCluster.getName().equals(destinationServerCluster.getName())) {
                return localStrings.getString(
                        "migrate.timers.fromServerAndTargetNotInSameCluster", fromServer, target);
            }
            //verify destinationServer is running
            if (!isServerRunning(target)) {
                return localStrings.getString("migrate.timers.destinationServerIsNotAlive", target);
            }
        }

        return null;
    }

    private boolean isServerRunning(String serverName) {
        Server server = domain.getServerNamed(serverName);
        return server == null ? false : server.isListeningOnAdminPort();
    }

    private int migrateTimers( String serverId ) {
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "[MigrateTimers] migrating timers from " + serverId);
        }

        int result = 0;
        if (EJBTimerService.isEJBTimerServiceLoaded()) {
            EJBTimerService ejbTimerService = EJBTimerService.getEJBTimerService();
            if (ejbTimerService != null) {
                result = ejbTimerService.migrateTimers( serverId );
            }
        } else {
            //throw new IllegalStateException("EJB Timer service is null. "
                    //+ "Cannot migrate timers for: " + serverId);
        }

        return result;
    }
}
