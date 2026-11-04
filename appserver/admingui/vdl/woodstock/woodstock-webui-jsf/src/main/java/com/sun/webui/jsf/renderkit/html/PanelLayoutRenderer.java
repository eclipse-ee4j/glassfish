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
import com.sun.webui.jsf.component.PanelLayout;
import com.sun.webui.jsf.util.RenderingUtilities;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * Renderer for a {@link com.sun.webui.jsf.component.PanelLayout} component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.PanelLayout"))
public final class PanelLayoutRenderer extends AbstractRenderer {

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        writer.startElement("div", component);
    }

    @Override
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        PanelLayout panelLayout = (PanelLayout) component;
        StringBuilder buffer = new StringBuilder();

        // Write id attribute
        String id = component.getId();
        writer.writeAttribute("id", panelLayout.getClientId(context), "id");

        // Write style attribute
        if (PanelLayout.GRID_LAYOUT.equals(panelLayout.getPanelLayout())) {
            buffer.append("position: relative; -rave-layout: grid;");
        }
        String style = panelLayout.getStyle();
        if (style != null && style.length() > 0) {
            buffer.append(" ");
            buffer.append(style);
        }
        writer.writeAttribute("style", buffer.toString(), "style");

        // Write style class attribute
        RenderingUtilities.renderStyleClass(context, writer, component, null);
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        writer.endElement("div");
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
