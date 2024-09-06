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

package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys;
import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextCfg;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.OsgiPlatform;
import com.sun.enterprise.module.bootstrap.StartupContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTALL_ROOT_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTALL_ROOT_URI_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTANCE_ROOT_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTANCE_ROOT_URI_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.log.LogFacade.BOOTSTRAP_FMWCONF;
import static com.sun.enterprise.glassfish.bootstrap.log.LogFacade.BOOTSTRAP_LOGGER;
import static com.sun.enterprise.module.bootstrap.ArgumentManager.argsToMap;
import static com.sun.enterprise.module.bootstrap.StartupContext.STARTUP_MODULE_NAME;
import static com.sun.enterprise.module.bootstrap.StartupContext.TIME_ZERO_NAME;
import static java.util.logging.Level.INFO;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE;

final class StartupContextCfgFactory {

    /** Location of the unified config properties file relative to the domain directory */
    private static final Path CONFIG_PROPERTIES = Path.of("config", "osgi.properties");


    private StartupContextCfgFactory() {
    }


    static StartupContextCfg createStartupContextCfg(OsgiPlatform platform, File installRoot, File instanceRoot, String[] args) {
        Properties properties = argsToMap(args);
        properties.setProperty(TIME_ZERO_NAME, Long.toString(System.currentTimeMillis()));

        properties.setProperty(INSTALL_ROOT_PROP_NAME, installRoot.getAbsolutePath());
        properties.setProperty(INSTALL_ROOT_URI_PROP_NAME, installRoot.toURI().toString());

        properties.setProperty(INSTANCE_ROOT_PROP_NAME, instanceRoot.getAbsolutePath());
        properties.setProperty(INSTANCE_ROOT_URI_PROP_NAME, instanceRoot.toURI().toString());

        if (properties.getProperty(STARTUP_MODULE_NAME) == null) {
            properties.setProperty(STARTUP_MODULE_NAME, BootstrapKeys.GF_KERNEL);
        }

        // temporary hack until CLI does that for us.
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-upgrade")) {
                if (i + 1 < args.length && !args[i + 1].equals("false")) {
                    properties.setProperty(StartupContext.STARTUP_MODULESTARTUP_NAME, "upgrade");
                }
            }
        }

        addRawStartupInfo(args, properties);

        return mergePlatformConfiguration(platform, properties);
    }


    /**
     * Need the raw unprocessed args for RestartDomainCommand in case we were NOT started by CLI
     *
     * @param args raw args to this main()
     * @param cfg the properties to save as a system property
     */
    private static void addRawStartupInfo(final String[] args, final Properties properties) {
        if (wasStartedByCLI(properties)) {
            return;
        }
        // no sense doing this if we were started by CLI...
        properties.setProperty(BootstrapKeys.ORIGINAL_CP, System.getProperty("java.class.path"));
        properties.setProperty(BootstrapKeys.ORIGINAL_CN, ASMain.class.getName());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(BootstrapKeys.ARG_SEP);
            }
            sb.append(args[i]);
        }
        properties.setProperty(BootstrapKeys.ORIGINAL_ARGS, sb.toString());
    }


    private static boolean wasStartedByCLI(final Properties properties) {
        // if we were started by CLI there will be some special args set...
        return properties.getProperty("-asadmin-classpath") != null
            && properties.getProperty("-asadmin-classname") != null
            && properties.getProperty("-asadmin-args") != null;
    }


    private static StartupContextCfg mergePlatformConfiguration(OsgiPlatform platform, Properties properties) {
        final StartupContextCfg cfg = new StartupContextCfg(platform, properties);
        final Properties platformCfg;
        try {
            platformCfg = readPlatformConfiguration(cfg);
        } catch (IOException e) {
            throw new IllegalStateException("The OSGI configuration could not be loaded!", e);
        }
        final String storageDirectoryName = platform.getFrameworkStorageDirectoryName();
        if (storageDirectoryName != null) {
            File frameworkStorage = cfg.getFileUnderInstanceRoot(Path.of("osgi-cache", storageDirectoryName));
            platformCfg.setProperty(FRAMEWORK_STORAGE, frameworkStorage.getAbsolutePath());
        }
        platformCfg.putAll(cfg.toProperties());
        // Perform variable substitution for system properties.
        for (String name : platformCfg.stringPropertyNames()) {
            platformCfg.setProperty(name, FelixUtil.substVars(platformCfg.getProperty(name), name, null, platformCfg));
        }

        // Starting with GlassFish 3.1.2, we allow user to overrride values specified in OSGi config file by
        // corresponding values as set via System properties. There are two properties that we must always read
        // from OSGi config file. They are felix.fileinstall.dir and felix.fileinstall.log.level, as their values have
        // changed incompatibly from 3.1 to 3.1.1, but we are not able to change domain.xml in 3.1.1 for
        // compatibility reasons.
        overrideBySystemProps(platformCfg, Arrays.asList("felix.fileinstall.dir", "felix.fileinstall.log.level"));
        return new StartupContextCfg(platform, platformCfg);
    }


    /**
     * @return platform specific configuration information
     * @throws IOException if the configuration could not be loaded
     */
    private static Properties readPlatformConfiguration(StartupContextCfg cfg) throws IOException {
        Properties platformConfig = new Properties();
        final File configFile = getFrameworkConfigFile(cfg);
        if (configFile == null) {
            return platformConfig;
        }
        try (InputStream in = new FileInputStream(configFile)) {
            platformConfig.load(in);
        }
        return platformConfig;
    }


    private static File getFrameworkConfigFile(StartupContextCfg cfg) {
        // First we search in domainDir. If it's not found there, we fall back on installDir
        File osgiPropertiesFile = cfg.getFileUnderInstanceRoot(CONFIG_PROPERTIES);
        if (osgiPropertiesFile.exists()) {
            BOOTSTRAP_LOGGER.log(INFO, BOOTSTRAP_FMWCONF, osgiPropertiesFile);
            return osgiPropertiesFile;
        }
        return cfg.getFileUnderInstallRoot(CONFIG_PROPERTIES);
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
