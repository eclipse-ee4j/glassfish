/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author wnevins
 * @author David Matejcek
 */
public class FileUtilsTest {

    @TempDir
    private static File tempDir;


    @Test
    public void testMkdirsMaybe() {
        assertFalse(FileUtils.mkdirsMaybe(null));
        File f = new File(".").getAbsoluteFile();
        assertFalse(FileUtils.mkdirsMaybe(null));
        File d1 = new File("junk" + System.currentTimeMillis());
        d1.deleteOnExit();
        File d2 = new File("gunk" + System.currentTimeMillis());
        d2.deleteOnExit();

        assertTrue(d1.mkdirs());
        assertFalse(d1.mkdirs());
        assertTrue(FileUtils.mkdirsMaybe(d1));
        assertTrue(FileUtils.mkdirsMaybe(d1));
        assertTrue(FileUtils.mkdirsMaybe(d2));
        assertTrue(FileUtils.mkdirsMaybe(d2));
        assertFalse(d2.mkdirs());
    }


    @Test
    public void testCopyFileToStream() throws Exception {
        File outputFile = new File(tempDir, "outputFile");
        File testFile = new File(FileUtilsTest.class.getResource("/adminport.xml").toURI());
        try (FileOutputStream os = new FileOutputStream(outputFile)) {
            FileUtils.copy(testFile, os);
        }
        assertEquals(testFile.length(), outputFile.length());
    }


    @Test
    public void testCopyFiles() throws Exception {
        File outputFile = new File(tempDir, "outputFile");
        File testFile = new File(FileUtilsTest.class.getResource("/adminport.xml").toURI());
        FileUtils.copy(testFile, outputFile);
        assertEquals(testFile.length(), outputFile.length());
    }


    @Test
    public void testCopyDirectoriesFiles() throws Exception {
        File outputDir = new File(tempDir, "outputDir");
        File testDir = new File(FileUtilsTest.class.getResource("/process").toURI());
        FileUtils.copy(testDir, outputDir);
        assertEquals(testDir.length(), outputDir.length());
    }


    @Test
    public void testCopyStreamWithKnownSizeToFile() throws Exception {
        File outputFile = new File(tempDir, "outputFile");
        File testFile = new File(FileUtilsTest.class.getResource("/adminport.xml").toURI());
        long length = testFile.length();
        try (FileInputStream stream = new FileInputStream(testFile)) {
            FileUtils.copy(stream, outputFile, length);
            assertEquals(testFile.length(), outputFile.length());
            assertThrows(IOException.class, () -> stream.available());
        }
        // do that once again to verify that the file was not appended or the operation blocked.
        try (FileInputStream stream = new FileInputStream(testFile)) {
            FileUtils.copy(stream, outputFile, length);
            assertEquals(testFile.length(), outputFile.length());
            assertThrows(IOException.class, () -> stream.available());
        }
    }


    @Test
    public void testCopyStreamToFile() throws Exception {
        File outputFile = new File(tempDir, "outputFile");
        File testFile = new File(FileUtilsTest.class.getResource("/adminport.xml").toURI());
        try (FileInputStream stream = new FileInputStream(testFile)) {
            FileUtils.copy(stream, outputFile);
            assertEquals(testFile.length(), outputFile.length());
            assertEquals(0, stream.available(), "available bytes");
        }
    }


    @Test
    public void testCopyFileStreamToFileStream() throws Exception {
        File outputFile = new File(tempDir, "outputFile");
        File testFile = new File(FileUtilsTest.class.getResource("/adminport.xml").toURI());
        try (FileOutputStream output = new FileOutputStream(outputFile);
            FileInputStream inputStream = new FileInputStream(testFile)) {
            FileUtils.copy(inputStream, output);
            assertEquals(testFile.length(), outputFile.length());
            assertEquals(0, inputStream.available(), "available bytes");
        }
    }


    @Test
    public void testCopyCLStreamToStream() throws Exception {
        File outputFile = new File(tempDir, "outputFile");
        File testFile = new File(FileUtilsTest.class.getResource("/adminport.xml").toURI());
        try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(outputFile));
        InputStream inputStream = new BufferedInputStream(FileUtilsTest.class.getResourceAsStream("/adminport.xml"))) {
            FileUtils.copy(inputStream, output);
            assertEquals(testFile.length(), outputFile.length());
            assertEquals(0, inputStream.available(), "available bytes");
        }
    }
}
