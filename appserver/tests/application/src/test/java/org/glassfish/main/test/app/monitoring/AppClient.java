/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.monitoring;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.net.HttpURLConnection;
import java.util.UUID;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;

import static java.lang.System.Logger.Level.DEBUG;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Stateless HTTP application client.
 */
final class AppClient {
    private static final Logger LOG = System.getLogger(AppClient.class.getName());
    private static final String APP_CONTEXT = "/threadpool-test";
    private static final String CONTEXT_FAST = APP_CONTEXT + "/test";
    private static final String CONTEXT_LOCK = APP_CONTEXT + "/lock?action=lock";
    private static final String CONTEXT_UNLOCK = APP_CONTEXT + "/lock?action=unlock";
    private static final String CONTEXT_COUNT_LOCKS = APP_CONTEXT + "/lock?action=count";

    private final int port;
    private final int requestTimeout;


    AppClient(int port, int requestTimeout) {
        this.port = port;
        this.requestTimeout = requestTimeout;
    }

    String test() {
        return httpGet(CONTEXT_FAST);
    }

    String lock(final UUID lockId) {
        return httpGet(CONTEXT_LOCK + "&idLock=" + lockId);
    }

    String unlock(final UUID lockId) {
        return httpGet(CONTEXT_UNLOCK + "&idLock=" + lockId);
    }

    int countLocks() {
        return Integer.parseInt(httpGet(CONTEXT_COUNT_LOCKS).strip());
    }

    private String httpGet(String context) {
        final HttpURLConnection conn = openHttpGetConnection(context);
        try {
            assertEquals(200, conn.getResponseCode(), "HTTP Response Code");
            try (InputStream output = conn.getInputStream()) {
                final String response = new String(output.readAllBytes(), UTF_8);
                LOG.log(DEBUG, () -> "Response from " + context + " \n" + response);
                return response;
            }
        } catch (IOException e) {
            LOG.log(DEBUG, () -> "Error response when querying " + context, e);
            throw new IllegalStateException(e);
        } finally {
            conn.disconnect();
        }
    }

    private HttpURLConnection openHttpGetConnection(String context) {
        try {
            HttpURLConnection connection = GlassFishTestEnvironment.openConnection(port, context);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setConnectTimeout(100);
            connection.setReadTimeout(requestTimeout);
            connection.connect();
            return connection;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open connection to " + context, e);
        }
    }
}
