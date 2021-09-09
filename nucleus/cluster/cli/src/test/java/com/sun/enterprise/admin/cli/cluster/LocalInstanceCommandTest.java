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

package com.sun.enterprise.admin.cli.cluster;

import com.sun.enterprise.universal.io.SmartFile;

import java.io.File;

import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 *
 * @author bnevins
 */
public class LocalInstanceCommandTest extends LocalInstanceCommand {

    private static File installDir;
    private static File nodeAgentsDir;

    @BeforeEach
    public void setUp() {
        String installDirPath = LocalInstanceCommandTest.class.getClassLoader().getResource("fake_gf_install_dir").getPath();
        installDir = SmartFile.sanitize(new File(installDirPath));
        System.out.println("install dir: " + installDir);
        nodeAgentsDir = new File(installDir, "nodes");
    }

    /**
     * Test of validate method, of class LocalInstanceCommand.
     */
    @Test
    public void testValidate() throws Exception {
        nodeDir = nodeAgentsDir.getAbsolutePath();
        instanceName = "i1";
        isCreateInstanceFilesystem = true;
        assertDoesNotThrow(() -> validate());
    }

    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {
        System.out.println("Do nothing!");
        return 0;
    }
}
