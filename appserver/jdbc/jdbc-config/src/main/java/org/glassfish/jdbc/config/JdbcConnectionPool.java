/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jdbc.config;

import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.admin.cli.resources.UniqueResourceNameConstraint;
import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.config.support.datatypes.Port;
import org.glassfish.connectors.config.validators.ConnectionPoolErrorMessages;
import org.glassfish.jdbc.config.validators.JdbcConnectionPoolConstraint;
import org.glassfish.jdbc.config.validators.JdbcConnectionPoolConstraints;
import org.glassfish.resourcebase.resources.ResourceDeploymentOrder;
import org.glassfish.resourcebase.resources.ResourceTypeOrder;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Defines configuration used to create and manage a pool physical database
 * connections. Pool definition is named, and can be referred to by multiple
 * {@code jdbc-resource} elements (See {@literal <jdbc-resource>}).
 *
 * <p>Each named pool definition results in a pool instantiated at server start-up.
 *
 * <p>Pool is populated when accessed for the first time. If two or more
 * {@code jdbc-resource} elements point to the same {@code jdbc-connection-pool} element,
 * they are using the same pool of connections, at run time.
 */
@Configured
@JdbcConnectionPoolConstraints ({
        @JdbcConnectionPoolConstraint(value = ConnectionPoolErrorMessages.MAX_STEADY_INVALID),
        @JdbcConnectionPoolConstraint(value = ConnectionPoolErrorMessages.STMT_WRAPPING_DISABLED),
        @JdbcConnectionPoolConstraint(value = ConnectionPoolErrorMessages.RES_TYPE_MANDATORY),
        @JdbcConnectionPoolConstraint(value = ConnectionPoolErrorMessages.TABLE_NAME_MANDATORY),
        @JdbcConnectionPoolConstraint(value = ConnectionPoolErrorMessages.CUSTOM_VALIDATION_CLASS_NAME_MANDATORY)
})
@RestRedirects({
        @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-jdbc-connection-pool"),
        @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-jdbc-connection-pool")
})
@ResourceTypeOrder(deploymentOrder = ResourceDeploymentOrder.JDBC_POOL)
@UniqueResourceNameConstraint(message ="{resourcename.isnot.unique}", payload = JdbcConnectionPool.class)
public interface JdbcConnectionPool extends ConfigBeanProxy, Resource, ResourcePool, PropertyBag {

    String CONNECTION_VALIDATION_METHODS = "(auto-commit|meta-data|custom-validation|table)";

    String ISOLATION_LEVELS = "(read-uncommitted|read-committed|repeatable-read|serializable)";

    String RESOURCE_TYPES = "(java.sql.Driver|javax.sql.DataSource|javax.sql.XADataSource|javax.sql.ConnectionPoolDataSource)";

    /**
     * Gets the value of the {@code datasourceClassname} property.
     *
     * <p>Name of the vendor supplied JDBC datasource resource manager.
     *
     * <p>An XA or global transactions capable datasource class will implement
     * {@link javax.sql.XADataSource} interface. Non XA or Local transactions only
     * datasources will implement {@link javax.sql.DataSource} interface.
     *
     @return possible object is {@link String}
     */
    @Attribute
    String getDatasourceClassname();

    /**
     * Sets the value of the {@code datasourceClassname} property.
     *
     * @param datasourceClassname allowed object is {@link String}
     */
    void setDatasourceClassname(String datasourceClassname) throws PropertyVetoException;

    /**
     * Gets the value of the {@code driverClassname} property.
     *
     * <p>Name of the vendor supplied JDBC driver resource manager.
     *
     * <p>Get classnames that implement {@link java.sql.Driver}.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getDriverClassname();

    /**
     * Sets the value of the {@code driverClassname} property.
     *
     * @param driverClassname allowed object is {@link String}
     */
    void setDriverClassname(String driverClassname) throws PropertyVetoException;

