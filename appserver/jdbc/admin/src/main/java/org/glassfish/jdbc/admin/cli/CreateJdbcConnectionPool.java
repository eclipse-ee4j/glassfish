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

package org.glassfish.jdbc.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.util.Properties;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.Job;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.admin.cli.ResourceConstants;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.config.serverbeans.ServerTags.DESCRIPTION;
import static org.glassfish.api.admin.RuntimeType.ALL;
import static org.glassfish.resourcebase.resources.api.ResourceStatus.FAILURE;
import static org.glassfish.resources.admin.cli.ResourceConstants.ENABLED;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.POOL_NAME;

/**
 * Create JDBC Connection Pool Command
 */
@ExecuteOn(ALL)
@Service(name = "create-jdbc-connection-pool")
@PerLookup
@I18n("create.jdbc.connection.pool")
public class CreateJdbcConnectionPool implements AdminCommand {

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(CreateJdbcConnectionPool.class);

    @Param(name = "datasourceClassname",  optional=true)
    private String datasourceclassname;

    @Param(optional=true, name = "resType",  acceptableValues="javax.sql.DataSource,javax.sql.XADataSource,javax.sql.ConnectionPoolDataSource,java.sql.Driver")
    private String restype;

    @Param(name = "steadyPoolSize",  optional=true, defaultValue = "8")
    private String steadypoolsize = "8";

    @Param(name = "maxPoolSize",  optional=true, defaultValue = "32")
    private String maxpoolsize = "32";

    @Param(name="maxWait", alias = "maxWaitTimeInMillis",  optional=true, defaultValue = "60000")
    private String maxwait = "60000";

    @Param(name="poolResize", alias = "poolResizeQuantity",  optional=true, defaultValue = "2")
    private String poolresize = "2";

    @Param(name="idleTimeout", alias = "idleTimeoutInSeconds",  optional=true, defaultValue = "300")
    private String idletimeout = "300";

    @Param(name = "initSql", optional=true)
    private String initsql;

    @Param(name="isolationLevel", alias = "transactionIsolationLevel",  optional=true)
    private String isolationlevel;

    @Param(name="isIsolationGuaranteed", alias = "isIsolationLevelGuaranteed",  optional=true, defaultValue="true")
    private Boolean isisolationguaranteed;

    @Param(name="isConnectValidateReq", alias = "isConnectionValidationRequired",  optional=true, defaultValue="false")
    private Boolean isconnectvalidatereq;

    @Param(name = "validationMethod", optional=true, alias = "connectionValidationMethod",  acceptableValues="auto-commit,meta-data,table,custom-validation", defaultValue = "table")
    private String validationmethod = "table";

    @Param(name="validationTable", alias = "validationTableName",  optional=true)
    private String validationtable;

    @Param(name="failConnection", alias = "failAllConnections",  optional=true, defaultValue="false")
    private Boolean failconnection;

    @Param(name = "allowNonComponentCallers",  optional=true, defaultValue="false")
    private Boolean allownoncomponentcallers;

    @Param(name = "nonTransactionalConnections",  optional=true, defaultValue="false")
    private Boolean nontransactionalconnections;

    @Param(name="validateAtMostOncePeriod", alias = "validateAtmostOncePeriodInSeconds",  optional=true, defaultValue = "0")
    private String validateatmostonceperiod = "0";

    @Param(name="leakTimeout", alias = "connectionLeakTimeoutInSeconds",  optional=true, defaultValue = "0")
    private String leaktimeout = "0";

    @Param(name="leakReclaim", alias = "connectionLeakReclaim",  optional=true, defaultValue="false")
    private Boolean leakreclaim;

    @Param(name="creationRetryAttempts", alias = "connectionCreationRetryAttempts",  optional=true, defaultValue = "0")
    private String creationretryattempts = "0";

    @Param(name="creationRetryInterval", alias = "connectionCreationRetryIntervalInSeconds",  optional=true, defaultValue = "10")
    private String creationretryinterval = "10";

    @Param(name = "sqlTraceListeners", optional=true)
    private String sqltracelisteners;

    @Param(name="statementTimeout", alias = "statementTimeoutInSeconds",  optional=true, defaultValue = "-1")
    private String statementtimeout = "-1";

    @Param(name="statementLeakTimeout", alias = "statementLeakTimeoutInSeconds",  optional=true, defaultValue = "0")
    private String statementLeaktimeout = "0";

    @Param(name="statementLeakReclaim", alias = "statementLeakReclaim",  optional=true, defaultValue="false")
    private Boolean statementLeakreclaim;

    @Param(name = "lazyConnectionEnlistment",  optional=true, defaultValue="false")
    private Boolean lazyconnectionenlistment;

    @Param(name = "lazyConnectionAssociation",  optional=true, defaultValue="false")
    private Boolean lazyconnectionassociation;

