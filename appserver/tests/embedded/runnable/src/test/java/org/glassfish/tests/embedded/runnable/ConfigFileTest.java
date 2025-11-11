/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.tests.embedded.runnable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.glassfish.tests.embedded.runnable.tool.TestArgumentProviders.GfEmbeddedJarNameProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.glassfish.tests.embedded.runnable.tool.BufferedReaderMatcher.readerContains;
import static org.glassfish.tests.embedded.runnable.tool.EmbeddedGlassFishStarter.start;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Ondro Mihalyi
 */
public class ConfigFileTest {

    @TempDir
    private Path tmpDir;
    private Path propertiesFile;

    @BeforeEach
    void init(TestInfo testInfo) throws IOException {
        String fileName = "glassfish-" + testInfo.getTestMethod().get().getName() + ".properties";
        propertiesFile = tmpDir.resolve(fileName);
        Files.copy(getClass().getClassLoader().getResourceAsStream(
            testInfo.getTestClass().get().getSimpleName() + "/" + fileName), propertiesFile, REPLACE_EXISTING);
    }

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testExecuteCommandsInOrderDefinedInPropertiesFile(String gfEmbeddedJarName) throws Exception {
        Process glassfish = start(gfEmbeddedJarName, "--properties=" + propertiesFile,
            "get server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size");
        assertThat(glassfish.errorReader(),
            readerContains("server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=200"));
        assertTrue(glassfish.waitFor(30, SECONDS), "Process finished.");
    }

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testPropertyNamesVariants(String gfEmbeddedJarName) throws Exception {
        Process glassfish = start(gfEmbeddedJarName, "--properties=" + propertiesFile,
            "get server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size",
            "get resources.jdbc-connection-pool.DerbyPool.connection-leak-timeout-in-seconds",
            "get server.network-config.protocols.protocol.http-listener.http.max-connections",
            "get security-configurations.authorization-service.authorizationService.default"
        );
        assertThat(glassfish.errorReader(),
            readerContains("server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=200",
                "resources.jdbc-connection-pool.DerbyPool.connection-leak-timeout-in-seconds=5",
                "server.network-config.protocols.protocol.http-listener.http.max-connections=10000",
                "security-configurations.authorization-service.authorizationService.default=false"));
        assertTrue(glassfish.waitFor(30, SECONDS), "Process finished.");
    }
}
