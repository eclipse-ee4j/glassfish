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

 /*
 * HiddenFieldRenderer.java
 */
package com.sun.webui.jsf.renderkit.html;

import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.ComplexComponent;
import com.sun.webui.jsf.component.Field;
import com.sun.webui.jsf.component.HiddenField;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.MessageUtil;

import jakarta.faces.FacesException;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Renderer for HiddenFieldRenderer {@link HiddenField} component.
 */
@Renderer(@Renderer.Renders(
        componentFamily = "com.sun.webui.jsf.HiddenField"))
public final class HiddenFieldRenderer extends jakarta.faces.render.Renderer {

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        if (!(component instanceof HiddenField)) {
            Object[] params = {
                component.toString(),
                this.getClass().getName(),
                HiddenField.class.getName()
            };
            String message = MessageUtil.getMessage(
                    "com.sun.webui.jsf.resources.LogMessages",
                    "Renderer.component", params);
            throw new FacesException(message);
        }

        HiddenField field = (HiddenField) component;
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("input", field);
        writer.writeAttribute("type", "hidden", null);
        String id = field.getClientId(context);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute("name", id, null);

        // Record the value that is rendered.
        // Note that getValueAsString conforms to JSF conventions
        // for NULL values, in that it returns "" if the component
        // value is NULL. This value cannot be trusted since
        // the fidelity of the data must be preserved, i.e. if the
        // value is null, it must remain null if the component is unchanged
        // by the user..
        //
        // What should be done in the case of submittedValue != null ?
        // This call to getValue may not be value is used by
        // getValueAsString, it may use the submittedValue.
        // Then should the previously set rendered value be
        // preserved ?
        //
        // If submittedValue is not null then the component's
        // model value or local value has not been updated
        // therefore assume that this is an immediate or premature
        // render response. Therefore just assume that if the rendered
        // value was null, the saved information is still valid.
        //
        if (((HiddenField) component).getSubmittedValue() == null) {
            ConversionUtilities.setRenderedValue(component,
                    ((HiddenField) component).getText());
        }

        // Still call the component's getValueAsString method
        // in order to render it.
        String value = field.getValueAsString(context);
        writer.writeAttribute("value", value, "value");

        if (field.isDisabled()) {
            writer.writeAttribute("disabled", "disabled", null);
        }
        writer.endElement("input");
    }

    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        decodeInput(context, component);
    }

    /**
     * Decode the component component.
     *
     * @param context The FacesContext associated with this request
     * @param component The TextField component to decode
     */
    static void decodeInput(final FacesContext context,
            final UIComponent component) {

        if (DEBUG) {
            log("decodeInput()");
        }

        String id = component.getClientId(context);
        Map params = context.getExternalContext().getRequestParameterMap();
        Object valueObject = params.get(id);
        String value = null;

        if (valueObject == null && component instanceof Field) {
            if (component instanceof ComplexComponent) {
                id = ((Field) component).getLabeledElementId(context);
            } else {
                id = component.getClientId(context);
            }
            valueObject = params.get(id);
        }

        if (valueObject != null) {
            value = (String) valueObject;
            if (DEBUG) {
                log("Submitted value is " + value);
            }

            if (component instanceof Field && ((Field) component).isTrim()) {
                value = value.toString().trim();
                if (DEBUG) {
                    log("Trimmed value is " + String.valueOf(value));
                }
            }
        } else if (DEBUG) {
            log("\tNo relevant input parameter");
        }

        ((EditableValueHolder) component).setSubmittedValue(value);
    }

    /**
     * Log a message to the standard output.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(msg);
    }
}
