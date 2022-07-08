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


/**
 * A {@link LogRotationTimerTask} used for delayed scheduling of the log file rotation.
 */
public class PeriodicalLogRotationTimerTask extends LogRotationTimerTask {

    private final long delay;

    /**
     * Creates a task which should be executed periodically after the delay from the time of
     * scheduling.
     *
     * @param action action to be executed
     * @param delayInMillis
     */
    public PeriodicalLogRotationTimerTask(final LogFileRotationImplementation action, final long delayInMillis) {
        super(action);
        this.delay = delayInMillis;
    }


    @Override
    public long computeDelayInMillis() {
        return delay;
    }
}
