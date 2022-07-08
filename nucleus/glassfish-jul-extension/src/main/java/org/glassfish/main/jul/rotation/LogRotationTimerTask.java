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

import java.util.TimerTask;

/**
 * A {@link TimerTask} used for log file rotation, scheduled to be executed after the delay.
 *
 * @author David Matejcek
 */
public abstract class LogRotationTimerTask extends TimerTask {

    private final LogFileRotationImplementation action;

    /**
     * @param action to be executed.
     */
    protected LogRotationTimerTask(final LogFileRotationImplementation action) {
        this.action = action;
    }


    /**
     * @return time in millis from now to the next execution of the action.
     */
    public abstract long computeDelayInMillis();


    @Override
    public void run() {
        action.execute();
    }
}
