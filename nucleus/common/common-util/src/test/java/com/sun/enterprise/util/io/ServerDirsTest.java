/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author bnevins
 */
public class ServerDirsTest {
    private static File childFile;
    private static File parentFile;
    private static File grandParentFile;
    private static File userTopLevelFile;
    private static File userNextToTopLevelFile;

    @BeforeAll
    public static void setUpClass() throws Exception {
        final ClassLoader cl = ServerDirsTest.class.getClassLoader();
        childFile = new File(cl.getResource("grandparent/parent/child").toURI());
        parentFile = new File(cl.getResource("grandparent/parent").toURI());
        grandParentFile = new File(cl.getResource("grandparent").toURI());
        initUserDirs();
        assertTrue(new File(childFile, "readme.txt").isFile());
        assertTrue(childFile.isDirectory());
        assertTrue(parentFile.isDirectory());
        assertTrue(grandParentFile.isDirectory());
        assertTrue(userNextToTopLevelFile.isDirectory());
        assertTrue(userTopLevelFile.isDirectory());
    }

    /**
     * It is not allowed to use a dir that has no parent...
     * @throws Exception
     */
    @Test
    public void testNoParent() throws Exception {
        assertNotNull(userTopLevelFile);
        assertTrue(userTopLevelFile.isDirectory());
        assertNull(userTopLevelFile.getParentFile());
        assertThrows(IOException.class, () -> new ServerDirs(userTopLevelFile));
    }


    @Test
    public void testSpecialFiles() throws IOException {
        ServerDirs sd = new ServerDirs(childFile);
        assertTrue(sd.getConfigDir() != null);
        assertTrue(sd.getDomainXml() != null);
    }

    @Test
    public void testNoArgConstructor() {
        ServerDirs sd = new ServerDirs();
        // check 3 volunteers for nullness...
        assertNull(sd.getPidFile());
        assertNull(sd.getServerGrandParentDir());
        assertFalse(sd.isValid());
    }

    private static void initUserDirs() {
        // this is totally developer-environment dependent!
        // very inefficient but who cares -- this is a unit test.
        // we need this info to simulate an illegal condition like
        // specifying a directory that has no parent and/or grandparent

        Stack<File> stack = new Stack<>();
        File f = childFile;  // guaranteed to have a valid parent and grandparent

        do {
            stack.push(f);
            f = f.getParentFile();
        } while (f != null);

        // the first pop has the top-level
        // the next pop has the next-to-top-level
        userTopLevelFile = stack.pop();
        userNextToTopLevelFile = stack.pop();
    }
}
