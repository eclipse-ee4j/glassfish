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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.glassfish.tests.embedded.runnable.TestArgumentProviders.SingleGfEmbeddedJarNameProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static java.lang.System.err;
import static org.glassfish.tests.embedded.runnable.GfEmbeddedUtils.outputToStreamOfLines;
import static org.glassfish.tests.embedded.runnable.GfEmbeddedUtils.runGlassFishEmbedded;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Ondro Mihalyi
 */
public class ConfigFileTest {

    private static final Logger LOG = Logger.getLogger(AddLibraryTest.class.getName());

    @ParameterizedTest
    @ArgumentsSource(SingleGfEmbeddedJarNameProvider.class)
    void testExecuteCommandsInOrderDefinedInPropertiesFile(String gfEmbeddedJarName) throws Exception {
        String propertiesFile = "glassfish.properties";

        Files.copy(getClass().getClassLoader().getResourceAsStream(getClass().getSimpleName() + "/" + propertiesFile),
                Paths.get(propertiesFile),
                StandardCopyOption.REPLACE_EXISTING);

        Process gfEmbeddedProcess = runGlassFishEmbedded(gfEmbeddedJarName,
                "--properties=" + propertiesFile,
                "get configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size"
        );
        AtomicReference<String> previousLine = new AtomicReference<>();
        assertTrue(outputToStreamOfLines(gfEmbeddedProcess)
                .peek(err::println)
                .filter(line -> {
                    if (previousLine.get() != null && previousLine.get().contains("Description: get") && line.contains("configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=200")) {
                        return true;
                    } else {
                        previousLine.set(line);
                        return false;
                    }
                })
                .findAny().isPresent(),
                "A log from deployed application is present");
        gfEmbeddedProcess
                .waitFor(30, TimeUnit.SECONDS);
    }
}
