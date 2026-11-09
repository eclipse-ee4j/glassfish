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
import com.sun.webui.jsf.component.Head;
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.sun.webui.jsf.util.JavaScriptUtilities.renderHeaderScriptTags;
import static com.sun.webui.jsf.util.RenderingUtilities.isPortlet;
import static com.sun.webui.jsf.util.RenderingUtilities.renderStyleSheetLink;
import static com.sun.webui.jsf.util.ThemeUtilities.getTheme;

/**
 * Renderer for a {@link Head} component.
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.Head"))
public final class HeadRenderer extends AbstractRenderer {

    /**
     * The set of String pass-through attributes to be rendered.
     */
    private static final String[] STRING_ATTRIBUTES = {
        "profile",
    };

    /**
     * Date one.
     */
    private static final String DATE_ONE =
            (new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US))
                    .format(new Date(1));

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // Start the appropriate element
        if (!isPortlet(context)) {
            writer.startElement("head", component);
        }
    }

    @Override
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Head head = (Head) component;
        if (!isPortlet(context)) {
            // Profile
            addStringAttributes(context, component, writer, STRING_ATTRIBUTES);

            // Meta tags
            if (head.isMeta()) {
                writer.write("\n");

                HttpServletResponse response = (HttpServletResponse)
                        FacesContext.getCurrentInstance()
                                .getExternalContext()
                                .getResponse();

                response.setHeader("Pragma", "no-cache");
                response.setHeader("Cache-Control", "no-store");
                response.setHeader("Cache-Control", "no-cache");
                response.setHeader("Expires", DATE_ONE);
                response.setHeader("X-Frame-Options", "SAMEORIGIN");

                renderMetaTag("no-cache", "Pragma", writer, head);
                renderMetaTag("no-cache", "Cache-Control", writer, head);
                renderMetaTag("no-store", "Cache-Control", writer, head);
                renderMetaTag("max-age=0", "Cache-Control", writer, head);
                renderMetaTag("1", "Expires", writer, head);
            }

            // Title
            String title = head.getTitle();
            if (title == null) {
                title = "";
            }

            writer.startElement("title", head);
            writer.write(title);
            writer.endElement("title");
            writer.write("\n");

            // Base
            if (head.isDefaultBase()) {
                writer.startElement("base", head);
                // TODO - verify the requirements w.r.t. printing this href
                writer.writeURIAttribute("href",
                        ComponentUtilities.getBase(context), null);
                writer.endElement("base");
                writer.write("\n");
            }

            // Master link to always write out.
            Theme theme = getTheme(context);
            renderStyleSheetLink(head, theme, context, writer);

            // Do not render any JavaScript.
            if (!head.isJavaScript()) {
                return;
            }

            // Render script tags
            renderHeaderScriptTags(head.isDebug(), head.isParseWidgets(),
                    writer);
        }
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // Start the appropriate element.
        if (!isPortlet(context)) {
            writer.endElement("head");
            writer.write("\n");
        }
    }

    /**
     * Render the meta tag.
     * @param content content attribute
     * @param httpEquivalent http-equiv attribute
     * @param writer writer to use
     * @param head head component
     * @throws IOException if an IO error occurs
     */
    private void renderMetaTag(final String content,
            final String httpEquivalent, final ResponseWriter writer,
            final Head head) throws IOException {

        writer.startElement("meta", head);
        writer.writeAttribute("content", content, null);
        writer.writeAttribute("http-equiv", httpEquivalent, null);
        writer.endElement("meta");
        writer.writeText("\n", null);
    }
}
