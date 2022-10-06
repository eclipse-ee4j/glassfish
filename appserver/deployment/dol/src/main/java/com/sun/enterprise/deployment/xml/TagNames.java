/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
 *
 * @author Jerome Dochez
 */
public interface TagNames {

    String NAME = "name";
    String DISPLAY_NAME = "display-name";
    String MODULE_NAME = "module-name";
    String ID = "id";
    String DESCRIPTION = "description";
    String VERSION = "version";
    String METADATA_COMPLETE = "metadata-complete";

    String ICON = "icon";
    String LARGE_ICON = "large-icon";
    String SMALL_ICON = "small-icon";

    String ENVIRONMENT_PROPERTY = "env-entry";
    String ENVIRONMENT_PROPERTY_NAME = "env-entry-name";
    String ENVIRONMENT_PROPERTY_VALUE = "env-entry-value";
    String ENVIRONMENT_PROPERTY_TYPE = "env-entry-type";

    String EJB_REFERENCE = "ejb-ref";
    String EJB_REFERENCE_NAME = "ejb-ref-name";
    String EJB_REFERENCE_TYPE = "ejb-ref-type";
    String EJB_LINK = "ejb-link";

    String EJB_LOCAL_REFERENCE = "ejb-local-ref";

    String EJB_NAME = "ejb-name";
    String HOME = "home";
    String REMOTE = "remote";
    String LOCAL_HOME = "local-home";
    String LOCAL = "local";

    String LOOKUP_NAME = "lookup-name";

    String RESOURCE_REFERENCE = "resource-ref";
    String RESOURCE_REFERENCE_NAME = "res-ref-name";
    String RESOURCE_SHARING_SCOPE = "res-sharing-scope";

    String MESSAGE_DESTINATION_REFERENCE = "message-destination-ref";
    String MESSAGE_DESTINATION_REFERENCE_NAME = "message-destination-ref-name";
    String MESSAGE_DESTINATION = "message-destination";
    String MESSAGE_DESTINATION_NAME = "message-destination-name";
    String MESSAGE_DESTINATION_TYPE = "message-destination-type";
    String MESSAGE_DESTINATION_USAGE = "message-destination-usage";
    String MESSAGE_DESTINATION_LINK = "message-destination-link";

    String RESOURCE_ENV_REFERENCE = "resource-env-ref";
    String RESOURCE_ENV_REFERENCE_NAME = "resource-env-ref-name";
    String RESOURCE_ENV_REFERENCE_TYPE = "resource-env-ref-type";

    String DATA_SOURCE = "data-source";
    String DATA_SOURCE_DESCRIPTION = "description";
    String DATA_SOURCE_NAME = "name";
    String DATA_SOURCE_CLASS_NAME = "class-name";
    String DATA_SOURCE_URL = "url";
    String DATA_SOURCE_SERVER_NAME = "server-name";
    String DATA_SOURCE_PORT_NUMBER = "port-number";
    String DATA_SOURCE_DATABASE_NAME = "database-name";
    String DATA_SOURCE_USER = "user";
    String DATA_SOURCE_PASSWORD = "password";
    String DATA_SOURCE_LOGIN_TIMEOUT = "login-timeout";
    String DATA_SOURCE_TRANSACTIONAL = "transactional";
    String DATA_SOURCE_ISOLATION_LEVEL = "isolation-level";
    String DATA_SOURCE_INITIAL_POOL_SIZE = "initial-pool-size";
    String DATA_SOURCE_MIN_POOL_SIZE = "min-pool-size";
    String DATA_SOURCE_MAX_POOL_SIZE = "max-pool-size";
    String DATA_SOURCE_MAX_IDLE_TIME = "max-idle-time";
    String DATA_SOURCE_MAX_STATEMENTS = "max-statements";

    String RESOURCE_PROPERTY_NAME = "name";
    String RESOURCE_PROPERTY_VALUE = "value";
    String RESOURCE_PROPERTY = "property";

