/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin.progress;

import org.glassfish.api.admin.ProgressStatus;

/**
 * Progress method was called.
 *
 * @author martinmares
 */
public class ProgressStatusEventProgress extends ProgressStatusEvent implements ProgressStatusMessage {

    private int steps;
    private String message;
    private boolean spinner;

    public ProgressStatusEventProgress(String progressStatusId, int steps, String message, boolean spinner) {
        super(progressStatusId);
        this.steps = steps;
        this.message = message;
        this.spinner = spinner;
    }

    public ProgressStatusEventProgress(String sourceId) {
        super(sourceId);
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSpinner(boolean spinner) {
        this.spinner = spinner;
    }

    public int getSteps() {
        return steps;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public boolean isSpinner() {
        return spinner;
    }

    @Override
    public ProgressStatus apply(ProgressStatus ps) {
        ps.progress(steps, message, spinner);
        return ps;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.steps;
        hash = 97 * hash + (this.message != null ? this.message.hashCode() : 0);
        hash = 97 * hash + (this.spinner ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProgressStatusEventProgress other = (ProgressStatusEventProgress) obj;
        if (this.steps != other.steps) {
            return false;
        }
        if (this.message == null ? other.message != null : !this.message.equals(other.message)) {
            return false;
        }
        if (this.spinner != other.spinner) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + getSourceId() + ", message=" + getMessage() + ']';
    }
}
