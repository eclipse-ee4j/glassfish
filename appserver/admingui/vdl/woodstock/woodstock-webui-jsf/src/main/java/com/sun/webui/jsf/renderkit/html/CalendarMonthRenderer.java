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
import com.sun.webui.jsf.component.CalendarMonth;
import com.sun.webui.jsf.component.DateManager;
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.Icon;
import com.sun.webui.jsf.component.IconHyperlink;
import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.MessageUtil;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.sun.webui.jsf.util.RenderingUtilities.renderAnchor;
import static com.sun.webui.jsf.util.RenderingUtilities.renderComponent;

/**
 * This class needs to be rewritten.
 */
@Renderer(@Renderer.Renders(
        componentFamily = "com.sun.webui.jsf.CalendarMonth"))
public final class CalendarMonthRenderer extends AbstractRenderer {

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Skip section.
     */
    private static final String SKIP_SECTION = "skipSection";

    /**
     * Calendar close image id.
     */
    private static final String CLOSE_IMAGE = "_closeImage";

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {
    }

    @Override
    public void encodeBegin(final FacesContext context,
            final UIComponent component) throws IOException {
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        super.decode(context, component);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        if (DEBUG) {
            log("encodeEnd()");
        }

        if (component == null) {
            return;
        }

        if (!(component instanceof CalendarMonth)) {
            Object[] params = {
                component.toString(),
                this.getClass().getName(),
                CalendarMonth.class.getName()
            };
            String message = MessageUtil.getMessage(
                    "com.sun.webui.jsf.resources.LogMessages",
                    "Renderer.component", params);
            throw new FacesException(message);
        }

        CalendarMonth calendarMonth = (CalendarMonth) component;
        SimpleDateFormat dateFormat =
                (SimpleDateFormat) calendarMonth.getDateFormat();
        initializeChildren(calendarMonth, dateFormat, context);

        ResponseWriter writer = context.getResponseWriter();
        Theme theme = ThemeUtilities.getTheme(context);
        String[] styles = getStyles(calendarMonth, context, theme);

        //renderComponent(calendarMonth.getDateField(), context);
        //writer.write("\n");

        String id = calendarMonth.getClientId(context);
        if (calendarMonth.isPopup()) {
            renderPopupStart(calendarMonth, id, styles, context, writer);
        } else {
            writer.startElement("div", calendarMonth);
            writer.writeAttribute("id", id, null);
            writer.writeText("\n", null);
        }

        // render calendar header
        renderCalendarHeader(calendarMonth, styles, context, writer, theme);

        // render the begining of the layout for calendar controls and days of
        // the week.
        writer.startElement("div", calendarMonth);
        writer.writeAttribute("class", styles[4], null);
        writer.writeText("\n", null);

        renderCalendarControls(calendarMonth, styles, context, writer);
        renderDateTable(calendarMonth, styles, id, dateFormat, context, writer);

        // close the div
        writer.endElement("div");
        writer.write("\n");

        if (calendarMonth.isPopup()) {
            renderPopupEnd(writer);
        } else {
            writer.endElement("div");
            writer.write("\n");
        }
    }

