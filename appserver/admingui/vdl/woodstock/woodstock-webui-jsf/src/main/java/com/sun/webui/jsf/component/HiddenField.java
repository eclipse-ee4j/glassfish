/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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
import com.sun.webui.jsf.util.LogUtil;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;

/**
 * The HiddenField component is used to create a hidden input field.
 */
@Component(type = "com.sun.webui.jsf.HiddenField",
        family = "com.sun.webui.jsf.HiddenField",
        displayName = "Hidden Field",
        tagName = "hiddenField",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_hidden_field",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_hidden_field_props")
        //CHECKSTYLE:ON
public class HiddenField extends WebuiInput {

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Flag indicating that the hidden field should not send its value to the
     * server.
     */
    @Property(name = "disabled",
            displayName = "Disabled",
            category = "Behavior")
    private boolean disabled;

    /**
     * disabled set flag.
     */
    private boolean disabledSet;

    /**
     * Creates a new instance of HiddenField.
     */
    public HiddenField() {
        super();
        setRendererType("com.sun.webui.jsf.HiddenField");
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.HiddenField}.
     * @return String
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.HiddenField";
    }

    /**
     * Return the value to be rendered as a string when the component is
     * readOnly. The default behavior is to invoke getValueAsString(). Override
     * this method in case a component needs specialized behavior.
     *
     * @param context FacesContext for the current request
     * @return A String value of the component
     */
    public String getReadOnlyValueString(final FacesContext context) {
        return getValueAsString(context);
    }

    /**
     * Return the value to be rendered, as a String (converted if necessary), or
     * {@code null} if the value is null.
     *
     * @param context FacesContext for the current request
     * @return A String value of the component
     */
    public String getValueAsString(final FacesContext context) {
        if (DEBUG) {
            log("getValueAsString()");
        }

        // This is done in case the RENDER_RESPONSE is occuring
        // prematurely due to some error or an immediate condition
        // on a button. submittedValue is set to null when the
        // component has been validated.
        // If the component has not passed through the PROCESS_VALIDATORS
        // phase then submittedValue will be non null if a value
        // was submitted for this component.
        Object submittedValue = getSubmittedValue();
        if (submittedValue != null) {
            if (DEBUG) {
                log("Submitted value is not null " + submittedValue.toString());
            }
            return (String) submittedValue;
        }
        String value = ConversionUtilities
                .convertValueToString(this, getText());

        if (value == null) {
            value = new String();
        }

        if (DEBUG) {
            log("Component value is " + value);
        }
        return value;
    }

    /**
     * This implementation uses
     * {@link ConversionUtilities#convertRenderedValue}.
     * @param context faces context
     * @param newValue value to convert
     * @return Object
     * @throws jakarta.faces.convert.ConverterException if a conversion error
     * occurs
     */
    @Override
    protected Object getConvertedValue(final FacesContext context,
            final Object newValue)
            throws jakarta.faces.convert.ConverterException {

        if (DEBUG) {
            log("getConvertedValue()");
        }

        Object value = ConversionUtilities.convertRenderedValue(context,
                newValue, this);

        if (DEBUG) {
            log("\tComponent is valid " + String.valueOf(isValid()));
        }

        if (DEBUG) {
            log("\tValue is " + String.valueOf(value));
        }
        return value;
    }

    /**
     * Log a message to the standard out.
     * @param msg message to log
     */
    protected void log(final String msg) {
        LogUtil.finest(this.getClass().getName() + "::" + msg);
    }

    /**
     * This implementation aliases {@code "text"} with {@code "value"} and
     * invokes {@code super.getValueExpression}.
     *
     * @param name value expression name
     * @return ValueExpression
     */
    @Override
    public ValueExpression getValueExpression(final String name) {
        if (name.equals("text")) {
            return super.getValueExpression("value");
        }
        return super.getValueExpression(name);
    }

    /**
     * This implementation aliases {@code "text"} with {@code "value"} and
     * invokes {@code super.setValueExpression}.
     *
     * @param name value expression name
     * @param binding value expression
     */
    @Override
    public void setValueExpression(final String name,
            final ValueExpression binding) {

        if (name.equals("text")) {
            super.setValueExpression("value", binding);
            return;
        }
        super.setValueExpression(name, binding);
    }

    // Hide required
    /**
     * This implementation invokes {@code super.isRequired}.
     * @return {@code boolean}
     */
    @Property(name = "required", isHidden = true, isAttribute = false)
    @Override
    public boolean isRequired() {
        return super.isRequired();
    }

    // Hide value as property
    /**
     * This implementation invokes {@code super.getValue}.
     * @return Object
     */
    @Property(name = "value", isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * Get the disabled flag value.
     * @return {@code boolean}
     */
    public boolean isDisabled() {
        if (this.disabledSet) {
            return this.disabled;
        }
        ValueExpression vb = getValueExpression("disabled");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return false;
    }

    /**
     * Flag indicating that the hidden field should not send its value to the
     * server.
     *
     * @see #isDisabled()
     * @param newDisabled disabled
     */
    public void setDisabled(final boolean newDisabled) {
        this.disabled = newDisabled;
        this.disabledSet = true;
    }

    /**
     * Literal value to be rendered in this hidden field. If this property is
     * specified by a value binding expression, the corresponding value will be
     * updated if validation succeeds.
     * @return Object
     */
    @Property(name = "text",
            displayName = "Text",
            category = "Data",
            isDefault = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    public Object getText() {
        return getValue();
    }

    /**
     * Literal value to be rendered in this hidden field. If this property is
     * specified by a value binding expression, the corresponding value will be
     * updated if validation succeeds.
     *
     * @see #getText()
     * @param text text
     */
    public void setText(final Object text) {
        setValue(text);
    }

    /**
     * This implementation restores the state of all properties.
     * @param context faces context
     * @param state state object
     */
    @Override
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.disabled = ((Boolean) values[1]);
        this.disabledSet = ((Boolean) values[2]);
    }

    /**
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[3];
        values[0] = super.saveState(context);
        if (this.disabled) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.disabledSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        return values;
    }
}
