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

import jakarta.inject.Inject;
import java.io.IOException;

/**
 * @author Jitendra Kotamraju
 */
@ServerSentEvent("/notifications")
public class NotificationsHandler extends ServerSentEventHandler {
    @Inject
    PlayingStatus playingStatus;

    @Override
    public void onConnected(ServerSentEventConnection client) {
        super.onConnected(client);
        String command = "{\"type\":\""+ playingStatus.getStatus()+"\"}";
        System.out.println("Connected a new client="+client+" Sending initial command="+command);
        sendMessage(command);
    }

    public void sendMessage(String data) {
        System.out.println("Handler="+this+" Sending data="+data);
        try {
            connection.sendMessage(data);
        } catch(IOException ioe) {
            // May be client is already disconnected. Just close it
            ioe.printStackTrace();
            connection.close();
        }
    }

}
