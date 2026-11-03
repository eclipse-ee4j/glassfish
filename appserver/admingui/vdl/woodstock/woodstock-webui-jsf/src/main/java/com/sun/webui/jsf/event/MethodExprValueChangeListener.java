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
package com.sun.webui.jsf.event;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.MethodExpression;
import jakarta.faces.component.StateHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.event.ValueChangeListener;

/**
 * <strong>MethodExprValueChangeListener</strong> is a
 * {@link ValueChangeListener} that wraps a {@link MethodExpression}. When it
 * receives a {@link ValueChangeEvent}, it executes a method on an object
 * identified by the {@link MethodExpression}.
 */
public final class MethodExprValueChangeListener
        implements ValueChangeListener, StateHolder {

    /**
     * Method expression.
     */
    private MethodExpression methodExpression = null;

    /**
     * Transient flag.
     */
    private boolean isTransient;

    /**
     * Create a new instance.
     */
    public MethodExprValueChangeListener() {
    }

    /**
     * Construct a {@link ValueChangeListener} that contains a
     * {@link MethodExpression}.
     *
     * @param newMethodExpression method expression
     */
    public MethodExprValueChangeListener(
            final MethodExpression newMethodExpression) {

        super();
        this.methodExpression = newMethodExpression;

    }

    @Override
    public void processValueChange(final ValueChangeEvent valueChangeEvent)
            throws AbortProcessingException {

        if (valueChangeEvent == null) {
            throw new NullPointerException();
        }
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ELContext elContext = context.getELContext();
            methodExpression.invoke(elContext, new Object[]{valueChangeEvent});
        } catch (ELException ee) {
            throw new AbortProcessingException(ee.getMessage(), ee.getCause());
        }
    }

    @Override
    public Object saveState(final FacesContext context) {
        return new Object[]{methodExpression};
    }

    @Override
    public void restoreState(final FacesContext context, final Object state) {
        methodExpression = (MethodExpression) ((Object[]) state)[0];
    }

    @Override
    public boolean isTransient() {
        return isTransient;
    }

    @Override
    public void setTransient(final boolean newTransientValue) {
        isTransient = newTransientValue;
    }

    /**
     * Get the method expression.
     * @return MethodExpression
     */
    public MethodExpression getMethodExpression() {
        return methodExpression;
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (!(otherObject instanceof MethodExprValueChangeListener)) {
            return false;
        }

        MethodExprValueChangeListener other =
                (MethodExprValueChangeListener) otherObject;
        MethodExpression otherMe = other.getMethodExpression();
        //methodExpression should not be null
        return methodExpression.equals(otherMe);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash;
        if (this.methodExpression != null) {
            hash = hash + this.methodExpression.hashCode();
        }
        hash = 47 * hash;
        if (this.isTransient) {
            hash = hash + 1;
        }
        return hash;
    }
}
