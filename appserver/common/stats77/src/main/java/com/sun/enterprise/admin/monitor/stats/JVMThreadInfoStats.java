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
 * about each individual thread in the the thread system of the JVM.
 * @since 8.1
 */
public interface JVMThreadInfoStats extends Stats {

    /**
     * Returns the Id of the thread
     * @return  CountStatistic  Id of the thread
     */
    public CountStatistic getThreadId();

    /**
     * Returns the name of the thread
     * @return  StringStatistic name of the thread
     */
    public StringStatistic getThreadName();

    /**
     * Returns the state of the thread
     * @return StringStatistic  Thread state
     */
    public StringStatistic getThreadState();

    /**
     * Returns the elapsed time (in milliseconds) that the thread associated
     * with this ThreadInfo has blocked to enter or reenter a monitor since
     * thread contention monitoring is enabled.
     * @return CountStatistic   time elapsed in milliseconds, since the thread
     *                          entered the BLOCKED state. Returns -1 if thread
     *                          contention monitoring is disabled
     */
     public CountStatistic getBlockedTime();

     /**
      * Returns the number of times that this thread has been in the blocked
      * state
      * @return CountStatistic  the total number of times that the thread
      *                         entered the BLOCKED state
      */
     public CountStatistic getBlockedCount();

     /**
      * Returns the elapsed time(in milliseconds) that the thread has been in
      * the waiting state.
      * @returns CountStatistic elapsed time in milliseconds that the thread has
      *                         been in a WAITING state. Returns -1 if thread
      *                         contention monitoring is disabled.
      */
     public CountStatistic getWaitedTime();

     /**
      * Returns the number of times that the thread has been in WAITING or
      * TIMED_WAITING states
      * @return CountStatistic  total number of times that the thread was in
      *                         WAITING or TIMED_WAITING states
      */
     public CountStatistic getWaitedCount();

     /**
      * Returns the string representation of the monitor lock that the thread
      * is blocked to enter or waiting to be notified through
      * the Object.wait method
      * @return StringStatistic the string representation of the monitor lock
      */
     public StringStatistic getLockName();

     /**
      * Returns the Id of the thread which holds the monitor lock of an
      * object on which this thread is blocking
      * @return CountStatistic Id of the thread holding the lock.
      */
     public CountStatistic getLockOwnerId();


     /**
      * Returns the name of the thread that holds the monitor lock of the
      * object this thread is blocking on
      * @return StringStatistic name of the thread holding the monitor lock.
      */
     public StringStatistic getLockOwnerName();

     /**
      * Returns the stacktrace associated with this thread
      */
     public StringStatistic getStackTrace();
}
