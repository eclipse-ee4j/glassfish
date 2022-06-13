/*
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

package com.sun.enterprise.deployment.xml;

/**
 * I hold the XML tag names common to the dtds across the J2EE platform.
 * @author Jerome Dochez
 */

public interface TagNames {

    public static final String NAME = "display-name";
    public final static String MODULE_NAME = "module-name";
    public static final String ID = "id";
    public static final String DESCRIPTION = "description";
    public static final String VERSION = "version";
    public static final String METADATA_COMPLETE = "metadata-complete";

    public static final String ICON = "icon";
    public static final String LARGE_ICON = "large-icon";
    public static final String SMALL_ICON = "small-icon";

    public static final String ENVIRONMENT_PROPERTY = "env-entry";
    public static final String ENVIRONMENT_PROPERTY_NAME = "env-entry-name";
    public static final String ENVIRONMENT_PROPERTY_VALUE = "env-entry-value";
    public static final String ENVIRONMENT_PROPERTY_TYPE = "env-entry-type";

    public static final String EJB_REFERENCE = "ejb-ref";
    public static final String EJB_REFERENCE_NAME = "ejb-ref-name";
    public static final String EJB_REFERENCE_TYPE = "ejb-ref-type";
    public static final String EJB_LINK = "ejb-link";

    public static final String EJB_LOCAL_REFERENCE = "ejb-local-ref";

    public final static String EJB_NAME = "ejb-name";
    public final static String HOME = "home";
    public final static String REMOTE = "remote";
    public final static String LOCAL_HOME = "local-home";
    public final static String LOCAL = "local";

    public static final String LOOKUP_NAME = "lookup-name";

    public static final String RESOURCE_REFERENCE = "resource-ref";
    public static final String RESOURCE_REFERENCE_NAME = "res-ref-name";
    public static final String RESOURCE_SHARING_SCOPE = "res-sharing-scope";

    public static final String MESSAGE_DESTINATION_REFERENCE = "message-destination-ref";
    public static final String MESSAGE_DESTINATION_REFERENCE_NAME = "message-destination-ref-name";
    public static final String MESSAGE_DESTINATION = "message-destination";
    public static final String MESSAGE_DESTINATION_NAME = "message-destination-name";
    public static final String MESSAGE_DESTINATION_TYPE = "message-destination-type";
    public static final String MESSAGE_DESTINATION_USAGE = "message-destination-usage";
    public static final String MESSAGE_DESTINATION_LINK = "message-destination-link";

    public static final String RESOURCE_ENV_REFERENCE = "resource-env-ref";
    public static final String RESOURCE_ENV_REFERENCE_NAME = "resource-env-ref-name";
    public static final String RESOURCE_ENV_REFERENCE_TYPE = "resource-env-ref-type";

    public static final String DATA_SOURCE = "data-source";
    public static final String DATA_SOURCE_DESCRIPTION = "description";
    public static final String DATA_SOURCE_NAME = "name";
    public static final String DATA_SOURCE_CLASS_NAME = "class-name";
    public static final String DATA_SOURCE_URL = "url";
    public static final String DATA_SOURCE_SERVER_NAME = "server-name";
    public static final String DATA_SOURCE_PORT_NUMBER = "port-number";
    public static final String DATA_SOURCE_DATABASE_NAME = "database-name";
    public static final String DATA_SOURCE_USER = "user";
    public static final String DATA_SOURCE_PASSWORD = "password";
    public static final String DATA_SOURCE_LOGIN_TIMEOUT = "login-timeout";
    public static final String DATA_SOURCE_TRANSACTIONAL = "transactional";
    public static final String DATA_SOURCE_ISOLATION_LEVEL = "isolation-level";
    public static final String DATA_SOURCE_INITIAL_POOL_SIZE = "initial-pool-size";
    public static final String DATA_SOURCE_MIN_POOL_SIZE = "min-pool-size";
    public static final String DATA_SOURCE_MAX_POOL_SIZE = "max-pool-size";
    public static final String DATA_SOURCE_MAX_IDLE_TIME = "max-idle-time";
    public static final String DATA_SOURCE_MAX_STATEMENTS = "max-statements";

    public static final String RESOURCE_PROPERTY_NAME = "name";
    public static final String RESOURCE_PROPERTY_VALUE = "value";
    public static final String RESOURCE_PROPERTY = "property";

    public static final String CONNECTION_FACTORY = "connection-factory";
    public static final String CONNECTION_FACTORY_DESCRIPTION = "description";
    public static final String CONNECTION_FACTORY_NAME = "name";
    public static final String CONNECTION_FACTORY_INTERFACE_NAME = "interface-name";
    public static final String CONNECTION_FACTORY_ADAPTER = "resource-adapter";
    public static final String CONNECTION_FACTORY_TRANSACTION_SUPPORT = "transaction-support";
    public static final String CONNECTION_FACTORY_MAX_POOL_SIZE = "max-pool-size";
    public static final String CONNECTION_FACTORY_MIN_POOL_SIZE = "min-pool-size";

