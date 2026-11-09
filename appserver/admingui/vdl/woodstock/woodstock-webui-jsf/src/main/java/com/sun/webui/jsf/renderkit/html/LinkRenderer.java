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
import com.sun.webui.jsf.component.Link;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

import static com.sun.webui.jsf.util.RenderingUtilities.isPortlet;

/**
 * This class is responsible for rendering the link component for the
 * HTML Render Kit.
 * The link component can be used as an Link.
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.Link"))
public final class LinkRenderer extends AbstractRenderer {

    /**
     * The set of String pass-through attributes to be rendered.
     */
    private static final String[] STRING_ATTRIBUTES = {
        "charset",
        "media",
        "rel",
        "type",
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
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // End the appropriate element
        Link link = (Link) component;

        if (!isPortlet(context)) {
            writer.startElement("link", link);
            addCoreAttributes(context, component, writer, null);
            addStringAttributes(context, component, writer, STRING_ATTRIBUTES);
            String lang = link.getUrlLang();
            if (null != lang) {
                writer.writeAttribute("hreflang", lang, "lang");
            }
            // the URL is the tough thing because it needs to be encoded:
            String url = link.getUrl();
            if (url != null) {
                writer.writeAttribute("href",
                        context.getApplication().getViewHandler()
                                .getResourceURL(context, url),
                        "url");
            }
            writer.endElement("link");
            writer.write("\n");
        }
    }
}
