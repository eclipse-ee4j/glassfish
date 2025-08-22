/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.tests.progress;

import jakarta.inject.Inject;

import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.Progress;
import org.glassfish.api.admin.ProgressStatus;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Example of other command execution with progress status support.
 *
 * @author mmares
 */
@Service(name = "progress-exec-other")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("progress")
@Progress(totalStepCount = 40)
public class ProgressExecOtherCommand implements AdminCommand {

    @Inject
    CommandRunner<?> commandRunner;

    @Override
    public void execute(AdminCommandContext context) {
        ProgressStatus progressStatus = context.getProgressStatus();

        // Do some before logic
        progressStatus.progress("Preparing");
        for (int i = 0; i < 10; i++) {
            doSomeLogic();
            progressStatus.progress(1);
        }

        // Execute other command
        // Number 20 is little bit tricky. Please see javadoc of ProgressStatus
        commandRunner.getCommandInvocation("progress-simple",
            context.getActionReport().addSubActionsReport(),
            context.getSubject())
        .progressStatus(progressStatus.createChild("subcommand", 20)).execute();

        // Do some after logic
        progressStatus.progress("Finishing outer command");
        for (int i = 0; i < 10; i++) {
            doSomeLogic();
            progressStatus.progress(1);
        }

        progressStatus.complete("Finished outer command");
    }

    private void doSomeLogic() {
        try {
            Thread.sleep(250L);
        } catch (Exception ex) {
        }
    }

}
