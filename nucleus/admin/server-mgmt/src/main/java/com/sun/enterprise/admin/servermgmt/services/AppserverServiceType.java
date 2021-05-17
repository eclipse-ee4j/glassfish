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

package com.sun.enterprise.admin.servermgmt.services;

/**
 * A type that defines the application server entity that can have service support. Currently, only a
 * <code> Domain </code> and <code> Instance </code> can have such support.
 *
 * @since 9.1
 * @author Kedar Mhaswade
 * @author Byron Nevins
 *
 */
public enum AppserverServiceType {
    Domain("start-domain", "restart-domain", "stop-domain"),
    Instance("start-local-instance", "restart-local-instance", "stop-local-instance");

    public String startCommand() {
        return start;
    }

    public String restartCommand() {
        return restart;
    }

    public String stopCommand() {
        return stop;
    }

    private AppserverServiceType(String start, String restart, String stop) {
        this.start = start;
        this.restart = restart;
        this.stop = stop;
    }

    private final String start;
    private final String restart;
    private final String stop;
}
