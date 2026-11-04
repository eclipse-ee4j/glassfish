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

package com.sun.webui.jsf.el;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;
import jakarta.faces.component.StateHolder;
import jakarta.faces.context.FacesContext;

/**
 * Drop down method expression.
 */
public final class DropDownMethodExpression extends MethodExpression
        implements StateHolder {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 3229154216397152494L;

    /**
     * The value.
     */
    private transient String value = null;

    /**
     * Transient flat.
     */
    private boolean transientFlag = false;

    /**
     * Creates a new instance.
     */
    public DropDownMethodExpression() {
    }

    /**
     * Set value.
     * @param newValue new value
     */
    public void setValue(final String newValue) {
        this.value = newValue;
    }

    @Override
    public MethodInfo getMethodInfo(final ELContext context) {
        return null;
    }

    @Override
    public Object invoke(final ELContext context, final Object[] params) {
        return value;
    }

    @Override
    public String getExpressionString() {
        return value;
    }

    @Override
    public boolean isLiteralText() {
        return true;
    }

    @Override
    public boolean equals(final Object obj) {
        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean isTransient() {
        return this.transientFlag;
    }

    @Override
    public void setTransient(final boolean newTransientFlag) {
        this.transientFlag = newTransientFlag;
    }

    @Override
    public void restoreState(final FacesContext context, final Object state) {
        this.value = (String) state;
    }

    @Override
    public Object saveState(final FacesContext context) {
        return this.value;
    }
}
