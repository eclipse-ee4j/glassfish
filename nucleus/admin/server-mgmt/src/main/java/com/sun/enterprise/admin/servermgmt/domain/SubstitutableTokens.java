/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.domain;

import com.sun.appserv.server.util.Version;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SubstitutableTokens {

    public static final String CONFIG_MODEL_NAME_TOKEN_NAME = "CONFIG_MODEL_NAME";
    public static final String CONFIG_MODEL_NAME_TOKEN_VALUE = "server-config";
    public static final String HOST_NAME_TOKEN_NAME = "HOST_NAME";
    public static final String DOMAIN_NAME_TOKEN_NAME = "DOMAIN_NAME";
    public static final String HTTP_PORT_TOKEN_NAME = "HTTP_PORT";
    public static final String ORB_LISTENER_PORT_TOKEN_NAME = "ORB_LISTENER_PORT";
    public static final String JMS_PROVIDER_PORT_TOKEN_NAME = "JMS_PROVIDER_PORT";
    public static final String SERVER_ID_TOKEN_NAME = "SERVER_ID";
    public static final String ADMIN_PORT_TOKEN_NAME = "ADMIN_PORT";
    public static final String HTTP_SSL_PORT_TOKEN_NAME = "HTTP_SSL_PORT";
    public static final String ORB_SSL_PORT_TOKEN_NAME = "ORB_SSL_PORT";
    public static final String ORB_MUTUALAUTH_PORT_TOKEN_NAME = "ORB_MUTUALAUTH_PORT";
    public static final String OSGI_SHELL_TELNET_PORT_TOKEN_NAME = "OSGI_SHELL_TELNET_PORT";
    public static final String JAVA_DEBUGGER_PORT_TOKEN_NAME = "JAVA_DEBUGGER_PORT";
    public static final String ADMIN_CERT_DN_TOKEN_NAME = "ADMIN_CERT_DN";
    public static final String INSTANCE_CERT_DN_TOKEN_NAME = "INSTANCE_CERT_DN";
    public static final String SECURE_ADMIN_IDENTIFIER_TOKEN_NAME = "SECURE_ADMIN_IDENTIFIER";
    //This token is used for SE/EE only now, but it is likely that we will want to expose it
    //in PE (i.e. to access the exposed Mbeans). Remember that the http jmx port (used by
    //asadmin) will not be exposed pubically.
    public static final String JMX_SYSTEM_CONNECTOR_PORT_TOKEN_NAME = "JMX_SYSTEM_CONNECTOR_PORT";

    // Tokens for index.html
    public static final String VERSION_TOKEN_NAME = "VERSION";
    public static final String INSTALL_ROOT_TOKEN_NAME = "INSTALL_ROOT";

    // Tokens for glassfish-acc.xml
    public static final String SERVER_ROOT = "SERVER_ROOT";
    public static final String SERVER_NAME = "SERVER_NAME";
    public static final String ORB_LISTENER1_PORT = "ORB_LISTENER1_PORT";

    private static final String DOMAIN_DIR = "DOMAIN_DIR";

    public static Map<String, String> getSubstitutableTokens(DomainConfig domainConfig) {
        Map<String, String> substitutableTokens = new HashMap<>();
        Properties domainProperties = domainConfig.getDomainProperties();

        String instanceName = (String) domainConfig.get(DomainConfig.K_SERVERID);
        if (instanceName == null || instanceName.isEmpty()) {
            instanceName = PEFileLayout.DEFAULT_INSTANCE_NAME;
        }
        substitutableTokens.put(SERVER_ID_TOKEN_NAME, instanceName);
        substitutableTokens.put(DOMAIN_NAME_TOKEN_NAME, domainConfig.getRepositoryName());

        substitutableTokens.put(CONFIG_MODEL_NAME_TOKEN_NAME, CONFIG_MODEL_NAME_TOKEN_VALUE);
        substitutableTokens.put(HOST_NAME_TOKEN_NAME, (String) domainConfig.get(DomainConfig.K_HOST_NAME));

        substitutableTokens.put(ADMIN_PORT_TOKEN_NAME, domainConfig.get(DomainConfig.K_ADMIN_PORT).toString());
        substitutableTokens.put(HTTP_PORT_TOKEN_NAME, domainConfig.get(DomainConfig.K_INSTANCE_PORT).toString());
        substitutableTokens.put(ORB_LISTENER_PORT_TOKEN_NAME, domainConfig.get(DomainConfig.K_ORB_LISTENER_PORT).toString());
        substitutableTokens.put(JMS_PROVIDER_PORT_TOKEN_NAME, domainConfig.get(DomainConfig.K_JMS_PORT).toString());
        substitutableTokens.put(HTTP_SSL_PORT_TOKEN_NAME, domainConfig.get(DomainConfig.K_HTTP_SSL_PORT).toString());
        substitutableTokens.put(ORB_SSL_PORT_TOKEN_NAME, domainConfig.get(DomainConfig.K_IIOP_SSL_PORT).toString());
        substitutableTokens.put(ORB_MUTUALAUTH_PORT_TOKEN_NAME, domainConfig.get(DomainConfig.K_IIOP_MUTUALAUTH_PORT).toString());
        substitutableTokens.put(JMX_SYSTEM_CONNECTOR_PORT_TOKEN_NAME, domainConfig.get(DomainConfig.K_JMX_PORT).toString());
        substitutableTokens.put(OSGI_SHELL_TELNET_PORT_TOKEN_NAME, domainConfig.get(DomainConfig.K_OSGI_SHELL_TELNET_PORT).toString());
        substitutableTokens.put(JAVA_DEBUGGER_PORT_TOKEN_NAME, domainConfig.get(DomainConfig.K_JAVA_DEBUGGER_PORT).toString());

        substitutableTokens.put(ADMIN_CERT_DN_TOKEN_NAME, (String) domainConfig.get(DomainConfig.K_ADMIN_CERT_DN));
        substitutableTokens.put(INSTANCE_CERT_DN_TOKEN_NAME, (String) domainConfig.get(DomainConfig.K_INSTANCE_CERT_DN));
        substitutableTokens.put(SECURE_ADMIN_IDENTIFIER_TOKEN_NAME, (String) domainConfig.get(DomainConfig.K_SECURE_ADMIN_IDENTIFIER));

        substitutableTokens.put(VERSION_TOKEN_NAME, Version.getVersionNumber());
        substitutableTokens.put(INSTALL_ROOT_TOKEN_NAME, domainConfig.getInstallRoot());

        substitutableTokens.put(SERVER_ROOT, FileUtils.makeForwardSlashes(domainConfig.getInstallRoot()));
        substitutableTokens.put(SERVER_NAME, domainConfig.get(DomainConfig.K_HOST_NAME).toString());
        substitutableTokens.put(ORB_LISTENER1_PORT, domainConfig.get(DomainConfig.K_ORB_LISTENER_PORT).toString());
        String domainLocation = new File(domainConfig.getRepositoryRoot(), domainConfig.getRepositoryName()).getAbsolutePath();
        substitutableTokens.put(DOMAIN_DIR, domainLocation);

        for (String pname : domainProperties.stringPropertyNames()) {
            if (!substitutableTokens.containsKey(pname)) {
                substitutableTokens.put(pname, domainProperties.getProperty(pname));
            }
        }
        return substitutableTokens;
    }
}
