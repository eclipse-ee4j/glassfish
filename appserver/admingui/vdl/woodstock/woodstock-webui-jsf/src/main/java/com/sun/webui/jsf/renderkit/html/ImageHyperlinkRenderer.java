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
import com.sun.webui.jsf.component.ImageHyperlink;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.RenderingUtilities;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Map;

/**
 * This class is responsible for rendering the {@link ImageHyperlink} component
 * for the HTML Render Kit. The {@link ImageHyperlink} component can be used as
 * an anchor, a plain hyperlink or a hyperlink that submits the form depending
 * on how the properties are filled out for the component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.ImageHyperlink"))
public class ImageHyperlinkRenderer extends HyperlinkRenderer {

    /**
     * Label left.
     */
    private static final String LABEL_LEFT = "left";

    /**
     * Label right.
     */
    private static final String LABEL_RIGHT = "right";

    /**
     * This implementation renders the image.
     * @param context faces context
     * @param component UI component
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    @Override
    protected void finishRenderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        //create an image component based on image attributes
        //write out image as escaped text
        //FIXME: suppress the text field from the XML
        ImageHyperlink ilink = (ImageHyperlink) component;

        // If there is no text property set, then label == null which prevents
        // rendering anything at all
        //
        Object text = ilink.getText();
        String label;
        if (text == null) {
            label = null;
        } else {
            label = ConversionUtilities.convertValueToString(component, text);
        }

        String textPosition = ilink.getTextPosition();
        if (label != null && textPosition.equalsIgnoreCase(LABEL_LEFT)) {
            writer.writeText(label, null);
            writer.write("&nbsp;");
        }

        // ImageURL
        UIComponent ic = ilink.getImageFacet();
        if (ic != null) {
            // GF-required 508 change
            Map<String, Object> atts = ic.getAttributes();
            Object value = atts.get("alt");
            if ((value == null) || (value.equals(""))) {
                atts.put("alt", label);
            }
            value = atts.get("toolTip");
            if ((value == null) || (value.equals(""))) {
                atts.put("toolTip", label);
            }
            RenderingUtilities.renderComponent(ic, context);
        }

        if (label != null && textPosition.equalsIgnoreCase(LABEL_RIGHT)) {
            writer.write("&nbsp;");
            writer.writeText(label, null);
        }
    }
}
