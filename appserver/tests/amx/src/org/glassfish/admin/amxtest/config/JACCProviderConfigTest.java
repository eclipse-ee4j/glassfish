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

package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.JACCProviderConfig;
import com.sun.appserv.management.config.SecurityServiceConfig;

import java.util.Map;

/**
 */
public final class JACCProviderConfigTest
        extends ConfigMgrTestBase {
    static final String PROVIDER = "com.sun.enterprise.security.jacc.provider.SimplePolicyProvider";
    static final String PROVIDER_FACTORY = "com.sun.enterprise.security.jacc.provider.SimplePolicyConfigurationFactory";
    static final Map<String, String> RESERVED = null;

    public JACCProviderConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getConfigConfig().getSecurityServiceConfig());
        }
    }

    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("JACCProviderConfig");
    }

    public static JACCProviderConfig
    ensureDefaultInstance(final SecurityServiceConfig ss) {
        JACCProviderConfig result = ss.getJACCProviderConfigMap().get(getDefaultInstanceName());

        if (result == null) {
            result = createInstance(ss, getDefaultInstanceName(), PROVIDER, PROVIDER_FACTORY);
        }

        return result;
    }

    public static JACCProviderConfig
    createInstance(
            final SecurityServiceConfig ss,
            final String name,
            final String policyProvider,
            final String policyConfigurationFactoryProvider) {
        return ss.createJACCProviderConfig(name,
                                           policyProvider, policyConfigurationFactoryProvider, null);
    }


    protected Container
    getProgenyContainer() {
        return getConfigConfig().getSecurityServiceConfig();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.JACC_PROVIDER_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        getConfigConfig().getSecurityServiceConfig().removeJACCProviderConfig(name);
    }

    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        return getConfigConfig().getSecurityServiceConfig().createJACCProviderConfig(name, PROVIDER, PROVIDER_FACTORY, options);
    }
}


