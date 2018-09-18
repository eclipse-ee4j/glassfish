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
 * Provides health checker information relavant to Load balancer tier.
 *
 * @author Satish Viswanatham
 */
public interface HealthCheckerReader extends BaseReader {

    /**
     * Return health checker url
     *
     * @return String           health checker url, it shoudld conform to
     *                          RFC 2396. java.net.URI.resolve(url) shoudl
     *                          return a valid URI.
     */
    public String getUrl() throws LbReaderException;

    /**
     * Health checker runs in the specified interval time.
     *
     * @return String           value must be > 0
     */
    public String getIntervalInSeconds() throws LbReaderException;

    /**
     *  Timeout where a server is considered un healthy.
     *
     * @return String           value must be > 0
     */
    public String getTimeoutInSeconds() throws LbReaderException;
}
