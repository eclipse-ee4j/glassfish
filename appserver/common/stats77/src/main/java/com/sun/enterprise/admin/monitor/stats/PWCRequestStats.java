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
 * Interface representing statistical information about the request bucket
 */
public interface PWCRequestStats extends Stats {

    /**
     * Gets the method of the last request serviced.
     *
     * @return Method of the last request serviced
     */
    StringStatistic getMethod();

    /**
     * Gets the URI of the last request serviced.
     *
     * @return URI of the last request serviced
     */
    StringStatistic getUri();

    /**
     * Gets the number of requests serviced.
     *
     * @return Number of requests serviced
     */
    CountStatistic getCountRequests();

    /**
     * Gets the number of bytes received.
     *
     * @return Number of bytes received, or 0 if this information is
     * not available
     */
    CountStatistic getCountBytesReceived();

    /**
     * Gets the number of bytes transmitted.
     *
     * @return Number of bytes transmitted, or 0 if this information
     * is not available
     */
    CountStatistic getCountBytesTransmitted();

    /**
     * Gets the rate (in bytes per second) at which data was transmitted
     * over some server-defined interval.
     *
     * @return Rate (in bytes per second) at which data was
     * transmitted over some server-defined interval, or 0 if this
     * information is not available
     */
    CountStatistic getRateBytesTransmitted();

    /**
     * Gets the maximum rate at which data was transmitted over some
     * server-defined interval.
     *
     * @return Maximum rate at which data was transmitted over some
     * server-defined interval, or 0 if this information is not available.
     */
    CountStatistic getMaxByteTransmissionRate();

    /**
     * Gets the number of open connections.
     *
     * @return Number of open connections, or 0 if this information
     * is not available
     */
    CountStatistic getCountOpenConnections();

    /**
     * Gets the maximum number of open connections.
     *
     * @return Maximum number of open connections, or 0 if this
     * information is not available
     */
    CountStatistic getMaxOpenConnections();

    /**
     * Gets the number of 200-level responses sent.
     *
     * @return Number of 200-level responses sent
     */
    CountStatistic getCount2xx();

    /**
     * Gets the number of 300-level responses sent.
     *
     * @return Number of 300-level responses sent
     */
    CountStatistic getCount3xx();

    /**
     * Gets the number of 400-level responses sent.
     *
     * @return Number of 400-level responses sent
     */
    CountStatistic getCount4xx();

    /**
     * Gets the number of 500-level responses sent.
     *
     * @return Number of 500-level responses sent
     */
    CountStatistic getCount5xx();

    /**
     * Gets the number of responses sent that were not 200, 300, 400,
     * or 500 level.
     *
     * @return Number of responses sent that were not 200, 300, 400,
     * or 500 level
     */
    CountStatistic getCountOther();

    /**
     * Gets the number of responses with a 200 response code.
     *
     * @return Number of responses with a 200 response code
     */
    CountStatistic getCount200();

    /**
     * Gets the number of responses with a 302 response code.
     *
     * @return Number of responses with a 302 response code
     */
    CountStatistic getCount302();

    /**
     * Gets the number of responses with a 304 response code.
     *
     * @return Number of responses with a 304 response code
     */
    CountStatistic getCount304();

    /**
     * Gets the number of responses with a 400 response code.
     *
     * @return Number of responses with a 400 response code
     */
    CountStatistic getCount400();

    /**
     * Gets the number of responses with a 401 response code.
     *
     * @return Number of responses with a 401 response code
     */
    CountStatistic getCount401();

    /**
     * Gets the number of responses with a 403 response code.
     *
     * @return Number of responses with a 403 response code
     */
    CountStatistic getCount403();

    /**
     * Gets the number of responses with a 404 response code.
     *
     * @return Number of responses with a 404 response code
     */
    CountStatistic getCount404();

    /**
     * Gets the number of responses with a 503 response code.
     *
     * @return Number of responses with a 503 response code
     */
    CountStatistic getCount503();

}
