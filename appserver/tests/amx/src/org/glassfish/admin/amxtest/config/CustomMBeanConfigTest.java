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

import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.config.CustomMBeanConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.helper.RefHelper;
import com.sun.appserv.management.util.misc.CollectionUtil;
import org.glassfish.admin.amxtest.AMXTestBase;

import javax.management.ObjectName;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public final class CustomMBeanConfigTest
        extends AMXTestBase {
    // built-into server already; use the name; we can't rely on the server jars
    private static final String IMPL_CLASSNAME = "org.glassfish.admin.amx.mbean.TestDummy";

    private static final String TEST_NAME_BASE = "custom";
    private static final String TEST_TYPE = "CustomMBeanConfigTest";

    public CustomMBeanConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getDomainConfig());
        }
    }

    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("CustomMBeanConfig");
    }

    public static CustomMBeanConfig
    ensureDefaultInstance(final DomainConfig domainConfig) {
        CustomMBeanConfig result =
                domainConfig.getResourcesConfig().getCustomMBeanConfigMap().get(getDefaultInstanceName());

        if (result == null) {
            result = createInstance(
                    domainConfig,
                    getDefaultInstanceName(),
                    createProps());
        }

        return result;
    }

    public static CustomMBeanConfig
    createInstance(
            final DomainConfig domainConfig,
            final String name,
            final Map<String, String> optional) {
        final CustomMBeanConfig custom =
                domainConfig.getResourcesConfig().createCustomMBeanConfig(name, IMPL_CLASSNAME,
                                                                          createObjectName(name),
                                                                          "false",
                                                                          optional);

        return custom;
    }


    public Map<String, CustomMBeanConfig>
    getCustomMBeanConfigs() {
        return getDomainConfig().getResourcesConfig().getCustomMBeanConfigMap();
    }

    private void
    _testGetAll() {
        final Map<String, CustomMBeanConfig> all = getCustomMBeanConfigs();
        assert (all != null);
    }

    private void
    sanityCheck(final CustomMBeanConfig config) {
        final String objectName = config.getObjectNameInConfig();

        final String implClassname = config.getImplClassname();
    }

    private synchronized void
    _testAttrs() {
        final Map<String, CustomMBeanConfig> all = getCustomMBeanConfigs();

        if (all.size() != 0) {
            // everything is already tested generically, but we'll
            // do some basic sanity checks here
            for (final CustomMBeanConfig config : all.values()) {
                sanityCheck(config);
            }
        } else {
            warning("CustomMBeanConfigTest: No custom MBeans to test");
        }
    }


    /**
     Create some dummy properties for creating a CustomMBeanConfig
     */
    private static Map<String, String>
    createProps() {
        final String PRP = PropertiesAccess.PROPERTY_PREFIX;    // shorten

        final Map<String, String> optional = new HashMap<String, String>();

        // these must be available in test MBean
        optional.put(PRP + "Attr1", "hello");
        optional.put(PRP + "Attr2", "world");

        return optional;
    }

    private static String
    createObjectName(final String name) {
        return CustomMBeanConfig.JMX_DOMAIN + ":name=" + name +
                ",type=" + TEST_TYPE;
    }

    public synchronized CustomMBeanConfig
    create(
            final DomainConfig domainConfig,
            final String name,
            final Map<String, String> optional) {
        return createInstance(domainConfig, name, optional);
    }

    public synchronized void
    verifyPropsAdded(
            final CustomMBeanConfig config,
            final Map<String, String> props) {
        for (final String key : props.keySet()) {
            if (key.startsWith(PropertiesAccess.PROPERTY_PREFIX)) {
                final String specifiedValue = props.get(key).toString();
                final String propName = key.substring(
                        PropertiesAccess.PROPERTY_PREFIX.length(), key.length());

                final String actualValue = config.getPropertyConfigMap().get(propName).getValue();
                assert (specifiedValue.equals(actualValue));
            }
        }
    }


    private void
    removeCustomMBean(final String name) {
        getDomainConfig().getResourcesConfig().removeCustomMBeanConfig(name);
    }

    private Set<ObjectName>
    getRegisteredCustoms() {
        final QueryMgr queryMgr = getQueryMgr();
        final Set<ObjectName> mbeans =
                queryMgr.queryPatternObjectNameSet(CustomMBeanConfig.JMX_DOMAIN, "type=" + TEST_TYPE);

        return mbeans;
    }

    private void
    unregisterAnyTestMBeans() {
        final Set<ObjectName> customs = getRegisteredCustoms();
        for (final ObjectName objectName : customs) {
            if (TEST_TYPE.equals(objectName.getKeyProperty("type"))) {
                try {
                    getMBeanServerConnection().unregisterMBean(objectName);
                    printVerbose("unregistered: " + objectName);
                }
                catch (Exception e) {
                }
            }
        }
    }

    public synchronized void
    testCreateRemove() {
        if (!checkNotOffline("testCreateRemove")) {
            return;
        }

        final DomainConfig domainConfig = getDomainConfig();

        final Map<String, String> optional = createProps();

        final Set<CustomMBeanConfig> created = new HashSet<CustomMBeanConfig>();

        final Map<String, CustomMBeanConfig> existing =
                getDomainConfig().getResourcesConfig().getCustomMBeanConfigMap();

        unregisterAnyTestMBeans();
        final Set<ObjectName> customsBefore = getRegisteredCustoms();
        if (customsBefore.size() != 0) {
            printVerbose("custom MBeans already registered:\n" +
                    CollectionUtil.toString(customsBefore, "\n"));
        }

        final int NUM = 3;
        for (int i = 0; i < NUM; ++i) {
            final String testName = TEST_NAME_BASE + i;

            if (existing.containsKey(testName)) {
                RefHelper.removeAllRefsTo(existing.get(testName), true);
                // leftover from a failed test...
                removeCustomMBean(testName);
            }

            final CustomMBeanConfig config =
                    create(domainConfig, TEST_NAME_BASE + i, optional);
            //printVerbose( "created: " + Util.getObjectName( config ) );

            assert (getCustomMBeanConfigs().get(config.getName()) == config);

            created.add(config);
            sanityCheck(config);

            verifyPropsAdded(config, optional);
        }

        _testGetAll();
        _testAttrs();

        for (final CustomMBeanConfig config : created) {
            //printVerbose( "removing: " + Util.getObjectName( config ) );
            final String name = config.getName();
            removeCustomMBean(name);

            assert (getCustomMBeanConfigs().get(name) == null);
        }

        _testGetAll();

        mySleep(100);
        final Set<ObjectName> customsAfter = getRegisteredCustoms();
        customsAfter.removeAll(customsBefore);
        if (customsAfter.size() != 0) {
            warning("after removing custom MBeans, " +
                    "they are still registered (not an AMX bug):\n" +
                    CollectionUtil.toString(customsAfter, "\n"));
        }
        unregisterAnyTestMBeans();
    }
}






































