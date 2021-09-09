/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli;

import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.api.admin.ParameterMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Tests for commands with the new syntax.
 * <p/>
 * <code>
 * [asadmin-program-options] command-name [command-options-and-operands]
 * </code>
 * <p/>
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @author Bill Shannon
 */
public class NewSyntaxTest {

    @Test
    public void testGetProgramOptionsForDefaults() throws CommandValidationException, CommandException {
        // this is the command line with *just* the command name on it.
        // Everything should be defaulted in that case.
        String[] argv = new String[] {"foo"};
        ProgramOptions po = parseCommand(argv);

        assertEquals(CLIConstants.DEFAULT_HOSTNAME, po.getHost());
        assertEquals(CLIConstants.DEFAULT_ADMIN_PORT, po.getPort());
        assertNull(po.getUser());
        assertNull(po.getPassword());
        assertNull(po.getPasswordFile());
        assertFalse(po.isEcho());
        assertFalse(po.isTerse());
    }


    @Test
    public void hostB4Cmd() throws CommandValidationException, CommandException {
        String cmd = "new-command";
        String opt1Name = "--opt1";
        String opt1Value = "operand1";
        String host = "foo";
        int port = 4544;
        String[] cmdline = new String[] {"--host", host, "--port", "" + port, "--secure", cmd, opt1Name, opt1Value};
        ProgramOptions po = parseCommand(cmdline);

        // now test program options
        assertEquals(host, po.getHost());
        assertEquals(port, po.getPort());
        assertTrue(po.isSecure());
    }


    private ProgramOptions parseCommand(String[] argv) throws CommandValidationException, CommandException {
        Parser rcp = new Parser(argv, 0, ProgramOptions.getValidOptions(), false);
        ParameterMap params = rcp.getOptions();
        Environment env = new Environment(true);
        //operands = rcp.getOperands();
        return new ProgramOptions(params, env);
    }
}
