/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.admin.cli.ResourceConstants;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_ASSOC_WITH_THREAD;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_CON_CREATION_RETRY_ATTEMPTS;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_CON_CREATION_RETRY_INTERVAL;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_CON_DEFN_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_CREATE_COMMAND_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_FAIL_ALL_CONNS;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_IDLE_TIMEOUT;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_IS_VALIDATION_REQUIRED;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_LAZY_CON_ASSOC;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_LAZY_CON_ENLISTMENT;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_LEAK_RECLAIM;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_LEAK_TIMEOUT;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_MATCH_CONNECTIONS;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_MAX_CON_USAGE_COUNT;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_MAX_POOL_SIZE;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_MAX_WAIT_TIME;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_PING;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_POOLING;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_POOL_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_POOL_RESIZE_QTY;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_RA_NAME;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_STEADY_POOL_SIZE;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_TXN_SUPPORT;
import static org.glassfish.connectors.admin.cli.CLIConstants.CCP.CCP_VALIDATE_ATMOST_PERIOD;
import static org.glassfish.connectors.admin.cli.CLIConstants.DESCRIPTION;
import static org.glassfish.connectors.admin.cli.CLIConstants.PROPERTY;
import static org.glassfish.connectors.admin.cli.CLIConstants.TARGET;

/**
 * Create Connector Connection Pool Command
 *
 */
