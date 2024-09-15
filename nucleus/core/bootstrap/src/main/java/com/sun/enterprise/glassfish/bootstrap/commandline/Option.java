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
package com.sun.enterprise.glassfish.bootstrap.commandline;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.sun.enterprise.glassfish.bootstrap.commandline.Arguments.DEFAULT_HTTPS_LISTENER;
import static com.sun.enterprise.glassfish.bootstrap.commandline.Arguments.DEFAULT_HTTP_LISTENER;
import static java.util.logging.Level.WARNING;

/**
 *
 * @author Ondro Mihalyi
 */
public enum Option {
    PROPERTIES("properties", "--properties=FILE",
            "Load GlassFish properties from a file. This option can be repeated to load properties from multiple files."
            + " The propertes in the file can be any of the following:\n"
            + " - Any properties supported by GlassFish Embedded.\n"
            + " - Any command line options with the name of the option as the key,"
            + " without the initial hyphens, and the value of the option as the value.\n"
            + " - Keys that start with the \"" + Arguments.COMMAND_KEY_PREFIX
            + "\" prefix, followed by any text. The value will be"
            + " treated as a command to execute at startup.\n"
            + " - Keys that start with the \"" + Arguments.DEPLOY_KEY_PREFIX
            + "\" prefix, followed by any text. The value will be"
            + " treated as an application to deploy at startup, as if it was specified on the command line.\n"
            + "For example, the GlassFish domain directory can be specified with the usual GlassFish Embedded"
            + " property \"glassfish.embedded.tmpdir=myDomainDir\", as well as with the property"
            + " \"domainDir=myDomainDir\" that represents the \"--domainDir=myDomainDir\" command-line option."
            + " A command to deploy an application can be specified via a property key"
            + " \"" + Arguments.COMMAND_KEY_PREFIX + "deploy.app=deploy --contextroot=app myapp.war\"."
            + " An application to deploy at startup with the default deploy behavior can be specified via a property key"
            + " \"" + Arguments.DEPLOY_KEY_PREFIX + "app=myapp.war\"."
            + " The property \"properties\" can also be defined in this file, pointing to another file."
            + " In that case, properties will be loaded also from that file.") {

        public void handle(String value, Arguments arguments) {
            loadPropertiesFromFile(value, arguments);
        }
    },
    HTTP_PORT("httpPort", Set.of("port", "p"), "-p=PORT, --httpPort=PORT, --port=PORT",
            "Bind the HTTP listener to the specified port. If not set, the HTTP listener binds to port"
            + " 8080 by default, unless it's disabled by the --noListener argument.") {
        @Override
        public void handle(String value, Arguments arguments) {
            setPort(DEFAULT_HTTP_LISTENER, value, arguments);
        }
    },
    HTTPS_PORT("httpsPort", "--httpsPort=PORT_NUMBER",
            "Bind the HTTPS listener to the specified port. If not set, the HTTPS listener is disabled by default.") {
        @Override
        public void handle(String value, Arguments arguments) {
            setPort(DEFAULT_HTTPS_LISTENER, value, arguments);
        }
    },
    DOMAIN_CFG_FILE("domainConfigFile", "--domainConfigFile=FILE",
            "Set the location of domain configuration file (i.e., domain.xml) using which GlassFish should run.") {
        @Override
        public void handle(String value, Arguments arguments) {
            arguments.glassFishProperties.setConfigFileURI(value);
        }
    },
    DOMAIN_DIR("domainDir", Set.of("instanceRoot"), "--domainDir=DIRECTORY, --instanceRoot=DIRECTORY",
            "Set the instance root (a.k.a. domain directory) using which GlassFish should run.") {
        @Override
        public void handle(String value, Arguments arguments) {
            arguments.glassFishProperties.setInstanceRoot(value);
        }
    },
    NO_LISTENER("noListener", Set.of("noPort"), "--noListener, --noPort",
            "Disable the HTTP listener, which is by default enabled and bound to port 8080.") {
        @Override
        public void handle(String value, Arguments arguments) {
            setPort(DEFAULT_HTTP_LISTENER, 0, arguments);
        }
    },
    AUTO_DEPLOY_DIR("autoDeployDir", "--autoDeployDir=DIRECTORY",
            "Files and directories in this directory will be deployed as applications (in random order), as if they"
            + " were specified on the command line.") {
        @Override
        public void handle(String value, Arguments arguments) {
            loadApplicationsFromDirectory(value, arguments);
        }
    },
    HELP("help", "--help", "Print this help") {
        @Override
        public void handle(String value, Arguments arguments) {
            arguments.askedForHelp = true;
        }
    };

    protected static final Logger logger = Logger.getLogger(Option.class.getName());

    private String mainName;
    private Set<String> aliases;
    private String helpText;
    private String usage;

    private Option(String mainName, Set<String> aliases, String usage, String helpText) {
        this.mainName = mainName;
        this.aliases = aliases;
        this.usage = usage;
        this.helpText = helpText;
    }

    private Option(String mainName, String usage, String helpText) {
        this(mainName, Set.of(), usage, helpText);
    }

    public static Option from(String key) throws NoSuchElementException {
        try {
            return Option.valueOf(key);
        } catch (IllegalArgumentException e) {
            final String upperCaseKey = key.toUpperCase();
            return Arrays.stream(Option.values())
                    .filter(option -> mainNameMatches(option, upperCaseKey) || aliasesMatch(option, upperCaseKey))
                    .findAny().get();
        }
    }

    public abstract void handle(String value, Arguments arguments);

    public String getMainName() {
        return mainName;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public String getHelpText() {
        return helpText;
    }

    public String getUsage() {
        return usage;
    }

    protected static boolean mainNameMatches(Option option, String upperCaseKey) {
        return option.mainName.toUpperCase().equals(upperCaseKey);
    }

    protected static boolean aliasesMatch(Option option, String upperCaseKey) {
        return option.aliases.stream().filter(alias -> alias.toUpperCase().equals(upperCaseKey)).findAny().isPresent();
    }

    protected void setPort(String listener, String value, Arguments arguments) throws NumberFormatException {
        setPort(listener, Integer.parseInt(value), arguments);
    }

    protected void setPort(String listener, int value, Arguments arguments) throws NumberFormatException {
        arguments.glassFishProperties.setPort(listener, value);
    }

    protected void loadPropertiesFromFile(String fileName, Arguments arguments) {
        try {
            final Properties properties = new Properties();
            properties.load(Files.newBufferedReader(Paths.get(fileName)));
            properties.forEach((k, v) -> {
                try {
                    arguments.setOption((String) k, (String) v);
                } catch (UnknownPropertyException e) {
                    logger.log(Level.WARNING, e, () -> "Invalid property '" + e.getKey() + "' in file " + fileName);
                }
            });
        } catch (IOException e) {
            logger.log(Level.WARNING, e, () -> "Could not read properties from file " + fileName + " - " + e.getMessage());
        }
    }

    protected void loadApplicationsFromDirectory(String directoryName, Arguments arguments) {
        final File directory = new File(directoryName);
        if (directory.isDirectory()) {
            for (File appFile : directory.listFiles()) {
                arguments.deployables.add(appFile.getAbsolutePath());
            }
        } else {
            logger.log(WARNING, () -> "The path specified with the " + this.getUsage() + " option is not a directory: " + directoryName);
        }
    }
}
