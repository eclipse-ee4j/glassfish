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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.glassfish.tests.embedded.runnable.TestArgumentProviders.GfEmbeddedJarNameProvider;
import org.glassfish.tests.embedded.runnable.app.SystemPropertyApp;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.glassfish.tests.embedded.runnable.GfEmbeddedUtils.outputToStreamOfLines;
import static org.glassfish.tests.embedded.runnable.GfEmbeddedUtils.runGlassFishEmbedded;
import static org.glassfish.tests.embedded.runnable.ShrinkwrapUtils.logArchiveContent;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ondro Mihalyi
 */
public class SystemPropertyTest {

    private static final Logger LOG = Logger.getLogger(SystemPropertyTest.class.getName());

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testSystemPropertyFromJvmOption(String gfEmbeddedJarName) throws Exception {
        File warFile = null;
        try {
            warFile = createSystemPropertyApp();
            Process gfEmbeddedProcess = runGlassFishEmbedded(gfEmbeddedJarName,
                    List.of("-Dmy.name=Embedded GlassFish"),
                    warFile.getAbsolutePath()
            );
            assertTrue(outputToStreamOfLines(gfEmbeddedProcess)
                    .anyMatch(line -> line.contains("System property my.name: Embedded GlassFish")),
                    "Application should print the value of the my.name system property.");
            gfEmbeddedProcess.waitFor(30, TimeUnit.SECONDS);
        } finally {
            Optional.ofNullable(warFile).ifPresent(File::delete);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testSystemPropertyFromAdminCommand(String gfEmbeddedJarName) throws Exception {
        File warFile = null;
        try {
            warFile = createSystemPropertyApp();
            Process gfEmbeddedProcess = runGlassFishEmbedded(gfEmbeddedJarName,
                    "create-system-properties my.name=Embedded\\ GlassFish",
                    warFile.getAbsolutePath()
            );
            assertTrue(outputToStreamOfLines(gfEmbeddedProcess)
                    .anyMatch(line -> line.contains("System property my.name: Embedded GlassFish")),
                    "Application should print the value of the my.name system property.");
            gfEmbeddedProcess.waitFor(30, TimeUnit.SECONDS);
        } finally {
            Optional.ofNullable(warFile).ifPresent(File::delete);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testSystemPropertyFromPropertyFileDirectly(String gfEmbeddedJarName, TestInfo testInfo) throws Exception {
        File warFile = null;
        Path propertiesFile = preparePropertiesFile(testInfo);
        try {
            warFile = createSystemPropertyApp();
            Process gfEmbeddedProcess = runGlassFishEmbedded(gfEmbeddedJarName,
                    "--properties=" + propertiesFile.toFile().getPath(),
                    warFile.getAbsolutePath()
            );
            assertTrue(outputToStreamOfLines(gfEmbeddedProcess)
                    .anyMatch(line -> line.contains("System property my.name: Embedded GlassFish")),
                    "Application should print the value of the my.name system property.");
            gfEmbeddedProcess.waitFor(30, TimeUnit.SECONDS);
        } finally {
            Optional.ofNullable(warFile).ifPresent(File::delete);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testSystemPropertyFromDomainConfig(String gfEmbeddedJarName, TestInfo testInfo) throws Exception {
        File warFile = null;
        Path domainConfigFile = prepareDomainConfig(testInfo);
        try {
            warFile = createSystemPropertyApp();
            Process gfEmbeddedProcess = runGlassFishEmbedded(gfEmbeddedJarName,
                    "--domainConfigFile=" + domainConfigFile.toFile().getPath(),
                    warFile.getAbsolutePath()
            );
            assertTrue(outputToStreamOfLines(gfEmbeddedProcess)
                    .anyMatch(line -> line.contains("System property my.name: Embedded GlassFish")),
                    "Application should print the value of the my.name system property.");
            gfEmbeddedProcess.waitFor(30, TimeUnit.SECONDS);
        } finally {
            Optional.ofNullable(warFile).ifPresent(File::delete);
        }
    }

    private File createSystemPropertyApp() throws Exception {
        String warName = "systemPropertyApp.war";
        WebArchive warArchive = ShrinkWrap.create(WebArchive.class, warName)
                .addClass(SystemPropertyApp.class);
        File warFile = new File(warName);
        warArchive.as(ZipExporter.class).exportTo(warFile, true);
        logArchiveContent(warArchive, warName, LOG::info);
        return warFile;
    }

    private Path prepareDomainConfig(TestInfo testInfo) throws IOException {
        String testClassName = testInfo.getTestClass().get().getSimpleName();
        String testMethodName = testInfo.getTestMethod().get().getName();
        Path domainConfigFile = Paths.get(testClassName + "-" + testMethodName + "-" + "domain.xml");
        Files.copy(getClass().getClassLoader().getResourceAsStream(testClassName + "/domain.xml"),
                domainConfigFile,
                StandardCopyOption.REPLACE_EXISTING);
        return domainConfigFile;
    }

    private Path preparePropertiesFile(TestInfo testInfo) throws IOException {
        String testClassName = testInfo.getTestClass().get().getSimpleName();
        String testMethodName = testInfo.getTestMethod().get().getName();
        Path propertiesFile = Paths.get(testClassName + "-" + testMethodName + "-" + "glassfish.properties");
        Files.copy(getClass().getClassLoader().getResourceAsStream(testClassName + "/glassfish.properties"),
                propertiesFile,
                StandardCopyOption.REPLACE_EXISTING);
        return propertiesFile;
    }

}
