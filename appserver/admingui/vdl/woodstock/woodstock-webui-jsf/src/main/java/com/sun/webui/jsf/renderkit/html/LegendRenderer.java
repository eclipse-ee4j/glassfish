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
import com.sun.webui.jsf.component.Legend;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * Renderer for an {@link Legend} component.
 *
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.Legend"))
public final class LegendRenderer extends AbstractRenderer {

    /**
     * Default position.
     */
    private static final String DEFAULT_POSITION = "right";

    /**
     * Creates a new instance of LegendRenderer.
     */
    public LegendRenderer() {
        // default constructor
    }

    /**
     * Renders the legend.
     *
     * @param context The current FacesContext
     * @param component The Legend object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        if (context == null || component == null || writer == null) {
            throw new NullPointerException();
        }

        Legend legend = (Legend) component;

        if (!legend.isRendered()) {
            return;
        }

        // Render the outer div
        renderOuterDiv(context, legend, writer);
        // Render the legend image
        RenderingUtilities.renderComponent(legend.getLegendImage(), context);
        writer.write("&nbsp;");
        // Render the legend text
        String text;
        if (legend.getText() != null) {
            text = legend.getText();
        } else {
            text = getTheme().getMessage("Legend.requiredField");
        }
        writer.writeText(text, null);
        // Close the outer div
        writer.endElement("div");
    }

    /**
     * Renders the outer div which contains the legend.
     *
     * @param context The current FacesContext
     * @param legend The Legend object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderOuterDiv(final FacesContext context,
            final Legend legend, final ResponseWriter writer)
            throws IOException {

        String style = legend.getStyle();
        String id = legend.getClientId(context);
        String divStyleClass = getTheme().getStyleClass(
                ThemeStyles.LABEL_REQUIRED_DIV);
        String align;
        if (legend.getPosition() != null) {
            align = legend.getPosition();
        } else {
            align = DEFAULT_POSITION;
        }

        writer.startElement("div", legend);
        if (id != null) {
            writer.writeAttribute("id", id, null);
        }
        writer.writeAttribute("align", align, null);
        if (style != null) {
            writer.writeAttribute("style", style, "style");
        }
        RenderingUtilities.renderStyleClass(context, writer,
                (UIComponent) legend, divStyleClass);
    }

    /**
     * Utility to get the current theme.
     * @return theme.
     */
    private Theme getTheme() {
        return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }
}
