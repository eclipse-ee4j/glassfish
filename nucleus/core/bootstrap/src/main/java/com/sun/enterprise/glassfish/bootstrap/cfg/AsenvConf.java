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
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.enterprise.glassfish.bootstrap.log.LogFacade.BOOTSTRAP_LOGGER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.FINE;

/**
 * The asenv.conf file content.
 */
public class AsenvConf {

    private static final Pattern LINE = Pattern.compile("(?i)(set +)?([^=]*)=\"?([^\"]*)\"?");

    private final Properties properties;

    public AsenvConf(File asenv, File configDir) {
        this.properties = new Properties();
        if (!asenv.exists()) {
            BOOTSTRAP_LOGGER.log(FINE, "{0} not found, ignoring", asenv.getAbsolutePath());
            return;
        }
        try (LineNumberReader lnReader = new LineNumberReader(new FileReader(asenv, UTF_8))) {
            String line = lnReader.readLine();
            // most of the asenv.conf values have surrounding "", remove them
            // and on Windows, they start with SET XXX=YYY
            while (line != null) {
                Matcher m = LINE.matcher(line);
                if (m.matches()) {
                    File configFile = new File(m.group(3));
                    if (!configFile.isAbsolute()) {
                        configFile = new File(configDir, m.group(3));
                        if (configFile.exists()) {
                            properties.put(m.group(2), configFile.getAbsolutePath());
                        } else {
                            properties.put(m.group(2), m.group(3));
                        }
                    } else {
                        properties.put(m.group(2), m.group(3));
                    }
                }
                line = lnReader.readLine();
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Error opening asenv.conf file.", ioe);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }


    public void mirrorToSystemProperties() {
        for (String name : properties.stringPropertyNames()) {
            System.setProperty(name, properties.getProperty(name));
        }
    }


    public Properties toProperties() {
        return (Properties) properties.clone();
    }
}
