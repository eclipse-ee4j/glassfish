/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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

import java.io.File;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
