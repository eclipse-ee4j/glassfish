/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal.io;


import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author bnevins
 */
public class SmartFileTest {

    /**
     * Test of sanitizePaths method, of class SmartFile.
     */
    @Test
    public void sanitizePaths() {
        String sep = File.pathSeparator;

        // where are we now?
        String here = SmartFile.sanitize(".");

        String cp1before = "/a/b/c" + sep + "qqq" + sep + "qqq" + sep + "qqq" + sep + "qqq" + sep + "qqq" + sep + "./././qqq/./." + sep + "z/e";
        String cp1expected = "/a/b/c" + sep + here + "/qqq" + sep + here + "/z/e";

        if (sep.equals(";")) {
            // Windows -- drive letter is needed...
            String drive = here.substring(0, 2);
            cp1expected = drive + "/a/b/c;" + here + "/qqq;" + here + "/z/e";
        }
        assertEquals(cp1expected, SmartFile.sanitizePaths(cp1before));
    }

    /**
     * Test of sanitizePaths method, of class SmartFile.
     */
    @Test
    public void sanitizePaths2() {
        if (com.sun.enterprise.util.OS.isWindows()) {
            String badPaths = "c:/xyz;\"c:\\a b\";c:\\foo";
            String convert = SmartFile.sanitizePaths(badPaths);
            String expect = "C:/xyz;C:/a b;C:/foo";
            assertEquals(convert, expect);
        } else {
            String badPaths = "/xyz:\"/a b\":/foo";
            String convert = SmartFile.sanitizePaths(badPaths);
            String expect = "/xyz:/a b:/foo";
            assertEquals(convert, expect);
        }
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void edgeCase() {

        String fn = "/../../../../../../../../foo";
        assertEquals(SmartFile.sanitize(fn), "/foo");
        fn = "/../foo";
        assertEquals(SmartFile.sanitize(fn), "/foo");

        fn = "/foo/../foo";
        assertEquals(SmartFile.sanitize(fn), "/foo");

        fn = "/foo/../../foo";
        assertEquals(SmartFile.sanitize(fn), "/foo");
    }
}
