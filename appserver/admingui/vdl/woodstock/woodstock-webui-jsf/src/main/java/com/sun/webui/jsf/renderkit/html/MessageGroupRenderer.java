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
import com.sun.webui.jsf.component.MessageGroup;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.FacesMessageUtils;
import com.sun.webui.jsf.util.MessageUtil;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.FacesMessage.Severity;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.beans.Beans;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

/**
 * This class is responsible for rendering the Message component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.MessageGroup"))
public final class MessageGroupRenderer extends AbstractRenderer {

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // End the appropriate element
        MessageGroup msgGrp = (MessageGroup) component;
        Iterator msgIt;
        String forComponentId = null;

        if (Beans.isDesignTime()
                && (msgGrp.isShowDetail() || msgGrp.isShowSummary())) {
            StringBuilder resourceNameBuffer = new StringBuilder();
            resourceNameBuffer.append("MessageGroup.");
            if (msgGrp.isShowGlobalOnly()) {
                resourceNameBuffer.append("global.");
            } else {
                resourceNameBuffer.append("default.");
            }
            if (msgGrp.isShowDetail() && msgGrp.isShowSummary()) {
                resourceNameBuffer.append("both");
            } else if (msgGrp.isShowDetail()) {
                resourceNameBuffer.append("detail");
            } else if (msgGrp.isShowSummary()) {
                resourceNameBuffer.append("summary");
            }
            String summary = MessageUtil.getMessage(context,
                    "com.sun.webui.jsf.renderkit.html.Bundle",
                    resourceNameBuffer.toString());
            FacesMessage defaultMessage = new FacesMessage();
            defaultMessage.setSummary(summary);
            msgIt = Collections.singletonList(defaultMessage).iterator();
        } else {
            if (msgGrp.isShowGlobalOnly()) {
                // for only global messages
                forComponentId = "";
            }
            msgIt = FacesMessageUtils.getMessageIterator(context,
                    forComponentId, msgGrp);
        }
        if (msgIt.hasNext()) {
            renderMessageGroup(context, msgGrp, writer, msgIt);
        }
    }

    /**
     * Renders the Message text.
     *
     * @param context The current FacesContext
     * @param component The VersionPage object to use
     * @param writer The current ResponseWriter
     * @param msgIt The message
     *
     * @exception IOException if an input/output error occurs
     */
    public void renderMessageGroup(final FacesContext context,
            final UIComponent component, final ResponseWriter writer,
            final Iterator msgIt) throws IOException {

        MessageGroup msgGrp = (MessageGroup) component;

        // Get the theme
        Theme theme = ThemeUtilities.getTheme(context);

        // Render the style/styleClass attributes in a surrounding div
        renderMessageGroupIdElement(context, msgGrp, writer);

        // Render the opening table
        renderOpeningTable(msgGrp, writer, theme);

        FacesMessage fMsg;
        boolean showSummary = msgGrp.isShowSummary();
        boolean showDetail = msgGrp.isShowDetail();
        String summaryStyle = theme.getStyleClass(
                ThemeStyles.MESSAGE_GROUP_SUMMARY_TEXT);
        String detailStyle = theme.getStyleClass(
                ThemeStyles.MESSAGE_GROUP_TEXT);

        String summary = null;
        String detail = null;

        // Optimization to reduce compiler construction of StringBuffer
        // for constant text within the loop.
        StringBuilder detailBuf = new StringBuilder().append(" ");

        while (msgIt.hasNext()) {

            fMsg = (FacesMessage) msgIt.next();
            // Check if we should show detail or summary
            if (showSummary) {
                summary = fMsg.getSummary();
                if ((summary != null) && (summary.length() <= 0)) {
                    summary = null;
                }
            }
            if (showDetail) {
                detail = fMsg.getDetail();
                if ((detail != null) && (detail.length() <= 0)) {
                    detail = null;
                }
            }

            if (summary == null && detail == null) {
                continue;
            }

            // Null these variables when a severity style is found.
            // Severity styles override the default styles.
            String summaryStyleTmp = summaryStyle;
            String detailStyleTmp = detailStyle;

            // Why is there a div with a list with only a single
            // bullet for each message ? Why not a bullet for each
            // messages and one list and one div ?
            //
            writer.startElement("div", msgGrp);
            writer.writeAttribute("class",
                    theme.getStyleClass(ThemeStyles.MESSAGE_GROUP_DIV),
                    null);
            writer.startElement("ul", msgGrp);
            writer.startElement("li", msgGrp);

            // render theme based style based on severity.
            String severityStyleClass = getSeverityStyleClass(fMsg, theme);

            // This renders the selector on the "li" element.
            // The default styles appear on the text's "span" element.
            // Severity styles override default styles
            if (severityStyleClass != null) {
                writer.writeAttribute("class", severityStyleClass,
                        "styleClass");
                summaryStyleTmp = null;
                detailStyleTmp = null;
            }

            if (summary != null) {
                renderMessageText(msgGrp, writer, summary, summaryStyleTmp);
            }

            if (detail != null) {
                // renderMessageText(msgGrp, writer, detail, detailStyle);
                // if severity based style  is set, don't use theme based
                // default styles.
                //
                // Places a space between the summary message and
                // the detail message. This should be part of the theme.
                // A style for the detail message when preceded by the
                // summary message.
                if (summary != null) {
                    detail = detailBuf.append(detail).toString();
                }
                renderMessageText(msgGrp, writer, detail, detailStyleTmp);

                // Rewind the buffer so only the " " exists.
                detailBuf.setLength(1);
            }
            writer.endElement("li");
            writer.endElement("ul");
            writer.endElement("div");
        }

        // Close tags
        renderClosingTable(writer);
        // Close the surrounding div
        writer.endElement("div");
    }

    /**
     * Helper method to render opening tags for the layout table.
     *
     * @param msgGrp The MessageGroup object to use
     * @param writer The current ResponseWriter
     * @param theme The theme to use
     *
     * @exception IOException if an input/output error occurs
     */
    public void renderOpeningTable(final MessageGroup msgGrp,
            final ResponseWriter writer, final Theme theme) throws IOException {

        // Render the layout table
        writer.startElement("table", msgGrp);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.MESSAGE_GROUP_TABLE), null);
        writer.writeAttribute("border", "0", null);
        writer.writeAttribute("cellpadding", "0", null);
        writer.writeAttribute("cellspacing", "0", null);
        if (msgGrp.getToolTip() != null) {
            writer.writeAttribute("title", msgGrp.getToolTip(), null);
        } else {
            // Required for A11Y
            writer.writeAttribute("title", "", null);
        }
        writer.writeText("\n", null);

        // Add the heading
        writer.startElement("tr", msgGrp);
        writer.startElement("th", msgGrp);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.MESSAGE_GROUP_TABLE_TITLE),
                null);
        String title = msgGrp.getTitle();
        if (title != null) {
            writer.writeText(title, null);
        } else {
            writer.writeText(theme.getMessage("messageGroup.heading"), null);
        }
        writer.endElement("th");
        writer.endElement("tr");
        writer.writeText("\n", null);

        // We know there is at least one message
        writer.startElement("tr", msgGrp);
        writer.startElement("td", msgGrp);
    }

    /**
     * Helper method to render closing tags for the layout table.
     *
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    public void renderClosingTable(final ResponseWriter writer)
            throws IOException {

        writer.endElement("td");
        writer.endElement("tr");
        writer.endElement("table");
        writer.writeText("\n", null);
    }

    /**
     * Helper method to write message text.
     *
     * @param msgGrp The MessageGroup object to use
     * @param writer The current ResponseWriter
     * @param msgText The message text
     * @param textStyle The text style
     *
     * @exception IOException if an input/output error occurs
     */
    public void renderMessageText(final MessageGroup msgGrp,
            final ResponseWriter writer, final String msgText,
            final String textStyle) throws IOException {

        writer.startElement("span", msgGrp);
        if (textStyle != null && textStyle.length() > 0) {
            writer.writeAttribute("class", textStyle, "class");
        }
        writer.writeText(msgText, null);
        writer.endElement("span");
    }

    /**
     * Render the enclosing element for the MesssageGroup messages that is
     * associated with the component's id.
     *
     * @param context The current FacesContext
     * @param msgGrp The MessageGroup object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    private void renderMessageGroupIdElement(final FacesContext context,
            final MessageGroup msgGrp, final ResponseWriter writer)
            throws IOException {

        String userStyle = msgGrp.getStyle();
        String userStyleClass = msgGrp.getStyleClass();
        String id = msgGrp.getClientId(context);

        writer.startElement("div", msgGrp);
        writer.writeAttribute("id", id, "id");
        if (userStyle != null && userStyle.length() > 0) {
            writer.writeAttribute("style", userStyle, "style");
        }
        RenderingUtilities.renderStyleClass(context, writer, msgGrp, null);
    }

    /**
     * Return a style class based on the FacesMesssage severity. If there is no
     * style for a given severity return null.
     *
     * @param facesMessage The FacesMessage
     * @param theme The current theme
     * @return String
     */
    protected String getSeverityStyleClass(final FacesMessage facesMessage,
            final Theme theme) {

        // Obtain a style based on message severity
        String severityStyleClass = null;
        Severity severity = facesMessage.getSeverity();
        if (severity == FacesMessage.SEVERITY_INFO) {
            severityStyleClass
                    = theme.getStyleClass(ThemeStyles.MESSAGE_GROUP_INFO);
        } else if (severity == FacesMessage.SEVERITY_WARN) {
            severityStyleClass
                    = theme.getStyleClass(ThemeStyles.MESSAGE_GROUP_WARN);
        } else if (severity == FacesMessage.SEVERITY_ERROR) {
            severityStyleClass
                    = theme.getStyleClass(ThemeStyles.MESSAGE_GROUP_ERROR);
        } else if (severity == FacesMessage.SEVERITY_FATAL) {
            severityStyleClass
                    = theme.getStyleClass(ThemeStyles.MESSAGE_GROUP_FATAL);
        }
        if (severityStyleClass == null || severityStyleClass.length() == 0) {
            return null;
        }
        return severityStyleClass;
    }
}
