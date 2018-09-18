/*
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

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import com.sun.enterprise.admin.cli.CLIUtil;
import org.glassfish.api.admin.CommandException;


/**
 * junit test to test CLIUtil class
 */
public class CLIUtilTest {
    @Test
    public void getUploadFileTest() {
        BufferedWriter out = null;
        String fileName = null;
        try {
            final File f = File.createTempFile("TestPasswordFile", ".tmp");
            fileName = f.toString();
            f.deleteOnExit();
            out = new BufferedWriter(new FileWriter(f));
            out.write("AS_ADMIN_PASSWORD=adminadmin\n");
            out.write("AS_ADMIN_MASTERPASSWORD=changeit\n");
        }
        catch (IOException ioe) {
        }
        finally {
            try {
                if (out != null)
                    out.close();
            } catch(final Exception ignore){}
        }
        try {
            Map<String, String> po = CLIUtil.readPasswordFileOptions(fileName, false);
            assertEquals("admin password", "adminadmin", po.get("password"));
            assertEquals("master password", "changeit", po.get("masterpassword"));
            assertEquals("null", null, po.get("foobar"));
        }
        catch (CommandException ce) {
            ce.printStackTrace();
        }
    }

    @Before
    public void setup() {
    }
}
