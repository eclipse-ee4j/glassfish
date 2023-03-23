/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.xml;

/**
 * I hold the tag names of the runtime binding information of a Jakarta EE application.
 *
 * @author Danny Coward
 */
public interface RuntimeTagNames extends TagNames {

    String S1AS_EJB_RUNTIME_TAG = "sun-ejb-jar";
    String S1AS_APPCLIENT_RUNTIME_TAG = "sun-application-client";
    String S1AS_APPLICATION_RUNTIME_TAG = "sun-application";
    String S1AS_WEB_RUNTIME_TAG = "sun-web-app";
    String S1AS_CONNECTOR_RUNTIME_TAG = "sun-connector";

    String WLS_EJB_RUNTIME_TAG = "weblogic-ejb-jar";
    String WLS_APPCLIENT_RUNTIME_TAG = "weblogic-application-client";
    String WLS_APPLICATION_RUNTIME_TAG = "weblogic-application";
    String WLS_WEB_RUNTIME_TAG = "weblogic-web-app";
    String WLS_CONNECTOR_RUNTIME_TAG = "weblogic-connector";

    String GF_EJB_RUNTIME_TAG = "glassfish-ejb-jar";
    String GF_APPCLIENT_RUNTIME_TAG = "glassfish-application-client";
    String GF_APPLICATION_RUNTIME_TAG = "glassfish-application";
    String GF_WEB_RUNTIME_TAG = "glassfish-web-app";

    String AS_CONTEXT = "as-context";
    String AUTH_METHOD = "auth-method";
    String CALLER_PROPAGATION = "caller-propagation";
    String CONFIDENTIALITY = "confidentiality";
    String DURABLE_SUBSCRIPTION = "jms-durable-subscription-name";
    String ESTABLISH_TRUST_IN_CLIENT = "establish-trust-in-client";
    String ESTABLISH_TRUST_IN_TARGET = "establish-trust-in-target";
    String INTEGRITY = "integrity";
    String IOR_CONFIG = "ior-security-config";
    String MDB_CONNECTION_FACTORY = "mdb-connection-factory";
    String MESSAGE_DESTINATION = "message-destination";
    String MESSAGE_DESTINATION_NAME = "message-destination-name";
    String REALM = "realm";
    String LOGIN_CONFIG = "login-config";
    String REQUIRED = "required";
    String RESOURCE_ADAPTER_MID = "resource-adapter-mid";
    String SAS_CONTEXT = "sas-context";
    String TRANSPORT_CONFIG = "transport-config";
    String MDB_RESOURCE_ADAPTER = "mdb-resource-adapter";
    String ACTIVATION_CONFIG = "activation-config";
    String ACTIVATION_CONFIG_PROPERTY = "activation-config-property";
    String ACTIVATION_CONFIG_PROPERTY_NAME = "activation-config-property-name";
    String ACTIVATION_CONFIG_PROPERTY_VALUE = "activation-config-property-value";

    String APPLICATION_CLIENT = "app-client";
    String CMP = "cmp";
    String CMPRESOURCE = "cmpresource";
    String DEFAULT_RESOURCE_PRINCIPAL = "default-resource-principal";
    String DISPLAY_NAME = "display-name";
    String EJB = "ejb";
    String EJB_NAME = "ejb-name";
    String EJB20_CMP = "ejb20-cmp";
    String EJBS = "enterprise-beans";
    String FIELD = "field";

    String GROUP = "group";
    String GROUPS = "groups";
    String JOIN_OBJECT = "join-object";
    String JNDI_NAME = "jndi-name";
    String LOCAL_PART = "localpart";
    String MAIL_CONFIG = "mail-configuration";
    String MAIL_FROM = "mail-from";
    String MAIL_HOST = "mail-host";
    String METHOD = "method";
    String NAME = "name";
    String NAMESPACE_URI = "namespace-uri";
    String OPERATION = "operation";

    String PASSWORD = "password";
    String PRINCIPALS = "principals";
    String PRINCIPAL = "principal";
    String REMOTE_ENTITY = "remote-entity";
    String ROLE = "role";
    String ROLE_MAPPING = "rolemapping";
    String ROLE_ENTRY = "role";
    String SERVER_NAME = "server-name";

    String SERVLET = "servlet";
    String SERVLET_NAME = "servlet-name";
    String SOURCE = "source";
    String SINK = "sink";
    String SQL = "sql";
    String SQL_STATEMENT = "sql-statement";
    String TABLE_CREATE = "table-create-sql";
    String TABLE_REMOVE = "table-remove-sql";


    String UNIQUE_ID = "unique-id";
    String WEB = "web";
    String WEB_SERVICE_ENDPOINT = "web-service-endpoint";


    String EJB_IMPL = "ejb-impl";
    String REMOTE_IMPL = "remote-impl";
    String LOCAL_IMPL = "local-impl";
    String REMOTE_HOME_IMPL = "remote-home-impl";
    String LOCAL_HOME_IMPL = "local-home-impl";
    String STATE_IMPL = "state-impl";
    String GEN_CLASSES = "gen-classes";

