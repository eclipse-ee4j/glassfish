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

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.PropertyConfig;
import com.sun.appserv.management.util.misc.GSetUtil;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.TestUtil;

import javax.management.ObjectName;
import java.util.Map;
import java.util.Set;

/**
 */
public final class PropertiesAccessTest
        extends AMXTestBase {
    public PropertiesAccessTest() {
    }

    private Set<ObjectName>
    getAllImplementorsOfProperties()
            throws Exception {
        final Set<AMX> amxs = getQueryMgr().queryInterfaceSet(
                PropertiesAccess.class.getName(), null);

        return (TestUtil.newSortedSet(Util.toObjectNames(amxs)));

    }


    private void
    testCreateEmptyProperty(final PropertiesAccess props) {
        final String NAME = "test.empty";

        final PropertyConfig pc = props.createPropertyConfig(NAME, "");
        assert props.getPropertyConfigMap().get(NAME) != null;
        props.removePropertyConfig(NAME);
        assert props.getPropertyConfigMap().get(NAME) == null;
    }

    private void
    testPropertiesGet(final PropertiesAccess props) {
        final Map<String, PropertyConfig> all = props.getPropertyConfigMap();

        for (final PropertyConfig prop : all.values() ) {
            final String name = prop.getName();
            final String value = prop.getValue();
        }
    }

    private void
    testPropertiesSetToSameValue(final PropertiesAccess props) {
        final Map<String, PropertyConfig> all = props.getPropertyConfigMap();

        // get each property, set it to the same value, the verify
        // it's the same.
        for ( final PropertyConfig prop : all.values() ) {

            final String value = prop.getValue();
            prop.setValue(value);

            assert (prop.getValue().equals(value));
        }
    }

    /**
     Adding or removing test properties to these types does not
     cause any side effects.  Plus, there is no need to test
     every MBean.
     */
    private static final Set<String> TEST_CREATE_REMOVE_TYPES =
            GSetUtil.newUnmodifiableStringSet(
                    XTypes.DOMAIN_CONFIG,
                    XTypes.CONFIG_CONFIG,
                    XTypes.PROFILER_CONFIG,
                    XTypes.STANDALONE_SERVER_CONFIG,
                    XTypes.CLUSTERED_SERVER_CONFIG,
                    XTypes.ORB_CONFIG,
                    XTypes.MODULE_MONITORING_LEVELS_CONFIG,
                    XTypes.NODE_AGENT_CONFIG
            );

    private void
    testPropertiesCreateRemove(final PropertiesAccess props) {

        final AMX amx = Util.asAMX(props);
        final String j2eeType = amx.getJ2EEType();
        if (!TEST_CREATE_REMOVE_TYPES.contains(j2eeType)) {
            return;
        }

        final Map<String, PropertyConfig> startProps = props.getPropertyConfigMap();
        // add some properties, then delete them
        final int numToAdd = 1;
        final long now = System.currentTimeMillis();
        for (int i = 0; i < numToAdd; ++i) {
            final String testName = "__junittest_" + i + now;

            if (props.getPropertyConfigMap().get(testName) != null) {
                failure("test property already exists: " + testName);
            }

            props.createPropertyConfig(testName, "value_" + i);
            assert (props.getPropertyConfigMap().get(testName) != null);
        }
        final int numProps = props.getPropertyConfigMap().keySet().size();

        if (numProps != numToAdd + startProps.keySet().size() ) {
            failure("expecting " + numProps + " have " + numToAdd + startProps.keySet().size());
        }

        // remove the ones we added
        for (int i = 0; i < numToAdd; ++i) {
            final String testName = "__junittest_" + i + now;

            props.removePropertyConfig(testName);
            assert props.getPropertyConfigMap().get(testName) == null;
        }

        assert (props.getPropertyConfigMap().size() == startProps.keySet().size() );
    }

    public void
    checkGetProperties(final ObjectName src)
            throws Exception {
        final AMX proxy = getProxy(src);

        if (!(proxy instanceof PropertiesAccess)) {
            throw new IllegalArgumentException(
                    "MBean does not implement PropertiesAccess: " + quote(src));
        }

        final PropertiesAccess props = (PropertiesAccess) proxy;
        testPropertiesGet(props);
    }

    public void
    checkSetPropertiesSetToSameValue(final ObjectName src)
            throws Exception {
        final PropertiesAccess props = (PropertiesAccess) getProxy(src);

        testPropertiesSetToSameValue(props);
    }


    public void
    checkCreateRemove(final ObjectName src)
            throws Exception {
        final PropertiesAccess props = (PropertiesAccess) getProxy(src);

        testPropertiesCreateRemove(props);
    }

    public synchronized void
    testPropertiesGet()
            throws Exception {
        final Set<ObjectName> all = getAllImplementorsOfProperties();

        testAll(all, "checkGetProperties");
    }

    public synchronized void
    testPropertiesSetToSameValue()
            throws Exception {
        final Set<ObjectName> all = getAllImplementorsOfProperties();

        testAll(all, "checkSetPropertiesSetToSameValue");
    }


    public synchronized void
    testPropertiesCreateRemove()
            throws Exception {
        if (checkNotOffline("testPropertiesCreateRemove")) {
            final Set<ObjectName> all = getAllImplementorsOfProperties();

            testAll(all, "checkCreateRemove");
        }
    }

}


