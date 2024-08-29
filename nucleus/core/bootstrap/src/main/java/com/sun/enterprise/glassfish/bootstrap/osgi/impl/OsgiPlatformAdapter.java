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

import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.sun.enterprise.glassfish.bootstrap.log.LogFacade.BOOTSTRAP_FMWCONF;
import static com.sun.enterprise.glassfish.bootstrap.log.LogFacade.BOOTSTRAP_LOGGER;
import static java.util.logging.Level.INFO;

public abstract class OsgiPlatformAdapter {

    /** Location of the unified config properties file relative to the domain directory */
    private static final String CONFIG_PROPERTIES = "config/osgi.properties";

    protected final File glassfishDir;
    protected final File domainDir;
    private final Properties properties;

    /**
     * @param properties Initial properties
     */
    public OsgiPlatformAdapter(Properties properties) {
        this.properties = properties;
        this.glassfishDir = StartupContextUtil.getInstallRoot(properties);
        this.domainDir = StartupContextUtil.getInstanceRoot(properties);
    }

    /**
     * Adds the jar files of the OSGi platform to the given {@link ClassPathBuilder}
     */
    public abstract void addFrameworkJars(ClassPathBuilder cpb) throws IOException;

    /**
     * @return platform specific configuration information
     * @throws IOException if the configuration could not be loaded
     */
    public Properties readPlatformConfiguration() throws IOException {
        Properties platformConfig = new Properties();
        final File configFile = getFrameworkConfigFile();
        if (configFile == null) {
            return platformConfig;
        }
        try (InputStream in = new FileInputStream(configFile)) {
            platformConfig.load(in);
        }
        return platformConfig;
    }


    protected File getFrameworkConfigFile() {
        // First we search in domainDir. If it's not found there, we fall back on installDir
        File osgiPropertiesFile = new File(domainDir, CONFIG_PROPERTIES);
        if (osgiPropertiesFile.exists()) {
            BOOTSTRAP_LOGGER.log(INFO, BOOTSTRAP_FMWCONF, osgiPropertiesFile);
            return osgiPropertiesFile;
        }
        return new File(glassfishDir, CONFIG_PROPERTIES);
    }
}
