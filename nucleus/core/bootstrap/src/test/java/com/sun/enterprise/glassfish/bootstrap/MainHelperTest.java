/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.glassfish.bootstrap.osgi.impl.Platform;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.PlatformHelper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTALL_ROOT_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTANCE_ROOT_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.PLATFORM_PROPERTY_KEY;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES;

/**
 * Created by kokil on 5/18/17.
 */
class MainHelperTest {

    /**
     * This test is used to test the regex pattern of "parseAsEnv" method of "MainHelper.java".
     * <br>
     * It creates two temporary files (asenv.conf and asenv.bat) for testing purpose.
     * The "parseAsEnv()" method of "MainHelper.java" reads the "asenv.*" file line by line to
     * generate the Properties "asenvProps" whose assertion has been done in this unit test.
     */
    @Test
    void parseAsEnvTest() throws Exception {
        File resources = File.createTempFile("helperTestResources", "config");
        resources.delete(); // delete the temp file
        resources.mkdir(); // reuse the name for a directory
        resources.deleteOnExit();
        File config = new File(resources, "config");
        config.mkdir();
        config.deleteOnExit();
        File asenv_bat = new File(config, "asenv.bat"); // test resource for windows
        File asenv_conf = new File(config, "asenv.conf");// test resource for linux
        asenv_bat.deleteOnExit();
        asenv_conf.deleteOnExit();

        PrintWriter pw1 = new PrintWriter(asenv_bat, UTF_8);
        pw1.println("set AbcVar=value1");
        pw1.println("SET Avar=\"value2\"");
        pw1.println("Set Bvar=\"value3\"");
        pw1.println("set setVar=\"value4\"");
        pw1.println("set SetVar=value5");
        pw1.println("set seVar=\"value6\"");
        pw1.println("set sVar=\"value7\"");
        pw1.close();
        PrintWriter pw2 = new PrintWriter(asenv_conf, UTF_8);
        pw2.println("AbcVar=value1");
        pw2.println("Avar=\"value2\"");
        pw2.println("Bvar=\"value3\"");
        pw2.println("setVar=\"value4\"");
        pw2.println("SetVar=value5");
        pw2.println("seVar=\"value6\"");
        pw2.println("sVar=\"value7\"");
        pw2.close();

        File installRoot = new File(resources.toString());
        Properties asenvProps = MainHelper.parseAsEnv(installRoot);
        assertEquals("value1", asenvProps.getProperty("AbcVar"));
        assertEquals("value2", asenvProps.getProperty("Avar"));
        assertEquals("value3", asenvProps.getProperty("Bvar"));
        assertEquals("value4", asenvProps.getProperty("setVar"));
        assertEquals("value5", asenvProps.getProperty("SetVar"));
        assertEquals("value6", asenvProps.getProperty("seVar"));
        assertEquals("value7", asenvProps.getProperty("sVar"));
    }


    @Test
    void createLauncher_Felix() throws Exception {
        Properties properties = createDefaultProperties();
        PlatformHelper platformHelper = PlatformFactory.getPlatformHelper(properties);
        assertNotNull(platformHelper);
        Properties cfg = platformHelper.readPlatformConfiguration();
        assertNotNull(cfg);
        cfg.putAll(properties);

        ClassLoader loader = MainHelper.createLauncherCL(cfg, ClassLoader.getPlatformClassLoader());
        assertNotNull(loader);
        Class<?> clazz = loader.loadClass("org.osgi.framework.Bundle");
        assertNotNull(clazz);
        String osgiPackages = cfg.getProperty(FRAMEWORK_SYSTEMPACKAGES);
        assertAll(
            () -> assertThat(osgiPackages,
                stringContainsInOrder("org.osgi.framework;version=\"1.10\"", "java.lang,",
                    "java.util.concurrent.locks,", "javax.xml.crypto.dom,", "org.w3c.dom.traversal,")),
            () -> assertThat(osgiPackages, not(stringContainsInOrder(".hk2."))),
            () -> assertThat(osgiPackages, anyOf(stringContainsInOrder("com.sun.jarsigner"),
                stringContainsInOrder("jdk.security.jarsigner")))
        );
    }


    private Properties createDefaultProperties() throws IOException {
        Properties properties = new Properties();
        properties.setProperty(PLATFORM_PROPERTY_KEY, Platform.Felix.name());
        Path installRoot = Files.createTempDirectory("FakeGFInstallRoot");
        Path felixBin = installRoot.resolve(Path.of("osgi", "felix", "bin"));
        Files.createDirectories(felixBin);
        Path modulesDir = installRoot.resolve("modules");
        Files.createDirectories(modulesDir);
        Path bootstrapDir = installRoot.resolve(Path.of("lib", "bootstrap"));
        Files.createDirectories(bootstrapDir);

        Path jarFilesDir = detectBasedir().toPath().resolve(Path.of("target", "test-osgi"));
        Files.copy(jarFilesDir.resolve("glassfish-jul-extension.jar"), bootstrapDir.resolve("glassfish-jul-extension.jar"));
        Files.copy(jarFilesDir.resolve("org.apache.felix.main.jar"), felixBin.resolve("felix.jar"));
        Files.copy(jarFilesDir.resolve("simple-glassfish-api.jar"), modulesDir.resolve("simple-glassfish-api.jar"));
        // This is a fake to avoid making this test an integration test - glassfish.jar file is not yet generated by Maven
        Files.copy(jarFilesDir.resolve("simple-glassfish-api.jar"), modulesDir.resolve("glassfish.jar"));

        Path cfgDir = Files.createDirectories(installRoot.resolve(Path.of("config")));
        Files.createFile(cfgDir.resolve("osgi.properties"));
        properties.setProperty(INSTALL_ROOT_PROP_NAME, installRoot.toFile().getAbsolutePath());

        Path instanceRoot = Files.createTempDirectory("FakeGFInstanceRoot");
        properties.setProperty(INSTANCE_ROOT_PROP_NAME, instanceRoot.toFile().getAbsolutePath());
        return properties;
    }


    /**
     * Useful for a heuristic inside Eclipse and other environments.
     *
     * @return Absolute path to the glassfish directory.
     */
    private static File detectBasedir() {
        final String basedir = System.getProperty("basedir");
        if (basedir != null) {
            return new File(basedir);
        }
        final File target = new File("target");
        if (target.exists()) {
            return target.getAbsoluteFile().getParentFile();
        }
        return new File(".").getAbsoluteFile().getParentFile();
    }
}
