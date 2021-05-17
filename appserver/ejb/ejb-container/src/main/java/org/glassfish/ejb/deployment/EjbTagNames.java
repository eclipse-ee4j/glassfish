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

package org.glassfish.ejb.deployment;

import com.sun.enterprise.deployment.xml.TagNames;

/**
 * The XML tag names for the ejb-jar dtd
 * @author Danny Coward
 */

public interface EjbTagNames extends TagNames {

    String SESSION = "session";
    String ENTITY = "entity";
    String SESSION_TYPE = "session-type";
    String MESSAGE_DRIVEN = "message-driven";

    String LOCAL_BEAN = "local-bean";
    String BUSINESS_LOCAL = "business-local";
    String BUSINESS_REMOTE = "business-remote";
    String EJB_CLASS = "ejb-class";
    String SERVICE_ENDPOINT_INTERFACE = "service-endpoint";
    String ROLE_REFERENCES = "security-role-refs";

    // entity
    String PERSISTENCE_TYPE = "persistence-type";
    String PRIMARY_KEY_CLASS = "prim-key-class";
    String PRIMARY_KEY_FIELD = "primkey-field";
    String REENTRANT = "reentrant";
    String PERSISTENT_FIELDS = "persistent-fields";
    String CMP_FIELD = "cmp-field";
    String CMP_VERSION = "cmp-version";
    String CMP_2_VERSION = "2.x";
    String CMP_1_VERSION = "1.x";
    String FIELD_NAME = "field-name";
    String ABSTRACT_SCHEMA_NAME = "abstract-schema-name";

    // relationships
    String RELATIONSHIPS = "relationships";
    String EJB_RELATION = "ejb-relation";
    String EJB_RELATION_NAME = "ejb-relation-name";
    String EJB_RELATIONSHIP_ROLE = "ejb-relationship-role";
    String EJB_RELATIONSHIP_ROLE_NAME = "ejb-relationship-role-name";
    String MULTIPLICITY = "multiplicity";
    String RELATIONSHIP_ROLE_SOURCE = "relationship-role-source";
    String CMR_FIELD = "cmr-field";
    String CMR_FIELD_NAME = "cmr-field-name";
    String CMR_FIELD_TYPE = "cmr-field-type";
    String CASCADE_DELETE = "cascade-delete";

    // application exceptions
    String APPLICATION_EXCEPTION = "application-exception";
    String APP_EXCEPTION_CLASS = "exception-class";
    String APP_EXCEPTION_ROLLBACK = "rollback";
    String APP_EXCEPTION_INHERITED = "inherited";

    // ejb-entity-ref
    String REMOTE_EJB_NAME = "remote-ejb-name";

    // query
    String QUERY = "query";
    String QUERY_METHOD = "query-method";
    String EJB_QL = "ejb-ql";
    String QUERY_RESULT_TYPE_MAPPING = "result-type-mapping";
    String QUERY_REMOTE_TYPE_MAPPING = "Remote";
    String QUERY_LOCAL_TYPE_MAPPING = "Local";

    // session
    String TRANSACTION_TYPE = "transaction-type";
    String TRANSACTION_SCOPE = "transaction-scope";
    String CONCURRENCY_MANAGEMENT_TYPE = "concurrency-management-type";
    String ASYNC_METHOD = "async-method";
    String CONCURRENT_METHOD = "concurrent-method";
    String CONCURRENT_LOCK = "lock";
    String CONCURRENT_ACCESS_TIMEOUT = "access-timeout";

    // message-driven
    String ACTIVATION_CONFIG = "activation-config";
    String ACTIVATION_CONFIG_PROPERTY = "activation-config-property";
    String ACTIVATION_CONFIG_PROPERTY_NAME = "activation-config-property-name";
    String ACTIVATION_CONFIG_PROPERTY_VALUE = "activation-config-property-value";
    String MESSAGING_TYPE = "messaging-type";
    String MSG_SELECTOR = "message-selector";
    String JMS_ACKNOWLEDGE_MODE = "acknowledge-mode";
    String MESSAGE_DRIVEN_DEST = "message-driven-destination";
    String JMS_SUBSCRIPTION_DURABILITY = "subscription-durability";
    String JMS_SUBSCRIPTION_IS_DURABLE = "Durable";
    String JMS_SUBSCRIPTION_NOT_DURABLE = "NonDurable";
    String JMS_AUTO_ACK_MODE = "Auto-acknowledge";
    String JMS_DUPS_OK_ACK_MODE  = "Dups-ok-acknowledge";
    String JMS_DEST_TYPE = "destination-type";

