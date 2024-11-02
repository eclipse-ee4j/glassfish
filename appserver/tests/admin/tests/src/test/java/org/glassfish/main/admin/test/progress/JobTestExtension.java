/*
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
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * @author David Matejcek
 */
public class JobTestExtension implements BeforeAllCallback, AfterAllCallback {

    private static final String ORIG_POLL_INTERVAL = "origPollInterval";
    private static final String ORIG_INITIAL_DELAY = "origInitialDelay";
    private static final String ORIG_RETENTION_PERIOD = "origRetentionPeriod";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin(false);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Namespace namespaceClass = Namespace.create(context.getRequiredTestClass());
        Store store = context.getStore(namespaceClass);
        store.put(ORIG_RETENTION_PERIOD, ASADMIN.getValue("managed-job-config.job-retention-period", identity()).getValue());
        store.put(ORIG_INITIAL_DELAY, ASADMIN.getValue("managed-job-config.initial-delay", identity()).getValue());
        store.put(ORIG_POLL_INTERVAL, ASADMIN.getValue("managed-job-config.poll-interval", identity()).getValue());
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        waitForCompletition();

        // minimal possible values.
        assertThat(ASADMIN.exec("configure-managed-jobs",
            "--job-retention-period=1s",
            "--cleanup-initial-delay=1s",
            "--cleanup-poll-interval=1s"), asadminOK());

        Thread.sleep(1100L);
        // cleanup should be finished.
        AsadminResult result = ASADMIN.exec("list-jobs");
        assertThat(result.getOutput(), stringContainsInOrder("Nothing to list"));

        Namespace namespaceClass = Namespace.create(context.getRequiredTestClass());
        Store store = context.getStore(namespaceClass);
        // reset original configuration
        assertThat(ASADMIN.exec("configure-managed-jobs",
            "--job-retention-period=" + store.get(ORIG_RETENTION_PERIOD),
            "--cleanup-initial-delay=" + store.get(ORIG_INITIAL_DELAY),
            "--cleanup-poll-interval=" + store.get(ORIG_POLL_INTERVAL)), asadminOK());
    }


    private void waitForCompletition() throws Exception {
        final long start = System.currentTimeMillis();
        while (true) {
            AsadminResult result = ASADMIN.exec("list-jobs");
            assertThat(result, asadminOK());
            if (!result.getStdOut().contains("RUNNING")) {
                return;
            }
            if (System.currentTimeMillis() > start + 10000L) {
                fail("Timed out waiting for completition of all jobs.");
            }
            Thread.sleep(500);
        }
    }


}
