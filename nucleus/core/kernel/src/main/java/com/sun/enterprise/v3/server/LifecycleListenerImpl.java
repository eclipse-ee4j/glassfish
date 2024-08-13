/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.server;

import com.sun.appserv.server.LifecycleEvent;
import com.sun.appserv.server.LifecycleEventContext;
import com.sun.appserv.server.LifecycleListener;
import com.sun.appserv.server.ServerLifecycleException;

import java.util.Properties;

/**
 *  LifecycleListenerImpl is a dummy implementation for the LifecycleListener interface.
 *  This implementaion stubs out various lifecycle interface methods.
 */
public class LifecycleListenerImpl implements LifecycleListener {

    /** receive a server lifecycle event
     *  @param event associated event
     *  @throws <code>ServerLifecycleException</code> for exceptional condition.
     *
     *  Configure this module as a lifecycle-module in server.xml:
     *
     *  <applications>
     *    <lifecycle-module name="test"
     *               class-name="com.sun.appserv.server.LifecycleListenerImpl"
                     is-failure-fatal="false">
     *      <property name="foo" value="fooval"/>
     *    </lifecycle-module>
     *  </applications>
     *
     *  Set<code>is-failure-fatal</code>in server.xml to <code>true</code> for
     *  fatal conditions.
     */
    @Override
    public void handleEvent(LifecycleEvent event) throws ServerLifecycleException {
        LifecycleEventContext ctx = event.getLifecycleEventContext();

        ctx.log("got event" + event.getEventType() + " event data: " + event.getData());

        Properties props;

        if (LifecycleEvent.INIT_EVENT == event.getEventType()) {
            System.out.println("LifecycleListener: INIT_EVENT");

            props = (Properties) event.getData();

            // handle INIT_EVENT
            return;
        }

        if (LifecycleEvent.STARTUP_EVENT == event.getEventType()) {
            System.out.println("LifecycleListener: STARTUP_EVENT");

            // handle STARTUP_EVENT
            return;
        }

        if (LifecycleEvent.SHUTDOWN_EVENT== event.getEventType()) {
            System.out.println("LifecycleListener: SHUTDOWN_EVENT");

            // handle SHUTDOWN_EVENT
            return;
        }

        if (LifecycleEvent.TERMINATION_EVENT == event.getEventType()) {
            System.out.println("LifecycleListener: TERMINATE_EVENT");

            // handle TERMINATION_EVENT
            return;
        }
    }
}
