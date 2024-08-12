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
import org.glassfish.j2ee.statistics.RangeStatistic;
import org.glassfish.j2ee.statistics.Stats;

/**
 *
 * @since 8.1
 */
public interface StatefulSessionStoreStats extends Stats {

    /**
     * Returns the number of passivated/checkpointed sessions in the
     * store
     * @return RangeStatistic
     */
    RangeStatistic getCurrentSize();

    /**
     * Returns the total number of sessions activated from the store
     * @return CountStatistic
     */
    CountStatistic getActivationCount();

    /**
     * Returns the total number of sessions successfully activated
     * from the store
     * @return CountStatistic
     */
    CountStatistic getActivationSuccessCount();

    /**
     * Returns the total number of sessions that could not be activated
     * from the store
     * @return CountStatistic
     */
    CountStatistic getActivationErrorCount();

    /**
     * Returns the total number of sessions passivated using this store
     * @return CountStatistic
     */
    CountStatistic getPassivationCount();

    /**
     * Returns the total number of sessions passivated successfully
     * using this store
     * @return CountStatistic
     */
    CountStatistic getPassivationSuccessCount();

    /**
     * Returns the total number of sessions that could not be passivated
     * using the store
     * @return CountStatistic
     */
    CountStatistic getPassivationErrorCount();

    /**
     * Returns the total number of expired sessions that were removed by
     * the store
     * @return CountStatistic
     */
    CountStatistic getExpiredSessionCount();

    /**
     * Returns the total number of bytes passivated by this store
     * @return AverageRangeStatistic
     */
    AverageRangeStatistic getPassivatedBeanSize();

    /**
     * Returns the time spent on passivating beans to the store
     * @return AverageRangeStatistic
     */
    AverageRangeStatistic getPassivationTime();

    /**
     * Returns the total number of bytes activated by this store
     * @return AverageRangeStatistic
     */
    AverageRangeStatistic getActivatedBeanSize();

    /**
     * Returns the time spent on activating beans from the store
     * @return AverageRangeStatistic
     */
    AverageRangeStatistic getActivationTime();

}
