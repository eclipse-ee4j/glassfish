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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.DetachedTerseAsadminResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author martinmares
 */
@ExtendWith(JobTestExtension.class)
public class DetachAttachITest {
    private static final Logger LOG = Logger.getLogger(DetachAttachITest.class.getName());
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin(false);


    @Test
    public void uptimePeriodically() throws Exception {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            LOG.log(Level.FINE, "detachAndAttachUptimePeriodically(): round " + i);
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
        final DetachedTerseAsadminResult detached = ASADMIN.execDetached("progress-custom", "6x1");
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
        ExecutorService pool = Executors.newCachedThreadPool(r -> {
            Thread result = new Thread(r);
            result.setDaemon(true);
            return result;
        });
        final DetachedTerseAsadminResult result = ASADMIN.execDetached("progress-custom", "8x1");
        assertThat(result, asadminOK());
        assertNotNull(result.getJobId(), "id");
        final int attachCount = 3;
        Collection<Callable<AsadminResult>> attaches = new ArrayList<>(attachCount);
        for (int i = 0; i < attachCount; i++) {
            attaches.add(() -> ASADMIN.exec("attach", result.getJobId()));
        }
        List<Future<AsadminResult>> results = pool.invokeAll(attaches);
        for (Future<AsadminResult> fRes : results) {
            AsadminResult res = fRes.get();
            assertThat(res, asadminOK());
            assertTrue(res.getStdOut().contains("progress-custom"));
            List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(res.getStdOut());
            assertFalse(prgs.isEmpty());
            assertThat(prgs.get(0).getValue(), greaterThanOrEqualTo(0));
            assertEquals(100, prgs.get(prgs.size() - 1).getValue());
        }
    }
}
