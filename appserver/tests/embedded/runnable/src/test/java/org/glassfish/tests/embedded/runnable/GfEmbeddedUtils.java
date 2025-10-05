/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.tests.embedded.runnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.System.err;
import static java.util.stream.Collectors.joining;

/**
 *
 * @author Ondro Mihalyi
 */
public class GfEmbeddedUtils {

    public static final String DEBUG_PROPERTY_NAME = "glassfish.test.debug.port";

    public static Process runGlassFishEmbedded(String glassfishEmbeddedJarName, String... additionalArguments) throws
IOException {
        return runGlassFishEmbedded(glassfishEmbeddedJarName, List.of(), additionalArguments);
    }

    public static Process runGlassFishEmbedded(String glassfishEmbeddedJarName, List<String> jvmOpts, String... additionalArguments) throws IOException {
        List<String> arguments = new ArrayList<>();
        arguments.add(ProcessHandle.current().info().command().get());
        addDebugArgsIfDebugEnabled(arguments);
        arguments.addAll(jvmOpts);
        arguments.addAll(List.of(
                "-jar", glassfishEmbeddedJarName,
                "--noPort",
                "--stop"));
        for (String argument : additionalArguments) {
            arguments.add(argument);
        }
        System.out.println("\nCurrent directory: " + Paths.get(".").toFile().getAbsolutePath());
        System.out.println("Going to run Embedded GlassFish: " + arguments.stream()
                .map(arg -> "'" + arg + "'")
                .collect(joining(" ")) + "\n");
        return new ProcessBuilder()
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .command(arguments)
                .start();
    }

    public static Stream<String> outputToStreamOfLines(Process gfEmbeddedProcess) {
        InputStream gfEmbeddedOutput = gfEmbeddedProcess.getErrorStream();
        return new BufferedReader(
                new InputStreamReader(gfEmbeddedOutput, StandardCharsets.UTF_8)
        )
                .lines()
                .peek(err::println);
    }

    private static void addDebugArgsIfDebugEnabled(List<String> arguments) {
        Optional.ofNullable(System.getProperty(DEBUG_PROPERTY_NAME))
                .filter(debugPort -> List.of("none", "disabled", "false").stream()
                        .allMatch(value -> !value.equalsIgnoreCase(debugPort))
                )
                .ifPresent(debugPort -> {
                    arguments.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:" + debugPort);
                });

    }

}
