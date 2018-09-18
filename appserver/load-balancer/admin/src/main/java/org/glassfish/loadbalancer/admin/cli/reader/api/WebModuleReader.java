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
 * Provides web module information relavant to Load balancer tier.
 *
 * @author Satish Viswanatham
 */
public interface WebModuleReader extends BaseReader {

    /**
     * Returns config context of this module
     *
     * @return String           context root for this web module
     */
    public String getContextRoot() throws LbReaderException;

    /**
     * Returns error url for this web module
     *
     * @return  String          This url acts as error page for this module
     */
    public String getErrorUrl() throws LbReaderException;

    /**
     * Returns load balancer enabled flag.
     *
     * @return @boolean         true, if enabled; false, if disabled in LB
     */
    public boolean getLbEnabled() throws LbReaderException;

    /**
     * Returns disable timeout for this module
     *
     * @return @String          value must be > 0
     */
    public String getDisableTimeoutInMinutes() throws LbReaderException;

    /**
     * Returns idempotent url patterns for this module.
     *
     * @return  IdempotentUrlPatternReader[]    list of idempotent url patterns
     */
    public IdempotentUrlPatternReader[] getIdempotentUrlPattern()
            throws LbReaderException;
}
