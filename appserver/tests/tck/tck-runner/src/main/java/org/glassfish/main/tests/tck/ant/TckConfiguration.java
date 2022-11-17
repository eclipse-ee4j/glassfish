/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.tests.tck.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;


/**
 * Configuration of the TCK runnable by this project.
 *
 * @author David Matejcek
 */
public class TckConfiguration {

    private final Properties cfg;


    /**
     * Loads the configuration file.
     *
     * @param cfgFile
     */
    public TckConfiguration(File cfgFile) {
        this.cfg = new Properties();
        try (FileInputStream is = new FileInputStream(cfgFile)) {
            this.cfg.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load the TCK configuration from '" + cfgFile + "'!");
        }
    }


    /**
     * Loads the configuration.
     *
     * @param cfgStream
     */
    public TckConfiguration(InputStream cfgStream) {
        this.cfg = new Properties();
        try {
            try {
                this.cfg.load(cfgStream);
            } finally {
                cfgStream.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load the TCK configuration!");
        }
    }


    /**
     * @return version of the TCK ZIP artifact.
     */
    public String getTckVersion() {
        return cfg.getProperty("tck.version");
    }


    /**
     * @return .../target/jakartaeetck
     */
    public File getJakartaeeDir() {
        return new File(getTargetDir(), "jakartaeetck");
    }


    /**
     * @return .../target/jakartaeetck/docker/run_jakartaeetck.sh
     */
    public Path getJakartaeetckCommand() {
        return getJakartaeeDir().toPath().resolve(Path.of("docker", "run_jakartaeetck.sh"));
    }


    /**
     * @return .../target
     */
    public File getTargetDir() {
        String dir = cfg.getProperty("target.directory");
        if (dir == null) {
            throw new IllegalStateException("The property target.directory is not set!");
        }
        return new File(dir);
    }


    /**
     * @return pom.xml of this project.
     */
    public File getPomFile() {
        String property = cfg.getProperty("pomFile");
        if (property == null) {
            throw new IllegalStateException("The property 'pomFile' is not set!");
        }
        return new File(property);
    }


    /**
     * @return JDK home directory
     */
    public File getJdkDirectory() {
        return new File(cfg.getProperty("jdk.directory"));
    }


    /**
     * Resolves to the first value set:
     * <ol>
     * <li>System property ant.home (set in command line or Maven profile)
     * <li>Environment property ANT_HOME (set in bash environment and resolved via pom.xml)
     * <li>Default /usr/share/ant
     * </ol>
     *
     * @return Ant home directory (note: 1.9.14 recommended)
     */
    public File getAntDirectory() {
        final String antHome = cfg.getProperty("ant.directory");
        return new File(antHome == null || antHome.isEmpty() || antHome.startsWith("$") ? "/usr/share/ant" : antHome);
    }


    /**
     * @return configured maven settings.xml file.
     */
    public File getSettingsXml() {
        return new File(cfg.getProperty("settingsXmlFile"));
    }


    /**
     * @return version of the GlassFish zip artifact.
     */
    public String getGlassFishVersion() {
        return cfg.getProperty("glassfish.version");
    }


    /**
     * @return logging.properties for the TCK's java client.
     */
    public File getClientLoggingProperties() {
        return new File(cfg.getProperty("log.properties.client"));
    }


    /**
     * @return logging.properties for both servers, ri and vi.
     */
    public File getServerLoggingProperties() {
        return new File(cfg.getProperty("log.properties.server"));
    }


    /**
     * @return true if you want to see verbose TCK's harness logs.
     */
    public boolean isHarnessLoggingEnabled() {
        return Boolean.parseBoolean(cfg.getProperty("log.harness"));
    }


    /**
     * @return true if you want to see logs of the communication of the asadmin wih the server.
     */
    public boolean isAsadminLoggingEnabled() {
        return Boolean.parseBoolean(cfg.getProperty("log.asadmin"));
    }


    /**
     * Prints internal properties to string.
     */
    @Override
    public String toString() {
        return this.cfg.toString();
    }
}
