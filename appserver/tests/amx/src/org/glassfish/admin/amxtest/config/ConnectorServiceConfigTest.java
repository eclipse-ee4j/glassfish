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

import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.ConnectorServiceConfig;
import org.glassfish.admin.amxtest.AMXTestBase;

/**
 */
public final class ConnectorServiceConfigTest
        extends AMXTestBase {
    public ConnectorServiceConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getConfigConfig());
        }
    }

    public static ConnectorServiceConfig
    ensureDefaultInstance(final ConfigConfig configConfig) {
        return configConfig.createConnectorServiceConfig();
    }

    public synchronized void
    testCreateRemove() {
        if (checkNotOffline("testCreateRemove")) {
            final ConfigConfig configConfig = getConfigConfig();

            // remove first, in case it's there
            configConfig.removeConnectorServiceConfig();

            // create and remove
            configConfig.createConnectorServiceConfig();
            configConfig.removeConnectorServiceConfig();

            // leave it there
            ensureDefaultInstance(configConfig);
        }
    }

    public synchronized void
    testGetters() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getConfigConfig());

            final ConnectorServiceConfig cs = getConfigConfig().getConnectorServiceConfig();

            final String value = cs.getShutdownTimeoutInSeconds();
            assert (value != null);

            try {
                final String newValue = "" + (Integer.parseInt(value) + 1);
                cs.setShutdownTimeoutInSeconds(newValue);
                final String after = cs.getShutdownTimeoutInSeconds();
                assert (after.equals(newValue));

                cs.setShutdownTimeoutInSeconds("30");
                cs.setShutdownTimeoutInSeconds(newValue);
            }
            catch (Exception e) {
                warning(getRootCauseStackTrace(e));

                failure("ConnectorServiceConfig.setShutdownTimeoutInSeconds FAILED " +
                        "(bug #6307916 in com.sun.appserv:type=connector-service MBean)");
            }
        }
    }
}










