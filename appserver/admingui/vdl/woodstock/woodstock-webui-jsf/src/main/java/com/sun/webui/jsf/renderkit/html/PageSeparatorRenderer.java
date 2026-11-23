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
 * $Id: PageSeparatorRenderer.java,v 1.1.20.1 2009-12-29 04:52:46 jyeary Exp $
 */
package com.sun.webui.jsf.renderkit.html;

import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.PageSeparator;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * Renderer for a {@link PageSeparator} component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.PageSeparator"))
public final class PageSeparatorRenderer extends AbstractRenderer {

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
    }

    @Override
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        PageSeparator pageSep = (PageSeparator) component;

        writer.startElement("table", component);
        String style = pageSep.getStyle();
        if (style != null) {
            writer.writeAttribute("style", style, null);
        }
        RenderingUtilities.renderStyleClass(context, writer, component, null);
        writer.writeAttribute("border", "0", null);
        writer.writeAttribute("width", "100%", null);
        writer.writeAttribute("cellpadding", "0", null);
        writer.writeAttribute("cellspacing", "0", null);
        writer.startElement("tr", component);
        writer.startElement("td", component);
        writer.writeAttribute("colspan", "3", null);

        RenderingUtilities.renderSpacer(context, writer, component, 30, 1);
        writer.endElement("td");
        writer.endElement("tr");
        writer.startElement("tr", component);
        writer.startElement("td", component);
        RenderingUtilities.renderSpacer(context, writer, component, 1, 10);
        writer.endElement("td");
        writer.startElement("td", component);
        Theme theme = ThemeUtilities.getTheme(context);

        writer.writeAttribute("class", theme.getStyleClass(
                ThemeStyles.TITLE_LINE), null);
        writer.writeAttribute("width", "100%", null);
        RenderingUtilities.renderSpacer(context, writer, component, 1, 1);
        writer.endElement("td");
        writer.startElement("td", component);
        RenderingUtilities.renderSpacer(context, writer, component, 1, 10);
        writer.endElement("td");
        writer.endElement("tr");
        writer.endElement("table");
    }
}
