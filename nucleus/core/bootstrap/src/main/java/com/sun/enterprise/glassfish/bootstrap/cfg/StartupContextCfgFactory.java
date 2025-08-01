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

package com.sun.enterprise.glassfish.bootstrap.cfg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTALL_ROOT_URI_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTANCE_ROOT_URI_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.STARTUP_MODULESTARTUP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.STARTUP_MODULE_NAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.TIME_ZERO_NAME;
import static com.sun.enterprise.glassfish.bootstrap.log.LogFacade.BOOTSTRAP_FMWCONF;
import static com.sun.enterprise.glassfish.bootstrap.log.LogFacade.BOOTSTRAP_LOGGER;
import static java.util.logging.Level.INFO;
import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE;

public final class StartupContextCfgFactory {

    /** Location of the unified config properties file relative to the domain directory */
    private static final Path CONFIG_PROPERTIES = Path.of("config", "osgi.properties");


    private StartupContextCfgFactory() {
    }


    public static StartupContextCfg createStartupContextCfg(OsgiPlatform platform, ServerFiles files, Properties args) {
        Properties properties = new Properties();
        properties.putAll(args);
        properties.setProperty(TIME_ZERO_NAME, Long.toString(System.currentTimeMillis()));

        File installRoot = files.getInstallRoot().toFile();
        properties.setProperty(INSTALL_ROOT.getPropertyName(), installRoot.getAbsolutePath());
        properties.setProperty(INSTALL_ROOT_URI_PROP_NAME, installRoot.toURI().toString());

        File instanceRoot = files.getInstanceRoot().toFile();
        properties.setProperty(INSTANCE_ROOT.getPropertyName(), instanceRoot.getAbsolutePath());
        properties.setProperty(INSTANCE_ROOT_URI_PROP_NAME, instanceRoot.toURI().toString());

        properties.setProperty(STARTUP_MODULE_NAME, BootstrapKeys.GF_KERNEL);

        // temporary hack until CLI does that for us.
        String upgrade = properties.getProperty("-upgrade");
        if (upgrade != null && !upgrade.equals("false")) {
            properties.setProperty(STARTUP_MODULESTARTUP_NAME, "upgrade");
        }
        return mergePlatformConfiguration(platform, files, properties);
    }


    private static StartupContextCfg mergePlatformConfiguration(OsgiPlatform platform, ServerFiles files,
        Properties properties) {
        final Properties platformCfg;
        try {
            platformCfg = readPlatformConfiguration(getFrameworkConfigFile(files));
        } catch (IOException e) {
            throw new IllegalStateException("The OSGI configuration could not be loaded!", e);
        }
        final String storageDirectoryName = platform.getFrameworkStorageDirectoryName();
        if (storageDirectoryName != null) {
            File frameworkStorage = files.getFileUnderInstanceRoot(Path.of("osgi-cache", storageDirectoryName));
            platformCfg.setProperty(FRAMEWORK_STORAGE, frameworkStorage.getAbsolutePath());
        }
        platformCfg.putAll(properties);

        // Perform variable substitution
        for (String name : platformCfg.stringPropertyNames()) {
            platformCfg.setProperty(name, FelixUtil.substVars(platformCfg.getProperty(name), name, null, platformCfg));
        }

        // Starting with GlassFish 3.1.2, we allow user to overrride values specified in OSGi config file by
        // corresponding values as set via System properties. There are two properties that we must always read
        // from OSGi config file. They are felix.fileinstall.dir and felix.fileinstall.log.level, as their values have
        // changed incompatibly from 3.1 to 3.1.1, but we are not able to change domain.xml in 3.1.1 for
        // compatibility reasons.
        overrideBySystemProps(platformCfg, Arrays.asList("felix.fileinstall.dir", "felix.fileinstall.log.level"));
        return new StartupContextCfg(platform, files, platformCfg);
    }


    /**
     * @return platform specific configuration information
     * @throws IOException if the configuration could not be loaded
     */
    private static Properties readPlatformConfiguration(File configFile) throws IOException {
        final Properties platformConfig = new Properties();
        if (configFile == null) {
            return platformConfig;
        }
        try (InputStream in = new FileInputStream(configFile)) {
            platformConfig.load(in);
        }
        return platformConfig;
    }


    private static File getFrameworkConfigFile(ServerFiles files) {
        // First we search in domainDir. If it's not found there, we fall back on installDir
        File osgiPropertiesFile = files.getFileUnderInstanceRoot(CONFIG_PROPERTIES);
        if (osgiPropertiesFile.exists()) {
            BOOTSTRAP_LOGGER.log(INFO, BOOTSTRAP_FMWCONF, osgiPropertiesFile);
            return osgiPropertiesFile;
        }
        return files.getFileUnderInstallRoot(CONFIG_PROPERTIES);
    }


    /**
     * Override property values in the given properties object by values set in corresponding
     * property names in System properties object.
     *
     * @param osgiCfg which will be updated by corresponding values in System properties.
     * @param excluding property names that should not be overridden
     */
    private static void overrideBySystemProps(Properties osgiCfg, Collection<String> excluding) {
        Properties sysProps = System.getProperties();
        for (Map.Entry<Object, Object> entry : osgiCfg.entrySet()) {
            if (excluding.contains(entry.getKey())) {
                continue;
            }
            Object systemPropValue = sysProps.get(entry.getKey());
            if (systemPropValue != null && !systemPropValue.equals(entry.getValue())) {
                osgiCfg.put(entry.getKey(), systemPropValue);
            }
        }
    }
}
