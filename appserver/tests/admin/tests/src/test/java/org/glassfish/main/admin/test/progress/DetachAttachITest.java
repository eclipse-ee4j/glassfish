/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

import java.lang.System.Logger;
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

import static com.sun.enterprise.tests.progress.ProgressCustomCommand.generateIntervals;
import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.containsString;
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
        final Set<String> ids = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            LOG.log(INFO, "detachAndAttachUptimePeriodically: round {0}", i);
            final String id;
            {
                DetachedTerseAsadminResult result = ASADMIN.execDetached("uptime");
                assertThat(result, asadminOK());
                id = result.getJobId();
                assertTrue(ids.add(id), () -> "Job id unique: " + id);
            }
            {
                AsadminResult result = GlassFishTestEnvironment.getAsadmin(true).exec("attach", id);
                assertThat(result, asadminOK());
                assertThat(result.getStdOut(), containsString("uptime"));
            }
        }
    }


    @Test
    public void commandWithProgressStatus() throws Exception {
        // We have to be faster than the job finishes.
        final String intervals = generateIntervals(1000, 10, 20, 30);
        final DetachedTerseAsadminResult detached = ASADMIN.execDetached("progress-custom", intervals);
        assertThat(detached, asadminOK());
        final AsadminResult attachResult = ASADMIN.exec("attach", detached.getJobId());
        assertThat(attachResult, asadminOK());
        assertThat(attachResult.getStdOut(), stringContainsInOrder("progress-custom"));
        List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(attachResult.getStdOut());
        assertFalse(prgs.isEmpty(), "ProgressMessages.isEmpty()");
        assertThat(prgs.get(0).getValue(), greaterThanOrEqualTo(0));
        assertEquals(100, prgs.get(prgs.size() - 1).getValue());

        JobTestExtension.doAndDisableJobCleanup();
        assertThat("Attach again", ASADMIN.exec("attach", detached.getJobId()), not(asadminOK()));
    }


    @Test
    public void detachOnesAttachMulti() throws Exception {
        // This affects scheduling of threads and makes the test repeatable
        final int attachCount = Runtime.getRuntime().availableProcessors() + 2;
        final String intervals = generateIntervals(1000);
        final DetachedTerseAsadminResult jobIdResult = ASADMIN.execDetached("progress-custom", intervals);
        assertThat(jobIdResult, asadminOK());
        assertNotNull(jobIdResult.getJobId(), "id");
        final List<CompletableFuture<AsadminResult>> futureResults = new ArrayList<>(attachCount);
        for (int i = 0; i < attachCount; i++) {
            futureResults.add(CompletableFuture.supplyAsync(() -> ASADMIN.exec("attach", jobIdResult.getJobId())));
        }
        LOG.log(INFO, () -> "Started " + attachCount + " attaches to job id " + jobIdResult.getJobId());
        // Let them all start
        // TODO: On Java21 we can have more control using Executors.newThreadPerTaskExecutor
        Thread.sleep(1500L);
        for (Future<AsadminResult> futureResult : futureResults) {
            final AsadminResult result = futureResult.get();
            final List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(result.getStdOut());
            assertAll(
                () -> assertThat(result, asadminOK()),
                () -> assertThat(result.getStdOut(), containsString("progress-custom")),
                () -> assertThat(result.getStdOut(), not(containsString("FAILURE")))
            );
            if (prgs.isEmpty()) {
                // We were late to watch the progress, however soon enough to get the result.
                continue;
            }
            assertAll(
                () -> assertThat(prgs.get(0).getValue(), greaterThanOrEqualTo(0)),
                () -> assertEquals(100, prgs.get(prgs.size() - 1).getValue())
            );
        }
    }
}
