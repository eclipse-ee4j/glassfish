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
 * Interface for querying web module statistics.
 */
public interface WebModuleStats extends Stats {

    /**
     * Gets the number of JSPs that have been loaded in the web module
     * associated with this WebModuleStats.
     *.
     * @return Number of JSPs that have been loaded
     */
    CountStatistic getJspCount();

    /**
     * Gets the number of JSPs that have been reloaded in the web module
     * associated with this WebModuleStats
     *.
     * @return Number of JSPs that have been reloaded
     */
    CountStatistic getJspReloadCount();

    /**
     * Gets the number of errors that were triggered by JSP invocations.
     *.
     * @return Number of errors triggered by JSP invocations
     */
    CountStatistic getJspErrorCount();

    /**
     * Gets the total number of sessions that have been created for the web
     * module associated with this WebModuleStats.
     *.
     * @return Total number of sessions created
     */
    CountStatistic getSessionsTotal();

    /**
     * Gets the number of currently active sessions for the web
     * module associated with this WebModuleStats.
     *.
     * @return Number of currently active sessions
     */
    CountStatistic getActiveSessionsCurrent();

    /**
     * Gets the maximum number of concurrently active sessions for the web
     * module associated with this WebModuleStats.
     *
     * @return Maximum number of concurrently active sessions
     */
    CountStatistic getActiveSessionsHigh();

    /**
     * Gets the total number of rejected sessions for the web
     * module associated with this WebModuleStats.
     *
     * <p>This is the number of sessions that were not created because the
     * maximum allowed number of sessions were active.
     *.
     * @return Total number of rejected sessions
     */
    CountStatistic getRejectedSessionsTotal();

    /**
     * Gets the total number of expired sessions for the web
     * module associated with this WebModuleStats.
     *.
     * @return Total number of expired sessions
     */
    CountStatistic getExpiredSessionsTotal();

    /**
     * Gets the cumulative processing times of all servlets in the web module
     * associated with this WebModuleStats.
     *
     * @return Cumulative processing times of all servlets in the web module
     * associated with this WebModuleStats
     */
    CountStatistic getServletProcessingTimes();

    /**
     * Returns comma-separated list of all sessions currently active in the web
     * module associated with this WebModuleStats.
     *
     * @return Comma-separated list of all sessions currently active in the
     * web module associated with this WebModuleStats
     */
    StringStatistic getSessions();

    /**
     * Resets this WebModuleStats.
     */
    void reset();

}
