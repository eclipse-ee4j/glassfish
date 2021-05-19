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

/**
 * Provides instance information relavant to Load balancer tier.
 *
 * @author Satish Viswanatham
 */
public interface InstanceReader extends BaseReader {

    /**
     * Return server instance's name.
     *
     * @return String           instance' name
     */
    public String getName() throws LbReaderException;

    /**
     * Returns if the server is enabled in the load balancer or not.
     *
     * @return boolean          true if enabled in LB; false if disabled
     */
    public boolean getLbEnabled() throws LbReaderException;

    /**
     * This is used in quicescing. Timeouts after this interval and disables the
     * instance in the load balancer.
     *
     * @return String           Disable time out in minutes
     */
    public String getDisableTimeoutInMinutes() throws LbReaderException;

    /**
     * Enlists both http and https listeners of this server instance
     * It will be form "http:<hostname>:<port> https:<hostname>:<port>"
     *
     * @return String   Listener(s) info.
     */
    public String getListeners() throws LbReaderException;

    /**
     * For weighted round robin gets the weight.
     * Default value is 100
     *
     * @return String   Weight
     */
    public String getWeight() throws LbReaderException;
}
