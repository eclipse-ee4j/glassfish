/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.grizzly.samples.websockets;

import java.util.logging.Level;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;

public class ChatApplication extends WebSocketApplication {
    @Override
    public boolean isApplicationRequest(HttpRequestPacket request) {
        return "/chat".equals(request.getRequestURI());
    }

    @Override
    public WebSocket createSocket(ProtocolHandler handler,
            HttpRequestPacket requestPacket,
            WebSocketListener... listeners) {
        final ChatWebSocket ws
                = new ChatWebSocket(handler, requestPacket, listeners);
        return ws;
    }

    @Override
    public void onMessage(WebSocket socket, String data) {
        if (data.startsWith("login:")) {
            login((ChatWebSocket) socket, data);
        } else {
            broadcast(((ChatWebSocket) socket).getUser(), data);
        }
    }

    @Override
    public void onClose(WebSocket websocket, DataFrame frame) {
        broadcast("system", ((ChatWebSocket)websocket).getUser() + " left the chat");
    }

    /**
     * Broadcasts the text message from the user.
     *
     * @param user the user name
     * @param text the text message
     */
    private void broadcast(String user, String text) {
        WebSocketsServlet.logger.log(Level.INFO, "Broadcasting: {0} from: {1}", new Object[]{text, user});
        for (WebSocket websocket : getWebSockets()) {
            final ChatWebSocket chat = (ChatWebSocket) websocket;
            if (chat.getUser() != null) {  // it may happen some websocket is on the list, but not logged in to the chat
                chat.sendJson(user, text);
            }
        }

    }

    private void login(ChatWebSocket socket, String data) {
        if (socket.getUser() == null) {
            WebSocketsServlet.logger.info("ChatApplication.login");
            socket.setUser(data.split(":")[1].trim());
            broadcast(socket.getUser(), " has joined the chat.");
        }
    }
}
