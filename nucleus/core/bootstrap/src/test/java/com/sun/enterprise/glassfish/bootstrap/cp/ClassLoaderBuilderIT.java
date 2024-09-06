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

package com.sun.enterprise.glassfish.bootstrap.cp;

import com.sun.enterprise.glassfish.bootstrap.StartupContextCfgFactory;
import com.sun.enterprise.glassfish.bootstrap.cfg.OsgiPlatform;
import com.sun.enterprise.glassfish.bootstrap.cfg.ServerFiles;
import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextCfg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTALL_ROOT_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTANCE_ROOT_PROP_NAME;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES;

/**
 * Created by kokil on 5/18/17.
 */
class ClassLoaderBuilderIT {

    @Test
    void createLauncher_Felix() throws Exception {
        StartupContextCfg cfg = createStartupContextCfg();
        ClassLoader loader = ClassLoaderBuilder.createLauncherCL(cfg, ClassLoader.getPlatformClassLoader());
        assertNotNull(loader);
        Class<?> osgiClass = loader.loadClass("org.osgi.framework.Bundle");
        assertNotNull(osgiClass);
        Class<?> clazz = loader.loadClass("org.apache.felix.framework.Felix");
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


    private StartupContextCfg createStartupContextCfg() throws IOException {
        Path installRoot = Files.createTempDirectory("FakeGFInstallRoot");
        Path instanceRoot = Files.createTempDirectory("FakeGFInstanceRoot");
        ServerFiles files = new ServerFiles(installRoot, instanceRoot);

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
        Files.copy(jarFilesDir.resolve(Path.of("..", "glassfish.jar")), modulesDir.resolve("glassfish.jar"));

        Properties properties = new Properties();
        Path cfgDir = Files.createDirectories(installRoot.resolve(Path.of("config")));
        Files.createFile(cfgDir.resolve("osgi.properties"));
        properties.setProperty(INSTALL_ROOT_PROP_NAME, installRoot.toFile().getAbsolutePath());

        properties.setProperty(INSTANCE_ROOT_PROP_NAME, instanceRoot.toFile().getAbsolutePath());

        return StartupContextCfgFactory.createStartupContextCfg(OsgiPlatform.Felix, files, new String[0]);
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
