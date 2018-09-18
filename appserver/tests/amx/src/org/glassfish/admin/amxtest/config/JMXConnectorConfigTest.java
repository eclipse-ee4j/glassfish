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

/*
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/config/JMXConnectorConfigTest.java,v 1.7 2007/05/05 05:23:54 tcfujii Exp $
* $Revision: 1.7 $
* $Date: 2007/05/05 05:23:54 $
*/
package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.AdminServiceConfig;
import com.sun.appserv.management.config.AuthRealmConfig;
import com.sun.appserv.management.config.JMXConnectorConfig;
import com.sun.appserv.management.config.JMXConnectorConfigKeys;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.SSLConfig;
import com.sun.appserv.management.config.SecurityServiceConfig;
import com.sun.appserv.management.util.misc.MapUtil;

import java.util.HashMap;
import java.util.Map;

/**
 */
public final class JMXConnectorConfigTest
        extends ConfigMgrTestBase {
    static final String ADDRESS = "0.0.0.0";
    static final String TEST_REALM_CLASS = "com.test.DUMMY";
    static final String DEFAULT_PORT = "17377";

    static final Map<String, String> OPTIONAL = new HashMap<String, String>();

    static {
        OPTIONAL.put(PropertiesAccess.PROPERTY_PREFIX + "xyz", "abc");
        OPTIONAL.put(JMXConnectorConfigKeys.SECURITY_ENABLED_KEY, "false");
    }

    public JMXConnectorConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getConfigConfig().getAdminServiceConfig());
        }
    }

    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("JMXConnectorConfig");
    }

    public static JMXConnectorConfig
    ensureDefaultInstance(final AdminServiceConfig adminServiceConfig) {
        JMXConnectorConfig result =
                adminServiceConfig.getJMXConnectorConfigMap().get(getDefaultInstanceName());

        if (result == null) {
            final SecurityServiceConfig securityServiceConfig =
                    getConfigConfig(adminServiceConfig).getSecurityServiceConfig();

            final AuthRealmConfig defaultAuthRealm =
                    AuthRealmConfigTest.ensureDefaultInstance(securityServiceConfig);

            result = createInstance(getDefaultInstanceName(),
                                    ADDRESS, DEFAULT_PORT, defaultAuthRealm, OPTIONAL);
        }

        return result;
    }

    public static JMXConnectorConfig
    createInstance(
            final String name,
            final String address,
            final String port,
            final AuthRealmConfig authRealm,
            final Map<String, String> optional) {
        final AdminServiceConfig adminServiceConfig =
                getConfigConfig(authRealm).getAdminServiceConfig();

        return adminServiceConfig.createJMXConnectorConfig(name,
                                                           address, port, authRealm.getName(), optional);
    }


    protected Container
    getProgenyContainer() {
        return getAdminServiceConfig();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.JMX_CONNECTOR_CONFIG;
    }

    final SecurityServiceConfig
    getSecurityServiceConfig() {
        return getConfigConfig().getSecurityServiceConfig();
    }

    final AuthRealmConfig
    createAuthRealmConfig(final String name) {
        removeAuthRealmConfig(name);

        return getSecurityServiceConfig().createAuthRealmConfig(
                name, TEST_REALM_CLASS, null);
    }

    private String
    createAuthRealmName(final String progenyName) {
        return progenyName + ".TestRealm";
    }

    final void
    removeAuthRealmConfig(final String name) {
        try {
            getSecurityServiceConfig().removeAuthRealmConfig(name);
        }
        catch (Exception e) {
        }
    }

    protected void
    removeProgeny(final String name) {
        try {
            getAdminServiceConfig().removeJMXConnectorConfig(name);
        }
        finally {
            try {
                removeAuthRealmConfig(createAuthRealmName(name));
            }
            catch (Exception e) {
            }
        }
    }

    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        final Map<String, String> allOptions = MapUtil.newMap(options, OPTIONAL);

        final int port = (name.hashCode() % 16000) + 33111;

        final String authRealmName = createAuthRealmName(name);
        final AuthRealmConfig authRealmConfig = createAuthRealmConfig(authRealmName);

        try {
            return getAdminServiceConfig().createJMXConnectorConfig(name,
                                                                    ADDRESS, "" + port, authRealmName, allOptions);
        }
        catch (Exception e) {
            removeAuthRealmConfig(authRealmName);
            throw new RuntimeException(e);
        }
    }

    final AdminServiceConfig
    getAdminServiceConfig() {
        return (getConfigConfig().getAdminServiceConfig());
    }

    public void
    testCreateSSL()
            throws Exception {
        if (!checkNotOffline("testCreateSSL")) {
            return;
        }

        final String NAME = "JMXConnectorConfigTest-testCreateSSL";
        try {
            removeEx(NAME);
            final JMXConnectorConfig newConfig =
                    (JMXConnectorConfig) createProgeny(NAME, null);

            final Map<String, JMXConnectorConfig> jmxConnectors =
                    getAdminServiceConfig().getJMXConnectorConfigMap();

            final JMXConnectorConfig jmxConnector = (JMXConnectorConfig)
                    jmxConnectors.get(NAME);
            assert jmxConnector != null;
            assert jmxConnector == newConfig;

            final String CERT_NICKNAME = NAME + "Cert";

            final SSLConfig ssl = jmxConnector.createSSLConfig(CERT_NICKNAME, null);
            assert ssl != null;
            assert ssl.getCertNickname().equals(CERT_NICKNAME);

            jmxConnector.removeSSLConfig();
        }
        finally {
            remove(NAME);
        }
    }
}


