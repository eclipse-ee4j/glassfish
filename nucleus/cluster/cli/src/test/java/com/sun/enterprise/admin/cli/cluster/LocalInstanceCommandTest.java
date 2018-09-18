/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli.cluster;

import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.io.*;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import com.sun.enterprise.admin.servermgmt.cli.LocalServerCommand;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */
public class LocalInstanceCommandTest extends LocalInstanceCommand{

    public LocalInstanceCommandTest() {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        me = new LocalInstanceCommandTest();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of validate method, of class LocalInstanceCommand.
     */
    @Test
    public void testValidate() throws Exception {
        System.out.println("test LocalInstanceCommand.validate");
        try {
            nodeDir = nodeAgentsDir.getAbsolutePath();
            instanceName = "i1";
            isCreateInstanceFilesystem = true;
            validate();
        }
        catch(CommandException e) {
            fail("validate failed!!!");
            throw e;
        }
    }

    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {
        System.out.println("Do nothing!");
        return 0;
    }

    private LocalInstanceCommandTest me;
    private static File installDir;
    private static File nodeAgentsDir;

    static {
        String installDirPath = LocalInstanceCommandTest.class.getClassLoader().getResource("fake_gf_install_dir").getPath();
        installDir = SmartFile.sanitize(new File(installDirPath));
        nodeAgentsDir = new File(installDir, "nodes");
    }
}
