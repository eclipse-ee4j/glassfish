/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.component.factory.ri;

import com.sun.jsftemplating.annotation.UIComponentFactory;
import com.sun.jsftemplating.component.ComponentUtil;
import com.sun.jsftemplating.component.factory.ComponentFactoryBase;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.util.LogUtil;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.application.Application;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;

/**
 * <p>
 * This factory is responsible for instantiating a <code>Validator</code> and adding it to the parent. This factory does
 * <b>not</b> create a <code>UIComponent</code>.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
@UIComponentFactory("f:validator")
public class ValidatorFactory extends ComponentFactoryBase {

    /**
     * <p>
     * This is the factory method responsible for creating the <code>UIComponent</code>.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param descriptor The {@link LayoutComponent} descriptor associated with the requested <code>UIComponent</code>.
     * @param parent The parent <code>UIComponent</code>
     *
     * @return The <b>parent</b> <code>UIComponent</code>.
     */
    @Override
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
        Object value = null;
        Validator validator = null;

        // Check for the "binding" property
        String binding = (String) descriptor.getOption("binding");
        if (binding != null) {
            value = ComponentUtil.getInstance(context).resolveValue(context, descriptor, parent, binding);
            if (value != null && !(value instanceof Validator)) {
                // Warn developer that attempted to set a Validator that was
                // not a Validator
                if (LogUtil.warningEnabled()) {
                    LogUtil.warning("JSFT0009", (Object) parent.getId());
                }
            } else {
                validator = (Validator) value;
            }
        }

        // Check to see if we still need to create one...
        if (validator == null) {
            // Check for the "validatorId" property
            String id = (String) descriptor.getOption("validatorId");
            if (id != null) {
                id = (String) ComponentUtil.getInstance(context).resolveValue(context, descriptor, parent, id);
                if (id != null) {
                    // Create a new Validator
                    Application app = context.getApplication();
                    validator = app.createValidator(id);
                    if (validator != null && binding != null) {
                        // Set the validator on the binding, if bound
                        ELContext elctx = context.getELContext();
                        ValueExpression ve = app.getExpressionFactory().createValueExpression(elctx, binding, Object.class);
                        ve.setValue(elctx, validator);
                    }
                }
            }
        }

        // Set the validator on the parent...
        if (validator != null) {
            if (!(parent instanceof EditableValueHolder)) {
                throw new IllegalArgumentException("You may only add " + "f:validator tags to components which are " + "EditableValueHolders.  Component ("
                        + parent.getId() + ") is not an EditableValueHolder.");
            }
            ((EditableValueHolder) parent).addValidator(validator);
        }

        // Return the validator
        return parent;
    }
}
