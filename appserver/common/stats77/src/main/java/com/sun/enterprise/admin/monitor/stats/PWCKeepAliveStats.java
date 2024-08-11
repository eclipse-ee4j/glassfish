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
 * Provides statistical information about the HttpService HTTP-level keep-alive system
 *
 * @author nsegura
 */
public interface PWCKeepAliveStats extends Stats {

    /**
     * Number of connections in keep-alive mode
     * @return number of connections
     */
    CountStatistic getCountConnections();

    /**
     * Maximum number of connections allowed in keep-alive mode simultaneously
     * @return Max number of connections allowed
     */
    CountStatistic getMaxConnections();

    /**
     * The number of times a request was successfully received from a
     * connection that had been kept alive
     * @return hits
     */
    CountStatistic getCountHits();

    /**
     * The number of times the server had to close a connection because the
     * KeepAliveCount exceeded the MaxKeepAliveConnections
     * @return connections
     */
    CountStatistic getCountFlushes();

    /**
     * The number of times the server could not hand off the connection to a
     * keep-alive thread, possibly due to too many persistent connections
     * @return refusals
     */
    CountStatistic getCountRefusals();

    /**
     * The number of times the server terminated keep-alive connections as the
     * client connections timed out, without any activity
     * @return connections timed out
     */
    CountStatistic getCountTimeouts();

    /**
     * The time (in seconds) before idle keep-alive connections are closed
     * @return time in seconds
     */
    CountStatistic getSecondsTimeouts();

}
