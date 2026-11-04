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
import com.sun.webui.jsf.component.Hyperlink;
import com.sun.webui.jsf.component.Icon;
import com.sun.webui.jsf.component.ImageComponent;
import com.sun.webui.jsf.component.Masthead;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Renders a Masthead component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.Masthead"))
public class MastheadRenderer extends AbstractRenderer {

    /**
     * skip utility property.
     */
    private static final String SKIP_UTILITY = "skipUtility";

    /**
     * Creates a new instance of MastheadRenderer.
     */
    public MastheadRenderer() {
    }

    /**
     * Render the current alarms info for the status area.
     *
     * @param context The current FacesContext
     * @param masthead The Masthead component
     * @param writer The current ResponseWriter
     * @param alarms An int[] containing the number of down, critical, major and
     * minor alarms (in that order)
     * @param theme The current Theme
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    protected void renderAlarmsInfo(final FacesContext context,
            final Masthead masthead, final ResponseWriter writer,
            final int[] alarms, final Theme theme)
            throws IOException {

        writer.startElement(HTMLElements.TD, masthead);
        writer.writeAttribute(HTMLAttributes.CLASS, theme.getStyleClass(
                ThemeStyles.MASTHEAD_ALARM_DIV), null);

        UIComponent alarmsFacet
                = masthead.getFacet("currentAlarmsInfo");

        if (alarmsFacet != null) {
            RenderingUtilities.renderComponent(alarmsFacet, context);
        } else {
            writer.startElement(HTMLElements.SPAN, masthead);
            writer.writeAttribute(HTMLAttributes.CLASS, theme.getStyleClass(
                    ThemeStyles.MASTHEAD_LABEL), null);
            writer.write(theme.getMessage("masthead.currentAlarms"));
            writer.endElement(HTMLElements.SPAN);
            writer.write("&nbsp;&nbsp;");
            // output the down alarm count
            writeAlarmCount(writer, context,
                    ThemeImages.ALARM_MASTHEAD_DOWN_MEDIUM,
                    "Alarm.downImageAltText", masthead,
                    ThemeStyles.MASTHEAD_ALARM_DOWN_TEXT, alarms[0], theme);

            // output the critical alarm count
            writeAlarmCount(writer, context,
                    ThemeImages.ALARM_MASTHEAD_CRITICAL_MEDIUM,
                    "Alarm.criticalImageAltText", masthead,
                    ThemeStyles.MASTHEAD_ALARM_CRITICAL_TEXT, alarms[1], theme);

            // output the major alarm count
            writeAlarmCount(writer, context,
                    ThemeImages.ALARM_MASTHEAD_MAJOR_MEDIUM,
                    "Alarm.majorImageAltText", masthead,
                    ThemeStyles.MASTHEAD_ALARM_MAJOR_TEXT, alarms[2], theme);

            // output the minor alarm count
            writeAlarmCount(writer, context,
                    ThemeImages.ALARM_MASTHEAD_MINOR_MEDIUM,
                    "Alarm.minorImageAltText", masthead,
                    ThemeStyles.MASTHEAD_ALARM_MINOR_TEXT, alarms[3], theme);
        }

        writer.endElement(HTMLElements.TD);
    }

    /**
     * Render the current application info in a table divider. This typically
     * consists of information about the current user, role (if any) and server.
     *
     * @param context The current FacesContext
     * @param masthead The Masthead component
     * @param writer The current ResponseWriter
     * @param theme The current Theme
     * @throws IOException if an IO error occurs
     */
    protected void renderApplicationInfo(final FacesContext context,
            final Masthead masthead, final ResponseWriter writer,
            final Theme theme)
            throws IOException {

        // render the the application details in a single table divider
        writer.startElement(HTMLElements.TD, masthead);
        writer.writeAttribute(HTMLAttributes.CLASS,
                theme.getStyleClass(ThemeStyles.MASTHEAD_TD_TITLE), null);
        writer.writeAttribute(HTMLAttributes.WIDTH, "99%", null);

        renderUserInfo(context, masthead, writer, theme);

        // now render the product name info / image
        renderProductInfo(context, masthead, writer,
                theme.getStyleClass(ThemeStyles.MASTHEAD_DIV_TITLE));

        // close the app info table divider
        writer.endElement(HTMLElements.TD);
    }

    /**
     * Render the date time information in the masthead status area.
     *
     * @param context The current FacesContext
     * @param masthead The current Masthead instance
     * @param writer The ResponseWriter to use
     * @param theme The current Theme
     * @throws IOException if an IO error occurs
     */
    protected void renderDateTimeInfo(final FacesContext context,
            final Masthead masthead, final ResponseWriter writer,
            final Theme theme) throws IOException {

        writer.startElement(HTMLElements.TD, masthead);
        writer.writeAttribute(HTMLAttributes.CLASS,
                theme.getStyleClass(ThemeStyles.MASTHEAD_TIME_DIV), null);

        UIComponent timeStampFacet
                = masthead.getFacet("dateTimeInfo");
        if (timeStampFacet != null) {
            RenderingUtilities.renderComponent(timeStampFacet, context);
        } else {
            String textStyle = theme.getStyleClass(ThemeStyles.MASTHEAD_LABEL);

            // display the current time and date
            writer.startElement(HTMLElements.SPAN, masthead);
            writer.writeAttribute(HTMLAttributes.CLASS, textStyle, null);
            writer.write(theme.getMessage("masthead.lastUpdate"));
            writer.endElement(HTMLElements.SPAN);
            writer.write("&nbsp;");
            writer.startElement(HTMLElements.SPAN, masthead);
            writer.writeAttribute(HTMLAttributes.CLASS,
                    theme.getStyleClass(ThemeStyles.MASTHEAD_TEXT), null);

            // FIXME: This date formatting should be in the theme.
            DateFormat dateFormat = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM, DateFormat.LONG,
                    context.getViewRoot().getLocale());

            writer.write(dateFormat.format(new Date()));
            writer.endElement(HTMLElements.SPAN);
            writer.write("&nbsp;");
        }

