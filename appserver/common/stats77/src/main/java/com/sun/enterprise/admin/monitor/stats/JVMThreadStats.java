/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.monitor.stats;
import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.Stats;

/**
 * A Stats interface, to expose the monitoring information
 * about the thread system of the JVM.
 * @since 8.1
 */
public interface JVMThreadStats extends Stats {

    /**
     * Returns the current number of live daemon and non-daemon
     * threads
     * @return CountStatistic   current number of live threads
     */
    CountStatistic getThreadCount();

    /**
     * Returns the peak live thread count, since the JVM started or
     * the peak was reset
     * @return CountStatistic   peak live thread count
     */
    CountStatistic getPeakThreadCount();

    /**
     * Returns the total number of threads created and also started
     * since the JVM started
     * @return CountStatistic   total number of threads started
     */
    CountStatistic getTotalStartedThreadCount();

    /**
     * Returns the current number of live daemon threads
     * @return CountStatistic   current number of live daemon threads
     */
    CountStatistic getDaemonThreadCount();

    /**
     * Returns a comma separated list of all live thread ids
     * @return StringStatistic  live thread ids
     */
    StringStatistic getAllThreadIds();

    /**
     * Returns the CPU time for the current thread in nanoseconds, if
     * CPU time measurement is enabled. Else returns -1
     * @return  CountStatistic  CPU time for the current thread
     */
    CountStatistic getCurrentThreadCPUTime();

    /**
     * Returns a comma separated list of thread ids that are
     * monitor deadlocked
     * @return StringStatistic
     */
    StringStatistic getMonitorDeadlockedThreads();

}
