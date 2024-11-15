/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.admin.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.lang.Thread.sleep;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author sanjeeb.sahoo@oracle.com
 */
public class OSGiCommandsITest {

    private static final Logger LOGGER = System.getLogger(OSGiCommandsITest.class.getName());
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin(true);

    @BeforeAll
    public static void waitOsgiReady() throws Exception {
        long timeout = System.currentTimeMillis() + 10_000L;
        while (System.currentTimeMillis() < timeout) {
            AsadminResult result = ASADMIN.exec("osgi", "lb");
            if (!result.isError()) {
                return;
            }
            LOGGER.log(Level.INFO, "Waiting for OSGi to be ready...");
            sleep(10L);
        }
    }

    @Test
    public void basicOsgiCmd() {
        assertThat(ASADMIN.exec("osgi", "lb"), asadminOK());
    }


    /**
     * Tests functionality of session handling of osgi command.
     * It creates sessions, lists them, executes commands against each session and finally stops them.
     * @throws Exception
     */
    @Test
    public void osgiCmdSession() throws Exception {
        // Create some sessions
        Set<String> sessions = new HashSet<>();
        for (int i = 0; i < 3; ++i) {
            sessions.add(newCmdSession());
        }

        // Let's list them to make sure list operation works.
        final Set<String> actual = listCmdSessions();
        assertEquals(sessions, actual, "listed sessions do not match with created sessions");

        // Let's set the same variable in each command session with a different value and make sure
        // the variables are scoped to sessions.
        for (String sessionId : sessions) {
            List<String> result = runCmd("osgi", "--session", "execute", "--session-id", sessionId, "var=" + sessionId);
            assertThat(result, IsEmptyCollection.empty());
        }
        for (String sessionId : sessions) {
            List<String> result = runCmd("osgi", "--session", "execute", "--session-id", sessionId, "echo $var");
            assertThat(result, not(IsEmptyCollection.empty()));
            assertEquals(sessionId, result.get(0));
        }

        // Let's stop all sessions.
        for (String sessionId : sessions) {
            List<String> result = runCmd("osgi", "--session", "stop", "--session-id", sessionId);
            assertThat(result, IsEmptyCollection.empty());
        }
        sessions = listCmdSessions();
        assertTrue(sessions.isEmpty(), "Not all sessions closed properly: " + sessions);
    }

    /**
     * Test osgi-shell command which is a local command. It takes a file as input. The file contains
     * a list of shell commands to be executed.
     * @throws IOException
     */
    @Test
    public void osgiShell() throws IOException {
        File cmdFile = File.createTempFile("osgi-commands", ".txt");
        cmdFile.deleteOnExit();
        try (PrintStream ps = new PrintStream(new FileOutputStream(cmdFile))) {
            ps.println("help");
            ps.println("lb");
        }
        AsadminResult result = ASADMIN.exec("osgi-shell", "--file", cmdFile.getAbsolutePath());
        assertThat(result.getStdOut(),
            stringContainsInOrder("cm:createFactoryConfiguration", "System Bundle", "Apache Felix Gogo Runtime"));
        assertThat(result, AsadminResultMatcher.asadminOK());
    }


    private String newCmdSession() throws Exception {
        List<String> value = runCmd("osgi", "--session", "new");
        if (value.size() != 1) {
            throw new AssertionError("Unexpected output: \n " + value);
        }
        return value.get(0);
    }

    private Set<String> listCmdSessions() throws Exception {
        List<String> sessions = runCmd("osgi", "--session", "list");
        return new HashSet<>(sessions);
    }


    private List<String> runCmd(String... cmd) throws Exception {
        AsadminResult value = ASADMIN.exec(cmd);
        assertThat(value, asadminOK());
        List<String> output = new ArrayList<>();
        for (String line : value.getStdOut().split("\\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith(ASADMIN.getCommandName()) || line.startsWith("Command")) {
                continue;
            }
            output.add(line);
        }
        return output;
    }
}
