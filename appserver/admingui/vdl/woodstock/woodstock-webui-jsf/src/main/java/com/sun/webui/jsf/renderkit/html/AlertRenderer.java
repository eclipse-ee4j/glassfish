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
import com.sun.webui.jsf.component.Alert;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.CookieUtils;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIParameter;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.beans.Beans;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.sun.webui.jsf.util.RenderingUtilities.isPortlet;
import static com.sun.webui.jsf.util.RenderingUtilities.renderComponent;
import static com.sun.webui.jsf.util.RenderingUtilities.renderStyleClass;
import static com.sun.webui.jsf.util.ThemeUtilities.getTheme;

/**
 * Renderer for an {@link Alert} component.
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.Alert"))
public final class AlertRenderer extends AbstractRenderer {

    /**
     * Creates a new instance of AlertRenderer.
     */
    public AlertRenderer() {
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // Render end of alert
        Alert alert = (Alert) component;
        String summary = alert.getSummary();

        // If a summary message is not specified, nothing to render.
        if (summary == null || summary.trim().length() == 0) {
            return;
        }

        // Get the theme
        Theme theme = getTheme(context);
        String spacerPath = theme.getImagePath(ThemeImages.DOT);
        String[] styles = getStyles(theme);

        // Neither Portlet nor design time supports HttpServletResponse.
        //
        // In the portler case, we need to understand if
        // ExternalContext does support the cookie interfaces.
        // Then only the isDesignTime condition must be suppoted.
        // If the ExternalContext instance is specific to design time
        // and behaves appropriately, then the
        // isDesignTime test will not be needed either.

        if (!isPortlet(context)
                && !Beans.isDesignTime()) {
            // Reset the scroll position by creating a new cookie
            // and setting its value to null. This would override
            // the cookie that the javascript code creates. CR 6251724.
            HttpServletResponse response
                    = (HttpServletResponse) context.getExternalContext()
                            .getResponse();
            String viewId = context.getViewRoot().getViewId();
            String urlString = context.getApplication().getViewHandler().
                    getActionURL(context, viewId);
            // Get this after we calculate the urlString...
            viewId = CookieUtils.getValidCookieName(viewId);
            Cookie cookie = new Cookie(viewId, "");
            cookie.setPath(urlString);
            response.addCookie(cookie);
        }

        // Render the outer div that wraps the alert
        // Get the default style for the alert
        String defaultStyleClass = theme.getStyleClass(ThemeStyles.ALERT_DIV);
        renderOuterDiv(context, alert, defaultStyleClass, writer);

        // Render the opening table
        renderOpeningTable(alert, writer);

        // Render the top row
        renderTopRow(alert, spacerPath, styles, writer);

        // Render the middle row
        renderMiddleRow(alert, spacerPath, styles, context, writer);

        // Render the bottom row
        renderBottomRow(alert, theme, spacerPath, styles, writer);

        // Render the closing tags
        renderClosingTags(writer);
    }

    /**
     * Renders the outer div which contains the alert.
     *
     * @param context The current FacesContext
     * @param alert The Alert object to use
     * @param defaultStyleClass The styleClass to use if not set on alert
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected static void renderOuterDiv(final FacesContext context,
            final Alert alert, final String defaultStyleClass,
            final ResponseWriter writer) throws IOException {

        String style = alert.getStyle();
        String id = alert.getClientId(context);

        writer.startElement("div", alert);

        // Write a id only if a style/class was specified?
        if (id != null) {
            writer.writeAttribute("id", id, null);
        }

        if (style != null) {
            writer.writeAttribute("style", style, null);
        }

        // Even though renderStyleClass obtains the component's
        // styleClass attribute, look for it here too. If it exists
        // do not pass the defaultStyleClass.
        //
        String styleClass = alert.getStyleClass();
        if (styleClass == null || styleClass.length() == 0) {
            styleClass = defaultStyleClass;
        } else {
            // Don't pass it or else it will get added twice
            styleClass = null;
        }
        renderStyleClass(context, writer, (UIComponent) alert, styleClass);
    }

    /**
     * Renders the attributes for the outer table containing the inline alert.
     * FIXME: Use div instead of tables for layout as soon as I can find a
     * solution that works for IE.
     *
     * @param alert The Alert object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderOpeningTable(final Alert alert,
            final ResponseWriter writer) throws IOException {

        writer.startElement("table", alert);
        writer.writeAttribute("border", "0", null);
        writer.writeAttribute("cellspacing", "0", null);
        writer.writeAttribute("cellpadding", "0", null);
        writer.writeAttribute("title", "", null);
        writer.writeText("\n", null);
    }

    /**
     * Renders the outer table top row containing three spacer columns.
     *
     * @param alert The Alert object to use
     * @param spacerPath The path to the spacer image
     * @param styles The array of styles
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderTopRow(final Alert alert, final String spacerPath,
            final String[] styles, final ResponseWriter writer)
            throws IOException {

        writer.startElement("tr", alert);
        writer.writeText("\n", null);
        renderSpacerCell(alert, styles[0], spacerPath, writer);
        renderSpacerCell(alert, styles[1], spacerPath, writer);
        renderSpacerCell(alert, styles[2], spacerPath, writer);
        writer.endElement("tr");
        writer.writeText("\n", null);
    }

    /**
     * Renders the outer table middle row containing two spacer columns and
     * a column that holds the alert.
     *
     * @param alert The Alert object to use
     * @param spacerPath The path to the spacer image
     * @param styles The array of styles
     * @param context The current FacesContext
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderMiddleRow(final Alert alert,
            final String spacerPath, final String[] styles,
            final FacesContext context, final ResponseWriter writer)
            throws IOException {

        writer.startElement("tr", alert);
        writer.writeAttribute("class", styles[3], null);
        writer.writeText("\n", null);

        // render a spacer in the left column
        writer.startElement("td", alert);
        writer.writeAttribute("class", styles[4], null);
        writer.writeText("\n", null);
        renderSpacerImage(alert, spacerPath, writer);
        writer.endElement("td");
        writer.writeText("\n", null);

        // render middle column
        renderMiddleCell(context, alert, writer, styles);

        // render a spacer in the right column
        writer.startElement("td", alert);
        writer.writeAttribute("class", styles[9], null);
        writer.writeText("\n", null);
        renderSpacerImage(alert, spacerPath, writer);
        writer.endElement("td");
        writer.writeText("\n", null);

        // close the middle row
        writer.endElement("tr");
        writer.writeText("\n", null);
    }

    /**
     * Renders the outer table bottom row containing three spacer columns.
     *
     * @param alert The Alert object to use
     * @param theme The Theme to use
     * @param spacerPath spacer path
     * @param styles CSS style
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static void renderBottomRow(final Alert alert, final Theme theme,
            final String spacerPath, final String[] styles,
            final ResponseWriter writer) throws IOException {

        writer.startElement("tr", alert);
        writer.writeText("\n", null);
        renderSpacerCell(alert, styles[10], spacerPath, writer);
        renderSpacerCell(alert, styles[11], spacerPath, writer);
        renderSpacerCell(alert, styles[12], spacerPath, writer);
        writer.endElement("tr");
        writer.writeText("\n", null);
    }

    /**
     * Renders a spacer coulmn.
     *
     * @param alert The Alert object to use
     * @param styleClass CSS class
     * @param spacerPath The path to the spacer image
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    private static void renderSpacerCell(final Alert alert,
            final String styleClass, final String spacerPath,
            final ResponseWriter writer) throws IOException {

        writer.startElement("td", alert);
        writer.writeText("\n", null);
        writer.startElement("div", alert);
        writer.writeAttribute("class", styleClass, null);
        renderSpacerImage(alert, spacerPath, writer);
        writer.endElement("div");
        writer.writeText("\n", null);
        writer.endElement("td");
        writer.writeText("\n", null);
    }

    /**
     * Renders the middle column  containing the alert icon, summary,
     * detail message and the optional link.
     *
     * @param context The current FacesContext
     * @param alert The Alert object to use
     * @param writer The current ResponseWriter
     * @param styles CSS styles
     *
     * @exception IOException if an input/output error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderMiddleCell(final FacesContext context,
            final Alert alert, final ResponseWriter writer,
            final String[] styles) throws IOException {

        writer.startElement("td", alert);
        writer.writeText("\n", null);

        // open the outer div containing the summary and detial areas
        writer.startElement("div", alert);
        writer.writeAttribute("class", styles[5], null);

        // open the alert header div
        writer.startElement("div", alert);
        writer.writeAttribute("class", styles[6], null);

        // Render the alert icon
        renderAlertIcon(context, alert);
        // Render the summary text
        renderAlertSummaryText(alert, styles, writer);

        // close the alert header div
        writer.endElement("div");
        writer.writeText("\n", null);

        // Render the detailed text and the optional link
        renderAlertDetailArea(context, alert, styles, writer);

        // Close the outer div
        writer.endElement("div");
        writer.writeText("\n", null);

        // Close the cell
        writer.endElement("td");
        writer.writeText("\n", null);
    }

    /**
     * Renders the icon associated with an inline alert message.
     *
     * @param context The current FacesContext
     * @param alert The Alert object to use
     *
     * @exception IOException if an input/output error occurs
     */
    private static void renderAlertIcon(final FacesContext context,
            final Alert alert) throws IOException {

        UIComponent alertIcon = alert.getAlertIcon();
        renderComponent(alertIcon, context);
    }

    /**
     * Renders the summary message of the inline alert.
     *
     * @param alert The Alert object to use
     * @param styles CSS styles
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static void renderAlertSummaryText(final Alert alert,
            final String[] styles, final ResponseWriter writer)
            throws IOException {

        // Render the summary text
        String summary = alert.getSummary();

        if (summary != null) {
            writer.startElement("span", alert);
            writer.writeAttribute("class", styles[7], null);
            writer.writeText("\n", null);
            renderFormattedMessage(writer, alert, summary);
            writer.endElement("span");
            writer.writeText("\n", null);
        }
    }

    /**
     * Renders the optional detail message of the inline alert.
     * Also renders the optional link.
     *
     * @param context faces context
     * @param alert The Alert object to use
     * @param styles CSS styles
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderAlertDetailArea(final FacesContext context,
            final Alert alert, final String[] styles,
            final ResponseWriter writer) throws IOException {

        // Get the detail text
        String detail = alert.getDetail();

        // Get the children, if any.
        List children = alert.getChildren();
        if ((detail == null
                || detail.trim().length() == 0)
                && children.size() <= 0) {
            return;
        }

        // Set the style
        writer.startElement("div", alert);
        writer.writeAttribute("class", styles[8], null);

        // Check if it should be HTML escaped (true by default).
        if (detail != null) {
            writer.startElement("span", alert);
            writer.writeAttribute("class", styles[7], null);

            //Added to force screen reader to read alert box incase of
            // deployment error
            writer.writeAttribute("role", "alert", null);

            writer.writeText("\n", null);
            renderFormattedMessage(writer, alert, detail);
            writer.endElement("span");
            writer.writeText("\n", null);
        }

        // render any children
        super.encodeChildren(context, alert);

        // Render the optional link, if specified
        renderAlertLink(context, alert);

        // Close the div
        writer.endElement("div");
        writer.writeText("\n", null);
    }

    /**
     * Renders the optional link at the end of the alert.
     *
     * @param context The current FacesContext
     * @param alert The Alert object to use
     *
     * @exception IOException if an input/output error occurs
     */
    private static void renderAlertLink(final FacesContext context,
            final Alert alert)
            throws IOException {

        UIComponent link = alert.getAlertLink();
        if (link != null) {
            renderComponent(link, context);
        }
    }

    /**
     * Renders the optional detail message of the inline alert.
     *
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    private static void renderClosingTags(final ResponseWriter writer)
            throws IOException {

        writer.endElement("table");
        writer.endElement("div");
        writer.writeText("\n", null);
    }

    /**
     * Render a formatter message.
     * @param writer writer to use
     * @param component UI component
     * @param msg message to format
     * @throws IOException if an IO error occurs
     */
    private static void renderFormattedMessage(final ResponseWriter writer,
            final UIComponent component, final String msg) throws IOException {

        ArrayList<Object> parameterList = new ArrayList<Object>();
        // get UIParameter children...
        for (UIComponent kid : component.getChildren()) {
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
            writer.write(message);
        }
    }

    /**
     * Renders a spacer image.
     * @param alert alert component
     * @param spacerPath spacer path
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private static void renderSpacerImage(final Alert alert,
            final String spacerPath, final ResponseWriter writer)
            throws IOException {

        writer.startElement("img", alert);
        writer.writeAttribute("src", spacerPath, null);
        writer.writeAttribute("alt", "", null);
        writer.endElement("img");
    }

    /**
     * Get the CSS style.
     * @param theme theme to use
     * @return String[]
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static String[] getStyles(final Theme theme) {
        String[] styles = new String[13];
        styles[0] = theme.getStyleClass(ThemeStyles.ALERT_TOP_LEFT_CORNER);
        styles[1] = theme.getStyleClass(ThemeStyles.ALERT_TOP_MIDDLE);
        styles[2] = theme.getStyleClass(ThemeStyles.ALERT_TOP_RIGHT_CORNER);
        styles[3] = theme.getStyleClass(ThemeStyles.ALERT_MIDDLE_ROW);
        styles[4] = theme.getStyleClass(ThemeStyles.ALERT_LEFT_MIDDLE);
        styles[5] = theme.getStyleClass(ThemeStyles.ALERT_MIDDLE);
        styles[6] = theme.getStyleClass(ThemeStyles.ALERT_HEADER);
        styles[7] = theme.getStyleClass(ThemeStyles.ALERT_TEXT);
        styles[8] = theme.getStyleClass(ThemeStyles.ALERT_DETAILS);
        styles[9] = theme.getStyleClass(ThemeStyles.ALERT_RIGHT_MIDDLE);
        styles[10] = theme.getStyleClass(ThemeStyles.ALERT_BOTTOM_LEFT_CORNER);
        styles[11] = theme.getStyleClass(ThemeStyles.ALERT_BOTTOM_MIDDLE);
        styles[12] = theme.getStyleClass(ThemeStyles.ALERT_BOTTOM_RIGHT_CORNER);
        return styles;
    }
}
