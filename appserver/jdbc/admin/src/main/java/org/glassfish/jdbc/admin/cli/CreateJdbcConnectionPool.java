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

package org.glassfish.jdbc.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.config.serverbeans.ServerTags.DESCRIPTION;
import static org.glassfish.api.admin.RuntimeType.ALL;
import static org.glassfish.resourcebase.resources.api.ResourceStatus.FAILURE;
import static org.glassfish.resources.admin.cli.ResourceConstants.ALLOW_NON_COMPONENT_CALLERS;
import static org.glassfish.resources.admin.cli.ResourceConstants.ASSOCIATE_WITH_THREAD;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_CREATION_RETRY_ATTEMPTS;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_LEAK_RECLAIM;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_LEAK_TIMEOUT_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_POOL_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_VALIDATION_METHOD;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONN_FAIL_ALL_CONNECTIONS;
import static org.glassfish.resources.admin.cli.ResourceConstants.DATASOURCE_CLASS;
import static org.glassfish.resources.admin.cli.ResourceConstants.DRIVER_CLASSNAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.ENABLED;
import static org.glassfish.resources.admin.cli.ResourceConstants.IDLE_TIME_OUT_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.INIT_SQL;
import static org.glassfish.resources.admin.cli.ResourceConstants.IS_CONNECTION_VALIDATION_REQUIRED;
import static org.glassfish.resources.admin.cli.ResourceConstants.IS_ISOLATION_LEVEL_GUARANTEED;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.LAZY_CONNECTION_ASSOCIATION;
import static org.glassfish.resources.admin.cli.ResourceConstants.LAZY_CONNECTION_ENLISTMENT;
import static org.glassfish.resources.admin.cli.ResourceConstants.MATCH_CONNECTIONS;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAX_CONNECTION_USAGE_COUNT;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAX_POOL_SIZE;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAX_WAIT_TIME_IN_MILLIS;
import static org.glassfish.resources.admin.cli.ResourceConstants.NON_TRANSACTIONAL_CONNECTIONS;
import static org.glassfish.resources.admin.cli.ResourceConstants.PING;
import static org.glassfish.resources.admin.cli.ResourceConstants.POOLING;
import static org.glassfish.resources.admin.cli.ResourceConstants.POOL_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.POOL_SIZE_QUANTITY;
import static org.glassfish.resources.admin.cli.ResourceConstants.RES_TYPE;
import static org.glassfish.resources.admin.cli.ResourceConstants.SQL_TRACE_LISTENERS;
import static org.glassfish.resources.admin.cli.ResourceConstants.STATEMENT_CACHE_SIZE;
import static org.glassfish.resources.admin.cli.ResourceConstants.STATEMENT_LEAK_RECLAIM;
import static org.glassfish.resources.admin.cli.ResourceConstants.STATEMENT_LEAK_TIMEOUT_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.STATEMENT_TIMEOUT_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.STEADY_POOL_SIZE;
import static org.glassfish.resources.admin.cli.ResourceConstants.TRANS_ISOLATION_LEVEL;
import static org.glassfish.resources.admin.cli.ResourceConstants.VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.VALIDATION_CLASSNAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.VALIDATION_TABLE_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.WRAP_JDBC_OBJECTS;

/**
 * Create JDBC Connection Pool Command
 *
 */
