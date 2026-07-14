/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.jms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getGlassFishDirectory;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenMqAdminServiceDefaultCredentialsTest {

    private static final Asadmin ASADMIN = getAsadmin();

    @Test
    void defaultAdminCredentialsCannotConnectToOpenMqAdminService() throws Exception {
        // Ensure the managed broker is up before invoking OpenMQ admin tooling.
        // jms-ping requires access to JMS admin so this also tests that GlassFish knows the correct admin password
        assertThat(ASADMIN.exec("jms-ping"), asadminOK());

        Path passwordFile = Files.createTempFile("imqcmd", ".pwd");
        try {
            Files.writeString(passwordFile, "imq.imqcmd.password=admin\n", UTF_8);

            CommandResult result = runImqcmd(passwordFile);

            assertFalse(result.success(),
                "imqcmd unexpectedly succeeded with default OpenMQ admin credentials. Output: " + result.output());
            assertTrue(result.output().contains("C4060"),
                "Expected OpenMQ admin service rejection (C4060/Login failed). Output: "
                    + result.output());
        } finally {
            Files.deleteIfExists(passwordFile);
        }
    }

    private static CommandResult runImqcmd(Path passwordFile) throws IOException, InterruptedException {
        String suffix = isWindows() ? ".exe" : "";
        Path imqcmd = getGlassFishDirectory().toPath().resolveSibling("mq").resolve("bin").resolve("imqcmd" + suffix);
        assertTrue(Files.isRegularFile(imqcmd), "Missing OpenMQ imqcmd executable: " + imqcmd);

        Process process = new ProcessBuilder(
            imqcmd.toString(),
            "-javahome", System.getProperty("java.home"),
            "query", "bkr",
            "-b", "localhost:7676",
            "-u", "admin",
            "-passfile", passwordFile.toAbsolutePath().toString())
            .redirectErrorStream(true)
            .start();

        boolean finished = process.waitFor(Duration.ofSeconds(30).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        assertTrue(finished, "imqcmd command timed out");

        String output;
        try (var in = process.getInputStream()) {
            output = new String(in.readAllBytes(), UTF_8);
        }
        return new CommandResult(process.exitValue() == 0, output);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    private record CommandResult(boolean success, String output) {
    }
}
