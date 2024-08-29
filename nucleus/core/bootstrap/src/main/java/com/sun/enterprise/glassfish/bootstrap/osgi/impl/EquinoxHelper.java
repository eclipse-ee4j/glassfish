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

public class EquinoxHelper extends PlatformHelper {

    /**
     * If equinox is installed under glassfish/eclipse this would be the
     * glassfish/eclipse/plugins dir that contains the equinox jars can be null
     */
    private static File pluginsDir;
    private final File fwDir;

    public EquinoxHelper(Properties properties) {
        super(properties);
        String fwPath = System.getenv("EQUINOX_HOME");
        if (fwPath == null) {
            fwPath = new File(glassfishDir, "osgi/equinox").getAbsolutePath();
        }
        this.fwDir = new File(fwPath);
        if (!fwDir.exists()) {
            throw new RuntimeException("Can't locate Equinox at " + fwPath);
        }
    }

    @Override
    public void addFrameworkJars(ClassPathBuilder cpb) throws IOException {
        // Add all the jars to classpath for the moment, since the jar name
        // is not a constant.
        if (pluginsDir == null) {
            cpb.addJarFolder(fwDir);
        } else {
            cpb.addGlob(pluginsDir, "org.eclipse.osgi_*.jar");
        }
    }

    @Override
    public Properties readPlatformConfiguration() throws IOException {
        // GlassFish filesystem layout does not recommend use of upper case char in file names.
        // So, we can't use ${GlassFish_Platform} to generically set the cache dir.
        // Hence, we set it here.
        Properties platformConfig = super.readPlatformConfiguration();
        platformConfig.setProperty(FRAMEWORK_STORAGE, new File(domainDir, "osgi-cache/equinox/").getAbsolutePath());
        return platformConfig;
    }
}