    /**
     * Render a spacer image.
     *
     * @param context The current FacesContext
     * @param calendarMonth The CalendarMonth component instance
     * @param theme The current Theme
     * @param height The height to use for the spacer image
     * @param width The width to use for the spacer image
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderSpacerImage(final FacesContext context,
            final CalendarMonth calendarMonth, final Theme theme,
            final int height, final int width) throws IOException {

        // TODO - this generates a component with no id set! Has to be fixed.
        Icon dot = ThemeUtilities.getIcon(theme, ThemeImages.DOT);
        dot.setWidth(width);
        dot.setHeight(height);
        dot.setId("icon");
        dot.setAlt("");

        renderComponent(dot, context);
    }

    /**
     * Render table start.
     * @param calendarMonth UI component
     * @param styles CSS styles
     * @param writer write to use
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderDateTableStart(final CalendarMonth calendarMonth,
            final String[] styles, final ResponseWriter writer)
            throws IOException {

        writer.startElement("div", calendarMonth);
        writer.writeAttribute("class", styles[4], null);
        writer.write("\n");
        writer.startElement("table", calendarMonth);
        writer.writeAttribute("width", "100%", null);
        writer.writeAttribute("cellspacing", "1", null);
        writer.writeAttribute("cellpadding", "0", null);
        writer.writeAttribute("border", "0", null);
        writer.write("\n");
        writer.startElement("tbody", calendarMonth);
        writer.writeAttribute("class", styles[5], null);
        writer.write("\n");
    }

    /**
     * Render day header row.
     * @param calendarMonth UI component
     * @param styles CSS styles
     * @param context faces context
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderDayHeaderRow(final CalendarMonth calendarMonth,
            final String[] styles, final FacesContext context,
            final ResponseWriter writer) throws IOException {

        writer.startElement("tr", calendarMonth);
        writer.write("\n");

        int firstDay = calendarMonth.getCalendar().getFirstDayOfWeek();

        String[] daysOfWeek = new String[8];
        daysOfWeek[Calendar.MONDAY] = styles[8];
        daysOfWeek[Calendar.TUESDAY] = styles[9];
        daysOfWeek[Calendar.WEDNESDAY] = styles[10];
        daysOfWeek[Calendar.THURSDAY] = styles[11];
        daysOfWeek[Calendar.FRIDAY] = styles[12];
        daysOfWeek[Calendar.SATURDAY] = styles[13];
        daysOfWeek[Calendar.SUNDAY] = styles[14];

        String styleClass = styles[15];

        for (int i = 0; i < 7; i++) {
            // render a table header for each day of the week
            renderWeekdayHeader(calendarMonth, writer, styleClass,
                    daysOfWeek[firstDay]);
            writer.write("\n");
            firstDay++;
            if (firstDay == 8) {
                firstDay = 1;
            }
        }
        writer.endElement("tr");
        writer.write("\n");
    }

    /**
     * Render the calendarMonth header containing the weekday table headers.
     *
     * @param calendarMonth The CalendarMonth component instance
     * @param writer The current ResponseWriter
     * @param styleClass The style class to use for the table header cell
     * @param header The contents to write for the table header cell
     *
     * @exception IOException if an input/output error occurs
     */
    private void renderWeekdayHeader(final CalendarMonth calendarMonth,
            final ResponseWriter writer, final String styleClass,
            final String header) throws IOException {

        // render a column header with the given style and header text
        writer.startElement("th", calendarMonth);
        writer.writeAttribute("align", "center", null);
        writer.writeAttribute("scope", "col", null);
        writer.write("\n");
        writer.startElement("span", calendarMonth);
        writer.writeAttribute("class", styleClass, null);
        writer.write("\n");
        writer.writeText(header, null);
        writer.write("\n");
        writer.endElement("span");
        writer.write("\n");
        writer.endElement("th");
    }