    String CONNECTION_FACTORY = "connection-factory";
    String CONNECTION_FACTORY_DESCRIPTION = "description";
    String CONNECTION_FACTORY_NAME = "name";
    String CONNECTION_FACTORY_INTERFACE_NAME = "interface-name";
    String CONNECTION_FACTORY_ADAPTER = "resource-adapter";
    String CONNECTION_FACTORY_TRANSACTION_SUPPORT = "transaction-support";
    String CONNECTION_FACTORY_MAX_POOL_SIZE = "max-pool-size";
    String CONNECTION_FACTORY_MIN_POOL_SIZE = "min-pool-size";

    String ADMINISTERED_OBJECT = "administered-object";
    String ADMINISTERED_OBJECT_DESCRIPTION = "description";
    String ADMINISTERED_OBJECT_NAME = "name";
    String ADMINISTERED_OBJECT_INTERFACE_NAME = "interface-name";
    String ADMINISTERED_OBJECT_CLASS_NAME = "class-name";
    String ADMINISTERED_OBJECT_ADAPTER = "resource-adapter";
    String ADMINISTERED_OBJECT_PROPERTY = "property";
    String ADMINISTERED_OBJECT_PROPERTY_NAME = "name";
    String ADMINISTERED_OBJECT_PROPERTY_VALUE = "value";

    String JMS_CONNECTION_FACTORY = "jms-connection-factory";
    String JMS_CONNECTION_FACTORY_DESCRIPTION = "description";
    String JMS_CONNECTION_FACTORY_NAME = "name";
    String JMS_CONNECTION_FACTORY_INTERFACE_NAME = "interface-name";
    String JMS_CONNECTION_FACTORY_CLASS_NAME = "class-name";
    String JMS_CONNECTION_FACTORY_RESOURCE_ADAPTER = "resource-adapter";
    String JMS_CONNECTION_FACTORY_USER = "user";
    String JMS_CONNECTION_FACTORY_PASSWORD = "password";
    String JMS_CONNECTION_FACTORY_CLIENT_ID = "client-id";
    String JMS_CONNECTION_FACTORY_TRANSACTIONAL = "transactional";
    String JMS_CONNECTION_FACTORY_MAX_POOL_SIZE = "max-pool-size";
    String JMS_CONNECTION_FACTORY_MIN_POOL_SIZE = "min-pool-size";
    String JMS_CONNECTION_FACTORY_PROPERTY = "property";
    String JMS_CONNECTION_FACTORY_PROPERTY_NAME = "name";
    String JMS_CONNECTION_FACTORY_PROPERTY_VALUE = "value";

    String JMS_DESTINATION = "jms-destination";
    String JMS_DESTINATION_DESCRIPTION = "description";
    String JMS_DESTINATION_NAME = "name";
    String JMS_DESTINATION_INTERFACE_NAME = "interface-name";
    String JMS_DESTINATION_CLASS_NAME = "class-name";
    String JMS_DESTINATION_RESOURCE_ADAPTER = "resource-adapter";
    String JMS_DESTINATION_DESTINATION_NAME = "destination-name";
    String JMS_DESTINATION_PROPERTY = "property";
    String JMS_DESTINATION_PROPERTY_NAME = "name";
    String JMS_DESTINATION_PROPERTY_VALUE = "value";

    String PERSISTENCE_CONTEXT_REF = "persistence-context-ref";
    String PERSISTENCE_CONTEXT_REF_NAME = "persistence-context-ref-name";

    String PERSISTENCE_PROPERTY = "persistence-property";

    String PERSISTENCE_UNIT_NAME = "persistence-unit-name";
    String PERSISTENCE_CONTEXT_TYPE = "persistence-context-type";
    String PERSISTENCE_CONTEXT_SYNCHRONIZATION_TYPE = "persistence-context-synchronizationType";
    String PERSISTENCE_UNIT_REF = "persistence-unit-ref";
    String PERSISTENCE_UNIT_REF_NAME = "persistence-unit-ref-name";

