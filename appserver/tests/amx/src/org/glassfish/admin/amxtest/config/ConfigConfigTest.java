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

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.SystemInfo;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.util.misc.StringUtil;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.MultipleServerSupportRequired;

import java.util.Map;


/**
 */
public final class ConfigConfigTest
        extends AMXTestBase
        implements MultipleServerSupportRequired {
    public ConfigConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getDomainRoot());
        }

    }

    public static ConfigConfig
    ensureDefaultInstance(final DomainRoot domainRoot) {
        ConfigConfig config = null;

        final DomainConfig domainConfig = domainRoot.getDomainConfig();
        final Map<String, ConfigConfig> existing = domainConfig.getConfigsConfig().getConfigConfigMap();

        if (supportsMultipleServers(domainRoot)) {
            config = existing.get(getDefaultInstanceName(domainRoot));
            if (config == null) {
                final ConfigSetup setup = new ConfigSetup(domainRoot);
                config = setup.createConfig(getDefaultInstanceName(domainRoot));
            }
        } else {
            config = existing.get(PE_CONFIG_NAME);
            assert (config != null) : "No config named " + StringUtil.quote(PE_CONFIG_NAME);
        }
        return config;
    }

    public static String
    getDefaultInstanceName(final DomainRoot domainRoot) {
        String name = null;

        if (domainRoot.getSystemInfo().supportsFeature(SystemInfo.MULTIPLE_SERVERS_FEATURE)) {
            name = getDefaultInstanceName("ConfigConfigTest");
        } else {
            name = PE_CONFIG_NAME;
        }
        return name;
    }


    private ConfigConfig
    create(final String name)
            throws Throwable {
        final ConfigSetup setup = new ConfigSetup(getDomainRoot());

        setup.removeConfig(name);

        final ConfigConfig config = setup.createConfig(name);
        assert (name.equals(config.getName()));

        // see that it responds to a request
        final Map<String, Object> attrs = Util.getExtra(config).getAllAttributes();
        //printVerbose( "Attributes for config " + config.getName() + ":" );
        //printVerbose( MapUtil.toString( attrs, NEWLINE ) );

        return config;
    }

    public void
    testCreateRemove()
            throws Throwable {
        if (!checkNotOffline("testCreateRemove")) {
            return;
        }

        final String NAME = "ConfigConfigTest.testCreateRemove";

        final Map<String, ConfigConfig> before = getDomainConfig().getConfigsConfig().getConfigConfigMap();

        final int NUM = 2;
        final ConfigConfig[] configs = new ConfigConfig[NUM];

        for (int i = 0; i < NUM; ++i) {
            configs[i] = create(NAME + i);
        }

        final ConfigSetup setup = new ConfigSetup(getDomainRoot());
        for (final ConfigConfig config : configs) {
            setup.removeConfig(config.getName());

            // verify that the config is gone
            try {
                Util.getExtra(config).getAllAttributes();
                fail("Config " + config.getName() + " should no longer exist");
            }
            catch (Exception e) {
                // good, we expected to be here
            }
        }

        final Map<String, ConfigConfig> after = getDomainConfig().getConfigsConfig().getConfigConfigMap();
        assert (before.keySet().equals(after.keySet()));
    }
}



























