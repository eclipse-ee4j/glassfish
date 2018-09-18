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

import com.sun.appserv.management.config.AccessLogConfig;
import com.sun.appserv.management.config.ConnectionPoolConfig;
import com.sun.appserv.management.config.HTTPFileCacheConfig;
import com.sun.appserv.management.config.HTTPProtocolConfig;
import com.sun.appserv.management.config.HTTPServiceConfig;
import com.sun.appserv.management.config.KeepAliveConfig;
import com.sun.appserv.management.config.RequestProcessingConfig;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 */
public final class HTTPServiceConfigTest
        extends AMXTestBase {
    public HTTPServiceConfigTest() {
    }

    synchronized final HTTPServiceConfig
    proxy()
            throws IOException {
        return getConfigConfig().getHTTPServiceConfig();
    }

    static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

    public void
    testRequestProcessing()
            throws Exception {
        if (!checkNotOffline("testRequestProcessing")) {
            return;
        }

        RequestProcessingConfig on = proxy().getRequestProcessingConfig();
        boolean exists = (on != null);
        if (!exists) {
            on = proxy().createRequestProcessingConfig(EMPTY_MAP);
            assert on == proxy().getRequestProcessingConfig();
        }
        RequestProcessingConfig rp = proxy().getRequestProcessingConfig();
        assert rp != null;
        if (!exists) {
            proxy().removeRequestProcessingConfig();
        }
    }

    public void
    testKeepAlive()
            throws Exception {
        if (!checkNotOffline("testKeepAlive")) {
            return;
        }

        KeepAliveConfig on = proxy().getKeepAliveConfig();
        boolean exists = (on != null);
        if (!exists) {
            on = proxy().createKeepAliveConfig(EMPTY_MAP);
            assert on == proxy().getKeepAliveConfig();
        }
        KeepAliveConfig rp = proxy().getKeepAliveConfig();
        assert rp != null;
        if (!exists) {
            proxy().removeKeepAliveConfig();
        }
    }

    public void
    testAccessLog()
            throws Exception {
        if (!checkNotOffline("testAccessLog")) {
            return;
        }

        AccessLogConfig on = proxy().getAccessLogConfig();
        boolean exists = (on != null);
        if (!exists) {
            on = proxy().createAccessLogConfig(EMPTY_MAP);
            assert on == proxy().getAccessLogConfig();
        }
        AccessLogConfig rp = proxy().getAccessLogConfig();
        assert rp != null;
        if (!exists) {
            proxy().removeAccessLogConfig();
        }
    }

    public void
    testHTTPFileCache()
            throws Exception {
        if (!checkNotOffline("testHTTPFileCache")) {
            return;
        }

        HTTPFileCacheConfig on = proxy().getHTTPFileCacheConfig();
        boolean exists = (on != null);
        if (!exists) {
            on = proxy().createHTTPFileCacheConfig(EMPTY_MAP);
            assert on == proxy().getHTTPFileCacheConfig();
        }
        HTTPFileCacheConfig rp = proxy().getHTTPFileCacheConfig();
        assert rp != null;
        if (!exists) {
            proxy().removeHTTPFileCacheConfig();
        }
    }

    public void
    testConnectionPool()
            throws Exception {
        if (!checkNotOffline("testConnectionPool")) {
            return;
        }

        ConnectionPoolConfig on = proxy().getConnectionPoolConfig();
        boolean exists = (on != null);
        if (!exists) {
            on = proxy().createConnectionPoolConfig(EMPTY_MAP);
            assert on == proxy().getConnectionPoolConfig();
        }
        ConnectionPoolConfig rp = proxy().getConnectionPoolConfig();
        assert rp != null;
        if (!exists) {
            proxy().removeConnectionPoolConfig();
        }
    }

    public void
    testHTTPProtocol()
            throws Exception {
        if (!checkNotOffline("testHTTPProtocol")) {
            return;
        }

        HTTPProtocolConfig on = proxy().getHTTPProtocolConfig();
        boolean exists = (on != null);
        if (!exists) {
            on = proxy().createHTTPProtocolConfig(EMPTY_MAP);
            assert on == proxy().getHTTPProtocolConfig();
        }
        HTTPProtocolConfig rp = proxy().getHTTPProtocolConfig();
        assert rp != null;
        if (!exists) {
            proxy().removeHTTPProtocolConfig();
        }
    }
}


