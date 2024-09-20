/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2018-2021 Oracle and/or its affiliates. All rights reserved.
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
import java.io.OutputStreamWriter;

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test class for {@link SubstitutionFileUtil}.
 */
public class SubstituionFileUtilTest {

    /**
     * Test the file size for which in-memory substitution can be performed.
     */
    @Test
    public void testInMemorySubstitutionFileSize() {
        int maxSize = SubstitutionFileUtil.getInMemorySubstitutionFileSizeInBytes();
        assertThat(maxSize, greaterThan(0));
        assertEquals(maxSize, SubstitutionFileUtil.getInMemorySubstitutionFileSizeInBytes());
    }

    /**
     * Test the creation of directory.
     * @throws Exception
     */
    @Test
    public void testDirSetUp() throws Exception {
        File dir = SubstitutionFileUtil.setupDir("testing");
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
        assertEquals(0, dir.list().length);

        SubstitutionFileUtil.removeDir(dir);
        assertFalse(dir.exists());
    }


    /**
     * Test the removal of null directory.
     */
    @Test
    public void testRemoveNullDir() {
        SubstitutionFileUtil.removeDir(null);
    }


    /**
     * Test the removal of directory recursively.
     * @throws Exception
     */
    @Test
    public void testDirRemovalRecursively() throws Exception {
        File dir = SubstitutionFileUtil.setupDir("testing");
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
        assertEquals(0, dir.list().length);

        File testFile = new File(dir, "testFile.txt");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testFile), UTF_8))) {
            writer.write("Testing: " + SubstitutionFileUtil.class.getSimpleName());
        }
        SubstitutionFileUtil.removeDir(dir);
        assertFalse(dir.exists());
    }
}
