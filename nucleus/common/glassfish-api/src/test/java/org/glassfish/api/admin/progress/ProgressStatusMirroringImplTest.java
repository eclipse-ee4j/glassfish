/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author mmares
 */
public class ProgressStatusMirroringImplTest {

    private DummyParent parent;

    @BeforeEach
    public void setUp() {
        parent = new DummyParent();
    }

    @Test
    public void testTotalStepCount() {
        ProgressStatusMirroringImpl prog = new ProgressStatusMirroringImpl("first", parent, null);
        assertEquals(-1, prog.getTotalStepCount());
        assertEquals(0, prog.currentStepCount);
        ProgressStatus ch1 = prog.createChild("A1", 0);
        assertNotNull(ch1);
        ProgressStatus ch2 = prog.createChild("A2", 0);
        assertNotNull(ch2);
        ProgressStatus chm = prog.createChild(null, 0, 10);
        assertNotNull(chm);
        assertEquals(-1, prog.getTotalStepCount());
        assertEquals(0, prog.currentStepCount);
        ch1.setTotalStepCount(4);
        assertEquals(-1, prog.getTotalStepCount());
        assertEquals(0, prog.currentStepCount);
        ch2.setTotalStepCount(6);
        assertEquals(20, prog.getTotalStepCount());
        assertEquals(0, prog.currentStepCount);
        prog = new ProgressStatusMirroringImpl("second", parent, null);
        assertEquals(-1, prog.getTotalStepCount());
        ch1 = prog.createChild("A1", 0, 10);
        assertEquals(10, prog.getTotalStepCount());
        ch2 = prog.createChild("A2", 0);
        assertEquals(-1, prog.getTotalStepCount());
    }

    @Test
    public void testProgress() {
        ProgressStatusMirroringImpl prog = new ProgressStatusMirroringImpl("first", parent, null);
        ProgressStatus ch1 = prog.createChild("A1", 0);
        assertNotNull(ch1);
        ProgressStatus ch2 = prog.createChild("A2", 0);
        assertNotNull(ch2);
        assertEquals(0, prog.currentStepCount);
        parent.resetLastProgressStatusEvent();
        ch1.progress(1);
        assertNotNull(parent.getLastProgressStatusEvent());
        parent.resetLastProgressStatusEvent();
        assertEquals(1, prog.currentStepCount);
        ch2.progress(2, "Some message");
        assertEquals(3, prog.currentStepCount);
    }

    @Test
    public void testComplete() {
        ProgressStatusMirroringImpl prog = new ProgressStatusMirroringImpl("first", parent, null);
        ProgressStatus ch1 = prog.createChild("A1", 0, 10);
        assertNotNull(ch1);
        ProgressStatus ch2 = prog.createChild("A2", 0, 20);
        assertNotNull(ch2);
        assertEquals(0, prog.currentStepCount);
        ch1.progress(2);
        ch2.progress(3);
        assertEquals(25, prog.getRemainingStepCount());
        assertFalse(prog.isComplete());
        assertFalse(ch1.isComplete());
        assertFalse(ch2.isComplete());
        ch2.complete();
        assertTrue(ch2.isComplete());
        assertEquals(8, prog.getRemainingStepCount());
        prog.complete();
        assertTrue(ch2.isComplete());
        assertTrue(prog.isComplete());
    }


}
