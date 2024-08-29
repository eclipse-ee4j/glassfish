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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.osgi.framework.Constants.FRAMEWORK_STORAGE;

public class FelixHelper extends PlatformHelper {
    private static final String FELIX_HOME = "FELIX_HOME";

    /**
     * Home of FW installation relative to Glassfish root installation.
     */
    public static final String GF_FELIX_HOME = "osgi/felix";

    /**
     * Location of the config properties file relative to the domain directory
     */
    public static final String CONFIG_PROPERTIES = "config/osgi.properties";

    private final File fwDir;

    /**
     * @param properties
     */
    public FelixHelper(Properties properties) {
        super(properties);
        String fwPath = System.getenv(FELIX_HOME);
        if (fwPath == null) {
            // try system property, which comes from asenv.conf
            fwPath = System.getProperty(FELIX_HOME, new File(glassfishDir, GF_FELIX_HOME).getAbsolutePath());
        }
        fwDir = new File(fwPath);
        if (!fwDir.exists()) {
            throw new RuntimeException("Can't locate Felix at " + fwPath);
        }
    }

    @Override
    public void addFrameworkJars(ClassPathBuilder cpb) throws IOException {
        File felixJar = new File(fwDir, "bin/felix.jar");
        cpb.addJar(felixJar);
    }

    @Override
    public Properties readPlatformConfiguration() throws IOException {
        // GlassFish filesystem layout does not recommend use of upper case char in file names.
        // So, we can't use ${GlassFish_Platform} to generically set the cache dir.
        // Hence, we set it here.
        Properties platformConfig = super.readPlatformConfiguration();
        platformConfig.setProperty(FRAMEWORK_STORAGE, new File(domainDir, "osgi-cache/felix/").getAbsolutePath());
        return platformConfig;
    }
}