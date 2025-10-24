/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.util;

import java.io.File;

import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;

public class SystemPropertyConstants {

    /**
     * Field used by Monitoring to encode a forward slash and/or dot
     */
    public static final String SLASH = "___SLASH___";
    public static final String MONDOT = "___MONDOT___";

    /**
     * Field
     */
    public static final String DEBUG_MODE_PROPERTY = "com.sun.aas.debugMode";

    /**
     * The certificate nick name specified in the System-Jmx-Conenctor of the DAS with which a Node Agent synchronizes
     */
    public static final String AGENT_CERT_NICKNAME = "com.sun.aas.agentCertNickname";

    public static final String ANT_ROOT_PROPERTY = "com.sun.aas.antRoot";

    public static final String ANT_LIB_PROPERTY = "com.sun.aas.antLib";

    public static final String JHELP_ROOT_PROPERTY = "com.sun.aas.jhelpRoot";

    /** name of the domain */
    public static final String DOMAIN_NAME = "com.sun.aas.domainName";

    /** name of the server instance key */
    public static final String SERVER_NAME = "com.sun.aas.instanceName";

    /** name of the server's config */
    public static final String CONFIG_NAME = "com.sun.aas.configName";

    /** name of the server's cluster */
    public static final String CLUSTER_NAME = "com.sun.aas.clusterName";

    /** name of the HADB location property **/
    public static final String HADB_ROOT_PROPERTY = "com.sun.aas.hadbRoot";

    public static final String NSS_ROOT_PROPERTY = "com.sun.aas.nssRoot";

    public static final String NSS_BIN_PROPERTY = "com.sun.aas.nssBin";

    public static final String NATIVE_LAUNCHER = "com.sun.aas.nativeLauncher";
    public static final String NATIVE_LAUNCHER_LIB_PREFIX = "com.sun.aas.nativeLauncherLibPrefix";

    public static final String DAS_SERVER_NAME = "server";
    @Deprecated
    public static final String DEFAULT_SERVER_INSTANCE_NAME = DAS_SERVER_NAME;

    public static final String DAS_SERVER_CONFIG = "server-config";

    public static final String DROP_INTERRUPTED_COMMANDS = "org.glassfish.job-manager.drop-interrupted-commands";

    /** Name of the default config that determines the configuration for the instances */
    public static final String TEMPLATE_CONFIG_NAME = "default-config";
    public static final String DEFAULT_ADMIN_USER = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "";

    public static final String DEFAULT_ADMIN_TIMEOUT_PROPERTY = "org.glassfish.admin.timeout";
    private static final int DEFAULT_ADMIN_TIMEOUT_VALUE = 5000;

    public static final String PREFER_ENV_VARS_OVER_PROPERTIES = "org.glassfish.variableExpansion.envPreferred";
    public static final String DISABLE_ENV_VAR_EXPANSION_PROPERTY = "org.glassfish.variableExpansion.envDisabled";

    public static final String TRUSTSTORE_FILENAME_DEFAULT = "cacerts.p12";
    public static final String KEYSTORE_FILENAME_DEFAULT = "keystore.p12";
    public static final String KEYSTORE_TYPE_DEFAULT = "PKCS12";
    public static final String KEYSTORE_PASSWORD_DEFAULT = "changeit";

    public static final String MASTER_PASSWORD_FILENAME = "master-password.p12";
    public static final String MASTER_PASSWORD_PASSWORD = "master-password";
    public static final String MASTER_PASSWORD_ALIAS = "master-password";


    /**
     * Returns the system specific file.separator delimited path to the asadmin script. Any changes to file layout should
     *
     * be reflected here. The path will contain '/' as the separator character, regardless of operating platform. Never
     * returns a null. Assumes the the property "INSTALL_ROOT_PROPERTY" is set in the VM before calling this. As of now
     * (September 2005) all the server instances and asadmin VM itself has this property set. The method does not guarantee
     * that the script exists on the given system. It should only be used when caller wants to know the location of the
     * script. Caller should make sure it exists.
     *
     * @return String representing the Path to asadmin script. Might return a string beginning with "null", if the
     * INSTALL_ROOT_PROPERTY is not defined
     */
    public static final String getAsAdminScriptLocation() {
        return getAdminScriptLocation(System.getProperty(INSTALL_ROOT.getSystemPropertyName()));
    }

    public static final String getAsAdminScriptLocation(String installRoot) {
        return getAdminScriptLocation(installRoot);
    }

    public static final String getAdminScriptLocation(String installRoot) {
        StringBuilder sb = new StringBuilder();
        String ext = OS.isWindows() ? OS.WINDOWS_BATCH_FILE_EXTENSION : "";
        String ASADMIN = "nadmin";
        String suffix = new StringBuilder("lib").append(File.separator).append(ASADMIN).append(ext).toString();

        sb.append(installRoot);
        final String fs = System.getProperty("file.separator");
        if (!sb.toString().endsWith(fs)) {
            sb.append(fs);
        }
        sb.append(suffix);

        return sb.toString();
    }

    /**
     * Returns the default timeout in milliseconds used in some Admin commands.
     *
     * @return The value of the system property {@link SystemPropertyConstants#DEFAULT_ADMIN_TIMEOUT_PROPERTY} or the
     * value {@link SystemPropertyConstants#DEFAULT_ADMIN_TIMEOUT_VALUE} if the system property not set.
     */
    public static final Integer getDefaultAdminTimeout() {
        final Integer result = Integer.getInteger(DEFAULT_ADMIN_TIMEOUT_PROPERTY);
        if (result == null) {
            return DEFAULT_ADMIN_TIMEOUT_VALUE;
        }
        return result;
    }
}
