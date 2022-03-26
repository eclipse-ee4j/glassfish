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

import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.Progress;
import org.glassfish.api.admin.ProgressStatus;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/** Same as {@code ProgressSimpleCommand} but this one has supplements.
 * It also does not specify totalStepCount in annotation but using API.
 * Percentage can be printed after {@code SupplementBefore} will be done.
 *
 * @see SupplementBefore
 * @see SupplementAfter
 * @author mmares
 */
@Service(name = "progress-supplement")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("progress")
@Progress()
public class ProgressWithSupplementCommand implements AdminCommand {

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(ProgressWithSupplementCommand.class);

    @Override
    public void execute(AdminCommandContext context) {
        ProgressStatus ps = context.getProgressStatus();
        ps.setTotalStepCount(4);
        ps.progress("Parsing");
        doSomeLogic();
        ps.progress(1, "Working on main part");
        for (int i = 0; i < 3; i++) {
            doSomeLogic();
            ps.progress(1);
        }
        ps.complete("Finished");
    }

    private void doSomeLogic() {
        try {
            Thread.sleep(300L);
        } catch (Exception ex) {
        }
    }

}
