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
import com.sun.webui.jsf.component.Alarm;
import com.sun.webui.jsf.component.ImageComponent;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * Renderer for an {@link Alarm} component.
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.Alarm"))
public final class AlarmRenderer extends ImageRenderer {

    /**
     * Left position.
     */
    private static final String LABEL_LEFT = "left";

    /**
     * Right position.
     */
    private static final String LABEL_RIGHT = "right";

    /**
     * HTML encoded space.
     */
    private static final String WHITE_SPACE = "&nbsp;";

    /**
     * Label critical alternate text key.
     */
    private static final String CRITICAL_ALT_TEXT_KEY =
            "Alarm.criticalImageAltText";

    /**
     * Label major image alternate text.
     */
    private static final String MAJOR_ALT_TEXT_KEY =
            "Alarm.majorImageAltText";

    /**
     * Label minor image alternate text.
     */
    private static final String MINOR_ALT_TEXT_KEY =
            "Alarm.minorImageAltText";

    /**
     * LAbel down alternate text.
     */
    private static final String DOWN_ALT_TEXT_KEY =
            "Alarm.downImageAltText";

    /**
     * Creates a new instance of AlarmRenderer.
     */
    public AlarmRenderer() {
    }

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        if (context == null || component == null) {
            throw new NullPointerException(
                    "FacesContext or UIComponent is null");
        }

        Alarm alarm = (Alarm) component;
        String label = alarm.getText();
        String severity = getSeverity(alarm);
        boolean severityOk = isSeverityOk(severity);

        // If the severity is "ok" and there is no url just render
        // the label, there's no image.
        boolean showImage = !(severityOk && alarm.getUrl() == null);

