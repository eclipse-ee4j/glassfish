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
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.glassfish.tests.embedded.runnable.app.SystemPropertyApp;
import org.glassfish.tests.embedded.runnable.tool.TestArgumentProviders.GfEmbeddedJarNameProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static java.lang.System.Logger.Level.INFO;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.glassfish.tests.embedded.runnable.tool.BufferedReaderMatcher.readerContains;
import static org.glassfish.tests.embedded.runnable.tool.EmbeddedGlassFishStarter.start;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Ondro Mihalyi
 */
public class SystemPropertyTest {

    private static final Logger LOG = System.getLogger(SystemPropertyTest.class.getName());

    @TempDir
    private Path tmpDir;
    private Path warFile;
    private Path propertiesFile;
    private Path domainConfigFile;

    @AfterEach
    void deleteFiles() throws Exception {
        if (warFile != null) {
            Files.deleteIfExists(warFile);
        }
        if (propertiesFile != null) {
            Files.deleteIfExists(propertiesFile);
        }
        if (domainConfigFile != null) {
            Files.deleteIfExists(domainConfigFile);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testSystemPropertyFromJvmOption(String gfEmbeddedJarName) throws Exception {
        warFile = createSystemPropertyApp();
        Process gfEmbeddedProcess = start(gfEmbeddedJarName, List.of("-Dmy.name=Embedded GlassFish"),
            warFile.toString());
        assertThat(gfEmbeddedProcess.errorReader(), readerContains("System property my.name: Embedded GlassFish"));
        gfEmbeddedProcess.waitFor(30, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testSystemPropertyFromAdminCommand(String gfEmbeddedJarName) throws Exception {
        warFile = createSystemPropertyApp();
        Process gfEmbeddedProcess = start(gfEmbeddedJarName,
            "create-system-properties my.name=Embedded\\ GlassFish", warFile.toString());
        assertThat(gfEmbeddedProcess.errorReader(), readerContains("System property my.name: Embedded GlassFish"));
        gfEmbeddedProcess.waitFor(30, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testSystemPropertyFromPropertyFileDirectly(String gfEmbeddedJarName, TestInfo testInfo) throws Exception {
        propertiesFile = preparePropertiesFile(testInfo);
        warFile = createSystemPropertyApp();
        Process gfEmbeddedProcess = start(gfEmbeddedJarName,
            "--properties=" + propertiesFile.toFile().getPath(), warFile.toString());
        assertThat(gfEmbeddedProcess.errorReader(), readerContains("System property my.name: Embedded GlassFish"));
        gfEmbeddedProcess.waitFor(30, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @ArgumentsSource(GfEmbeddedJarNameProvider.class)
    void testSystemPropertyFromDomainConfig(String gfEmbeddedJarName, TestInfo testInfo) throws Exception {
        domainConfigFile = prepareDomainConfig(testInfo);
        warFile = createSystemPropertyApp();
        Process gfEmbeddedProcess = start(gfEmbeddedJarName,
            "--domainConfigFile=" + domainConfigFile.toFile().getPath(), warFile.toString());
        assertThat(gfEmbeddedProcess.errorReader(), readerContains("System property my.name: Embedded GlassFish"));
        gfEmbeddedProcess.waitFor(30, TimeUnit.SECONDS);
    }

    private Path createSystemPropertyApp() throws Exception {
        String warName = "systemPropertyApp.war";
        WebArchive warArchive = ShrinkWrap.create(WebArchive.class, warName)
                .addClass(SystemPropertyApp.class);
        Path war = tmpDir.resolve(warName);
        warArchive.as(ZipExporter.class).exportTo(war.toFile(), true);
        LOG.log(INFO, () -> "Generated war file:\n" + warArchive.toString(true));
        return war;
    }

    private Path prepareDomainConfig(TestInfo testInfo) throws IOException {
        String testClassName = testInfo.getTestClass().get().getSimpleName();
        String testMethodName = testInfo.getTestMethod().get().getName();
        Path cfgFile = tmpDir.resolve(testClassName + "-" + testMethodName + "-" + "domain.xml");
        Files.copy(getClass().getClassLoader().getResourceAsStream(testClassName + "/domain.xml"), cfgFile,
            REPLACE_EXISTING);
        return cfgFile;
    }


    private Path preparePropertiesFile(TestInfo testInfo) throws IOException {
        String testClassName = testInfo.getTestClass().get().getSimpleName();
        String testMethodName = testInfo.getTestMethod().get().getName();
        Path cfgFile = tmpDir.resolve(testClassName + "-" + testMethodName + "-" + "glassfish.properties");
        Files.copy(getClass().getClassLoader().getResourceAsStream(testClassName + "/glassfish.properties"), cfgFile,
            REPLACE_EXISTING);
        return cfgFile;
    }

}
