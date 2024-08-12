/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents a Server-Sent Event connection for sending push notifications
 *
 * @author Jitendra Kotamraju
 */
public abstract class ServerSentEventConnection implements Closeable {

    /**
     * Servlet request for Server-Sent Event connection
     *
     * @return servlet request
     */
    public abstract HttpServletRequest getRequest();

    /**
     * Event source's last event ID.
     *
     * @return null if there is no HTTP header in the request otherwise, last event ID
     */
    public String getLastEventID() {
        return getRequest().getHeader("Last-Event-ID");
    }

    /**
     * Sends the Server-Sent event to client
     *
     * @param eventData Server-Sent event data
     * @throws IOException when there is an error in sending
     * @throws IllegalStateException when called after calling close method
     * @see ServerSentEventData
     */
    public abstract void sendMessage(String eventData) throws IOException;

    /**
     * Sends the Server-Sent event to client
     *
     * @param eventData Server-Sent event data
     * @throws IOException when there is an error in sending
     * @throws IllegalStateException when called after calling close method
     * @see ServerSentEventData
     */
    public abstract void sendMessage(ServerSentEventData eventData) throws IOException;

    /**
     * Closes the connection
     */
    public abstract void close();

}
