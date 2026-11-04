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
package com.sun.webui.jsf.component;

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.util.ConversionUtilities;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;

/**
 * The PasswordField component is used to create a password text field.
 */
@Component(type = "com.sun.webui.jsf.PasswordField",
        family = "com.sun.webui.jsf.PasswordField",
        displayName = "Password Field",
        instanceName = "passwordField",
        tagName = "passwordField",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_password_field",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_password_field_props")
        //CHECKSTYLE:ON
public final class PasswordField extends Field {

    /**
     * Default constructor.
     */
    public PasswordField() {
        super();
        setRendererType("com.sun.webui.jsf.PasswordField");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.PasswordField";
    }

    /**
     * Return the value to be rendered as a string when the component is
     * readOnly. The value will be represented using asterisks.
     *
     * @param context FacesContext for the current request
     * @return A String value of the component
     */
    @Override
    public String getReadOnlyValueString(final FacesContext context) {
        String value = ConversionUtilities
                .convertValueToString(this, getValue());
        if (value == null) {
            return new String();
        }
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            chars[i] = '*';
        }
        return new String(chars);
    }

    /**
     * Flag indicating that an input value for this field is mandatory, and
     * failure to provide one will trigger a validation error.
     * @param required required
     */
    @Property(name = "required")
    @Override
    public void setRequired(final boolean required) {
        super.setRequired(required);
    }

    @Override
    public ValueExpression getValueExpression(final String name) {
        if (name.equals("password")) {
            return super.getValueExpression("value");
        }
        return super.getValueExpression(name);
    }

    @Override
    public void setValueExpression(final String name,
            final ValueExpression binding) {

        if (name.equals("password")) {
            super.setValueExpression("value", binding);
            return;
        }
        super.setValueExpression(name, binding);
    }

    // Hide text
    @Property(name = "text", isHidden = true, isAttribute = false)
    @Override
    public Object getText() {
        return super.getText();
    }

    // Hide value
    @Property(name = "value", isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * Literal value to be rendered in this input field. If this property is
     * specified by a value binding expression, the corresponding value will be
     * updated if validation succeeds.
     * @return Object
     */
    @Property(name = "password",
            displayName = "Password",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    public Object getPassword() {
        return getValue();
    }

    /**
     * Literal value to be rendered in this input field. If this property is
     * specified by a value binding expression, the corresponding value will be
     * updated if validation succeeds.
     *
     * @see #getPassword()
     * @param password password
     */
    public void setPassword(final Object password) {
        setValue(password);
    }

    @Override
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
    }

    @Override
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[1];
        values[0] = super.saveState(context);
        return values;
    }
}
