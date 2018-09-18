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
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.HTTPAccessLogConfig;
import com.sun.appserv.management.config.HTTPServiceConfig;
import com.sun.appserv.management.config.VirtualServerConfig;
import com.sun.appserv.management.config.VirtualServerConfigKeys;
import com.sun.appserv.management.util.misc.MapUtil;

import java.util.HashMap;
import java.util.Map;

/**
 */
public final class VirtualServerConfigTest
        extends ConfigMgrTestBase {
    static final String HOSTS = "localhost";

    public VirtualServerConfigTest() {
    }

    protected Container
    getProgenyContainer() {
        return getHTTPService();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.VIRTUAL_SERVER_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        getHTTPService().removeVirtualServerConfig(name);
    }

    protected final VirtualServerConfig
    create(String name) {
        return (VirtualServerConfig) createProgeny(name, null);
    }

    private Map<String, String>
    getOptional() {
        final Map<String, String> m = new HashMap<String, String>();
        m.put(VirtualServerConfigKeys.STATE_KEY, VirtualServerConfigKeys.STATE_DISABLED);
        m.put(VirtualServerConfigKeys.DOC_ROOT_PROPERTY_KEY, "/");
        m.put(VirtualServerConfigKeys.ACCESS_LOG_PROPERTY_KEY, "/");

        return m;
    }

    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> extra) {
        final Map<String, String> allOptions = MapUtil.newMap(extra, getOptional());

        return (getHTTPService().createVirtualServerConfig(name, "localhost", allOptions));
    }

    protected final HTTPServiceConfig
    getHTTPService() {
        return getConfigConfig().getHTTPServiceConfig();
    }

    public void
    testCreateHTTPAccessLog()
            throws Exception {
        if (!checkNotOffline("testCreateRemove")) {
            return;
        }

        final String NAME = "VirtualServerConfigMgrTest-testCreateHTTPAccessLog";
        try {
            removeEx(NAME);
            final VirtualServerConfig newVS =
                    (VirtualServerConfig) createProgeny(NAME, null);
            assert newVS != null;
            //trace( "newVS.getState: " + newVS.getState() );
            // assert newVS.getState().equals("disabled");

            assert (newVS.getHTTPAccessLogConfig() == null);

            final HTTPAccessLogConfig accessLog =
                    newVS.createHTTPAccessLogConfig("false", "${com.sun.aas.instanceRoot}/logs/access", null);
            assert (accessLog != null);
            assert (Util.getObjectName(accessLog).equals(Util.getObjectName(newVS.getHTTPAccessLogConfig())));

            newVS.removeHTTPAccessLogConfig();
        }
        finally {
            remove(NAME);
        }
    }
}


