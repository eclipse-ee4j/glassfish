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

package org.glassfish.resources.api;

/**
 * @author Jagadish Ramu
 */
public interface ResourceConstants {

    /**
     * Constant to denote external jndi resource type.
     */
    public static final String RES_TYPE_EXTERNAL_JNDI = "external-jndi";

    public static final String RES_TYPE_JDBC = "jdbc";

    /**
     * Constant to denote jdbc connection pool resource type.
     */
    public static final String RES_TYPE_JCP = "jcp";

    /**
     * Constant to denote connector connection pool  resource type.
     */
    public static final String RES_TYPE_CCP = "ccp";

    /**
     * Constant to denote connector resource type.
     */
    public static final String RES_TYPE_CR = "cr";

    /**
     * Constant to denote custom resource type.
     */
    public static final String RES_TYPE_CUSTOM = "custom";

    /**
     * Constant to denote admin object resource type.
     */
    public static final String RES_TYPE_AOR = "aor";

    /**
     * Constant to denote resource adapter config type.
     */
    public static final String RES_TYPE_RAC = "rac";

    /**
     * Constant to denote connector-work-security-map type.
     */
    public static final String RES_TYPE_CWSM = "cwsm";

    /**
     * Constant to denote mail resource type.
     */
    public static final String RES_TYPE_MAIL = "mail";

    /**
     * Represents the glassfish-resources.xml handling module name / type for .ear
     */
    public static final String GF_RESOURCES_MODULE_EAR = "resources_ear";

    /**
     * Represents the glassfish-resources.xml handling module name / type for standalone application
     */
    public static final String GF_RESOURCES_MODULE = "resources";

    /**
     * Represents the location where glassfish-resources.xml will be present in an archive
     */
    public static final String GF_RESOURCES_LOCATION ="META-INF/glassfish-resources.xml";

    /** resource type residing in an external JNDI repository */
    public static final String EXT_JNDI_RES_TYPE = "external-jndi-resource";

    public static final String JMS_QUEUE = "javax.jms.Queue";
    public static final String JMS_TOPIC = "javax.jms.Topic";
    public static final String JMS_QUEUE_CONNECTION_FACTORY = "javax.jms.QueueConnectionFactory";
    public static final String JMS_TOPIC_CONNECTION_FACTORY = "javax.jms.TopicConnectionFactory";
    public static final String JMS_MESSAGE_LISTENER = "javax.jms.MessageListener";

    //TODO should be refactored to non-resources module
    /**
     *  Reserved sub-context where datasource-definition objets (resource and pool) are bound with generated names.
     */
    public static String DATASOURCE_DEFINITION_JNDINAME_PREFIX="__datasource_definition/";
    public static String MAILSESSION_DEFINITION_JNDINAME_PREFIX="__mailsession_definition/";
    public static String CONNECTION_FACTORY_DEFINITION_JNDINAME_PREFIX="__connection_factory_definition/";
    public static String JMS_CONNECTION_FACTORY_DEFINITION_JNDINAME_PREFIX = "__jms_connection_factory_definition/";
    public static String JMS_DESTINATION_DEFINITION_JNDINAME_PREFIX = "__jms_destination_definition/";
    public static String ADMINISTERED_OBJECT_DEFINITION_JNDINAME_PREFIX="__administered_object_definition/";

    public static final String JAVA_SCOPE_PREFIX = "java:";
    public static final String JAVA_APP_SCOPE_PREFIX = "java:app/";
    public static final String JAVA_COMP_SCOPE_PREFIX = "java:comp/";
    public static final String JAVA_MODULE_SCOPE_PREFIX = "java:module/";
    public static final String JAVA_GLOBAL_SCOPE_PREFIX = "java:global/";

    public static enum TriState {
        TRUE, FALSE, UNKNOWN
    }

    public final static String CONNECTOR_RESOURCES = "CONNECTOR";
    public final static String NON_CONNECTOR_RESOURCES = "NON-CONNECTOR";

    /**
     * Token used for generating the name to refer to the embedded rars.
     * It will be AppName+EMBEDDEDRAR_NAME_DELIMITER+embeddedRarName.
     */

    public static String EMBEDDEDRAR_NAME_DELIMITER="#";

    public final static String APP_META_DATA_RESOURCES = "app-level-resources-config";
    public final static String APP_SCOPED_RESOURCES_MAP = "app-scoped-resources-map";

}
