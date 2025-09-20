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

package org.glassfish.tests.utils.junit.matcher;

import java.lang.System.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import static java.lang.System.Logger.Level.WARNING;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Repeats the executable action and catches the {@link AssertionError} until the test passes or times out.
 * <p>
 * Note: Dont't confuse with {@link org.junit.jupiter.api.AssertTimeout}
 *
 * @author David Matejcek
 */
public class WaitForExecutable implements Executable {

    private static final Logger LOG = System.getLogger(WaitForExecutable.class.getName());

    private final Executable executable;

    private WaitForExecutable(final Executable executable) {
        this.executable = executable;
    }


    /**
     * Creates and runs new executable waiting to pass given executable.
     *
     * @param executable executable that has to end without errors to pass the test
     * @param timeoutInMillis timeout to pass
     * @throws Throwable thrown after timeout, not sooner
     */
    public static void waitFor(final Executable executable, final long timeoutInMillis) throws Throwable {
        waitUntilPassed(executable, timeoutInMillis).execute();
    }


    /**
     * Creates a new executable waiting to pass given executable. Don't forget to execute in
     * {@link Assertions#assertAll(Executable...)} or similar method..
     *
     * @param executable executable that has to end without errors to pass the test
     * @param timeoutInMillis timeout to pass
     * @return new instance of {@link WaitForExecutable}
     */
    public static WaitForExecutable waitUntilPassed(final Executable executable, final long timeoutInMillis) {
        final Executable newExecutable = () -> {
            long currentTimeMillis = System.currentTimeMillis();
            final long limit = currentTimeMillis + timeoutInMillis;
            while (true) {
                try {
                    executable.execute();
                    return;
                } catch (final AssertionError e) {
                    final long now = System.currentTimeMillis();
                    if (now > limit) {
                        throw e;
                    }
                    LOG.log(WARNING, "Nope. Remaining time is {0} ms. Waiting ...", limit - now);
                    Thread.sleep(100L);
                } catch (final Throwable e) {
                    fail(e);
                }
            }
        };
        return new WaitForExecutable(newExecutable);
    }


    @Override
    public void execute() throws Throwable {
        executable.execute();
    }
}
