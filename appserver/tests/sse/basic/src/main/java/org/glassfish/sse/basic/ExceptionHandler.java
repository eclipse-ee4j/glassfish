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
 * Tests for IllegalStateException when sendMessage is called after
 * close method.
 *
 * @author Jitendra Kotamraju
 */
@ServerSentEvent("/exception")
public class ExceptionHandler extends ServerSentEventHandler {

    @Override
    public void onConnected(ServerSentEventConnection con) {
        super.onConnected(con);
        try {
            con.sendMessage("message1");
            con.close();
            try {
                con.sendMessage("message2");
            } catch(IllegalStateException expected) {
                System.out.println("Received expected IllegalStateException");
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