    String JMS_QUEUE_DEST_TYPE = "jakarta.jms.Queue";
    String JMS_TOPIC_DEST_TYPE = "jakarta.jms.Topic";

    String RESOURCE_TYPE = "res-type";
    String RESOURCE_AUTHORIZATION = "res-auth";

    String ROLE = "security-role";
    String ROLE_NAME = "role-name";
    String ROLE_REFERENCE = "security-role-ref";
    String ROLE_LINK = "role-link";
    String RUNAS_SPECIFIED_IDENTITY = "run-as";

    String ENCODING_STYLE = "encoding-style";
    String JAVA_TYPE = "java-type";

    String WEB_SERVICE_ENDPOINT = "web-service-endpoint";
    String XML_NAMESPACE_PREFIX = "xml:";
    String LANG = "lang";

    String MAIL_SESSION = "mail-session";
    String MAIL_SESSION_NAME = "name";
    String MAIL_SESSION_STORE_PROTOCOL = "store-protocol";
    String MAIL_SESSION_TRANSPORT_PROTOCOL = "transport-protocol";
    String MAIL_SESSION_HOST = "host";
    String MAIL_SESSION_USER = "user";
    String MAIL_SESSION_PASSWORD = "password";
    String MAIL_SESSION_FROM = "from";

    String NAME_VALUE_PAIR_NAME = "name";
    String NAME_VALUE_PAIR_VALUE = "value";

    // FIXME by srini - should go away from here, longer term.
    String METHOD_NAME = "method-name";
    String METHOD_INTF = "method-intf";
    String METHOD_PARAMS = "method-params";
    String METHOD_PARAM = "method-param";

    // injection tags
    String INJECTION_TARGET = "injection-target";
    String INJECTION_TARGET_CLASS = "injection-target-class";
    String INJECTION_TARGET_NAME = "injection-target-name";
    String MAPPED_NAME = "mapped-name";

    String POST_CONSTRUCT = "post-construct";
    String PRE_DESTROY = "pre-destroy";
    String LIFECYCLE_CALLBACK_CLASS = "lifecycle-callback-class";
    String LIFECYCLE_CALLBACK_METHOD = "lifecycle-callback-method";

    String J2EE_DEFAULTNAMESPACEPREFIX = "j2ee";
    String J2EE_NAMESPACE = "http://java.sun.com/xml/ns/j2ee";
    String JAVAEE_DEFAULTNAMESPACEPREFIX = "javaee";
    String JAVAEE_NAMESPACE = "http://xmlns.jcp.org/xml/ns/javaee";
    String JAKARTAEE_NAMESPACE = "https://jakarta.ee/xml/ns/jakartaee";
    String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";

    String WLS_WEB_APP_NAMESPACE = "http://xmlns.oracle.com/weblogic/weblogic-web-app";
    String WLS_EJB_JAR_NAMESPACE = "http://xmlns.oracle.com/weblogic/weblogic-ejb-jar";
    String WLS_WEBSERVICES_NAMESPACE = "http://xmlns.oracle.com/weblogic/weblogic-webservices";
    String WLS_CONNECTOR_NAMESPACE = "http://xmlns.oracle.com/weblogic/weblogic-connector";
    String WLS_APPLICATION_NAMESPACE = "http://xmlns.oracle.com/weblogic/weblogic-application";
    String WLS_APPLICATION_CLIENT_NAMESPACE = "http://xmlns.oracle.com/weblogic/weblogic-application-client";
    String WLS_APPLICATION_SCHEMA_LOCATION = "http://xmlns.oracle.com/weblogic/weblogic-application http://xmlns.oracle.com/weblogic/weblogic-application/1.5/weblogic-application.xsd";

    String PERSISTENCE_XML_NAMESPACE = "urn:ejb3-namespace";
    String W3C_XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
    String SCHEMA_LOCATION_TAG = "xsi:schemaLocation";
    String XMLNS = "http://www.w3.org/2000/xmlns/";
    String XMLNS_XSI = "xmlns:xsi";
}
