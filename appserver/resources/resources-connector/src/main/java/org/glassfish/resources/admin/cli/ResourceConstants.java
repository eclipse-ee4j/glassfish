/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.resources.admin.cli;

/**
 * A constants class housing all the resource related constants
 * @author PRASHANTH ABBAGANI
 */
public final class ResourceConstants {

    //Attribute names constants
    // JDBC Resource
    public static final String JNDI_NAME = "jndi-name";

    public static final String POOL_NAME = "pool-name";

    // JMS Resource
    public static final String RES_TYPE = "res-type";

    public static final String FACTORY_CLASS = "factory-class";

    public static final String ENABLED = "enabled";

    // External JNDI Resource
    public static final String JNDI_LOOKUP = "jndi-lookup-name";

    // JDBC Connection pool
    public static final String CONNECTION_POOL_NAME = "name";

    public static final String STEADY_POOL_SIZE = "steady-pool-size";

    public static final String MAX_POOL_SIZE = "max-pool-size";

    public static final String MAX_WAIT_TIME_IN_MILLIS = "max-wait-time-in-millis";

    public static final String POOL_SIZE_QUANTITY = "pool-resize-quantity";

    public static final String IDLE_TIME_OUT_IN_SECONDS = "idle-timeout-in-seconds";

    public static final String INIT_SQL = "init-sql";

    public static final String IS_CONNECTION_VALIDATION_REQUIRED = "is-connection-validation-required";

    public static final String CONNECTION_VALIDATION_METHOD = "connection-validation-method";

    public static final String CUSTOM_VALIDATION = "custom-validation";

    public static final String FAIL_ALL_CONNECTIONS = "fail-all-connections";

    public static final String VALIDATION_TABLE_NAME = "validation-table-name";

    public static final String DATASOURCE_CLASS = "datasource-classname";

    public static final String TRANS_ISOLATION_LEVEL = "transaction-isolation-level";

    public static final String IS_ISOLATION_LEVEL_GUARANTEED = "is-isolation-level-guaranteed";

    public static final String NON_TRANSACTIONAL_CONNECTIONS = "non-transactional-connections";

    public static final String ALLOW_NON_COMPONENT_CALLERS = "allow-non-component-callers";

    public static final String VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS = "validate-atmost-once-period-in-seconds";

    public static final String CONNECTION_LEAK_TIMEOUT_IN_SECONDS = "connection-leak-timeout-in-seconds";

    public static final String CONNECTION_LEAK_RECLAIM = "connection-leak-reclaim";

    public static final String CONNECTION_CREATION_RETRY_ATTEMPTS = "connection-creation-retry-attempts";

    public static final String CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS = "connection-creation-retry-interval-in-seconds";

    public static final String STATEMENT_TIMEOUT_IN_SECONDS = "statement-timeout-in-seconds";

    public static final String DRIVER_CLASSNAME = "driver-classname";

    public static final String LAZY_CONNECTION_ENLISTMENT = "lazy-connection-enlistment";

    public static final String LAZY_CONNECTION_ASSOCIATION = "lazy-connection-association";

    public static final String ASSOCIATE_WITH_THREAD = "associate-with-thread";

    public static final String ASSOCIATE_WITH_THREAD_CONNECTIONS_COUNT = "associate-with-thread-connections-count";

    public static final String MATCH_CONNECTIONS = "match-connections";

    public static final String MAX_CONNECTION_USAGE_COUNT = "max-connection-usage-count";

    public static final String PING = "ping";

    public static final String POOLING = "pooling";

    public static final String SQL_TRACE_LISTENERS = "sql-trace-listeners";

    public static final String STATEMENT_CACHE_SIZE = "statement-cache-size";

    public static final String VALIDATION_CLASSNAME = "validation-classname";

    public static final String WRAP_JDBC_OBJECTS = "wrap-jdbc-objects";

    public static final String CASCADE = "cascade";

    public static final String STATEMENT_LEAK_TIMEOUT_IN_SECONDS = "statement-leak-timeout-in-seconds";

    public static final String STATEMENT_LEAK_RECLAIM = "statement-leak-reclaim";

    //Mail resource
    public static final String MAIL_HOST = "host";

    public static final String MAIL_USER = "user";

    public static final String MAIL_FROM_ADDRESS = "from";

    public static final String MAIL_STORE_PROTO = "store-protocol";

    public static final String MAIL_STORE_PROTO_CLASS = "store-protocol-class";

    public static final String MAIL_TRANS_PROTO = "transport-protocol";