@ExecuteOn(RuntimeType.ALL)
@Service(name=CCP_CREATE_COMMAND_NAME)
@PerLookup
@I18n("create.connector.connection.pool")
public class CreateConnectorConnectionPool implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateConnectorConnectionPool.class);

    @Param(name=CCP_RA_NAME, alias= "resourceAdapterName")
    String raname;

    @Param(name=CCP_CON_DEFN_NAME, alias="connectiondefinitionname")
    String connectiondefinition;

    @Param(name=CCP_STEADY_POOL_SIZE, alias="steadyPoolSize", optional=true, defaultValue="8")
    String steadypoolsize = "8";

    @Param(name=CCP_MAX_POOL_SIZE, alias="maxPoolSize", optional=true, defaultValue="32")
    String maxpoolsize = "32";

    @Param(name=CCP_MAX_WAIT_TIME, optional=true, alias="maxWaitTimeInMillis", defaultValue="60000")
    String maxwait = "60000";

    @Param(name=CCP_POOL_RESIZE_QTY, optional=true, alias="poolResizeQuantity", defaultValue="2")
    String poolresize = "2";

    @Param(name=CCP_IDLE_TIMEOUT, optional=true, alias="idleTimeoutInSeconds", defaultValue="300")
    String idletimeout = "300";

    @Param(name=CCP_IS_VALIDATION_REQUIRED, optional=true, defaultValue="false", alias="isConnectionValidationRequired")
    Boolean isconnectvalidatereq;

    @Param(name=CCP_FAIL_ALL_CONNS, optional=true, defaultValue="false", alias="failAllConnections")
    Boolean failconnection;

    @Param(name=CCP_LEAK_TIMEOUT, alias="connectionLeakTimeoutInSeconds", optional=true, defaultValue="0")
    String leaktimeout = "0";

    @Param(name=CCP_LEAK_RECLAIM, alias="connectionLeakReclaim", optional=true, defaultValue="false")
    Boolean leakreclaim;

    @Param(name=CCP_CON_CREATION_RETRY_ATTEMPTS, alias="connectionCreationRetryAttempts", optional=true, defaultValue="0")
    String creationretryattempts = "0";

    @Param(name=CCP_CON_CREATION_RETRY_INTERVAL, alias="connectionCreationRetryIntervalInSeconds", optional=true, defaultValue="10")
    String creationretryinterval = "10";

    @Param(name=CCP_LAZY_CON_ENLISTMENT, alias="lazyConnectionEnlistment", optional=true, defaultValue="false")
    Boolean lazyconnectionenlistment;

    @Param(name=CCP_LAZY_CON_ASSOC, alias="lazyConnectionAssociation", optional=true, defaultValue="false")
    Boolean lazyconnectionassociation;

    @Param(name=CCP_ASSOC_WITH_THREAD, alias="associateWithThread", optional=true, defaultValue="false")
    Boolean associatewiththread;

    @Param(name=CCP_MATCH_CONNECTIONS, alias="matchConnections", optional=true, defaultValue="true")
    Boolean matchconnections;

    @Param(name=CCP_MAX_CON_USAGE_COUNT, alias="maxConnectionUsageCount", optional=true, defaultValue="0")
    String maxconnectionusagecount = "0";

    @Param(name=CCP_PING, optional=true, defaultValue="false")
    Boolean ping;

    @Param(name=CCP_POOLING, optional=true, defaultValue="true")
    Boolean pooling;

    @Param(name=CCP_VALIDATE_ATMOST_PERIOD, alias="validateAtmostOncePeriodInSeconds", optional=true, defaultValue="0")
    String validateatmostonceperiod;

    @Param(name=CCP_TXN_SUPPORT, alias="transactionSupport", acceptableValues="XATransaction,LocalTransaction,NoTransaction", optional=true)
    String transactionsupport;

    @Param(name=DESCRIPTION, optional=true)
    String description;

    @Param(name= PROPERTY, optional=true, separator=':')
    Properties properties;

    @Param(name= TARGET, optional=true, obsolete = true)
    String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Param(name=CCP_POOL_NAME, primary=true)
    String poolname;

    @Inject
    private Domain domain;

    @Inject
    private Provider<ConnectorConnectionPoolManager> connectorConnectionPoolManagerProvider;

    @Inject
    private CommandRunner commandRunner;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        ResourceAttributes attrList = new ResourceAttributes();
        attrList.set(ResourceConstants.RES_ADAPTER_NAME, raname);
        attrList.set(ResourceConstants.CONN_DEF_NAME, connectiondefinition);
        attrList.set(ServerTags.DESCRIPTION, description);
        attrList.set(ResourceConstants.STEADY_POOL_SIZE, steadypoolsize);
        attrList.set(ResourceConstants.MAX_POOL_SIZE, maxpoolsize);
        attrList.set(ResourceConstants.MAX_WAIT_TIME_IN_MILLIS, maxwait);
        attrList.set(ResourceConstants.POOL_SIZE_QUANTITY, poolresize);
        attrList.set(ResourceConstants.IDLE_TIME_OUT_IN_SECONDS, idletimeout);
        attrList.set(ResourceConstants.IS_CONNECTION_VALIDATION_REQUIRED, isconnectvalidatereq.toString());
        attrList.set(ResourceConstants.CONN_FAIL_ALL_CONNECTIONS, failconnection.toString());
        attrList.set(ResourceConstants.VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS, validateatmostonceperiod);
        attrList.set(ResourceConstants.CONNECTION_LEAK_TIMEOUT_IN_SECONDS, leaktimeout);
        attrList.set(ResourceConstants.CONNECTION_LEAK_RECLAIM, leakreclaim.toString());
        attrList.set(ResourceConstants.CONNECTION_CREATION_RETRY_ATTEMPTS, creationretryattempts);
        attrList.set(ResourceConstants.CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS, creationretryinterval);
        attrList.set(ResourceConstants.LAZY_CONNECTION_ASSOCIATION, lazyconnectionassociation.toString());
        attrList.set(ResourceConstants.LAZY_CONNECTION_ENLISTMENT, lazyconnectionenlistment.toString());
        attrList.set(ResourceConstants.ASSOCIATE_WITH_THREAD, associatewiththread.toString());
        attrList.set(ResourceConstants.MATCH_CONNECTIONS, matchconnections.toString());
        attrList.set(ResourceConstants.MAX_CONNECTION_USAGE_COUNT, maxconnectionusagecount);
        attrList.set(ResourceConstants.CONNECTOR_CONNECTION_POOL_NAME, poolname);
        attrList.set(ResourceConstants.CONN_TRANSACTION_SUPPORT, transactionsupport);
        attrList.set(ResourceConstants.PING, ping.toString());
        attrList.set(ResourceConstants.POOLING, pooling.toString());

        ResourceStatus rs;

        try {
            ConnectorConnectionPoolManager connPoolMgr = connectorConnectionPoolManagerProvider.get();
            rs = connPoolMgr.create(domain.getResources(), attrList, properties, target);
        } catch(Exception e) {
            Logger.getLogger(CreateConnectorConnectionPool.class.getName()).log(Level.SEVERE,
                    "Unable to create connector connection pool " + poolname, e);
            String def = "Connector connection pool: {0} could not be created, reason: {1}";
            report.setMessage(localStrings.getLocalString("create.connector.connection.pool.fail",
                    def, poolname) + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        if (rs.getStatus() == ResourceStatus.FAILURE) {
            ec = ActionReport.ExitCode.FAILURE;
            if (rs.getMessage() != null) {
                report.setMessage(rs.getMessage());
            } else {
                report.setMessage(localStrings.getLocalString("create.connector.connection.pool.fail",
                    "Connector connection pool {0} creation failed.", poolname));
            }
            if (rs.getException() != null) {
                report.setFailureCause(rs.getException());
            }
        } else {
            //TODO only for DAS ?
            if ("true".equalsIgnoreCase(ping.toString())) {
                ActionReport subReport = report.addSubActionsReport();
                ParameterMap parameters = new ParameterMap();
                parameters.set("pool_name", poolname);
                commandRunner.getCommandInvocation("ping-connection-pool", subReport, context.getSubject()).parameters(parameters).execute();
                if (ActionReport.ExitCode.FAILURE.equals(subReport.getActionExitCode())) {
                    subReport.setMessage(localStrings.getLocalString("ping.create.connector.connection.pool.fail",
                            "\nAttempting to ping during Connector Connection " +
                            "Pool Creation : {0} - Failed.", poolname));
                    subReport.setActionExitCode(ActionReport.ExitCode.FAILURE);
                } else {
                    subReport.setMessage(localStrings.getLocalString("ping.create.connector.connection.pool.success",
                            "\nAttempting to ping during Connector Connection " +
                            "Pool Creation : {0} - Succeeded.", poolname));
                }
            }
        }
        if (rs.getMessage() != null) {
            report.setMessage(rs.getMessage());
        }
        report.setActionExitCode(ec);
    }
}
