/*
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

package org.glassfish.admin.rest.composite;

import java.util.Map;
import java.util.Set;

import org.glassfish.admin.rest.composite.metadata.RestModelMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author jdlee
 */
public class RestCollectionTest {
    private RestCollection<TestModel> rc;

    @BeforeEach
    public void setUp() {
        rc = new RestCollection();
    }

    @Test
    public void testAdd() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);
        assertEquals(1, rc.size());
        assertFalse(rc.isEmpty());
    }

    @Test
    public void testGet() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);

        RestModel<TestModel> rm = rc.get("1");
        assertEquals(tm, rm);
    }

    @Test
    public void testRemove() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);
        assertEquals(1, rc.size());
        assertFalse(rc.isEmpty());
        rc.remove("1");
        assertEquals(0, rc.size());
        assertTrue(rc.isEmpty());
    }

    @Test
    public void testContainsKey() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);
        assertEquals(1, rc.size());
        assertFalse(rc.isEmpty());
        assertTrue(rc.containsKey("1"));
    }

    @Test
    public void testContainsValue() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);
        assertEquals(1, rc.size());
        assertTrue(rc.containsValue(tm));
    }

    @Test
    public void testClear() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);
        assertEquals(1, rc.size());
        rc.clear();
        assertEquals(0, rc.size());
        assertTrue(rc.isEmpty());
    }

    @Test
    public void testGetKeySet() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);
        assertTrue(rc.keySet().contains(new RestModelMetadata("1")));
    }

    @Test
    public void testGetValues() throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);

        rc.put("1", tm);
        assertEquals(1, rc.values().size());
    }

    @Test
    public void testEntrySet()  throws Exception {
        TestModel tm = CompositeUtil.instance().getModel(TestModel.class);
        assertNotNull(tm);
        tm.setName("one");
        rc.put("1", tm);
        tm = CompositeUtil.instance().getModel(TestModel.class);
        tm.setName("two");
        rc.put("2", tm);

        Set<Map.Entry<RestModelMetadata, TestModel>> entries = rc.entrySet();
        assertEquals(2, entries.size());
        // Test contents...
    }

    public interface TestModel extends RestModel {
        String getName();
        void setName(String name);
    }
}
