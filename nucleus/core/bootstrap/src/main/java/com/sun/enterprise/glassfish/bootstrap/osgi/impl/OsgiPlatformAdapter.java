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

package com.sun.enterprise.glassfish.bootstrap.osgi.impl;

import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextCfg;
import com.sun.enterprise.glassfish.bootstrap.cp.ClassPathBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

import static com.sun.enterprise.glassfish.bootstrap.log.LogFacade.BOOTSTRAP_FMWCONF;
import static com.sun.enterprise.glassfish.bootstrap.log.LogFacade.BOOTSTRAP_LOGGER;
import static java.util.logging.Level.INFO;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE;

public abstract class OsgiPlatformAdapter {

    /** Location of the unified config properties file relative to the domain directory */
    private static final Path CONFIG_PROPERTIES = Path.of("config", "osgi.properties");

    private final StartupContextCfg cfg;

    /**
     * @param cfg Initial properties
     */
    public OsgiPlatformAdapter(StartupContextCfg cfg) {
        this.cfg = cfg;
    }

    /**
     * Adds the jar files of the OSGi platform to the given {@link ClassPathBuilder}
     */
    public abstract void addFrameworkJars(ClassPathBuilder builder) throws IOException;

    protected abstract String getFrameworkStorageDirectoryName();

    /**
     * @return platform specific configuration information
     * @throws IOException if the configuration could not be loaded
     */
    public final Properties readPlatformConfiguration() throws IOException {
        Properties platformConfig = new Properties();
        final File configFile = getFrameworkConfigFile();
        if (configFile == null) {
            return platformConfig;
        }
        try (InputStream in = new FileInputStream(configFile)) {
            platformConfig.load(in);
        }
        final String storageDirectoryName = getFrameworkStorageDirectoryName();
        if (storageDirectoryName != null) {
            platformConfig.setProperty(FRAMEWORK_STORAGE, this.cfg
                .getFileUnderInstanceRoot(Path.of("osgi-cache", storageDirectoryName)).getAbsolutePath());
        }
        return platformConfig;
    }


    protected File getFrameworkConfigFile() {
        // First we search in domainDir. If it's not found there, we fall back on installDir
        File osgiPropertiesFile = cfg.getInstanceRoot().resolve(CONFIG_PROPERTIES).toFile();
        if (osgiPropertiesFile.exists()) {
            BOOTSTRAP_LOGGER.log(INFO, BOOTSTRAP_FMWCONF, osgiPropertiesFile);
            return osgiPropertiesFile;
        }
        return cfg.getInstallRoot().resolve(CONFIG_PROPERTIES).toFile();
    }
}
