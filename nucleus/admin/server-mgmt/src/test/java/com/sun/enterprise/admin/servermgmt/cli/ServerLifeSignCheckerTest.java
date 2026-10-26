/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.servermgmt.cli;

import com.sun.enterprise.admin.servermgmt.cli.ServerLifeSignChecker.GlassFishProcess;
import com.sun.enterprise.admin.servermgmt.cli.ServerLifeSignChecker.ServerLifeSigns;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.util.HostAndPort;

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The admin endpoint is already listening, while the pid file is written only when the server is ready.
 * The admin endpoint must not be probed in that window, each probe is a new connection to the server.
 */
public class ServerLifeSignCheckerTest {

    private static final GlassFishProcess PROCESS = GlassFishProcess.of(ProcessHandle.current().pid());

    @TempDir
    private File tempDir;

    private ServerSocket adminSocket;
    private AtomicInteger adminEndpointChecks;
    private Supplier<List<HostAndPort>> adminEndpoints;

    @BeforeEach
    void startAdminListener() throws Exception {
        adminSocket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        final HostAndPort endpoint = new HostAndPort(adminSocket.getInetAddress().getHostAddress(),
            adminSocket.getLocalPort(), false);
        adminEndpointChecks = new AtomicInteger();
        adminEndpoints = () -> {
            adminEndpointChecks.incrementAndGet();
            return List.of(endpoint);
        };
    }

    @AfterEach
    void stopAdminListener() throws Exception {
        adminSocket.close();
    }

    @Test
    void adminEndpointIsNotProbedUntilPidFileExists() throws Exception {
        final File pidFile = new File(tempDir, "pid");
        final ServerLifeSigns signs = createChecker(pidFile).watchStartup(PROCESS, Duration.ofMillis(500L));
        assertTrue(signs.isError(), signs::getSummary);
        assertThat(adminEndpointChecks.get(), equalTo(0));
    }

    @Test
    void adminEndpointIsProbedOnceWhenPidFileExists() throws Exception {
        final File pidFile = new File(tempDir, "pid");
        ProcessUtils.saveCurrentPid(pidFile);
        final ServerLifeSigns signs = createChecker(pidFile).watchStartup(PROCESS, Duration.ofSeconds(10L));
        assertFalse(signs.isError(), signs::getSummary);
        assertThat(adminEndpointChecks.get(), equalTo(1));
    }

    private ServerLifeSignChecker createChecker(File pidFile) {
        final ServerLifeSignCheck checks = new ServerLifeSignCheck("domain test", false, true, true, true, List.of());
        return new ServerLifeSignChecker(checks, pidFile, adminEndpoints, false);
    }
}
