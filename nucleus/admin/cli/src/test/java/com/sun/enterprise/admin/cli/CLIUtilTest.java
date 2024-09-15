/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * junit test to test CLIUtil class
 */
public class CLIUtilTest {

    @Test
    public void getUploadFileTest() throws Exception {
        final File f = File.createTempFile("TestPasswordFile", ".tmp");
        String fileName = f.toString();
        f.deleteOnExit();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(f, UTF_8))) {
            out.write("AS_ADMIN_PASSWORD=adminadmin\n");
            out.write("AS_ADMIN_MASTERPASSWORD=changeit\n");
        }

        Map<String, String> po = CLIUtil.readPasswordFileOptions(fileName, false);
        assertEquals("adminadmin", po.get("password"), "admin password");
        assertEquals("changeit", po.get("masterpassword"), "master password");
        assertNull(po.get("foobar"), "null");
    }
}
