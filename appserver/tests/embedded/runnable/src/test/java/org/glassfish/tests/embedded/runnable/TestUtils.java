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
package org.glassfish.tests.embedded.runnable;

/**
 *
 * @author Ondro Mihalyi
 */
public abstract class TestUtils {

    private static final int WAIT_SECONDS = 30;

    private TestUtils() {
    }

    public static <T> T waitFor(String what, org.junit.jupiter.api.function.ThrowingSupplier<T> supplier) throws InterruptedException {
        Throwable lastThrowable = null;
        for (int i = 0; i < WAIT_SECONDS * 2; i++) {
            try {
                T result = supplier.get();
                if (result != null) {
                    return result;
                }
            } catch (UnsupportedOperationException unrecoverableError) {
                throw unrecoverableError;
            } catch (Throwable ignore) {
                lastThrowable = ignore;
            }
            Thread.sleep(500);
        }
        throw new RuntimeException(what + " not received within timeout", lastThrowable);
    }

}
