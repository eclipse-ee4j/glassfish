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

import org.glassfish.nucleus.test.tool.DomainLifecycleExtension;
import org.glassfish.nucleus.test.tool.NucleusTestUtils.NadminReturn;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.glassfish.nucleus.test.tool.NucleusTestUtils.deleteJobsFile;
import static org.glassfish.nucleus.test.tool.NucleusTestUtils.deleteOsgiDirectory;
import static org.glassfish.nucleus.test.tool.NucleusTestUtils.nadmin;
import static org.glassfish.nucleus.test.tool.NucleusTestUtils.nadminWithOutput;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author martinmares
 */
@ExtendWith(DomainLifecycleExtension.class)
public class DetachAttachTest {

    @AfterEach
    public void cleanUp() throws Exception {
        nadmin("stop-domain");
        deleteJobsFile();
        deleteOsgiDirectory();
        nadmin("start-domain");
    }

    @Test
    public void uptimePeriodically() throws Exception {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            System.out.println("detachAndAttachUptimePeriodically(): round " + i);
            final String id;
            {
                NadminReturn result = nadminWithOutput("--detach", "--terse", "uptime");
                assertTrue(result.returnValue);
                id = parseJobIdFromEchoTerse(result.out);
                assertTrue(ids.add(id));
            }
            Thread.sleep(1000L);
            {
                NadminReturn result = nadminWithOutput("--terse", "attach", id);
                assertTrue(result.returnValue);
                assertTrue(result.out.contains("uptime"));
            }
        }
    }


    @Test
    public void commandWithProgressStatus() throws Exception {
        NadminReturn result = nadminWithOutput("--detach", "--terse", "progress-custom", "6x1");
        assertTrue(result.returnValue);
        String id = parseJobIdFromEchoTerse(result.out);
        Thread.sleep(2000L);
        // Now attach running
        result = nadminWithOutput("attach", id);
        assertTrue(result.returnValue);
        assertThat(result.out, stringContainsInOrder("progress-custom"));
        List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(result.out);
        assertFalse(prgs.isEmpty());
        assertTrue(prgs.get(0).getValue() > 0);
        assertEquals(100, prgs.get(prgs.size() - 1).getValue());
        // Now attach finished - must NOT exist - seen progress job is removed
        result = nadminWithOutput("attach", id);
        assertFalse(result.returnValue);
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
        NadminReturn result = nadminWithOutput("--detach", "--terse", "progress-custom", "8x1");
        assertTrue(result.returnValue);
        final String id = parseJobIdFromEchoTerse(result.out);
        Thread.sleep(1500L);
        final int attachCount = 3;
        Collection<Callable<NadminReturn>> attaches = new ArrayList<>(attachCount);
        for (int i = 0; i < attachCount; i++) {
            attaches.add(new Callable<NadminReturn>() {
                @Override
                public NadminReturn call() throws Exception {
                    return nadminWithOutput("attach", id);
                }
            });
        }
        List<Future<NadminReturn>> results = pool.invokeAll(attaches);
        for (Future<NadminReturn> fRes : results) {
            NadminReturn res = fRes.get();
            assertTrue(res.returnValue);
            assertTrue(res.out.contains("progress-custom"));
            List<ProgressMessage> prgs = ProgressMessage.grepProgressMessages(res.out);
            assertFalse(prgs.isEmpty());
            assertTrue(prgs.get(0).getValue() > 0);
            assertEquals(100, prgs.get(prgs.size() - 1).getValue());
        }
    }

    private String parseJobIdFromEchoTerse(String str) {
        List<Object> stok = Collections.list(new StringTokenizer(str));
        assertThat(stok, hasSize(1));
        return (String) stok.get(0);
    }
}
