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

import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.adaptors.BootstrapUtils;
import org.glassfish.contextpropagation.internal.Entry.ContextType;
import org.glassfish.contextpropagation.wireadapters.glassfish.DefaultWireAdapter;
import org.glassfish.tests.utils.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ViewImplTest {

    private static ViewImpl view;
    private static SimpleMap sm;

    @BeforeAll
    public static void setupClass() {
        BootstrapUtils.bootstrap(new DefaultWireAdapter());
        view = new ViewImpl("prefix");
        sm = new SimpleMap();
        Utils.setField(view, "sMap", sm);
        sm.put("prefix.removeMe",
            new Entry("removeMe", PropagationMode.defaultSet(), ContextType.STRING).init(true, true));
        sm.put("prefix.getMe", new Entry("getMe", PropagationMode.defaultSet(), ContextType.STRING).init(true, true));
        sm.put("prefix.string", new Entry("string", PropagationMode.defaultSet(), ContextType.STRING).init(true, true));
        sm.put("prefix.asciiString",
            new Entry("asciistring", PropagationMode.defaultSet(), ContextType.ASCII_STRING).init(true, true));
        sm.put("prefix.long", new Entry(1L, PropagationMode.defaultSet(), ContextType.LONG).init(true, true));
        sm.put("prefix.boolean", new Entry(true, PropagationMode.defaultSet(), ContextType.BOOLEAN).init(true, true));
        sm.put("prefix.char", new Entry('c', PropagationMode.defaultSet(), ContextType.CHAR).init(true, true));
        sm.put("prefix.serializable",
            new Entry("serializable", PropagationMode.defaultSet(), ContextType.SERIALIZABLE).init(true, true));
    }


    @Test
    public void testGet() {
        assertEquals("getMe", view.get("getMe"));
    }


    @Test
    public void testPutString() {
        String key = "string";
        String oldValue = "string";
        String newValue = "new_string";
        String returnedValue = view.put(key, newValue, PropagationMode.defaultSet());
        checkPut(key, oldValue, newValue, returnedValue);
    }


    @Test
    public void testPutLong() {
        String key = "long";
        long oldValue = 1L;
        long newValue = 2L;
        Long returnedValue = view.put(key, newValue, PropagationMode.defaultSet());
        checkPut(key, oldValue, newValue, returnedValue);
    }


    @Test
    public void testPutBoolean() {
        String key = "boolean";
        boolean oldValue = true;
        boolean newValue = false;
        Boolean returnedValue = view.put(key, newValue, PropagationMode.defaultSet());
        checkPut(key, oldValue, newValue, returnedValue);
    }


    @Test
    public void testPutChar() {
        String key = "char";
        char oldValue = 'c';
        char newValue = 'd';
        Character returnedValue = view.put(key, newValue, PropagationMode.defaultSet());
        checkPut(key, oldValue, newValue, returnedValue);
    }


    @Test
    public void testRemove() {
        assertEquals("removeMe", view.remove("removeMe"));
    }


    private void checkPut(String key, Object origValue, Object newValue, Object returnedValue) {
        assertEquals(origValue, returnedValue);
        assertEquals(newValue, sm.get("prefix." + key));
        assertEquals(ContextType.STRING, sm.getEntry("prefix.string").getContextType());
    }
}
