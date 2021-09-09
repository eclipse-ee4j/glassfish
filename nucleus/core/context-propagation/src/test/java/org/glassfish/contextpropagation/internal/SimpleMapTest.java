/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.contextpropagation.internal;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.glassfish.contextpropagation.ContextLifecycle;
import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.ViewCapable;
import org.glassfish.contextpropagation.adaptors.BootstrapUtils;
import org.glassfish.contextpropagation.adaptors.MockContextAccessController;
import org.glassfish.contextpropagation.adaptors.MockThreadLocalAccessor;
import org.glassfish.contextpropagation.adaptors.RecordingLoggerAdapter;
import org.glassfish.contextpropagation.bootstrap.ContextBootstrap;
import org.glassfish.contextpropagation.bootstrap.LoggerAdapter.Level;
import org.glassfish.contextpropagation.bootstrap.LoggerAdapter.MessageID;
import org.glassfish.contextpropagation.internal.Entry.ContextType;
import org.glassfish.contextpropagation.internal.SimpleMap.Filter;
import org.glassfish.contextpropagation.wireadapters.glassfish.DefaultWireAdapter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleMapTest {

    private SimpleMap sm;
    private static RecordingLoggerAdapter logger;

    public static class LifeCycleEventRecorder implements ContextLifecycle {

        StackTraceElement lastElement;
        Object lastArg;

        @Override
        public void contextChanged(Object replacementContext) {
            set(Thread.currentThread().getStackTrace(), replacementContext);
        }


        @Override
        public void contextAdded() {
            set(Thread.currentThread().getStackTrace(), null);
        }


        @Override
        public void contextRemoved() {
            set(Thread.currentThread().getStackTrace(), null);
        }


        @Override
        public ViewCapable contextToPropagate() {
            set(Thread.currentThread().getStackTrace(), null);
            return this;
        }


        void set(StackTraceElement[] trace, Object arg) {
            lastElement = trace[1];
            lastArg = arg;
        }


        void verify(String methodName, Object arg) {
            assertEquals(methodName, lastElement.getMethodName());
            assertEquals(arg, lastArg);
            lastElement = null;
            lastArg = null;
        }

    }

    private static final LifeCycleEventRecorder LIFE_CYCLE_CONTEXT = new LifeCycleEventRecorder();
    static final Entry DUMMY_ENTRY = createEntry(LIFE_CYCLE_CONTEXT, PropagationMode.defaultSet(), ContextType.OPAQUE);

    private static Entry createEntry(Object context, EnumSet<PropagationMode> propModes, ContextType ct) {
        return new Entry(context, propModes, ct) {

            @Override
            void validate() {
            }
        };
    }


    @BeforeAll
    public static void bootstrap() {
        // must be called here too, because isConfigured is initialized in static block
        BootstrapUtils.reset();
        logger = new RecordingLoggerAdapter();
        ContextBootstrap.configure(logger, new DefaultWireAdapter(), new MockThreadLocalAccessor(),
            new MockContextAccessController(), "guid");
    }

    @AfterAll
    public static void reset() {
        BootstrapUtils.reset();
    }


    @BeforeEach
    public void setup() {
        sm = new SimpleMap();
        sm.put("foo", createEntry("fooString", PropagationMode.defaultSet(), ContextType.STRING));
    }


    @Test
    public void testGetEntry() {
        Entry entry = sm.getEntry("foo");
        assertEquals("fooString", entry.getValue());
        logger.verify(Level.DEBUG, null, MessageID.OPERATION, new Object[] {"getEntry", "foo", entry});
    }


    @Test
    public void testGetEntryWithNullKey() {
        assertThrows(IllegalArgumentException.class, () -> sm.getEntry(null));
    }


    @Test
    public void testGet() {
        assertEquals("fooString", sm.get("foo"));
        logger.verify(Level.DEBUG, null, MessageID.OPERATION, new Object[] {"get", "foo", "fooString"});
    }


    @Test
    public void testGetWithNullKey() {
        assertThrows(IllegalArgumentException.class, () -> sm.get(null));
    }


    @Test
    public void testPutWhereNoBefore() {
        Entry e = sm.put("new key", DUMMY_ENTRY);
        assertNull(e);
        logger.verify(Level.DEBUG, null, MessageID.PUT, new Object[] {"new key", DUMMY_ENTRY.value, null});
        LIFE_CYCLE_CONTEXT.verify("contextAdded", null);
    }


    @Test
    public void testPutReplace() {
        LifeCycleEventRecorder oldRecorder = new LifeCycleEventRecorder();
        String fooString = sm.put("foo", createEntry(oldRecorder, PropagationMode.defaultSet(), ContextType.OPAQUE));
        assertEquals("fooString", fooString);
        logger.verify(Level.DEBUG, null, MessageID.PUT, new Object[] {"foo", oldRecorder, "fooString"});
        LifeCycleEventRecorder oldValue = sm.put("foo", DUMMY_ENTRY);
        assertEquals(oldRecorder, oldValue);
        // oldRecoder finds out about the new value
        oldRecorder.verify("contextChanged", LIFE_CYCLE_CONTEXT);
        LIFE_CYCLE_CONTEXT.verify("contextAdded", null);
    }


    @Test
    public void testPutWithNullKey() {
        assertThrows(IllegalArgumentException.class, () -> sm.put(null, DUMMY_ENTRY));
    }


    @Test
    public void testPutWithNullEntry() {
        assertThrows(IllegalArgumentException.class, () -> sm.put("dummy key", null));
    }


    @Test
    public void testPutWithNullValue() {
        assertThrows(IllegalArgumentException.class,
            () -> sm.put("dummy key", createEntry(null, PropagationMode.defaultSet(), ContextType.ATOMICINTEGER)));
    }


    @Test
    public void testPutWithInvalidEntry() {
        Entry entry = new Entry(null, PropagationMode.defaultSet(), ContextType.ATOMICINTEGER) {

            @Override
            void validate() {
                throw new IllegalStateException();
            }
        };
        assertThrows(IllegalArgumentException.class, () -> sm.put("dummy key", entry));
    }


    @Test
    public void testRemove() {
        sm.put("removeMe", createEntry(LIFE_CYCLE_CONTEXT, PropagationMode.defaultSet(), ContextType.STRING));
        Object removeMe = sm.remove("removeMe");
        assertEquals(LIFE_CYCLE_CONTEXT, removeMe);
        logger.verify(Level.DEBUG, null, MessageID.OPERATION, new Object[] {"remove", "removeMe", LIFE_CYCLE_CONTEXT});
        LIFE_CYCLE_CONTEXT.verify("contextRemoved", null);
    }


    @Test
    public void testRemoveNoneExistent() {
        String removeMe = sm.remove("removeMe");
        assertEquals(null, removeMe);
    }


    @Test
    public void testEmptyIterator() {
        SimpleMap emptyMap = new SimpleMap();
        Iterator<?> iter = emptyMap.iterator(null, null);
        assertFalse(iter.hasNext());
    }


    @Test
    public void testIteratorFiltersAll() {
        sm.put("dummy", DUMMY_ENTRY);
        Iterator<Map.Entry<String, Entry>> iter = sm.iterator(new Filter() {

            @Override
            public boolean keep(java.util.Map.Entry<String, Entry> mapEntry, PropagationMode mode) {
                return false;
            }
        }, PropagationMode.JMS_QUEUE);
        assertFalse(iter.hasNext());
    }


    @SuppressWarnings("serial")
    @Test
    public void testIteratorFilterNone() {
        sm.put("dummy", DUMMY_ENTRY);
        Iterator<Map.Entry<String, Entry>> iter = sm.iterator(new Filter() {

            @Override
            public boolean keep(java.util.Map.Entry<String, Entry> mapEntry, PropagationMode mode) {
                return true;
            }
        }, PropagationMode.JMS_QUEUE);
        int count = 0;
        HashSet<String> keys = new HashSet<>();
        while (iter.hasNext()) {
            keys.add(iter.next().getKey());
            count++;
        }
        assertEquals(2, count);
        assertEquals(new HashSet<String>() {

            {
                add("foo");
                add("dummy");
            }
        }, keys);
    }


    @Test
    public void testIteratorRemove() {
        sm.put("dummy", DUMMY_ENTRY);
        Iterator<Map.Entry<String, Entry>> iter = sm.iterator(new Filter() {

            @Override
            public boolean keep(java.util.Map.Entry<String, Entry> mapEntry, PropagationMode mode) {
                return true;
            }
        }, PropagationMode.JMS_QUEUE);
        assertEquals(2, sm.map.size());
        assertNotNull(iter.next());
        iter.remove();
        assertEquals(1, sm.map.size());
        assertNotNull(iter.next());
        iter.remove();
        assertEquals(0, sm.map.size());
        int exceptionCount = 0;
        try {
            iter.next();
        } catch (NoSuchElementException nsee) {
            exceptionCount++;
        }
        assertEquals(1, exceptionCount, "Expected NoSuchElementException after the last element was retrieved");
        try {
            iter.remove();
        } catch (IllegalStateException ise) {
            exceptionCount++;
        }
        assertEquals(2, exceptionCount, "Expected IllegalStateException on last remove call");
    }

}
