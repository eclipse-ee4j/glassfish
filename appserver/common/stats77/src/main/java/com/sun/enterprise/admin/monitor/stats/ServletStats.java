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
 * Defines additional Sun ONE Application Server specific statistics
 * ServletStats interface.
 * The ServletStats interface that is defined by JSR77, cannot be used
 * here, as it is not possible to encapsulate the data pertaining to
 * the service method in a TimeStatistic. Therefore it becomes necessary
 * to define our own interface for exposing Servlet Statistics.
 *
 * @since S1AS8.0
 */
public interface ServletStats extends Stats {

    /**
     * Number of requests processed by this servlet.
     * @return CountStatistic
     */
    CountStatistic getRequestCount();

    /**
     * Cumulative Value, indicating the time taken to process the
     * requests received so far.
     * @return CountStatistic
     */
    CountStatistic getProcessingTime();

    /**
     * Gets the execution time of the servlet's service method.
     *
     * This method is identical in functionality to getProcessingTime(),
     * except that it exposes the execution time of the servlet's service
     * method under the JSR 77 compliant property name and type.
     *
     * @return Execution time of the servlet's service method
     */
    TimeStatistic getServiceTime();

    /**
     * The maximum processing time of a servlet request
     * @return CountStatistic
     */
    CountStatistic getMaxTime();

    /**
     * The errorCount represents the number of cases where the response
     * code was >= 400
     * @return CountStatistic
     */
    CountStatistic getErrorCount();

}
