/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import java.util.List;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author martinmares
 */
@ExtendWith(JobTestExtension.class)
public class ProgressStatusFailITest {

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin(false);

    @AfterAll
    public static void waitForJobsToFinish() {
        JobTestExtension.waitForAllJobCompleted(10);
    }

    @Test
    public void failDuringExecution() {
        AsadminResult result = ASADMIN.exec("progress-fail-in-half");
        assertThat(result, not(asadminOK()));
        List<ProgressMessage> messages = ProgressMessage.grepProgressMessages(result.getStdOut());
        assertFalse(messages.isEmpty());
        assertEquals(50, messages.get(messages.size() - 1).getValue());
    }

    /**
     * This tests that we receive output even for job executed synchronously (not detached)
     * but timing out. This is possible because we use the SSE (Server Sent Events for the
     * communication between the asadmin command and the server.
     */
    @Test
    public void timeout() {
        AsadminResult result = ASADMIN.exec(2_000, "progress-custom", "1x1", "1x2", "1x1");
        assertThat(result, not(asadminOK()));
        List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(result.getStdOut());
        assertFalse(prgs.isEmpty(), "progress messages empty");
        assertEquals(33, prgs.get(prgs.size() - 1).getValue());
    }
}
