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
import com.sun.webui.jsf.component.Markup;
import com.sun.webui.jsf.util.RenderingUtilities;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class is responsible for rendering any type of XML tag markup.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.Markup"))
public final class MarkupRenderer extends AbstractRenderer {

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        //intentionally empty
        Markup markup = (Markup) component;

        String tagName = markup.getTag();

        if (tagName == null) {
            return; //FIXME: write out log message
        }
        if (!markup.isSingleton()) {
            // Markup is a singleton
            writeInsides(markup, context, writer);
        }
    }

    @Override
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {

        Markup markup = (Markup) component;

        if (!markup.isSingleton()) {
            super.encodeChildren(context, component);
        }
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // End the appropriate element
        Markup markup = (Markup) component;
        String tagName = markup.getTag();
        if (tagName == null) {
            return; //FIXME: write out log message
        }
        if (markup.isSingleton()) {
            // Markup is a singleton
            writeInsides(markup, context, writer);
        }
        writer.endElement(tagName);
    }

    /**
     * Write write insides.
     * @param markup markup component
     * @param context faces context
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void writeInsides(final Markup markup, final FacesContext context,
            final ResponseWriter writer) throws IOException {

        String tagName = markup.getTag();
        writer.startElement(tagName, markup);
        writeId(markup, context, writer);
        writeStyles(markup, context, writer);
        writeExtraAttributes(markup, context, writer);
    }

    /**
     * Write the id.
     * @param markup markup component
     * @param context faces context
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void writeId(final Markup markup, final FacesContext context,
            final ResponseWriter writer) throws IOException {

        String id = markup.getClientId(context);
        if (id != null) {
            writer.writeAttribute("id", id, null);
        }
    }

    /**
     * Write the styles.
     * @param markup markup component
     * @param context faces context
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void writeStyles(final Markup markup, final FacesContext context,
            final ResponseWriter writer) throws IOException {

        String style = markup.getStyle();
        String styleClass = markup.getStyleClass();
        if (style != null) {
            writer.writeAttribute("style", style, null);
        }

        if (styleClass != null) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }
    }

    /**
     * Write extra attributes.
     * @param markup markup component
     * @param context faces context
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void writeExtraAttributes(final Markup markup,
            final FacesContext context, final ResponseWriter writer)
            throws IOException {

        String extra = markup.getExtraAttributes();
        if (extra != null) {
            RenderingUtilities.renderExtraHtmlAttributes(writer, extra);
        }

        Map attributes = markup.getAttributes();

        Set keys = attributes.keySet();

        Iterator listOfKeys = keys.iterator();

        while (listOfKeys.hasNext()) {
            String key = (String) listOfKeys.next();
            Object value = attributes.get(key);

            // Only take strings (their is a private arraylist that I need to
            // avoid
            if (value != null && value instanceof String) {
                writer.writeAttribute(key, value, null);
            }
        }
    }
}
