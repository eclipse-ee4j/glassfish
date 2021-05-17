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


import com.sun.appserv.management.config.AvailabilityServiceConfig;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.ClusterSupportRequired;

import javax.management.InstanceNotFoundException;

import com.sun.appserv.management.helper.AttributeResolverHelper;

public class AvailabilityServiceConfigTest
        extends AMXTestBase
        implements ClusterSupportRequired {
    public AvailabilityServiceConfigTest()
            throws InstanceNotFoundException {
    }

    private AvailabilityServiceConfig
    getIt() {
        return getConfigConfig().getAvailabilityServiceConfig();
    }

    public void
    testWarnAvail() {
        if (getIt() == null) {
            assert false : "AvailabilityServiceConfigTest:  no AvailabilityServiceConfig to test";
        }
    }

    /**
     Test of [g/s]etAvailabilityEnabled method, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
     */
    public void testAvailabilityEnabled() {
        final AvailabilityServiceConfig  asc = getIt();
        if (asc != null) {
            asc.setAvailabilityEnabled("" + false);
            assertFalse("getAvailabilityEnabled() was supposed to return false.", asc.getAvailabilityEnabled().equals("false"));
            asc.setAvailabilityEnabled( ""+ true);
            assertTrue("getAvailabilityEnabled() was supposed to return true.", asc.getAvailabilityEnabled().equals("true"));
        }
    }

    /**
     Test of [g/s]etAutoManageHAStore method, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
     */
    public void testAutoManageHAStore() {
        final AvailabilityServiceConfig  asc = getIt();
        if (asc != null) {
            final String save = asc.getAutoManageHAStore();
            asc.setAutoManageHAStore("" + true);
            assertTrue("getAutoManageHAStore() was supposed to return true.", asc.getAutoManageHAStore().equals("true"));

            asc.setAutoManageHAStore("" + false);
            assertFalse("getAutoManageHAStore() was supposed to return false.", asc.getAutoManageHAStore().equals("false"));
            asc.setAutoManageHAStore(save);
        }
    }

    /**
     Test of [g/s]etHAAgentHosts methods, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
     */
    public void testHAAgentHosts() {
        final AvailabilityServiceConfig  asc = getIt();
        if (asc != null) {
            final String hosts = "hp,hp,hp,hp";
            final String save = asc.getHAAgentHosts();
            asc.setHAAgentHosts(hosts);
            String s = asc.getHAAgentHosts();
            assertEquals(hosts, s);
            asc.setHAAgentHosts((save == null) ? "" : save);
        }
    }

    /**
     Test of [g/s]etHAAgentPort methods, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
     */
    public void testHAAgentPort() {
        final AvailabilityServiceConfig  asc = getIt();
        if (asc != null) {
            final String port = "3456";
            final String save = asc.getHAAgentPort();
            asc.setHAAgentPort(port);
            final String s = asc.getHAAgentPort();
            assertEquals(port, s);
            asc.setHAAgentPort((save == null) ? "" : save);
        }
    }

    /**
     Test of [g/s]etHAStoreHealthcheckIntervalSeconds methods, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
     */
    public void testHAStoreHealthcheckIntervalSeconds() {
        final AvailabilityServiceConfig  asc = getIt();
        if (asc != null) {
            final String time = "90";
            final String save = asc.getHAStoreHealthcheckIntervalSeconds();
            asc.setHAStoreHealthcheckIntervalSeconds(time);
            String s = asc.getHAStoreHealthcheckIntervalSeconds();
            assertEquals(time, s);
            asc.setHAStoreHealthcheckIntervalSeconds((save == null) ? "" : save);
        }
    }

    /**
     Test of [g/s]etHAStoreName methods, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
     */
    public void testHAStoreName() {
        final AvailabilityServiceConfig  asc = getIt();
        if (asc != null) {
            final String storeName = "cluster1";
            final String save = asc.getHAStoreName();
            asc.setHAStoreName(storeName);
            final String s = asc.getHAStoreName();
            assertEquals(storeName, s);
            asc.setHAStoreName((save == null) ? "" : save);
        }
    }

    /**
     Test of [g/s]etStorePoolName methods, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
     */
    public void testStorePoolName() {
        final AvailabilityServiceConfig  asc = getIt();
        if (asc!= null) {
            final String storeName = "xxxx";
            final String save = asc.getStorePoolName();
            asc.setStorePoolName(storeName);
            final String s = asc.getStorePoolName();
            assertEquals(storeName, s);
            asc.setStorePoolName((save == null) ? "" : save);
        }
    }

    /**
     Test of [g/s]etHAStoreHealthcheckEnabled methods, of class com.sun.appserv.management.config.AvailabilityServiceConfig.
     */
    public void testHAStoreHealthcheckEnabled() {
        final AvailabilityServiceConfig  asc = getIt();
        if (asc!= null) {
            final String save = asc.getHAStoreHealthcheckEnabled();
            final boolean b = AttributeResolverHelper.resolveBoolean( asc, save);

            asc.setHAStoreHealthcheckEnabled("" + false);
            assertFalse("getHAStoreHealthcheckEnabled() was supposed to return false.",
                AttributeResolverHelper.resolveBoolean( asc, asc.getHAStoreHealthcheckEnabled()));

            asc.setHAStoreHealthcheckEnabled("" + true);
            assertTrue("getHAStoreHealthcheckEnabled() was supposed to return true.",
                AttributeResolverHelper.resolveBoolean( asc, asc.getHAStoreHealthcheckEnabled()));
            asc.setHAStoreHealthcheckEnabled( save );
        }
    }
}


