/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.glassfish.main.jul.formatter.OneLineFormatter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the output for the user is not affected by the logging configuration.
 */
public class AsadminOutputTest {

    private static final String NL = System.lineSeparator();

    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    private final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    public void redirect() {
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(stdout, true, UTF_8));
        System.setErr(new PrintStream(stderr, true, UTF_8));
    }

    @AfterEach
    public void restore() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void resultIsPrintedEvenWhenTerse() {
        AsadminOutput.result("domain1 running");
        AsadminOutput.result("ends with new line\n");
        AsadminOutput.result(null);
        AsadminOutput.result("");
        assertAll(
            () -> assertEquals("domain1 running" + NL + "ends with new line\n", stdout()),
            () -> assertEquals("", stderr())
        );
    }

    @Test
    public void messageIsSuppressedWhenTerse() {
        AsadminOutput.message(true, "Command version executed successfully.");
        AsadminOutput.message(false, "Command list-domains executed successfully.");
        assertAll(
            () -> assertEquals("Command list-domains executed successfully." + NL, stdout()),
            () -> assertEquals("", stderr())
        );
    }

    @Test
    public void errorGoesToStdErr() {
        AsadminOutput.error("Remote server does not listen for requests.");
        assertAll(
            () -> assertEquals("", stdout()),
            () -> assertEquals("Remote server does not listen for requests." + NL, stderr())
        );
    }

    @Test
    public void userMessagesIgnoreDiagnosticFormatter() {
        CLILoggerHandler handler = new CLILoggerHandler(false, new OneLineFormatter());
        handler.publish(record(Level.INFO, "Eclipse GlassFish 8"));
        handler.publish(record(Level.FINE, "Command version executed successfully."));
        handler.publish(record(Level.SEVERE, "Command failed."));
        assertAll(
            () -> assertEquals("Eclipse GlassFish 8" + NL + "Command version executed successfully." + NL, stdout()),
            () -> assertEquals("Command failed." + NL, stderr())
        );
    }

    @Test
    public void diagnosticsGoToStdErrWithFormatter() {
        CLILoggerHandler handler = new CLILoggerHandler(false, new OneLineFormatter());
        handler.publish(record(Level.FINER, "Prepare"));
        handler.publish(record(Level.FINEST, "Processing line"));
        final String err = stderr();
        assertAll(
            () -> assertEquals("", stdout()),
            () -> assertTrue(err.contains("FINER") && err.contains("Prepare"), err),
            () -> assertTrue(err.contains("FINEST") && err.contains("Processing line"), err),
            () -> assertNotEquals("Prepare" + NL + "Processing line" + NL, err)
        );
    }

    @Test
    public void diagnosticsWithoutFormatterArePlain() {
        CLILoggerHandler handler = new CLILoggerHandler(false, null);
        handler.publish(record(Level.FINER, "Prepare"));
        assertAll(
            () -> assertEquals("", stdout()),
            () -> assertEquals("Prepare" + NL, stderr())
        );
    }

    private static LogRecord record(Level level, String message) {
        LogRecord record = new LogRecord(level, message);
        record.setLoggerName("com.sun.enterprise.admin.cli");
        return record;
    }

    private String stdout() {
        return stdout.toString(UTF_8);
    }

    private String stderr() {
        return stderr.toString(UTF_8);
    }
}
