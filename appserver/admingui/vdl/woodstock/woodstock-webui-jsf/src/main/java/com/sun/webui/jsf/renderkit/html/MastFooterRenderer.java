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
import com.sun.webui.html.HTMLAttributes;
import com.sun.webui.html.HTMLElements;
import com.sun.webui.jsf.component.Icon;
import com.sun.webui.jsf.component.ImageComponent;
import com.sun.webui.jsf.component.MastFooter;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * MastFooter renderer.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.MastFooter"))
public final class MastFooterRenderer extends AbstractRenderer {

    /**
     * Creates a new instance of MastheadRenderer.
     */
    public MastFooterRenderer() {
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        MastFooter footer = (MastFooter) component;
        Theme theme = ThemeUtilities.getTheme(context);

        writer.startElement(HTMLElements.DIV, footer);
        String styleClass = footer.getStyleClass();
        if (styleClass != null && styleClass.length() > 0) {
            writer.writeAttribute(HTMLAttributes.CLASS, styleClass, null);
        } else {
            writer.writeAttribute(HTMLAttributes.CLASS,
                    theme.getStyleClass(ThemeStyles.MASTHEAD_FOOTER),
                    null);
        }

        String style = footer.getStyle();
        if (style != null && style.length() > 0) {
            writer.writeAttribute(HTMLAttributes.STYLE, style, null);
        }

        writer.writeAttribute(HTMLAttributes.ID, footer.getClientId(context),
                null);
        writer.writeAttribute(HTMLAttributes.ALIGN, "right", null);
        renderCorporateImage(context, footer, writer, theme);
        writer.endElement(HTMLElements.DIV);

    }

    /**
     * Render the corporate logo as an image in the footer associated with the
     * masthead.
     *
     * @param context The current FacesContext
     * @param footer The MastFooter component
     * @param writer The current ResponseWriter
     * @param theme The current Theme
     * @throws IOException if an IO error occurs
     */
    protected void renderCorporateImage(final FacesContext context,
            final MastFooter footer, final ResponseWriter writer,
            final Theme theme) throws IOException {

        UIComponent corporateFacet
                = footer.getFacet("corporateImage");
        if (corporateFacet != null) {
            RenderingUtilities.renderComponent(corporateFacet, context);
            return;
        }

        // no facet specified
        // use the values specified for component props if set.
        //
        String imageAttr = footer.getCorporateImageURL();
        if (imageAttr != null && imageAttr.trim().length() != 0) {
            ImageComponent image = new ImageComponent();
            image.setUrl(imageAttr);
            imageAttr = footer.getCorporateImageDescription();
            if (imageAttr != null) {
                image.setAlt(imageAttr);
            }
            int dim = footer.getCorporateImageHeight();
            if (dim != 0) {
                image.setHeight(dim);
            }
            dim = footer.getCorporateImageWidth();
            if (dim != 0) {
                image.setWidth(dim);
            }
            RenderingUtilities.renderComponent(image, context);
            return;
        }

        // use default Theme corporate image
        // First see if there is an image path. If there is
        // call ThemeUtilities.getIcon to an Icon.
        try {
            String imagePath
                    = theme.getImagePath(ThemeImages.MASTHEAD_CORPNAME);
            if (imagePath == null) {
                return;
            }
            Icon icon = ThemeUtilities.getIcon(theme,
                    ThemeImages.MASTHEAD_CORPNAME);
            icon.setId(footer.getId() + "_corporateImage");
            RenderingUtilities.renderComponent(icon, context);
        } catch (IOException e) {
            // Don't care.
        }
    }
}
