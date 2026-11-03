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
import com.sun.webui.jsf.component.ThemeLinks;
import com.sun.webui.jsf.util.MessageUtil;
import com.sun.webui.theme.Theme;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

import static com.sun.webui.jsf.util.JavaScriptUtilities.renderHeaderScriptTags;
import static com.sun.webui.jsf.util.RenderingUtilities.renderStyleSheetInline;
import static com.sun.webui.jsf.util.RenderingUtilities.renderStyleSheetLink;
import static com.sun.webui.jsf.util.ThemeUtilities.getTheme;

/**
 * Renderer for a {@link Theme} component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.ThemeLinks"))
public final class ThemeLinksRenderer extends jakarta.faces.render.Renderer {

    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {
    }

    @Override
    public void encodeBegin(final FacesContext context,
            final UIComponent component) throws IOException {

        if (component == null) {
            return;
        }
        if (!(component instanceof ThemeLinks)) {
            Object[] params = {
                component.toString(),
                this.getClass().getName(),
                ThemeLinks.class.getName()
            };
            String message = MessageUtil.getMessage(
                    "com.sun.webui.jsf.resources.LogMessages",
                    "Renderer.component", params);
            throw new FacesException(message);
        }

        ThemeLinks themeLinks = (ThemeLinks) component;
        ResponseWriter writer = context.getResponseWriter();

        // Link and Scripts
        Theme theme = getTheme(context);
        if (themeLinks.isStyleSheetInline()) {
            renderStyleSheetInline(themeLinks, theme, context, writer);
        } else if (themeLinks.isStyleSheetLink()) {
            renderStyleSheetLink(themeLinks, theme, context, writer);
        }

        // Do not render any JS.
        if (!themeLinks.isJavaScript()) {
            return;
        }

        // Render script tags
        renderHeaderScriptTags(themeLinks.isDebug(),
                themeLinks.isParseWidgets(), writer);
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {
    }
}
