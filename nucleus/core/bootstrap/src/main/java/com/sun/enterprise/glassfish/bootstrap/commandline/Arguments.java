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
import java.util.concurrent.atomic.AtomicInteger;
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
    final String HELP_FIRST_LINE_INDENT = "    "; // 4 spaces to align with man page content
    final String HELP_LINE_INDENT = "        "; // 8 spaces to align with man page content
    public final List<String> commands = new ArrayList<>();
    public final GlassFishProperties glassFishProperties = new GlassFishProperties();
    public final List<String> deployables = new ArrayList<>();
    public boolean askedForHelp = false;

    private static final Logger logger = Logger.getLogger(Arguments.class.getName());

    public Arguments() {
        glassFishProperties.setPort(DEFAULT_HTTP_LISTENER, 8080);
        if (new File(CommandLineParser.DEFAULT_PROPERTIES_FILE).isFile()) {
            setProperty(Option.PROPERTIES, CommandLineParser.DEFAULT_PROPERTIES_FILE);
        }
    }

    void setProperty(Option option, String value) {
        try {
            setProperty(option.getMainName(), value);
        } catch (UnknownPropertyException ex) {
            throw new Error("Should not happen");
        }
    }

    void setProperty(String key, String value) throws UnknownPropertyException {
        try {
            Option option;
            try {
                option = Option.from(key);
            } catch (NoSuchElementException e) {
                if (value != null) {
                    glassFishProperties.setProperty(key, value);
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

    public void printHelp() throws IOException {
        try (final BufferedReader manPageReader = openManPageReader()) {
            manPageReader.lines()
                    .map(this::replaceArguments)
                    .map(this::replaceOptions)
                    .forEach(System.out::println);
        }
    }

    private String replaceArguments(String line) {
        final int MAX_LINE_LENGTH = 80;
        AtomicInteger characterCount = new AtomicInteger(40); // 40 characters already in the static manpage template

        final String arguments = Stream.concat(
                    Arrays.stream(Option.values())
                            .map(option -> "[" + option.getUsage() + "]"),
                    Stream.of("[applications or admin commands...]")
                )
                .peek(optText -> characterCount.addAndGet(optText.length() + 1))
                .map(optionText -> {
                    if (characterCount.get() > MAX_LINE_LENGTH) {
                        final String newOptionText = HELP_LINE_INDENT + optionText;
                        characterCount.set(newOptionText.length());
                        return "\n" + newOptionText;
                    } else {
                        return optionText;
                    }
                })
                .collect(Collectors.joining(" "));
        return line.replace("${ARGUMENTS}", arguments);
    }

    private String replaceOptions(String line) {
        final String options = Arrays.stream(Option.values())
                .map(option -> {
                    return HELP_FIRST_LINE_INDENT + option.getUsage() + "\n"
                            + option.getHelpText().lines()
                                    .map(aLine -> HELP_LINE_INDENT + aLine)
                                    .collect(Collectors.joining("\n"));
                })
                .collect(Collectors.joining("\n\n"));
        return line.replace(
                "${OPTIONS}", options);
    }

    private BufferedReader openManPageReader() {
        final InputStream manPageInputStream = this.getClass().getClassLoader().getResourceAsStream("manpages/glassfish-embedded.1");
        return new BufferedReader(new InputStreamReader(manPageInputStream, StandardCharsets.UTF_8));
    }

}