    /**
     * Render days.
     * @param calendarMonth UI component
     * @param id element id
     * @param styles CSS styles
     * @param dateFormat date format
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderDays(final CalendarMonth calendarMonth,
            final String id, final String[] styles, final DateFormat dateFormat,
            final ResponseWriter writer) throws IOException {

        // now render each week in a row with each day in a td
        Calendar monthToShow = calendarMonth.getCalendar();
        monthToShow.set(Calendar.YEAR, calendarMonth.getCurrentYear());
        monthToShow.set(Calendar.MONTH, calendarMonth.getCurrentMonth() - 1);
        monthToShow.set(Calendar.DAY_OF_MONTH, 1);

        if (DEBUG) {
            log("Month to show " + monthToShow.getTime().toString());
        }
        // get the int constant repsenting the day of the week (i.e. SUNDAY)
        int weekStartDay = monthToShow.getFirstDayOfWeek();

        // Get the startDate
        Calendar startDate = (Calendar) (monthToShow.clone());
        while (startDate.get(Calendar.DAY_OF_WEEK) != weekStartDay) {
            startDate.add(Calendar.DATE, -1);
            startDate.getTime();
        }
        if (DEBUG) {
            log("First day " + startDate.getTime().toString());
        }

        // Get the end date
        Calendar endDate = (Calendar) (monthToShow.clone());
        endDate.add(Calendar.MONTH, 1);
        endDate.getTime();

        if (endDate.get(Calendar.DAY_OF_WEEK) == weekStartDay) {
            endDate.add(Calendar.DATE, -1);
            endDate.getTime();
        } else {
            while (endDate.get(Calendar.DAY_OF_WEEK) != weekStartDay) {
                endDate.add(Calendar.DATE, 1);
                endDate.getTime();
            }
            endDate.add(Calendar.DATE, -1);
            endDate.getTime();
        }
        if (DEBUG) {
            log("Last day " + endDate.getTime().toString());
        }

        String rowIdPrefix = id.concat(":row");
        String dateLinkPrefix = id.concat(":dateLink");
        boolean selected;
        boolean dayInMonth;
        int displayedMonth = monthToShow.get(Calendar.MONTH);
        Calendar todaysDate = calendarMonth.getCalendar();

        int dateLinkId = 0;
        int rowNum = 0;
        while (startDate.before(endDate)) {

            writer.startElement("tr", calendarMonth);
            String rowId = rowIdPrefix + rowNum++;
            writer.writeAttribute("id", rowId, null);
            writer.write("\n");

            for (int i = 0; i < 7; ++i) {

                if (DEBUG) {
                    log("Now rendering " + startDate.getTime().toString());
                }

                selected = calendarMonth.isDateSelected(startDate, endDate);
                dayInMonth = (startDate.get(Calendar.MONTH) == displayedMonth);
                String style = styles[17];

                if (selected) {
                    if (dayInMonth) {
                        style = styles[18];
                    } else {
                        style = styles[19];
                    }
                } else if (dayInMonth) {
                    if (calendarMonth.compareDate(startDate, todaysDate)) {
                        style = styles[20];
                    } else {
                        style = styles[16];
                    }
                } else if (DEBUG) {
                    log("Date is outside month and selected");
                }

                renderDateLink(startDate, style,
                        dateLinkPrefix.concat(String.valueOf(dateLinkId)),
                        calendarMonth,
                        dateFormat, writer);

                dateLinkId++;
                startDate.add(Calendar.DAY_OF_YEAR, 1);
                startDate.getTime();
            }
            writer.endElement("tr");
            writer.write("\n");
        }
        if (rowNum < 6) {
            writer.startElement("tr", calendarMonth);
            String rowId = rowIdPrefix + rowNum++;
            writer.writeAttribute("id", rowId, null);
            writer.writeAttribute("style", "display:none;", null);
            writer.write("\n");

            for (int i = 0; i < 7; ++i) {
                renderDateLink(startDate, styles[17],
                        dateLinkPrefix.concat(String.valueOf(dateLinkId)),
                        calendarMonth, dateFormat, writer);

                dateLinkId++;
                startDate.add(Calendar.DAY_OF_YEAR, 1);
                startDate.getTime();
            }
            writer.endElement("tr");
            writer.write("\n");
        }
    }

    /**
     * Render date link.
     * @param startDate start date
     * @param style CSS style
     * @param id element id
     * @param calendarMonth UI component
     * @param dateFormat date format
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderDateLink(final Calendar startDate, final String style,
            final String id, final CalendarMonth calendarMonth,
            final DateFormat dateFormat, final ResponseWriter writer)
            throws IOException {

        writer.startElement("td", calendarMonth);
        writer.writeAttribute("align", "center", null);
        writer.writeText("\n", null);

        int day = startDate.get(Calendar.DAY_OF_MONTH);

        // For performance reasons, don't create a hyperlink component
        // for each date...
        writer.startElement("a", calendarMonth);
        writer.writeAttribute("class", style, null);
        writer.writeAttribute("id", id, null);

        String dateString = dateFormat.format(startDate.getTime());
        writer.writeAttribute("title", dateString, null);

        StringBuilder buffer = new StringBuilder(128);

        if (calendarMonth.isPopup()) {
            buffer.append(calendarMonth.getJavaScriptObjectName());
            buffer.append(".dayClicked(this); return false;");
        } else {
            buffer.append(calendarMonth.getJavaScriptObjectName());
            buffer.append(".setDateValue('");
            buffer.append(dateString);
            buffer.append("', this); return false;");
        }

        writer.writeAttribute("onclick", buffer.toString(), null);
        writer.writeAttribute("href", "#", null);
        writer.write(String.valueOf(day));
        writer.endElement("a");
        writer.write("\n");
        writer.endElement("td");
        writer.write("\n");
    }

    /**
     * Render end of table.
     * @param writer write to use
     * @throws IOException if an IO error occurs
     */
    private void renderDateTableEnd(final ResponseWriter writer)
            throws IOException {

        writer.endElement("tbody");
        writer.write("\n");
        writer.endElement("table");
        writer.write("\n");
        writer.endElement("div");
        writer.write("\n");
    }

