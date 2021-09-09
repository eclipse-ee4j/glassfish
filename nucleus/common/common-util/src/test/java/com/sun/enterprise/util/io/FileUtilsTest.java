/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.universal.io.SmartFile;

import java.io.File;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author wnevins
 */
public class FileUtilsTest {

    /**
     * Test of mkdirsMaybe method, of class FileUtils.
     */
    @Test
    public void testMkdirsMaybe() {
        assertFalse(FileUtils.mkdirsMaybe(null));
        File f = new File(".").getAbsoluteFile();
        assertFalse(FileUtils.mkdirsMaybe(null));
        File d1 = new File("junk" + System.currentTimeMillis());
        File d2 = new File("gunk" + System.currentTimeMillis());

        assertTrue(d1.mkdirs());
        assertFalse(d1.mkdirs());
        assertTrue(FileUtils.mkdirsMaybe(d1));
        assertTrue(FileUtils.mkdirsMaybe(d1));
        assertTrue(FileUtils.mkdirsMaybe(d2));
        assertTrue(FileUtils.mkdirsMaybe(d2));
        assertFalse(d2.mkdirs());

        if (!d1.delete()) {
            d1.deleteOnExit();
        }

        if (!d2.delete()) {
            d2.deleteOnExit();
        }

    }


    @Test
    public void testParent() {
        File f = null;
        assertNull(FileUtils.getParentFile(f));
        f = new File("/foo/././././.");
        File wrongGrandParent = f.getParentFile().getParentFile();
        File correctParent = FileUtils.getParentFile(f);
        File sanitizedChild = SmartFile.sanitize(f);
        File sanitizedWrongGrandParent = SmartFile.sanitize(wrongGrandParent);
        File shouldBeSameAsChild = new File(correctParent, "foo");

        // check this out -- surprise!!!!
        assertEquals(sanitizedWrongGrandParent, sanitizedChild);
        assertEquals(shouldBeSameAsChild, sanitizedChild);
    }


    @Test
    public void testResourceToString() {
        String resname = "simplestring.txt";
        String contents = "Simple String Here!";
        String fetched = FileUtils.resourceToString(resname);
        assertEquals(contents, fetched);
    }


    @Test
    public void testEmptyButExistingResourceToString() {
        String resname = "empty.txt";
        String fetched = FileUtils.resourceToString(resname);
        assertNotNull(fetched);
        assertTrue(fetched.length() == 0);
    }


    @Test
    public void testNonExistingResourceToString() {
        String resname = "doesnotexist.txt";
        String fetched = FileUtils.resourceToString(resname);
        assertNull(fetched);
    }


    @Test
    public void testNonExistingResourceToBytes() {
        String resname = "doesnotexist.txt";
        byte[] fetched = FileUtils.resourceToBytes(resname);
        // null -- not an empty array!
        assertNull(fetched);
    }


    @Test
    public void testResourceToBytes() {
        String resname = "verysimplestring.txt";
        byte[] fetched = FileUtils.resourceToBytes(resname);

        assertEquals(fetched[0], 65);
        assertEquals(fetched[1], 66);
        assertEquals(fetched[2], 67);
        assertEquals(fetched.length, 3);
    }
}
