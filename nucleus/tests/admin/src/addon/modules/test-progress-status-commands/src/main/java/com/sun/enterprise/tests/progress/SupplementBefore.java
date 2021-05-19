/*
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

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.Progress;
import org.glassfish.api.admin.ProgressStatus;
import org.glassfish.api.admin.Supplemental;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

@Service
@Supplemental(value = "progress-supplement", on= Supplemental.Timing.Before )
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@Progress(name="before", totalStepCount=4)
public class SupplementBefore implements AdminCommand {

    @Override
    public void execute(AdminCommandContext context) {
        ProgressStatus ps = context.getProgressStatus();
        ps.progress("2 seconds supplemental command");
        for (int i = 0; i < 4; i++) {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException ex) {
            }
            ps.progress(1);
        }
        context.getActionReport().setActionExitCode(ActionReport.ExitCode.SUCCESS);
        ps.complete();
    }

}
