/*
 * Copyright (c) 2022,2025 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import org.hamcrest.CustomTypeSafeMatcher;

/**
 * Matcher checking that {@link Asadmin} command succeeded. Prints its output otherwise.
 *
 * @author David Matejcek
 */
public class AsadminResultMatcher extends CustomTypeSafeMatcher<AsadminResult> {

    private AsadminResultMatcher() {
        super("asadmin succeeded");
    }

    @Override
    protected boolean matchesSafely(AsadminResult item) {
        return !item.isError();
    }

    /**
     * @return matcher checking that {@link Asadmin} command succeeded. Prints it's output otherwise.
     */
    public static CustomTypeSafeMatcher<AsadminResult> asadminOK() {
        return new AsadminResultMatcher();
    }

    /**
     * Returns true if the command failed and contains the specified message in stderr
     * @param expectedErrorMessage A message that should appear in the stderr to match this error
     * @return matcher checking that {@link Asadmin} command failed.
     */
    public static CustomTypeSafeMatcher<AsadminResult> asadminError(String expectedErrorMessage) {
        return new AsadminFailedMatcher(expectedErrorMessage);
    }

    private static class AsadminFailedMatcher extends CustomTypeSafeMatcher<AsadminResult> {

        private String expectedErrorMessage;

        private AsadminFailedMatcher(String expectedErrorMessage) {
            super("asadmin failed in an expected way");
            this.expectedErrorMessage = expectedErrorMessage;
        }

        @Override
        protected boolean matchesSafely(AsadminResult item) {
            return item.isError() && item.getStdErr().contains(expectedErrorMessage);
        }

    }
}