    /**
     * Gets the value of the {@code resType} property.
     *
     * <p>DataSource implementation class could implement one of
     * {@link javax.sql.DataSource}, {@link javax.sql.XADataSource} or
     * {@link javax.sql.ConnectionPoolDataSource} interfaces.
     *
     * <p>This optional attribute must be specified to disambiguate when a Datasource
     * class implements two or more of these interfaces. An error is produced when
     * this attribute has a legal value and the indicated interface is not implemented by
     * the datasource class.
     *
     * <p>This attribute has no default value.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @Pattern(regexp = RESOURCE_TYPES, message = "Valid values: " + RESOURCE_TYPES)
    String getResType();

    /**
     * Sets the value of the {@code resType} property.
     *
     * @param resType allowed object is {@link String}
     */
    void setResType(String resType) throws PropertyVetoException;

    /**
     * Gets the value of the {@code steadyPoolSize} property.
     *
     * <p>Minimum and initial number of connections maintained in the pool.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "8", dataType = Integer.class)
    @Min(value = 0)
    @Max(value = Integer.MAX_VALUE)
    String getSteadyPoolSize();

    /**
     * Sets the value of the {@code steadyPoolSize} property.
     *
     * @param poolSize allowed object is {@link String}
     */
    void setSteadyPoolSize(String poolSize) throws PropertyVetoException;

    /**
     * Gets the value of the {@code maxPoolSize} property.
     *
     * <p>Maximum number of connections that can be created.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "32", dataType = Integer.class)
    @Min(value = 1)
    @Max(value = Integer.MAX_VALUE)
    String getMaxPoolSize();

    /**
     * Sets the value of the {@code maxPoolSize} property.
     *
     * @param maxPoolSize allowed object is {@link String}
     */
    void setMaxPoolSize(String maxPoolSize) throws PropertyVetoException;

    /**
     * Gets the value of the {@code maxWaitTimeInMillis} property.
     *
     * <p>Amount of time the caller will wait before getting a connection timeout.
     *
     * <p>Default is {@code 60} sec. A value of {@code 0} will force caller to wait indefinitely.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "60000", dataType = Integer.class)
    @Min(value = 0)
    @Max(value = Integer.MAX_VALUE)
    String getMaxWaitTimeInMillis();

    /**
     * Sets the value of the {@code maxWaitTimeInMillis} property.
     *
     * @param maxWaitTime allowed object is {@link String}
     */
    void setMaxWaitTimeInMillis(String maxWaitTime) throws PropertyVetoException;

    /**
     * Gets the value of the {@code poolResizeQuantity} property.
     *
     * <p>Number of connections to be removed when {@code idle-timeout-in-seconds}
     * timer expires. Connections that have idled for longer than the timeout are
     * candidates for removal. When the pool size reaches {@code steady-pool-size},
     * the connection removal stops.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "2", dataType = Integer.class)
    @Min(value = 1)
    @Max(value = Integer.MAX_VALUE)
    String getPoolResizeQuantity();

    /**
     * Sets the value of the {@code poolResizeQuantity} property.
     *
     * @param resizeQuantity allowed object is {@link String}
     */
    void setPoolResizeQuantity(String resizeQuantity) throws PropertyVetoException;

    /**
     * Gets the value of the {@code idleTimeoutInSeconds} property.
     *
     * <p>Maximum time in seconds, that a connection can remain idle in the pool.
     * After this time, the pool implementation can close this connection.
     * Note that this does not control connection timeouts enforced at the
     * database server side. Administrators are advised to keep this timeout
     * shorter than the database server side timeout (if such timeouts are
     * configured on the specific vendor's database), to prevent accumulation of
     * unusable connection in Application Server.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "300", dataType = Integer.class)
    @Min(value = 0)
    @Max(value = Integer.MAX_VALUE)
    String getIdleTimeoutInSeconds();

    /**
     * Sets the value of the {@code idleTimeoutInSeconds} property.
     *
     * @param idleTimeout allowed object is {@link String}
     */
    void setIdleTimeoutInSeconds(String idleTimeout) throws PropertyVetoException;

