/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import java.util.concurrent.atomic.AtomicInteger;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import static java.util.function.Function.identity;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * @author David Matejcek
 */
public class JobTestExtension implements BeforeAllCallback, AfterAllCallback {

    private static final String ORIG_POLL_INTERVAL = "origPollInterval";
    private static final String ORIG_INITIAL_DELAY = "origInitialDelay";
    private static final String ORIG_RETENTION_PERIOD = "origRetentionPeriod";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin(false);
    /** This is a trick to cause the change propagation every time we execute the command */
    private static final AtomicInteger POLL_CHANGER = new AtomicInteger(100);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Namespace namespaceClass = Namespace.create(context.getRequiredTestClass());
        Store store = context.getStore(namespaceClass);
        store.put(ORIG_RETENTION_PERIOD, ASADMIN.getValue("managed-job-config.job-retention-period", identity()).getValue());
        store.put(ORIG_INITIAL_DELAY, ASADMIN.getValue("managed-job-config.initial-delay", identity()).getValue());
        store.put(ORIG_POLL_INTERVAL, ASADMIN.getValue("managed-job-config.poll-interval", identity()).getValue());
        doAndDisableJobCleanup();
    }


    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        doAndDisableJobCleanup();
        Namespace namespaceClass = Namespace.create(context.getRequiredTestClass());
        Store store = context.getStore(namespaceClass);
        // reset original configuration
        assertThat(ASADMIN.exec("configure-managed-jobs",
            "--job-retention-period=" + store.get(ORIG_RETENTION_PERIOD),
            "--cleanup-initial-delay=" + store.get(ORIG_INITIAL_DELAY),
            "--cleanup-poll-interval=" + store.get(ORIG_POLL_INTERVAL)), asadminOK());
    }

    /**
     * Sets the retention period to 0 s to clean up all jobs.
     * Sets cleanup initial delay to 0 s to do the cleanup immediately.
     * Sets cleanup interval to always changing number to make it a change,
     * so JobCleanupService will get the change, but the job will not be scheduled
     * before we end tests.
     *
     * @throws Exception
     */
    public static void doAndDisableJobCleanup() throws Exception {
        assertThat(ASADMIN.exec("configure-managed-jobs",
            "--job-retention-period=0s",
            "--cleanup-initial-delay=0s",
            "--cleanup-poll-interval=" + POLL_CHANGER.incrementAndGet() + "m"), asadminOK());
        waitForJobCleanup(1);
    }


    /**
     * Expectation: the initial delay and retention are always zero
     *
     * @param cleanupPeriod the period in seconds of the job cleanup
     */
    public static void waitForJobCleanup(long cleanupPeriod) {
        //  the configured period plus reserve time
        final long maxEnd = System.currentTimeMillis() + cleanupPeriod * 1000L + 1000L;
        while (true) {
            AsadminResult result = ASADMIN.exec("list-jobs");
            assertThat(result, asadminOK());
            if (result.getStdOut().contains("Nothing to list")) {
                return;
            }
            if (System.currentTimeMillis() > maxEnd) {
                fail("Timed out waiting for cleanup of all jobs.");
            }
        }
    }


    /**
     * @param timeout seconds to wait for the job to complete
     */
    public static void waitForAllJobCompleted(long timeout) {
        //  the configured period plus reserve time
        final long maxEnd = System.currentTimeMillis() + timeout * 1000L;
        while (true) {
            AsadminResult result = ASADMIN.exec("list-jobs");
            assertThat(result, asadminOK());
            if (!result.getStdOut().contains("RUNNING")) {
                return;
            }
            if (System.currentTimeMillis() > maxEnd) {
                fail("Timed out waiting for cleanup of all jobs.");
            }
        }
    }
}
