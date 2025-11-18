/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;

import org.glassfish.tests.embedded.runnable.app.App;
import org.glassfish.tests.embedded.runnable.library.MockExecutorService;
import org.glassfish.tests.embedded.runnable.tool.TestArgumentProviders.GfEmbeddedJarNameProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static java.lang.System.Logger.Level.INFO;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.glassfish.tests.embedded.runnable.tool.BufferedReaderMatcher.readerContains;
import static org.glassfish.tests.embedded.runnable.tool.EmbeddedGlassFishStarter.start;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ondro Mihalyi
 * @author David Matejcek
 */
public class AddLibraryTest {

    private static final Logger LOG = System.getLogger(AddLibraryTest.class.getName());

    @TempDir
    private Path tmpDir;
    private Path jarFile;
    private Path warFile;

    @AfterEach
    void deleteFiles() throws Exception {
        if (jarFile != null) {
            Files.deleteIfExists(jarFile);
        }
        if (warFile != null) {
            Files.deleteIfExists(warFile);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testAddLibraryForApp(String gfEmbeddedJarName) throws Exception {
        jarFile = testLibraryJavaArchive();
        warFile = warArchiveThatDependsOnTestLibrary();
        Process glassfish = start(gfEmbeddedJarName, "add-library " + jarFile, warFile.toString());
        assertThat(glassfish.errorReader(), readerContains("App initialized"));
        assertTrue(glassfish.waitFor(30, SECONDS), "Process finished.");
    }

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testAddLibraryForGrizzlyExecutor(String gfEmbeddedJarName) throws Exception {
        jarFile = testLibraryJavaArchive();
        Process glassfish = start(gfEmbeddedJarName, "add-library " + jarFile,
            "set configs.config.server-config.thread-pools.thread-pool.http-thread-pool.classname="
                + MockExecutorService.class.getName());
        assertThat(glassfish.errorReader(), not(readerContains("ClassNotFoundException")));
        assertTrue(glassfish.waitFor(30, SECONDS), "Process finished.");
    }

    private Path testLibraryJavaArchive() throws Exception {
        String jarName = "testLibrary.jar";
        JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class, jarName).addClass(MockExecutorService.class);
        jarFile = tmpDir.resolve(jarName).toAbsolutePath();
        javaArchive.as(ZipExporter.class).exportTo(jarFile.toFile());
        LOG.log(INFO, () -> "Java library:\n" + javaArchive.toString(true));
        return jarFile;
    }

    private Path warArchiveThatDependsOnTestLibrary() throws Exception {
        String warName = "testLibraryApp.war";
        WebArchive warArchive = ShrinkWrap.create(WebArchive.class, warName).addPackage(App.class.getPackage());
        warFile = tmpDir.resolve(warName).toAbsolutePath();
        warArchive.as(ZipExporter.class).exportTo(warFile.toFile());
        LOG.log(INFO, () -> "WAR:\n" + warArchive.toString(true));
        return warFile;
    }

}