    /**
     * Gets the value of the {@code transactionIsolationLevel} property.
     *
     * <p>Specifies the Transaction Isolation Level on pooled database connections.
     *
     * <p>Optional.
     *
     * <p>Has no default. If left unspecified the pool operates with
     * default isolation level provided by the JDBC Driver. A desired isolation
     * level can be set using one of the standard transaction isolation levels,
     * which see.
     *
     * <p>Applications that change the Isolation level on a pooled connection
     * programmatically, risk polluting the pool and this could lead to program
     * errors.
     *
     * @see "is-isolation-level-guaranteed"
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @Pattern(regexp = ISOLATION_LEVELS, message = "Valid values: " + ISOLATION_LEVELS)
    String getTransactionIsolationLevel();

    /**
     * Sets the value of the {@code transactionIsolationLevel} property.
     *
     * @param isolationLevel allowed object is {@link String}
     */
    void setTransactionIsolationLevel(String isolationLevel) throws PropertyVetoException;

    /**
     * Gets the value of the {@code isIsolationLevelGuaranteed} property.
     *
     * <p>Applicable only when a particular isolation level is specified for
     * {@code transaction-isolation-level}.
     *
     * <p>The default value is {@code true}. This assures that every time a connection
     * is obtained from the pool, it is guaranteed to have the isolation set to the
     * desired value.
     *
     * <p>This could have some performance impact on some JDBC drivers. Can be set
     * to {@code false} by that administrator when they are certain that the application
     * does not change the isolation level before returning the connection.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getIsIsolationLevelGuaranteed();

    /**
     * Sets the value of the {@code isIsolationLevelGuaranteed} property.
     *
     * @param isGuaranteed allowed object is {@link String}
     */
    void setIsIsolationLevelGuaranteed(String isGuaranteed) throws PropertyVetoException;

    /**
     * Gets the value of the {@code isConnectionValidationRequired} property.
     *
     * <p>If {@code true}, connections are validated (checked to find out if they are
     * usable) before giving out to the application.
     *
     * <p>The default is {@code false}.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "false", dataType = Boolean.class)
    String getIsConnectionValidationRequired();

    /**
     * Sets the value of the {@code isConnectionValidationRequired} property.
     *
     * @param isValidationRequired allowed object is {@link String}
     */
    void setIsConnectionValidationRequired(String isValidationRequired) throws PropertyVetoException;

    /**
     * Gets the value of the {@code connectionValidationMethod} property.
     *
     * <p>Specifies the type of validation to be performed when
     * {@code is-connection-validation-required} is {@code true}. The following types of
     * validation are supported:
     * <ul>
     * <li>{@code auto-commit} using connection.autoCommit()</li>
     * <li>{@code meta-data} using connection.getMetaData()</li>
     * <li>{@code table} performing a query on a user specified table(see validation-table-name)</li>
     * </ul>
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "table")
    @Pattern(regexp = CONNECTION_VALIDATION_METHODS, message = "Valid values: " + CONNECTION_VALIDATION_METHODS)
    String getConnectionValidationMethod();

    /**
     * Sets the value of the {@code connectionValidationMethod} property.
     *
     * @param validationMethod allowed object is {@link String}
     */
    void setConnectionValidationMethod(String validationMethod) throws PropertyVetoException;

    /**
     * Gets the value of the {@code validationTableName} property.
     *
     * <p>Specifies the table name to be used to perform a query to validate a
     * connection.
     *
     * <p>This parameter is mandatory, if {@code connection-validation-type} is
     * set to {@code table}.
     *
     * <p>Verification by accessing a user specified table may become
     * necessary for connection validation, particularly if database driver
     * caches calls to {@code setAutoCommit()} and {@code getMetaData()}.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getValidationTableName();

    /**
     * Sets the value of the {@code validationTableName} property.
     *
     * @param tableName allowed object is {@link String}
     */
    void setValidationTableName(String tableName) throws PropertyVetoException;

    /**
     * Gets the value of the {@code validationClassName} property.
     *
     * <p>Specifies the custom validation class name to be used to perform
     * connection validation.
     *
     * <p>This parameter is mandatory, if {@code connection-validation-type} is
     * set to {@code custom-validation}.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getValidationClassname();

    /**
     * Sets the value of the {@code validationClassName} property.
     *
     * @param classname allowed object is {@link String}
     */
    void setValidationClassname(String classname) throws PropertyVetoException;

