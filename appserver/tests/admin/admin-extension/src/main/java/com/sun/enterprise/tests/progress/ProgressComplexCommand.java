/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import java.util.logging.Logger;

import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ManagedJob;
import org.glassfish.api.admin.Progress;
import org.glassfish.api.admin.ProgressStatus;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Basic progress status example.
 * Contains 10 steps
 *
 * @author mmares
 */
@Service(name = "progress-complex")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("progress")
@ManagedJob
@Progress(name = "complex", totalStepCount = 20)
public class ProgressComplexCommand implements AdminCommand {

    @Override
    public void execute(final AdminCommandContext context) {
        final ProgressStatus parent = context.getProgressStatus();

        // Sum 16 allocated of 20 set in annotation.
        final ProgressStatus child1 = parent.createChild("child1", 5);
        child1.setTotalStepCount(10);

        final ProgressStatus child2 = parent.createChild("child2", 5);
        child2.setTotalStepCount(50);

        final ProgressStatus child3 = parent.createChild("child3", 6);
        child3.setTotalStepCount(112);

        // child1 has 2 children, each has 5 steps of 10 of their parent.
        // child11 sets also total step count to 5.
        // child12 doesn't.
        final ProgressStatus child11 = child1.createChild("child11", 5);
        child11.setTotalStepCount(5);
        final ProgressStatus child12 = child1.createChild("child12", 5);

        // child2 has 4 children, they allocate 10+10+10+15=45 of parent's 50
        // each child sets total step count to 25
        final ProgressStatus child21 = child2.createChild("child21", 10);
        child21.setTotalStepCount(25);
        final ProgressStatus child22 = child2.createChild("child22", 10);
        child22.setTotalStepCount(25);
        final ProgressStatus child23 = child2.createChild("child23", 10);
        child23.setTotalStepCount(25);
        final ProgressStatus child24 = child2.createChild("child24", 15);
        child24.setTotalStepCount(25);

        // We have prepared all children of child1 and child2.
        // Now we do 4 steps of child11.
        doProgress(child11, 4, 200, "progress child11");

        // child3 has 2 children which allocate 108 of 112 total steps
        final ProgressStatus child31 = child3.createChild("child31", 100);
        child31.setTotalStepCount(5);
        final ProgressStatus child32 = child3.createChild("child32", 8);
        child32.setTotalStepCount(5);

        // Now we do 4 steps of child3 (it's children have 108)
        // Then 1 (the last one) of child11
        // Then 5/5 of child32
        doProgress(child3, 4, 150, "progress child3");
        doProgress(child11, 1, 150, "progress child11");
        doProgress(child32, 5, 150, "progress child32");

        // Well, this is quite late, but we did not set it yet.
        // 5/5 of child 31, then 6/6 of child12
        child12.setTotalStepCount(6);
        doProgress(child31, 5, 150, "progress child31");
        doProgress(child12, 6, 150, "progress child12");

        // Finally let's do a paralel progress of child2x
        final Thread th21 = new Thread(new ProgressRunnable(child21, 25, 100, "progress child21"));
        final Thread th22 = new Thread(new ProgressRunnable(child22, 25, 100, "progress child22"));
        final Thread th23 = new Thread(new ProgressRunnable(child23, 25, 100, "progress child23"));
        final Thread th24 = new Thread(new ProgressRunnable(child24, 25, 100, "progress child24"));
        th21.start();
        th22.start();
        th23.start();
        th24.start();
        try {
            th21.join();
            th22.join();
            th23.join();
            th24.join();
        } catch (final InterruptedException ex) {
            context.getActionReport().failure(Logger.getGlobal(), "Unexpected interrupt", ex);
            return;
        }
        doProgress(parent, 4, 100, "progress main");
        doProgress(child2, 5, 100, "progress child2");
        context.getActionReport().appendMessage("All done");
    }


    private static void doProgress(final ProgressStatus ps, final int count, final long interval,
        final String message) {
        for (int i = 0; i < count; i++) {
            try {
                Thread.sleep(interval);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
            final int remainingStepCount = Math.max(0, ps.getRemainingStepCount() - 1);
            ps.progress(1, message + ", remaining " + remainingStepCount);
        }
    }

    static class ProgressRunnable implements Runnable {

        private final ProgressStatus ps;
        private final int count;
        private final long interval;
        private final String message;

        public ProgressRunnable(final ProgressStatus ps, final int count, final long interval, final String message) {
            this.ps = ps;
            this.count = count;
            this.interval = interval;
            this.message = message;
        }


        @Override
        public void run() {
            doProgress(ps, count, interval, message);
        }
    }
}
