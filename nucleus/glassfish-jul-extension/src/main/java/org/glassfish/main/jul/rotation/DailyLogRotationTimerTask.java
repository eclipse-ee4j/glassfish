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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Special {@link LogRotationTimerTask} used for scheduling of the log file rotation on the start
 * of each day.
 * <p>
 * If we would use the {@link Timer#scheduleAtFixedRate(TimerTask, long, long)}, it would
 * not respect changes in local time (summer/winter), so that is the reason why we implemented
 * this.
 */
public class DailyLogRotationTimerTask extends LogRotationTimerTask {

    /**
     * Creates a task which should be executed at the start of each day.
     *
     * @param action action to be executed
     */
    public DailyLogRotationTimerTask(final LogFileRotationImplementation action) {
        super(action);
    }


    /**
     * @return time in millis from now to the next midnight in local time.
     */
    @Override
    public long computeDelayInMillis() {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return now.until(nextMidnight, ChronoUnit.MILLIS);
    }
}