    /**
     * Gets the value of the {@code failAllConnections} property.
     *
     * <p>Indicates if all connections in the pool must be closed should a single
     * validation check fail.
     *
     * <p>The default is {@code false}.
     *
     * <p>One attempt will be made to re-establish failed connections.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "false", dataType = Boolean.class)
    String getFailAllConnections();

    /**
     * Sets the value of the {@code failAllConnections} property.
     *
     * @param failAllConnections allowed object is {@link String}
     */
    void setFailAllConnections(String failAllConnections) throws PropertyVetoException;

    /**
     * Gets the value of the {@code nonTransactionalConnections} property.
     *
     * <p>A pool with this property set to {@code true} returns non-transactional
     * connections. This connection does not get automatically enlisted
     * with the transaction manager.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "false", dataType = Boolean.class)
    String getNonTransactionalConnections();

    /**
     * Sets the value of the {@code nonTransactionalConnections} property.
     *
     * @param nonTransactional allowed object is {@link String}
     */
    void setNonTransactionalConnections(String nonTransactional) throws PropertyVetoException;

    /**
     * Gets the value of the {@code allowNonComponentCallers} property.
     *
     * <p>A pool with this property set to {@code true}, can be used by non-J2EE components
     * (i.e components other than EJBs or Servlets). The returned connection is
     * enlisted automatically with the transaction context obtained from the
     * transaction manager. This property is to enable the pool to be used by
     * non-component callers such as ServletFilters, Lifecycle modules, and
     * 3rd party persistence managers. Standard J2EE components can continue to
     * use such pools. Connections obtained by non-component callers are not
     * automatically cleaned at the end of a transaction by the container. They
     * need to be explicitly closed by the caller.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "false", dataType = Boolean.class)
    String getAllowNonComponentCallers();

    /**
     * Sets the value of the {@code allowNonComponentCallers} property.
     *
     * @param allowNonComponentCallers allowed object is {@link String}
     */
    void setAllowNonComponentCallers(String allowNonComponentCallers) throws PropertyVetoException;

    /**
     * Gets the value of the {@code validateAtmostOncePeriodInSeconds} property.
     *
     * <p>Used to set the {@code time-interval} within which a connection is validated
     * atmost once.
     *
     * <p>Default is {@code 0} which implies that it is  not enabled.
     *
     * <p><strong>TBD:</strong> Documentation is to be corrected.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "0", dataType = Integer.class)
    @Min(value = 0)
    @Max(value = Integer.MAX_VALUE)
    String getValidateAtmostOncePeriodInSeconds();

    /**
     * Sets the value of the {@code validateAtmostOncePeriodInSeconds} property.
     *
     * @param validatePeriod allowed object is {@link String}
     */
    void setValidateAtmostOncePeriodInSeconds(String validatePeriod) throws PropertyVetoException;

    /**
     * Gets the value of the {@code connectionLeakTimeoutInSeconds} property.
     *
     * <p>To aid user in detecting potential connection leaks by the application.
     *
     * <p>When a connection is not returned back to the pool by the application
     * within the specified period, it is assumed to be a potential leak and
     * stack trace of the caller will be logged.
     *
     * <p>Default is 0, which implies there is no leak detection, by default.
     *
     * <p>A positive non-zero value turns on leak detection. Note however that,
     * this attribute only detects if there is a connection leak. The connection
     * can be reclaimed only if {@code connection-leak-reclaim} is set to true.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "0", dataType = Integer.class)
    @Min(value = 0)
    @Max(value = Integer.MAX_VALUE)
    String getConnectionLeakTimeoutInSeconds();

    /**
     * Sets the value of the {@code connectionLeakTimeoutInSeconds} property.
     *
     * @param leakTimeout allowed object is {@link String}
     */
    void setConnectionLeakTimeoutInSeconds(String leakTimeout) throws PropertyVetoException;

    /**
     * Gets the value of the {@code connectionLeakReclaim} property.
     *
     * <p>If enabled, connection will be reusable (put back into pool) after
     * connection-leak-timeout-in-seconds occurs.
     *
     * <p>Default value is {@code false}.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "false", dataType = Boolean.class)
    String getConnectionLeakReclaim();

    /**
     * Sets the value of the {@code connectionLeakReclaim} property.
     *
     * @param leakReclaim allowed object is {@link String}
     */
    void setConnectionLeakReclaim(String leakReclaim) throws PropertyVetoException;

