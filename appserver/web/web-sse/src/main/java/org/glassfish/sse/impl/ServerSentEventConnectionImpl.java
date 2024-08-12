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
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.glassfish.sse.api.ServerSentEventConnection;
import org.glassfish.sse.api.ServerSentEventData;
import org.glassfish.sse.api.ServerSentEventHandler;

/**
 * ServerSentEventClientImpl class.
 *
 * @author Jitendra Kotamraju
 */
final class ServerSentEventConnectionImpl extends ServerSentEventConnection implements AsyncListener {
    final HttpServletRequest request;
    final ServerSentEventHandler sseh;
    final AsyncContext asyncContext;
    final CreationalContext<?> cc;
    private final ServerSentEventApplication owner;
    private boolean closed;

    ServerSentEventConnectionImpl(ServerSentEventApplication owner, HttpServletRequest request,
                ServerSentEventHandler sseh, CreationalContext<?> cc, AsyncContext asyncContext) {
        this.owner = owner;
        this.request = request;
        this.sseh = sseh;
        this.cc = cc;
        this.asyncContext = asyncContext;
    }

    void init() {
        // Call onConnected() callback on handler
        sseh.onConnected(this);
    }

    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    @Override
    public void sendMessage(String eventData) throws IOException {
        // Can avoid creating ServerSentEventData for performance(if required)
        sendMessage(new ServerSentEventData().data(eventData));
    }

    @Override
    public void sendMessage(ServerSentEventData eventData) throws IOException {
        if (closed) {
            throw new IllegalStateException("sendMessage cannot be called after the connection is closed.");
        }
        synchronized (sseh) {       // so that events don't interleave
            try {
                // Write message on response and flush
                HttpServletResponse res = (HttpServletResponse) asyncContext.getResponse();
                ServletOutputStream sos = res.getOutputStream();
                sos.write(eventData.toString().getBytes("UTF-8"));
                sos.write('\n');
                sos.flush();
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() {
        closed = true;
        destroy();
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        // no-op
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        // no-op
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        destroy();
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
        // no-op
    }

    private void destroy() {
        cc.release();
        owner.destroyConnection(this);
        asyncContext.complete();        // calls onComplete()
    }
}
