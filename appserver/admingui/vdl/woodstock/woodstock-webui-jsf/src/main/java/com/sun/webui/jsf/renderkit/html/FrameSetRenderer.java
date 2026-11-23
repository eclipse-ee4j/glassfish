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
import com.sun.webui.jsf.component.FrameSet;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

import static com.sun.webui.jsf.util.RenderingUtilities.isPortlet;

/**
 * Renderer for a {@link FrameSet} component.
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.FrameSet"))
public final class FrameSetRenderer extends AbstractRenderer {

    /**
     * The set of {@code String} pass-through attributes to be rendered.
     */
    private static final String[] STRING_ATTRIBUTES = {
        "rows",
        "cols",
        "borderColor",
    };

    /**
     * The set of {@code Integer} attributes to be rendered.
     */
    private static final String[] INTEGER_ATTRIBUTES = {
        "border",
        "frameSpacing",
    };

    /**
     * The set of {@code boolean} attributes to be rendered.
     */
    private static final String[] BOOLEAN_ATTRIBUTES = {
        "frameBorder",
    };

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // I don't think this is the correct way to write the XML
        // header
        if (!isPortlet(context)) {
            writer.startElement("frameset", component);
        }
    }

    @Override
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        FrameSet frameset = (FrameSet) component;

        // Render a nested "head" element
        if (!isPortlet(context)) {
            String id = frameset.getClientId(context);
            if (id != null) {
                writer.writeAttribute("id", id, null);
            }
            String styleClass = frameset.getStyleClass();
            if (styleClass != null) {
                writer.writeAttribute("class", styleClass, null);
            }
            String style = frameset.getStyle();
            if (style != null) {
                writer.writeAttribute("style", style, null);
            }
            String toolTip = frameset.getToolTip();
            if (toolTip != null) {
                writer.writeAttribute("title", toolTip, "toolTip");
            }
            // write out the rest of the attributes
            addStringAttributes(context, component, writer,
                    STRING_ATTRIBUTES);
            addBooleanAttributes(context, component, writer,
                    BOOLEAN_ATTRIBUTES);
            addIntegerAttributes(context, component, writer,
                    INTEGER_ATTRIBUTES);
            writer.write("\n");
        }

    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // End the outermost "html" element
        if (!isPortlet(context)) {
            writer.endElement("frameset");
            writer.write("\n");
        }
    }
}
