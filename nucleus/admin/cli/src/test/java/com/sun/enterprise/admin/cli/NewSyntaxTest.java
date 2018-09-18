/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.*;

import org.glassfish.api.admin.*;

import static org.junit.Assert.*;
import org.junit.Test;


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
    public void testGetProgramOptionsForDefaults()
                        throws CommandValidationException, CommandException {
        String cmd = "foo";

        // this is the command line with *just* the command name on it.
        // Everything should be defaulted in that case.
        String[] argv = new String[] { cmd };
        ProgramOptions po = parseCommand(argv);

        assertEquals(CLIConstants.DEFAULT_HOSTNAME, po.getHost());
        assertEquals(CLIConstants.DEFAULT_ADMIN_PORT, po.getPort());
        assertNull(po.getUser());
        assertNull(po.getPassword());
        assertNull(po.getPasswordFile());
        assertFalse(po.isEcho());
        assertFalse(po.isTerse());
        // XXX - can't test, depends on how run
        //assertFalse(po.isInteractive());
    }

    @Test
    public void hostB4Cmd()
                        throws CommandValidationException, CommandException {
        String cmd = "new-command";
        String cmdArg1 = "--opt1";
        String cmdArg2 = "operand1";
        String GIVEN_HOST = "foo";
        int GIVEN_PORT = 4544;
        String[] cmdline = new String[] {
            "--host", GIVEN_HOST, "--port", "" + GIVEN_PORT,
            "--secure", cmd, cmdArg1, cmdArg2
        };
        ProgramOptions po = parseCommand(cmdline);

        /* XXX
        assertEquals(cmd, r.getCommandName());
        assertArrayEquals(new String[]{cmdArg1, cmdArg2}, r.getCommandArguments());
        */
        // now test program options
        assertEquals(GIVEN_HOST, po.getHost());
        assertEquals(GIVEN_PORT, po.getPort());
        assertTrue(po.isSecure());
    }

    /*
     * Commented out until I get a chance to convert this.
    @Test
    public void reuseOption() throws ParserException {
        String cmd = "some-cmd";
        String arg1 = "--host";
        String arg2 = "cmdhost";
        String arg3 = "operand1";
        String[] cmdArgs = new String[]{arg1, arg2, arg3};
        String pHost = "asadminhost";
        String[] cmdline = new String[]{"--host", pHost, cmd, arg1, arg2, arg3};
        CommandRunner r = new CommandRunner(System.out, System.err);
        r.parseMetaOptions(cmdline);
        assertFalse(r.usesDeprecatedSyntax());
        assertEquals(cmd, r.getCommandName());
        assertArrayEquals(cmdArgs, r.getCommandArguments());

        //now test program options
        Option propt = getOptionNamed(r.getProgramOptions(), PORT);
        assertEquals("" + DEFAULT_PORT, propt.getEffectiveValue());

        propt = getOptionNamed(r.getProgramOptions(), HOST);
        assertEquals(pHost, propt.getEffectiveValue());

        propt = getOptionNamed(r.getProgramOptions(), SECURE);
        assertEquals("false", propt.getEffectiveValue().toLowerCase());
    }

    @Test(expected = ParserException.class)
    public void invalidProgramOption() throws ParserException {
        String[] cmdline = new String[]{"--invalid", "some-command", "--option", "value", "operand"}; //there is no program option named invalid
        CommandRunner r = new CommandRunner(System.out, System.err);
        r.parseMetaOptions(cmdline);
    }

    @Test(expected = ParserException.class)
    public void missingCommand() throws ParserException {
        String[] cmdline = new String[]{"--host", "foo", "--port=1234", "-s", "-eI", "-u", "admin"}; // all valid program options, but no command :-)
        CommandRunner r = new CommandRunner(System.out, System.err);
        r.parseMetaOptions(cmdline);
    }

    @Test
    public void allDefaults() throws ParserException {
        String[] cmdline = new String[]{"command-alone"};
        CommandRunner r = new CommandRunner(System.out, System.err);
        r.parseMetaOptions(cmdline);
        assertFalse(r.usesDeprecatedSyntax());
        Set<Option> propts = r.getProgramOptions();
        for (Option propt : propts) {
            String name = propt.getName();
            String value = propt.getEffectiveValue();
            if (HOST.equals(name))
                assertEquals(DEFAULT_HOST, value);
            else if (PORT.equals(name))
                assertEquals(DEFAULT_PORT + "", value);
            else if (USER.equals(name))
                assertEquals(DEFAULT_USER, value);
            else if (SECURE.equals(name))
                assertEquals(DEFAULT_SECURE.toLowerCase(), value.toLowerCase());
            else if (ECHO.equals(name))
                assertEquals(DEFAULT_ECHO.toLowerCase(), value.toLowerCase());
            else if (TERSE.equals(name))
                assertEquals(DEFAULT_TERSE.toLowerCase(), value.toLowerCase());
            else if (INTERACTIVE.equals(name))
                assertEquals(DEFAULT_INTERACTIVE.toLowerCase(), value.toLowerCase());
            else if (PASSWORD.equals(name))
                assertNull(value);
            else {
                //do nothing, we don't check passwordfile, although we should have defaulted password file!
            }
        }
    }

    @Test
    public void symbol4Host() throws ParserException {
        String host = "myhost";
        String cmd = "cmd";
        String[] cmdline = new String[]{"-H", host, cmd};
        CommandRunner r = new CommandRunner(System.out, System.err);
        r.parseMetaOptions(cmdline);
        assertFalse(r.usesDeprecatedSyntax());
        Option op = getOptionNamed(r.getProgramOptions(), HOST);
        assertNotNull(op);
        assertEquals(op.getEffectiveValue(), host);
    }

    @Test
    public void symbol4Port() throws ParserException {
        String port = "1234";
        String cmd = "cmd";
        String[] cmdline = new String[]{"-p", port, cmd};
        CommandRunner r = new CommandRunner(System.out, System.err);
        r.parseMetaOptions(cmdline);
        assertFalse(r.usesDeprecatedSyntax());
        Option op = getOptionNamed(r.getProgramOptions(), PORT);
        assertNotNull(op);
        assertEquals(op.getEffectiveValue(), port);
    }

    static Option getOptionNamed(Set<Option> ops, String name) {
        for (Option op : ops)
            if (op.getName().equals(name))
                return op;
        return null;
    }
    */

    private ProgramOptions parseCommand(String[] argv)
                        throws CommandValidationException, CommandException {
        Parser rcp = new Parser(argv, 0,
                        ProgramOptions.getValidOptions(), false);
        ParameterMap params = rcp.getOptions();
        Environment env = new Environment(true);
        //operands = rcp.getOperands();
        return new ProgramOptions(params, env);
    }
}