    // acceptable values
    String TRUE = "true";
    String FALSE = "false";

    // SECURITY related
    String SECURITY_ROLE_MAPPING = "security-role-mapping";
    String SECURITY_ROLE_ASSIGNMENT = "security-role-assignment";
    String ROLE_NAME = "role-name";
    String PRINCIPAL_NAME = "principal-name";
    String GROUP_NAME = "group-name";
    String EXTERNALLY_DEFINED = "externally-defined";

    // common
    String EJB_REF = "ejb-ref";
    String RESOURCE_REF = "resource-ref";
    String RESOURCE_ENV_REF = "resource-env-ref";

    // S1AS specific
    String PASS_BY_REFERENCE = "pass-by-reference";
    String JMS_MAX_MESSAGES_LOAD = "jms-max-messages-load";
    String IS_READ_ONLY_BEAN = "is-read-only-bean";
    String REFRESH_PERIOD_IN_SECONDS = "refresh-period-in-seconds";
    String COMMIT_OPTION = "commit-option";
    String CMT_TIMEOUT_IN_SECONDS = "cmt-timeout-in-seconds";
    String USE_THREAD_POOL_ID = "use-thread-pool-id";
    String AVAILABILITY_ENABLED = "availability-enabled";
    String DISABLE_NONPORTABLE_JNDI_NAMES = "disable-nonportable-jndi-names";
    String PER_REQUEST_LOAD_BALANCING = "per-request-load-balancing";

    // CMP related
    String CMP_RESOURCE = "cmp-resource";
    String MAPPING_PROPERTIES = "mapping-properties";
    String IS_ONE_ONE_CMP = "is-one-one-cmp";
    String ONE_ONE_FINDERS = "one-one-finders";
    String METHOD_NAME = "method-name";
    String QUERY_PARAMS = "query-params";
    String QUERY_FILTER = "query-filter";
    String QUERY_VARIABLES = "query-variables";
    String QUERY_ORDERING = "query-ordering";
    String FINDER = "finder";
    String CREATE_TABLES_AT_DEPLOY = "create-tables-at-deploy";
    String DROP_TABLES_AT_UNDEPLOY = "drop-tables-at-undeploy";
    String DATABASE_VENDOR_NAME = "database-vendor-name";
    String SCHEMA_GENERATOR_PROPERTIES = "schema-generator-properties";

    // PM-DESCRIPTORS related
    @Deprecated(forRemoval = true, since = "3.1")
    String PM_DESCRIPTORS = "pm-descriptors";
    @Deprecated(forRemoval = true, since = "3.1")
    String PM_DESCRIPTOR = "pm-descriptor";
    @Deprecated(forRemoval = true, since = "3.1")
    String PM_IDENTIFIER = "pm-identifier";
    @Deprecated(forRemoval = true, since = "3.1")
    String PM_VERSION = "pm-version";
    @Deprecated(forRemoval = true, since = "3.1")
    String PM_CONFIG = "pm-config";
    @Deprecated(forRemoval = true, since = "3.1")
    String PM_CLASS_GENERATOR = "pm-class-generator";
    @Deprecated(forRemoval = true, since = "3.1")
    String PM_MAPPING_FACTORY = "pm-mapping-factory";
    @Deprecated(forRemoval = true, since = "3.1")
    String PM_INUSE = "pm-inuse";

    // BEAN-POOL related
    String BEAN_POOL = "bean-pool";
    String STEADY_POOL_SIZE = "steady-pool-size";
    String POOL_RESIZE_QUANTITY = "resize-quantity";
    String MAX_POOL_SIZE = "max-pool-size";
    String POOL_IDLE_TIMEOUT_IN_SECONDS = "pool-idle-timeout-in-seconds";
    String MAX_WAIT_TIME_IN_MILLIS = "max-wait-time-in-millis";

    // BEAN-CACHE related
    String BEAN_CACHE = "bean-cache";
    String MAX_CACHE_SIZE = "max-cache-size";
    String RESIZE_QUANTITY = "resize-quantity";
    String IS_CACHE_OVERFLOW_ALLOWED = "is-cache-overflow-allowed";
    String CACHE_IDLE_TIMEOUT_IN_SECONDS = "cache-idle-timeout-in-seconds";
    String REMOVAL_TIMEOUT_IN_SECONDS = "removal-timeout-in-seconds";
    String VICTIM_SELECTION_POLICY = "victim-selection-policy";

    // thread-pool related
    String THREAD_CORE_POOL_SIZE = "thread-core-pool-size";
    String THREAD_MAX_POOL_SIZE  = "thread-max-pool-size";
    String THREAD_KEEP_ALIVE_SECONDS = "thread-keep-alive-seconds";
    String THREAD_QUEUE_CAPACITY = "thread-queue-capacity";
    String ALLOW_CORE_THREAD_TIMEOUT = "allow-core-thread-timeout";
    String PRESTART_ALL_CORE_THREADS = "prestart-all-core-threads";

