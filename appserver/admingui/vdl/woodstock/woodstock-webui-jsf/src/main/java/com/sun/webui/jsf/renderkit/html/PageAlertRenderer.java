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
import com.sun.webui.jsf.component.PageAlert;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIParameter;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Renders a full page alert.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.PageAlert"))
public final class PageAlertRenderer extends AbstractRenderer {

    /**
     * Content Page Title Button facet.
     */
    public static final String PAGETITLE_BUTTON_FACET = "pageButtons";

    /**
     * Creates a new instance of MastheadRenderer.
     */
    public PageAlertRenderer() {
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        if (context == null || component == null || writer == null) {
            throw new NullPointerException();
        }

        PageAlert pagealert = (PageAlert) component;
        writer.startElement("div", component);
        addCoreAttributes(context, component, writer, null);
        renderAlert(context, component, writer);
        renderSeparator(context, component, writer);
        renderButtons(context, component, writer);
        writer.endElement("div");
    }

    /**
     * Renders alert summary message.
     *
     * @param context The current FacesContext
     * @param component The Alert object to use
     * @param theme The Theme to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAlertSummary(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        PageAlert pagealert = (PageAlert) component;
        String summary = pagealert.getSummary();
        // If a summary message is not specified, nothing to render.
        if (summary == null || summary.trim().length() == 0) {
            return;
        }

        writer.startElement("div", pagealert);
        // Set the containing div style based on the theme
        String style = theme.getStyleClass(ThemeStyles.ALERT_HEADER_DIV);
        RenderingUtilities.renderStyleClass(context, writer, component, style);
        writer.startElement("span", pagealert);
        style = theme.getStyleClass(ThemeStyles.ALERT_HEADER_TXT);
        writer.writeAttribute("class", style, null);
        renderFormattedMessage(writer, component, context, summary);
        // Close the span, div
        writer.endElement("span");
        writer.endElement("div");

        writer.writeText("\n", null);
    }

    /**
     * Renders detail summary message.
     *
     * @param context The current FacesContext
     * @param component The Alert object to use
     * @param theme The Theme to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAlertDetail(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        PageAlert pagealert = (PageAlert) component;
        String detail = pagealert.getDetail();
        // If a detail message is not specified, nothing to render.
        if (detail == null || detail.trim().length() == 0) {
            return;
        }

        writer.startElement("div", pagealert);
        // Set the containing div style based on the theme
        String style = theme.getStyleClass(ThemeStyles.ALERT_MESSAGE_DIV);
        writer.writeAttribute("class", style, null);
        writer.startElement("span", pagealert);
        style = theme.getStyleClass(ThemeStyles.ALERT_MESSAGE_TEXT);
        writer.writeAttribute("class", style, null);
        renderFormattedMessage(writer, component, context, detail);
        // Close the span, div
        writer.endElement("span");
        writer.endElement("div");
        writer.writeText("\n", null);
    }

    /**
     * Renders PageAlert Icon.
     *
     * @param context The current FacesContext
     * @param component The Alert object to use
     * @param theme The Theme to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAlertIcon(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        PageAlert pagealert = (PageAlert) component;

        writer.startElement("table", component);
        writer.writeAttribute("title", "", null);
        writer.writeAttribute("border", "0", null);
        writer.writeAttribute("cellpadding", "0", null);
        writer.writeAttribute("cellspacing", "0", null);
        writer.writeAttribute("width", "100%", null);
        writer.startElement("tr", component);
        writer.startElement("td", component);
        writer.startElement("div", component);

        UIComponent titleFacet = pagealert
                .getFacet(PageAlert.PAGEALERT_TITLE_FACET);
        if (titleFacet == null) {
            String style;
            style = theme.getStyleClass(ThemeStyles.TITLE_TEXT_DIV);
            writer.writeAttribute("class", style, null);

            writer.startElement("span", component);
            style = theme.getStyleClass(ThemeStyles.TITLE_TEXT);
            writer.writeAttribute("class", style, null);

            // Get the image specified via the type attribute or the image
            // facet.
            UIComponent image = pagealert.getPageAlertImage();
            RenderingUtilities.renderComponent(image, context);
            writer.write(pagealert.getSafeTitle());
            writer.endElement("span");
        } else {
            // Render the title facet
            RenderingUtilities.renderComponent(titleFacet, context);
        }
        writer.endElement("div");
        writer.endElement("td");
        writer.endElement("tr");
        writer.endElement("table");

    }

    /**
     * Renders alert - summary message, detail message and any input components
     * contained in the facet.
     *
     * @param context The current FacesContext
     * @param component The Alert object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAlert(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        PageAlert pagealert = (PageAlert) component;
        // Get the theme
        Theme theme = ThemeUtilities.getTheme(context);
        // Render the Alert summary and detail message
        renderAlertIcon(context, component, theme, writer);
        renderAlertSummary(context, component, theme, writer);
        renderAlertDetail(context, component, theme, writer);

        // Render any input component specified in a facet.
        UIComponent inputComponent = pagealert.getPageAlertInput();

        if (inputComponent != null) {
            writer.startElement("div", pagealert);
            // Set the containing div style based on the theme
            String style = theme.getStyleClass(ThemeStyles.ALERT_FORM_DIV);
            RenderingUtilities.renderStyleClass(context, writer, component,
                    style);
            RenderingUtilities.renderComponent(inputComponent, context);
            writer.endElement("div");
            writer.writeText("\n", null);
        }
    }

    /**
     * Render the separator.
     * @param context faces context
     * @param component UI component
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void renderSeparator(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        PageAlert pageAlert = (PageAlert) component;
        UIComponent separator = pageAlert.getPageAlertSeparator();
        RenderingUtilities.renderComponent(separator, context);
    }

    /**
     * Render the buttons.
     * @param context faces context
     * @param component UI component
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void renderButtons(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        PageAlert pagealert = (PageAlert) component;
        UIComponent buttonFacet = pagealert.getPageAlertButtons();

        if (buttonFacet == null) {
            return;
        }

        // Get the theme
        Theme theme = ThemeUtilities.getTheme(context);
        writer.startElement("table", pagealert);
        writer.writeAttribute("border", "0", null);
        writer.writeAttribute("width", "100%", null);
        writer.writeAttribute("cellpadding", "0", null);
        writer.writeAttribute("cellspacing", "0", null);
        writer.startElement("tr", pagealert);
        String style = theme.getStyleClass(ThemeStyles.TITLE_BUTTON_BOTTOM_DIV);
        writer.startElement("td", pagealert);
        writer.writeAttribute("align", "right", null);
        writer.writeAttribute("nowrap", "nowrap", null);
        writer.startElement("div", pagealert);
        writer.writeAttribute("class", style, null);
        RenderingUtilities.renderComponent(buttonFacet, context);
        writer.endElement("div");
        writer.endElement("td");
        writer.endElement("tr");
        writer.endElement("table");
    }

    /**
     * Render a formatted message.
     * @param writer writer to use
     * @param component component
     * @param context faces context
     * @param msg message to render
     * @throws IOException if an IO error occurs
     */
    private void renderFormattedMessage(final ResponseWriter writer,
            final UIComponent component, final FacesContext context,
            final String msg) throws IOException {

        ArrayList<Object> parameterList = new ArrayList<Object>();
        // get UIParameter children...

        for (UIComponent kid : component.getChildren()) {
            //PENDING(rogerk) ignore if child is not UIParameter?
            if (!(kid instanceof UIParameter)) {
                continue;
            }
            parameterList.add(((UIParameter) kid).getValue());
        }

        // If at least one substitution parameter was specified,
        // use the string as a MessageFormat instance.
        String message;
        if (parameterList.size() > 0) {
            message = MessageFormat.format(msg,
                    parameterList.toArray(new Object[parameterList.size()]));
        } else {
            message = msg;
        }

        if (message != null) {
            PageAlert pa = (PageAlert) component;

            // Check if it the message be HTML escaped (true by default).
            if (!pa.isEscape()) {
                writer.write(message);
            } else {
                writer.writeText(message, "message");
            }
        }
    }
}
