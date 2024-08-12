/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.sse.impl;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.glassfish.sse.api.ServerSentEventHandler;

/**
 * ServerSentEventApplicationImpl class.
 *
 * @author Jitendra Kotamraju
 */
final class ServerSentEventApplication {

    private final Set<ServerSentEventHandler> handlers;
    private final String path;
    private final ServerSentEventHandlerContextImpl context;
    private final Class<?> clazz;

    ServerSentEventApplication(Class<?> clazz, String path) {
        this.clazz = clazz;
        this.path = path;
        handlers = new CopyOnWriteArraySet<ServerSentEventHandler>();
        context = new ServerSentEventHandlerContextImpl(path, handlers);
    }

    ServerSentEventConnectionImpl createConnection(HttpServletRequest request, ServerSentEventHandler sseh,
            CreationalContext<?> cc, AsyncContext ac) {
        ServerSentEventConnectionImpl con = new ServerSentEventConnectionImpl(this, request, sseh, cc, ac);
        handlers.add(sseh);
        return con;
    }

    void destroyConnection(ServerSentEventConnectionImpl connection) {
        handlers.remove(connection.sseh);
    }

    ServerSentEventHandlerContextImpl getHandlerContext() {
        return context;
    }

    Class<?> getHandlerClass() {
        return clazz;
    }

    String getPath() {
        return path;
    }

}
