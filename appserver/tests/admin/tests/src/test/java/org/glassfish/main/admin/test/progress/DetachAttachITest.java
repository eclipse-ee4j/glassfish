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

package org.glassfish.main.admin.test.progress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.main.admin.test.tool.asadmin.Asadmin;
import org.glassfish.main.admin.test.tool.asadmin.AsadminResult;
import org.glassfish.main.admin.test.tool.asadmin.GlassFishTestEnvironment;
import org.junit.jupiter.api.Test;

import static org.glassfish.main.admin.test.tool.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author martinmares
 */
public class DetachAttachITest {
    private static final Logger LOG = Logger.getLogger(DetachAttachITest.class.getName());
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @Test
    public void uptimePeriodically() throws Exception {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            LOG.log(Level.FINE, "detachAndAttachUptimePeriodically(): round " + i);
            final String id;
            {
                AsadminResult result = ASADMIN.execDetached("--terse", "uptime");
                assertThat(result, asadminOK());
                id = parseJobIdFromEchoTerse(result.getStdOut());
                assertTrue(ids.add(id));
            }
            Thread.sleep(1000L);
            {
                AsadminResult result = ASADMIN.exec("--terse", "attach", id);
                assertThat(result, asadminOK());
                assertTrue(result.getStdOut().contains("uptime"));
            }
        }
    }


    @Test
    public void commandWithProgressStatus() throws Exception {
        AsadminResult result = ASADMIN.execDetached("--terse", "progress-custom", "6x1");
        assertThat(result, asadminOK());
        String id = parseJobIdFromEchoTerse(result.getStdOut());
        Thread.sleep(2000L);
        // Now attach running
        result = ASADMIN.exec("attach", id);
        assertThat(result, asadminOK());
        assertThat(result.getStdOut(), stringContainsInOrder("progress-custom"));
        List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(result.getStdOut());
        assertFalse(prgs.isEmpty());
        assertThat(prgs.get(0).getValue(), greaterThan(0));
        assertEquals(100, prgs.get(prgs.size() - 1).getValue());
        // Now attach finished - must NOT exist - seen progress job is removed
        assertThat(ASADMIN.exec("attach", id), not(asadminOK()));
    }


    @Test
    public void detachOnesAttachMulti() throws Exception {
        ExecutorService pool = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread result = new Thread(r);
                result.setDaemon(true);
                return result;
            }
        });
        AsadminResult result = ASADMIN.execDetached("--terse", "progress-custom", "8x1");
        assertThat(result, asadminOK());
        final String id = parseJobIdFromEchoTerse(result.getStdOut());
        assertNotNull(id, "id");
        Thread.sleep(1500L);
        final int attachCount = 3;
        Collection<Callable<AsadminResult>> attaches = new ArrayList<>(attachCount);
        for (int i = 0; i < attachCount; i++) {
            attaches.add(new Callable<AsadminResult>() {
                @Override
                public AsadminResult call() throws Exception {
                    return ASADMIN.exec("attach", id);
                }
            });
        }
        List<Future<AsadminResult>> results = pool.invokeAll(attaches);
        for (Future<AsadminResult> fRes : results) {
            AsadminResult res = fRes.get();
            assertThat(res, asadminOK());
            assertTrue(res.getStdOut().contains("progress-custom"));
            List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(res.getStdOut());
            assertFalse(prgs.isEmpty());
            assertThat(prgs.get(0).getValue(), greaterThan(0));
            assertEquals(100, prgs.get(prgs.size() - 1).getValue());
        }
    }

    private String parseJobIdFromEchoTerse(String str) {
        List<Object> stok = Collections.list(new StringTokenizer(str));
        assertThat(stok, hasSize(1));
        return (String) stok.get(0);
    }
}
