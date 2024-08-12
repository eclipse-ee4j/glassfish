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
import org.glassfish.j2ee.statistics.TimeStatistic;

/**
 * A Stats interface to represent the statistical data exposed by an EJB Business Method.
 * These are based on the statistics exposed in S1AS7.0.
 * All the EJB Methods should expose statistical data by implementing this interface.
 *
 * @author Muralidhar Vempaty
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */
public interface EJBMethodStats extends Stats {

    /**
     * Returns the statistics of the method invocation as an instance of TimeStatistic.
     * Note that it returns the number of times the operation called, the total time
     * that was spent during the invocation and so on. All the calculations of the
     * statistic are being done over time.
     *
     * @return in instance of {@link TimeStatistic}
     */
    TimeStatistic getMethodStatistic();


    /**
     * Returns the total number of errors as a CountStatistic. It is upto the method
     * implementor to characterize what an error is. Generally if an operation results in
     * an exception, this count will increment by one.
     *
     * @return an instance of {@link CountStatistic}
     */
    CountStatistic getTotalNumErrors();


    /**
     * Returns the total number of successful runs, as a CountStatistic. It is upto the method
     * implementor to characterize what a successful run is. Generally if an operation returns
     * normally, this count will increment by one.
     *
     * @return an instance of {@link CountStatistic}
     */
    CountStatistic getTotalNumSuccess();


    /**
     * Returns the time spent during the last successful/unsuccessful attempt to execute the
     * operation, as a CountStatistic.
     * The time spent is generally an indication of the system load/processing time.
     *
     * @return an instance of {@link CountStatistic}
     */
    CountStatistic getExecutionTime();
}
