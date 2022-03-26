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

import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ManagedJob;
import org.glassfish.api.admin.Progress;
import org.glassfish.api.admin.ProgressStatus;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/** Basic progress status example.
 * Contains 10 steps
 *
 * @author mmares
 */
@Service(name = "progress-double-totals")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("progress")
@Progress(totalStepCount=6)
@ManagedJob
public class ProgressDoubleTotalsCommand implements AdminCommand {

    @Override
    public void execute(AdminCommandContext context) {
        ProgressStatus ps = context.getProgressStatus();
        ps.progress("Parsing");
        doSomeLogic();
        ps.progress(1, "Working");
        for (int i = 0; i < 2; i++) {
            doSomeLogic();
            ps.progress(1);
        }
        ps.setTotalStepCount(12);
        ps.progress("Double");
        for (int i = 0; i < 4; i++) {
            doSomeLogic();
            ps.progress(2);
        }
        doSomeLogic();
        ps.progress(1);
        ps.complete("Finished");
        context.getActionReport().appendMessage("All done");
    }

    private void doSomeLogic() {
        try {
            Thread.sleep(200L);
        } catch (Exception ex) {
        }
    }

}
