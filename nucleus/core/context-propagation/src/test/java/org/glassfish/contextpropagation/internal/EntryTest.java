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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.internal.Entry.ContextType;
import org.junit.jupiter.api.Test;

import static org.glassfish.contextpropagation.internal.Entry.ContextType.ASCII_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EntryTest {

    private static final boolean IS_ORIGIN = true;
    private static final boolean ALLOW_ALL_TO_READ = true;

    @Test
    public void validateOk() {
        createContext("value", PropagationMode.defaultSet(), ASCII_STRING, IS_ORIGIN, ALLOW_ALL_TO_READ).validate();
    }


    @Test
    public void validateNullValue() {
        Entry context = createContext(null, PropagationMode.defaultSet(), ASCII_STRING, IS_ORIGIN, ALLOW_ALL_TO_READ);
        assertThrows(IllegalStateException.class, () -> context.validate());
    }


    @Test
    public void validateNullPropMode() {
        Entry context = createContext("value", null, ASCII_STRING, IS_ORIGIN, ALLOW_ALL_TO_READ);
        assertThrows(IllegalStateException.class, () -> context.validate());
    }


    @Test
    public void validateNullContextType() {
        Entry context = createContext("value", PropagationMode.defaultSet(), null, IS_ORIGIN, ALLOW_ALL_TO_READ);
        assertThrows(IllegalStateException.class, () -> context.validate());
    }


    @Test
    public void validateNullISOriginator() {
        Entry context = createContext("value", PropagationMode.defaultSet(), ASCII_STRING, (Boolean) null,
            ALLOW_ALL_TO_READ);
        assertThrows(IllegalStateException.class, () -> context.validate());
    }


    @Test
    public void validateNullAllowAllToRead() {
        Entry context = createContext("value", PropagationMode.defaultSet(), ASCII_STRING, IS_ORIGIN, null);
        assertThrows(IllegalStateException.class, () -> context.validate());
    }


    private Entry createContext(String value, EnumSet<PropagationMode> propModes, ContextType type, Boolean isOrigin,
        Boolean allowAllToRead) {
        Entry entry = new Entry(value, propModes, type);
        return entry.init(isOrigin, allowAllToRead);
    }


    @Test
    public void testToContextTypeFromNumberClass() {
        assertEquals(ContextType.ATOMICINTEGER, ContextType.fromNumberClass(AtomicInteger.class));
        assertEquals(ContextType.ATOMICLONG, ContextType.fromNumberClass(AtomicLong.class));
        assertEquals(ContextType.BIGDECIMAL, ContextType.fromNumberClass(BigDecimal.class));
        assertEquals(ContextType.BIGINTEGER, ContextType.fromNumberClass(BigInteger.class));
        assertEquals(ContextType.BYTE, ContextType.fromNumberClass(Byte.class));
        assertEquals(ContextType.DOUBLE, ContextType.fromNumberClass(Double.class));
        assertEquals(ContextType.FLOAT, ContextType.fromNumberClass(Float.class));
        assertEquals(ContextType.INT, ContextType.fromNumberClass(Integer.class));
        assertEquals(ContextType.LONG, ContextType.fromNumberClass(Long.class));
        assertEquals(ContextType.SHORT, ContextType.fromNumberClass(Short.class));
    }
}
