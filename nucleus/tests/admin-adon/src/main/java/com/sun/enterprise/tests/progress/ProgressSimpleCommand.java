/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ManagedJob;
import org.glassfish.api.admin.Progress;
import org.glassfish.api.admin.ProgressStatus;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Basic progress status example. Contains 10 steps
 *
 * @author mmares
 */
@Service(name = "progress-simple")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("progress")
@ManagedJob
@Progress
public class ProgressSimpleCommand implements AdminCommand {

    @Param(name = "nototalsteps", primary = false, optional = true, defaultValue = "false")
    boolean noTotalSteps;

    @Override
    public void execute(AdminCommandContext context) {
        ProgressStatus progressStatus = context.getProgressStatus();

        if (!noTotalSteps) {
            progressStatus.setTotalStepCount(10);
        }

        progressStatus.progress("Parsing");

        doSomeLogic();
        progressStatus.progress(1, "Working on main part");
        for (int i = 0; i < 7; i++) {
            doSomeLogic();
            progressStatus.progress(1);
        }

        progressStatus.progress(1, "Cleaning");
        doSomeLogic();
        progressStatus.complete("Finished");

        context.getActionReport().appendMessage("All done");
    }

    private void doSomeLogic() {
        try {
            Thread.sleep(500L);
        } catch (Exception ex) {
        }
    }

}
