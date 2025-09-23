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

package com.sun.enterprise.admin.util;

import com.sun.enterprise.util.SystemPropertyConstants;

import java.nio.file.Path;

import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;

public final class AdminConstants {

    public static final String HOST_PROPERTY_NAME = "client-hostname";

    public static final String SYSTEM_CONNECTOR_NAME = "system";
    public static final String RENDEZVOUS_PROPERTY_NAME = "rendezvousOccurred";

    public static final String DOMAIN_TARGET = "domain";
    public static final String STANDALONE_CONFIGURATION_SUFFIX = "-config";

    //FIXHTHIS: Change the name when the configuration cloning is in place.
    public static final String DEFAULT_CONFIGURATION_NAME = SystemPropertyConstants.TEMPLATE_CONFIG_NAME;

    public static final String DAS_NODECONTROLLER_MBEAN_NAME = "com.sun.appserv:type=node-agents,category=config";
    public static final String NODEAGENT_STARTINSTANCES_OVERRIDE = " startInstancesOverride";
    public static final String NODEAGENT_SYNCINSTANCES_OVERRIDE = "syncInstances";
    public static final String NODEAGENT_DOMAIN_XML_LOCATION = "/config/domain.xml";

    public static final String DAS_SERVER_NAME = "server";

    public static final String DAS_CONFIG_OBJECT_NAME_PATTERN = "*:type=config,category=config,name=server-config";

    public static final String kAdminServletURI = "web1/entry";
    public static final String kHttpPrefix = "http://";
    public static final String kHttpsPrefix = "https://";
    public static final int kTypeWebModule = 0;
    public static final int kTypeEjbModule = 1;

    public static final int kDebugMode = 0;
    public static final int kNonDebugMode = 1;

    public static final String CLIENT_VERSION = "clientVersion";
    public static final String OBJECT_NAME = "objectName";
    public static final String OPERATION_NAME = "operationName";
    public static final String OPERATION_SIGNATURE = "signature";
    public static final String OPERATION_PARAMS = "params";
    public static final String EXCEPTION = "exception";
    public static final String RETURN_VALUE = "returnValue";
    public static final String ATTRIBUTE_NAME = "attributeName";
    public static final String ATTRIBUTE = "jmxAttribute";
    public static final String ATTRIBUTE_LIST = "jmxAttributeList";
    public static final String ATTRIBUTE_NAMES = "attributeNames";
    public static final String CLIENT_JAR = "Client.jar";

    public static final String kLoggerName = AdminLoggerInfo.ADMIN_LOGGER;

    public static final String DOMAIN_ADMIN_GROUP_NAME = "asadmin";

    public static final String AS_INSTALL_DIR_NAME = Path.of(System.getProperty(INSTALL_ROOT.getSystemPropertyName()))
        .getFileName().toString();

    private AdminConstants() {
        // hidden
    }
}
