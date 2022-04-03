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

import org.glassfish.nucleus.test.tool.DomainLifecycleExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.glassfish.nucleus.test.tool.NucleusTestUtils.deleteJobsFile;
import static org.glassfish.nucleus.test.tool.NucleusTestUtils.deleteOsgiDirectory;
import static org.glassfish.nucleus.test.tool.NucleusTestUtils.nadmin;
import static org.glassfish.nucleus.test.tool.NucleusTestUtils.nadminDetachWithOutput;
import static org.glassfish.nucleus.test.tool.NucleusTestUtils.nadminWithOutput;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This tests the functionality of JobManager, list-jobs
 *
 * @author Bhakti Mehta
 * @author David Matejcek
 */
@TestMethodOrder(OrderAnnotation.class)
@ExtendWith(DomainLifecycleExtension.class)
public class JobManagerITest {

    private static final String COMMAND_PROGRESS_SIMPLE = "progress-simple";

    @BeforeEach
    public void setUp() throws Exception {
        nadmin("stop-domain");
        deleteJobsFile();
        deleteOsgiDirectory();
        assertTrue(nadmin("start-domain"), "start-domain failed");
    }

    @Test
    @Order(1)
    public void noJobsTest() {
        assertThat(nadminWithOutput("list-jobs").outAndErr, stringContainsInOrder("Nothing to list"));
    }


    @Test
    @Order(2)
    public void jobSurvivesRestart() throws Exception {
        assertTrue(nadminWithOutput("--terse", "progress-simple").returnValue);
        assertThat(nadminWithOutput("list-jobs").out, stringContainsInOrder(COMMAND_PROGRESS_SIMPLE, "COMPLETED"));
        assertThat(nadminWithOutput("list-jobs", "1").out, stringContainsInOrder(COMMAND_PROGRESS_SIMPLE, "COMPLETED"));

        assertTrue(nadmin("stop-domain"));
        assertTrue(nadmin("start-domain"));
        assertThat(nadminWithOutput("list-jobs", "1").out, stringContainsInOrder(COMMAND_PROGRESS_SIMPLE, "COMPLETED"));
    }


    @Test
    @Order(3)
    public void detachAndAttach() throws Exception {
        assertThat(nadminDetachWithOutput(COMMAND_PROGRESS_SIMPLE).out, stringContainsInOrder("Job ID: "));
        assertThat(nadminWithOutput("list-jobs", "1").out, stringContainsInOrder(COMMAND_PROGRESS_SIMPLE));
        assertTrue(nadmin("attach", "1"));

        // list-jobs and it should be purged since the user
        // starting is the same as the user who attached to it
        assertThat(nadminWithOutput("list-jobs").outAndErr, stringContainsInOrder("Nothing to list"));
    }


    @Test
    @Order(4)
    public void runConfigureManagedJobsTest() throws Exception {
        assertTrue(nadmin("configure-managed-jobs", "--job-retention-period=60s", "--cleanup-initial-delay=1s", "--cleanup-poll-interval=1s"));
        assertTrue(nadmin(COMMAND_PROGRESS_SIMPLE), COMMAND_PROGRESS_SIMPLE + " failed to start");
        assertThat(nadminWithOutput("list-jobs", "1").out, stringContainsInOrder(COMMAND_PROGRESS_SIMPLE));

        // FIXME: Random race condition on Linux caused by some bug in restart-domain; 4848 port is then blocked for start-domain in setUp();
        assertTrue(nadmin("stop-domain"));
        assertTrue(nadmin("start-domain"));
        assertThat(nadminWithOutput("list-jobs", "1").out, stringContainsInOrder(COMMAND_PROGRESS_SIMPLE));

        assertTrue(nadmin("configure-managed-jobs", "--job-retention-period=1s", "--cleanup-initial-delay=1s", "--cleanup-poll-interval=1s"));
        Thread.sleep(2100L);
        assertThat(nadminWithOutput("list-jobs").outAndErr, stringContainsInOrder("Nothing to list"));
        assertTrue(nadmin("configure-managed-jobs", "--job-retention-period=1h", "--cleanup-initial-delay=5m", "--cleanup-poll-interval=20m"));
    }
}
