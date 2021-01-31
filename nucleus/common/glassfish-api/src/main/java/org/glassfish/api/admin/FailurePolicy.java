/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin;

import org.glassfish.api.ActionReport;

/**
 * Defines the expected behaviour from the system when a supplemental command (could be a local or remote invocation)
 * fails to execute properly.
 *
 * @author Jerome Dochez
 */
public enum FailurePolicy {

    /**
     * Ignore the failure, do not report to the user.
     */
    Ignore,
    /**
     * Warn the user of the failure, does not change the overall exit code of the command execution.
     */
    Warn,
    /**
     * Return an error exit code to the user but do not rollback any successful invocations of the commands.
     */
    Error;

    public static ActionReport.ExitCode applyFailurePolicy(FailurePolicy f, ActionReport.ExitCode e) {
        ActionReport.ExitCode result = ActionReport.ExitCode.FAILURE;
        if (f == null) {
            f = Error;
        }
        switch (f) {
        case Ignore:
            // If policy is to ignore, always return success
            result = ActionReport.ExitCode.SUCCESS;
            break;
        case Warn:
            // Switch failures to Warning; Leave Warnings and Successes as is
            if (e.equals(ActionReport.ExitCode.FAILURE)) {
                result = ActionReport.ExitCode.WARNING;
            } else {
                result = e;
            }
            break;
        case Error:
            result = e;
            break;
        }
        return result;
    }
}