    String JNDI_NAME = "jndi-name";

    String EJB_BUNDLE_TAG = "ejb-jar";
    String EJBS = "enterprise-beans";
    String ASSEMBLY_DESCRIPTOR = "assembly-descriptor";
    String METHOD_PERMISSION = "method-permission";
    String UNCHECKED = "unchecked";
    String EXCLUDE_LIST = "exclude-list";
    String METHOD = "method";
    String CONTAINER_TRANSACTION = "container-transaction";
    String TRANSACTION_ATTRIBUTE = "trans-attribute";

    String EJB_CLIENT_JAR = "ejb-client-jar";

    // security-identity
    String SECURITY_IDENTITY = "security-identity";
    String USE_CALLER_IDENTITY = "use-caller-identity";

    // interceptors
    String INTERCEPTOR = "interceptor";
    String INTERCEPTORS = "interceptors";
    String INTERCEPTOR_BINDING = "interceptor-binding";
    String INTERCEPTOR_CLASS = "interceptor-class";
    String INTERCEPTOR_ORDER = "interceptor-order";
    String INTERCEPTOR_BUSINESS_METHOD = "method";
    String EXCLUDE_DEFAULT_INTERCEPTORS = "exclude-default-interceptors";
    String EXCLUDE_CLASS_INTERCEPTORS = "exclude-class-interceptors";
    String AROUND_CONSTRUCT = "around-construct";

    // around-invoke
    String AROUND_INVOKE_METHOD = "around-invoke";
    String AROUND_INVOKE_CLASS_NAME = "class";
    String AROUND_INVOKE_METHOD_NAME = "method-name";

    // around-timeout
    String AROUND_TIMEOUT_METHOD = "around-timeout";

    // stateful session
    String POST_ACTIVATE_METHOD = "post-activate";
    String PRE_PASSIVATE_METHOD = "pre-passivate";
    String INIT_METHOD = "init-method";
    String INIT_CREATE_METHOD = "create-method";
    String INIT_BEAN_METHOD = "bean-method";
    String REMOVE_METHOD = "remove-method";
    String REMOVE_BEAN_METHOD = "bean-method";
    String REMOVE_RETAIN_IF_EXCEPTION = "retain-if-exception";
    String STATEFUL_TIMEOUT = "stateful-timeout";
    String AFTER_BEGIN_METHOD = "after-begin-method";
    String BEFORE_COMPLETION_METHOD = "before-completion-method";
    String AFTER_COMPLETION_METHOD = "after-completion-method";
    String PASSIVATION_CAPABLE = "passivation-capable";

    // singleton
    String INIT_ON_STARTUP = "init-on-startup";
    String DEPENDS_ON = "depends-on";

    // Common timeout value elements for STATEFUL_TIMEOUT, ACCESS_TIMEOUT
    String TIMEOUT_VALUE = "timeout";
    String TIMEOUT_UNIT = "unit";

    // timeout method
    String TIMEOUT_METHOD = "timeout-method";
    String TIMER_SCHEDULE = "schedule";

    String TIMER = "timer";
    String TIMER_START = "start";
    String TIMER_END = "end";
    String TIMER_PERSISTENT = "persistent";
    String TIMER_TIMEZONE = "timezone";
    String TIMER_INFO = "info";
    String TIMER_SECOND = "second";
    String TIMER_MINUTE = "minute";
    String TIMER_HOUR = "hour";
    String TIMER_DAY_OF_MONTH = "day-of-month";
    String TIMER_MONTH = "month";
    String TIMER_DAY_OF_WEEK = "day-of-week";
    String TIMER_YEAR = "year";

}