    // flush-at-end-of-method
    String FLUSH_AT_END_OF_METHOD =
        "flush-at-end-of-method";
    // checkpointed-methods, support backward compatibility with 7.1
    String CHECKPOINTED_METHODS =
        "checkpointed-methods";
    // checkpoint-at-end-of-method, equivalent element of
    // checkpointed-methods in 8.1 and later releases
    String CHECKPOINT_AT_END_OF_METHOD =
        "checkpoint-at-end-of-method";
    // prefetch-disabled
    String PREFETCH_DISABLED =
        "prefetch-disabled";

    String QUERY_METHOD = "query-method";

    // Connector related
    String RESOURCE_ADAPTER = "resource-adapter";
    String ROLE_MAP = "role-map";
    String IDLE_TIMEOUT_IN_SECONDS = "idle-timeout-in-seconds";
    String PROPERTY = "property";
    String MAP_ELEMENT = "map-element";
    String MAP_ID = "map-id";
    String BACKEND_PRINCIPAL = "backend-principal";
    String USER_NAME = "user-name";
    String CREDENTIAL = "credential";

    // application related
    String WEB_URI = "web-uri";
    String CONTEXT_ROOT = "context-root"; // also used in java web start support
    String ARCHIVE_NAME = "archive-name";
    String COMPATIBILITY = "compatibility";
    String KEEP_STATE = "keep-state";
    String VERSION_IDENTIFIER = "version-identifier";
    String APPLICATION_PARAM = "application-param";
    String PARAM_NAME = "param-name";
    String PARAM_VALUE = "param-value";
    String MODULE = "module";
    String TYPE = "type";
    String PATH = "path";

    // Web
    String CACHE_MAPPING = "cache-mapping";
    String CACHE_HELPER = "cache-helper";
    String CACHE_HELPER_REF = "cache-helper-ref";
    String CLASS_NAME = "class-name";
    String COOKIE_PROPERTIES = "cookie-properties";
    String CONSTRAINT_FIELD = "constraint-field";
    String CONSTRAINT_FIELD_VALUE = "constraint-field-value";
    String LOCALE_CHARSET_INFO = "locale-charset-info";
    String DEFAULT_LOCALE = "default-locale";
    String DEFAULT_HELPER = "default-helper";
    String LOCALE = "locale";
    String MAX_ENTRIES = "max-entries";
    String TIMEOUT_IN_SECONDS = "timeout-in-seconds";
    String ENABLED = "enabled";
    String AGENT = "agent";
    String CHARSET = "charset";
    String LOCALE_CHARSET_MAP = "locale-charset-map";
    String PARAMETER_ENCODING = "parameter-encoding";
    String FORM_HINT_FIELD = "form-hint-field";
    String DEFAULT_CHARSET = "default-charset";
    String STORE_PROPERTIES = "store-properties";
    String MANAGER_PROPERTIES = "manager-properties";
    String REFRESH_FIELD = "refresh-field";
    String SESSION_MANAGER = "session-manager";
    String SESSION_PROPERTIES = "session-properties";
    String SESSION_CONFIG = "session-config";
    String TIMEOUT = "timeout";
    String PERSISTENCE_TYPE = "persistence-type";
    String JSP_CONFIG = "jsp-config";
    String CLASS_LOADER = "class-loader";
    String EXTRA_CLASS_PATH = "extra-class-path";
    String DELEGATE = "delegate";
    String DYNAMIC_RELOAD_INTERVAL =
        "dynamic-reload-interval";
    String CACHE = "cache";
    String KEY_FIELD = "key-field";
    String URL_PATTERN = "url-pattern";
    String HTTP_METHOD = "http-method";
    String DISPATCHER = "dispatcher";
    String SCOPE = "scope";
    String CACHE_ON_MATCH = "cache-on-match";
    String CACHE_ON_MATCH_FAILURE = "cache-on-match-failure";
    String MATCH_EXPR = "match-expr";
    String VALUE = "value";
    String IDEMPOTENT_URL_PATTERN = "idempotent-url-pattern";
    String ERROR_URL = "error-url";
    String HTTPSERVLET_SECURITY_PROVIDER = "httpservlet-security-provider";
    String NUM_OF_RETRIES = "num-of-retries";

    String JAVA_METHOD = "java-method";
    String METHOD_PARAMS = "method-params";
    String METHOD_PARAM = "method-param";

    String VALVE = "valve";

    // Java Web Start-support related
    String JAVA_WEB_START_ACCESS = "java-web-start-access";
    String ELIGIBLE = "eligible";
    String VENDOR = "vendor";
    String JNLP_DOC = "jnlp-doc";
    // also uses CONTEXT_ROOT defined above in the application-related section


    // Weblogic specific
    String RESOURCE_DESCRIPTION = "resource-description";
    String RESOURCE_ENV_DESCRIPTION = "resource-env-description";
    String EJB_REFERENCE_DESCRIPTION = "ejb-reference-description";
}
