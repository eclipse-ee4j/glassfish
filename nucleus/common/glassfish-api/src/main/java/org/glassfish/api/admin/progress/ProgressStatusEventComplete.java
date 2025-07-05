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
public class ProgressStatusEventComplete extends ProgressStatusEvent implements ProgressStatusMessage {

    private String message;

    public ProgressStatusEventComplete(String progressStatusId, String message) {
        super(progressStatusId);
        this.message = message;
    }

    public ProgressStatusEventComplete(String sourceId) {
        super(sourceId);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ProgressStatus apply(ProgressStatus ps) {
        ps.complete(message);
        return ps;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.message != null ? this.message.hashCode() : 0);
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
        final ProgressStatusEventComplete other = (ProgressStatusEventComplete) obj;
        if (this.message == null ? other.message != null : !this.message.equals(other.message)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + getSourceId() + ", message=" + getMessage() + ']';
    }
}
