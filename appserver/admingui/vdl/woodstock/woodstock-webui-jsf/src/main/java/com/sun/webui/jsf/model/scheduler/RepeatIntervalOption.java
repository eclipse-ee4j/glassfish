/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.webui.jsf.model.scheduler;

import com.sun.webui.jsf.model.Option;

import jakarta.faces.context.FacesContext;

/**
 * Repeat interval option.
 */
public final class RepeatIntervalOption extends Option {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 5893100201596785304L;

    /**
     * Creates a new instance of RepeatMonthly.
     *
     * @param repeatInterval interval
     */
    public RepeatIntervalOption(final RepeatInterval repeatInterval) {
        this.setValue(repeatInterval);
    }

    @Override
    public String getLabel() {
        return ((RepeatInterval) getValue())
                .getLabel(FacesContext.getCurrentInstance());
    }
}
