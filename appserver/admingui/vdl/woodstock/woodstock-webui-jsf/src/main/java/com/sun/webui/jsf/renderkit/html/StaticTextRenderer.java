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

 /*
 * $Id: StaticTextRenderer.java,v 1.1.20.1 2009-12-29 04:52:44 jyeary Exp $
 */
package com.sun.webui.jsf.renderkit.html;

import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.StaticText;
import com.sun.webui.jsf.util.ConversionUtilities;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIParameter;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Renderer for a {@link StaticText} component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.StaticText"))
public final class StaticTextRenderer extends AbstractRenderer {

    /**
     * The set of String pass-through attributes to be rendered.
     */
    private static final String[] STRING_ATTRIBUTES = {
        "onClick",
        "onDblClick",
        "onMouseUp",
        "onMouseDown",
        "onMouseMove",
        "onMouseOut",
        "onMouseOver",
    };

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        writer.writeText("\n", null);
        writer.startElement("span", component);
    }

    @Override
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        StaticText st = (StaticText) component;
        addCoreAttributes(context, component, writer, null);
        addStringAttributes(context, component, writer, STRING_ATTRIBUTES);
        if (st.getToolTip() != null) {
            writer.writeAttribute("title", st.getToolTip(), null);
        }
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        StaticText staticText = (StaticText) component;
        String currentValue = ConversionUtilities
                .convertValueToString(component, staticText.getText());

        String style = staticText.getStyle();
        String styleClass = staticText.getStyleClass();

        if (currentValue != null) {
            ArrayList<Object> parameterList = new ArrayList<Object>();
            // get UIParameter children...
            for (UIComponent kid : component.getChildren()) {
                //PENDING(rogerk) ignore if child is not UIParameter?
                if (!(kid instanceof UIParameter)) {
                    continue;
                }

                parameterList.add(((UIParameter) kid).getValue());
            }

            // If at least one substitution parameter was specified,
            // use the string as a MessageFormat instance.
            String message;
            if (parameterList.size() > 0) {
                message = MessageFormat.format(currentValue,
                        parameterList.toArray(
                                new Object[parameterList.size()]));
            } else {
                message = currentValue;
            }

            if (message != null) {
                if (staticText.isEscape()) {
                    writer.writeText(message, "value");
                } else {
                    writer.write(message);
                }
            }
        }
        writer.endElement("span");
    }
}
