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

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

import java.io.Serializable;

// Delete the setters once you have reimplemented this not to
// use the default Serializable mechanism, but the same as
// in the converter....
/**
 * Repeat unit converter.
 */
public final class RepeatUnitConverter implements Converter, Serializable {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 8474867258235790714L;

    @Override
    public String getAsString(final FacesContext context,
            final UIComponent component, final Object value) {

        if (value == null) {
            return null;
        }
        return ((RepeatUnit) value).getRepresentation();
    }

    @Override
    public Object getAsObject(final FacesContext context,
            final UIComponent component, final String value) {

        return RepeatUnit.getInstance(value);
    }
}
