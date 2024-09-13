/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.embeddable.GlassFishProperties;

/**
 *
 * @author Ondro Mihalyi
 */
public class CommandLineParser {

    public static String DEFAULT_PROPERTIES_FILE = "glassfish.properties";

    private static Logger logger = Logger.getLogger(CommandLineParser.class.getName());

    public static class Arguments {

        private static final String DEFAULT_HTTP_LISTENER = "http-listener";
        private static final String DEFAULT_HTTPS_LISTENER = "https-listener";
        private static final String KEY_PROPERTIES = "properties";
        private static final String KEY_HTTP_PORT = "httpPort";
        private static final String KEY_HTTPS_PORT = "httpsPort";
        private static final String KEY_DOMAIN_CFG_FILE = "domainConfigFile";
        private static final String KEY_DOMAIN_DIR = "domainDir";
        private static final String KEY_NO_LISTENER = "noHttpListener";

        public final List<String> commands = new ArrayList<>();
        public final GlassFishProperties glassFishProperties = new GlassFishProperties();
        public final List<String> deployables = new ArrayList<>();

        public Arguments() {
            glassFishProperties.setPort(DEFAULT_HTTP_LISTENER, 8080);
            if (new File(DEFAULT_PROPERTIES_FILE).isFile()) {
                setProperty(KEY_PROPERTIES, DEFAULT_PROPERTIES_FILE);
            }
        }

        public void setProperty(String key, String value) {
            try {
                switch (key) {
                    case KEY_PROPERTIES:
                        loadPropertiesFromFile(value);
                        break;
                    case KEY_HTTP_PORT:
                        glassFishProperties.setPort(DEFAULT_HTTP_LISTENER, Integer.parseInt(value));
                        break;
                    case KEY_HTTPS_PORT:
                        glassFishProperties.setPort(DEFAULT_HTTPS_LISTENER, Integer.parseInt(value));
                        break;
                    case KEY_DOMAIN_CFG_FILE:
                        glassFishProperties.setConfigFileURI(value);
                        break;
                    case KEY_DOMAIN_DIR:
                        glassFishProperties.setInstanceRoot(value);
                        break;
                    case KEY_NO_LISTENER:
                        glassFishProperties.setPort(DEFAULT_HTTP_LISTENER, 0);
                        break;
                    default:
                        glassFishProperties.setProperty(key, value);
                }
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, e, () -> "Could not set property " + key + " to value " + value + " - " + e.getMessage());
            }
        }

        private void loadPropertiesFromFile(String fileName) {
            try {
                final Properties properties = new Properties();
                properties.load(Files.newBufferedReader(Paths.get(fileName)));
                properties.forEach((k, v) -> this.setProperty((String) k, (String) v));
            } catch (IOException e) {
                logger.log(Level.WARNING, e, () -> "Could not read properties from file " + fileName + " - " + e.getMessage());
            }
        }
    }

    public Arguments parse(String[] commandLineArgs) {
        Arguments arguments = new Arguments();
        for (String arg : commandLineArgs) {
            if (arg.startsWith("--")) {
                String[] keyValue = arg.substring(2).split("=", 2);
                if (keyValue.length == 2) {
                    arguments.setProperty(keyValue[0], keyValue[1]);
                } else {
                    arguments.setProperty(keyValue[0], null); // No value, it's a flag
                }
            }
            if (isdeployable(arg)) {
                arguments.deployables.add(arg);
            } else {
                arguments.commands.add(arg);
            }
        }
        return arguments;
    }

    private boolean isdeployable(String arg) {
        final File file = new File(arg);
        return file.exists();
    }

}
