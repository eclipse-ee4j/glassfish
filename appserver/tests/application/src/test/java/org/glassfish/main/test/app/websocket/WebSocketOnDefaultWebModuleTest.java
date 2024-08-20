/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.glassfish.main.test.app.websocket;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.HttpListenerType.HTTP;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.webSocketUri;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class WebSocketOnDefaultWebModuleTest {

    private static final System.Logger LOG = System.getLogger(WebSocketOnDefaultWebModuleTest.class.getName());

    private static final int HTTP_PORT = GlassFishTestEnvironment.getPort(HTTP);

    private static final String WEBAPP_FILE_NAME = "webapp.war";

    private static final String WEBAPP_NAME = "webapp1";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File webAppDir;

    @BeforeAll
    public static void deployAll() throws IOException {
        File webApp = createWebApp();

        AsadminResult result = ASADMIN.exec("deploy",
                "--contextroot", "/" + WEBAPP_NAME,
                "--name", WEBAPP_NAME,
                webApp.getAbsolutePath());
        assertThat(result, asadminOK());

        result = ASADMIN.exec("set",
                "server-config.http-service.virtual-server.server.default-web-module=" + WEBAPP_NAME);
        assertThat(result, asadminOK());
    }

    @AfterAll
    public static void undeployAll() {
        assertAll(
                () -> assertThat(ASADMIN.exec("set",
                        "server-config.http-service.virtual-server.server.default-web-module="), asadminOK()),
                () -> assertThat(ASADMIN.exec("undeploy", WEBAPP_NAME), asadminOK())
        );
    }

    @Test
    public void testWebSocketOnDefaultWebModule() throws IOException, URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        final WebSocketClient webSocketClient = new WebSocketClient(webSocketUri(HTTP_PORT, "/hello"));
        webSocketClient.sendMessage("Hello");
        CompletableFuture<String> waitForMessage = new CompletableFuture<>();
        webSocketClient.addMessageHandler(msg -> waitForMessage.complete(msg));

        final String message = waitForMessage.get(10, TimeUnit.SECONDS);

        assertThat(message, equalTo("World"));
    }

    @Test
    public void testWebSocketOnAppContextRoot() throws IOException, URISyntaxException, InterruptedException, ExecutionException, TimeoutException {

        final WebSocketClient webSocketClient = new WebSocketClient(webSocketUri(HTTP_PORT, "/" + WEBAPP_NAME + "/hello"));
        webSocketClient.sendMessage("Hello");
        CompletableFuture<String> waitForMessage = new CompletableFuture<>();
        webSocketClient.addMessageHandler(msg -> waitForMessage.complete(msg));

        final String message = waitForMessage.get(10, TimeUnit.SECONDS);

        assertThat(message, equalTo("World"));
    }

    private static File createWebApp() throws IOException {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
                .addClass(HelloWebSocketEndpoint.class);

        LOG.log(INFO, webArchive.toString(true));

        File webApp = new File(webAppDir, WEBAPP_FILE_NAME);
        webArchive.as(ZipExporter.class).exportTo(webApp, true);
        return webApp;
    }
}