    @Param(name = "associateWithThread",  optional=true, defaultValue="false")
    private Boolean associatewiththread;

    @Param(name = "driverClassname",  optional=true)
    private String driverclassname;

    @Param(name = "matchConnections",  optional=true, defaultValue="false")
    private Boolean matchconnections;

    @Param(name = "maxConnectionUsageCount",  optional=true, defaultValue = "0")
    private String maxconnectionusagecount = "0";

    @Param(optional=true, defaultValue="false")
    private Boolean ping;

    @Param(optional=true, defaultValue="true")
    private Boolean pooling;

    @Param(optional=true, name = "statementCacheSize",  defaultValue="0")
    private String statementcachesize;

    @Param(name = "validationClassname",  optional=true)
    private String validationclassname;

    @Param(name = "wrapJdbcObjects",  optional=true, defaultValue="true")
    private Boolean wrapjdbcobjects;

    @Param(name="description", optional=true)
    private String description;

    @Param(name="property", optional=true, separator=':')
    private Properties properties;

    @Param(optional=true, obsolete = true)
    private String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Param(optional = true, separator = ':')
    private String[] resourceNames;

    @Param(optional = true)
    private String sqlFileName;

    @Param(name="jdbc_connection_pool_id", alias = "name" /*Mapped to ResourceConstants.CONNECTION_POOL_NAME below */,  primary=true)
    private String jdbc_connection_pool_id;

    @Inject
    private Domain domain;

