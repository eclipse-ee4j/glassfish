/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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
import com.sun.webui.jsf.component.Calendar;
import com.sun.webui.jsf.component.CalendarMonth;
import com.sun.webui.jsf.component.ImageHyperlink;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.MessageUtil;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.json.JsonObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.MissingResourceException;

import static com.sun.webui.jsf.util.JavaScriptUtilities.renderInitScriptTag;
import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;

/**
 * Renders an instance of the Calendar component.
 *
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.Calendar"))
public class CalendarRenderer extends FieldRenderer {

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Creates a new instance of CalendarRenderer.
     */
    public CalendarRenderer() {
    }

    /**
     * <p>Render the component end element.</p>
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code UIComponent} to be rendered
     * @exception IOException if an input/output error occurs
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        if (component == null) {
            return;
        }
        if (!(component instanceof Calendar)) {
            Object[] params = {
                component.toString(),
                this.getClass().getName(),
                Calendar.class.getName(),
            };
            String message = MessageUtil.getMessage(
                    "com.sun.webui.jsf.resources.LogMessages",
                    "Renderer.component", params);
            throw new FacesException(message);
        }

        Calendar calendar = (Calendar) component;
        boolean readOnly = calendar.isReadOnly();

        ResponseWriter writer = context.getResponseWriter();
        String[] styles = getStyles(calendar, context);
        String clientId = calendar.getClientId(context);

        // Start a table
        renderTableStart(calendar, styles[18], styles[2], context, writer);

        // Table cell for the label
        UIComponent label = calendar.getLabelComponent(context, null);
        if (label != null) {
            renderCellStart(calendar, styles[6], writer);
            RenderingUtilities.renderComponent(label, context);
            renderCellEnd(writer);
        }

        // Table cell for the field and the date format string
        if (readOnly) {
            renderCellStart(calendar, styles[6], writer);
            UIComponent text = calendar.getReadOnlyComponent(context);
            RenderingUtilities.renderComponent(text, context);
            if (calendar.getValue() != null) {
                renderPattern(calendar, styles[7], styles[2], context, writer);
            }
        } else {
            renderCellStart(calendar, styles[4], writer);
            renderInput(calendar, "text", clientId.concat(Calendar.INPUT_ID),
                    false, styles, context, writer);
            writer.write("\n");
            renderPattern(calendar, styles[7], styles[2], context, writer);
        }

        renderCellEnd(writer);

        if (!readOnly) {

            // Start of table cell
            writer.startElement("td", calendar);
            writer.writeAttribute("valign", "top", null);
            writer.writeText("\n", null);

            // Create a common CSS containing block used for positioning
            writer.startElement("div", calendar);
            // Use relative so that icon is considered in normal flow
            writer.writeAttribute("style", "position: relative;", null);
            writer.writeText("\n", null);

            // This is the span for the link
            writer.startElement("span", calendar);
            writer.writeAttribute("class", styles[5], null);

            ImageHyperlink link =
                    calendar.getDatePickerLink(context);
            if (calendar.isDisabled()) {
                writer.writeAttribute("style", "display:none;", null);
            }
            writer.write("\n");

            link.setIcon(styles[14]);
            link.setToolTip(styles[13]);
            RenderingUtilities.renderComponent(link, context);

            // Close link span
            writer.endElement("span");
            writer.write("\n");

            renderDatePicker(context, writer, styles, calendar);

            // Close the remaining div and table cell
            writer.endElement("div");
            writer.writeText("\n", null);
            writer.endElement("td");
            writer.writeText("\n", null);
        }

        // End the table
        renderTableEnd(writer);
        writer.writeText("\n", null);
    }

    /**
     * Render the date picker.
     * @param context faces context
     * @param writer writer to use
     * @param styles CSS styles
     * @param calendar calendar
     * @throws IOException if an IO error occurs
     */
    private void renderDatePicker(final FacesContext context,
            final ResponseWriter writer, final String[] styles,
            final Calendar calendar) throws IOException {

        // render date picker
        CalendarMonth datePicker = calendar.getDatePicker();
        Object value = calendar.getSubmittedValue();
        if (value != null) {
            try {
                Object dO =
                        ConversionUtilities.convertValueToObject(calendar,
                        (String) value,
                        context);
                datePicker.setValue(dO);
            } catch (Exception ex) {
                // do nothing
            }
        } else if (calendar.getValue() != null) {
            datePicker.setValue(calendar.getValue());
        }
        datePicker.initCalendarControls(
                calendar.getJavaScriptObjectName(context));
        RenderingUtilities.renderComponent(datePicker, context);

        //JS should be initialized by CalendarMonth, not by this component....
        renderJavaScript(context, calendar, writer, styles);
    }

    /**
     * Render the table start.
     * @param calendar calendar
     * @param rootStyle root style
     * @param hiddenStyle hidden style
     * @param context faces context
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void renderTableStart(final Calendar calendar,
            final String rootStyle, final String hiddenStyle,
            final FacesContext context, final ResponseWriter writer)
            throws IOException {

        writer.startElement("table", calendar);
        writer.writeAttribute("border", "0", null);
        writer.writeAttribute("cellspacing", "0", null);
        writer.writeAttribute("cellpadding", "0", null);
        writer.writeAttribute("title", "", null);
        writer.writeAttribute("id", calendar.getClientId(context), "id");
        String style = calendar.getStyle();
        if (style != null && style.length() > 0) {
            writer.writeAttribute("style", style, "style");
        }

        style = getStyleClass(calendar, hiddenStyle);

        // Append a styleclass to top level table element so we can use it to
        // fix a pluto portal bug by restoring the initial width value
        if (style == null) {
            style = rootStyle;
        } else {
            style += " " + rootStyle;
        }
        if (style != null) {
            writer.writeAttribute("class", style, "class");
        }

        writer.writeText("\n", null);
        writer.startElement("tr", calendar);
        writer.writeText("\n", null);
    }

    /**
     * Render table cell start.
     * @param calendar calendar
     * @param style CSS style
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void renderCellStart(final Calendar calendar, final String style,
            final ResponseWriter writer) throws IOException {

        writer.startElement("td", calendar);
        writer.writeAttribute("valign", "top", null);
        writer.writeText("\n", null);
        writer.startElement("span", calendar);
        writer.writeAttribute("class", style, null);
        writer.writeText("\n", null);
    }

    /**
     * Render table cell end.
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void renderCellEnd(final ResponseWriter writer)
            throws IOException {

        writer.writeText("\n", null);
        writer.endElement("span");
        writer.writeText("\n", null);
        writer.endElement("td");
        writer.writeText("\n", null);
    }

    /**
     * Render table end.
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void renderTableEnd(final ResponseWriter writer)
            throws IOException {

        writer.endElement("tr");
        writer.writeText("\n", null);
        writer.endElement("table");
        writer.writeText("\n", null);
    }

    /**
     * Render pattern.
     * @param calendar calendar
     * @param styleClass CSS style
     * @param hiddenStyle hidden CSS style
     * @param context faces context
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void renderPattern(final Calendar calendar, final String styleClass,
            final String hiddenStyle, final FacesContext context,
            final ResponseWriter writer) throws IOException {

        String hint = calendar.getDateFormatPatternHelp();
        if (hint == null) {
            try {
                String pattern = calendar.getDatePicker()
                        .getDateFormatPattern();
                hint = ThemeUtilities.getTheme(context).getMessage(
                        "calendar.".concat(pattern));
            } catch (MissingResourceException mre) {
                hint = ((SimpleDateFormat) (calendar.getDateFormat()))
                        .toLocalizedPattern()
                        .toLowerCase();
            }
        }
        if (hint != null) {
            writer.startElement("div", calendar);
            String id = calendar.getClientId(context);
            id = id.concat(Calendar.PATTERN_ID);
            writer.writeAttribute("id", id, null);
            String style = styleClass;
            if (calendar.isDisabled()) {
                style = style.concat(" ").concat(hiddenStyle);
            }
            writer.writeAttribute("class", style, null);
            writer.writeText(hint, null);
            writer.endElement("div");
        }
    }

    /**
     * Render the JS.
     * @param context faces context
     * @param calendar calendar
     * @param writer writer to use
     * @param styles CSS style
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderJavaScript(final FacesContext context,
            final Calendar calendar, final ResponseWriter writer,
            final String[] styles) throws IOException {

        if (DEBUG) {
            log("renderJavaScript()");
        }

        // First argument is the first day of the week
        int firstDay = calendar.getDatePicker().getCalendar()
                .getFirstDayOfWeek();
        String day = null;
        switch (firstDay) {
            case java.util.Calendar.SUNDAY:
                day = "0";
                break;
            case java.util.Calendar.MONDAY:
                day = "1";
                break;
            case java.util.Calendar.FRIDAY:
                day = "5";
                break;
            case java.util.Calendar.SATURDAY:
                day = "6";
                break;
            case java.util.Calendar.TUESDAY:
                day = "2";
                break;
            case java.util.Calendar.WEDNESDAY:
                day = "3";
                break;
            case java.util.Calendar.THURSDAY:
                day = "4";
                break;
            default:
                break;
        }

        String clientId = calendar.getClientId(context);
        CalendarMonth datePicker = calendar.getDatePicker();
        String datePickerId = datePicker.getClientId(context);

        // Append properties.
        JsonObject initProps = JSON_BUILDER_FACTORY.createObjectBuilder()
                .add("id", calendar.getClientId(context))
                .add("firstDay", day)
                .add("fieldId", clientId.concat(Calendar.INPUT_ID))
                .add("patternId", clientId.concat(Calendar.PATTERN_ID))
                .add("calendarToggleId", calendar.getDatePickerLink(context)
                        .getClientId(context))
                .add("datePickerId", datePickerId)
                .add("monthMenuId", datePicker.getMonthMenu()
                        .getClientId(context))
                .add("yearMenuId", datePicker.getYearMenu()
                        .getClientId(context))
                .add("rowId", datePickerId + ":row5")
                .add("showButtonSrc", styles[8])
                .add("hideButtonSrc", styles[9])
                .add("dateFormat", datePicker.getDateFormatPattern())
                .add("dateClass", styles[10])
                .add("edgeClass", styles[11])
                .add("selectedClass", styles[15])
                .add("edgeSelectedClass", styles[16])
                .add("todayClass", styles[17])
                .add("hiddenClass", styles[2])
                .build();

            renderInitScriptTag(writer, "calendar", initProps);
    }

    /**
     * Get the CSS styles.
     * @param calendar calendar
     * @param context faces context
     * @return String[]
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private String[] getStyles(final Calendar calendar,
            final FacesContext context) {

        Theme theme = ThemeUtilities.getTheme(context);
        String[] styles = new String[19];
        styles[0] = theme.getStyleClass(ThemeStyles.TEXT_FIELD);
        styles[1] = theme.getStyleClass(ThemeStyles.TEXT_FIELD_DISABLED);
        styles[2] = theme.getStyleClass(ThemeStyles.HIDDEN);
        styles[3] = "";
        styles[4] = ""; // used to be CalPopFld, removed
        styles[5] = theme.getStyleClass(ThemeStyles.CALENDAR_FIELD_IMAGE);
        styles[6] = theme.getStyleClass(ThemeStyles.CALENDAR_FIELD_LABEL);
        styles[7] = theme.getStyleClass(ThemeStyles.HELP_FIELD_TEXT);
        styles[8] = theme.getImagePath(ThemeImages.CALENDAR_BUTTON);
        styles[9] = theme.getImagePath(ThemeImages.CALENDAR_BUTTON_FLIP);
        styles[10] = theme.getStyleClass(ThemeStyles.DATE_TIME_LINK);
        styles[11] = theme.getStyleClass(ThemeStyles.DATE_TIME_OTHER_LINK);
        styles[12] = null;
        styles[13] = theme.getMessage("calendar.popupImageAlt");
        styles[14] = ThemeImages.CALENDAR_BUTTON;
        // Style for the selected date
        styles[15] = theme.getStyleClass(ThemeStyles.DATE_TIME_BOLD_LINK);
        // Style for selected date on the edge
        styles[16] = theme.getStyleClass(ThemeStyles.DATE_TIME_OTHER_BOLD_LINK);
        // Style for today's date
        styles[17] = theme.getStyleClass(ThemeStyles.DATE_TIME_TODAY_LINK);
        styles[18] = theme.getStyleClass(ThemeStyles.CALENDAR_ROOT_TABLE);
        return styles;
    }

    /**
     * Log an error - only used during development time.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(CalendarRenderer.class.getName() + "::" + msg);
    }
}
