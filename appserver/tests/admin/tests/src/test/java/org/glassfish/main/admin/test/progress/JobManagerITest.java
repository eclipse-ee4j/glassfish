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

import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.DetachedTerseAsadminResult;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.stringContainsInOrder;

/**
 * This tests the functionality of JobManager, list-jobs
 *
 * @author Bhakti Mehta
 * @author David Matejcek
 */
@ExtendWith(JobTestExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class JobManagerITest {

    private static final String COMMAND_PROGRESS_SIMPLE = "progress-simple";
    private static final Asadmin ASADMIN = getAsadmin();


    @Test
    @Order(1)
    public void noJobsTest() {
        AsadminResult result = ASADMIN.exec("list-jobs");
        assertThat(result, asadminOK());
        assertThat(result.getOutput(), stringContainsInOrder("Nothing to list"));
    }


    @Test
    @Order(2)
    public void jobSurvivesRestart() throws Exception {
        assertThat(ASADMIN.exec("configure-managed-jobs",
            "--job-retention-period=1h",
            "--cleanup-initial-delay=0s",
            "--cleanup-poll-interval=0s"), asadminOK());
        assertThat(ASADMIN.exec(COMMAND_PROGRESS_SIMPLE), asadminOK());
        assertThat(ASADMIN.exec(60_000, "restart-domain"), asadminOK());
        assertThat(ASADMIN.exec("list-jobs").getStdOut(), stringContainsInOrder(COMMAND_PROGRESS_SIMPLE, "COMPLETED"));
        JobTestExtension.doAndDisableJobCleanup();
    }


    @Test
    @Order(3)
    public void detachAndAttach() throws Exception {
        DetachedTerseAsadminResult detached = ASADMIN.execDetached(COMMAND_PROGRESS_SIMPLE);
        assertThat(detached.getJobId(), matchesPattern("[1-9][0-9]*"));
        assertThat(ASADMIN.exec("list-jobs", detached.getJobId()).getStdOut(), stringContainsInOrder(COMMAND_PROGRESS_SIMPLE));
        assertThat(ASADMIN.exec("attach", detached.getJobId()), asadminOK());

        JobTestExtension.doAndDisableJobCleanup();

        assertThat(ASADMIN.exec("list-jobs", detached.getJobId()).getOutput(), stringContainsInOrder("Nothing to list"));
    }


    @Test
    @Order(4)
    public void runSynchronously() throws Exception {
        assertThat(ASADMIN.exec(COMMAND_PROGRESS_SIMPLE), asadminOK());
        assertThat(ASADMIN.exec("list-jobs").getStdOut(), stringContainsInOrder(COMMAND_PROGRESS_SIMPLE));

        assertThat(ASADMIN.exec("configure-managed-jobs",
            "--job-retention-period=0s",
            "--cleanup-initial-delay=1s",
            "--cleanup-poll-interval=60s"), asadminOK());

        Thread.sleep(1500L);
        assertThat("Completed jobs should be removed after delay", ASADMIN.exec("list-jobs").getOutput(),
            stringContainsInOrder("Nothing to list"));
    }
}
