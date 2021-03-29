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

package com.sun.enterprise.admin.util.proxy;

/**
 * Represents state of a call.
 */
public class CallState {
    public static final CallState IN_PROCESS = new CallState("Processing");
    public static final CallState FAILED = new CallState("Failed");
    public static final CallState SUCCESS = new CallState("Success");

    private String callState;

    /** Create a new instance of CallState */
    private CallState() {
    }

    private CallState(String state) {
        callState = state;
    }

    public boolean isFinished() {
        return (this != IN_PROCESS);
    }

    public boolean isSuccess() {
        return (this == SUCCESS);
    }

    public boolean isFailed() {
        return (this == FAILED);
    }

    public String toString() {
        return callState;
    }
}
