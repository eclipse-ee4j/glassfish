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
import com.sun.webui.jsf.component.SkipHyperlink;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * This class is responsible for rendering the {@link SkipHyperlink} component.
 */
@Renderer(
        @Renderer.Renders(
                componentFamily = "com.sun.webui.jsf.SkipHyperlink"))
public final class SkipHyperlinkRenderer extends jakarta.faces.render.Renderer {

    /**
     * Creates a new instance of AlertRenderer.
     */
    public SkipHyperlinkRenderer() {
        // default constructor
    }

    // We don't render our own children - defer to the default behaviour,
    // which allows for interweaving compoenents with non-components.
    @Override
    public boolean getRendersChildren() {
        return false;
    }

    @Override
    public void encodeBegin(final FacesContext context,
            final UIComponent component) throws IOException {

        SkipHyperlink link = (SkipHyperlink) component;
        if (!link.isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();

        // Get the theme
        Theme theme = ThemeUtilities.getTheme(context);

        // Components must have a top-level element with the component ID
        Integer index = null;
        int tabIndex = link.getTabIndex();
        if (tabIndex != Integer.MIN_VALUE) {
            index = tabIndex;
        }

        String styleClass;
        if (link.getStyleClass() != null) {
            styleClass = link.getStyleClass();
        } else {
            styleClass = theme.getStyleClass(ThemeStyles.SKIP_WHITE);
        }

        RenderingUtilities.renderSkipLink("", styleClass, link.getStyle(),
                link.getDescription(), index, link, context);
        writer.write("\n");
    }

    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        SkipHyperlink link = (SkipHyperlink) component;
        if (!link.isRendered()) {
            return;
        }
        RenderingUtilities.renderAnchor("", component, context);
    }
}
