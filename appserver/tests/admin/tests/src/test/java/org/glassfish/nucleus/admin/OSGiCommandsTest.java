/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.nucleus.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.glassfish.nucleus.test.tool.DomainLifecycleExtension;
import org.glassfish.nucleus.test.tool.NucleusTestUtils;
import org.glassfish.nucleus.test.tool.NucleusTestUtils.NadminReturn;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.glassfish.nucleus.test.tool.NucleusTestUtils.nadmin;
import static org.glassfish.nucleus.test.tool.NucleusTestUtils.nadminWithOutput;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author sanjeeb.sahoo@oracle.com
 */
@ExtendWith(DomainLifecycleExtension.class)
public class OSGiCommandsTest {

    @Test
    public void basicOsgiCmd() {
        assertTrue(nadmin("osgi", "lb"));
    }

    private List<String> runCmd(String... cmd) throws Exception {
        NadminReturn value = nadminWithOutput(cmd);
        if (!value.returnValue) {
            throw new Exception("Cmd failed: \n" + value.outAndErr);
        }
        List<String> output = new ArrayList<>();
        for (String line : value.out.split("\\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("nadmin") || line.startsWith("Command")) {
                continue;
            }
            output.add(line);
        }
        return output;
    }

    private String newCmdSession() throws Exception {
        List<String> value = runCmd("osgi", "--session", "new");
        if (value.size() != 1) {
            throw new Exception("Unexpected output: \n " + value);
        }
        return value.get(0);
    }

    private Set<String> listCmdSessions() throws Exception {
        List<String> sessions = runCmd("osgi", "--session", "list");
        return new HashSet<>(sessions);
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
        // the variables
        // are scoped to sessions.
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
        PrintStream ps = new PrintStream(new FileOutputStream(cmdFile));
        try {
            ps.println("help");
            ps.println("lb");
            NucleusTestUtils.NadminReturn value = nadminWithOutput("osgi-shell", "--file", cmdFile.getAbsolutePath());
            assertTrue(value.out.contains("System Bundle"));
        } finally {
            try {
                ps.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
