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

package org.glassfish.sse.videoplayer;

import org.glassfish.sse.api.*;

import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import jakarta.inject.Inject;
import java.io.IOException;

/**
 * @author Jitendra Kotamraju
 */
@WebServlet("/remotecontrol/*")
public class PlayerServlet extends HttpServlet {
    @Inject
    PlayingStatus broadcastStatus;

    @Inject @ServerSentEventContext("/notifications")
    ServerSentEventHandlerContext<NotificationsHandler> ctxt;

    public void doGet(HttpServletRequest req, HttpServletResponse res) {
        System.out.println("Got Servlet Request Path="+req.getPathInfo());
        String path = req.getPathInfo();
        if (path != null) {
            if (path.equals("/play")) {
                broadcastStatus.setStatus("play");
                sendEvent("play");
                res.setStatus(200);
                return;
            } else if (path.equals("/pause")) {
                broadcastStatus.setStatus("pause");
                sendEvent("pause");
                res.setStatus(200);
                return;
            }
        }
        res.setStatus(500);
    }

    private void sendEvent(String event) {
        String command = "{\"type\":\""+event+"\"}";
        for(NotificationsHandler handler : ctxt.getHandlers()) {
            handler.sendMessage(command);
        }
    }

}
