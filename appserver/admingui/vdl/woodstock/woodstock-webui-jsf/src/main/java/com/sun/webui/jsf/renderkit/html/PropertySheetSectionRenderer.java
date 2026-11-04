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

import com.sun.webui.html.HTMLAttributes;
import com.sun.webui.html.HTMLElements;
import com.sun.webui.jsf.component.PropertySheetSection;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.Renderer;

import java.io.IOException;
import java.util.List;

/**
 * Renders a version page.
 */
@com.sun.faces.annotation.Renderer(
        @com.sun.faces.annotation.Renderer.Renders(
                componentFamily = "com.sun.webui.jsf.PropertySheetSection"))
public final class PropertySheetSectionRenderer extends Renderer {

    /**
     * Creates a new instance of PropertySheetSectionRenderer.
     */
    public PropertySheetSectionRenderer() {
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        if (context == null || component == null) {
            throw new NullPointerException();
        }

        if (!component.isRendered()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        PropertySheetSection propertySheetSection
                = (PropertySheetSection) component;

        // Get the theme
        Theme theme = ThemeUtilities.getTheme(context);
        renderPropertySheetSection(context, propertySheetSection, theme,
                writer);
    }

    // There is an extensive use of the request map by the
    // template renderer.
    // The setAttribute handler places the key/value pair in the
    // request map.
    /**
     * Render the property sheet sections.
     *
     * @param context The current FacesContext
     * @param propertySheetSection The PropertySheet object to render
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderPropertySheetSection(final FacesContext context,
            final PropertySheetSection propertySheetSection, final Theme theme,
            final ResponseWriter writer) throws IOException {

        int numChildren = propertySheetSection.getSectionChildrenCount();
        if (numChildren <= 0) {
            return;
        }

        writer.startElement(HTMLElements.DIV, propertySheetSection);
        writer.writeAttribute(HTMLAttributes.ID,
                propertySheetSection.getClientId(context), "id");
        String propValue = RenderingUtilities.getStyleClasses(context,
                propertySheetSection,
                theme.getStyleClass(ThemeStyles.CONTENT_FIELDSET));
        writer.writeAttribute(HTMLAttributes.CLASS, propValue, null);

        // There was a distinction made between ie and other browsers.
        // If the browser was ie, fieldsets were used, and if not
        // divs were used. Why ? Just use divs here.
        writer.startElement(HTMLElements.DIV, propertySheetSection);
        writer.writeAttribute(HTMLAttributes.CLASS,
                theme.getStyleClass(ThemeStyles.CONTENT_FIELDSET_DIV), null);

        // Render the section label
        // Why isn't this a label facet on PropertySheetSection, too ?
        propValue = propertySheetSection.getLabel();
        if (propValue != null) {
            writer.startElement(HTMLElements.DIV, propertySheetSection);
            writer.writeAttribute(HTMLAttributes.CLASS,
                    theme.getStyleClass(
                            ThemeStyles.CONTENT_FIELDSET_LEGEND_DIV),
                    null);
            writer.writeText(propValue, null);
            writer.endElement(HTMLElements.DIV);
        }

        renderProperties(context, propertySheetSection, theme, writer);
        writer.endElement(HTMLElements.DIV);
        writer.endElement(HTMLElements.DIV);
    }

    /**
     * Render a required fields legend. If
     * {@code propertySheet.getRequiredFields} returns null a spacer is
     * rendered.
     *
     * @param context The current FacesContext
     * @param propertySheetSection The PropertySheet object to render
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderProperties(final FacesContext context,
            final PropertySheetSection propertySheetSection, final Theme theme,
            final ResponseWriter writer) throws IOException {

        List properties = propertySheetSection.getVisibleSectionChildren();

        writer.startElement(HTMLElements.TABLE, propertySheetSection);
        writer.writeAttribute(HTMLAttributes.BORDER, 0, null);
        writer.writeAttribute(HTMLAttributes.CELLSPACING, 0, null);
        writer.writeAttribute(HTMLAttributes.CELLPADDING, 0, null);
        writer.writeAttribute(HTMLAttributes.TITLE, "", null);
        writer.writeAttribute(HTMLAttributes.ROLE,
                HTMLAttributes.ROLE_PRESENTATION, null);

        // Unfortunately the PropertyRenderer needs to render
        // a TR and TD since we are opening a table context here.
        // This can't be changed easily unless a strategy like the
        // radio button and checkbox group renderer is used, where there is
        // a table layout renderer. I'm not sure if that is sufficiently
        // robust to handle "properties".
        for (Object property : properties) {
            RenderingUtilities.renderComponent((UIComponent) property, context);
        }
        writer.endElement(HTMLElements.TABLE);
    }

    /**
     * Render a property sheet.
     *
     * @param context The current FacesContext
     * @param component The PropertySheet object to render
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {

        if (context == null || component == null) {
            throw new NullPointerException();
        }
    }
}
