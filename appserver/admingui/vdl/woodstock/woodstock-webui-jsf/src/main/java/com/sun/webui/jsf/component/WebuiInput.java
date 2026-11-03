/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
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

import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.event.MethodExprValueChangeListener;
import com.sun.webui.jsf.validator.MethodExprValidator;

import jakarta.el.MethodExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.event.ValueChangeListener;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * WebUI input.
 */
public class WebuiInput extends UIInput {

    /**
     * The converter attribute is used to specify a method to translate native
     * property values to String and back for this component. The converter
     * attribute value must be one of the following:
     * <ul>
     * <li>A JavaServer Faces EL expression that resolves to a backing bean or
     * bean property that implements the
     * {@code jakarta.faces.converter.Converter} interface; or
     * </li><li>the ID of a registered converter (a String).</li>
     * </ul>
     * This implementation invokes {@code super.setConverter}.
     * @param converter converter
     */
    @Property(name = "converter")
    @Override
    public void setConverter(final Converter converter) {
        super.setConverter(converter);
    }

    /**
     * The component identifier for this component. This value must be unique
     * within the closest parent component that is a naming container.
     * This implementation invokes {@code super.setId}.
     * @param id id
     */
    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    /**
     * Flag indicating that event handling for this component should be handled
     * immediately (in Apply Request Values phase) rather than waiting until
     * Invoke Application phase.
     * This implementation invokes {@code super.setImmediate}.
     * @param immediate immediate
     */
    @Property(name = "immediate")
    @Override
    public void setImmediate(final boolean immediate) {
        super.setImmediate(immediate);
    }

    /**
     * Use the rendered attribute to indicate whether the HTML code for the
     * component should be included in the rendered HTML page. If set to false,
     * the rendered HTML page does not include the HTML for the component. If
     * the component is not rendered, it is also not processed on any subsequent
     * form submission.
     * This implementation invokes {@code super.setRendered}.
     * @param rendered rendered
     */
    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * Flag indicating that an input value for this field is mandatory, and
     * failure to provide one will trigger a validation error.
     * This implementation invokes {@code super.setRequired}.
     * @param required required
     */
    @Property(name = "required")
    @Override
    public void setRequired(final boolean required) {
        super.setRequired(required);
    }

    /**
     * The {@code valueChangeListener} attribute is used to specify a
     * method to handle an value-change event that is triggered when the
     * user enters data in the input component. The value of the
     * attribute value must be a JavaServer Faces EL expression that
     * resolves to a backing bean method. The method must take a single
     * parameter of type {@code jakarta.faces.event.ValueChangeEvent},
     * and its return type must be void. The backing bean where the
     * method is defined must implement {@code java.io.Serializable}
     * or {@code jakarta.faces.component.StateHolder}.
     */
    @Property(name = "valueChangeListenerExpression",
            isHidden = true,
            displayName = "Value Change Listener Expression",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.MethodBindingPropertyEditor")
            //CHECKSTYLE:ON
    @Property.Method(event = "valueChange")
    private MethodExpression valueChangeListenerExpression;

    /**
     * Get the {@code valueChangeListenerExpression}.
     * The corresponding listener will be called from the
     * {@code broadcast} method.
     * @return MethodExpression
     */
    public MethodExpression getValueChangeListenerExpression() {
        return this.valueChangeListenerExpression;
    }

    /**
     * Set the {@code valueChangeListenerExpression}.
     * The corresponding listener will be called from the
     * {@code broadcast} method.
     * @param newValueChangeListenerExpression valueChangeListenerExpression
     */
    public void setValueChangeListenerExpression(
            final MethodExpression newValueChangeListenerExpression) {

        this.valueChangeListenerExpression = newValueChangeListenerExpression;
    }

    /**
     * The validator attribute is used to specify a method in a backing bean to
     * validate input to the component. The validator attribute value must be a
     * JavaServer Faces EL expression that resolves to a public method with
     * return type void. The method must take three parameters:
     * <ul>
     * <li>a {@code jakarta.faces.context.FacesContext}</li>
     * <li>a {@code jakarta.faces.component.UIComponent} (the component whose
     * data is to be validated)</li>
     * <li>a {@code java.lang.Object} containing the data to be validated.
     * </li>
     * </ul>
     * <p>The backing bean where the method is defined must implement
     * {@code java.io.Serializable} or
     * {@code jakarta.faces.component.StateHolder}.
     * </p>
     * <p>The method is invoked during the Process Validations Phase.</p>
     */
    @Property(name = "validatorExpression",
            displayName = "Validator Expression",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.ValidatorPropertyEditor")
            //CHECKSTYLE:ON
    @Property.Method(event = "validate")
    private MethodExpression validatorExpression;

    /**
     * Get the {@code validatorExpression}.
     * The corresponding validator will be called from the
     * {@code validateValue} method.
     * @return MethodExpression
     */
    public MethodExpression getValidatorExpression() {
        return this.validatorExpression;
    }

    /**
     * Set the {@code validatorExpression}.
     * The corresponding validator will be called from the
     * {@code validateValue} method.
     * @param newValidatorExpression validatorExpression
     */
    public void setValidatorExpression(
            final MethodExpression newValidatorExpression) {

        this.validatorExpression = newValidatorExpression;
    }

    /**
     * {@inheritDoc}
     * This implementation invokes {@code super.broadcast} and
     * evaluates the value change listener expression.
     **/
    @Override
    public void broadcast(final FacesEvent event)
            throws AbortProcessingException {

        // Perform standard superclass processing
        super.broadcast(event);

        if (event instanceof ValueChangeEvent) {
            MethodExpression vclExpression =
                    getValueChangeListenerExpression();
            if (vclExpression != null) {
                ValueChangeListener vcl =
                        new MethodExprValueChangeListener(vclExpression);
                //just to be sure, use the semantics of the inherited broadcast
                // method
                if (event.isAppropriateListener(vcl)) {
                    event.processListener(vcl);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * This implementation invokes {@code super.validateValue} and
     * evaluates the validator expression.
     **/
    @Override
    @SuppressWarnings("unchecked")
    protected void validateValue(final FacesContext context,
            final Object newValue) {

        // Perform standard superclass processing
        super.validateValue(context, newValue);

        // If our value is valid and not empty, try to call validatorExpression
        if (isValid() && !isEmpty(newValue)) {
            MethodExpression vExpression = getValidatorExpression();
            if (vExpression != null) {
                Validator validator = new MethodExprValidator(vExpression);
                try {
                    validator.validate(context, this, newValue);
                } catch (ValidatorException ve) {
                    // If the validator throws an exception, we're
                    // invalid, and we need to add a message
                    setValid(false);
                    FacesMessage message;
                    String validatorMessageString = getValidatorMessage();

                    if (null != validatorMessageString) {
                        message = new FacesMessage(validatorMessageString,
                                validatorMessageString);
                    } else {
                        message = ve.getFacesMessage();
                    }
                    if (message != null) {
                        message.setSeverity(FacesMessage.SEVERITY_ERROR);
                        context.addMessage(getClientId(context), message);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * This implementation restores the state of all properties.
     * @param context faces context
     * @param state state object
     *
     */
    @Override
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        valueChangeListenerExpression = (MethodExpression)
                restoreAttachedState(context, values[1]);
        validatorExpression = (MethodExpression)
                restoreAttachedState(context, values[2]);
    }

    /**
     * {@inheritDoc}
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     **/
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[3];
        values[0] = super.saveState(context);
        values[1] = saveAttachedState(context, valueChangeListenerExpression);
        values[2] = saveAttachedState(context, validatorExpression);
        return values;
    }
}
