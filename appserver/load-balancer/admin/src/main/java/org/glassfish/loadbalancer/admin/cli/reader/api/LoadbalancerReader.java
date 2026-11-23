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

package org.glassfish.loadbalancer.admin.cli.reader.api;

import org.glassfish.loadbalancer.config.LbConfig;

/**
 * Reader class to get information about load balancer configuration.
 *
 * @author Satish Viswanatham
 */
public interface LoadbalancerReader extends BaseReader {

    /**
     * Returns properties of the load balancer.
     * For example response-timeout-in-seconds, reload-poll-interval-in-seconds
     * and https-routing etc.
     *
     * @return PropertyReader[]     array of properties
     */
    PropertyReader[] getProperties() throws LbReaderException;

    /**
     * Returns the cluster info that are load balanced by this LB.
     *
     * @return ClusterReader        array of cluster readers
     */
    ClusterReader[] getClusters() throws LbReaderException;

    /**
     * Returns the name of the load balancer
     *
     * @return String               name of the LB
     */
    String getName() throws LbReaderException;

    /**
     * Returns the lbconfig associated with the load balancer
     *
     * @return LbConfig               lbconfig of the LB
     */
    LbConfig getLbConfig();

    /*** Supported Attribute names for Load balancer **/
    String RESP_TIMEOUT = "response-timeout-in-seconds";
    String RESP_TIMEOUT_VALUE = "60";
    String RELOAD_INTERVAL =
            "reload-poll-interval-in-seconds";
    String RELOAD_INTERVAL_VALUE = "60";
    String HTTPS_ROUTING = "https-routing";
    String HTTPS_ROUTING_VALUE = "false";
    String REQ_MONITOR_DATA = "require-monitor-data";
    String REQ_MONITOR_DATA_VALUE = "false";
    String ROUTE_COOKIE = "route-cookie-enabled";
    String LAST_EXPORTED = "last-exported";
    String ACTIVE_HEALTH_CHECK_VALUE = "false";
    String NUM_HEALTH_CHECK_VALUE = "3";
    String REWRITE_LOCATION_VALUE = "true";
    String ACTIVE_HEALTH_CHECK = "active-healthcheck-enabled";
    String NUM_HEALTH_CHECK = "number-healthcheck-retries";
    String REWRITE_LOCATION = "rewrite-location";
    String REWRITE_COOKIES = "rewrite-cookies";
    String REWRITE_COOKIES_VALUE = "false";
    String PREFERRED_FAILOVER_INSTANCE = "preferred-failover-instance";
    String PREFERRED_FAILOVER_INSTANCE_VALUE = "true";

    //server ref attributes default values
    boolean LBENABLED_VALUE = true;
    String DISABLE_TIMEOUT_IN_MINUTES_VALUE = "30";
}
