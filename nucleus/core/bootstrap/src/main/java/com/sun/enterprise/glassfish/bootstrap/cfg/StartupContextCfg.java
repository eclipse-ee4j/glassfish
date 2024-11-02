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
import java.nio.file.Path;
import java.util.Properties;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.PLATFORM_PROPERTY_KEY;

public class StartupContextCfg {

    private final OsgiPlatform platform;
    private final Properties properties;
    private final ServerFiles files;


    public StartupContextCfg(OsgiPlatform platform, ServerFiles files, Properties properties) {
        properties.setProperty(PLATFORM_PROPERTY_KEY, platform.name());
        this.platform = platform;
        this.properties = properties;
        this.files = files;
    }


    /**
     * @return {@link OsgiPlatform} from constructor
     */
    public OsgiPlatform getPlatform() {
        return platform;
    }


    public Path getInstallRoot() {
        return files.getInstallRoot();
    }


    public Path getInstanceRoot() {
        return files.getInstanceRoot();
    }


    public File getFileUnderInstallRoot(Path relativePath) {
        return files.getFileUnderInstallRoot(relativePath);
    }


    public File getFileUnderInstanceRoot(Path relativePath) {
        return files.getFileUnderInstanceRoot(relativePath);
    }


    /**
     * @param envKey Key to use with {@link System#getenv(String)}
     * @param sysPropsKey Key to use with {@link System#getProperty(String)}
     * @param defaultSubdir Relative path to {@link #getInstanceRoot()}
     * @return first configured directory.
     * @throws IllegalArgumentException if the directory does not exist.
     */
    public File getOsgiHome(String envKey, String sysPropsKey, Path defaultSubdir) {
        return files.getOsgiHome(envKey, sysPropsKey, defaultSubdir);
    }


    public String getProperty(String key) {
        return properties.getProperty(key);
    }


    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }


    public boolean isPropertySet(String key) {
        return properties.containsKey(key);
    }


    public void setAll(Properties source) {
        properties.putAll(source);
    }


    /**
     * Note: it is expected that you don't use this instance after this call.
     *
     * @return internal properties.
     */
    public Properties toProperties() {
        return properties;
    }
}
