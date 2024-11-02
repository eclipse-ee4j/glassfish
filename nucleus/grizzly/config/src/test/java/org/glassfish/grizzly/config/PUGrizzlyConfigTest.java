/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.glassfish.grizzly.config.test.GrizzlyConfigTestHelper;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created Jan 5, 2009
 *
 * @author <a href="mailto:justin.d.lee@oracle.com">Justin Lee</a>
 */
public class PUGrizzlyConfigTest {

    private static final GrizzlyConfigTestHelper helper = new GrizzlyConfigTestHelper(PUGrizzlyConfigTest.class);

    private static int count;

    @Test
    public void puConfig() throws Exception {
        GrizzlyConfig grizzlyConfig = null;
        try {
            grizzlyConfig = new GrizzlyConfig("grizzly-config-pu.xml");
            grizzlyConfig.setupNetwork();
            for (GrizzlyListener listener : grizzlyConfig.getListeners()) {
                helper.addStaticHttpHandler((GenericGrizzlyListener) listener, count++);
            }
            final String httpContent = helper.getContent(new URL("http://localhost:38082").openConnection());
            assertEquals("<html><body>You've found the server on port 38082</body></html>", httpContent);

            final String xProtocolContent = getXProtocolContent("localhost", 38082);
            assertEquals("X-Protocol-Response", xProtocolContent);
        } finally {
            if (grizzlyConfig != null) {
                grizzlyConfig.shutdownNetwork();
            }
        }
    }

    @Test
    public void puHttpHttpsSamePortConfig() throws Exception {
        GrizzlyConfig grizzlyConfig = null;
        try {
            grizzlyConfig = new GrizzlyConfig("grizzly-config-pu-http-https-same-port.xml");
            grizzlyConfig.setupNetwork();
            for (GrizzlyListener listener : grizzlyConfig.getListeners()) {
                helper.addStaticHttpHandler((GenericGrizzlyListener) listener, count++);
            }
            final String httpContent1 = helper.getContent(new URL("http://localhost:38082").openConnection());
            assertEquals("<html><body>You've found the server on port 38082</body></html>", httpContent1);

            HttpsURLConnection.setDefaultSSLSocketFactory(helper.getSSLSocketFactory());
            final String httpContent2 = helper.getContent(new URL("https://localhost:38082").openConnection());
            assertEquals("<html><body>You've found the server on port 38082</body></html>", httpContent2);
        } finally {
            if (grizzlyConfig != null) {
                grizzlyConfig.shutdownNetwork();
            }
        }
    }

    @Test
    public void wrongPuConfigLoop() throws IOException, InstantiationException {
        GrizzlyConfig grizzlyConfig = null;
        boolean isIllegalState = false;
        try {
            grizzlyConfig = new GrizzlyConfig("grizzly-config-pu-loop.xml");
            grizzlyConfig.setupNetwork();
        } catch (IllegalStateException e) {
            isIllegalState = true;
        } finally {
            if (grizzlyConfig != null) {
                grizzlyConfig.shutdownNetwork();
            }
        }

        assertTrue(isIllegalState, "Double http definition should throw IllegalStateException");
    }


    private String getXProtocolContent(String host, int port) throws IOException {
        try (Socket s = new Socket(host, port); OutputStream os = s.getOutputStream();) {
            os.write("X-protocol".getBytes(UTF_8));
            os.flush();
            try (InputStream is = s.getInputStream(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                int b;
                while ((b = is.read()) != -1) {
                    baos.write(b);
                }
                return new String(baos.toByteArray(), UTF_8);
            }
        }
    }
}
