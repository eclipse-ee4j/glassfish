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

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.Progress;
import org.glassfish.api.admin.ProgressStatus;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * @author mmares
 * @author David Matejcek
 */
@Service(name = "progress-custom")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("progress")
@Progress
public class ProgressCustomCommand implements AdminCommand {

    /**
     * Value must be in for {@code [Nx][MINSEC-]MAXSEC}
     */
    @Param(primary = true, separator = ',')
    private String[] intervals;


    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        if (intervals == null || intervals.length == 0) {
            report.setMessage("Done command process without waiting interval.");
            return;
        }

        final ProgressStatus progressStatus = context.getProgressStatus();
        final List<Duration> parsedIntervals = Stream.of(intervals).map(Duration::parse).toList();

        progressStatus.setTotalStepCount(parsedIntervals.size());
        int blockId = 0;
        for (Duration interval : parsedIntervals) {
            blockId++;
            String intervalText = interval.toMillis() + " ms";
            if (interval.isZero()) {
                progressStatus.progress(1, "Finished block without sleeping: [" + blockId + "] " + intervalText);
            } else {
                progressStatus.progress(0, "Starting block [" + blockId + "] " + intervalText, false);
                try {
                    Thread.sleep(interval.toMillis());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                progressStatus.progress(1, "Finished block [" + blockId + "] " + intervalText);
            }
        }
        report.setMessage("Finished command process in " + parsedIntervals.size() + " block(s).");
    }

    /**
     * @param intervals in milliseconds
     * @return String which can be used as an argument.
     */
    public static String generateIntervals(Integer... intervals) {
        return Stream.of(intervals).map(Duration::ofMillis).map(Duration::toString).collect(Collectors.joining(","));
    }
}
