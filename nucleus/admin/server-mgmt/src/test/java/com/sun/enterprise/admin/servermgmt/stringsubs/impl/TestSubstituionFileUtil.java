/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test class for {@link SubstitutionFileUtil}.
 */
public class TestSubstituionFileUtil {

    /**
     * Test the file size for which in-memory substitution can be performed.
     */
    @Test
    public void testInMemorySubstitutionFileSize() {
        int maxSize = SubstitutionFileUtil.getInMemorySubstitutionFileSizeInBytes();
        Assert.assertTrue(maxSize > 0);
        Assert.assertEquals(maxSize, SubstitutionFileUtil.getInMemorySubstitutionFileSizeInBytes());
    }

    /**
     * Test the creation of directory.
     */
    @Test
    public void testDirSetUp() {
        try {
            File dir = SubstitutionFileUtil.setupDir("testing");
            Assert.assertTrue(dir.exists());
            Assert.assertTrue(dir.isDirectory());
            Assert.assertTrue(dir.list().length == 0);

            SubstitutionFileUtil.removeDir(dir);
            Assert.assertFalse(dir.exists());
        } catch (IOException e) {
            Assert.fail("Failed to setUp/remove directory by using subsitution file utility.", e);
        }
    }

    /**
     * Test the removal of null directory.
     */
    @Test
    public void testRemoveNullDir() {
        try {
            SubstitutionFileUtil.removeDir(null);
        } catch (Exception e) {
            Assert.fail("Error occurred in directory deletion.", e);
        }
    }

    /**
     * Test the removal of directory recursively.
     */
    @Test
    public void testDirRemovalRecursively() {
        try {
            File dir = SubstitutionFileUtil.setupDir("testing");
            Assert.assertTrue(dir.exists());
            Assert.assertTrue(dir.isDirectory());
            Assert.assertTrue(dir.list().length == 0);

            BufferedWriter writer = null;
            try {
                File testFile = new File(dir.getAbsolutePath() + File.separator + "testFile.txt");
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testFile)));
                writer.write("Testing : " + SubstitutionFileUtil.class.getSimpleName());
                writer.close();
            } catch (Exception e) {
                Assert.fail("Not able to create test Text file.", e);
            }
            SubstitutionFileUtil.removeDir(dir);
            Assert.assertFalse(dir.exists());
        } catch (IOException e) {
            Assert.fail("Failed to setUp/remove directory by using subsitution file utility.", e);
        }
    }
}
