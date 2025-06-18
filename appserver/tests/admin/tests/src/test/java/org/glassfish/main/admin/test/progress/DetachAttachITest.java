/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.admin.test.progress;

import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.util.OS;

import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.DetachedTerseAsadminResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author martinmares
 */
@ExtendWith(JobTestExtension.class)
public class DetachAttachITest {
    private static final Logger LOG = System.getLogger(DetachAttachITest.class.getName());
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin(false);


    @Test
    public void uptimePeriodically() throws Exception {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            LOG.log(DEBUG, "detachAndAttachUptimePeriodically(): round " + i);
            final String id;
            {
                DetachedTerseAsadminResult result = ASADMIN.execDetached("uptime");
                assertThat(result, asadminOK());
                id = result.getJobId();
                assertTrue(ids.add(id));
            }
            Thread.sleep(1000L);
            {
                AsadminResult result = GlassFishTestEnvironment.getAsadmin(true).exec("attach", id);
                assertThat(result, asadminOK());
                assertTrue(result.getStdOut().contains("uptime"));
            }
        }
    }


    @Test
    public void commandWithProgressStatus() throws Exception {
        final DetachedTerseAsadminResult detached = ASADMIN.execDetached("progress-custom", "4x1");
        assertThat(detached, asadminOK());
        final AsadminResult attachResult = ASADMIN.exec("attach", detached.getJobId());
        assertThat(attachResult, asadminOK());
        assertThat(attachResult.getStdOut(), stringContainsInOrder("progress-custom"));
        List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(attachResult.getStdOut());
        assertFalse(prgs.isEmpty());
        assertThat(prgs.get(0).getValue(), greaterThanOrEqualTo(0));
        assertEquals(100, prgs.get(prgs.size() - 1).getValue());
        // Now attach finished - must NOT exist - seen progress job is removed
        assertThat(ASADMIN.exec("attach", detached.getJobId()), not(asadminOK()));
    }


    @Test
    public void detachOnesAttachMulti() throws Exception {
        final int attachCount = 20;
        final DetachedTerseAsadminResult jobIdResult = ASADMIN.execDetached("progress-custom", "6x1");
        assertThat(jobIdResult, asadminOK());
        assertNotNull(jobIdResult.getJobId(), "id");
        final List<CompletableFuture<AsadminResult>> futureResults = new ArrayList<>(attachCount);
        for (int i = 0; i < attachCount; i++) {
            futureResults.add(CompletableFuture.supplyAsync(() -> ASADMIN.exec("attach", jobIdResult.getJobId())));
        }
        LOG.log(INFO, () -> "Started " + attachCount + " attaches to job id " + jobIdResult.getJobId());
        Thread.sleep(1500L);
        if (LOG.isLoggable(INFO)) {
            try {
            ProcessHandle.allProcesses()//.filter(ph -> ph.info().commandLine().orElse("").contains("java"))
                .forEach(ph -> {
                    try {
                        LOG.log(INFO, () -> "Process " + ph.pid() + " command line and stacktrace: "
                            + ph.info().commandLine().orElse(""));
                        ProcessManager stack = new ProcessManager(
                            Path.of(System.getProperty("java.home"))
                                .resolve(Path.of("bin", "jcmd" + (OS.isWindows() ? ".exe" : ""))).toString(),
                            String.valueOf(ph.pid()), "Thread.print");
                        stack.execute();
                        LOG.log(INFO, () -> "Stack stdout: \n" + stack.getStdout());
                        LOG.log(INFO, () -> "Stack stderr: \n" + stack.getStderr());
                        ProcessManager mem = new ProcessManager(
                            Path.of(System.getProperty("java.home"))
                                .resolve(Path.of("bin", "jcmd" + (OS.isWindows() ? ".exe" : ""))).toString(),
                            String.valueOf(ph.pid()), "VM.info");
                        mem.execute();
                        LOG.log(INFO, () -> "VM.info stdout: \n" + mem.getStdout());
                        LOG.log(INFO, () -> "VM.info stderr: \n" + mem.getStderr());
                    } catch (Exception e) {
                        LOG.log(ERROR,  () -> "Error while processing process " + ph.pid(), e);
                    }
                });
        } catch (Exception e) {
            LOG.log(ERROR, "Error while processing all processes", e);
        }
        }
        for (Future<AsadminResult> futureResult : futureResults) {
            final AsadminResult result = futureResult.get();
            final List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(result.getStdOut());
            assertAll(
                () -> assertThat(result, asadminOK()),
                () -> assertTrue(result.getStdOut().contains("progress-custom")),
                () -> assertFalse(prgs.isEmpty(), "progress messages empty")
            );
            assertAll(
                () -> assertThat(prgs.get(0).getValue(), greaterThanOrEqualTo(0)),
                () -> assertEquals(100, prgs.get(prgs.size() - 1).getValue())
            );
        }
    }
}