    public static final String ADMINISTERED_OBJECT = "administered-object";
    public static final String ADMINISTERED_OBJECT_DESCRIPTION = "description";
    public static final String ADMINISTERED_OBJECT_NAME = "name";
    public static final String ADMINISTERED_OBJECT_INTERFACE_NAME = "interface-name";
    public static final String ADMINISTERED_OBJECT_CLASS_NAME = "class-name";
    public static final String ADMINISTERED_OBJECT_ADAPTER = "resource-adapter";
    public static final String ADMINISTERED_OBJECT_PROPERTY = "property";
    public static final String ADMINISTERED_OBJECT_PROPERTY_NAME = "name";
    public static final String ADMINISTERED_OBJECT_PROPERTY_VALUE = "value";

    public static final String JMS_CONNECTION_FACTORY = "jms-connection-factory";
    public static final String JMS_CONNECTION_FACTORY_DESCRIPTION = "description";
    public static final String JMS_CONNECTION_FACTORY_NAME = "name";
    public static final String JMS_CONNECTION_FACTORY_INTERFACE_NAME = "interface-name";
    public static final String JMS_CONNECTION_FACTORY_CLASS_NAME = "class-name";
    public static final String JMS_CONNECTION_FACTORY_RESOURCE_ADAPTER = "resource-adapter";
    public static final String JMS_CONNECTION_FACTORY_USER = "user";
    public static final String JMS_CONNECTION_FACTORY_PASSWORD = "password";
    public static final String JMS_CONNECTION_FACTORY_CLIENT_ID = "client-id";
    public static final String JMS_CONNECTION_FACTORY_TRANSACTIONAL = "transactional";
    public static final String JMS_CONNECTION_FACTORY_MAX_POOL_SIZE = "max-pool-size";
    public static final String JMS_CONNECTION_FACTORY_MIN_POOL_SIZE = "min-pool-size";
    public static final String JMS_CONNECTION_FACTORY_PROPERTY = "property";
    public static final String JMS_CONNECTION_FACTORY_PROPERTY_NAME = "name";
    public static final String JMS_CONNECTION_FACTORY_PROPERTY_VALUE = "value";

    public static final String JMS_DESTINATION = "jms-destination";
    public static final String JMS_DESTINATION_DESCRIPTION = "description";
    public static final String JMS_DESTINATION_NAME = "name";
    public static final String JMS_DESTINATION_INTERFACE_NAME = "interface-name";
    public static final String JMS_DESTINATION_CLASS_NAME = "class-name";
    public static final String JMS_DESTINATION_RESOURCE_ADAPTER = "resource-adapter";
    public static final String JMS_DESTINATION_DESTINATION_NAME = "destination-name";
    public static final String JMS_DESTINATION_PROPERTY = "property";
    public static final String JMS_DESTINATION_PROPERTY_NAME = "name";
    public static final String JMS_DESTINATION_PROPERTY_VALUE = "value";

    public static final String MANAGED_EXECUTOR = "managed-executor";
    public static final String MANAGED_EXECUTOR_NAME = "name";
    public static final String MANAGED_EXECUTOR_MAX_ASYNC = "max-async";
    public static final String MANAGED_EXECUTOR_HUNG_TASK_THRESHOLD = "hung-task-threshold";
    public static final String MANAGED_EXECUTOR_CONTEXT_SERVICE_REF = "context-service-ref";

    public static final String MANAGED_THREAD_FACTORY = "managed-thread-factory";
    public static final String MANAGED_THREAD_FACTORY_NAME = "name";
    public static final String MANAGED_THREAD_FACTORY_CONTEXT_SERVICE_REF = "context-service-ref";
    public static final String MANAGED_THREAD_FACTORY_PRIORITY = "priority";

    public static final String MANAGED_SCHEDULED_EXECUTOR = "managed-scheduled-executor";
    public static final String MANAGED_SCHEDULED_EXECUTOR_NAME = "name";
    public static final String MANAGED_SCHEDULED_EXECUTOR_CONTEXT_SERVICE_REF = "context-service-ref";
    public static final String MANAGED_SCHEDULED_EXECUTOR_MAX_ASYNC = "max-async";
    public static final String MANAGED_SCHEDULED_EXECUTOR_HUNG_TASK_THRESHOLD = "hung-task-threshold";

    public static final String CONTEXT_SERVICE = "context-service";
    public static final String CONTEXT_SERVICE_NAME = "name";
    public static final String CONTEXT_SERVICE_CLEARED = "cleared";
    public static final String CONTEXT_SERVICE_PROPAGATED = "propagated";
    public static final String CONTEXT_SERVICE_UNCHANGED = "unchanged";

    public static final String PERSISTENCE_CONTEXT_REF = "persistence-context-ref";
    public static final String PERSISTENCE_CONTEXT_REF_NAME = "persistence-context-ref-name";

