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
import com.sun.webui.jsf.component.AlarmStatus;
import com.sun.webui.jsf.component.ImageComponent;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

import static com.sun.webui.jsf.util.ConversionUtilities.convertValueToString;
import static com.sun.webui.jsf.util.RenderingUtilities.renderComponent;

/**
 * Render an instance of the AlarmStatus component.
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.AlarmStatus"))
public final class AlarmStatusRenderer extends HyperlinkRenderer {

    /**
     * Creates a new instance of AlarmStatusRenderer.
     */
    public AlarmStatusRenderer() {
    }

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        AlarmStatus alarmStatus = (AlarmStatus) component;
        Theme theme = ThemeUtilities.getTheme(context);
        UIComponent facet = alarmStatus.getFacet("alarmLabel");
        if (facet != null) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            renderAlarmLabel(context, alarmStatus, writer, theme);
        }

        int numAlarms;
        String icon;
        String themeIcon;
        String alt;
        facet = alarmStatus.getFacet("downAlarms");

        if (facet != null) {
            // render the developer specified facet for down alarms
            RenderingUtilities.renderComponent(facet, context);
        } else if (alarmStatus.isDownAlarms()) {
            // render the down alarm image + count
            numAlarms = alarmStatus.getNumDownAlarms();
            if (numAlarms != 0) {
                themeIcon = ThemeImages.ALARM_MASTHEAD_DOWN_MEDIUM;
            } else {
                themeIcon = ThemeImages.ALARM_MASTHEAD_DOWN_DIMMED;
            }
            if (alarmStatus.getDownIcon() != null) {
                icon = alarmStatus.getDownIcon();
            } else {
                icon = themeIcon;
            }
            alt = theme.getMessage("Alarm.downImageAltText");
            renderAlarmCount(context, writer, alarmStatus, icon,
                    theme.getStyleClass(ThemeStyles.MASTHEAD_ALARM_LINK),
                    numAlarms, alt);
        }

        facet = alarmStatus.getFacet("criticalAlarms");

        if (facet != null) {
            // render the developer specified facet for critical alarms
            RenderingUtilities.renderComponent(facet, context);
        } else if (alarmStatus.isCriticalAlarms()) {
            // render the critical alarm + count
            numAlarms = alarmStatus.getNumCriticalAlarms();
            if (numAlarms != 0) {
                themeIcon = ThemeImages.ALARM_MASTHEAD_CRITICAL_MEDIUM;
            } else {
                themeIcon = ThemeImages.ALARM_MASTHEAD_CRITICAL_DIMMED;
            }
            if (alarmStatus.getCriticalIcon() != null) {
                icon = alarmStatus.getCriticalIcon();
            } else {
                icon = themeIcon;
            }
            alt = theme.getMessage("Alarm.criticalImageAltText");
            renderAlarmCount(context, writer, alarmStatus, icon,
                    theme.getStyleClass(ThemeStyles.MASTHEAD_ALARM_LINK),
                    numAlarms, alt);
        }

        facet = alarmStatus.getFacet("majorAlarms");

        if (facet != null) {
            // render the developer specified facet for major alarms
            RenderingUtilities.renderComponent(facet, context);
        } else if (alarmStatus.isMajorAlarms()) {
            // render the major alarm + count
            numAlarms = alarmStatus.getNumMajorAlarms();
            if (numAlarms != 0) {
                themeIcon = ThemeImages.ALARM_MASTHEAD_MAJOR_MEDIUM;
            } else {
                themeIcon = ThemeImages.ALARM_MASTHEAD_MAJOR_DIMMED;
            }
            if (alarmStatus.getMajorIcon() != null) {
                icon = alarmStatus.getMajorIcon();
            } else {
                icon = themeIcon;
            }
            alt = theme.getMessage("Alarm.majorImageAltText");
            renderAlarmCount(context, writer, alarmStatus, icon,
                    theme.getStyleClass(ThemeStyles.MASTHEAD_ALARM_LINK),
                    numAlarms, alt);
        }

        facet = alarmStatus.getFacet("minorAlarms");

        if (facet != null) {
            // render the developer specified facet for minor alarms
            RenderingUtilities.renderComponent(facet, context);
        } else if (alarmStatus.isMinorAlarms()) {
            // render the minor alarm + count
            numAlarms = alarmStatus.getNumMinorAlarms();
            if (numAlarms != 0) {
                themeIcon = ThemeImages.ALARM_MASTHEAD_MINOR_MEDIUM;
            } else {
                themeIcon = ThemeImages.ALARM_MASTHEAD_MINOR_DIMMED;
            }
            if (alarmStatus.getMinorIcon() != null) {
                icon = alarmStatus.getMinorIcon();
            } else {
                icon = themeIcon;
            }
            alt = theme.getMessage("Alarm.minorImageAltText");
            renderAlarmCount(context, writer, alarmStatus, icon,
                    theme.getStyleClass(ThemeStyles.MASTHEAD_ALARM_LINK),
                    numAlarms, alt);
        }
    }

    /**
     * Render an alarm label.
     *
     * @param context faces context
     * @param alarmStatus alarm status
     * @param writer writer to use
     * @param theme theme in-use
     * @throws IOException if an IO error occurs
     */
    private void renderAlarmLabel(final FacesContext context,
            final AlarmStatus alarmStatus, final ResponseWriter writer,
            final Theme theme) throws IOException {

        writer.startElement("span", alarmStatus);
        addCoreAttributes(context, alarmStatus, writer,
                theme.getStyleClass(ThemeStyles.MASTHEAD_TEXT));

        Object textObj = alarmStatus.getText();
        String text;
        if (textObj == null) {
            text = theme.getMessage("masthead.currentAlarms");
        } else {
            text = convertValueToString(alarmStatus, textObj);
        }

        writer.write(text);
        writer.write("&nbsp;");
        writer.endElement("span");
    }

    /**
     * Render the alarm count.
     * @param context faces context
     * @param writer writer to use
     * @param alarmStatus alarm status component
     * @param icon icon key
     * @param styleClass CSS class
     * @param numAlarms alarm count
     * @param alt alternate text
     * @throws IOException if an IO error occurs
     */
    private void renderAlarmCount(final FacesContext context,
            final ResponseWriter writer, final AlarmStatus alarmStatus,
            final String icon, final String styleClass, final int numAlarms,
            final String alt) throws IOException {

        // We don't want conversion here. This is just to cache
        // the original value so we can restore it after calling
        // renderLink.
        Object realText = alarmStatus.getText();
        String realIcon = alarmStatus.getIcon();
        String realStyleClass = alarmStatus.getStyleClass();
        boolean reallyDisabled = alarmStatus.isDisabled();
        String realAlt = alarmStatus.getAlt();

        // This is really bad practice.
        // A renderer MUST not modify a component.
        // It looks like we need to either make
        // renderLink more granular or reimplement it here.
        //
        alarmStatus.setIcon(icon);
        alarmStatus.setText("&nbsp;" + numAlarms);
        alarmStatus.setStyleClass(styleClass);
        alarmStatus.setAlt(alt);

        if (numAlarms == 0) {
            // alarm count of zero is NOT linked
            alarmStatus.setDisabled(true);
        }

        renderLink(context, alarmStatus, writer);
        writer.write("&nbsp;&nbsp;&nbsp;");

        alarmStatus.setText(realText);
        alarmStatus.setIcon(realIcon);
        alarmStatus.setStyleClass(realStyleClass);
        alarmStatus.setDisabled(reallyDisabled);
        alarmStatus.setAlt(alt);
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // override super so tha we do nothing here
    }

    @Override
    protected void finishRenderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // this method will (usually) be called 4 times to render each alarm
        // image and count as a separate instance of the same anchor tag
        AlarmStatus alarmStatus = (AlarmStatus) component;
        UIComponent image = alarmStatus.getImageFacet();
        String label = convertValueToString(alarmStatus,
                alarmStatus.getText());

        if (image != null) {
            renderComponent(image, context);
        }

        if (label != null && label.length() != 0) {
            writer.write(label);
        }
    }

    /**
     * @param context faces context
     * @param alarmStatus alarm status
     * @param writer writer to sue
     * @param theme theme to sue
     * @param alarmSrc alarm source
     * @throws java.io.IOException if an IO error occurs
     * @deprecated do not use
     */
    private static void renderAlarmImage(final FacesContext context,
            final AlarmStatus alarmStatus, final ResponseWriter writer,
            final Theme theme, final String alarmSrc) throws IOException {

        ImageComponent image = new ImageComponent();
        image.setIcon(alarmSrc);
        RenderingUtilities.renderComponent(image, context);
    }
}
