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
import com.sun.webui.jsf.component.Script;
import com.sun.webui.jsf.util.RenderingUtilities;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * This class is responsible for rendering the script component for the HTML
 * Render Kit. The script component can be used as an Script
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.Script"))
public final class ScriptRenderer extends AbstractRenderer {

    /**
     * The set of String pass-through attributes to be rendered.
     */
    private static final String[] STRING_ATTRIBUTES = {
        "charset",
        "type"
    };

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        writer.startElement("script", component);
    }

    @Override
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Script script = (Script) component;
        addCoreAttributes(context, component, writer, null);
        addStringAttributes(context, component, writer, STRING_ATTRIBUTES);
        // the URL is the tough thing because it needs to be encoded:
        String url = script.getUrl();
        if (url != null) {
            // Get resource URL -- bugtraq #6305522.
            RenderingUtilities.renderURLAttribute(context, writer, script,
                    "src", context.getApplication().getViewHandler()
                            .getResourceURL(context, url),
                    "url");
        }
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // End the appropriate element
        writer.endElement("script");
        writer.write("\n");
    }
}