    public static final String PERSISTENCE_PROPERTY = "persistence-property";

    public static final String PERSISTENCE_UNIT_NAME = "persistence-unit-name";
    public static final String PERSISTENCE_CONTEXT_TYPE = "persistence-context-type";
    public static final String PERSISTENCE_CONTEXT_SYNCHRONIZATION_TYPE = "persistence-context-synchronizationType";
    public static final String PERSISTENCE_UNIT_REF = "persistence-unit-ref";
    public static final String PERSISTENCE_UNIT_REF_NAME = "persistence-unit-ref-name";

    public static final String JMS_QUEUE_DEST_TYPE = "jakarta.jms.Queue";
    public static final String JMS_TOPIC_DEST_TYPE = "jakarta.jms.Topic";

    public static final String RESOURCE_TYPE = "res-type";
    public static final String RESOURCE_AUTHORIZATION = "res-auth";

    public static final String ROLE = "security-role";
    public static final String ROLE_NAME = "role-name";
    public static final String ROLE_REFERENCE = "security-role-ref";
    public static final String ROLE_LINK = "role-link";
    public static final String RUNAS_SPECIFIED_IDENTITY = "run-as";

    public static final String ENCODING_STYLE = "encoding-style";
    public static final String JAVA_TYPE = "java-type";

    public static final String WEB_SERVICE_ENDPOINT = "web-service-endpoint";
    public static final String XML_NAMESPACE_PREFIX = "xml:";
    public static final String LANG = "lang";

    public static final String MAIL_SESSION = "mail-session";
    public static final String MAIL_SESSION_NAME = "name";
    public static final String MAIL_SESSION_STORE_PROTOCOL = "store-protocol";
    public static final String MAIL_SESSION_TRANSPORT_PROTOCOL = "transport-protocol";
    public static final String MAIL_SESSION_HOST = "host";
    public static final String MAIL_SESSION_USER = "user";
    public static final String MAIL_SESSION_PASSWORD = "password";
    public static final String MAIL_SESSION_FROM = "from";

    public static final String NAME_VALUE_PAIR_NAME = "name";
    public static final String NAME_VALUE_PAIR_VALUE = "value";

    // FIXME by srini - should go away from here, longer term.
    public final static String METHOD_NAME = "method-name";
    public final static String METHOD_INTF = "method-intf";
    public final static String METHOD_PARAMS = "method-params";
    public final static String METHOD_PARAM = "method-param";

    // injection tags
    public static final String INJECTION_TARGET = "injection-target";
    public static final String INJECTION_TARGET_CLASS = "injection-target-class";
    public static final String INJECTION_TARGET_NAME = "injection-target-name";
    public static final String MAPPED_NAME = "mapped-name";

    public static final String POST_CONSTRUCT = "post-construct";
    public static final String PRE_DESTROY = "pre-destroy";
    public static final String LIFECYCLE_CALLBACK_CLASS = "lifecycle-callback-class";
    public static final String LIFECYCLE_CALLBACK_METHOD = "lifecycle-callback-method";

    public static final String J2EE_DEFAULTNAMESPACEPREFIX = "j2ee";
    public static final String J2EE_NAMESPACE = "http://java.sun.com/xml/ns/j2ee";
    public static final String JAVAEE_DEFAULTNAMESPACEPREFIX = "javaee";
    public static final String JAVAEE_NAMESPACE = "http://xmlns.jcp.org/xml/ns/javaee";
    public static final String JAKARTAEE_NAMESPACE = "https://jakarta.ee/xml/ns/jakartaee";
    public static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";

    public static final String WLS_WEB_APP_NAMESPACE = "http://xmlns.oracle.com/weblogic/weblogic-web-app";
    public static final String WLS_EJB_JAR_NAMESPACE = "http://xmlns.oracle.com/weblogic/weblogic-ejb-jar";
    public static final String WLS_WEBSERVICES_NAMESPACE = "http://xmlns.oracle.com/weblogic/weblogic-webservices";
    public static final String WLS_CONNECTOR_NAMESPACE = "http://xmlns.oracle.com/weblogic/weblogic-connector";
    public static final String WLS_APPLICATION_NAMESPACE = "http://xmlns.oracle.com/weblogic/weblogic-application";
    public static final String WLS_APPLICATION_CLIENT_NAMESPACE = "http://xmlns.oracle.com/weblogic/weblogic-application-client";
    public final static String WLS_APPLICATION_SCHEMA_LOCATION = "http://xmlns.oracle.com/weblogic/weblogic-application http://xmlns.oracle.com/weblogic/weblogic-application/1.5/weblogic-application.xsd";

    public static final String PERSISTENCE_XML_NAMESPACE = "urn:ejb3-namespace";
    public final static String W3C_XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
    public final static String SCHEMA_LOCATION_TAG = "xsi:schemaLocation";
    public final static String XMLNS = "http://www.w3.org/2000/xmlns/";
    public final static String XMLNS_XSI = "xmlns:xsi";
}
