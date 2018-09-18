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
import com.sun.appserv.management.config.SystemPropertiesAccess;
import com.sun.appserv.management.config.SystemPropertyConfig;
import org.glassfish.admin.amxtest.AMXTestBase;

import javax.management.ObjectName;
import java.util.Map;
import java.util.Set;

/**
 */
public final class SystemPropertiesAccessTest
        extends AMXTestBase {
    public SystemPropertiesAccessTest() {
    }

    private Set<ObjectName>
    getAll()
            throws Exception {
        final Set<ObjectName> objectNames =
                getQueryMgr().queryInterfaceObjectNameSet(
                        SystemPropertiesAccess.class.getName(), null);

        return (objectNames);
    }


    private void
    checkPropertiesGet(final SystemPropertiesAccess props) {
        final Map<String, SystemPropertyConfig> all = props.getSystemPropertyConfigMap();

        for (final SystemPropertyConfig prop : all.values() ) {
            final String value = prop.getValue();
        }
    }

    private void
    testPropertiesSetToSameValue(final SystemPropertiesAccess props) {
        final Map<String, SystemPropertyConfig> all = props.getSystemPropertyConfigMap();

        // get each property, set it to the same value, the verify
        // it's the same.
        for ( final SystemPropertyConfig prop : all.values() ) {

            final String value = prop.getValue();
            prop.setValue(value);

            assert prop.getValue().equals(value);
        }
    }

    private void
    testCreateEmptySystemProperty(final SystemPropertiesAccess props) {
        final String NAME = "test.empty";

        props.createSystemPropertyConfig(NAME, "");
        assert props.getSystemPropertyConfigMap().get(NAME) != null;
        props.removeSystemPropertyConfig(NAME);
        assert props.getSystemPropertyConfigMap().get(NAME) == null;
    }

    private void
    testSystemPropertiesCreateRemove(final SystemPropertiesAccess props) {
        final Map<String, SystemPropertyConfig> all = props.getSystemPropertyConfigMap();

        // add some properties, then delete them
        final int numToAdd = 1;
        final long now = System.currentTimeMillis();
        for (int i = 0; i < numToAdd; ++i) {
            final String testName = "__junittest_" + i + now;

            if ( all.get(testName) != null) {
                failure("test property already exists: " + testName);
            }

            props.createSystemPropertyConfig(testName, "value_" + i);
            assert props.getSystemPropertyConfigMap().get(testName) != null;
        }
        final int numProps = props.getSystemPropertyConfigMap().keySet().size();

        if (numProps != numToAdd + all.keySet().size() ) {
            failure("expecting " + numProps + " have " + numToAdd + all.keySet().size());
        }

        // remove the ones we added
        for (int i = 0; i < numToAdd; ++i) {
            final String testName = "__junittest_" + i + now;

            props.removeSystemPropertyConfig(testName);
            assert props.getSystemPropertyConfigMap().get(testName) == null;
        }

        assert (props.getSystemPropertyConfigMap().keySet().size() == all.keySet().size() );

    }

    public synchronized void
    checkGetProperties(final ObjectName src)
            throws Exception {
        final AMX proxy = getProxy(src, AMX.class);

        if (!(proxy instanceof SystemPropertiesAccess)) {
            throw new IllegalArgumentException(
                    "MBean does not implement SystemPropertiesAccess: " + quote(src));
        }

        final SystemPropertiesAccess props = (SystemPropertiesAccess) proxy;
        checkPropertiesGet(props);
    }

    public void
    checkSetPropertiesSetToSameValue(final ObjectName src)
            throws Exception {
        final SystemPropertiesAccess props = getProxy(src, SystemPropertiesAccess.class);

        testPropertiesSetToSameValue(props);
    }


    public void
    checkCreateRemove(final ObjectName src)
            throws Exception {
        final SystemPropertiesAccess props =
                getProxy(src, SystemPropertiesAccess.class);

        testSystemPropertiesCreateRemove(props);
    }

    public synchronized void
    testPropertiesGet()
            throws Exception {
        final Set<ObjectName> all = getAll();

        testAll(all, "checkGetProperties");
    }

    public synchronized void
    testPropertiesSetToSameValue()
            throws Exception {
        final Set<ObjectName> all = getAll();

        testAll(all, "checkSetPropertiesSetToSameValue");
    }


    public synchronized void
    testCreateRemove()
            throws Exception {
        if (checkNotOffline("testCreateRemove")) {
            final Set<ObjectName> all = getAll();
            testAll(all, "checkCreateRemove");
        }
    }

}


