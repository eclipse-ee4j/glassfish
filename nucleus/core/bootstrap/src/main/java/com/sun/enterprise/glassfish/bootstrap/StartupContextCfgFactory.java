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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTALL_ROOT_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTALL_ROOT_URI_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTANCE_ROOT_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INSTANCE_ROOT_URI_PROP_NAME;
import static com.sun.enterprise.module.bootstrap.ArgumentManager.argsToMap;
import static com.sun.enterprise.module.bootstrap.StartupContext.STARTUP_MODULE_NAME;
import static com.sun.enterprise.module.bootstrap.StartupContext.TIME_ZERO_NAME;

final class StartupContextCfgFactory {


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

        StartupContextCfg cfg = new StartupContextCfg(platform, properties);
        addRawStartupInfo(args, cfg);

        return mergePlatformConfiguration(cfg);
    }


    /**
     * Need the raw unprocessed args for RestartDomainCommand in case we were NOT started by CLI
     *
     * @param args raw args to this main()
     * @param cfg the properties to save as a system property
     */
    private static void addRawStartupInfo(final String[] args, final StartupContextCfg cfg) {
        if (wasStartedByCLI(cfg)) {
            return;
        }
        // no sense doing this if we were started by CLI...
        cfg.setProperty(BootstrapKeys.ORIGINAL_CP, System.getProperty("java.class.path"));
        cfg.setProperty(BootstrapKeys.ORIGINAL_CN, ASMain.class.getName());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(BootstrapKeys.ARG_SEP);
            }
            sb.append(args[i]);
        }
        cfg.setProperty(BootstrapKeys.ORIGINAL_ARGS, sb.toString());
    }


    private static boolean wasStartedByCLI(final StartupContextCfg cfg) {
        // if we were started by CLI there will be some special args set...
        return cfg.getProperty("-asadmin-classpath") != null
            && cfg.getProperty("-asadmin-classname") != null
            && cfg.getProperty("-asadmin-args") != null;
    }


    private static StartupContextCfg mergePlatformConfiguration(StartupContextCfg cfg) {
        final Properties osgiCfg;
        try {
            osgiCfg = OsgiPlatformFactory.getOsgiPlatformAdapter(cfg).readPlatformConfiguration();
        } catch (IOException e) {
            throw new IllegalStateException("The OSGI configuration could not be loaded!", e);
        }
        osgiCfg.putAll(cfg.toProperties());
        // Perform variable substitution for system properties.
        for (String name : osgiCfg.stringPropertyNames()) {
            osgiCfg.setProperty(name, FelixUtil.substVars(osgiCfg.getProperty(name), name, null, osgiCfg));
        }

        // Starting with GlassFish 3.1.2, we allow user to overrride values specified in OSGi config file by
        // corresponding values as set via System properties. There are two properties that we must always read
        // from OSGi config file. They are felix.fileinstall.dir and felix.fileinstall.log.level, as their values have
        // changed incompatibly from 3.1 to 3.1.1, but we are not able to change domain.xml in 3.1.1 for
        // compatibility reasons.
        overrideBySystemProps(osgiCfg, Arrays.asList("felix.fileinstall.dir", "felix.fileinstall.log.level"));
        return new StartupContextCfg(cfg.getPlatform(), osgiCfg);
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
