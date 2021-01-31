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
import org.jvnet.hk2.annotations.Contract;

/**
 * A ClusterExecutor is responsible for remotely executing commands. The list of target servers (either clusters or
 * remote instances) is obtained from the parameter list.
 *
 * @author Jerome Dochez
 */
@Contract
public interface ClusterExecutor {

    /**
     * <p>
     * Execute the passed command on targeted remote instances. The list of remote instances is usually retrieved from the
     * passed parameters (with a "target" parameter for instance) or from the configuration.
     *
     * <p>
     * Each remote execution must return a different ActionReport so the user or framework can get feedback on the success
     * or failure or such executions.
     *
     * @param commandName the name of the command to execute
     * @param command the command to execute
     * @param context the original command context
     * @param parameters the parameters passed to the original local command
     * @return the exit status of overall command replication
     */
    public ActionReport.ExitCode execute(String commandName, AdminCommand command, AdminCommandContext context, ParameterMap parameters);
}
