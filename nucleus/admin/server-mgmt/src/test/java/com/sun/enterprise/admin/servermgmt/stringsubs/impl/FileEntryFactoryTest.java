/*
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

import com.sun.enterprise.admin.servermgmt.stringsubs.Substitutable;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.FileEntry;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

import static com.sun.enterprise.admin.servermgmt.test.ServerMgmgtTestFiles.getClassFile;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Unit test for {@link FileEntryFactory} functionality.
 */
public class FileEntryFactoryTest {

    private static final FileEntryFactory FACTORY = new FileEntryFactory();
    private static final File CLASS_FILE = getClassFile(FileEntryFactoryTest.class);


    /**
     * Test get file by mentioning the path of an directory.
     */
    @Test
    public void testGetFileFromDir() {
        FileEntry fileEntry = new FileEntry();
        fileEntry.setName(CLASS_FILE.getParentFile().getAbsolutePath());
        List<Substitutable> substitutables = FACTORY.getFileElements(fileEntry);
        assertFalse(substitutables.isEmpty());
        for (Substitutable substitutable : substitutables) {
            if (substitutable.getName().endsWith(CLASS_FILE.getAbsolutePath())) {
                return;
            }
        }
        fail("File was not found:" + CLASS_FILE);
    }

    /**
     * Test get file by mentioning the absolute path of an file.
     */
    @Test
    public void testGetFile() {
        FileEntry fileEntry = new FileEntry();
        fileEntry.setName(CLASS_FILE.getAbsolutePath());
        List<Substitutable> substitutables = FACTORY.getFileElements(fileEntry);
        assertEquals(1, substitutables.size(), "substitutables.size");
        assertEquals(CLASS_FILE.getAbsolutePath(), substitutables.get(0).getName());
    }

    /**
     * Test get file by using wild card.
     */
    @Test
    public void testGetFilesUsingWildCard() {
        FileEntry fileEntry = new FileEntry();
        fileEntry.setName(CLASS_FILE.getParentFile().getAbsolutePath() + File.separator + "*Test.class");
        List<Substitutable> substitutables = FACTORY.getFileElements(fileEntry);
        assertFalse(substitutables.isEmpty());
        for (Substitutable substitutable : substitutables) {
            assertThat(new File(substitutable.getName()).getName(), endsWith("Test.class"));
        }
    }

    /**
     * Test get file by using wild card in between file path.
     */
    @Test
    public void testGetFilesUsingWildCardBetweenPath() {
        FileEntry fileEntry = new FileEntry();
        File parentFile = CLASS_FILE.getParentFile();
        File grandParentFile = parentFile.getParentFile();
        if (grandParentFile == null || !grandParentFile.exists()) {
            throw new IllegalStateException("grandParentFile doesn't exist!");
        }
        String className = this.getClass().getSimpleName() + ".class";
        fileEntry.setName(grandParentFile.getAbsolutePath() + File.separator + "*" + File.separator + className);
        List<Substitutable> substitutables = FACTORY.getFileElements(fileEntry);
        assertEquals(1, substitutables.size(), "substitutables.size");
        assertEquals(className, new File(substitutables.get(0).getName()).getName());
    }

    /**
     * Test get file by using regex pattern.
     */
    @Test
    public void testGetFilesUsingRegex() {
        FileEntry fileEntry = new FileEntry();
        fileEntry.setName(CLASS_FILE.getParentFile().getAbsolutePath() + File.separator + "(.*+)");
        fileEntry.setRegex("yes");
        List<Substitutable> substitutables = FACTORY.getFileElements(fileEntry);
        for (Substitutable substitutable : substitutables) {
            if (substitutable.getName().endsWith(CLASS_FILE.getAbsolutePath())) {
                return;
            }
        }
        fail("File wasn't found using regex.");
    }

    /**
     * Test get files for invalid file name.
     */
    @Test
    public void testGetFileInvalidInput() {
        FileEntry fileEntry = new FileEntry();
        fileEntry.setName(CLASS_FILE.getAbsolutePath() + File.separator + "zzzzzzzzz.class");
        List<Substitutable> substitutables = FACTORY.getFileElements(fileEntry);
        assertTrue(substitutables.isEmpty());
    }
}