    /**
     * Gets the value of the {@code connectionCreationRetryAttempts} property.
     *
     * <p>The number of attempts to create a new connection.
     *
     * <p>Default is {@code 0}, which implies no retries.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "0", dataType = Integer.class)
    @Min(value = 0)
    @Max(value = Integer.MAX_VALUE)
    String getConnectionCreationRetryAttempts();

    /**
     * Sets the value of the {@code connectionCreationRetryAttempts} property.
     *
     * @param creationAttempts allowed object is {@link String}
     */
    void setConnectionCreationRetryAttempts(String creationAttempts) throws PropertyVetoException;

    /**
     * Gets the value of the {@code connectionCreationRetryIntervalInSeconds} property.
     *
     * <p>The time interval between retries while attempting to create a connection
     *
     * <p>Default is {@code 10} seconds.
     *
     * <p>Effective when {@code connection-creation-retry-attempts} is greater than {@code 0}.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "10", dataType = Integer.class)
    @Min(value = 0)
    @Max(value = Integer.MAX_VALUE)
    String getConnectionCreationRetryIntervalInSeconds();

    /**
     * Sets the value of the {@code connectionCreationRetryIntervalInSeconds} property.
     *
     * @param retryInterval allowed object is {@link String}
     */
    void setConnectionCreationRetryIntervalInSeconds(String retryInterval) throws PropertyVetoException;

    /**
     * Gets the value of the {@code statementTimeoutInSeconds} property.
     *
     * <p>Sets the timeout property of a connection to enable termination of
     * abnormally long running queries.
     *
     * <p>Default value of {@code -1} implies that it is not enabled.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "-1", dataType = Integer.class)
    @Min(value = -1)
    String getStatementTimeoutInSeconds();

    /**
     * Sets the value of the {@code statementTimeoutInSeconds} property.
     *
     * @param statementTimeout allowed object is {@link String}
     */
    void setStatementTimeoutInSeconds(String statementTimeout) throws PropertyVetoException;

    /**
     * Gets the value of the {@code lazyConnectionEnlistment} property.
     *
     * <p>Enlist a resource to the transaction only when it is actually used in a
     * method, which avoids enlistment of connections that are not used in a
     * transaction. This also prevents unnecessary enlistment of connections
     * cached in the calling components.
     *
     * <p>Default value is {@code false}.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "false", dataType = Boolean.class)
    String getLazyConnectionEnlistment();

    /**
     * Sets the value of the {@code lazyConnectionEnlistment} property.
     *
     * @param lazyEnlistment allowed object is {@link String}
     */
    void setLazyConnectionEnlistment(String lazyEnlistment) throws PropertyVetoException;

    /**
     * Gets the value of the {@code lazyConnectionAssociation} property.
     *
     * <p>Connections are lazily associated when an operation is performed on them.
     * Also, they are disassociated when the transaction is completed and a
     * component method ends, which helps reuse of the physical connections.
     *
     * <p>Default value is {@code false}.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "false", dataType = Boolean.class)
    String getLazyConnectionAssociation();

    /**
     * Sets the value of the {@code lazyConnectionAssociation} property.
     *
     * @param lazyAssociation allowed object is {@link String}
     */
    void setLazyConnectionAssociation(String lazyAssociation) throws PropertyVetoException;

    /**
     * Gets the value of the {@code associateWithThread} property.
     *
     * <p>Associate a connection with the thread such that when the same thread is
     * in need of a connection, it can reuse the connection already associated
     * with that thread, thereby not incurring the overhead of getting a
     * connection from the pool.
     *
     * <p>Default value is {@code false}.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "false", dataType = Boolean.class)
    String getAssociateWithThread();

    /**
     * Sets the value of the {@code associateWithThread} property.
     *
     * @param associateWithThread allowed object is {@link String}
     */
    void setAssociateWithThread(String associateWithThread) throws PropertyVetoException;

