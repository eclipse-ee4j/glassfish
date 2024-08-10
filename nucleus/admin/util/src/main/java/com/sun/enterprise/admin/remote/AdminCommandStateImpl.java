/*
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

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandState;

/**
 * Basic implementation. Good for unmarshaling.
 *
 * @author mmares
 */
public class AdminCommandStateImpl implements AdminCommandState, Serializable {

    private static final long serialVersionUID = 1L;

    protected State state = State.PREPARED;
    protected ActionReport actionReport;
    private boolean payloadIsEmpty;
    protected String id;

    public AdminCommandStateImpl(final State state, final ActionReport actionReport, final boolean payloadIsEmpty, final String id) {
        this.state = state;
        this.actionReport = actionReport;
        this.payloadIsEmpty = payloadIsEmpty;
        this.id = id;
    }

    public AdminCommandStateImpl(String id) {
        this(State.PREPARED, null, true, id);
    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public void complete(ActionReport actionReport) {
        this.actionReport = actionReport;
        if (getState().equals(State.REVERTING)) {
            setState(State.REVERTED);
        } else {
            setState(State.COMPLETED);
        }
    }

    @Override
    public ActionReport getActionReport() {
        return this.actionReport;
    }

    @Override
    public boolean isOutboundPayloadEmpty() {
        return this.payloadIsEmpty;
    }

    @Override
    public String getId() {
        return this.id;
    }

    protected void setState(State state) {
        this.state = state;
    }

}
