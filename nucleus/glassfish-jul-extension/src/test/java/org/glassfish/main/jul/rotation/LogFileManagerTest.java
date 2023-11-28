/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.glassfish.main.jul.rotation.LogFileManager.HandlerSetStreamMethod;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
public class LogFileManagerTest {

    private static File dir;
    private OutputStream stream;
    private AtomicBoolean closeCalled;
    private File file;
    private LogFileManager manager;


    @BeforeAll
    public static void initTmpDir() throws Exception {
        dir = Files.createTempDirectory(LogFileManagerTest.class.getSimpleName()).toFile();
        System.out.println("Temp log directory useful when debugging: " + dir);
    }


    @BeforeEach
    public void init() throws Exception {
        HandlerSetStreamMethod streamSetter = s -> stream = s;
        closeCalled = new AtomicBoolean();
        file = new File(dir, "logFileManagerTest.log");
        manager = new LogFileManager(file, 100L, true, 2, streamSetter, () -> closeCalled.set(true));
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
        assertFalse(closeCalled.get());
        assertNull(stream);

        manager.enableOutput();
        assertTrue(manager.isOutputEnabled());
        assertFalse(closeCalled.get());
        assertNotNull(stream);

        manager.enableOutput();
        assertTrue(manager.isOutputEnabled());
        assertFalse(closeCalled.get());
        assertNotNull(stream);

        manager.disableOutput();
        assertFalse(manager.isOutputEnabled());
        // why? StreamHandler doesn't allow to set null as the stream. So it is just closed.
        assertNotNull(stream);
        assertTrue(closeCalled.get());

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
        manager.enableOutput();
        stream.write("Just a few bytes".getBytes(UTF_8));
        stream.flush();
        assertEquals(16, manager.getFileSize());
        assertEquals(file.length(), manager.getFileSize());

        manager.rollIfFileNotEmpty();
        assertEquals(0, manager.getFileSize());
        assertEquals(file.length(), manager.getFileSize());

        stream.write(RandomStringUtils.randomAlphabetic(101).getBytes(UTF_8));
        stream.flush();
        assertEquals(101, manager.getFileSize());
        // disableOutput should not affect the result.
        manager.disableOutput();
        manager.rollIfFileTooBig();
        assertEquals(0, manager.getFileSize());
        assertEquals(file.length(), manager.getFileSize());

        manager.enableOutput();
        assertDoesNotThrow(() -> {
            stream.write(8);
            stream.flush();
        });

        manager.roll();
        Thread.sleep(100L);
        File[] files = dir.listFiles(File::isFile);
        assertThat(toString(files), files, Matchers.arrayWithSize(3));
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
            final LogFileManager mgr = new LogFileManager(logFile, 1000L, false, 2, null, null);
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