@ExecuteOn(ALL)
@Service(name = "create-jdbc-connection-pool")
@PerLookup
@I18n("create.jdbc.connection.pool")
public class CreateJdbcConnectionPool implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateJdbcConnectionPool.class);

    @Param(name = "datasourceClassname", optional = true)
    String datasourceclassname;

    @Param(optional = true, name = "resType", acceptableValues = "javax.sql.DataSource,javax.sql.XADataSource,javax.sql.ConnectionPoolDataSource,java.sql.Driver")
    String restype;

    @Param(name = "steadyPoolSize", optional = true, defaultValue = "8")
    String steadypoolsize = "8";

    @Param(name = "maxPoolSize", optional = true, defaultValue = "32")
    String maxpoolsize = "32";

    @Param(name = "maxWait", alias = "maxWaitTimeInMillis", optional = true, defaultValue = "60000")
    String maxwait = "60000";

    @Param(name = "poolResize", alias = "poolResizeQuantity", optional = true, defaultValue = "2")
    String poolresize = "2";

    @Param(name = "idleTimeout", alias = "idleTimeoutInSeconds", optional = true, defaultValue = "300")
    String idletimeout = "300";

    @Param(name = "initSql", optional = true)
    String initsql;

    @Param(name = "isolationLevel", alias = "transactionIsolationLevel", optional = true)
    String isolationlevel;

    @Param(name = "isIsolationGuaranteed", alias = "isIsolationLevelGuaranteed", optional = true, defaultValue = "true")
    Boolean isisolationguaranteed;

    @Param(name = "isConnectValidateReq", alias = "isConnectionValidationRequired", optional = true, defaultValue = "false")
    Boolean isconnectvalidatereq;

    @Param(name = "validationMethod", optional = true, alias = "connectionValidationMethod", acceptableValues = "auto-commit,meta-data,table,custom-validation", defaultValue = "table")
    String validationmethod = "table";

    @Param(name = "validationTable", alias = "validationTableName", optional = true)
    String validationtable;

    @Param(name = "failConnection", alias = "failAllConnections", optional = true, defaultValue = "false")
    Boolean failconnection;

    @Param(name = "allowNonComponentCallers", optional = true, defaultValue = "false")
    Boolean allownoncomponentcallers;

    @Param(name = "nonTransactionalConnections", optional = true, defaultValue = "false")
    Boolean nontransactionalconnections;

    @Param(name = "validateAtMostOncePeriod", alias = "validateAtmostOncePeriodInSeconds", optional = true, defaultValue = "0")
    String validateatmostonceperiod = "0";

    @Param(name = "leakTimeout", alias = "connectionLeakTimeoutInSeconds", optional = true, defaultValue = "0")
    String leaktimeout = "0";

    @Param(name = "leakReclaim", alias = "connectionLeakReclaim", optional = true, defaultValue = "false")
    Boolean leakreclaim;

    @Param(name = "creationRetryAttempts", alias = "connectionCreationRetryAttempts", optional = true, defaultValue = "0")
    String creationretryattempts = "0";

    @Param(name = "creationRetryInterval", alias = "connectionCreationRetryIntervalInSeconds", optional = true, defaultValue = "10")
    String creationretryinterval = "10";

    @Param(name = "sqlTraceListeners", optional = true)
    String sqltracelisteners;

    @Param(name = "statementTimeout", alias = "statementTimeoutInSeconds", optional = true, defaultValue = "-1")
    String statementtimeout = "-1";

    @Param(name = "statementLeakTimeout", alias = "statementLeakTimeoutInSeconds", optional = true, defaultValue = "0")
    String statementLeaktimeout = "0";

    @Param(name = "statementLeakReclaim", alias = "statementLeakReclaim", optional = true, defaultValue = "false")
    Boolean statementLeakreclaim;

    @Param(name = "lazyConnectionEnlistment", optional = true, defaultValue = "false")
    Boolean lazyconnectionenlistment;

    @Param(name = "lazyConnectionAssociation", optional = true, defaultValue = "false")
    Boolean lazyconnectionassociation;

    @Param(name = "associateWithThread", optional = true, defaultValue = "false")
    Boolean associatewiththread;

    @Param(name = "driverClassname", optional = true)
    String driverclassname;

    @Param(name = "matchConnections", optional = true, defaultValue = "false")
    Boolean matchconnections;

    @Param(name = "maxConnectionUsageCount", optional = true, defaultValue = "0")
    String maxconnectionusagecount = "0";

    @Param(optional = true, defaultValue = "false")
    Boolean ping;

    @Param(optional = true, defaultValue = "true")
    Boolean pooling;

    @Param(optional = true, name = "statementCacheSize", defaultValue = "0")
    String statementcachesize;

    @Param(name = "validationClassname", optional = true)
    String validationclassname;

    @Param(name = "wrapJdbcObjects", optional = true, defaultValue = "true")
    Boolean wrapjdbcobjects;

    @Param(name = "description", optional = true)
    String description;

    @Param(name = "property", optional = true, separator = ':')
    Properties properties;

    @Param(optional = true, obsolete = true)
    String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Param(optional = true, separator = ':')
    String[] resourceNames;

    @Param(optional = true)
    String sqlFileName;

    @Param(name = "jdbc_connection_pool_id", alias = "name" /* Mapped to ResourceConstants.CONNECTION_POOL_NAME below */, primary = true)
    String jdbc_connection_pool_id;

    @Inject
    Domain domain;

    @Inject
    CommandRunner commandRunner;

    @Inject
    private JDBCResourceManager jdbcResourceManager;

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and the
     * values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(CONNECTION_POOL_NAME, jdbc_connection_pool_id);
        attributes.put(DATASOURCE_CLASS, datasourceclassname);
        attributes.put(DESCRIPTION, description);
        attributes.put(RES_TYPE, restype);
        attributes.put(STEADY_POOL_SIZE, steadypoolsize);
        attributes.put(MAX_POOL_SIZE, maxpoolsize);
        attributes.put(MAX_WAIT_TIME_IN_MILLIS, maxwait);
        attributes.put(POOL_SIZE_QUANTITY, poolresize);
        attributes.put(INIT_SQL, initsql);
        attributes.put(IDLE_TIME_OUT_IN_SECONDS, idletimeout);
        attributes.put(TRANS_ISOLATION_LEVEL, isolationlevel);
        attributes.put(IS_ISOLATION_LEVEL_GUARANTEED, isisolationguaranteed.toString());
        attributes.put(IS_CONNECTION_VALIDATION_REQUIRED, isconnectvalidatereq.toString());
        attributes.put(CONNECTION_VALIDATION_METHOD, validationmethod);
        attributes.put(VALIDATION_TABLE_NAME, validationtable);
        attributes.put(CONN_FAIL_ALL_CONNECTIONS, failconnection.toString());
        attributes.put(NON_TRANSACTIONAL_CONNECTIONS, nontransactionalconnections.toString());
        attributes.put(ALLOW_NON_COMPONENT_CALLERS, allownoncomponentcallers.toString());
        attributes.put(VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS, validateatmostonceperiod);
        attributes.put(CONNECTION_LEAK_TIMEOUT_IN_SECONDS, leaktimeout);
        attributes.put(CONNECTION_LEAK_RECLAIM, leakreclaim.toString());
        attributes.put(CONNECTION_CREATION_RETRY_ATTEMPTS, creationretryattempts);
        attributes.put(CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS, creationretryinterval);
        attributes.put(DRIVER_CLASSNAME, driverclassname);
        attributes.put(SQL_TRACE_LISTENERS, sqltracelisteners);
        attributes.put(STATEMENT_TIMEOUT_IN_SECONDS, statementtimeout);
        attributes.put(STATEMENT_LEAK_TIMEOUT_IN_SECONDS, statementLeaktimeout);
        attributes.put(STATEMENT_LEAK_RECLAIM, statementLeakreclaim.toString());
        attributes.put(STATEMENT_CACHE_SIZE, statementcachesize);
        attributes.put(LAZY_CONNECTION_ASSOCIATION, lazyconnectionassociation.toString());
        attributes.put(LAZY_CONNECTION_ENLISTMENT, lazyconnectionenlistment.toString());
        attributes.put(ASSOCIATE_WITH_THREAD, associatewiththread.toString());
        attributes.put(MATCH_CONNECTIONS, matchconnections.toString());
        attributes.put(MAX_CONNECTION_USAGE_COUNT, maxconnectionusagecount);
        attributes.put(PING, ping.toString());
        attributes.put(POOLING, pooling.toString());
        attributes.put(VALIDATION_CLASSNAME, validationclassname);
        attributes.put(WRAP_JDBC_OBJECTS, wrapjdbcobjects.toString());

        ResourceStatus resourceStatus;

        try {
            resourceStatus = new JDBCConnectionPoolManager().create(domain.getResources(), (HashMap)attributes, properties, target);
            if (resourceNames != null && resourceNames.length > 0 && resourceStatus.getException() == null && resourceStatus.getStatus() != FAILURE) {

                for (String resourceName : resourceNames) {
                    HashMap<String, String> jdbcAttributes = new HashMap<>();
                    jdbcAttributes.put(JNDI_NAME, resourceName);
                    jdbcAttributes.put(POOL_NAME, jdbc_connection_pool_id);
                    jdbcAttributes.put(DESCRIPTION, description);
                    jdbcAttributes.put(ENABLED, "true");

                    ResourceStatus jdbcStatus = jdbcResourceManager.create(domain.getResources(), jdbcAttributes, new Properties(), target);

                    resourceStatus =
                        new ResourceStatus(
                            jdbcStatus.getStatus(),
                            (resourceStatus.getMessage() == null? "" : resourceStatus.getMessage()) + " " + jdbcStatus.getMessage(),
                            resourceStatus.isAlreadyExists() || jdbcStatus.isAlreadyExists());

                    if (jdbcStatus.getException() != null) {
                        resourceStatus.setException(jdbcStatus.getException());
                    }
                }

                if (sqlFileName != null && resourceStatus.getException() == null && resourceStatus.getStatus() != FAILURE) {
                    ResourceStatus executeStatus = jdbcResourceManager.executeSql(resourceNames[0], sqlFileName);

                    resourceStatus =
                        new ResourceStatus(
                            executeStatus.getStatus(),
                            (resourceStatus.getMessage() == null? "" : resourceStatus.getMessage()) + " " + executeStatus.getMessage(),
                            resourceStatus.isAlreadyExists() || executeStatus.isAlreadyExists());
                }

            }

        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString(
                "create.jdbc.connection.pool.fail",
                "JDBC connection pool: {0} could not be created, reason: {1}", jdbc_connection_pool_id, e.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }

        if (resourceStatus.getMessage() != null) {
            report.setMessage(resourceStatus.getMessage());
        }

        ActionReport.ExitCode exitCode = ActionReport.ExitCode.SUCCESS;
        if (resourceStatus.getStatus() == FAILURE) {
            exitCode = ActionReport.ExitCode.FAILURE;
            if (resourceStatus.getMessage() == null) {
                report.setMessage(localStrings.getLocalString("create.jdbc.connection.pool.fail",
                        "JDBC connection pool {0} creation failed", jdbc_connection_pool_id, ""));
            }
            if (resourceStatus.getException() != null)
                report.setFailureCause(resourceStatus.getException());
        } else {
            // TODO only for DAS
            if ("true".equalsIgnoreCase(ping.toString())) {
                ActionReport subReport = report.addSubActionsReport();
                ParameterMap parameters = new ParameterMap();
                parameters.set("pool_name", jdbc_connection_pool_id);
                commandRunner.getCommandInvocation("ping-connection-pool", subReport, context.getSubject())
                             .parameters(parameters)
                             .execute();

                if (ActionReport.ExitCode.FAILURE.equals(subReport.getActionExitCode())) {
                    subReport.setMessage(localStrings.getLocalString("ping.create.jdbc.connection.pool.fail",
                            "\nAttempting to ping during JDBC Connection Pool " + "Creation : {0} - Failed.", jdbc_connection_pool_id));
                    subReport.setActionExitCode(ActionReport.ExitCode.FAILURE);
                } else {
                    subReport.setMessage(localStrings.getLocalString("ping.create.jdbc.connection.pool.success",
                            "\nAttempting to ping during JDBC Connection Pool " + "Creation : {0} - Succeeded.", jdbc_connection_pool_id));
                }
            }
        }

        report.setActionExitCode(exitCode);
    }
}
