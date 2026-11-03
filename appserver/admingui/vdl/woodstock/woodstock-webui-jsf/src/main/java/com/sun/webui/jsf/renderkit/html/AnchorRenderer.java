/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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
import com.sun.webui.jsf.component.Anchor;
import com.sun.webui.jsf.component.Icon;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.util.ClientSniffer;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.RenderingUtilities;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.beans.Beans;
import java.io.IOException;
import java.net.URL;

/**
 * This class is responsible for rendering the {@link Anchor} component for the
 * HTML Render Kit.
 * The {@link Anchor} component can be used as an anchor.
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.Anchor"))
public final class AnchorRenderer extends AbstractRenderer {

    /**
     * Id of the transparent image to be rendered for IE browsers.
     */
    private static final String ANCHOR_IMAGE = "_img";

    /**
     * Icon displayed during design time.
     */
    private static final String DESIGN_TIME_ICON =
            "/com/sun/webui/jsf/design/resources/AnchorIcon.gif";

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Anchor anchor = (Anchor) component;
        writer.startElement("a", anchor);
    }

    @Override
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // Set up local variables we will need
        Anchor anchor = (Anchor) component;
        String id = anchor.getId();

        // Design time only behavior - display an icon so that this
        // component has a visual appearance in the IDE
        if (Beans.isDesignTime()) {
            writer.startElement("img", anchor);
            try {
                URL url = this.getClass().getResource(DESIGN_TIME_ICON);
                writer.writeURIAttribute("src", url, null);
            } catch (IOException e) {
                LogUtil.severe("Error getting anchor icon: ", e);
            }
            writer.endElement("img");
        }

        // Render core and pass through attributes as necessary
        // NOTE - id is being rendered "as is" instead of the normal convention
        // that we render the client id.
        writer.writeAttribute("id", id, "id");
        String style = anchor.getStyle();
        String styleClass = anchor.getStyleClass();
        if (styleClass != null) {
            RenderingUtilities.renderStyleClass(context, writer, component,
                    null);
        }
        if (style != null) {
            writer.writeAttribute("style", style, null);
        }

        // XHTML requires that this been the same as the id and it may
        // removed.
        writer.writeAttribute("name", id, null);
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        ClientSniffer sniffer = ClientSniffer.getInstance(context);
        if (sniffer.isIe6up() || sniffer.isIe7() || sniffer.isIe7up()) {
            Icon icon = new Icon();
            icon.setIcon(ThemeImages.DOT);
            icon.setId(component.getId() + ANCHOR_IMAGE);
            RenderingUtilities.renderComponent(icon, context);
        }

        // End the appropriate element
        Anchor anchor = (Anchor) component;
        writer.endElement("a");
    }
}
