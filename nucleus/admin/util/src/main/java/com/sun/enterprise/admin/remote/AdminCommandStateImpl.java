/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.remote;

import java.io.Serializable;
import java.util.Objects;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandState;

/**
 * Basic implementation. Good for unmarshaling.
 *
 * @author mmares
 */
public class AdminCommandStateImpl implements AdminCommandState, Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final boolean payloadIsEmpty;
    private ActionReport actionReport;
    private volatile State state;

    public AdminCommandStateImpl(String id) {
        this(State.PREPARED, null, true, id);
    }

    public AdminCommandStateImpl(final State state, final ActionReport actionReport, final boolean payloadIsEmpty, final String id) {
        this.id = id;
        this.payloadIsEmpty = payloadIsEmpty;
        this.actionReport = actionReport;
        this.state = state;
    }

    @Override
    public final String getId() {
        return this.id;
    }

    @Override
    public boolean isOutboundPayloadEmpty() {
        return this.payloadIsEmpty;
    }

    @Override
    public final ActionReport getActionReport() {
        return this.actionReport;
    }

    @Override
    public final State getState() {
        return this.state;
    }

    /**
     * Sets the state and the action report.
     *
     * @param state must not be null.
     * @param report must not be null.
     */
    protected final void setState(State state, ActionReport report) {
        // The order matters - setStatus is overridden by some children,
        // following actions depend on the report!
        this.actionReport = Objects.requireNonNull(report, "report");
        setState(state);
    }

    /**
     * Sets the state.
     *
     * @param state must not be null
     */
    protected void setState(State state) {
        this.state = Objects.requireNonNull(state, "state");
    }

    @Override
    public String toString() {
        return super.toString() + "[id=" + id + ", state=" + state + ", report=" + actionReport + "]";
    }
}
