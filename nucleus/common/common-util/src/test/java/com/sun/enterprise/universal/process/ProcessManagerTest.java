/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal.process;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledOnOs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.OS.WINDOWS;


/**
 * @author bnevins
 */
public class ProcessManagerTest {
    private static final List<String> HUGE_INPUT = hugeInput();
    private static File textfile;

    @BeforeAll
    public static void setUpClass() throws Exception {
        textfile = new File(ProcessManagerTest.class.getClassLoader().getResource("process/lots_o_text.txt").toURI());
        assertTrue(textfile != null && textfile.length() > 0);
    }


    /**
     * The cat doesn't expect the STDIN when it received an input file, does the work and
     * terminates while we are still writing to the input which causes IOException
     * "Stream closed" or something like that).
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    public void writeToStdInAfterProcessTerminated() {
        final ProcessManager pm = new ProcessManager("cat", textfile.getAbsolutePath());
        pm.setStdinLines(HUGE_INPUT);
        pm.setEcho(false);
        ProcessManagerException e = assertThrows(ProcessManagerException.class, pm::execute);
        assertInstanceOf(IOException.class, e.getCause());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void parseCommandOutput() {
        ProcessManager pm = new ProcessManager("sh", "-c", "echo hello");
        pm.setEcho(false);
        int exitCode = assertDoesNotThrow(pm::execute);
        assertAll(
                () -> assertEquals(0, exitCode),
                () -> assertEquals("hello\n", pm.getStdout())
        );
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void waitForTextInStdOutAndAbandonBeforeProcessTerminated() {
        ProcessManager pm = new ProcessManager("sh", "-c", "echo \"start\nhello\"; sleep 1; echo \"continue\"; sleep 8");
        pm.setEcho(false);
        pm.setTextToWaitFor("hello");
        int exitCode = assertDoesNotThrow(pm::execute);
        assertAll(
                () -> assertEquals(0, exitCode),
                () -> assertEquals("start\nhello\n", pm.getStdout())
        );
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void waitForTextInStdErrAndAbandonBeforeProcessTerminated() {
        ProcessManager pm = new ProcessManager("sh", "-c", "echo \"start\nhello\ncontinue\" >&2; sleep 10");
        pm.setEcho(false);
        pm.setTextToWaitFor("continue");
        int exitCode = assertDoesNotThrow(pm::execute);
        assertAll(
                () -> assertEquals(0, exitCode),
                () -> assertEquals("start\nhello\ncontinue\n", pm.getStderr())
        );
    }

    @RepeatedTest(1000)
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void waitForTextInStdOutAfterProcessTerminated() throws Exception {
        ProcessManager pm = new ProcessManager("sh", "-c", "echo \"start\nhello\ncontinue\"");
        pm.setEcho(false);
        pm.setTextToWaitFor("hello");
        int exitCode = assertDoesNotThrow(pm::execute);
        assertAll(
                () -> assertEquals(0, exitCode),
                // Can return whole string, depends if reader thread finishes first or the process
                () -> assertThat(pm.getStdout(), startsWith("start\nhello\n"))
        );
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void waitForTextTimeout() {
        ProcessManager pm = new ProcessManager("sh", "-c", "echo \"Iamnothere\"; sleep 10");
        pm.setEcho(false);
        pm.setTextToWaitFor("somethingelse");
        pm.setTimeout(10);
        assertThrows(ProcessManagerTimeoutException.class, pm::execute);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void waitForTextTerminated() {
        ProcessManager pm = new ProcessManager("sh", "-c", "echo \"Iamnothere\"");
        pm.setEcho(false);
        pm.setTextToWaitFor("somethingelse");
        pm.setTimeout(10000);
        ProcessManagerException e = assertThrows(ProcessManagerException.class, pm::execute);
        assertEquals("Process finished with exit code 0, but did not produce expected output: somethingelse",
            e.getMessage());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void setEnvProperty() {
        String value = String.valueOf(System.currentTimeMillis());
        ProcessManager pm = new ProcessManager("sh", "-c", "echo ${PM_TEST_ENV_VAR}; sleep 1");
        pm.setEcho(false);
        pm.setEnvironment("PM_TEST_ENV_VAR", value);
        int exitCode = assertDoesNotThrow(pm::execute);
        assertAll(
                () -> assertEquals(0, exitCode),
                () -> assertEquals(value + "\n", pm.getStdout())
        );
    }

    /**
     * Covers race conditions between STDOUT, STDIN and death of the process.
     * As of 2025 on AMD Ryzen 9 7945HX this test had incidence 9 failures of 2000 runs
     * when I forgot to add the new line in {@link ReaderThread#finish}.
     */
    @RepeatedTest(1000)
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void catAllStdInLines() {
        List<String> inputLines = Arrays.asList("line1", "line2", "line3");
        String expectedOutput = String.join("\n", inputLines) + "\n";
        ProcessManager pm = new ProcessManager("cat");
        pm.setEcho(false);
        pm.setStdinLines(inputLines);
        int exitCode = assertDoesNotThrow(pm::execute);
        assertAll(
                () -> assertEquals(0, exitCode),
                () -> assertEquals(expectedOutput, pm.getStdout())
        );
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void timeoutShort() {
        int sleepTimeSeconds = 2;
        int timeoutMsec = 10; // timeout is shorter than sleep time
        ProcessManager pm = new ProcessManager("sleep", String.valueOf(sleepTimeSeconds));
        pm.setTimeout(timeoutMsec);
        assertThrows(ProcessManagerTimeoutException.class, pm::execute);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void timeoutLong() {
        int sleepTimeSeconds = 1;
        int timeoutMsec = sleepTimeSeconds * 2 * 1000; // timeout is 2 times longer than sleep time
        ProcessManager pm = new ProcessManager("sleep", String.valueOf(sleepTimeSeconds));
        pm.setTimeout(timeoutMsec);
        int exitCode = assertDoesNotThrow(pm::execute);
        assertEquals(0, exitCode);  // Assert that the process completes successfully
    }

    private static List<String> hugeInput() {
        List<String> input = new ArrayList<>();
        for (int i = 0; i < 50000; i++) {
            input.add("line number " + i + " here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        return input;
    }
}
