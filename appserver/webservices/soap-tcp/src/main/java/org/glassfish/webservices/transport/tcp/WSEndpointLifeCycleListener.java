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

package org.glassfish.webservices.transport.tcp;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.webservices.monitoring.Endpoint;
import org.glassfish.webservices.monitoring.EndpointLifecycleListener;

/**
 * @author Alexey Stashok
 */
public final class WSEndpointLifeCycleListener implements EndpointLifecycleListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void endpointAdded(final Endpoint endpoint) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, LogUtils.SOAPTCP_ENDPOINT_ADDED, endpoint);
        }
        AppServRegistry.getInstance().registerEndpoint(endpoint);
    }

    @Override
    public void endpointRemoved(final Endpoint endpoint) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, LogUtils.SOAPTCP_ENDPOINT_REMOVED, endpoint);
        }
        AppServRegistry.getInstance().deregisterEndpoint(endpoint);
    }
}