    /**
     * Render the calendar header.
     * @param calendarMonth UI component
     * @param styles CSS styles
     * @param context faces context
     * @param writer writer to use
     * @param theme theme in-use
     * @throws IOException if an IO error occurs
     */
    private void renderCalendarHeader(final CalendarMonth calendarMonth,
            final String[] styles, final FacesContext context,
            final ResponseWriter writer, final Theme theme) throws IOException {

        renderHeaderTable(calendarMonth, writer, styles, theme);
        renderTodayDate(calendarMonth, writer, styles, theme, context);
    }

    /**
     * Render a table to layout the rounded corners and top border of the
     * calendar header.
     * @param calendarMonth UI component
     * @param writer writer to use
     * @param styles CSS styles
     * @param theme theme in-use
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderHeaderTable(final CalendarMonth calendarMonth,
            final ResponseWriter writer, final String[] styles,
            final Theme theme) throws IOException {

        writer.startElement("table", calendarMonth);
        writer.writeAttribute("border", "0", null);
        writer.writeAttribute("cellpadding", "0", null);
        writer.writeAttribute("cellspacing", "0", null);
        writer.writeAttribute("width", "100%", null);
        writer.write("\n");
        writer.startElement("tr", calendarMonth);
        writer.writeText("\n", null);

        // Render the header's left corner.
        renderHeaderCornerCell(calendarMonth, writer,
                theme.getImagePath(ThemeImages.SCHEDULER_TOP_LEFT));

        // Render a spacer column.
        String path = theme.getImagePath(ThemeImages.DOT);
        writer.startElement("td", calendarMonth);
        writer.writeAttribute("class", styles[23], null);
        writer.writeAttribute("width", "100%", null);
        writer.startElement("img", calendarMonth);
        writer.writeAttribute("src", path, null);
        writer.writeAttribute("height", String.valueOf(1), null);
        writer.writeAttribute("width", String.valueOf(10), null);
        writer.writeAttribute("alt", "", null);
        writer.endElement("img");
        writer.endElement("td");
        writer.writeText("\n", null);

        // Render the header's right corner.
        renderHeaderCornerCell(calendarMonth, writer,
                theme.getImagePath(ThemeImages.SCHEDULER_TOP_RIGHT));

        // Close the table.
        writer.endElement("tr");
        writer.writeText("\n", null);
        writer.endElement("table");
        writer.writeText("\n", null);
    }

    /**
     * Render header corner cell.
     * @param calendarMonth UI component
     * @param writer writer to use
     * @param src image source attribute value
     * @throws IOException if an IO error occurs
     */
    private void renderHeaderCornerCell(final CalendarMonth calendarMonth,
            final ResponseWriter writer, final String src) throws IOException {

        writer.startElement("td", calendarMonth);
        writer.writeAttribute("width", "6", null);
        writer.writeText("\n", null);
        writer.startElement("img", calendarMonth);
        writer.writeAttribute("src", src, null);
        writer.writeAttribute("alt", "", null);
        writer.endElement("img");
        writer.endElement("td");
        writer.writeText("\n", null);
    }

