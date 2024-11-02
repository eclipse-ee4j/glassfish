/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.util.cache;

import com.sun.enterprise.security.store.AsadminSecurityUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * General test for AdminCache implementations which has file system
 * on background
 *
 * @author mmares
 */
public abstract class AdminCacheTstBase {

    public static final String TEST_CACHE_COTEXT = "junit-test-temp/";
    private final AdminCache cache;

    public AdminCacheTstBase(final AdminCache cache) {
        this.cache = cache;
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        // Clean up temp directory
        final File dir = new File(AsadminSecurityUtil.GF_CLIENT_DIR, TEST_CACHE_COTEXT);
        recursiveDelete(dir);
        // Test to create and write data
        if (!dir.mkdirs()) {
            fail("AdminCache tests: Can not do this test. Can not create " + dir.getPath() + " directory.");
            return;
        }
        final File f = new File(dir, "qeen.junit");
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write("Another One Bites the Dust".getBytes(UTF_8));
        }
        if (!f.exists()) {
            fail("AdminCache tests: Can not do this test. Can not write to files in " + dir.getPath() + " directory.");
        }
        recursiveDelete(dir);
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        recursiveDelete(new File(AsadminSecurityUtil.GF_CLIENT_DIR, TEST_CACHE_COTEXT));
    }


    @Test
    public void testPutGet() {
        // 1
        final String qeen1 = "Crazy Little Thing Called Love";
        final String qeen1Key = TEST_CACHE_COTEXT + "Qeen1";
        cache.put(qeen1Key, qeen1);
        File f = new File(AsadminSecurityUtil.GF_CLIENT_DIR, qeen1Key);
        assertTrue(f.exists());
        assertTrue(f.isFile());
        String str = cache.get(qeen1Key, String.class);
        assertNotNull(str);
        assertEquals(qeen1, str);

        // 2
        final String qeen2 = "You\'re My Best Friend";
        final String qeen2Key = TEST_CACHE_COTEXT + "A-Night-at-the-Opera/Qeen2";
        cache.put(qeen2Key, qeen2);
        f = new File(AsadminSecurityUtil.GF_CLIENT_DIR, qeen2Key);
        assertTrue(f.exists());
        assertTrue(f.isFile());
        str = cache.get(qeen2Key, String.class);
        assertNotNull(str);
        assertEquals(qeen2, str);

        // 1 - re read
        str = cache.get(qeen1Key, String.class);
        assertNotNull(str);
        assertEquals(qeen1, str);

        // 2 - update
        final String qeen2b = "Bohemian Rhapsody";
        cache.put(qeen2Key, qeen2b);
        str = cache.get(qeen2Key, String.class);
        assertNotNull(str);
        assertEquals(qeen2b, str);
    }


    @Test
    public void testExistence() throws InterruptedException {
        // 1
        final String stones1 = "Paint it black";
        final String stones1Key = TEST_CACHE_COTEXT + "Rolling.Stones.1";
        cache.put(stones1Key, stones1);

        // 2
        final String stones2 = "Jumpin\' Jack Flash";
        final String stones2Key = TEST_CACHE_COTEXT + "Rolling.Stones.2";
        cache.put(stones2Key, stones2);

        // contains
        assertTrue(cache.contains(stones1Key));
        assertTrue(cache.contains(stones2Key));
        assertFalse(cache.contains(stones1Key + "ooops"));

        // lastUpdated
        final Date lastUpdated1 = cache.lastUpdated(stones1Key);
        assertNotNull(lastUpdated1);
        Thread.sleep(50L);
        final String stones1b = "Good times, bad times";
        cache.put(stones1Key, stones1b);
        final Date lastUpdated2 = cache.lastUpdated(stones1Key);
        assertNotNull(lastUpdated2);
        assertTrue(lastUpdated1.getTime() < lastUpdated2.getTime());
    }


    protected static void recursiveDelete(final File f) {
        if (f == null || !f.exists()) {
            return;
        }
        if (".".equals(f.getName()) || "..".equals(f.getName())) {
            return;
        }
        if (f.isDirectory()) {
            final File[] subFiles = f.listFiles();
            for (final File subFile : subFiles) {
                recursiveDelete(subFile);
            }
        }
        f.delete();
    }

    protected AdminCache getCache() {
        return cache;
    }
}
