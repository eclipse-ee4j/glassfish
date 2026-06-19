/*
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.util;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * This {@link TypeConversion} makes an attempt to convert a String clientId to a UIComponent.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class UIComponentTypeConversion implements TypeConversion {

    /**
     *
     */
    @Override
    public Object convertValue(Object value) {
        if (value == null) {
            return null;
        }

        if (!(value instanceof UIComponent)) {
            String strVal = value.toString();
            if (strVal.trim().length() == 0) {
                value = null;
            } else {
                // Treat String as clientId
                FacesContext ctx = FacesContext.getCurrentInstance();
                if (!strVal.startsWith(":")) {
                    strVal = ":" + strVal;
                }
                value = ctx.getViewRoot().findComponent(strVal);
            }
        }

        return value;
    }
}
