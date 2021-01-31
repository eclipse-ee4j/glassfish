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

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.component.MultiMap;

/**
 * An executor responsible for executing supplemental commands registered for a main command
 *
 * @author Vijay Ramachandran
 */
@Contract
public interface SupplementalCommandExecutor {

    Collection<SupplementalCommand> listSuplementalCommands(String commandName);

    ActionReport.ExitCode execute(Collection<SupplementalCommand> suplementals, Supplemental.Timing time, AdminCommandContext context,
            ParameterMap parameters, MultiMap<String, File> optionFileMap);

    public interface SupplementalCommand {

        void execute(AdminCommandContext ctxt);

        AdminCommand getCommand();

        boolean toBeExecutedBefore();

        boolean toBeExecutedAfter();

        boolean toBeExecutedAfterReplication();

        FailurePolicy onFailure();

        List<RuntimeType> whereToRun();

        ProgressStatus getProgressStatus();

        void setProgressStatus(ProgressStatus progressStatus);

        Progress getProgressAnnotation();

    }

}
