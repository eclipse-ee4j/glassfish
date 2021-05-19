/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author martinmares
 */
public class BeanUtilsTest {

    public static class Bean1 {

        private String one;
        private boolean two;
        private int three;

        public String getOne() {
            return one;
        }

        public void setOne(String one) {
            this.one = one;
        }

        public boolean isTwo() {
            return two;
        }

        public void setTwo(boolean two) {
            this.two = two;
        }

        public void setTwo(boolean two, String neco) {
            this.two = two;
        }

        public int getThree() {
            return three;
        }

        public int someMethod() {
            return 0;
        }
    }

    public BeanUtilsTest() {
    }

    @Test
    public void testBeanToMap() throws Exception {
        Bean1 b = new Bean1();
        b.setOne("1");
        b.setTwo(true);
        Map<String, Object> map = BeanUtils.beanToMap(b);
        assertNotNull(map);
        assertEquals(3, map.size());
        assertEquals("1", map.get("one"));
        assertTrue((Boolean) map.get("two"));
        assertNotNull(map.get("three"));
    }

    @Test
    public void testMapToBean1() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("one", "hello");
        map.put("two", true);
        Bean1 b = new Bean1();
        BeanUtils.mapToBean(b, map, false);
        assertEquals("hello", b.getOne());
        assertTrue(b.isTwo());
    }

    @Test
    public void testMapToBean2() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("one", "hello");
        map.put("two", true);
        map.put("some", "some");
        Bean1 b = new Bean1();
        BeanUtils.mapToBean(b, map, true);
        try {
            BeanUtils.mapToBean(b, map, false);
            assertTrue(false);
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    public void testGetGetters() throws Exception {
        Bean1 b = new Bean1();
        Collection<Method> getters = BeanUtils.getGetters(b);
        assertNotNull(getters);
        assertEquals(3, getters.size());
    }

    @Test
    public void testGetSetters() throws Exception {
        Bean1 b = new Bean1();
        Collection<Method> setters = BeanUtils.getSetters(b);
        assertNotNull(setters);
        assertEquals(2, setters.size());
    }

}
