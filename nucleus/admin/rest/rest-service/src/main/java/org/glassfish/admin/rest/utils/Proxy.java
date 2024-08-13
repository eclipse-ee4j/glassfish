/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.utils;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.net.URL;
import java.util.Properties;

import org.glassfish.hk2.api.ServiceLocator;

/**
 * Implements
 *
 * @author Mitesh Meswani
 */
public interface Proxy {
    /**
     * Execute request in given <code> sourceUriInfo</code> by acting as a proxy to the target instance
     *
     * @return Result of execution as Properties object
     */
    Properties proxyRequest(UriInfo sourceUriInfo, Client client, ServiceLocator habitat);

    /**
     * Extract instance name from given <code>uriInfo</code>
     */
    String extractTargetInstanceName(UriInfo uriInfo);

    /**
     * construct URL that will be used to contact the target
     */
    UriBuilder constructForwardURLPath(UriInfo sourceUriInfo);

    /**
     * construct URL to be given back to the client by processing the response URL received from target
     */
    UriBuilder constructTargetURLPath(UriInfo sourceUriInfo, URL responseURLReceivedFromTarget);
}
