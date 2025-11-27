/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.servermgmt.cli;

import com.sun.enterprise.util.HostAndPort;

import java.util.List;

public class ServerLifeSignCheck {

    private final String serverTitleAndName;
    private final Boolean printServerOutput;

    private final boolean pidFile;
    private final boolean processAlive;
    private final boolean adminEndpoint;
    private final List<HostAndPort> customEndpoints;



    public ServerLifeSignCheck(String serverTitleAndName, Boolean printServerOutput, boolean pidFile,
        boolean processAlive, boolean adminEndpoint, List<HostAndPort> customEndpoints) {
        this.serverTitleAndName = serverTitleAndName;
        this.printServerOutput = printServerOutput;
        this.pidFile = pidFile;
        this.processAlive = processAlive;
        this.adminEndpoint = adminEndpoint;
        this.customEndpoints = customEndpoints;
    }


    public String getServerTitleAndName() {
        return serverTitleAndName;
    }


    /**
     * <ul>
     * <li>null - print on error
     * <li>true - print always
     * <li>false - print never
     * </ul>
     *
     * @return null/true/false
     */
    public Boolean getPrintServerOutput() {
        return printServerOutput;
    }


    /**
     * Usage: Rare, when we don't have permissions to check if the process is alive, or user uses own set of checks.
     * @return true to check the pid file existence.
     */
    public boolean isPidFile() {
        return pidFile;
    }


    /**
     * Usage: When we don't need to wait until server is listening on ports.
     * @return true to check the {@link Process#isAlive()}.
     */
    public boolean isProcessAlive() {
        return processAlive;
    }


    /**
     * Usage: When we need just admin endpoint to communicate with.
     * @return true to check at least one admin endpoint loaded from domain.xml.
     */
    public boolean isAdminEndpoint() {
        return adminEndpoint;
    }


    /**
     * Usage: When we use strict control over incoming requests, ie. requests
     * @return true to check custom endpoints provided by the user.
     */
    public boolean isCustomEndpoints() {
        return !customEndpoints.isEmpty();
    }


    /**
     * Usage: When we use strict control over incoming requests, ie. requests
     * @return custom endpoints provided by the user.
     */
    public List<HostAndPort> getCustomEndpoints() {
        return customEndpoints;
    }
}
