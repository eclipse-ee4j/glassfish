/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul.rotation;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author David Matejcek
 */
public class DailyLogRotationTimerTaskTest {

    private static final String MESSAGE = "Hi, it works!";

    @Test
    public void test() {
        final StringBuilder message = new StringBuilder();
        final DailyLogRotationTimerTask task = new DailyLogRotationTimerTask(() -> message.append(MESSAGE));
        assertAll(() -> assertThat("delay must be positive", task.computeDelayInMillis(), greaterThan(0L)),
            () -> assertEquals(0L, task.scheduledExecutionTime(), "task is not scheduled yet"));
        task.run();
        assertEquals(MESSAGE, message.toString(), "run() did not execute the action");
    }

}
