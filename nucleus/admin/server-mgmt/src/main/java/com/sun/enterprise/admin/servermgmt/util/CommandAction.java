/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.servermgmt.util;

import java.time.Duration;
import java.time.Instant;

import org.glassfish.api.admin.CommandException;

/**
 * Action to be executed by {@link #step(String, Duration, CommandAction)} and wraoped by a simple time
 * monitoring report.
 * Example of the standard output:
 * <pre>
 * Waiting until start of domain domain1 completes... finished after 1488 ms
 * </pre>
 */
@FunctionalInterface
public interface CommandAction {

    /**
     * Action implementation.
     * Use {@link #step(String, Duration, CommandAction)} to execute the action.
     *
     * @throws CommandException
     */
    void action() throws CommandException;

    /**
     * Action to be executed by {@link #step(String, Duration, CommandAction)} and wraoped by a simple time
     * monitoring report.
     * Example of the standard output:
     * <pre>
     * Waiting until start of domain domain1 completes... finished after 1488 ms
     * </pre>
     * @param message The message will be printed before the action.
     * @param timeout can be null
     * @param action action to be executed
     * @return remaining timeout.
     * @throws CommandException
     */
    static Duration step(String message, Duration timeout, CommandAction action) throws CommandException {
        if (timeout != null && timeout.isNegative()) {
            return timeout;
        }
        if (message != null) {
            System.out.print(message);
        }
        Instant start = Instant.now();
        action.action();
        Duration stopDuration = Duration.between(start, Instant.now());
        if (message != null) {
            System.out.println(" ... finished after " + stopDuration.toMillis() + " ms.");
        }
        return timeout == null ? null : timeout.minus(stopDuration);
    }
}
