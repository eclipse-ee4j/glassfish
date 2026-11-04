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
import com.sun.webui.jsf.component.Property;
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
                componentFamily = "com.sun.webui.jsf.Property"))
public final class PropertyRenderer extends Renderer {

    /**
     * Creates a new instance of PropertyRenderer.
     */
    public PropertyRenderer() {
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
        Property property = (Property) component;

        // Get the theme
        Theme theme = ThemeUtilities.getTheme(context);

        // Ideally the PropertyRenderer shouldn't have to know
        // what it is contained by. But this implementation assumes
        // that a table has been used as the container and therefore
        // the responsibility for rendering rows and cells
        // is left to the PropertyRenderer. This is where a
        // LayoutComponent could be useful. The PropertyLayoutComponent
        // knows that a Property consists of a Label and some other
        // components and lays it out accordingly.
        writer.startElement(HTMLElements.TR, property);
        writer.writeAttribute(HTMLAttributes.ID, property.getClientId(context),
                null);

        // Its not clear if the developer realizes that the styleClass
        // and style attributes are applied to a "tr".
        String propValue = RenderingUtilities.getStyleClasses(context,
                component, null);
        if (propValue != null) {
            writer.writeAttribute(HTMLAttributes.CLASS, propValue, null);
        }

        propValue = property.getStyle();
        if (propValue != null) {
            writer.writeAttribute(HTMLAttributes.STYLE, propValue,
                    HTMLAttributes.STYLE);
        }

        writer.startElement(HTMLElements.TD, property);
        writer.writeAttribute(HTMLAttributes.VALIGN, "top", null);

        // Always render the nowrap, and labelAlign even if there
        // isn't a label.
        boolean nowrap = property.isNoWrap();
        if (nowrap) {
            writer.writeAttribute(HTMLAttributes.NOWRAP, "nowrap", null);
        }
        String labelAlign = property.getLabelAlign();
        if (labelAlign != null) {
            writer.writeAttribute(HTMLAttributes.ALIGN, labelAlign, null);
        }

        // If overlapLabel is true, then render the TD with colspan 2.
        // and the property components share the div.
        // If overlapLabel is false then render two TD's and two DIV's
        //
        boolean overlapLabel = property.isOverlapLabel();
        if (overlapLabel) {
            writer.writeAttribute(HTMLAttributes.COLSPAN, "2", null);
        }

        writer.startElement(HTMLElements.DIV, property);
        writer.writeAttribute(HTMLAttributes.CLASS,
                theme.getStyleClass(ThemeStyles.CONTENT_TABLE_COL1_DIV), null);

        renderLabel(context, property, theme, writer);

        // If overlapLabel is false, close the label TD and DIV
        // and open another TD and DIV for the property components.
        //
        if (!overlapLabel) {
            writer.endElement(HTMLElements.DIV);
            writer.endElement(HTMLElements.TD);
            writer.startElement(HTMLElements.TD, property);
            writer.startElement(HTMLElements.DIV, property);
            writer.writeAttribute(HTMLAttributes.CLASS,
                    theme.getStyleClass(ThemeStyles.CONTENT_TABLE_COL2_DIV),
                    null);
        }

        renderPropertyComponents(context, property, theme, writer);
        renderHelpText(context, property, theme, writer);

        writer.endElement(HTMLElements.DIV);
        writer.endElement(HTMLElements.TD);
        writer.endElement(HTMLElements.TR);
    }

    /**
     * Render the property components. If the {@code content} facet it
     * defined it takes precedence over the existence of child components. If
     * there is not {@code content} facet, then the children are rendered.
     *
     * @param context The current FacesContext
     * @param property The Property object to render
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderPropertyComponents(final FacesContext context,
            final Property property, final Theme theme,
            final ResponseWriter writer) throws IOException {

        // The presence of a content facet takes priority over
        // children. I'm not sure why there is both a content facet
        // and children.
        UIComponent content = property.getContentComponent();
        if (content != null) {
            RenderingUtilities.renderComponent(content, context);
            return;
        }

        // If there isn't a content facet, render any children.
        List children = property.getChildren();
        for (Object child : children) {
            RenderingUtilities.renderComponent((UIComponent) child, context);
        }
    }

    /**
     * Render a label for the property.
     *
     * @param context The current FacesContext
     * @param property The Property object to render
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderLabel(final FacesContext context,
            final Property property, final Theme theme,
            final ResponseWriter writer) throws IOException {

        // Render a label facet if there is one, else a span
        // and "&nbsp".
        UIComponent label = property.getLabelComponent();
        if (label != null) {
            RenderingUtilities.renderComponent(label, context);
            return;
        }

        writer.startElement(HTMLElements.SPAN, property);

        // The class selector should be minimally themeable.
        // Not sure why we even need it for "nbsp".
        // Other components offer a labelLevel attribute as well.
        writer.writeAttribute(HTMLAttributes.CLASS,
                theme.getStyleClass(ThemeStyles.LABEL_LEVEL_TWO_TEXT), null);
        writer.write("&nbsp;");
        writer.endElement(HTMLElements.SPAN);
    }

    /**
     * Render help text for the property.
     *
     * @param context The current FacesContext
     * @param property The Property object to render
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderHelpText(final FacesContext context,
            final Property property, final Theme theme,
            final ResponseWriter writer) throws IOException {

        // Render a help text facet if there is one
        UIComponent helpText = property.getHelpTextComponent();
        if (helpText != null) {
            RenderingUtilities.renderComponent(helpText, context);
        }
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {

        if (context == null || component == null) {
            throw new NullPointerException();
        }
    }
}
