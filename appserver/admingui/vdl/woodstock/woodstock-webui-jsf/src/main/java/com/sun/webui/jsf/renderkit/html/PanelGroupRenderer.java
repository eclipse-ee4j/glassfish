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
import com.sun.webui.jsf.component.PanelGroup;
import com.sun.webui.jsf.util.RenderingUtilities;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.List;

/**
 * Renderer for a {@link com.sun.webui.jsf.component.PanelGroup} component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.PanelGroup"))
public final class PanelGroupRenderer extends AbstractRenderer {

    /**
     * Element name.
     */
    private String elementName;

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        PanelGroup panelGroup = (PanelGroup) component;
        if (panelGroup.isBlock()) {
            elementName = "div";
        } else {
            elementName = "span";
        }
        writer.startElement(elementName, component);
    }

    @Override
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        addCoreAttributes(context, component, writer, null);
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {

        PanelGroup panelGroup = (PanelGroup) component;
        List children = panelGroup.getChildren();
        ResponseWriter writer = context.getResponseWriter();
        UIComponent separatorFacet = panelGroup
                .getFacet(PanelGroup.SEPARATOR_FACET);
        if (separatorFacet != null) {
            for (int i = 0; i < children.size(); i++) {
                if (i > 0) {
                    RenderingUtilities.renderComponent(separatorFacet, context);
                }
                RenderingUtilities.renderComponent(
                        (UIComponent) children.get(i), context);
            }
        } else {
            String separator = panelGroup.getSeparator();
            if (separator == null) {
                separator = "\n";
            }
            for (int i = 0; i < children.size(); i++) {
                if (i > 0) {
                    writer.write(separator);
                }
                RenderingUtilities.renderComponent(
                        (UIComponent) children.get(i), context);
            }
        }
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        writer.endElement(elementName);
    }
}
