/*
 * Copyright (c) 2021, 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.extras.embedded.test.all;

import java.io.File;
import java.net.URL;

import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFish.Status;
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
 * @author David Matejcek
 */
public class StartupITest {

    private static final String WEBAPP_NAME = TestServlet.class.getSimpleName() + "WebApp";
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
    public void startAndStopNoPorts() throws Exception {
        final BootstrapProperties bootProps = new BootstrapProperties();
        runtime = GlassFishRuntime.bootstrap(bootProps);
        final GlassFish glassfish = runtime.newGlassFish(new GlassFishProperties());
        assertEquals(Status.INIT, glassfish.getStatus());
        glassfish.start();
        while (glassfish.getStatus() == Status.STARTING) {
            Thread.yield();
        }
        assertEquals(Status.STARTED, glassfish.getStatus());
        glassfish.stop();
        while (glassfish.getStatus() == Status.STOPPING) {
            Thread.yield();
        }
        assertEquals(Status.STOPPED, glassfish.getStatus());
    }


    @Test
    public void startAndStopWithListenerAndDeploy() throws Exception {
        runtime = GlassFishRuntime.bootstrap();
        GlassFishProperties props = new GlassFishProperties();
        props.setPort("http-listener", HTTP_PORT);
        GlassFish glassfish = runtime.newGlassFish(props);
        glassfish.start();
        Deployer deployer = glassfish.getDeployer();

        File war = new File(tempDir, WEBAPP_NAME + ".war");
        ServerUtils.createWar(war, TestServlet.class);
        String result = deployer.deploy(war);
        assertEquals(WEBAPP_NAME, result);
        URL url = new URL("http", ServerUtils.getLocalIP4Address(), HTTP_PORT, "/" + WEBAPP_NAME);
        assertEquals(RESPONSE_TEXT, ServerUtils.download(url));
    }
}
