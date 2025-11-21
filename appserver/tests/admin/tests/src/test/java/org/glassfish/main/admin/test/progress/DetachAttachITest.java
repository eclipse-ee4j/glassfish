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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.DetachedTerseAsadminResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.sun.enterprise.tests.progress.ProgressCustomCommand.generateRegularIntervals;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.admin.test.progress.UsualLatency.getMeasuredLatency;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminError;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author martinmares
 */
@ExtendWith(JobTestExtension.class)
public class DetachAttachITest {

    private static final Logger LOG = System.getLogger(DetachAttachITest.class.getName());

    private static final long MEASURED_LATENCY = getMeasuredLatency();

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin(false);

    private ExecutorService threadPool;

    @AfterEach
    void poolShutdown() throws Exception {
        if (threadPool != null) {
            threadPool.shutdown();
            assertTrue(threadPool.awaitTermination(1, TimeUnit.MINUTES), "Threads did not finish in one minute.");
        }
    }

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
    public void detachOnesAttachLifecycle() throws Exception {
        {
            final AsadminResult result = ASADMIN.exec((int) MEASURED_LATENCY + 1000, "attach", "99999999");
            assertThat(result, asadminError("Job with id 99999999 does not exist"));
        }

        // We have to be faster than the job finishes.
        final String intervals = generateRegularIntervals(5 * MEASURED_LATENCY);
        final DetachedTerseAsadminResult jobIdResult = ASADMIN.execDetached("progress-custom", intervals);
        assertThat(jobIdResult, asadminOK());
        assertNotNull(jobIdResult.getJobId(), "id");

        // This will block until jobs finishes.
        final AsadminResult progResult = ASADMIN.exec("attach", jobIdResult.getJobId());
        // This will get the final result without waiting
        final AsadminResult finalResult = ASADMIN.exec("attach", jobIdResult.getJobId());
        assertAll(
            () -> assertThat(progResult, asadminOK()),
            () -> assertThat(progResult.getStdOut(), containsString("progress-custom")),
            () -> checkProgressOutput(progResult), () -> assertThat(finalResult, asadminOK()),
            () -> assertThat(finalResult.getStdOut(), containsString("progress-custom")),
            () -> assertThat(finalResult.getStdOut(), not(containsString("%")))
        );
        JobTestExtension.doAndDisableJobCleanup();
        assertThat("Attach after cleanup", ASADMIN.exec("attach", jobIdResult.getJobId()), not(asadminOK()));
    }


    @Test
    public void detachOnesAttachConcurrent() throws Exception {
        // This affects scheduling of threads and makes the test repeatable
        final int attachCount = Runtime.getRuntime().availableProcessors();
        // Latency multiplier estimated to make tests pass on most environments.
        // With faster asadmin and job scheduling it may me needed to change it.
        final String intervals = generateRegularIntervals(10 * MEASURED_LATENCY);
        final DetachedTerseAsadminResult jobIdResult = ASADMIN.execDetached("progress-custom", intervals);
        assertThat(jobIdResult, asadminOK());
        assertNotNull(jobIdResult.getJobId(), "id");

        // Let the job start. It will spend 10 * latency by its work.
        // There is a bit tricky causality issue:
        // The job must be already running and managed by JobManager so we could
        // monitor its progress.
        // If the job is still in some preparation phase we will not find it.
        // If it already completed, we will only find its report.
        // So we measured how much time it usually takes to run this command and get result.
        Thread.sleep(MEASURED_LATENCY);
        threadPool = createExecutor("Detached-asadmin-", Thread.MAX_PRIORITY, attachCount);
        final List<Future<AsadminResult>> futureResults = new ArrayList<>(attachCount);
        for (int i = 0; i < attachCount; i++) {
            futureResults.add(threadPool.submit(() -> {
                AsadminResult result = ASADMIN.exec("--echo", "attach", jobIdResult.getJobId());
                LOG.log(DEBUG, () -> "Received result. Error: " + result.isError());
                return result;
            }));
        }
        LOG.log(INFO, () -> "Started " + attachCount + " attaches to job id " + jobIdResult.getJobId());
        for (Future<AsadminResult> futureResult : futureResults) {
            final AsadminResult result = futureResult.get();
            final List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(result.getStdOut());
            assertAll(
                () -> assertThat(result, asadminOK()),
                // Twice. First is caused by --echo
                () -> assertThat(result.getStdOut(), containsString("progress-custom")),
                () -> assertThat(result.getStdOut(), not(containsString("FAILURE")))
            );
            if (prgs.isEmpty()) {
                // We were late to watch the progress, however soon enough to get the result.
                continue;
            }
            checkProgressOutput(result);
        }
    }

    private static ExecutorService createExecutor(final String namePrefix, final int priority, final int attachCount) {
        final AtomicInteger counter = new AtomicInteger();
        return Executors.newFixedThreadPool(attachCount, command -> {
            Thread thread = new Thread(command, namePrefix + counter.incrementAndGet());
            thread.setPriority(priority);
            thread.setDaemon(true);
            return thread;
        });
    }

    private static void checkProgressOutput(final AsadminResult progResult) {
        final List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(progResult.getStdOut());
        assertFalse(prgs.isEmpty(), "ProgressMessages.isEmpty()");
        assertAll(
            () -> assertThat("Min progress", prgs.get(0).getValue(), greaterThanOrEqualTo(0)),
            // Sometimes the completion status comes earlier than the last progress
            () -> assertThat("Max progress", prgs.get(prgs.size() - 1).getValue(), greaterThanOrEqualTo(99))
        );
    }
}