    /**
     * Render today's date in the header.
     * @param calendarMonth UI component
     * @param writer writer to use
     * @param context faces context
     * @param styles CSS styles
     * @param theme theme in-use
     * @throws IOException if an IO error occurs
     */
    private void renderTodayDate(final CalendarMonth calendarMonth,
            final ResponseWriter writer, final String[] styles,
            final Theme theme, final FacesContext context) throws IOException {

        renderTodayDateBegin(calendarMonth, writer, styles);
        renderTodayDateText(calendarMonth, writer, context, styles, theme);

        if (calendarMonth.isPopup()) {
            renderCloseButton(calendarMonth, styles, context, writer, theme);
        }

        renderTodayDateEnd(writer);
    }

    /**
     * Render the beginning of the layout for today date.
     * @param calendarMonth UI component
     * @param writer writer to use
     * @param styles CSS styles
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderTodayDateBegin(final CalendarMonth calendarMonth,
            final ResponseWriter writer, final String[] styles)
            throws IOException {

        writer.startElement("div", calendarMonth);
        writer.writeAttribute("class", styles[22], null);
        writer.write("\n");
    }

    /**
     * Render today date text.
     * @param calendarMonth UI component
     * @param writer writer to use
     * @param context faces context
     * @param styles CSS styles
     * @param theme theme in-use
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderTodayDateText(final CalendarMonth calendarMonth,
            final ResponseWriter writer, final FacesContext context,
            final String[] styles, final Theme theme) throws IOException {

        writer.startElement("div", calendarMonth);
        writer.writeAttribute("class", styles[24], null);

        DateFormat dateFormat =
                SimpleDateFormat.getDateInstance(DateFormat.MEDIUM,
                context.getViewRoot().getLocale());
        dateFormat.setTimeZone((TimeZone) (calendarMonth.getTimeZone()));
        Date today = calendarMonth.getCalendar().getTime();
        if (DEBUG) {
            log("Today is " + today.toString());
        }
        String[] detailArg = {dateFormat.format(today)};
        String detailMsg = theme.getMessage("CalendarMonth.todayIs", detailArg);
        writer.writeText(detailMsg, null);

        writer.endElement("div");
        writer.writeText("\n", null);
    }

    /**
     * Render the end of the layout for today date.
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void renderTodayDateEnd(final ResponseWriter writer)
            throws IOException {

        writer.endElement("div");
        writer.writeText("\n", null);
    }

    /**
     * Render the calendarMonth controls: the month and year menus as well as
     * the previous and next month IconHyperlink's.
     *
     * @param context The current FacesContext
     * @param styles CSS styles
     * @param calendarMonth The CalendarMonth component instance
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderCalendarControls(final CalendarMonth calendarMonth,
            final String[] styles, final FacesContext context,
            final ResponseWriter writer) throws IOException {

        if (DEBUG) {
            log("renderCalendarControls");
        }

        String id = calendarMonth.getClientId(context);
        writer.startElement("div", calendarMonth);
        writer.writeAttribute("class", styles[3], null);
        writer.writeText("\n", null);

        writer.startElement("div", calendarMonth);
        writer.writeAttribute("class", styles[26], null);

        String pattern = calendarMonth.getDateFormatPattern();
        if (pattern.indexOf("yyyy") < pattern.indexOf("MM")) {
            renderYearControl(calendarMonth, styles, context, writer);
            writer.endElement("div");
            writer.writeText("\n", null);
            writer.startElement("div", calendarMonth);
            writer.writeAttribute("class", styles[27], null);
            renderMonthControl(calendarMonth, styles, context, writer);
        } else {
            renderMonthControl(calendarMonth, styles, context, writer);
            writer.endElement("div");
            writer.writeText("\n", null);
            writer.startElement("div", calendarMonth);
            writer.writeAttribute("class", styles[27], null);
            renderYearControl(calendarMonth, styles, context, writer);
        }

        writer.endElement("div");
        writer.writeText("\n", null);
        writer.endElement("div");
        writer.writeText("\n", null);
    }

    /**
     * Render the year control.
     * @param calendarMonth month
     * @param styles CSS styles
     * @param context faces context
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderYearControl(final CalendarMonth calendarMonth,
            final String[] styles, final FacesContext context,
            final ResponseWriter writer) throws IOException {

        if (DEBUG) {
            log("renderYearControl()");
        }
        DropDown yearDropDown = calendarMonth.getYearMenu();
        yearDropDown.setToolTip(styles[6]);
        RenderingUtilities.renderComponent(yearDropDown, context);
        writer.write("\n");
    }

    /**
     * Render the month control.
     * @param calendarMonth month
     * @param styles CSS styles
     * @param context faces context
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderMonthControl(final CalendarMonth calendarMonth,
            final String[] styles, final FacesContext context,
            final ResponseWriter writer) throws IOException {

        if (DEBUG) {
            log("renderMonthControl()");
        }
        IconHyperlink decreaseLink = calendarMonth.getPreviousMonthLink();
        RenderingUtilities.renderComponent(decreaseLink, context);
        writer.write("\n");
        if (DEBUG) {
            log("\t rendered PreviousLink");
        }

        if (DEBUG) {
            log(calendarMonth.toString());
        }
        DropDown monthDropDown = calendarMonth.getMonthMenu();
        monthDropDown.setToolTip(styles[7]);
        if (DEBUG) {
            log("Got DropDown");
        }
        RenderingUtilities.renderComponent(monthDropDown, context);
        writer.write("\n");
        if (DEBUG) {
            log("\t rendered Month Menu");
        }

        IconHyperlink increaseLink = calendarMonth.getNextMonthLink();
        RenderingUtilities.renderComponent(increaseLink, context);
        writer.write("\n");
        if (DEBUG) {
            log("\t rendered IncreaseLink");
        }
    }

    /**
     * Render the close button.
     * @param calendarMonth month
     * @param styles CSS styles
     * @param context faces context
     * @param writer writer to use
     * @param theme theme to use
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderCloseButton(final CalendarMonth calendarMonth,
            final String[] styles, final FacesContext context,
            final ResponseWriter writer, final Theme theme) throws IOException {

        RenderingUtilities.renderAnchor(SKIP_SECTION, calendarMonth, context);

        StringBuilder strBuffer = new StringBuilder(128);
        strBuffer.append(calendarMonth.getJavaScriptObjectName());
        strBuffer.append(".setInitialFocus(); ");
        strBuffer.append(calendarMonth.getJavaScriptObjectName());
        strBuffer.append(".toggle(); return false;");

        writer.startElement("a", calendarMonth);
        writer.writeAttribute("onclick", strBuffer.toString(), null);
        writer.writeAttribute("class", styles[25], null);
        writer.writeAttribute("href", "#", null);

        Icon icon = ThemeUtilities.getIcon(theme,
                ThemeImages.CALENDAR_CLOSE_BUTTON);
        icon.setParent(calendarMonth);
        icon.setId(CLOSE_IMAGE);
        RenderingUtilities.renderComponent(icon, context);

        writer.endElement("a");
        writer.write("\n");
    }

    /**
     * Render the popup start.
     * @param calendarMonth month
     * @param id component id
     * @param styles CSS styles
     * @param context faces context
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderPopupStart(final CalendarMonth calendarMonth,
            final String id, final String[] styles, final FacesContext context,
            final ResponseWriter writer) throws IOException {

        writer.startElement("div", calendarMonth);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute("class", styles[0], null);
        writer.writeText("\n", null);

        writer.startElement("div", calendarMonth);
        writer.writeAttribute("class", styles[1], null);
        writer.writeText("\n", null);

        writer.startElement("div", calendarMonth);
        writer.writeAttribute("class", styles[2], null);
        writer.writeText("\n", null);
    }

    /**
     * Render the popup end.
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void renderPopupEnd(final ResponseWriter writer)
            throws IOException {

        writer.endElement("div");
        writer.write("\n");
        writer.endElement("div");
        writer.write("\n");
        writer.endElement("div");
        writer.write("\n");
    }

    /**
     * Render the date table.
     * @param calendarMonth month
     * @param styles CSS styles
     * @param id component id
     * @param dateFormat date format
     * @param context faces context
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderDateTable(final CalendarMonth calendarMonth,
            final String[] styles, final String id,
            final DateFormat dateFormat, final FacesContext context,
            final ResponseWriter writer) throws IOException {

        RenderingUtilities.renderSkipLink(SKIP_SECTION, styles[21], null,
                null, null, calendarMonth, context);

        renderDateTableStart(calendarMonth, styles, writer);
        renderDayHeaderRow(calendarMonth, styles, context, writer);
        renderDays(calendarMonth, id, styles, dateFormat, writer);
        renderDateTableEnd(writer);

        if (!calendarMonth.isPopup()) {
            renderAnchor(SKIP_SECTION, calendarMonth, context);
        }
    }

    /**
     * Initialize the children.
     * @param cm month
     * @param dateFormat date format
     * @param context faces context
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void initializeChildren(final CalendarMonth cm,
            final SimpleDateFormat dateFormat, final FacesContext context) {

        if (DEBUG) {
            log("initializeChildren()");
        }
        // This variable is used to track whether the calendar
        // controls have to be updated based on the calculations
        // performed in this method.
        boolean updateCalendarControls = false;

        // Get a calendar instance with the correct timezone and locale
        // from the CalendarMonth component. This calendar is initialized
        // with today's date.
        Calendar calendar = cm.getCalendar();

        // The displayDate reflects the month that will be displayed
        // (we only use the year and month component of the date).
        // We start by assuming that this will be the today's date
        Date displayDate = calendar.getTime();

        // Find out what the current year and month settings are of the
        // CalendarMonth component (this is whatever the user has
        // selected using the menus and the buttons on the control row).
        // Update the calendar with this data - it will be used when
        // calculating the dates on the display.
        // If the user hasn't made any selections yet, the values
        // will be updated later, and will be based on today's date.
        Integer year = cm.getCurrentYear();
        Integer month = cm.getCurrentMonth();
        if (year != null && month != null) {
            if (DEBUG) {
                log("Menus have values...");
            }
            if (DEBUG) {
                log("Month is " + month.toString());
            }
            if (DEBUG) {
                log("Year is " + year.toString());
            }
            calendar.set(Calendar.YEAR, year);
            // Adjust for the fact that we display the months as 1 - 12, but
            // java.util.Calendar has them as 0 to 11.
            calendar.set(Calendar.MONTH, month - 1);
        //calendar.set(Calendar.DAY_OF_MONTH, 1);
        } else {
            updateCalendarControls = true;
        }

        // Calculate which years should be displayed, based on the
        // settings of the of the CalendarMonth component
        // We should probably store these options as an attribute,
        // instead of calculating them every time.

        // Calculate min and max dates
        Date minDate = null;
        Date maxDate = null;

        UIComponent parent = cm.getParent();
        if (parent instanceof DateManager) {
            minDate = ((DateManager) parent).getFirstAvailableDate();
            maxDate = ((DateManager) parent).getLastAvailableDate();
        }

        if (DEBUG) {
            log("Min date set to  " + minDate.toString());
            log("Max date set to " + maxDate.toString());
            log("Date to display is " + displayDate.toString());
        }

        if (displayDate.before(minDate)) {
            if (DEBUG) {
                log("date is before mindate");
            }
            displayDate = minDate;
            updateCalendarControls = true;
        }
        if (maxDate.before(displayDate)) {
            if (DEBUG) {
                log("date is after maxdate");
            }
            displayDate = maxDate;
            updateCalendarControls = true;
        }

        DropDown yearMenu = cm.getYearMenu();
        DropDown monthMenu = cm.getMonthMenu();

        if (updateCalendarControls) {
            calendar.setTime(displayDate);
            String yearValue = String.valueOf(calendar.get(Calendar.YEAR));
            yearMenu.setSubmittedValue(new String[]{yearValue});
            if (DEBUG) {
                log("Value of year: " + yearValue);
            }

            String monthValue = String.valueOf(
                    calendar.get(Calendar.MONTH) + 1);
            monthMenu.setSubmittedValue(new String[]{monthValue});
            if (DEBUG) {
                log("Value of month: " + monthValue);
            }
        }

        // Calculate the years to show on the menu.
        calendar.setTime(minDate);
        int firstYear = calendar.get(Calendar.YEAR);
        calendar.setTime(maxDate);
        int lastYear = calendar.get(Calendar.YEAR);

        int numYears = lastYear - firstYear + 1;
        Integer yearInteger;
        Option[] yearOptions = new Option[numYears];
        for (int i = 0; i < numYears; ++i) {
            yearInteger = firstYear + i;
            yearOptions[i] = new Option(yearInteger, yearInteger.toString());
        }
        yearMenu.setItems(yearOptions);

        // Set the items of the month component
        // construct an option[] for the locale specific months
        String[] monthNames = dateFormat.getDateFormatSymbols().getMonths();
        Option[] months = new Option[12];

        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        int monthInt;
        for (int i = 0; i < 12; i++) {
            monthInt = calendar.get(Calendar.MONTH);
            months[i] = new Option(monthInt + 1, monthNames[i]);
            calendar.add(Calendar.MONTH, 1);
        }
        if (DEBUG) {
            log("Created the month options");
        }
        monthMenu.setItems(months);

        if (DEBUG) {
            log("initializeChildren() - END");
        }
    }

    /**
     * Get the styles.
     * @param calendarMonth UI component
     * @param context faces context
     * @param theme theme in-use
     * @return String[]
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private String[] getStyles(final CalendarMonth calendarMonth,
            final FacesContext context, final Theme theme) {

        String[] styles = new String[28];
        styles[0] = theme.getStyleClass(ThemeStyles.CALENDAR_DIV_SHOW);
        styles[1] = theme.getStyleClass(ThemeStyles.CALENDAR_DIV_SHOW2);
        styles[2] = theme.getStyleClass(ThemeStyles.CALENDAR_DIV);
        styles[3] = theme.getStyleClass(ThemeStyles.DATE_TIME_SELECT_DIV);
        styles[4] = theme.getStyleClass(ThemeStyles.DATE_TIME_CALENDAR_DIV);
        styles[5] = theme.getStyleClass(ThemeStyles.DATE_TIME_CALENDAR_TABLE);
        styles[6] = theme.getMessage("CalendarMonth.selectYear");
        styles[7] = theme.getMessage("CalendarMonth.selectMonth");
        styles[8] = theme.getMessage("CalendarMonth.weekdayMon");
        styles[9] = theme.getMessage("CalendarMonth.weekdayTue");
        styles[10] = theme.getMessage("CalendarMonth.weekdayWed");
        styles[11] = theme.getMessage("CalendarMonth.weekdayThu");
        styles[12] = theme.getMessage("CalendarMonth.weekdayFri");
        styles[13] = theme.getMessage("CalendarMonth.weekdaySat");
        styles[14] = theme.getMessage("CalendarMonth.weekdaySun");
        styles[15] = theme.getStyleClass(ThemeStyles.DATE_TIME_DAY_HEADER);
        styles[16] = theme.getStyleClass(ThemeStyles.DATE_TIME_LINK);
        styles[17] = theme.getStyleClass(ThemeStyles.DATE_TIME_OTHER_LINK);
        styles[18] = theme.getStyleClass(ThemeStyles.DATE_TIME_BOLD_LINK);
        styles[19] = theme.getStyleClass(ThemeStyles.DATE_TIME_OTHER_BOLD_LINK);
        styles[20] = theme.getStyleClass(ThemeStyles.DATE_TIME_TODAY_LINK);
        styles[21] = theme.getStyleClass(ThemeStyles.SKIP_MEDIUM_GREY1);
        styles[22] = theme.getStyleClass(ThemeStyles.DATE_TIME_SELECT_CONTENT);
        styles[23] = theme.getStyleClass(
                ThemeStyles.DATE_TIME_SELECT_TOP_MIDDLE);
        styles[24] = theme.getStyleClass(ThemeStyles.DATE_TIME_SELECT_DATE);
        styles[25] = theme.getStyleClass(ThemeStyles.CALENDAR_CLOSE_BUTTON);
        styles[26] = theme.getStyleClass(ThemeStyles.DATE_TIME_CALENDAR_LEFT);
        styles[27] = theme.getStyleClass(ThemeStyles.DATE_TIME_CALENDAR_RIGHT);
        return styles;
    }

    /**
     * Log a message to the standard output.
     * @param msg message to log
     */
    private void log(final String msg) {
        LogUtil.finest(this.getClass().getName() + "::" + msg);
    }
}
