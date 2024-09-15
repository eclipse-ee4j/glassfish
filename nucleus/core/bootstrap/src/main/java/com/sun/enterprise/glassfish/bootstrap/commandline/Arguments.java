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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.embeddable.GlassFishProperties;

/**
 *
 * @author Ondro Mihalyi
 */
public class Arguments {

    public static final String DEFAULT_HTTP_LISTENER = "http-listener";
    public static final String DEFAULT_HTTPS_LISTENER = "https-listener";
    public static final String DEFAULT_PROPERTIES_FILE = "glassfish.properties";
    public static final String DEFAULT_AUTO_DEPLOY_DIR = "autodeploy";
    public static final String DEFAULT_DOMAIN_DIRECTORY = "glassfish-domain";
    public static final String DEFAULT_DOMAIN_CONFIG_FILE = "domain.xml";
    public static final String COMMAND_KEY_PREFIX = "command.";
    public static final String DEPLOY_KEY_PREFIX = "deploy.";
    private static final int HELP_LINE_LENGTH = 80;              // wrap to max 80 columns per line
    private static final String HELP_FIRST_LINE_INDENT = "    "; // 4 spaces to align with man page content
    private static final String HELP_LINE_INDENT = "        ";   // 8 spaces to align with man page content

    public final List<String> commands = new ArrayList<>();
    public final GlassFishProperties glassFishProperties = new GlassFishProperties();
    public final List<String> deployables = new ArrayList<>();
    public boolean askedForHelp = false;

    private static final Logger logger = Logger.getLogger(Arguments.class.getName());

    public void setDefaults() {
        glassFishProperties.setPort(DEFAULT_HTTP_LISTENER, 8080);
        if (new File(DEFAULT_PROPERTIES_FILE).isFile()) {
            try {
                setOption(Option.PROPERTIES, DEFAULT_PROPERTIES_FILE);
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, e, () -> "Could not read properties from the implicit properties file "
                        + DEFAULT_PROPERTIES_FILE + " - " + e.getMessage());
            }
        }
        if (new File(DEFAULT_AUTO_DEPLOY_DIR).isDirectory()) {
            try {
                setOption(Option.AUTO_DEPLOY_DIR, DEFAULT_AUTO_DEPLOY_DIR);
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, e, () -> "Could not find applications in the implicit directory "
                        + DEFAULT_AUTO_DEPLOY_DIR + " - " + e.getMessage());
            }
        }
        if (new File(DEFAULT_DOMAIN_DIRECTORY).isDirectory()) {
            try {
                setOption(Option.DOMAIN_DIR, DEFAULT_DOMAIN_DIRECTORY);
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, e, () -> "Corrupted implicit domain directory "
                        + DEFAULT_DOMAIN_DIRECTORY + " - " + e.getMessage());
            }
        }
        if (new File(DEFAULT_DOMAIN_CONFIG_FILE).isFile()) {
            try {
                setOption(Option.DOMAIN_CFG_FILE, DEFAULT_DOMAIN_CONFIG_FILE);
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, e, () -> "Corrupted implicit domain configuration file "
                        + DEFAULT_DOMAIN_CONFIG_FILE + " - " + e.getMessage());
            }
        }
    }

    /**
     * Set an option or an argument with an optional value. If value not applicable, it should be {@code null}.
     * @param option Chosen option to set
     * @param value Value of the option or {@code null}
     */
    public void setOption(Option option, String value) {
        option.handle(value, this);
    }

    /**
     * Add a command to execute at start. Commands are executed in the order in which they are added.
     * @param command
     */
    public void addCommand(String command) {
        commands.add(command);
    }

    /**
     * Add a deployable (e.g. a WAR application) to deploy at start. Deployables are deployed in the order in which they are added.
     * @param deployable
     */
    public void addDeployable(String deployable) {
        deployables.add(deployable);
    }

    void setOption(String key, String value) throws UnknownPropertyException {
        try {
            Option option;
            try {
                option = Option.from(key);
            } catch (NoSuchElementException e) {
                if (value != null) {
                    if (key.startsWith(COMMAND_KEY_PREFIX)) {
                        commands.add(value);
                    } else if (key.startsWith(DEPLOY_KEY_PREFIX)) {
                        deployables.add(value);
                    } else {
                        glassFishProperties.setProperty(key, value);
                    }
                } else {
                    throw new UnknownPropertyException(key, value);
                }
                return;
            }

            option.handle(value, this);

        } catch (RuntimeException e) {
            logger.log(Level.WARNING, e, () -> "Could not set property " + key + " to value " + value + " - " + e.getMessage());
        }
    }

    /**
     * Print help information into the standard output
     */
    public void printHelp() {
        printHelp(System.out::println);
    }

    /**
     * Produce help information
     * @return All the help information as String
     */
    public String getHelpText() {
        StringBuilder sb = new StringBuilder();
        printHelp(sb::append);
        return sb.toString();
    }

    /**
     * Produce help information and execute lineConsumer for every line.
     * @param lineConsumer Called for every line
     */
    public void printHelp(Consumer<String> lineConsumer) {
        try (final BufferedReader manPageReader = openManPageReader()) {
            manPageReader.lines()
                    .map(this::replaceArguments)
                    .map(this::replaceOptions)
                    .forEach(lineConsumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String replaceArguments(String line) {
        final WordWrapper wordWrapper = new WordWrapper(HELP_LINE_LENGTH, 40, HELP_LINE_INDENT);

        final String arguments = Stream.concat(
                Arrays.stream(Option.values())
                        .map(option -> "[" + option.getUsage() + "]"),
                Stream.of("[applications or admin commands...]")
        )
                .map(wordWrapper::map)
                .collect(Collectors.joining(" "));
        return line.replace("${ARGUMENTS}", arguments);
    }

    private String replaceOptions(String line) {
        final String options = Arrays.stream(Option.values())
                .map(option -> {
                    final WordWrapper wordWrapper = new WordWrapper(HELP_LINE_LENGTH, HELP_LINE_INDENT.length(), HELP_LINE_INDENT);
                    return HELP_FIRST_LINE_INDENT + option.getUsage() + "\n"
                            + HELP_LINE_INDENT
                            + getStreamOfWords(option.getHelpText())
                                    .map(wordWrapper::map)
                                    .collect(Collectors.joining(" "));
                })
                .collect(Collectors.joining("\n\n"));
        return line.replace(
                "${OPTIONS}", options);
    }

    protected Stream<String> getStreamOfWords(String helpText) {
        return Arrays.stream(helpText.split(" "));
    }

    private BufferedReader openManPageReader() {
        final InputStream manPageInputStream = this.getClass().getClassLoader().getResourceAsStream("manpages/glassfish-embedded.1");
        return new BufferedReader(new InputStreamReader(manPageInputStream, StandardCharsets.UTF_8));
    }

}
