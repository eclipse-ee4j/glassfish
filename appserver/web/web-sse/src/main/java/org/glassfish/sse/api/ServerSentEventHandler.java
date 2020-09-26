/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.sse.api;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * A handler that handles Server-Sent Events.
 *
 * @see ServerSentEvent
 * @author Jitendra Kotamraju
 */
public abstract class ServerSentEventHandler {
    protected ServerSentEventConnection connection;

    public enum Status { DONT_RECONNECT, OK }

    /**
     * A callback to indicate that a client connects to receive Server-Sent Events.
     * The application has full access to HTTP request and can decide whether
     * to accept the connection or reject it. In SSE, clients will reconnect
     * if the connection is closed, but can be told to stop reconnecting by
     * returning the appropriate status.
     *
     * <p>
     * Last-Event-ID may be used in determining the status and it can be
     * got using {@code HttpServletRequest.getHeader("Last-Event-ID")}
     *
     * @param request connection request
     * @return Status to accept, or don't reconnect etc
     */
    public Status onConnecting(HttpServletRequest request) {
        return Status.OK;
    }

    /**
     * A callback to indicate that a client connects to receive Server-Sent Events.
     * The application has full access to HTTP request and can decide whether
     * to accept the connection or reject it. In SSE, clients will reconnect
     * if the connection is closed, but can be told to stop reconnecting by
     * returning the appropriate status.
     *
     * @param connection Server-Sert event connection
     * @return Status to accept, or don't reconnect etc
     */
    public void onConnected(ServerSentEventConnection connection) {
        this.connection = connection;
    }

    /**
     * Callback to indicate that the client closed the connection
     */
    public void onClosed() {
    }

}
