/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/**
 * test server lifecycle event listener
 */
import java.util.Properties;
import java.io.InputStream;

import com.sun.appserv.server.LifecycleListener;
import com.sun.appserv.server.LifecycleEvent;
import com.sun.appserv.server.LifecycleEventContext;
import com.sun.appserv.server.ServerLifecycleException;

public class TestLifecycleModule implements LifecycleListener {

    // receive a server lifecycle event
    public void handleEvent(LifecycleEvent event) throws ServerLifecycleException {

         LifecycleEventContext ctx = event.getLifecycleEventContext();
        if (LifecycleEvent.INIT_EVENT == event.getEventType()) {
            System.out.println("*");
            System.out.println("*");
            ctx.log("TestLifecycleModule: INIT_EVENT PASSED");
            System.out.println("*");
            System.out.println("*");
            return;
        }

        if (LifecycleEvent.STARTUP_EVENT == event.getEventType()) {
            System.out.println("*");
            System.out.println("*");
            ctx.log("TestLifecycleModule: STARTUP_EVENT PASSED");
            System.out.println("*");
            System.out.println("*");
            return;
        }

        if (LifecycleEvent.READY_EVENT == event.getEventType()) {
            System.out.println("*");
            System.out.println("*");
            ctx.log("TestLifecycleModule: READY_EVENT PASSED");
            System.out.println("*");
            System.out.println("*");

            return;
        }
        if (LifecycleEvent.SHUTDOWN_EVENT== event.getEventType()) {
            System.out.println("*");
            System.out.println("*");
            ctx.log("TestLifecycleModule: SHUTDOWN_EVENT PASSED");
            System.out.println("*");
            System.out.println("*");

            return;
        }

        if (LifecycleEvent.TERMINATION_EVENT == event.getEventType()) {
            System.out.println("*");
            System.out.println("*");
            ctx.log("TestLifecycleModule: TERMINATION_EVENT PASSED");
            System.out.println("*");
            System.out.println("*");

            return;
        }
   }
}
