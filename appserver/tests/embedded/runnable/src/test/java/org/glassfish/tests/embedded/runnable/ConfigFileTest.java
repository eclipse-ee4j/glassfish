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
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.glassfish.tests.embedded.runnable.TestArgumentProviders.GfEmbeddedJarNameProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.glassfish.tests.embedded.runnable.GfEmbeddedUtils.outputToStreamOfLines;
import static org.glassfish.tests.embedded.runnable.GfEmbeddedUtils.runGlassFishEmbedded;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Ondro Mihalyi
 */
public class ConfigFileTest {

    String propertiesFile;

    @BeforeEach
    void init(TestInfo testInfo) throws IOException {
        propertiesFile = "glassfish-" + testInfo.getTestMethod().get().getName() + ".properties";
        Files.copy(getClass().getClassLoader().getResourceAsStream(testInfo.getTestClass().get().getSimpleName() + "/" + propertiesFile),
                Paths.get(propertiesFile),
                StandardCopyOption.REPLACE_EXISTING);

    }

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testExecuteCommandsInOrderDefinedInPropertiesFile(String gfEmbeddedJarName) throws Exception {
        Process gfEmbeddedProcess = runGlassFishEmbedded(gfEmbeddedJarName,
                "--properties=" + propertiesFile,
                "get server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size"
        );
        assertTrue(outputToStreamOfLines(gfEmbeddedProcess)
                .filter(new TwoLineMatcher(
                        line -> line.contains("Description: get"),
                        line -> line.contains("server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=200")
                ))
                .findAny().isPresent(),
                "Message about max-thread-pool-size=200 set is found");
        gfEmbeddedProcess
                .waitFor(30, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testPropertyNamesVariants(String gfEmbeddedJarName) throws Exception {
        Process gfEmbeddedProcess = runGlassFishEmbedded(gfEmbeddedJarName,
                "--properties=" + propertiesFile,
                "get server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size",
                "get resources.jdbc-connection-pool.DerbyPool.connection-leak-timeout-in-seconds",
                "get server.network-config.protocols.protocol.http-listener.http.max-connections",
                "get security-configurations.authorization-service.authorizationService.default"

        );
        gfEmbeddedProcess
                .waitFor(30, TimeUnit.SECONDS);
        List<String> appliedPropertyLogs = outputToStreamOfLines(gfEmbeddedProcess)
                .filter(new TwoLineMatcher(
                        line -> line.contains("Description: get"),
                        line -> true
                ))
                .collect(Collectors.toList());
        assertTrue(appliedPropertyLogs
                .get(0).contains("server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=200"));
        assertTrue(appliedPropertyLogs
                .get(1).contains("resources.jdbc-connection-pool.DerbyPool.connection-leak-timeout-in-seconds=5"));
        assertTrue(appliedPropertyLogs
                .get(2).contains("server.network-config.protocols.protocol.http-listener.http.max-connections=10000"));
        assertTrue(appliedPropertyLogs
                .get(3).contains("security-configurations.authorization-service.authorizationService.default=false"));
        assertTrue(appliedPropertyLogs.size() == 4,"4 properties should be logged");
    }

    private class TwoLineMatcher implements Predicate<String> {

        AtomicReference<String> previousLine = new AtomicReference<>();
        Predicate<String> previousLinePredicate;
        Predicate<String> currentLinePredicate;

        public TwoLineMatcher(Predicate<String> previousLinePredicate, Predicate<String> currentLinePredicate) {
            this.previousLinePredicate = previousLinePredicate;
            this.currentLinePredicate = currentLinePredicate;
        }

        @Override
        public boolean test(String line) {
            boolean lineMatches = previousLine.get() != null
                    && previousLinePredicate.test(previousLine.get())
                    && currentLinePredicate.test(line);
            previousLine.set(line);
            return lineMatches;
        }
    }

}
