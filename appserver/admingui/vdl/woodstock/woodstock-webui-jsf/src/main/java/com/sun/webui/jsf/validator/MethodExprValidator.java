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
package com.sun.webui.jsf.validator;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.MethodExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * <strong>MethodExprValidator</strong> is a {@link Validator} that wraps a
 * {@link MethodExpression}, and it performs validation by executing a method on
 * an object identified by the {@link MethodExpression}.
 */
public final class MethodExprValidator implements Validator, StateHolder {

    /**
     * Method expression.
     */
    private MethodExpression methodExpression = null;

    /**
     * Create a new instance.
     */
    public MethodExprValidator() {
        super();
    }

    /**
     * Transient flag.
     */
    private boolean transientValue = false;

    /**
     * Construct a {@link Validator} that contains a {@link MethodExpression}.
     *
     * @param newMethodExpression method expression
     */
    public MethodExprValidator(final MethodExpression newMethodExpression) {
        super();
        this.methodExpression = newMethodExpression;
    }

    @Override
    public void validate(final FacesContext context,
            final UIComponent component, final Object value)
            throws ValidatorException {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }
        if (value != null) {
            try {
                ELContext elContext = context.getELContext();
                methodExpression.invoke(elContext, new Object[]{
                    context,
                    component,
                    value
                });
            } catch (ELException ee) {
                Throwable e = ee.getCause();
                if (e instanceof ValidatorException) {
                    throw (ValidatorException) e;
                }
                FacesMessage message = new FacesMessage(ee.getMessage());
                throw new ValidatorException(message, ee.getCause());
            }
        }
    }

    @Override
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[1];
        values[0] = methodExpression;
        return (values);
    }

    @Override
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        methodExpression = (MethodExpression) values[0];
    }

    @Override
    public boolean isTransient() {
        return (this.transientValue);
    }

    @Override
    public void setTransient(final boolean newTransientValue) {
        this.transientValue = newTransientValue;
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
        if (!(otherObject instanceof MethodExprValidator)) {
            return false;
        }
        MethodExprValidator other = (MethodExprValidator) otherObject;
        MethodExpression otherMe = other.getMethodExpression();
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
        return hash;
    }
}
