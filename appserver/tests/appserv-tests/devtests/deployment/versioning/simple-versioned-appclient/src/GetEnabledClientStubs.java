/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package versionedappclient.client;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Retrieves the client stubs of the currently enabled version of an application.
 * <p>
 * The asadmin get-client-stubs command needs the exact version name, so the enabled
 * version is looked up first with asadmin list-applications.
 * <p>
 * Arguments: asadmin executable, untagged application name, target directory, deployment target.
 * The stubs of the previous run are removed first, so no enabled version means no stubs.
 */
public class GetEnabledClientStubs {

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.out.println("arguments: asadmin untaggedName dirPath target");
            System.exit(1);
        }
        final String asadmin = args[0];
        final String untaggedName = args[1];
        final File stubsDir = new File(args[2]);
        final String target = args[3];

        deleteRecursively(new File(stubsDir, untaggedName + "Client.jar").toPath());
        deleteRecursively(new File(stubsDir, untaggedName + "Client").toPath());

        final String enabledVersion = findEnabledVersion(asadmin, untaggedName, target);
        if (enabledVersion == null) {
            log("no enabled version of " + untaggedName + " on " + target);
            System.exit(1);
        }
        log("enabled version: " + enabledVersion);
        final int exitCode = run(asadmin, "get-client-stubs", "--appname=" + enabledVersion, stubsDir.getPath()).exitCode;
        log("get-client-stubs return code: " + exitCode);
        System.exit(exitCode == 0 ? 0 : 1);
    }


    private static String findEnabledVersion(String asadmin, String untaggedName, String target)
        throws IOException, InterruptedException {
        final Result result = run(asadmin, "list-applications", "--long", target);
        if (result.exitCode != 0) {
            return null;
        }
        // NAME TYPE STATUS
        for (String line : result.output) {
            final String[] columns = line.trim().split("\\s+");
            if (columns.length < 3) {
                continue;
            }
            final String name = columns[0];
            final boolean isVersionOfApp = name.equals(untaggedName) || name.startsWith(untaggedName + ":");
            if (isVersionOfApp && "enabled".equals(columns[columns.length - 1])) {
                return name;
            }
        }
        return null;
    }


    private static Result run(String... command) throws IOException, InterruptedException {
        log("running " + String.join(" ", command));
        final Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        final List<String> output;
        try (Stream<String> lines = process.inputReader(StandardCharsets.UTF_8).lines()) {
            output = lines.peek(GetEnabledClientStubs::log).toList();
        }
        return new Result(process.waitFor(), output);
    }


    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(path)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        }
    }


    private static void log(String message) {
        System.err.println("[versionedappclient.client.GetEnabledClientStubs]:: " + message);
    }


    private record Result(int exitCode, List<String> output) {
    }
}
