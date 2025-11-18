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
package org.glassfish.tests.embedded.runnable.tool;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.System.Logger;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.System.Logger.Level.INFO;
import static java.util.stream.Collectors.joining;

/**
 * Starts the Embedded GlassFish process.
 *
 * @author Ondro Mihalyi
 */
public final class EmbeddedGlassFishStarter {

    private static final Logger LOG = System.getLogger(EmbeddedGlassFishStarter.class.getName());

    private static final String DEBUG_PROPERTY_NAME = "glassfish.test.debug";


    /**
     * Starts the embedded glassfish jar - it will stop after it finishes deployment automatically.
     *
     * @param glassfishEmbeddedJarName
     * @param additionalArguments
     * @return the {@link Process} to wait for.
     * @throws IOException
     */
    public static Process start(String glassfishEmbeddedJarName, String... additionalArguments) throws IOException {
        return start(glassfishEmbeddedJarName, List.of(), additionalArguments);
    }


    /**
     * Starts the embedded glassfish jar - it will stop after it finishes deployment automatically.
     *
     * @param glassfishEmbeddedJarName
     * @param jvmOpts
     * @param additionalArguments
     * @return the {@link Process} to wait for.
     * @throws IOException
     */
    public static Process start(String glassfishEmbeddedJarName, List<String> jvmOpts, String... additionalArguments)
        throws IOException {
        return start(glassfishEmbeddedJarName, false, jvmOpts, additionalArguments);
    }


    /**
     * Starts the embedded glassfish jar.
     *
     * @param glassfishEmbeddedJarName
     * @param keepRunning if true, glassfish doesn't stop after deployment. You have to terminate it.
     * @param jvmOpts JVM options
     * @param additionalArguments program arguments
     * @return the {@link Process}.
     * @throws IOException
     */
    public static Process start(String glassfishEmbeddedJarName, boolean keepRunning, List<String> jvmOpts,
        String... additionalArguments) throws IOException {
        List<String> arguments = new ArrayList<>();
        arguments.add(ProcessHandle.current().info().command().get());
        getDebugArg().ifPresent(arguments::add);
        arguments.addAll(jvmOpts);
        arguments.addAll(List.of("-jar", glassfishEmbeddedJarName, "--noPort"));
        if (!keepRunning) {
            arguments.add("--stop");
        }
        for (String argument : additionalArguments) {
            arguments.add(argument);
        }
        LOG.log(INFO, () -> "Current directory: " + Paths.get(".").toFile().getAbsolutePath());
        LOG.log(INFO, () -> "Going to run Embedded GlassFish: "
            + arguments.stream().map(arg -> "'" + arg + "'").collect(joining(" ")));
        return new ProcessBuilder()
                .redirectOutput(Redirect.PIPE)
                .redirectError(Redirect.PIPE)
                .command(arguments)
                .start();
    }

    static boolean isDebugEnabled() {
        return getDebugArg().isPresent();
    }

    private static Optional<String> getDebugArg() {
        return Optional.ofNullable(System.getProperty(DEBUG_PROPERTY_NAME)).filter(debugPort -> List
            .of("none", "disabled", "false").stream().allMatch(value -> !value.equalsIgnoreCase(debugPort)));
    }
}
