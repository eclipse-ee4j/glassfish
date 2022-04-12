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

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.logging.LogDomains;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ManagedJob;
import org.glassfish.api.admin.Progress;
import org.glassfish.api.admin.ProgressStatus;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/** Basic progress status example.
 * Contains 10 steps
 *
 * @author mmares
 */
@Service(name = "progress-complex")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("progress")
@ManagedJob
@Progress(name="complex", totalStepCount=20)
public class ProgressComplexCommand implements AdminCommand {

    private final static Logger logger =
            LogDomains.getLogger(ProgressComplexCommand.class, LogDomains.ADMIN_LOGGER);

    @Override
    public void execute(AdminCommandContext context) {
        ProgressStatus ps = context.getProgressStatus();
        ProgressStatus ch1 = ps.createChild("ch1", 5);
        ProgressStatus ch2 = ps.createChild("ch2-paral", 5);
        ProgressStatus ch3 = ps.createChild("ch3", 6);
        //Prepare ch1
        ch1.setTotalStepCount(10);
        ProgressStatus ch11 = ch1.createChild("ch11", 5);
        ch11.setTotalStepCount(5);
        ProgressStatus ch12 = ch1.createChild("ch12", 5);
        //Prepare ch2
        ch2.setTotalStepCount(50);
        ProgressStatus ch21 = ch2.createChild("ch21", 10);
        ch21.setTotalStepCount(25);
        ProgressStatus ch22 = ch2.createChild("ch22", 10);
        ch22.setTotalStepCount(25);
        ProgressStatus ch23 = ch2.createChild("ch23", 10);
        ch23.setTotalStepCount(25);
        ProgressStatus ch24 = ch2.createChild("ch24", 15);
        ch24.setTotalStepCount(25);
        //First move ch1
        doProgress(ch11, 4, 200, "progress ch1.1");
        //Init ch3
        ch3.setTotalStepCount(112);
        ProgressStatus ch31 = ch3.createChild("ch31", 100);
        ch31.setTotalStepCount(5);
        ProgressStatus ch32 = ch3.createChild("ch32", 8);
        ch32.setTotalStepCount(5);
        //Move ch3 then ch1 and then ch3 and then finish ch1
        doProgress(ch3, 4, 150, "progress ch3");
        doProgress(ch11, 1, 150, "progress ch1.1");
        doProgress(ch32, 5, 150, "progress ch3.2");
        ch12.setTotalStepCount(6);
        doProgress(ch31, 5, 150, "progress ch3.1");
        doProgress(ch12, 6, 150, "progress ch1.2");
        //Do paralel progress of ch2.x
        Thread th21 = new Thread(new ProgressRunnable(ch21, 25, 100, "progress ch2.1"));
        Thread th22 = new Thread(new ProgressRunnable(ch22, 25, 100, "progress ch2.2"));
        Thread th23 = new Thread(new ProgressRunnable(ch23, 25, 100, "progress ch2.3"));
        Thread th24 = new Thread(new ProgressRunnable(ch24, 25, 100, "progress ch2.4"));
        th21.start();
        th22.start();
        th23.start();
        th24.start();
        try {
            th21.join();
            th22.join();
            th23.join();
            th24.join();
        } catch (InterruptedException ex) {
            context.getActionReport().failure(Logger.global, "Unexpected interrupt", ex);
            return;
        }
        doProgress(ps, 4, 100, "progress main");
        doProgress(ch2, 5, 100, "progress ch2");
        context.getActionReport().appendMessage("All done");
    }

    private static void doProgress(ProgressStatus ps, int count, long interval, String message) {
        for (int i = 0; i < count; i++) {
            try {
                Thread.sleep(interval);
            } catch (Exception ex) {
            }
            if (message != null) {
                int rsc = ps.getRemainingStepCount() - 1;
                ps.progress(1, message + ", remaining " + rsc);
            } else {
                ps.progress(1);
            }
        }
    }

    static class ProgressRunnable implements Runnable {

        private final ProgressStatus ps;
        private final int count;
        private final long interval;
        private final String message;

        public ProgressRunnable(ProgressStatus ps, int count, long interval, String message) {
            this.ps = ps;
            this.count = count;
            this.interval = interval;
            this.message = message;
        }

        public void run() {
            doProgress(ps, count, interval, message);
        }


    }

}
