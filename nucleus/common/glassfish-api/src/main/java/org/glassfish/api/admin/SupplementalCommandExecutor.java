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
import org.glassfish.api.Param;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.component.MultiMap;

import java.io.File;
import java.util.*;

/**
 * An executor responsible for executing supplemental commands registered for a main command
 *
 * @author Vijay Ramachandran
 */
@Contract
public interface SupplementalCommandExecutor {

    public Collection<SupplementalCommand> listSuplementalCommands(String commandName);

    public ActionReport.ExitCode execute(Collection<SupplementalCommand> suplementals, Supplemental.Timing time, AdminCommandContext context,
            ParameterMap parameters, MultiMap<String, File> optionFileMap);

    public interface SupplementalCommand {

        public void execute(AdminCommandContext ctxt);

        public AdminCommand getCommand();

        public boolean toBeExecutedBefore();

        public boolean toBeExecutedAfter();

        public boolean toBeExecutedAfterReplication();

        public FailurePolicy onFailure();

        public List<RuntimeType> whereToRun();

        public ProgressStatus getProgressStatus();

        public void setProgressStatus(ProgressStatus progressStatus);

        public Progress getProgressAnnotation();

    }

}
