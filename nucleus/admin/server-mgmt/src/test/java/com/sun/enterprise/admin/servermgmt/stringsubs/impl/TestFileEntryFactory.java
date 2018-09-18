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

import java.io.File;
import java.net.URL;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sun.enterprise.admin.servermgmt.stringsubs.Substitutable;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.FileEntry;

/**
 * Unit test for {@link FileEntryFactory} functionality.
 */
public class TestFileEntryFactory {

    private static final String _qualifiedClassName = TestFileEntryFactory.class.getName().replace('.', '/') + ".class";
    private FileEntryFactory _factory;
    private File _classFile;

    @BeforeClass
    public void init() {
        URL url = TestFileEntryFactory.class.getClassLoader().getResource(_qualifiedClassName);
        _factory = new FileEntryFactory();
        _classFile = new File(url.getPath());
    }

    /**
     * Test get file by mentioning the path of an directory.
     */
    @Test
    public void testGetFileFromDir() {
        FileEntry fileEntry = new FileEntry();
        fileEntry.setName(_classFile.getParentFile().getAbsolutePath());
        List<Substitutable> substitutables = _factory.getFileElements(fileEntry);
        Assert.assertTrue(!substitutables.isEmpty());
        boolean fileFound = false;
        for (Substitutable substitutable : substitutables) {
            if (substitutable.getName().endsWith(_classFile.getAbsolutePath())) {
                fileFound = true;
                break;
            }
        }
        Assert.assertTrue(fileFound);
    }

    /**
     * Test get file by mentioning the absolute path of an file.
     */
    @Test
    public void testGetFile() {
        FileEntry fileEntry = new FileEntry();
        fileEntry.setName(_classFile.getAbsolutePath());
        List<Substitutable> substitutables = _factory.getFileElements(fileEntry);
        Assert.assertTrue(!substitutables.isEmpty());
        Assert.assertTrue(substitutables.size() == 1);
        Assert.assertTrue(substitutables.get(0).getName().equals(_classFile.getAbsolutePath()));
    }

    /**
     * Test get file by using wild card.
     */
    @Test
    public void testGetFilesUsingWildCard() {
        FileEntry fileEntry = new FileEntry();
        fileEntry.setName(_classFile.getParentFile().getAbsolutePath() + File.separator + "Test*");
        List<Substitutable> substitutables = _factory.getFileElements(fileEntry);
        Assert.assertTrue(!substitutables.isEmpty());
        boolean validResult = true;
        for (Substitutable substitutable : substitutables) {
            if (!(new File(substitutable.getName())).getName().startsWith("Test")) {
                validResult = false;
                break;
            }
        }
        Assert.assertTrue(validResult);
    }

    /**
     * Test get file by using wild card in between file path.
     */
    @Test
    public void testGetFilesUsingWildCardBetweenPath() {
        FileEntry fileEntry = new FileEntry();
        File parentFile = _classFile.getParentFile();
        File grandParentFile = parentFile.getParentFile();
        if (grandParentFile == null || !grandParentFile.exists()) {
            return;
        }
        String className = this.getClass().getSimpleName() + ".class";
        fileEntry.setName(grandParentFile.getAbsolutePath() + File.separator + "*" + File.separator + className);
        List<Substitutable> substitutables = _factory.getFileElements(fileEntry);
        Assert.assertTrue(!substitutables.isEmpty());
        Assert.assertTrue(substitutables.size() == 1);
        Assert.assertTrue((new File(substitutables.get(0).getName())).getName().equals(className));
    }

    /**
     * Test get file by using regex pattern.
     */
    @Test
    public void testGetFilesUsingRegex() {
        FileEntry fileEntry = new FileEntry();
        if (!_classFile.exists()) {
            Assert.fail("Not able to locate Test class :" + TestFileEntryFactory.class.getSimpleName());
        }
        fileEntry.setName(_classFile.getParentFile().getAbsolutePath() + File.separator + "(.*+)");
        fileEntry.setRegex("yes");
        List<Substitutable> substitutables = _factory.getFileElements(fileEntry);
        boolean fileFound = false;
        for (Substitutable substitutable : substitutables) {
            if (substitutable.getName().endsWith(_classFile.getAbsolutePath())) {
                fileFound = true;
                break;
            }
        }
        Assert.assertTrue(fileFound);
    }

    /**
     * Test get files for invalid file name.
     */
    @Test
    public void testGetFileInvalidInput() {
        FileEntry fileEntry = new FileEntry();
        fileEntry.setName(_classFile.getAbsolutePath() + File.separator + "zzzzzzzzz.class");
        List<Substitutable> substitutables = _factory.getFileElements(fileEntry);
        Assert.assertTrue(substitutables.isEmpty());
    }
}
