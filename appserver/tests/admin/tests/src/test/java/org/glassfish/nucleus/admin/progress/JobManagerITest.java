/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.nucleus.admin.progress;

import org.glassfish.nucleus.test.tool.asadmin.Asadmin;
import org.glassfish.nucleus.test.tool.asadmin.AsadminResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.glassfish.nucleus.test.tool.AsadminResultMatcher.asadminOK;
import static org.glassfish.nucleus.test.tool.asadmin.GlassFishTestEnvironment.deleteJobsFile;
import static org.glassfish.nucleus.test.tool.asadmin.GlassFishTestEnvironment.deleteOsgiDirectory;
import static org.glassfish.nucleus.test.tool.asadmin.GlassFishTestEnvironment.getAsadmin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;

/**
 * This tests the functionality of JobManager, list-jobs
 *
 * @author Bhakti Mehta
 * @author David Matejcek
 */
@TestMethodOrder(OrderAnnotation.class)
public class JobManagerITest {

    private static final String COMMAND_PROGRESS_SIMPLE = "progress-simple";
    private static final Asadmin ASADMIN = getAsadmin();

    @BeforeEach
    public void setUp() throws Exception {
        assertThat(ASADMIN.exec("stop-domain"), asadminOK());
        deleteJobsFile();
        deleteOsgiDirectory();
        assertThat(ASADMIN.exec("start-domain"), asadminOK());
    }

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
        assertThat(ASADMIN.exec("--terse", "progress-simple"), asadminOK());
        assertThat(ASADMIN.exec("list-jobs").getStdOut(), stringContainsInOrder(COMMAND_PROGRESS_SIMPLE, "COMPLETED"));
        assertThat(ASADMIN.exec("list-jobs", "1").getStdOut(), stringContainsInOrder(COMMAND_PROGRESS_SIMPLE, "COMPLETED"));

        assertThat(ASADMIN.exec("stop-domain"), asadminOK());
        assertThat(ASADMIN.exec("start-domain"), asadminOK());
        AsadminResult result = ASADMIN.exec("list-jobs", "1");
        assertThat(result, asadminOK());
        assertThat(result.getStdOut(), stringContainsInOrder(COMMAND_PROGRESS_SIMPLE, "COMPLETED"));
    }


    @Test
    @Order(3)
    public void detachAndAttach() throws Exception {
        assertThat(ASADMIN.execDetached(COMMAND_PROGRESS_SIMPLE).getStdOut(), stringContainsInOrder("Job ID: "));
        assertThat(ASADMIN.exec("list-jobs", "1").getStdOut(), stringContainsInOrder(COMMAND_PROGRESS_SIMPLE));
        assertThat(ASADMIN.exec("attach", "1"), asadminOK());

        // list-jobs and it should be purged since the user
        // starting is the same as the user who attached to it
        assertThat(ASADMIN.exec("list-jobs").getOutput(), stringContainsInOrder("Nothing to list"));
    }


    @Test
    @Order(4)
    public void runConfigureManagedJobsTest() throws Exception {
        assertThat(ASADMIN.exec("configure-managed-jobs", "--job-retention-period=60s", "--cleanup-initial-delay=1s", "--cleanup-poll-interval=1s"), asadminOK());
        assertThat(ASADMIN.exec(COMMAND_PROGRESS_SIMPLE), asadminOK());
        assertThat(ASADMIN.exec("list-jobs", "1").getStdOut(), stringContainsInOrder(COMMAND_PROGRESS_SIMPLE));

        // FIXME: Random race condition on Linux caused by some bug in restart-domain; 4848 port is then blocked for start-domain in setUp();
        assertThat(ASADMIN.exec("stop-domain"), asadminOK());
        assertThat(ASADMIN.exec("start-domain"), asadminOK());
        assertThat(ASADMIN.exec("list-jobs", "1").getStdOut(), stringContainsInOrder(COMMAND_PROGRESS_SIMPLE));

        assertThat(ASADMIN.exec("configure-managed-jobs", "--job-retention-period=1s", "--cleanup-initial-delay=1s", "--cleanup-poll-interval=1s"), asadminOK());
        Thread.sleep(2100L);
        assertThat(ASADMIN.exec("list-jobs").getOutput(), stringContainsInOrder("Nothing to list"));
        assertThat(ASADMIN.exec("configure-managed-jobs", "--job-retention-period=1h", "--cleanup-initial-delay=5m", "--cleanup-poll-interval=20m"), asadminOK());
    }
}
