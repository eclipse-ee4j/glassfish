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

package com.sun.enterprise.connectors.jms.inflow;

/**
 * Represents the properties of MDBContainer.
 *
 * @author Binod P.G
 */
public final class MdbContainerProps {

    private static int cmtMaxRuntimeExceptions;
    private static int reconnectDelayInSeconds;
    private static int reconnectMaxRetries;
    private static boolean reconnectEnabled;

    /**
     * Sets the max runtime exception that are allowed.
     *
     * @param v value.
     */
    public synchronized  static void setMaxRuntimeExceptions(int v){
        cmtMaxRuntimeExceptions = v;
    }

    /**
     * Retrives the max runtime exception that are allowed.
     *
     * @return int value
     */
    public synchronized static int getMaxRuntimeExceptions() {
        return cmtMaxRuntimeExceptions;
    }

    /**
     * Sets the reconnect delay in seconds.
     *
     * @param v value.
     */
    public synchronized static void setReconnectDelay(int v) {
        reconnectDelayInSeconds = v;
    }

    /**
     * Retrieves the reconnect delay in seconds.
     *
     * @return int value
     */
    public synchronized static int getReconnectDelay() {
        return reconnectDelayInSeconds;
    }

    /**
     * Sets the reconnect max retries.
     *
     * @param v value.
     */
    public synchronized static void setReconnectMaxRetries(int v) {
        reconnectMaxRetries = v;
    }

    /**
     * Gets the reconnect max retries.
     *
     * @return int value
     */
    public synchronized static int getReconnectMaxRetries() {
        return reconnectMaxRetries;
    }

    /**
     * Sets reconnect enabled flag.
     *
     * @param v value.
     */
    public synchronized static void setReconnectEnabled(boolean v){
        reconnectEnabled = v;
    }

    /**
     * Gets reconnect enabled flag.
     *
     * @return boolean indicating whether reconnect is enabled or not.
     */
    public synchronized static boolean getReconnectEnabled() {
        return reconnectEnabled;
    }

}
