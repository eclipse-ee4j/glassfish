/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
 * Returns the statistical information associated with the HttpService thread pool
 *
 * @author  nsegura
 */
public interface PWCThreadPoolStats extends Stats {

    /**
     * Returns the thread pool Id
     * @return id
     */
    StringStatistic getId();

    /**
     * Returns the number of threads that are currently idle
     * @return idle threads
     */
    CountStatistic getCountThreadsIdle();

    /**
     * Returns current number of threads
     * @return current threads
     */
    CountStatistic getCountThreads();

    /**
     * Returns the maximum number of native threads allowed in the thread pool
     * @return max number of threads allowed
     */
    CountStatistic getMaxThreads();

    /**
     * Returns the current number of requests waiting for a native thread
     * @return queued requests
     */
    CountStatistic getCountQueued();

    /**
     * Returns the highest number of requests that were ever queued up
     * simultaneously for the use of a native thread since the server
     * was started
     */
    CountStatistic getPeakQueued();

    /**
     * Returns the maximum number of requests that can be queued at one
     * time to wait for a native thread
     * @return max number of request to be queued
     */
    CountStatistic getMaxQueued();

}
