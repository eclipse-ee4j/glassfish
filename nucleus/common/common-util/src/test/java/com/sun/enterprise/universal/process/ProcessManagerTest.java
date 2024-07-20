/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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


import com.sun.enterprise.util.OS;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledOnOs;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.OS.WINDOWS;


/**
 * @author bnevins
 */
public class ProcessManagerTest {

    private static String textfile;

    @BeforeAll
    public static void setUpClass() throws Exception {
        textfile = new File(ProcessManagerTest.class.getClassLoader().getResource("process/lots_o_text.txt").getPath()).getAbsolutePath();
        assertTrue(textfile != null && textfile.length() > 0);
    }

    /**
     * Temporary Test of ProcessManager
     * This stuff is platform dependent.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void test1() throws ProcessManagerException {
        ProcessManager pm;

        System.out.println("If it is FROZEN RIGHT NOW -- then Houston, we have a problem!");
        System.out.println("ProcessManager must have the write to stdin before the reader threads have started!");

        if (OS.isWindows()) {
            pm = new ProcessManager("cmd", "/c", "type", textfile);
        } else {
            pm = new ProcessManager("cat", textfile);
        }

        pm.setStdinLines(hugeInput());
        pm.setEcho(false);
        pm.execute();
    }

    private List<String> hugeInput() {
        List<String> l = new ArrayList<>();

        for (int i = 0; i < 50000; i++) {
            l.add("line number " + i + "here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }

        return l;
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void testCommandExecution() {
        ProcessManager pm = new ProcessManager("sh", "-c", "echo hello; sleep 1");
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
    void testWaitUntilTextInStdOut() {
        ProcessManager pm = new ProcessManager("sh", "-c", "echo \"start\nhello\ncontinue\"; sleep 10");
        pm.setEcho(false);
        pm.setTextToWaitFor("hello");
        int exitCode = assertDoesNotThrow(pm::execute);
        assertAll(
                () -> assertEquals(0, exitCode),
                () -> assertEquals("start\nhello\ncontinue\n", pm.getStdout())
        );
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void testWaitUntilTextInStdErr() {
        ProcessManager pm = new ProcessManager("sh", "-c", "echo \"start\nhello\ncontinue\" >&2; sleep 10");
        pm.setEcho(false);
        pm.setTextToWaitFor("hello");
        int exitCode = assertDoesNotThrow(pm::execute);
        assertAll(
                () -> assertEquals(0, exitCode),
                () -> assertEquals("start\nhello\ncontinue\n", pm.getStderr())
        );
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void testDetectTextInStdOutIfProcessStops() throws InterruptedException {
        ProcessManager pm = new ProcessManager("sh", "-c", "echo \"start\nhello\ncontinue\"");
        pm.setEcho(false);
        pm.setTextToWaitFor("hello");
        int exitCode = assertDoesNotThrow(pm::execute);
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            // ignore.
        }
        assertAll(
                () -> assertEquals(0, exitCode),
                () -> assertEquals("start\nhello\ncontinue\n", pm.getStdout())
        );
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void testSetEnvironment() {
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

    @RepeatedTest(value = 2000)
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void testSetStdinLines() {
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
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void testSetTimeoutLessThanExecutionTime() {
        int sleepTimeSeconds = 2;
        int timeoutMsec = (sleepTimeSeconds - 1) * 1000; // timeout is shorter than sleep time
        ProcessManager pm = new ProcessManager("sleep", String.valueOf(sleepTimeSeconds));
        pm.setTimeoutMsec(timeoutMsec);
        assertThrows(ProcessManagerTimeoutException.class, pm::execute);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisabledOnOs(WINDOWS)
    void testSetTimeoutGreaterThanExecutionTime() {
        int sleepTimeSeconds = 1;
        int timeoutMsec = sleepTimeSeconds * 2 * 1000; // timeout is 2 times longer than sleep time
        ProcessManager pm = new ProcessManager("sleep", String.valueOf(sleepTimeSeconds));
        pm.setTimeoutMsec(timeoutMsec);
        int exitCode = assertDoesNotThrow(pm::execute);
        assertEquals(0, exitCode);  // Assert that the process completes successfully
    }
}
