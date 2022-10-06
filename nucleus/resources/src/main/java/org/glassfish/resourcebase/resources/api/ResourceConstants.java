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

package org.glassfish.resourcebase.resources.api;

/**
 * @author Jagadish Ramu
 */
public interface ResourceConstants {

    /**
     * Constant to denote external jndi resource type.
     */
    String RES_TYPE_EXTERNAL_JNDI = "external-jndi";

    String RES_TYPE_JDBC = "jdbc";

    /**
     * Constant to denote jdbc connection pool resource type.
     */
    String RES_TYPE_JCP = "jcp";

    /**
     * Constant to denote connector connection pool  resource type.
     */
    String RES_TYPE_CCP = "ccp";

    /**
     * Constant to denote connector resource type.
     */
    String RES_TYPE_CR = "cr";

    /**
     * Constant to denote custom resource type.
     */
    String RES_TYPE_CUSTOM = "custom";

    /**
     * Constant to denote admin object resource type.
     */
    String RES_TYPE_AOR = "aor";

    /**
     * Constant to denote resource adapter config type.
     */
    String RES_TYPE_RAC = "rac";

    /**
     * Constant to denote connector-work-security-map type.
     */
    String RES_TYPE_CWSM = "cwsm";

    /**
     * Constant to denote mail resource type.
     */
    String RES_TYPE_MAIL = "mail";

    /**
     * Represents the glassfish-resources.xml handling module name / type for .ear
     */
    String GF_RESOURCES_MODULE_EAR = "resources_ear";

    /**
     * Represents the glassfish-resources.xml handling module name / type for standalone application
     */
    String GF_RESOURCES_MODULE = "resources";

    /**
     * Represents the location where glassfish-resources.xml will be present in an archive
     */
    String GF_RESOURCES_LOCATION ="META-INF/glassfish-resources.xml";

    /** resource type residing in an external JNDI repository */
    String EXT_JNDI_RES_TYPE = "external-jndi-resource";

    String JMS_QUEUE = "jakarta.jms.Queue";
    String JMS_TOPIC = "jakarta.jms.Topic";
    String JMS_QUEUE_CONNECTION_FACTORY = "jakarta.jms.QueueConnectionFactory";
    String JMS_TOPIC_CONNECTION_FACTORY = "jakarta.jms.TopicConnectionFactory";
    String JMS_MESSAGE_LISTENER = "jakarta.jms.MessageListener";

    //TODO should be refactored to non-resources module
    /**
     *  Reserved sub-context where datasource-definition objets (resource and pool) are bound with generated names.
     */
    String DATASOURCE_DEFINITION_JNDINAME_PREFIX = "__datasource_definition/";
    String MAILSESSION_DEFINITION_JNDINAME_PREFIX="__mailsession_definition/";
    String CONNECTION_FACTORY_DEFINITION_JNDINAME_PREFIX = "__connection_factory_definition/";
    String JMS_CONNECTION_FACTORY_DEFINITION_JNDINAME_PREFIX = "__jms_connection_factory_definition/";
    String JMS_DESTINATION_DEFINITION_JNDINAME_PREFIX = "__jms_destination_definition/";
    String ADMINISTERED_OBJECT_DEFINITION_JNDINAME_PREFIX="__administered_object_definition/";

    String JAVA_SCOPE_PREFIX = "java:";
    String JAVA_APP_SCOPE_PREFIX = "java:app/";
    String JAVA_COMP_SCOPE_PREFIX = "java:comp/";
    String JAVA_MODULE_SCOPE_PREFIX = "java:module/";
    String JAVA_GLOBAL_SCOPE_PREFIX = "java:global/";
    String JAVA_COMP_ENV_SCOPE_PREFIX = "java:comp/env/";

    public enum TriState {
        TRUE, FALSE, UNKNOWN
    }

    String CONNECTOR_RESOURCES = "CONNECTOR";
    String NON_CONNECTOR_RESOURCES = "NON-CONNECTOR";

    /**
     * Token used for generating the name to refer to the embedded rars.
     * It will be AppName+EMBEDDEDRAR_NAME_DELIMITER+embeddedRarName.
     */

    String EMBEDDEDRAR_NAME_DELIMITER="#";

    String APP_META_DATA_RESOURCES = "app-level-resources-config";
    String APP_SCOPED_RESOURCES_JNDI_NAMES = "app-scoped-resources-jndi-names";
    String APP_SCOPED_RESOURCES_RA_NAMES = "app-scoped-resources-ra-names";
    String APP_SCOPED_RESOURCES_MAP = "app-scoped-resources-map";

    String CONCURRENT_CONTEXT_SERVICE_DEFINITION_JNDINAME_PREFIX = "__context_service_definition/";
}
