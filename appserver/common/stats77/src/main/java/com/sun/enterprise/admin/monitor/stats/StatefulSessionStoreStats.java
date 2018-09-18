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
import org.glassfish.j2ee.statistics.Stats;
import org.glassfish.j2ee.statistics.RangeStatistic;
import org.glassfish.j2ee.statistics.CountStatistic;
import com.sun.enterprise.admin.monitor.stats.AverageRangeStatistic;

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
    public RangeStatistic getCurrentSize();
    
    /**
     * Returns the total number of sessions activated from the store
     * @return CountStatistic
     */
    public CountStatistic getActivationCount();
    
    /**
     * Returns the total number of sessions successfully activated
     * from the store
     * @return CountStatistic
     */
    public CountStatistic getActivationSuccessCount();
    
    /**
     * Returns the total number of sessions that could not be activated
     * from the store
     * @return CountStatistic
     */
    public CountStatistic getActivationErrorCount();
    
    /**
     * Returns the total number of sessions passivated using this store
     * @return CountStatistic
     */
    public CountStatistic getPassivationCount();
    
    /**
     * Returns the total number of sessions passivated successfully
     * using this store
     * @return CountStatistic
     */
    public CountStatistic getPassivationSuccessCount();
    
    /**
     * Returns the total number of sessions that could not be passivated
     * using the store
     * @return CountStatistic
     */
    public CountStatistic getPassivationErrorCount();
    
    /**
     * Returns the total number of expired sessions that were removed by 
     * the store
     * @return CountStatistic
     */
    public CountStatistic getExpiredSessionCount();
    
    /**
     * Returns the total number of bytes passivated by this store
     * @return AverageRangeStatistic
     */
    public AverageRangeStatistic getPassivatedBeanSize();
    
    /**
     * Returns the time spent on passivating beans to the store
     * @return AverageRangeStatistic
     */
    public AverageRangeStatistic getPassivationTime();
    
    /**
     * Returns the total number of bytes activated by this store
     * @return AverageRangeStatistic
     */
    public AverageRangeStatistic getActivatedBeanSize();
    
    /**
     * Returns the time spent on activating beans from the store
     * @return AverageRangeStatistic
     */
    public AverageRangeStatistic getActivationTime();
    
}
