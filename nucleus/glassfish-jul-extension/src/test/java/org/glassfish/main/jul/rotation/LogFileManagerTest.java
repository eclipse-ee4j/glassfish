/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul.rotation;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author David Matejcek
 */
public class LogFileManagerTest {

    private static File dir;
    private File file;
    private LogFileManager manager;


    @BeforeAll
    public static void initTmpDir() throws Exception {
        dir = Files.createTempDirectory(LogFileManagerTest.class.getSimpleName()).toFile();
        System.out.println("Temp log directory useful when debugging: " + dir);
    }


    @BeforeEach
    public void init() throws Exception {
        file = new File(dir, "logFileManagerTest.log");
        manager = new LogFileManager(file, UTF_8, 100L, true, 10);
    }


    @AfterEach
    public void removeFile() throws Exception {
        System.out.println(toString(dir.listFiles(File::isFile)));
        if (file == null) {
            return;
        }
        Stream.of(dir.listFiles()).forEach(File::delete);
    }


    @Test
    public void enableAndDisable() throws Exception {
        assertFalse(manager.isOutputEnabled());
        manager.disableOutput();
        assertFalse(manager.isOutputEnabled());

        manager.enableOutput();
        assertTrue(manager.isOutputEnabled());

        assertThrows(IllegalStateException.class, manager::enableOutput);
        assertTrue(manager.isOutputEnabled());

        manager.disableOutput();
        assertFalse(manager.isOutputEnabled());

        manager.disableOutput();
        assertFalse(manager.isOutputEnabled());

        manager.enableOutput();
        assertTrue(manager.isOutputEnabled());

        manager.disableOutput();
        assertFalse(manager.isOutputEnabled());
        assertEquals(0, file.length());
        assertEquals(file.length(), manager.getFileSize());
    }


    @Test
    public void rolling() throws Exception {
        assertDoesNotThrow(manager::rollIfFileNotEmpty);
        assertDoesNotThrow(manager::rollIfFileTooBig);

        // simplest test
        manager.enableOutput();
        manager.write("Just a few bytes");
        // this will flush output, but doesn't roll
        manager.flush();
        assertEquals(16, manager.getFileSize());
        assertEquals(file.length(), manager.getFileSize());
        manager.rollIfFileNotEmpty();
        assertEquals(0, manager.getFileSize());
        assertEquals(file.length(), manager.getFileSize());

        // limit is 100, so flush will roll.
        manager.write(RandomStringUtils.randomAlphabetic(101));
        manager.flush();
        assertEquals(0, manager.getFileSize());

        // disableOutput does not affect roll.
        manager.write(RandomStringUtils.randomAlphabetic(101));
        manager.disableOutput();
        manager.flush();
        assertEquals(0, manager.getFileSize());
        assertEquals(file.length(), manager.getFileSize());

        // enabling output should make writes possible again.
        manager.enableOutput();
        assertDoesNotThrow(() -> {
            manager.write("X");
            manager.flush();
        });
        assertEquals(1, manager.getFileSize());
        manager.flush();
        assertEquals(1, manager.getFileSize());

        // forced roll
        manager.roll();

        long start = System.currentTimeMillis();
        while (true) {
            File[] files = dir.listFiles(File::isFile);
            if (files.length == 5) {
                break;
            }
            if (System.currentTimeMillis() > start + 5000) {
                fail("Incorrect numbe of files, expected 5, but found these:\n" + toString(files));
            }
            Thread.sleep(10);
        }
    }


    /**
     * This test verifies that it is safe
     * <ul>
     * <li>to use null as the streamSetter parameter
     * <li>to use null as the streamCloser parameter
     * <li>to use an output file without parent (just a file name)
     * </ul>
     */
    @Test
    public void nullChecks() throws Exception {
        final String logFilename = "___logfile_garbage.log";
        try {
            final File logFile = new File(logFilename);
            final LogFileManager mgr = new LogFileManager(logFile, UTF_8, 1000L, false, 2);
            mgr.enableOutput();
            assertTrue(mgr.isOutputEnabled());
            mgr.disableOutput();
            assertFalse(mgr.isOutputEnabled());
            assertEquals(0, mgr.getFileSize());
            mgr.roll();
        } finally {
            final File[] files = new File(".").listFiles(f -> f.getName().startsWith(logFilename));
            for (final File f : files) {
                f.delete();
            }
            // First delete those files wherever was the current directory.
            assertThat(toString(files), files, Matchers.arrayWithSize(2));
        }
    }


    private String toString(File[] files) {
        return "Created files: \n" + Stream.of(files).map(f -> f.getAbsolutePath() + ": " + f.length() + " B")
            .collect(Collectors.joining("\n"));
    }

}
