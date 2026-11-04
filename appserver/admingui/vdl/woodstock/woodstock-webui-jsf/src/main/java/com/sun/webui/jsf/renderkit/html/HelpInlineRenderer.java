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
import com.sun.webui.jsf.component.HelpInline;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * Renders an instance of the {@link HelpInline} component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.HelpInline"))
public final class HelpInlineRenderer extends AbstractRenderer {

    /**
     * Creates a new instance of HelpInlineRenderer.
     */
    public HelpInlineRenderer() {
    }

    /**
     * Render the start of the HelpInline component.
     *
     * @param context The current FacesContext
     * @param component The ImageComponent object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // render start of HelpInline
        HelpInline help = (HelpInline) component;
        Theme theme = ThemeUtilities.getTheme(context);

        writer.startElement("div", help);

        String style;
        if (help.getType().equals("page")) {
            style = theme.getStyleClass(ThemeStyles.HELP_PAGE_TEXT);
        } else {
            style = theme.getStyleClass(ThemeStyles.HELP_FIELD_TEXT);
        }

        addCoreAttributes(context, help, writer, style);

        String text = ConversionUtilities.convertValueToString(help,
                help.getText());
        if (text != null) {
            writer.write(text);
            writer.write("&nbsp;&nbsp;");
        }
    }

    /**
     * Render the end of the HelpInline component.
     *
     * @param context The current FacesContext
     * @param component The ImageComponent object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        writer.endElement("div");
    }
}