    public static final String MAIL_TRANS_PROTO_CLASS = "transport-protocol-class";

    public static final String MAIL_DEBUG = "debug";

    //Persistence Manager Factory resource
    public static final String JDBC_RESOURCE_JNDI_NAME = "jdbc-resource-jndi-name";

    //Admin Object resource
    public static final String RES_ADAPTER = "res-adapter";

    public static final String ADMIN_OBJECT_CLASS_NAME = "class-name";

    //Connector resource
    public static final String RESOURCE_TYPE = "resource-type";

    // ConnectorConnection Pool resource ...
    // child elements
    public static final String CONNECTOR_CONN_DESCRIPTION = "description";

    public static final String CONNECTOR_SECURITY_MAP = "security-map";

    public static final String CONNECTOR_PROPERTY = "property";

    //attributes....
    public static final String CONNECTOR_CONNECTION_POOL_NAME = "name";

    public static final String RESOURCE_ADAPTER_CONFIG_NAME = "resource-adapter-name";

    public static final String CONN_DEF_NAME = "connection-definition-name";

    public static final String CONN_STEADY_POOL_SIZE = "steady-pool-size";

    public static final String CONN_MAX_POOL_SIZE = "max-pool-size";

    public static final String CONN_POOL_RESIZE_QUANTITY = "pool-resize-quantity";

    public static final String CONN_IDLE_TIME_OUT = "idle-timeout-in-seconds";

    public static final String CONN_FAIL_ALL_CONNECTIONS = "fail-all-connections";

    public static final String CONN_TRANSACTION_SUPPORT = "transaction-support";

    //Security Map elements...
    public static final String SECURITY_MAP = "security-map";

    public static final String SECURITY_MAP_NAME = "name";

    public static final String SECURITY_MAP_PRINCIPAL = "principal";

    public static final String SECURITY_MAP_USER_GROUP = "user-group";

    public static final String SECURITY_MAP_BACKEND_PRINCIPAL = "backend-principal";

    //Resource -Adapter config attributes.
    public static final String RES_ADAPTER_CONFIG = "resource-adapter-config";

    public static final String THREAD_POOL_IDS = "thread-pool-ids";

    public static final String RES_ADAPTER_NAME = "resource-adapter-name";

    //Backend Principal elements....
    public static final String USER_NAME = "user-name";

    public static final String PASSWORD = "password";

    //work security map elements.
    public static final String WORK_SECURITY_MAP = "work-security-map";

    public static final String WORK_SECURITY_MAP_NAME = "name";

    public static final String WORK_SECURITY_MAP_RA_NAME = "resource-adapter-name";

    public static final String WORK_SECURITY_MAP_GROUP_MAP = "group-map";

    public static final String WORK_SECURITY_MAP_PRINCIPAL_MAP = "principal-map";

    //work security map - group-map elements ..
    public static final String WORK_SECURITY_MAP_EIS_GROUP = "eis-group";

    public static final String WORK_SECURITY_MAP_MAPPED_GROUP = "mapped-group";

    //work security map - eis-map elements ..
    public static final String WORK_SECURITY_MAP_EIS_PRINCIPAL = "eis-principal";

    public static final String WORK_SECURITY_MAP_MAPPED_PRINCIPAL = "mapped-principal";

    // concurrent resource objects
    public static final String CONTEXT_INFO = "context-info";
    public static final String CONTEXT_INFO_DEFAULT_VALUE = "Classloader,JNDI,Security,WorkArea";
    public static final String CONTEXT_INFO_ENABLED = "context-info-enabled";
    public static final String THREAD_PRIORITY = "thread-priority";
    public static final String LONG_RUNNING_TASKS = "long-runnings-tasks";
    public static final String HUNG_AFTER_SECONDS = "hung-after-seconds";
    public static final String HUNG_LOGGER_INITIAL_DELAY_SECONDS = "hung-logger-initial-delay-seconds";
    public static final String HUNG_LOGGER_INTERVAL_SECONDS = "hung-logger-interval-seconds";
    public static final String CORE_POOL_SIZE = "core-pool-size";
    public static final String MAXIMUM_POOL_SIZE = "maximum-pool-size";
    public static final String KEEP_ALIVE_SECONDS = "keep-alive-seconds";
    public static final String THREAD_LIFETIME_SECONDS = "thread-lifetime-seconds";
    public static final String TASK_QUEUE_CAPACITY = "task-queue-capacity";

    public static final String SYSTEM_ALL_REQ = "system-all-req";
}
