/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.glassfish.main.extras.embedded.test.all.mphealth;

import jakarta.json.Json;
import jakarta.json.JsonReader;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.tests.utils.ServerUtils;
import org.glassfish.tests.utils.example.TestServlet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.glassfish.tests.utils.example.TestServlet.RESPONSE_TEXT;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that the MicroProfile Health endpoint of Embedded GlassFish works with an application
 * that does not use CDI.
 *
 * @see <a href="https://github.com/eclipse-ee4j/glassfish/issues/26255">issue 26255</a>
 */
public class MpHealthITest {

    private static final String WEBAPP_NAME = MpHealthITest.class.getSimpleName() + "WebApp";
    private static final int HTTP_PORT = ServerUtils.getFreePort();

    private GlassFishRuntime runtime;

    @TempDir
    private File tempDir;

    @AfterEach
    public void shutdown() throws Exception {
        if (runtime != null) {
            runtime.shutdown();
        }
    }

    @Test
    public void healthWithoutCdiApplication() throws Exception {
        runtime = GlassFishRuntime.bootstrap();
        GlassFishProperties props = new GlassFishProperties();
        props.setPort("http-listener", HTTP_PORT);
        GlassFish glassfish = runtime.newGlassFish(props);
        glassfish.start();

        File war = new File(tempDir, WEBAPP_NAME + ".war");
        ServerUtils.createWar(war, TestServlet.class);
        assertEquals(WEBAPP_NAME, glassfish.getDeployer().deploy(war));
        assertEquals(RESPONSE_TEXT, ServerUtils.download(toURL("/" + WEBAPP_NAME)));

        HttpURLConnection connection = (HttpURLConnection) toURL("/health").openConnection();
        try {
            assertEquals(200, connection.getResponseCode());
            try (InputStream input = connection.getInputStream(); JsonReader reader = Json.createReader(input)) {
                assertEquals("UP", reader.readObject().getString("status"));
            }
        } finally {
            connection.disconnect();
        }
    }

    private static URL toURL(String path) throws Exception {
        return new URI("http", null, "localhost", HTTP_PORT, path, null, null).toURL();
    }
}