        writer.endElement(HTMLElements.TD);
    }

    /**
     * Render the jobs running info. If the "jobsInfo" facet was specified, this
     * should be rendered inside of the appropriate div tag. If not the standard
     * jobs running image and "Jobs Running: x" label should be displayed.
     *
     * @param context The current FacesContext
     * @param masthead The Masthead component
     * @param writer The current ResponseWriter
     * @param divIsOpen If true the div tag to output the jobsInfo in is already
     * open. If false we need to open the div before outputting anything
     * @param theme The current Theme
     * @throws IOException if an IO error occurs
     */
    protected void renderJobsInfo(final FacesContext context,
            final Masthead masthead, final ResponseWriter writer,
            final boolean divIsOpen, final Theme theme) throws IOException {

        writer.startElement(HTMLElements.TD, masthead);
        writer.writeAttribute(HTMLAttributes.CLASS,
                theme.getStyleClass(ThemeStyles.MASTHEAD_STATUS_DIV), null);

        UIComponent jobsFacet = masthead.getFacet("jobsInfo");

        if (jobsFacet != null) {
            RenderingUtilities.renderComponent(jobsFacet, context);
        } else {

            Icon icon = ThemeUtilities.getIcon(theme,
                    ThemeImages.MASTHEAD_STATUS_ICON);
            icon.setId(masthead.getId() + "_jobStatusImage");
            icon.setAlt(
                    theme.getMessage("masthead.tasksRunningAltText"));
            icon.setAlign("top");
            icon.setBorder(0);

            RenderingUtilities.renderComponent(icon, context);

            writer.write("&nbsp;");
            Hyperlink hl = (Hyperlink) masthead.getJobCountLink();
            RenderingUtilities.renderComponent(hl, context);
        }
        writer.endElement(HTMLElements.TD);
    }

    /**
     * Render the notification info for the given masthead component.
     *
     * @param context The current FacesContext
     * @param masthead The Masthead component instance
     * @param writer The ResponseWriter to use
     * @param leaveDivOpen If true the div enclosing the notification info
     * should not be closed
     * @param theme The current Theme
     * @throws IOException if an IO error occurs
     */
    protected void renderNotificationInfo(final FacesContext context,
            final Masthead masthead, final ResponseWriter writer,
            final boolean leaveDivOpen, final Theme theme) throws IOException {

        // display the specified notification message or facet
        writer.startElement(HTMLElements.TD, masthead);
        writer.writeAttribute(HTMLAttributes.CLASS,
                theme.getStyleClass(ThemeStyles.MASTHEAD_TABLE_END), null);

        UIComponent notificationFacet
                = masthead.getFacet("notificationInfo");

        if (notificationFacet != null) {
            RenderingUtilities.renderComponent(notificationFacet, context);
        } else {
            ImageComponent image = new ImageComponent();
            image.setId(masthead.getId() + "_notificationInfo");
            image.setParent(masthead);
            image.setIcon(ThemeImages.MASTHEAD_STATUS_ICON);

            image.setAlign("top");
            image.setBorder(0);
            image.setAlt(theme.getMessage("Alert.infoImageAltText"));

            RenderingUtilities.renderComponent(image, context);

            writer.write("&nbsp;");
            writer.startElement(HTMLElements.SPAN, masthead);
            writer.writeAttribute(HTMLAttributes.CLASS,
                    theme.getStyleClass(ThemeStyles.MASTHEAD_LABEL), null);
            writer.write(masthead.getNotificationMsg());
            writer.endElement(HTMLElements.SPAN);
        }
        writer.endElement(HTMLElements.TD);

    }

    /**
     * Render the product info as an image in the appropriate div tag.
     *
     * @param context The current FacesContext
     * @param masthead The Masthead component
     * @param writer The current ResponseWriter
     * @param styleClass style class
     * @throws IOException if an IO error occurs
     */
    protected void renderProductInfo(final FacesContext context,
            final Masthead masthead, final ResponseWriter writer,
            final String styleClass) throws IOException {

        UIComponent productImage = getProductImage(context, masthead,
                ThemeUtilities.getTheme(context));
        if (productImage == null) {
            return;
        }

        // render the product name image
        writer.startElement(HTMLElements.DIV, masthead);
        if (styleClass != null && styleClass.length() > 0) {
            writer.writeAttribute(HTMLAttributes.CLASS, styleClass, null);
        }
        RenderingUtilities.renderComponent(productImage, context);
        writer.endElement(HTMLElements.DIV);
    }

    /**
     * This implementation renders the masthead component.
     * @param context faces context
     * @param component UI component
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Masthead masthead = (Masthead) component;
        Theme theme = ThemeUtilities.getTheme(context);

        // check if it is a secondary masthead
        if (masthead.isSecondary()) {
            renderSecondaryMasthead(context, masthead, theme, writer);
        } else {
            renderPrimaryMasthead(context, masthead, theme, writer);
        }

    }

    /**
     * Render the status area in a table divider.
     *
     * @param context The current FacesContext
     * @param masthead The Masthead component
     * @param writer The current ResponseWriter
     * @param theme The current Theme
     * @throws IOException if an IO error occurs
     */
    protected void renderStatusArea(final FacesContext context,
            final Masthead masthead, final ResponseWriter writer,
            final Theme theme) throws IOException {

        UIComponent statusArea = masthead.getFacet("statusArea");
        if (statusArea != null) {
            // render the start of the bottom table
            startTable(writer, masthead, theme.getStyleClass(
                    ThemeStyles.MASTHEAD_TABLE_END));
            writer.startElement(HTMLElements.TR, masthead);
            writer.startElement(HTMLElements.TD, masthead);
            // get the text and label styles from the theme
            String labelStyle = theme.getStyleClass(ThemeStyles.MASTHEAD_LABEL);
            String textStyle = theme.getStyleClass(ThemeStyles.MASTHEAD_TEXT);

            RenderingUtilities.renderComponent(statusArea, context);

            writer.endElement(HTMLElements.TD);
            writer.endElement(HTMLElements.TR);
            writer.endElement(HTMLElements.TABLE);
        } else {
            renderStatusAreaComponents(context, masthead, theme, writer);
        }
    }

    /**
     * Render the current user information in the appropriate div tag.
     *
     * @param context The current FacesContext
     * @param masthead The Masthead component
     * @param writer The current ResponseWriter
     * @param theme The current Theme
     * @throws IOException if an IO error occurs
     */
    protected void renderUserInfo(final FacesContext context,
            final Masthead masthead, final ResponseWriter writer,
            final Theme theme) throws IOException {

        // retrieve the label and text styles for the current theme
        String labelStyle = theme.getStyleClass(ThemeStyles.MASTHEAD_LABEL);
        String textStyle = theme.getStyleClass(ThemeStyles.MASTHEAD_TEXT);

        // render the user, role (if any) and server details in a div
        writer.startElement(HTMLElements.DIV, masthead);
        writer.writeAttribute(HTMLAttributes.CLASS,
                theme.getStyleClass(ThemeStyles.MASTHEAD_DIV_USER), null);

        // Create a separator for the following methods.
        ImageComponent separator = new ImageComponent();
        separator.setParent(masthead);
        separator.setIcon(ThemeImages.MASTHEAD_SEPARATOR);
        // GF-required 508 change
        separator.setAlt(theme.getMessage("masthead.statusAreaSepAltText"));

        renderUserInfo(context, masthead, theme, labelStyle, textStyle,
                separator, writer);

        // Renders the separator if it needs to.
        renderRoleInfo(context, masthead, theme, labelStyle, textStyle,
                separator, writer);

        renderServerInfo(context, masthead, theme, labelStyle,
                textStyle, writer);

        writer.endElement(HTMLElements.DIV);
    }

    /**
     * Render the utility bar in a table row. Note that if there is a
     * {@code utilityBar} it is expected to render an opening and closing
     * {@code tr} element.
     *
     * @param context The current FacesContext
     * @param masthead The Masthead component
     * @param writer The current ResponseWriter
     * @param theme The current Theme
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings({
        "checkstyle:magicnumber",
        "checkstyle:methodlength"
    })
    protected void renderUtilityBar(final FacesContext context,
            final Masthead masthead, final ResponseWriter writer,
            final Theme theme) throws IOException {

        UIComponent facet = masthead.getFacet("utilityBar");
        if (facet != null) {
            RenderingUtilities.renderComponent(facet, context);
            return;
        }

        // render the the utility bar in a table row
        writer.startElement(HTMLElements.TR, masthead);

        // render the console & version facets (if necessary) in a table divider
        writer.startElement(HTMLElements.TD, masthead);
        writer.writeAttribute("nowrap", "nowrap", null);

        String styleName;
        // DO NOT HARD CODE STYLES!!!
        String buttonClassName
                = theme.getStyleClass(ThemeStyles.MASTHEAD_BUTTON);

        // render the console facet if specified
        facet = masthead.getFacet("consoleLink");
        boolean consoleLinkDisplayed = facet != null;
        if (consoleLinkDisplayed) {
            writer.startElement(HTMLElements.DIV, masthead);
            writer.writeAttribute(HTMLAttributes.CLASS, buttonClassName, null);

            styleName = ThemeStyles.MASTHEAD_LINK;
            setAttrs(facet, "MastheadConsoleLink", masthead,
                    theme.getMessage("masthead.consoleLabel"),
                    theme.getStyleClass(styleName),
                    theme.getMessage("masthead.consoleTooltip"),
                    theme.getMessage("masthead.consoleStatus"));

            RenderingUtilities.renderComponent(facet, context);
            writer.endElement(HTMLElements.DIV);
            appendDotImage(writer, context, masthead, "_utilPad", 1, 8, theme);
        }

        // render the version facet if specified
        facet = masthead.getFacet("versionLink");
        if (facet != null) {

            writer.startElement(HTMLElements.DIV, masthead);
            writer.writeAttribute(HTMLAttributes.CLASS, buttonClassName, null);

            if (consoleLinkDisplayed) {
                styleName = ThemeStyles.MASTHEAD_LINK_RIGHT;
            } else {
                styleName = ThemeStyles.MASTHEAD_LINK;
            }
            String styleClass = theme.getStyleClass(styleName);

            setAttrs(facet, "MastheadVersionLink", masthead,
                    theme.getMessage("masthead.versionLabel"),
                    styleClass,
                    theme.getMessage("masthead.versionTooltip"),
                    theme.getMessage("masthead.versionStatus"));

            RenderingUtilities.renderComponent(facet, context);
            writer.endElement(HTMLElements.DIV);
        }

        // close the console / version table divider
        writer.endElement(HTMLElements.TD);

        // if specified, render the search, logout and help facets as well as
        // any other developer specified links
        writer.startElement(HTMLElements.TD, masthead);
        writer.writeAttribute(HTMLAttributes.ALIGN, "right", null);
        writer.writeAttribute(HTMLAttributes.VALIGN, "bottom", null);

        String leftLinkStyle
                = theme.getStyleClass(ThemeStyles.MASTHEAD_LINK_LEFT);
        String centerLinkStyle
                = theme.getStyleClass(ThemeStyles.MASTHEAD_LINK_CENTER);
        String rightLinkStyle
                = theme.getStyleClass(ThemeStyles.MASTHEAD_LINK_RIGHT);
        String singleLinkStyle
                = theme.getStyleClass(ThemeStyles.MASTHEAD_LINK);

        // determine what optional elements are being displayed
        UIComponent logoutFacet = masthead.getFacet("logoutLink");
        UIComponent helpFacet = masthead.getFacet("helpLink");
        UIComponent searchFacet = masthead.getFacet("search");
        Hyperlink[] extraLinks = masthead.getUtilities();

        boolean logoutLinkDisplayed = logoutFacet != null;
        boolean helpLinkDisplayed = helpFacet != null;
        boolean areExtraLinks = extraLinks != null;

        // first render the search facet if specified
        if (searchFacet != null) {
            // render the search facet
            RenderingUtilities.renderComponent(searchFacet, context);
            // now render the separator image if any other elements to the right
            if (logoutLinkDisplayed || helpLinkDisplayed || areExtraLinks) {
                ImageComponent separator = new ImageComponent();
                separator.setId("searchSeparator");
                separator.setIcon(ThemeImages.MASTHEAD_SEPARATOR_BUTTONS);
                RenderingUtilities.renderComponent(separator, context);
            }
        }

        // render any developer specified links if necessary
        if (areExtraLinks) {
            boolean extraLinksOnly
                    = !(logoutLinkDisplayed || helpLinkDisplayed);

            // Don't use appendDotImage in a loop, we can reuse the
            // same icon component.
            StringBuilder sbId = new StringBuilder("_lp");
            int len = sbId.length();
            Icon dot = ThemeUtilities.getIcon(theme, ThemeImages.DOT);
            dot.setParent(masthead);
            dot.setWidth(8);
            dot.setHeight(1);
            dot.setBorder(0);
            dot.setAlt("");

            // render any developer specifed custom links
            List children = masthead.getChildren();
            for (int i = 0; i < extraLinks.length; i++) {
                Hyperlink link = extraLinks[i];
                if (link.getParent() == null) {
                    link.setParent(masthead);
                }
                writer.startElement(HTMLElements.DIV, masthead);
                writer.writeAttribute(HTMLAttributes.CLASS,
                        buttonClassName, null);
                RenderingUtilities.renderComponent(link, context);
                writer.endElement(HTMLElements.DIV);
                dot.setId(sbId.append(Integer.toString(i)).toString());
                sbId.setLength(len);
                RenderingUtilities.renderComponent(dot, context);
            }
        }

        // now render the logoutLink facet if specified
        if (logoutLinkDisplayed) {
            writer.startElement(HTMLElements.DIV, masthead);
            writer.writeAttribute(HTMLAttributes.CLASS, buttonClassName, null);
            String style = ThemeStyles.MASTHEAD_LINK;

            if (areExtraLinks && helpLinkDisplayed) {
                // extra links and help are present, use center link style
                style = ThemeStyles.MASTHEAD_LINK_CENTER;
            } else if (areExtraLinks && !helpLinkDisplayed) {
                // extra links but no help - use right link style
                style = ThemeStyles.MASTHEAD_LINK_RIGHT;
            } else if (!areExtraLinks && helpLinkDisplayed) {
                // help but no extra links - use left link style
                style = ThemeStyles.MASTHEAD_LINK_LEFT;
            }

            setAttrs(logoutFacet, "MastheadLogoutLink", masthead,
                    theme.getMessage("masthead.logoutLabel"),
                    theme.getStyleClass(style),
                    theme.getMessage("masthead.logoutTooltip"),
                    theme.getMessage("masthead.logoutStatus"));

            RenderingUtilities.renderComponent(logoutFacet, context);
            writer.endElement(HTMLElements.DIV);
            appendDotImage(writer, context, masthead,
                    "_logoutPad", 1, 8, theme);
        }

        if (helpFacet != null) {
            writer.startElement(HTMLElements.DIV, masthead);
            writer.writeAttribute(HTMLAttributes.CLASS, buttonClassName, null);
            String style = ThemeStyles.MASTHEAD_LINK;

            if (areExtraLinks || logoutLinkDisplayed) {
                // extra and/or logout link displayed, use right link style
                style = ThemeStyles.MASTHEAD_LINK_RIGHT;
            }

            setAttrs(helpFacet, "MastheadHelpLink", masthead,
                    theme.getMessage("masthead.helpLabel"),
                    theme.getStyleClass(style),
                    theme.getMessage("masthead.helpLabel"),
                    theme.getMessage("masthead.helpLabel"));

            RenderingUtilities.renderComponent(helpFacet, context);
            writer.endElement(HTMLElements.DIV);
            appendDotImage(writer, context, masthead, "_helpPad", 1, 8, theme);
        }

        // close the table dividier for the logout & help links
        writer.endElement(HTMLElements.TD);

        // close the utility bar table row
        writer.endElement(HTMLElements.TR);
    }

    /**
     * Helper method to set the given id, parent, label and styleClass for the
     * given component (if they haven't already been set).
     *
     * @param component UI component
     * @param id component id
     * @param parent parent component
     * @param label label text
     * @param styleClass CSS class
     * @param toolTip tool-tip text
     * @param focusText onFocus text
     */
    private void setAttrs(final UIComponent component, final String id,
            final UIComponent parent, final String label,
            final String styleClass, final String toolTip,
            final String focusText) {

        Map<String, Object> attrs = component.getAttributes();
        StringBuilder focusBuff = new StringBuilder();
        focusBuff.append("window.status='")
                .append(focusText).append("'; return true; ");
        String focusJs = focusBuff.toString();

        if (component.getId() == null) {
            component.setId(id);
        }

        // FIXME: it is never good to add children in a renderer.
        if (component.getParent() == null) {
            parent.getChildren().add(component);
        }

        if (attrs.get("text") == null) {
            attrs.put("text", label);
        }

        if (attrs.get("toolTip") == null) {
            attrs.put("toolTip", toolTip);
        }

        if (attrs.get("onFocus") == null) {
            attrs.put("onFocus", focusJs);
        }

        if (attrs.get("onMouseOver") == null) {
            attrs.put("onMouseOver", focusJs);
        }

        if (attrs.get("onMouseOut") == null) {
            attrs.put("onMouseOut", "window.status=''; return true;");
        }

        if (attrs.get("onBlur") == null) {
            attrs.put("onBlur", "window.status=''; return true;");
        }
    }

    /**
     * Helper method to start a layout table with the given style class name.
     *
     * @param writer The current ResponseWriter
     * @param masthead The current Masthead component
     * @param styleName The name of the style class to use for this table
     * @throws IOException if an IO error occurs
     */
    private void startTable(final ResponseWriter writer,
            final Masthead masthead, final String styleName)
            throws IOException {

        writer.startElement(HTMLElements.TABLE, masthead);
        writer.writeAttribute(HTMLAttributes.WIDTH, "100%", null);
        writer.writeAttribute(HTMLAttributes.BORDER, "0", null);
        writer.writeAttribute(HTMLAttributes.CELLPADDING, "0", null);
        writer.writeAttribute(HTMLAttributes.CELLSPACING, "0", null);
        writer.writeAttribute(HTMLAttributes.CLASS, styleName, null);
        writer.writeAttribute(HTMLAttributes.TITLE, "", null);
    }

    /**
     * Output the link count in the given style.
     * @param writer writer to use
     * @param context faces context
     * @param imageName image name
     * @param imageAlt image alternate text
     * @param masthead masthead component
     * @param style CSS style
     * @param count alarm count
     * @param theme the current theme
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private void writeAlarmCount(final ResponseWriter writer,
            final FacesContext context, final String imageName,
            final String imageAlt, final Masthead masthead, final String style,
            final int count, final Theme theme) throws IOException {

        Icon icon = ThemeUtilities.getIcon(theme, imageName);
        icon.setId(imageName);
        icon.setAlt(theme.getMessage(imageAlt));
        RenderingUtilities.renderComponent(icon, context);

        writer.startElement(HTMLElements.SPAN, masthead);
        writer.writeAttribute(HTMLAttributes.CLASS, theme.getStyleClass(style),
                null);
        writer.write("&nbsp;" + count + "&nbsp;&nbsp;&nbsp;");
        writer.endElement(HTMLElements.SPAN);
    }

    /**
     * Helper function to write a span tag with the given style and given text.
     *
     * @param writer The current ResponseWriter
     * @param masthead The Masthead component
     * @param styleName The style class name to use
     * @param text The text to output inside of the span
     * @param context faces context
     * @param id component id
     * @throws IOException if an IO error occurs
     */
    private void writeSpan(final ResponseWriter writer, final Masthead masthead,
            final String styleName, final String text,
            final FacesContext context, final String id) throws IOException {

        writer.startElement(HTMLElements.SPAN, masthead);
        writer.writeAttribute(HTMLAttributes.ID,
                masthead.getClientId(context) + id, HTMLAttributes.ID);
        writer.writeAttribute(HTMLAttributes.CLASS, styleName, null);
        if (text != null) {
            writer.write(text);
        }
        writer.endElement(HTMLElements.SPAN);
    }

    /**
     * Append the dot image.
     * @param writer writer to use
     * @param context faces context
     * @param masthead masthead component
     * @param id component id
     * @param ht height
     * @param wd width
     * @param theme the current theme
     * @throws IOException if an IO error occurs
     */
    private void appendDotImage(final ResponseWriter writer,
            final FacesContext context, final Masthead masthead,
            final String id, final int ht, final int wd, final Theme theme)
            throws IOException {

        Icon dot = ThemeUtilities.getIcon(theme, ThemeImages.DOT);
        dot.setParent(masthead);
        dot.setId(id);
        dot.setParent(masthead);
        dot.setWidth(wd);
        dot.setHeight(ht);
        dot.setBorder(0);
        // GF-required 508 change
        dot.setAlt(id + " dot image");
        RenderingUtilities.renderComponent(dot, context);
    }

    /**
     * Append the separator.
     * @param writer writer to use
     * @param context faces context
     * @param align align value
     * @throws IOException if an IO error occurs
     */
    private void appendSeparator(final ResponseWriter writer,
            final FacesContext context, final String align) throws IOException {

        ImageComponent separator = new ImageComponent();
        separator.setId("searchSeparator");
        separator.setIcon(ThemeImages.MASTHEAD_SEPARATOR_STATUS);
        if (align != null) {
            separator.setAlign(align);
        }
        RenderingUtilities.renderComponent(separator, context);
    }

    /**
     * Return a UIComponent suitable to render for the brand image. If the
     * {@code brandImage} facet exists return it, otherwise if the
     * {@code masthead.getBrandImageURL()} exists create a component
     * initialized with appropriate values and return it.
     * <p>
     * In this implementation, if a value for {@code getBrandImageUrl} is
     * not specified it returns an {@code Icon} component, by calling
     * {@code ThemeUtilities.getIcon} with the
     * {@code ThemeImages.MASTHEAD_CORPLOGO} key. If there is no image for
     * this key, return {@code null}
     * </p>
     * @param context faces context
     * @param masthead masthead component
     * @param theme the current
     * @return UIComponent
     */
    protected UIComponent getBrandImage(final FacesContext context,
            final Masthead masthead, final Theme theme) {

        UIComponent facet = masthead.getFacet("brandImage");
        if (facet != null) {
            return facet;
        }

        String imageAttr = masthead.getBrandImageURL();
        if (imageAttr != null && imageAttr.trim().length() != 0) {

            ImageComponent image = new ImageComponent();

            // use the brand image properties specified on the component
            image.setUrl(imageAttr);
            imageAttr = masthead.getBrandImageDescription();
            if (imageAttr != null && imageAttr.trim().length() != 0) {
                image.setAlt(imageAttr);
            }

            int dim = masthead.getBrandImageHeight();
            if (dim != 0) {
                image.setHeight(masthead.getBrandImageHeight());
            }

            dim = masthead.getBrandImageWidth();
            if (dim != 0) {
                image.setWidth(masthead.getBrandImageWidth());
            }
            return image;
        }

        // no facet, no props - output the standard java brand image
        // First see if there is valid image. If there is no
        // image, return null, else retun the value from
        // ThemeUtilities.getIcon.
        Icon icon = null;
        try {
            String imagePath
                    = theme.getImagePath(ThemeImages.MASTHEAD_CORPLOGO);
            if (imagePath == null) {
                return null;
            }
            icon = ThemeUtilities.getIcon(theme, ThemeImages.MASTHEAD_CORPLOGO);
            icon.setId(masthead.getId() + "_brandImage");
            icon.setParent(masthead);
        } catch (Exception e) {
            // Don't care.
        }
        return icon;
    }

    /**
     * Return a UIComponent suitable to render for the product image. If the
     * {@code productInfo} facet exists return it, otherwise if the
     * {@code masthead.getProductImageURL()} exists create an
     * {@code ImageComponent} initialized with appropriate values and
     * return it, otherwise return {@code null}.
     * @param context faces context
     * @param masthead masthead component
     * @param theme the current theme
     * @return UIComponent
     */
    protected UIComponent getProductImage(final FacesContext context,
            final Masthead masthead, final Theme theme) {

        UIComponent productFacet = masthead.getFacet("productInfo");
        if (productFacet != null) {
            return productFacet;
        }

        String imageUrl = masthead.getProductImageURL();
        if (imageUrl == null || imageUrl.trim().length() == 0) {
            return null;
        }

        ImageComponent image = new ImageComponent();
        image.setId(masthead.getId() + "_productInfo");
        image.setParent(masthead);
        image.setUrl(imageUrl);
        image.setHeight(masthead.getProductImageHeight());
        image.setWidth(masthead.getProductImageWidth());
        image.setAlt(masthead.getProductImageDescription());
        return image;
    }

    /**
     * Render a secondary masthead.
     * @param context faces context
     * @param masthead masthead component
     * @param theme the current theme
     * @param writer the writer to use
     * @throws IOException if an IO error occurs
     */
    protected void renderSecondaryMasthead(final FacesContext context,
            final Masthead masthead, final Theme theme,
            final ResponseWriter writer) throws IOException {

        startTable(writer, masthead,
                theme.getStyleClass(ThemeStyles.MASTHEAD_SECONDARY_STYLE));
        writer.startElement(HTMLElements.TR, masthead);
        writer.startElement(HTMLElements.TD, masthead);

        renderProductInfo(context, masthead, writer, null);
        writer.endElement(HTMLElements.TD);
        writer.startElement(HTMLElements.TD, masthead);
        writer.writeAttribute(HTMLAttributes.ALIGN, "right", null);

        UIComponent brandImage = getBrandImage(context, masthead, theme);
        if (brandImage != null) {
            RenderingUtilities.renderComponent(brandImage, context);
        }
        writer.endElement(HTMLElements.TD);
        writer.endElement(HTMLElements.TR);
        writer.endElement(HTMLElements.TABLE);
    }

    /**
     * Render a primary masthead.
     * @param context faces context
     * @param masthead masthead component
     * @param theme the current theme
     * @param writer the writer to use
     * @throws IOException if an IO error occurs
     */
    protected void renderPrimaryMasthead(final FacesContext context,
            final Masthead masthead, final Theme theme,
            final ResponseWriter writer) throws IOException {

        // start the div the entire masthead is wrapped in
        writer.startElement(HTMLElements.DIV, masthead);
        String styleClass = theme.getStyleClass(ThemeStyles.MASTHEAD_DIV);
        RenderingUtilities.renderStyleClass(context, writer, masthead,
                styleClass);

        String style = masthead.getStyle();
        if (style != null) {
            writer.writeAttribute(HTMLAttributes.STYLE, style, null);
        }
        writer.write("\n");

        RenderingUtilities.renderSkipLink(SKIP_UTILITY,
                theme.getStyleClass(ThemeStyles.SKIP_MEDIUM_GREY1), null,
                theme.getMessage("masthead.statusSkipTagAltText"),
                null, masthead, context);

        // start the table the masthead uses for layout
        startTable(writer, masthead,
                theme.getStyleClass(ThemeStyles.MASTHEAD_TABLE_TOP));

        // render the utility bar
        renderUtilityBar(context, masthead, writer, theme);

        // close the utility bay layout table
        writer.endElement(HTMLElements.TABLE);

        // start the layout table for the app info, status area &
        // brand image
        startTable(writer, masthead,
                theme.getStyleClass(ThemeStyles.MASTHEAD_TABLE_BOTTOM));

        // all these areas go in a single row
        writer.startElement(HTMLElements.TR, masthead);

        // render the app info - typically user, server and product name details
        // userinfo, role info and serverinfo appear in a single TD
        //
        renderApplicationInfo(context, masthead, writer, theme);

        // The product and brand appear in a single TD
        //
        renderBrandImage(context, masthead, theme, writer);

        writer.endElement(HTMLElements.TR);

        // Insert a row of space below the product name and brand image
        //
        writer.startElement(HTMLElements.TR, masthead);
        writer.startElement(HTMLElements.TD, masthead);
        writer.writeAttribute(HTMLAttributes.COLSPAN, "2", null);

        writer.startElement(HTMLElements.DIV, masthead);
        writer.writeAttribute(HTMLAttributes.CLASS,
                theme.getStyleClass(ThemeStyles.MASTHEAD_HRULE), null);
        appendDotImage(writer, context, masthead, "_mhrule", 1, 1, theme);
        writer.endElement(HTMLElements.DIV);

        writer.endElement(HTMLElements.TD);
        writer.endElement(HTMLElements.TR);

        writer.endElement(HTMLElements.TABLE);

        renderStatusArea(context, masthead, writer, theme);

        // close the div that wraps the entire masthead
        writer.endElement(HTMLElements.DIV);
        RenderingUtilities.renderAnchor(SKIP_UTILITY, masthead, context);
    }

    /**
     * Render the status area based on the status area masthead properties.
     * @param context faces context
     * @param masthead masthead component
     * @param theme the current theme
     * @param writer the writer to use
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    protected void renderStatusAreaComponents(final FacesContext context,
            final Masthead masthead, final Theme theme,
            final ResponseWriter writer) throws IOException {

        boolean isDateTime = masthead.isDateTime();
        String notificationMsg = masthead.getNotificationMsg();
        int[] alarmCounts = masthead.getAlarmCounts();
        int jobCount = masthead.getJobCount();

        UIComponent notificationInfo
                = masthead.getFacet("notificationInfo");
        UIComponent jobsInfo = masthead.getFacet("jobsInfo");
        UIComponent dateTimeInfo = masthead.getFacet("dateTimeInfo");
        UIComponent currentAlarmsInfo
                = masthead.getFacet("currentAlarmsInfo");

        // at least one of the default status area items is displayed
        boolean showNotification = notificationInfo != null
                || (notificationMsg != null
                && notificationMsg.length() != 0);

        boolean showJobs = jobCount != -1
                || jobsInfo != null;

        boolean havestatus = isDateTime
                || alarmCounts != null
                || showJobs
                || dateTimeInfo != null
                || currentAlarmsInfo != null
                || showNotification;

        // No status area artifacts
        if (!havestatus) {
            return;
        }

        // render the start of the bottom table
        startTable(writer, masthead, theme.getStyleClass(
                ThemeStyles.MASTHEAD_TABLE_END));
        writer.startElement(HTMLElements.TR, masthead);

        // get the text and label styles from the theme
        String labelStyle = theme.getStyleClass(ThemeStyles.MASTHEAD_LABEL);
        String textStyle = theme.getStyleClass(ThemeStyles.MASTHEAD_TEXT);

        boolean separatorFlag = false;

        if (showNotification) {
            // notication message needs to be displayed
            renderNotificationInfo(context, masthead, writer,
                    showNotification && showJobs, theme);
            separatorFlag = true;
        }

        if (showJobs) {
            if (separatorFlag) {
                writer.startElement(HTMLElements.TD, masthead);
                writer.writeAttribute(HTMLAttributes.VALIGN, "middle", null);
                appendSeparator(writer, context, "top");
                writer.endElement(HTMLElements.TD);
                separatorFlag = false;
            }
            // jobs info needs to displayed
            renderJobsInfo(context, masthead, writer,
                    showNotification && showJobs, theme);
            separatorFlag = true;
        }

        if (isDateTime || dateTimeInfo != null) {
            if (separatorFlag) {
                writer.startElement(HTMLElements.TD, masthead);
                writer.writeAttribute(HTMLAttributes.VALIGN, "middle",
                        null);
                appendSeparator(writer, context, "top");
                writer.endElement(HTMLElements.TD);
                separatorFlag = false;
            }
            // date and time stamp needs to be displayed
            renderDateTimeInfo(context, masthead, writer, theme);
            separatorFlag = true;
        }

        if ((alarmCounts != null && alarmCounts.length == 4)
                || currentAlarmsInfo != null) {
            if (separatorFlag) {
                writer.startElement(HTMLElements.TD, masthead);
                writer.writeAttribute(HTMLAttributes.VALIGN, "middle", null);
                appendSeparator(writer, context, "top");
                writer.endElement(HTMLElements.TD);
            }
            // current alarms info needs to be displayed
            renderAlarmsInfo(context, masthead, writer, alarmCounts, theme);
        }

        writer.endElement(HTMLElements.TR);
        writer.endElement(HTMLElements.TABLE);
    }

    /**
     * Its not clear what the policy should be regarding the rendering of the
     * user info. Before this code was refactored there was an illogical
     * dependency between the existence user info and server info. Unlink
     * roleInfo user info and server info are rendered whether or not there is
     * data. This can yield a label without any data. The roleInfo is only
     * rendered if there is "userInfo" or facets.
     *
     * @param context faces context
     * @param masthead masthead component
     * @param theme the current theme
     * @param labelStyle label CSS style
     * @param textStyle text CSS style
     * @param separator separator component
     * @param writer the writer to use
     * @throws IOException if an IO error occurs
     */
    private void renderUserInfo(final FacesContext context,
            final Masthead masthead, final Theme theme, final String labelStyle,
            final String textStyle, final UIComponent separator,
            final ResponseWriter writer) throws IOException {

        UIComponent facet = masthead.getFacet("userInfoLabel");
        if (facet != null) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            String label = masthead.getUserInfoLabel();
            if (label == null) {
                label = theme.getMessage("masthead.userLabel");
            }
            writeSpan(writer, masthead, labelStyle, label, context,
                    "_userLabel");
        }

        // This should be some sort of CSS selector on the
        // span or the previous span or the span should contain
        // the facet since the facet may not add any space.
        writer.write("&nbsp;");

        facet = masthead.getFacet("userInfo");
        if (facet != null) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            writeSpan(writer, masthead, textStyle, masthead.getUserInfo(),
                    context, "_userInfo");
        }

        // We know parent has been set.
        separator.setId("_userInfoSeparator");
        RenderingUtilities.renderComponent(separator, context);
    }

    /**
     * Unlike user info and server info role info is only rendered if there are
     * role facets, either one, or if {@code masthead.getRoleInfo} returns
     * non null and a non empty string.
     *
     * Note that if there is a {@code roleInfoLabel} facet but no
     * {@code roleInfo} facet, the {@code roleInfoLabel} facet will be
     * rendered, whether or not the {@code roleInfo} attribute has data.
     * Likewise if there is a {@code roleInfo} facet and not
     * {@code roleInfoLabel} facet and no {@code roleInfo} attribute
     * data, the {@code roleInfo} facet is still rendered, yielding data
     * without a label.
     * @param context faces context
     * @param masthead masthead component
     * @param theme the current theme
     * @param labelStyle label CSS style
     * @param textStyle text CSS style
     * @param separator separator component
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void renderRoleInfo(final FacesContext context,
            final Masthead masthead, final Theme theme, final String labelStyle,
            final String textStyle, final UIComponent separator,
            final ResponseWriter writer) throws IOException {

        boolean haveRoleFacet = false;

        // If there is no roleInfo nothing is rendered unless there
        // are facets.
        String roleInfo = masthead.getRoleInfo();
        boolean haveRoleInfo
                = roleInfo != null && roleInfo.trim().length() != 0;

        // We either have a facet or roleinfo
        // So render a label.
        UIComponent roleLabelFacet
                = masthead.getFacet("roleInfoLabel");
        if (roleLabelFacet != null) {
            RenderingUtilities.renderComponent(roleLabelFacet, context);
            haveRoleFacet = true;
        } else {
            if (haveRoleInfo) {
                String label = masthead.getRoleInfoLabel();
                if (label == null) {
                    label = theme.getMessage("masthead.roleLabel");
                }
                writeSpan(writer, masthead, labelStyle, label, context,
                        "_roleLabel");
            }
        }

        // This should be some sort of CSS selector on the
        // span or the previous span or the span should contain
        // the facet since the facet may not add any space.
        writer.write("&nbsp;");

        UIComponent roleInfoFacet = masthead.getFacet("roleInfo");
        if (roleInfoFacet != null) {
            RenderingUtilities.renderComponent(roleInfoFacet, context);
            haveRoleFacet = true;
        } else {
            if (haveRoleInfo) {
                writeSpan(writer, masthead, textStyle, roleInfo,
                        context, "_roleInfo");
            }
        }

        // Reuse the separator
        // We know the parent was already set.
        if (haveRoleFacet || haveRoleInfo) {
            separator.setId("_roleInfoSeparator");
            RenderingUtilities.renderComponent(separator, context);
        }
    }

    /**
     * Render the server info.
     * @param context faces context
     * @param masthead masthead component
     * @param theme the current theme
     * @param labelStyle label CSS style
     * @param textStyle text CSS style
     * @param writer the writer to use
     * @throws IOException if an IO error occurs
     */
    private void renderServerInfo(final FacesContext context,
            final Masthead masthead, final Theme theme, final String labelStyle,
            final String textStyle, final ResponseWriter writer)
            throws IOException {

        UIComponent facet = masthead.getFacet("serverInfoLabel");
        if (facet != null) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            String label = masthead.getServerInfoLabel();
            if (label == null) {
                label = theme.getMessage("masthead.serverLabel");
            }
            writeSpan(writer, masthead, labelStyle, label, context,
                    "_serverLabel");
        }

        // This should be some sort of CSS selector on the
        // span or the previous span or the span should contain
        // the facet since the facet may not add any space.
        writer.write("&nbsp;");

        facet = masthead.getFacet("serverInfo");
        if (facet != null) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            writeSpan(writer, masthead, textStyle, masthead.getServerInfo(),
                    context, "_serverInfo");
        }
    }

    /**
     * Renders the logo separator and then the logo. If there is no logo a
     * "transparent" separator is rendered to maintain the height when there is
     * a logo.
     * @param context faces context
     * @param masthead masthead component
     * @param theme the current theme
     * @param writer the writer to use
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderBrandImage(final FacesContext context,
            final Masthead masthead, final Theme theme,
            final ResponseWriter writer) throws IOException {

        writer.startElement(HTMLElements.TD, masthead);
        writer.writeAttribute(HTMLAttributes.CLASS,
                theme.getStyleClass(ThemeStyles.MASTHEAD_TD_LOGO), null);
        writer.writeAttribute(HTMLAttributes.WIDTH, "1%", null);
        writer.writeAttribute(HTMLAttributes.ALIGN, "right", null);
        writer.writeAttribute(HTMLAttributes.NOWRAP, HTMLAttributes.NOWRAP,
                HTMLAttributes.NOWRAP);

        // If we don't have a brand image, just render a transparend
        // spacer to maintain the height of the masthead body.
        UIComponent brandImage = getBrandImage(context, masthead, theme);
        if (brandImage == null) {
            appendDotImage(writer, context, masthead, "_logoSep", 37, 2, theme);
        } else {
            UIComponent separatorFacet
                    = masthead.getFacet("separatorImage");
            if (separatorFacet == null) {
                // Use the default separator
                separatorFacet = ThemeUtilities.getIcon(theme,
                        ThemeImages.MASTHEAD_JAVA_LOGO_SEPARATOR);
                separatorFacet.setId("_logoSep");
                separatorFacet.setParent(masthead);
            }
            RenderingUtilities.renderComponent(separatorFacet, context);
            appendDotImage(writer, context, masthead, "_logoPad", 1, 10, theme);
            RenderingUtilities.renderComponent(brandImage, context);
        }
        writer.endElement(HTMLElements.TD);
    }
}
