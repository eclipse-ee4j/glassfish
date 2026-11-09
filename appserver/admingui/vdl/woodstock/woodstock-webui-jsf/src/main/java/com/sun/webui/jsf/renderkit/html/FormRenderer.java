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

package com.sun.webui.jsf.renderkit.html;

import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.Form;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.RenderingUtilities;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Renderer for a {@link Form} component.
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.Form"))
public final class FormRenderer extends AbstractRenderer {

    /**
     * The set of String pass-through attributes to be rendered.
     */
    private static final String[] STRING_ATTRIBUTES = {
        "enctype",
        "accessKey",
        "onReset",
        "onSubmit",
        "target",
    };

    /**
     * Hidden field.
     */
    private static final String SUBMISSION_COMPONENT_HIDDEN_FIELD =
            "_submissionComponentId";

    /**
     * Form hidden field.
     */
    private static final String FORM_HIDDEN_FIELD = "_hidden";

    /**
     * Record a flag indicating whether this was the form (of the several
     * forms on the current page) that was submitted. Also, if the submission
     * component id is known, then set the submitted virtual form if
     * appropriate.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code UIComponent} to be decoded
     *
     * @throws NullPointerException if {@code context} or
     *  {@code component} is {@code null}
     */
    @Override
    public void decode(final FacesContext context, final UIComponent component)
        throws NullPointerException {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

        Form form = (Form) component;
        Map map = context.getExternalContext().getRequestParameterMap();
        boolean b = map.containsKey(form.getClientId(context)
                + FORM_HIDDEN_FIELD);
        form.setSubmitted(b);
        if (LogUtil.fineEnabled()) {
            LogUtil.fine("Form(id=" + form.getId() + ",submitted="
                    + form.isSubmitted() + ")");
        }

        String hiddenFieldClientId = SUBMISSION_COMPONENT_HIDDEN_FIELD;
        String submissionComponentId = (String) map.get(hiddenFieldClientId);
        if (submissionComponentId != null) {
            Form.VirtualFormDescriptor vfd = form
                    .getVFormCompSubmits(submissionComponentId);
            if (vfd != null) {
                form.setSubmittedVirtualForm(vfd);
            }
        }

    }

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // Start the appropriate element
        Form form = (Form) component;
        if (LogUtil.fineEnabled()) {
            LogUtil.fine("Form(id=" + form.getId() + ")");
        }
        writer.startElement("form", form);

        //reapply any submitted values erased by the virtual forms mechanism
        form.restoreNonParticipatingSubmittedValues();
    }

    @Override
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Form form = (Form) component;

        // Render the core attributes for the "form" element
        addCoreAttributes(context, form, writer, "form");
        writer.writeAttribute("method", "post", null);
        writer.writeAttribute("action", action(context), null);
        addStringAttributes(context, form, writer, EVENTS_ATTRIBUTES);
        addStringAttributes(context, form, writer, STRING_ATTRIBUTES);

        if (!form.isAutoComplete()) {
            //only render it if it's false
            writer.writeAttribute("autocomplete", "off", null);
        }
        // Render a newline for pretty printing
        writer.write("\n");
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Form form = (Form) component;

        writer.write("\n");
        // Render the hidden field noting this form as being submitted
        RenderingUtilities.renderHiddenField(component, writer,
                form.getClientId(context) + FORM_HIDDEN_FIELD,
                form.getClientId(context) + FORM_HIDDEN_FIELD);

        writer.write("\n");
        context.getApplication().getViewHandler().writeState(context);
        writer.write("\n");

        // Render the end of the form element
        writer.endElement("form");
        writer.write("\n");

        if (LogUtil.finestEnabled()) {
            LogUtil.finest("  Rendering completed");
        }
    }


    /**
     * Return the URI to which this form should be submitted.
     *
     * @param context {@code FacesContext} for the current request
     * @return String
     */
    private String action(final FacesContext context) {
        String viewId = context.getViewRoot().getViewId();
        String url = context.getApplication()
                .getViewHandler()
                .getActionURL(context, viewId);
        return context.getExternalContext().encodeActionURL(url);

    }

    /**
     * Return the name of a JavaScript function that will be called
     * by the specified event handler.
     *
     * @param context {@code FacesContext} for the current request
     * @param form {@link Form} being rendered
     * @param handler Name of the event handler that will call
     * @return String
     */
    private String function(final FacesContext context, final Form form,
            final String handler) {

        String clientId = form.getClientId(context);
        return handler + "_" + clientId.replace(':', '_');

    }
}
