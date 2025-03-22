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
package org.glassfish.tests.embedded.runnable;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.glassfish.tests.embedded.runnable.TestArgumentProviders.GfEmbeddedJarNameProvider;
import org.glassfish.tests.embedded.runnable.app.App;
import org.glassfish.tests.embedded.runnable.library.MockExecutorService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.UnknownExtensionTypeException;
import org.jboss.shrinkwrap.api.exporter.ArchiveExportException;
import org.jboss.shrinkwrap.api.exporter.FileExistsException;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static java.lang.System.err;
import static org.glassfish.tests.embedded.runnable.GfEmbeddedUtils.outputToStreamOfLines;
import static org.glassfish.tests.embedded.runnable.GfEmbeddedUtils.runGlassFishEmbedded;
import static org.glassfish.tests.embedded.runnable.ShrinkwrapUtils.logArchiveContent;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ondro Mihalyi
 */
public class AddLibraryTest {

    private static final Logger LOG = Logger.getLogger(AddLibraryTest.class.getName());

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testAddLibraryForApp(String gfEmbeddedJarName) throws Exception {

        File jarFile = null;
        File warFile = null;
        try {
            jarFile = testLibraryJavaArchive(jarFile);
            warFile = warArchiveThatDependsOnTestLibrary(warFile);
            Process gfEmbeddedProcess = runGlassFishEmbedded(gfEmbeddedJarName,
                    "add-library " + jarFile.getAbsolutePath(),
                    warFile.getAbsolutePath()
            );
            assertTrue(outputToStreamOfLines(gfEmbeddedProcess)
                    .peek(err::println)
                    .filter(line -> line.contains("App initialized"))
                    .findAny().isPresent(),
                    "A log from deployed application is present");
            gfEmbeddedProcess
                    .waitFor(30, TimeUnit.SECONDS);
        } finally {
            Optional.ofNullable(jarFile).ifPresent(File::delete);
            Optional.ofNullable(warFile).ifPresent(File::delete);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testAddLibraryForGrizzlyExecutor(String gfEmbeddedJarName) throws Exception {

        File jarFile = null;
        File warFile = null;
        try {
            jarFile = testLibraryJavaArchive(jarFile);
            Process gfEmbeddedProcess = runGlassFishEmbedded(gfEmbeddedJarName,
                    "add-library " + jarFile.getAbsolutePath(),
                    "set configs.config.server-config.thread-pools.thread-pool.http-thread-pool.classname=" + MockExecutorService.class.getName()
            );
            assertTrue(! outputToStreamOfLines(gfEmbeddedProcess)
                    .peek(err::println)
                    .filter(line -> line.contains("ClassNotFoundException"))
                    .findAny().isPresent(),
                    "ClassNotFoundException should not be thrown for " + MockExecutorService.class.getSimpleName());
            gfEmbeddedProcess
                    .waitFor(30, TimeUnit.SECONDS);
        } finally {
            Optional.ofNullable(jarFile).ifPresent(File::delete);
            Optional.ofNullable(warFile).ifPresent(File::delete);
        }
    }

    private File testLibraryJavaArchive(File jarFile) throws FileExistsException, ArchiveExportException, UnknownExtensionTypeException, IllegalArgumentException {
        String jarName = "testLibrary.jar";
        JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class, jarName)
                .addClass(MockExecutorService.class);
        jarFile = new File(jarName);
        javaArchive.as(ZipExporter.class).exportTo(jarFile);
        logArchiveContent(javaArchive, jarName, LOG::info);
        return jarFile;
    }

    private File warArchiveThatDependsOnTestLibrary(File warFile) throws FileExistsException, ArchiveExportException, IllegalArgumentException {
        String warName = "testLibraryApp.war";
        WebArchive warArchive = ShrinkWrap.create(WebArchive.class, warName)
                .addPackage(App.class.getPackage());
        warFile = new File(warName);
        warArchive.as(ZipExporter.class).exportTo(warFile);
        logArchiveContent(warArchive, warName, LOG::info);
        return warFile;
    }

}