/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.glassfish.sse.api.ServerSentEventHandler;

/**
 * ServerSentEventServlet class.
 *
 * @author Jitendra Kotamraju
 */
@WebServlet(asyncSupported = true)
public final class ServerSentEventServlet extends HttpServlet {

    private static final long serialVersionUID = -2281462213280496518L;

    @Inject
    private transient ServerSentEventCdiExtension extension;

    @Inject
    private transient BeanManager beanManager;

    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // TODO CDI is not strictly required unless ServerSentEventHandlerContext
        // TODO needs to be injected
        if (extension == null) {
            throw new RuntimeException("Enable CDI by including empty WEB-INF/beans.xml");
        }

        Map<String, ServerSentEventApplication> applicationMap = extension.getApplicationMap();
        ServerSentEventApplication sseApp = applicationMap.get(request.getServletPath());
        Class<?> clazz = sseApp.getHandlerClass();
        ServerSentEventHandler serverSentEventHandler;
        CreationalContext creationalContext;

        // Check if SSE handler can be instantiated via CDI
        Iterator<Bean<?>> it = beanManager.getBeans(clazz).iterator();
        if (it.hasNext()) {
            Bean bean = it.next();
            creationalContext = beanManager.createCreationalContext(bean);
            serverSentEventHandler = (ServerSentEventHandler) bean.create(creationalContext);
        } else {
            throw new RuntimeException("Cannot create ServerSentEventHandler using CDI");
        }

        ServerSentEventHandler.Status status = serverSentEventHandler.onConnecting(request);
        if (status == ServerSentEventHandler.Status.DONT_RECONNECT) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            creationalContext.release();
            return;
        }

        if (status != ServerSentEventHandler.Status.OK) {
            throw new RuntimeException("Internal Error: need to handle status " + status);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/event-stream");
        response.flushBuffer(); // writes status code and headers

        AsyncContext asyncContext = request.startAsync(request, response);
        asyncContext.setTimeout(0); // no timeout. need config ?
        ServerSentEventConnectionImpl sseConnection = sseApp.createConnection(request, serverSentEventHandler, creationalContext,
                asyncContext);
        asyncContext.addListener(sseConnection);
        sseConnection.init();
    }

}
