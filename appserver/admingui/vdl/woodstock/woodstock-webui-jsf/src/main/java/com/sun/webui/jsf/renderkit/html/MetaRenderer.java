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
import com.sun.webui.jsf.component.Meta;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * This class is responsible for rendering the meta component for the HTML
 * Render Kit.
 * The meta component can be used as an Meta.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.Meta"))
public final class MetaRenderer extends AbstractRenderer {

    /**
     * The set of String pass-through attributes to be rendered.
     */
    private static final String[] STRING_ATTRIBUTES = {
        "name",
        "content",
        "scheme"
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
        Meta meta = (Meta) component;
        writer.startElement("meta", meta);
        addCoreAttributes(context, component, writer, null);
        String header = meta.getHttpEquiv();
        if (header != null) {
            writer.writeAttribute("http-equiv", header, null);
        }
        addStringAttributes(context, component, writer, STRING_ATTRIBUTES);
        writer.endElement("meta");
        writer.write("\n");
    }
}
