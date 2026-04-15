/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.glassfish.bootstrap.cfg.OsgiPlatform;
import com.sun.enterprise.glassfish.bootstrap.cfg.ServerFiles;
import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextCfg;
import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextCfgFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.ASADMIN_ARGS;
import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;

/**
 *
 * @author Ondro Mihalyi
 */
public final class StartupContextUtil {

    // Util class
    private StartupContextUtil() {
    }

    public static StartupContextCfg createStartupContextCfg() throws IOException {
        return createStartupContextCfg(new Properties());
    }

    public static StartupContextCfg createStartupContextCfg(Properties osgiProps) throws IOException {
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
        Files.copy(jarFilesDir.resolve("simple-glassfish-api.jar"), bootstrapDir.resolve("simple-glassfish-api.jar"));
        Files.copy(jarFilesDir.resolve(Path.of("..", "glassfish.jar")), bootstrapDir.resolve("glassfish.jar"));

        Path cfgDir = Files.createDirectories(installRoot.resolve(Path.of("config")));
        try (var out = Files.newOutputStream(cfgDir.resolve("osgi.properties"))) {
            osgiProps.store(out, null);
        }

        Properties args = new Properties();
        args.setProperty(INSTALL_ROOT.getPropertyName(), installRoot.toFile().getAbsolutePath());
        args.setProperty(INSTANCE_ROOT.getPropertyName(), instanceRoot.toFile().getAbsolutePath());
        args.setProperty(ASADMIN_ARGS, "-something");

        return StartupContextCfgFactory.createStartupContextCfg(OsgiPlatform.Felix, files, args);
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
