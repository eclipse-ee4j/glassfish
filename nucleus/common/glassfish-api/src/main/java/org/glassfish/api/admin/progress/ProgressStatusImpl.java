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

package org.glassfish.api.admin.progress;

import java.io.Serializable;

/**
 * {@code ProgressStatus} implementation suggested for {@code AdminCommand} implementation.
 *
 * @author mmares
 */
//TODO: Move to admin-utils if possible. It is now in API only because ProgressStatusImpl is here, too
public class ProgressStatusImpl extends ProgressStatusBase implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * Constructor for instancing dummy (without propagation) instance.
     */
    public ProgressStatusImpl() {
        this(null, -1, null, "no-id");
    }

    /**
     * Construct unnamed {@code ProgressStatusImpl}
     *
     * @param parent Parent {@code ProgressStatusBase}
     * @param id Is useful for event transfer
     */
    protected ProgressStatusImpl(ProgressStatusBase parent, String id) {
        super(null, -1, parent, id);
    }

    /**
     * Construct named {@code ProgressStatusImpl}.
     *
     * @param name of the {@code ProgressStatus} implementation is used to identify source of progress messages.
     * @param parent Parent {@code ProgressStatusBase}
     * @param id Is useful for event transfer
     */
    protected ProgressStatusImpl(String name, ProgressStatusBase parent, String id) {
        super(name, -1, parent, id);
    }

    /**
     * Construct named {@code ProgressStatusImpl} with defined expected count of steps.
     *
     * @param name of the {@code ProgressStatus} implementation is used to identify source of progress messages.
     * @param totalStepCount How many steps are expected in this {@code ProgressStatus}
     * @param parent Parent {@code ProgressStatusBase}
     * @param id Is useful for event transfer
     */
    protected ProgressStatusImpl(String name, int totalStepCount, ProgressStatusBase parent, String id) {
        super(name, totalStepCount, parent, id);
    }

    @Override
    protected ProgressStatusBase doCreateChild(String name, int totalStepCount) {
        String childId = (id == null ? "" : id) + "." + (children.size() + 1);
        return new ProgressStatusImpl(name, totalStepCount, this, childId);
    }

}
