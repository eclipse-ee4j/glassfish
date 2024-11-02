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

package org.glassfish.api.admin.progress;

import org.glassfish.api.admin.ProgressStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author mmares
 */
public class ProgressStatusImplTest {

    private DummyParent parent;

    @BeforeEach
    public void prepareParent() {
        parent = new DummyParent();
    }

    @Test
    public void testGetSetTotalStepCount() {
        ProgressStatusImpl psi = new ProgressStatusImpl("first", parent, null);
        assertThat(psi.getTotalStepCount(), lessThan(0));
        psi.setTotalStepCount(10);
        assertNotNull(parent.getLastProgressStatusEvent());
        parent.resetLastProgressStatusEvent();
        assertEquals(10, psi.getTotalStepCount());
        psi = new ProgressStatusImpl("first", 10, parent, null);
        assertEquals(10, psi.getTotalStepCount());
        psi.progress(8);
        assertNotNull(parent.getLastProgressStatusEvent());
        parent.resetLastProgressStatusEvent();
        psi.setTotalStepCount(6);
        assertNotNull(parent.getLastProgressStatusEvent());
        parent.resetLastProgressStatusEvent();
        assertEquals(6, psi.getTotalStepCount());
        assertEquals(0, psi.getRemainingStepCount());
        psi.setTotalStepCount(10);
        assertEquals(10, psi.getTotalStepCount());
        assertEquals(4, psi.getRemainingStepCount());
        psi.complete();
        assertNotNull(parent.getLastProgressStatusEvent());
        parent.resetLastProgressStatusEvent();
        psi.setTotalStepCount(15);
        assertNull(parent.getLastProgressStatusEvent());
        assertEquals(10, psi.getTotalStepCount());
    }

    @Test
    public void testProgressAndGetRemainingStepCount() {
        ProgressStatusImpl psi = new ProgressStatusImpl("first", 10, parent, null);
        assertEquals(10, psi.getRemainingStepCount());
        psi.progress(1);
        assertNotNull(parent.getLastProgressStatusEvent());
        parent.resetLastProgressStatusEvent();
        assertEquals(9, psi.getRemainingStepCount());
        psi.progress(2);
        assertEquals(7, psi.getRemainingStepCount());
        psi.progress("Some message");
        assertEquals(7, psi.getRemainingStepCount());
        psi.progress(4, "Other message");
        assertEquals(3, psi.getRemainingStepCount());
        psi.progress(null);
        assertEquals(3, psi.getRemainingStepCount());
        psi.progress(2, null);
        assertEquals(1, psi.getRemainingStepCount());
        psi.progress(1);
        assertNotNull(parent.getLastProgressStatusEvent());
        parent.resetLastProgressStatusEvent();
        assertEquals(0, psi.getRemainingStepCount());
        psi.progress(1);
        assertNull(parent.getLastProgressStatusEvent());
        assertEquals(0, psi.getRemainingStepCount());
        psi = new ProgressStatusImpl("second", parent, null);
        assertThat(psi.getRemainingStepCount(), lessThan(0));
        psi.progress(1);
        assertThat(psi.getRemainingStepCount(), lessThan(0));
        psi.setTotalStepCount(10);
        assertEquals(9, psi.getRemainingStepCount());
        psi.complete();
        assertEquals(0, psi.getRemainingStepCount());
    }

    @Test
    public void testSetCurrentStepCount() {
        ProgressStatusImpl psi = new ProgressStatusImpl("first", 10, parent, null);
        psi.setCurrentStepCount(5);
        assertEquals(5, psi.getRemainingStepCount());
        psi.progress(1);
        assertEquals(4, psi.getRemainingStepCount());
        psi.setCurrentStepCount(8);
        assertEquals(2, psi.getRemainingStepCount());
        psi.setCurrentStepCount(12);
        assertEquals(0, psi.getRemainingStepCount());
        psi.setTotalStepCount(15);
        assertEquals(5, psi.getRemainingStepCount());
        psi.setCurrentStepCount(5);
        assertEquals(10, psi.getRemainingStepCount());
    }

    @Test
    public void testComplete() {
        ProgressStatusImpl psi = new ProgressStatusImpl("first", 10, parent, null);
        assertFalse(psi.isComplete());
        psi.complete();
        assertTrue(psi.isComplete());
        assertNotNull(parent.getLastProgressStatusEvent());
        parent.resetLastProgressStatusEvent();
        psi = new ProgressStatusImpl("first", parent, null);
        assertFalse(psi.isComplete());
        psi.complete();
        assertTrue(psi.isComplete());
        psi = new ProgressStatusImpl("first", 10, parent, null);
        psi.progress(8);
        assertFalse(psi.isComplete());
        psi.complete();
        assertTrue(psi.isComplete());
        psi = new ProgressStatusImpl("first", 10, parent, null);
        psi.progress(5);
        psi.progress(5);
        assertFalse(psi.isComplete());
        psi.progress(1);
        assertFalse(psi.isComplete());
        psi = new ProgressStatusImpl("first", 10, parent, null);
        psi.complete();
        parent.resetLastProgressStatusEvent();
        psi.complete();
        assertNull(parent.getLastProgressStatusEvent());
    }

    @Test
    public void testCreateChild() {
        ProgressStatusImpl psi = new ProgressStatusImpl("A", 10, parent, null);
        ProgressStatus ch1 = psi.createChild(2);
        ProgressStatus ch2 = psi.createChild("A.2", 3);
        assertEquals(5, psi.getRemainingStepCount());
        ProgressStatus ch3 = psi.createChild("A.3", 10);
        assertEquals(0, psi.getRemainingStepCount());
        assertEquals(15, psi.getTotalStepCount());
        parent.resetLastProgressStatusEvent();
        ch1.progress(1);
        assertNotNull(parent.getLastProgressStatusEvent());
        psi.complete();
        assertTrue(psi.isComplete());
        assertTrue(ch1.isComplete());
        assertTrue(ch2.isComplete());
        assertTrue(ch3.isComplete());
        psi = new ProgressStatusImpl("B", 10, parent, null);
        ch1 = psi.createChild("B.1", 3);
        psi.progress(2);
        assertEquals(5, psi.getRemainingStepCount());
        ch2 = psi.createChild("B.2", 8);
        assertEquals(11, psi.getTotalStepCount());
        psi.setTotalStepCount(15);
        assertEquals(4, psi.getRemainingStepCount());
        psi.complete();
        assertTrue(psi.isComplete());
        assertTrue(ch1.isComplete());
        assertTrue(ch2.isComplete());
    }

    @Test
    public void testIdGeneration() {
        ProgressStatusImpl psi = new ProgressStatusImpl("A", 10, null, null);
        assertNotNull(psi.id);
    }

}
