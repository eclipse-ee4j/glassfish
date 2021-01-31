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

package com.sun.appserv.server;

/**
 * lifecycle modules implement <code>com.sun.appserv.server.LifecycleListener</code> interface. There is just one method
 * in this interface: <code>handleEvent()</code> which posts server lifecycle events to the lifecycle modules.
 * <p>
 * Upon start up, before initializing its subsystems application server posts lifcycle modules the
 * <code>INIT_EVENT</code>. This is followed by server posting the <code>STARTUP_EVENT</code> to the lifecycle modules
 * upon which server starts loading and initializaing the applications. Once this phase is completed, the
 * <code>READY_EVENT</code> is posted to the lifecycle modules.
 * <p>
 * When the server is shutdown, server posts the <code>SHUTDOWN_EVENT</code> to the lifecycle modules and then shuts
 * down the applications and subsystems. Once this phase is completed the <code>TERMINATION_EVENT</code> is posted.
 * <p>
 * Note that lifecycle modules may obtain the event specific data by calling <code>getData()</code> on the event
 * parameter in the <code>handleEvent()</code>. For the INIT_EVENT event, <code>getData()</code> returns the lifecycle
 * module's properties configured in server.xml.
 * <p>
 * When <code>is-failure-fatal</code> in server.xml is set to <code>true</code>, all exceptions from the lifecycle
 * modules are treated as fatal conditions.
 */
public interface LifecycleListener {

    /**
     * receive a server lifecycle event
     *
     * @param event associated event
     * @throws <code> ServerLifecycleException </code> for exception condition.
     *
     */
    void handleEvent(LifecycleEvent event) throws ServerLifecycleException;
}