    /**
     * Gets the value of the {@code pooling} property.
     *
     * <p>Property to disable pooling for the pool.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "true", dataType = Boolean.class)
    String getPooling();

    /**
     * Sets the value of the {@code pooling} property.
     *
     * @param pooling allowed object is {@link String}
     */
    void setPooling(String pooling) throws PropertyVetoException;

    /**
     * Gets the value of the {@code statementCacheSize} property.
     *
     * <p>When specified, statement caching is turned on to cache statements,
     * prepared statements, callable statements that are repeatedly executed by
     * applications.
     *
     * <p>Default value is {@code 0}, which implies the feature is not enabled.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "0", dataType = Integer.class)
    @Min(value = 0)
    @Max(value = Integer.MAX_VALUE)
    String getStatementCacheSize();

    /**
     * Sets the value of the {@code statementCacheSize} property.
     *
     * @param cacheSize allowed object is {@link String}
     */
    void setStatementCacheSize(String cacheSize) throws PropertyVetoException;

    /**
     * Gets the value of the {@code statementCacheType} property.
     *
     * <p>When specified, statement caching type is set to cache statements,
     * prepared statements, callable statements that are repeatedly executed by
     * applications.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "")
    String getStatementCacheType();

    /**
     * Sets the value of the {@code statementCacheType} property.
     *
     * @param cacheType allowed object is {@link String}
     */
    void setStatementCacheType(String cacheType) throws PropertyVetoException;

    /**
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "0", dataType = Integer.class)
    @Min(value = 0)
    @Max(value = Integer.MAX_VALUE)
    String getStatementLeakTimeoutInSeconds();

    /**
     * Sets the value of the {@code statementLeakTimeoutInSeconds} property.
     *
     * @param leakTimeout allowed object is {@link String}
     */
    void setStatementLeakTimeoutInSeconds(String leakTimeout) throws PropertyVetoException;

    @Attribute (defaultValue = "false", dataType = Boolean.class)
    String getStatementLeakReclaim();

    /**
     * Sets the value of the {@code statementLeakReclaim} property.
     *
     * @param leakReclaim allowed object is {@link String}
     */
    void setStatementLeakReclaim(String leakReclaim) throws PropertyVetoException;

    /**
     * Gets the value of the {@code initSql} property.
     *
     * <p>Init sql is executed whenever a connection created from the pool.
     *
     * <p>This is mostly useful when the state of a connection is to be initialized.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getInitSql();

    /**
     * Sets the value of the {@code initSql} property.
     *
     * @param initSql allowed object is {@link String}
     */
    void setInitSql(String initSql) throws PropertyVetoException;

    /**
     * Gets the value of the {@code matchConnections} property.
     *
     * <p>To switch on/off connection matching for the pool.
     *
     * <p>It can be set to {@code false} if the administrator knows that
     * the connections in the pool will always be homogeneous and hence a
     * connection picked from the pool need not be matched by the resource adapter.
     *
     * <p>Default value is {@code false}.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "false", dataType = Boolean.class)
    String getMatchConnections();

    /**
     * Sets the value of the {@code matchConnections} property.
     *
     * @param matchConnections allowed object is {@link String}
     */
    void setMatchConnections(String matchConnections) throws PropertyVetoException;

    /**
     * Gets the value of the {@code maxConnectionUsageCount} property.
     *
     * <p>When specified, connections will be re-used by the pool for the specified
     * number of times after which it will be closed. This is useful for
     * instance, to avoid statement-leaks.
     *
     * <p>Default value is {@code 0}, which implies the
     * feature is not enabled.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "0", dataType = Integer.class)
    @Min(value = 0)
    @Max(value = Integer.MAX_VALUE)
    String getMaxConnectionUsageCount();

    /**
     * Sets the value of the {@code maxConnectionUsageCount} property.
     *
     * @param maxUsageCount allowed object is {@link String}
     */
    void setMaxConnectionUsageCount(String maxUsageCount) throws PropertyVetoException;

