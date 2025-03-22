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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.System.err;

/**
 *
 * @author Ondro Mihalyi
 */
public class GfEmbeddedUtils {

    public static Process runGlassFishEmbedded(String glassfishEmbeddedJarName, String... additionalArguments) throws IOException {
        List<String> arguments = new ArrayList<>();
        arguments.addAll(List.of(ProcessHandle.current().info().command().get(),
//                "-Xrunjdwp:transport=dt_socket,server=y,suspend=y", // enable debugging on random port
                "-jar", glassfishEmbeddedJarName,
                "--stop"));
        for (String argument : additionalArguments) {
            arguments.add(argument);
        }
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
        ).lines();
    }

}
