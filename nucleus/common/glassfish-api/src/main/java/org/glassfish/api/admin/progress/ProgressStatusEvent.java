/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.api.admin.progress;

import org.glassfish.api.admin.ProgressEvent;
import org.glassfish.api.admin.ProgressStatus;

/**
 * {@code ProgressStatus} is changed
 *
 * @author mmares
 */
//TODO: Move to AdminUtil if possible. It is now in API only because ProgressStatusImpl is here, too
public abstract class ProgressStatusEvent implements ProgressEvent {

    private static final long serialVersionUID = 1L;
    private final String sourceId;

    public ProgressStatusEvent(String sourceId) {
        if (sourceId == null) {
            throw new IllegalArgumentException("id == null");
        }
        this.sourceId = sourceId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public abstract ProgressStatus apply(ProgressStatus ps);

}
