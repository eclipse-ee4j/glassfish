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
 * A Stats interface to represent the statistical data exposed by an
 * HTTP Listener. This include data about the GlobalRequestProcessor
 * and the ThreadPool.
 * The GlobalRequestProcessor collects data about request processing
 * from each of the RequestProcessor threads.
 *
 * @author Murali Vempaty
 * @since S1AS8.0
 * @version 1.0
 */
public interface HTTPListenerStats extends Stats {

    // GlobalRequestProcessor statistics for the listener
    // TODO: Consolidate the statistics into Boundary or BoundedRange
    // statistics, as necessitated. For now, will leave everything
    // as a CountStatistic

    /**
     * Cumulative value of the bytesReceived by each of the
     * RequestProcessors
     * @return CountStatistic
     */
    CountStatistic getBytesReceived();

    /**
     * Cumulative value of the bytesSent by each of the
     * RequestProcessors
     * @return CountStatistic
     */
    CountStatistic getBytesSent();

    /**
     * Cumulative value of the errorCount of each of the
     * RequestProcessors. The errorCount represents the number of
     * cases where the response code was >= 400
     * @return CountStatistic
     */
    CountStatistic getErrorCount();

    /**
     * The longest response time for a request. This is not a
     * cumulative value, but is the maximum of the response times
     * for each of the RequestProcessors.
     * @return CountStatistic
     */
    CountStatistic getMaxTime();

    /**
     * Cumulative value of the processing times of each of the
     * RequestProcessors. The processing time of a RequestProcessor
     * is the average of request processing times over the request
     * count.
     * @return CountStatistic
     */
    CountStatistic getProcessingTime();

    /**
     * Cumulative number of the requests processed so far,
     * by the RequestProcessors.
     * @return CountStatistic
     */
    CountStatistic getRequestCount();


    /**
     * Returns the number of responses with a status code in the 2xx range
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code in the 2xx range
     */
    CountStatistic getCount2xx();


    /**
     * Returns the number of responses with a status code in the 3xx range
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code in the 3xx range
     */
    CountStatistic getCount3xx();


    /**
     * Returns the number of responses with a status code in the 4xx range
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code in the 4xx range
     */
    CountStatistic getCount4xx();


    /**
     * Returns the number of responses with a status code in the 5xx range
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code in the 5xx range
     */
    CountStatistic getCount5xx();


    /**
     * Returns the number of responses with a status code outside the 2xx,
     * 3xx, 4xx, and 5xx range, sent by the HTTP listener whose statistics
     * are exposed by this <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code outside the 2xx, 3xx,
     * 4xx, and 5xx range
     */
    CountStatistic getCountOther();


    /**
     * Returns the number of responses with a status code equal to 200
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 200
     */
    CountStatistic getCount200();


    /**
     * Returns the number of responses with a status code equal to 302
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 302
     */
    CountStatistic getCount302();


    /**
     * Returns the number of responses with a status code equal to 304
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 304
     */
    CountStatistic getCount304();


    /**
     * Returns the number of responses with a status code equal to 400
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 400
     */
    CountStatistic getCount400();


    /**
     * Returns the number of responses with a status code equal to 401
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 401
     */
    CountStatistic getCount401();


    /**
     * Returns the number of responses with a status code equal to 403
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 403
     */
    CountStatistic getCount403();


    /**
     * Returns the number of responses with a status code equal to 404
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 404
     */
    CountStatistic getCount404();


    /**
     * Returns the number of responses with a status code equal to 503
     * sent by the HTTP listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Number of responses with a status code equal to 503
     */
    CountStatistic getCount503();


    /**
     * Returns the number of open connections managed by the HTTP listener
     * whose statistics are exposed by this <code>HTTPListenerStats</code>.
     *
     * @return Number of open connections
     */
    CountStatistic getCountOpenConnections();


    /**
     * Returns the maximum number of open connections managed by the HTTP
     * listener whose statistics are exposed by this
     * <code>HTTPListenerStats</code>.
     *
     * @return Maximum number of open connections
     */
    CountStatistic getMaxOpenConnections();


    // ThreadPool statistics for the listener

    /**
     * The number of request processing threads currently in the
     * thread pool
     * @return CountStatistic
     */
    CountStatistic getCurrentThreadCount();

    /**
     * The number of request processing threads currently in the
     * thread pool, serving requests.
     * @return CountStatistic
     */
    CountStatistic getCurrentThreadsBusy();

    /**
     * The maximum number of request processing threads that are
     * created by the listener. It determines the maximum number of
     * simultaneous requests that can be handled
     * @return CountStatistic
     */
    CountStatistic getMaxThreads();

    /**
     * The maximum number of unused request processing threads that will
     * be allowed to exist until the thread pool starts stopping the
     * unnecessary threads.
     * @return CountStatistic
     */
    CountStatistic getMaxSpareThreads();

    /**
     * The number of request processing threads that will be created
     * when this listener is first started.
     * @return CountStatistic
     */
    CountStatistic getMinSpareThreads();

}