    @Inject
    private CommandRunner<Job> commandRunner;

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
        createPool(report);
        if (report.hasFailures()) {
            return;
        }
        if (resourceNames != null && resourceNames.length > 0) {
            createResources(report);
            if (report.hasFailures()) {
                return;
            }
            if (sqlFileName != null) {
                execSqlFile(resourceNames[0], report);
            }
        }
        if (report.hasFailures()) {
            return;
        }
        if (ping == Boolean.TRUE) {
            pingPool(context.getSubject(), report);
        }
    }

    private void pingPool(Subject subject, ActionReport report) {
        ActionReport subReport = report.addSubActionsReport();
        ParameterMap parameters = new ParameterMap();
        parameters.set("pool_name", jdbc_connection_pool_id);
        commandRunner.getCommandInvocation("ping-connection-pool", subReport, subject)
                     .parameters(parameters)
                     .execute();

        if (ExitCode.FAILURE.equals(subReport.getActionExitCode())) {
            subReport.setMessage(I18N.getLocalString("ping.create.jdbc.connection.pool.fail",
                    "\nAttempting to ping during JDBC Connection Pool " +
                    "Creation : {0} - Failed.", jdbc_connection_pool_id));
            subReport.setActionExitCode(ExitCode.FAILURE);
        } else {
            subReport.setMessage(I18N.getLocalString("ping.create.jdbc.connection.pool.success",
                    "\nAttempting to ping during JDBC Connection Pool " +
                    "Creation : {0} - Succeeded.", jdbc_connection_pool_id));
        }
    }

    private void createPool(ActionReport report) {
        ResourceAttributes attrList = new ResourceAttributes();
        attrList.set(ResourceConstants.CONNECTION_POOL_NAME, jdbc_connection_pool_id);
        attrList.set(ResourceConstants.DATASOURCE_CLASS, datasourceclassname);
        attrList.set(ServerTags.DESCRIPTION, description);
        attrList.set(ResourceConstants.RES_TYPE, restype);
        attrList.set(ResourceConstants.STEADY_POOL_SIZE, steadypoolsize);
        attrList.set(ResourceConstants.MAX_POOL_SIZE, maxpoolsize);
        attrList.set(ResourceConstants.MAX_WAIT_TIME_IN_MILLIS, maxwait);
        attrList.set(ResourceConstants.POOL_SIZE_QUANTITY, poolresize);
        attrList.set(ResourceConstants.INIT_SQL, initsql);
        attrList.set(ResourceConstants.IDLE_TIME_OUT_IN_SECONDS, idletimeout);
        attrList.set(ResourceConstants.TRANS_ISOLATION_LEVEL, isolationlevel);
        attrList.set(ResourceConstants.IS_ISOLATION_LEVEL_GUARANTEED, isisolationguaranteed.toString());
        attrList.set(ResourceConstants.IS_CONNECTION_VALIDATION_REQUIRED, isconnectvalidatereq.toString());
        attrList.set(ResourceConstants.CONNECTION_VALIDATION_METHOD, validationmethod);
        attrList.set(ResourceConstants.VALIDATION_TABLE_NAME, validationtable);
        attrList.set(ResourceConstants.CONN_FAIL_ALL_CONNECTIONS, failconnection.toString());
        attrList.set(ResourceConstants.NON_TRANSACTIONAL_CONNECTIONS, nontransactionalconnections.toString());
        attrList.set(ResourceConstants.ALLOW_NON_COMPONENT_CALLERS, allownoncomponentcallers.toString());
        attrList.set(ResourceConstants.VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS, validateatmostonceperiod);
        attrList.set(ResourceConstants.CONNECTION_LEAK_TIMEOUT_IN_SECONDS, leaktimeout);
        attrList.set(ResourceConstants.CONNECTION_LEAK_RECLAIM, leakreclaim.toString());
        attrList.set(ResourceConstants.CONNECTION_CREATION_RETRY_ATTEMPTS, creationretryattempts);
        attrList.set(ResourceConstants.CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS, creationretryinterval);
        attrList.set(ResourceConstants.DRIVER_CLASSNAME, driverclassname);
        attrList.set(ResourceConstants.SQL_TRACE_LISTENERS, sqltracelisteners);
        attrList.set(ResourceConstants.STATEMENT_TIMEOUT_IN_SECONDS, statementtimeout);
        attrList.set(ResourceConstants.STATEMENT_LEAK_TIMEOUT_IN_SECONDS, statementLeaktimeout);
        attrList.set(ResourceConstants.STATEMENT_LEAK_RECLAIM, statementLeakreclaim.toString());
        attrList.set(ResourceConstants.STATEMENT_CACHE_SIZE, statementcachesize);
        attrList.set(ResourceConstants.LAZY_CONNECTION_ASSOCIATION, lazyconnectionassociation.toString());
        attrList.set(ResourceConstants.LAZY_CONNECTION_ENLISTMENT, lazyconnectionenlistment.toString());
        attrList.set(ResourceConstants.ASSOCIATE_WITH_THREAD, associatewiththread.toString());
        attrList.set(ResourceConstants.MATCH_CONNECTIONS, matchconnections.toString());
        attrList.set(ResourceConstants.MAX_CONNECTION_USAGE_COUNT, maxconnectionusagecount);
        attrList.set(ResourceConstants.PING, ping.toString());
        attrList.set(ResourceConstants.POOLING, pooling.toString());
        attrList.set(ResourceConstants.VALIDATION_CLASSNAME, validationclassname);
        attrList.set(ResourceConstants.WRAP_JDBC_OBJECTS, wrapjdbcobjects.toString());
        try {
            JDBCConnectionPoolManager connPoolMgr = new JDBCConnectionPoolManager();
            ResourceStatus result = connPoolMgr.create(domain.getResources(), attrList, properties, target);
            if (result.getStatus() == ResourceStatus.FAILURE) {
                report.setMessage(I18N.getLocalString("create.jdbc.connection.pool.fail",
                    "JDBC connection pool {0} creation failed", jdbc_connection_pool_id));
                report.setFailureCause(result.getException());
                report.setActionExitCode(ExitCode.FAILURE);
            } else {
                report.setMessage(result.getMessage());
            }
        } catch(Exception e) {
            report.setMessage(I18N.getLocalString("create.jdbc.connection.pool.fail",
                "JDBC connection pool: {0} could not be created, reason: {1}",
                jdbc_connection_pool_id, e.getMessage()));
            report.setActionExitCode(ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }


    private void createResources(ActionReport report) {
        for (String resourceName : resourceNames) {
            ResourceAttributes attributes = new ResourceAttributes();
            attributes.set(JNDI_NAME, resourceName);
            attributes.set(POOL_NAME, jdbc_connection_pool_id);
            attributes.set(DESCRIPTION, description);
            attributes.set(ENABLED, "true");
            final Resources resources = domain.getResources();
            try {
                ResourceStatus result = jdbcResourceManager.create(resources, attributes, new Properties(), target);
                toSubReport(result, report);
            } catch (Exception e) {
                report.appendMessage(e.getMessage());
                report.setActionExitCode(ExitCode.FAILURE);
                report.setFailureCause(e);
            }
        }
    }

    private void execSqlFile(final String resourceName, final ActionReport report) {
        try {
            ResourceStatus execStatus = jdbcResourceManager.executeSql(resourceName, sqlFileName);
            toSubReport(execStatus, report);
        } catch (Exception e) {
            report.appendMessage("SQL execution of file " + sqlFileName + " failed for jdbc resource "
                + resourceName + " using pool " + jdbc_connection_pool_id + ", due to " + e.getMessage());
            report.setActionExitCode(ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    private void toSubReport(ResourceStatus result, ActionReport report) {
        ActionReport subReport = report.addSubActionsReport();
        subReport.setMessage(result.getMessage());
        if (result.getStatus() == FAILURE) {
            subReport.setActionExitCode(ExitCode.FAILURE);
            subReport.setFailureCause(result.getException());
            report.setActionExitCode(ExitCode.FAILURE);
        } else {
            subReport.setActionExitCode(ExitCode.SUCCESS);
        }
    }
}
