/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.universal.process;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProcessUtilsTest {

    @Test
    void waitForDoesNotSpin() {
        final AtomicInteger evaluations = new AtomicInteger();
        assertFalse(ProcessUtils.waitFor(() -> evaluations.incrementAndGet() < 0, Duration.ofMillis(200L), false));
        assertTrue(evaluations.get() < 100, () -> "The sign was evaluated " + evaluations.get() + " times in 200 ms");
    }

    @Test
    void waitForReturnsWhenSignIsTrue() {
        final AtomicInteger evaluations = new AtomicInteger();
        assertTrue(ProcessUtils.waitFor(() -> evaluations.incrementAndGet() == 3, Duration.ofSeconds(10L), false));
        assertEquals(3, evaluations.get());
    }
}
