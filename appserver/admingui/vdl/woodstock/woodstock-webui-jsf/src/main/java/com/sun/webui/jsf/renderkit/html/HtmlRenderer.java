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

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

import static com.sun.webui.jsf.util.RenderingUtilities.isPortlet;

/**
 * Renderer for a {@code Html} component.
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.Html"))
public final class HtmlRenderer extends AbstractRenderer {

    /**
     * The set of String pass-through attributes to be rendered.
     */
    private static final String[] STRING_ATTRIBUTES = {
        "xmlns",
        "lang",
    };

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // Start the appropriate element
        if (!isPortlet(context)) {
            writer.startElement("html", component);
        }
    }

    @Override
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        if (!isPortlet(context)) {
            addStringAttributes(context, component, writer, STRING_ATTRIBUTES);
            //these attributes needed for accessibility support
            writer.writeAttribute("xmlns:wairole",
                    "http://www.w3.org/2005/01/wai-rdf/GUIRoleTaxonomy#",
                    null);
            writer.writeAttribute("xmlns:waistate",
                    "http://www.w3.org/2005/07/aaa", null);
            writer.write("\n");
        }
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        if (!isPortlet(context)) {
            writer.endElement("html");
            writer.write("\n");
        }
    }
}