        if (showImage) {
            Theme theme = ThemeUtilities.getTheme(context);
            if (label == null) {
                renderImage(context, alarm, severity, writer);
            } else {
                // Since we have a label and an image always add the space
                if (LABEL_LEFT.equalsIgnoreCase(alarm.getTextPosition())) {
                    renderLabel(label, true, true, writer);
                    renderImage(context, alarm, severity, writer);
                } else {
                    renderImage(context, alarm, severity, writer);
                    renderLabel(label, false, true, writer);
                }
            }
        } else if (label != null) {
            // Just a label no additional space.
            renderLabel(label, false, false, writer);
        }
    }

    /**
     * Render the label with additional white space if addSpace is true.
     * Render the space first if addSpace is true and labelLeft is true
     * or render the label first if labelLeft is false.
     * @param label label text
     * @param labelLeft {@code true} if positioned left, {@code false} if right
     * @param addSpace {@code true} to add space
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private static void renderLabel(final String label,
            final boolean labelLeft, final boolean addSpace,
            final ResponseWriter writer) throws IOException {

        if (labelLeft) {
            writer.writeText(label, null);
            if (addSpace) {
                writer.write(WHITE_SPACE);
            }
        } else {
            if (addSpace) {
                writer.write(WHITE_SPACE);
            }
            writer.writeText(label, null);
        }
    }

    /**
     * Render an image.
     * @param context faces context
     * @param alarm alarm component
     * @param severity alarm severity
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void renderImage(final FacesContext context, final Alarm alarm,
            final String severity, final ResponseWriter writer)
            throws IOException {

        super.renderStart(context, alarm, writer);
        ImageComponent sevImage = getImage(context, alarm, severity);
        super.renderAttributes(context, sevImage, writer);

        String[] integerAttributes = {"border", "hspace", "vspace"};
        addIntegerAttributes(context, alarm, writer, integerAttributes);
        String[] stringAttributes = {"align", "onClick", "onDblClick"};
        addStringAttributes(context, alarm, writer, stringAttributes);
    }

    /**
     * Get the alarm image.
     * @param context faces context
     * @param alarm component
     * @param severity severity
     * @return ImageComponent
     */
    private ImageComponent getImage(final FacesContext context,
            final Alarm alarm, final String severity) {

        Theme theme = ThemeUtilities.getTheme(context);

        String sevIcon = null;
        String sevAlt = null;
        String sevToolTip = null;
        String sevUrl = null;

        // If the data for the alarm icons existed in Themes then you could
        // obtain the necessary data from "theme.getIcon()" but you would have
        // to transfer the Icon's data from the returned Icon to the
        // ImageComponent to ensure that developer values are respected.
        if (severity.equalsIgnoreCase(Alarm.SEVERITY_CRITICAL)) {
            sevIcon = ThemeImages.ALARM_CRITICAL_MEDIUM;
            sevAlt = theme.getMessage(CRITICAL_ALT_TEXT_KEY);
            sevToolTip = theme.getMessage(CRITICAL_ALT_TEXT_KEY);
        } else if (severity.equalsIgnoreCase(Alarm.SEVERITY_MAJOR)) {
            sevIcon = ThemeImages.ALARM_MAJOR_MEDIUM;
            sevAlt = theme.getMessage(MAJOR_ALT_TEXT_KEY);
            sevToolTip = theme.getMessage(MAJOR_ALT_TEXT_KEY);

        } else if (severity.equalsIgnoreCase(Alarm.SEVERITY_MINOR)) {
            sevIcon = ThemeImages.ALARM_MINOR_MEDIUM;
            sevAlt = theme.getMessage(MINOR_ALT_TEXT_KEY);
            sevToolTip = theme.getMessage(MINOR_ALT_TEXT_KEY);
        } else if (severity.equalsIgnoreCase(Alarm.SEVERITY_DOWN)) {
            sevIcon = ThemeImages.ALARM_DOWN_MEDIUM;
            sevAlt = theme.getMessage(DOWN_ALT_TEXT_KEY);
            sevToolTip = theme.getMessage(DOWN_ALT_TEXT_KEY);
        }

        // If the developer specified an URL it takes precendence
        // over the icon in ImageRenderer.
        // See if the developer overrode the severity based icon.
        String icon = alarm.getIcon();
        if (icon != null) {
            sevIcon = icon;
        }
        String alt = alarm.getAlt();
        if (alt != null) {
            sevAlt = alt;
        }
        String toolTip = alarm.getToolTip();
        if (toolTip != null) {
            sevToolTip = toolTip;
        }

        String url = alarm.getUrl();
        if (url != null) {
            sevUrl = url;
        }

        // We don't want to pass an Icon to the ImageRenderer
        // because if it sees an Icon, it ignores too much
        ImageComponent sevImage = new ImageComponent();
        sevImage.setIcon(sevIcon);
        sevImage.setUrl(url);
        sevImage.setAlt(sevAlt);
        sevImage.setToolTip(sevToolTip);
        int dim = alarm.getHeight();
        if (dim >= 0) {
            sevImage.setHeight(dim);
        //height = dim;
        }
        // width
        dim = alarm.getWidth();
        if (dim >= 0) {
            sevImage.setWidth(dim);
        //image.width = dim;
        }

        return sevImage;
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        String severity = getSeverity((Alarm) component);
        boolean severityOk = isSeverityOk(severity);

        // If the severity is "ok" and there is no url just render
        // or severity was not "ok", close the img element.
        if (!(severityOk && ((Alarm) component).getUrl() == null)) {
            super.renderEnd(context, component, writer);
        }
    }

    /**
     * Render the image element's attributes
     * Does nothing. super.renderAttributes is called in
     * other private methods to render the image.
     * But we don't want AbstractRenderer's call to this method
     * to mess up the sequencing of the rendering.
     *
     * @param context The current FacesContext
     * @param component The ImageComponent object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {
    }

    /**
     * Get alarm severity.
     * Return the severity or DEFAULT_SEVERITY if severity is not set
     * or if the severity is a custom severity
     *
     * @param alarm The Alarm component.
     * @return String
     */
    private static String getSeverity(final Alarm alarm) {
        String severity = alarm.getSeverity();
        if (severity == null || !(severity.equals(Alarm.SEVERITY_CRITICAL)
                || severity.equals(Alarm.SEVERITY_DOWN)
                || severity.equals(Alarm.SEVERITY_MAJOR)
                || severity.equals(Alarm.SEVERITY_MINOR))) {
            severity = Alarm.DEFAULT_SEVERITY;
        }
        return severity.toLowerCase();
    }

    /**
     * If the severity is acceptable, but URL is not null, show an image
     * even though the severity is fine. The guidelines say acceptable
     * has no image but if the developer wants it show it.
     * @param severity severity to test
     * @return {@code true} if acceptable, {@code false} otherwise
     */
    private static boolean isSeverityOk(final String severity) {
        return Alarm.DEFAULT_SEVERITY.equalsIgnoreCase(severity);
    }
}