    /**
     * Gets the value of the {@code wrapJdbcObjects} property.
     *
     * <p>When set to {@code true}, application will get wrapped jdbc objects for
     * {@link java.sql.Statement}, {@link java.sql.PreparedStatement},
     * {@link java.sql.CallableStatement}, {@link java.sql.ResultSet},
     * {@link java.sql.DatabaseMetaData}.
     *
     * <p>Defaults to {@code false}.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "true", dataType = Boolean.class)
    String getWrapJdbcObjects();

    /**
     * Sets the value of the {@code wrapJdbcObjects} property.
     *
     * @param wrapJdbcObjects allowed object is {@link String}
     */
    void setWrapJdbcObjects(String wrapJdbcObjects) throws PropertyVetoException;

    /**
     * Gets the value of the {@code sqlTraceListeners} property.
     *
     * <p>Comma separated list of SQL trace listener implementations to be used to
     * trace the SQL statements executed by the applications.
     *
     * <p>The default logger used by the system logs the SQL statements based on a set of
     * values stored in {@code SQLTraceRecord} object.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getSqlTraceListeners();

    /**
     * Sets the value of the {@code sqlTraceListeners} property.
     *
     * @param traceListeners allowed object is {@link String}
     */
    void setSqlTraceListeners(String traceListeners) throws PropertyVetoException;

    /**
     * Gets the value of the {@code description} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getDescription();

    /**
     * Sets the value of the {@code description} property.
     *
     * @param description allowed object is {@link String}
     */
    void setDescription(String description) throws PropertyVetoException;

    /**
     * Properties.
     *
     * <p>This list is likely incomplete as of 21 October 2008.
     *
     * <p>Most JDBC 2.0 drivers permit use of standard property lists, to specify
     * User, Password and other resource configuration. While these are optional
     * properties, according to the specification, several of these properties
     * may be necessary for most databases. See Section 5.3 of JDBC 2.0 Standard
     * Extension API.
     *
     * <p>The following are the names and corresponding values for these properties:
     * <ul>
     * <li>{@code databaseName} name of the Database</li>
     * <li>{@code serverName} database Server name</li>
     * <li>{@code port} port where a Database server is listening for requests</li>
     * <li>{@code networkProtocol} communication Protocol used</li>
     * <li>{@code user}
     *      default name of the database user with which connections
     *      will be stablished. Programmatic database authentication
     *      or default-resource-principal specified in vendor
     *      specific web and ejb deployment descriptors will take
     *      precedence, over this default. The details and caveats
     *      are described in detail in the Administrator's guide</li>
     * <li>{@code password} password for default database user</li>
     * <li>{@code roleName} the initial SQL role name</li>
     * <li>{@code datasourceName}
     *      used to name an underlying XADataSource, or ConnectionPoolDataSource
     *      when pooling of connections is done</li>
     * <li>{@code description} textual Description
     *</ul>
     *
     * <p>When one or more of these properties are specified, they are passed as
     * is using {@literal set<Name>(<Value>)} methods to the vendors {@code Datasource}
     * class (specified in {@code datasource-classname}). {@code User} and {@code Password}
     * properties are used as default principal, if Container Managed authentication is
     * specified and a {@code default-resource-principal} is not found in application
     * deployment descriptors.
     */
@Override
@PropertiesDesc(props = {
        @PropertyDesc(
                name = "PortNumber",
                defaultValue = "1527",
                dataType = Port.class,
                description = "Port on which the database server listens for requests"
        ),
        @PropertyDesc(
                name = "Password",
                defaultValue = "APP",
                description = "Password for connecting to the database"
        ),
        @PropertyDesc(
                name = "User",
                defaultValue = "APP",
                description = "User name for connecting to the database"
        ),
        @PropertyDesc(
                name = "serverName",
                defaultValue = "localhost",
                description = "Database server for this connection pool"
        ),
        @PropertyDesc(
                name = "DatabaseName",
                defaultValue = "sun-appserv-samples",
                description = "Database for this connection pool."
        ),
        @PropertyDesc(
                name = "connectionAttributes",
                defaultValue = ";create=true",
                description = "connection attributes"
        )
    })
    @Element
    List<Property> getProperty();

    @Override
    default String getIdentity() {
        return getName();
    }
}
