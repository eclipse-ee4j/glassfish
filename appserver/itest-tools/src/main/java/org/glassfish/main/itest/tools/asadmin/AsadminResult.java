/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.itest.tools.asadmin;

/**
 * Result of the {@link Asadmin} execution.
 *
 * @author David Matejcek
 */
public class AsadminResult {

    private final boolean error;
    private final String stdOut;
    private final String stdErr;
    private final String output;

    /**
     * Creates a value object instance.
     *
     * @param commandName
     * @param exitCode
     * @param stdOut
     * @param stdErr
     */
    public AsadminResult(final String commandName, final int exitCode, final String stdOut, final String stdErr) {
        this.error = exitCode != 0 || containsError(stdOut, String.format("Command %s failed.", commandName));
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.output = (this.stdOut + " " + this.stdErr).strip();
    }


    /**
     * @return true if the error code was not zero OR stdOut contained text <i>Command x failed</i>.
     */
    public boolean isError() {
        return error;
    }


    /**
     * @return standard output made by the command.
     */
    public String getStdOut() {
        return stdOut;
    }

    /**
     * @return error output made by the command.
     */
    public String getStdErr() {
        return stdErr;
    }


    /**
     * @return {@link #getStdOut()} + {@link #getStdErr()}
     */
    public String getOutput() {
        return output;
    }


    /**
     * Returns {@link #getOutput()}. Important for hamcrest matchers!
     */
    @Override
    public String toString() {
        return getOutput();
    }


    private static boolean containsError(final String text, final String... invalidResults) {
        for (final String result : invalidResults) {
            if (text.contains(result)) {
                return true;
            }
        }
        return false;
    }
}