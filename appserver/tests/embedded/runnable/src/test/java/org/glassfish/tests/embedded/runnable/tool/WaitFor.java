/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.tests.embedded.runnable.tool;

import java.lang.System.Logger;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.function.ThrowingSupplier;

import static java.lang.System.Logger.Level.INFO;

/**
 * @author Ondro Mihalyi
 */
public final class WaitFor {
    private static final Logger LOG = System.getLogger(WaitFor.class.getName());

    private WaitFor() {
    }


    /**
     * Repeats the supplier's action until it returns an object.
     * Ignores null and all exceptions except {@link InterruptedException}.
     *
     * @param <T> Expected return type
     * @param maxTime maximal duration of waiting for the non-null result.
     * @param actionDescription description of the expected result.
     * @param action
     * @return result.
     */
    public static <T> T waitFor(Duration maxTime, String actionDescription, ThrowingSupplier<T> action) {
        Throwable lastThrowable = null;
        final Instant deadline = Instant.now().plus(maxTime);
        while (true) {
            try {
                T result = action.get();
                LOG.log(INFO, () -> actionDescription + " result is: " + result);
                if (result != null) {
                    return result;
                }
            } catch (UnsupportedOperationException unrecoverableError) {
                throw unrecoverableError;
            } catch (Throwable t) {
                lastThrowable = t;
            }
            if (deadline.isBefore(Instant.now())) {
                throw new RuntimeException(actionDescription + " not received within timeout", lastThrowable);
            }
            Thread.onSpinWait();
        }
    }
}
