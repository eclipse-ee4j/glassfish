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

package com.sun.webui.jsf.component;

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.model.ScheduledEvent;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.IntegerConverter;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;

import java.beans.Beans;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <h3>NOT FOR DEVELOPER USE - base renderer class for {@code ui:calendar} and
 * {@code ui:scheduler}</h3>
 * <p>Do not release as API.</p>
 */
@Component(type = "com.sun.webui.jsf.CalendarMonth",
        family = "com.sun.webui.jsf.CalendarMonth",
        displayName = "Calendar Month", isTag = false,
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_calendar_month",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_calendar_month_props")
        //CHECKSTYLE:ON
public final class CalendarMonth extends UIOutput implements NamingContainer {

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Month menu id.
     */
    public static final String MONTH_MENU_ID = "monthMenu";

    /**
     * Year menu id.
     */
    public static final String YEAR_MENU_ID = "yearMenu";

    /**
     * Previous month link id.
     */
    public static final String PREVIOUS_MONTH_LINK_ID = "previousMonthLink";

    /**
     * Next month link id.
     */
    public static final String NEXT_MONTH_LINK_ID = "nextMonthLink";

    /**
     * Date link id.
     */
    public static final String DATE_LINK_ID = "dateLink";

    /**
     * Date field id.
     */
    public static final String DATE_FIELD_ID = "dateField";

    /**
     * Date format attribute.
     */
    public static final String DATE_FORMAT_ATTR = "dateFormatAttr";

    /**
     * Date format pattern attribute.
     */
    public static final String DATE_FORMAT_PATTERN_ATTR =
            "dateFormatPatternAttr";

    /**
     * Time zone attribute.
     */
    private static final String TIME_ZONE_ATTR = "timeZoneAttr";

    /**
     * The {@code java.util.Calendar} object to use for this CalendarMonth
     * component.
     */
    private java.util.Calendar calendar = null;

    /**
     * Holds value of property javaScriptObject.
     */
    private String javaScriptObjectName = null;

    /**
     * Flag determining whether the component should be rendered in its
     * popup version (as used by Calendar), or in the
     * inline version used by Scheduler.
     */
    @Property(name = "popup",
            displayName = "Popup Version",
            category = "Behavior")
    private boolean popup = false;

    /**
     * popup set flag.
     */
    private boolean popupSet = false;

    /**
     * Default constructor.
     */
    public CalendarMonth() {
        super();
        setRendererType("com.sun.webui.jsf.CalendarMonth");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.CalendarMonth";
    }

    /**
     * Test if the specified date range is selected.
     * @param current first date in the range
     * @param endDate last date in the range
     * @return {@code boolean}
     */
    public boolean isDateSelected(final java.util.Calendar current,
            final java.util.Calendar endDate) {

        if (DEBUG) {
            log("isDateSelected()");
        }
        Object value = getValue();
        if (value == null) {
            if (DEBUG) {
                log("Value is null");
            }
            return false;
        } else if (value instanceof Date) {
            if (DEBUG) {
                log("Value is date");
            }
            Calendar cal = getCalendar();
            cal.setTime((Date) value);
            return compareDate(cal, current);
        } else if (value instanceof ScheduledEvent) {
            if (DEBUG) {
                log("Value is ScheduledEvent");
            }
            if (DEBUG) {
                log("Checking dates before " + endDate.getTime().toString());
            }
            Iterator dates = ((ScheduledEvent) value).getDates(endDate);
            Calendar cal;
            while (dates.hasNext()) {
                cal = (Calendar) (dates.next());
                if (compareDate(cal, current)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * Compare the two specified dates.
     * @param selected first date to compare
     * @param current second date to compare
     * @return {@code boolean}
     */
    public boolean compareDate(final java.util.Calendar selected,
            final java.util.Calendar current) {

        if (DEBUG) {
            log("Rendered data is " + current.getTime().toString());
        }
        if (DEBUG) {
            log("Compare to " + selected.getTime().toString());
        }
        if (selected.get(Calendar.YEAR) == current.get(Calendar.YEAR)
                && selected.get(Calendar.MONTH) == current.get(Calendar.MONTH)
                && selected.get(Calendar.DAY_OF_MONTH) == current
                        .get(Calendar.DAY_OF_MONTH)) {
            if (DEBUG) {
                log("Found match");
            }
            return true;
        }
        return false;
    }

    /**
     * Returns a new Calendar instance.
     *
     * @return {@code java.util.Calendar} A new Calendar instance with the
     * correct locale and time zone.
     */
    public java.util.Calendar getCalendar() {
        if (DEBUG) {
            log("getCalendar()");
        }

        if (calendar == null) {
            if (DEBUG) {
                log("...Initializing....");
            }
            initializeCalendar();
        }
        return (java.util.Calendar) (calendar.clone());
    }

    /**
     * Initialize a this calendar instance.
     */
    private void initializeCalendar() {
        if (DEBUG) {
            log("initializeCalendar()");
        }

        Locale locale = getLocale();
        TimeZone tz = getTimeZone();
        if (tz == null) {
            calendar = java.util.Calendar.getInstance(locale);
        } else {
            calendar = java.util.Calendar.getInstance(tz, locale);
        }
        if (DEBUG) {
            log("Initial date is " + calendar.getTime().toString());
        }
        if (DEBUG) {
            log("initializeCalendar() - END");
        }
    }

    /**
     * Return the DateFormat object for this CalendarMonth.
     * @return DateFormat
     */
    public DateFormat getDateFormat() {
        if (DEBUG) {
            log("getDateFormat()");
        }

        Object o = getAttributes().get(DATE_FORMAT_ATTR);
        DateFormat dateFormat;
        if (o != null && o instanceof DateFormat) {
            if (DEBUG) {
                log("DateFormat was already set");
            }
            dateFormat = (DateFormat) o;
            return dateFormat;
        }

        // Derive it
        if (DEBUG) {
            log("Dateformat not calculated");
        }
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat)
                SimpleDateFormat.getDateInstance(DateFormat.SHORT, getLocale());

        // We need to set the locale and the timeZone of the dateFormat.
        // I can't tell from the spec whether just setting the Calendar
        // does this correctly (the Calendar does know both).
        simpleDateFormat.setCalendar(getCalendar());
        ((SimpleDateFormat) simpleDateFormat).applyPattern(
                getDateFormatPattern(simpleDateFormat));

        // For creator don't store the value, always derive it.
        // It's not clear if storing the value prevents responding
        // to a dymnamic locale change.
        if (!Beans.isDesignTime()) {
            getAttributes().put(DATE_FORMAT_ATTR, simpleDateFormat);
        }
        return simpleDateFormat;
    }

    /**
     * Return the TimeZone object for this CalendarMonth.
     * If the parent is a DateManager, return its TimeZone
     * property, else return the time zone from a {@code java.util.Calendar}
     * based on the locale.
     * @return TimeZone
     */
    @Property(name = "timeZone", category = "Advanced")
    public TimeZone getTimeZone() {

        if (DEBUG) {
            log("getTimeZone()");
        }
        Object tzo = getAttributes().get(TIME_ZONE_ATTR);
        if (tzo != null && tzo instanceof TimeZone) {
            return (TimeZone) tzo;
        }

        UIComponent parent = getParent();
        TimeZone tz = null;
        try {
            tz = ((DateManager) parent).getTimeZone();
            if (DEBUG) {
                log("Parent is date manager");
            }
        } catch (Exception e) {
            if (DEBUG) {
                log("Parent is not date manager or is null");
            }
        }
        if (tz == null) {
            tz = java.util.Calendar.getInstance(getLocale()).getTimeZone();
        }

        // For creator don't store this.
        if (!Beans.isDesignTime()) {
            getAttributes().put(TIME_ZONE_ATTR, tz);
        }
        if (tz == null) {
            return TimeZone.getDefault();
        }
        return tz;
    }

    /**
     * Clear the cached timezone.
     * @param tz new time zone
     */
    public void setTimeZone(final TimeZone tz) {
        getAttributes().remove(TIME_ZONE_ATTR);
        // Make sure the calendar gets re-initialized.
        this.calendar = null;
    }

    /**
     * Get the DropDown menu instance to use for this CalendarMonths's year
     * menu.
     *
     * @return The DropDown instance to use for the year menu
     */
    public DropDown getMonthMenu() {
        if (DEBUG) {
            log("getMonthMenu()");
        }

        UIComponent comp = getFacet(MONTH_MENU_ID);
        DropDown monthMenu;
        if (comp == null || !(comp instanceof DropDown)) {
            monthMenu = new DropDown();
            monthMenu.setSubmitForm(true);
            monthMenu.setConverter(new IntegerConverter());
            monthMenu.setId(MONTH_MENU_ID);

            // The year menu is controlled by JavaScript when
            // this component is shown in popup mode. When used
            // in the Scheduler, we need to do the following
            // to control the behaviour on submit
            if (!isPopup()) {
                monthMenu.setImmediate(true);
                //yearMenu.addValueChangeListener(new MonthListener());
            }

            // add the year menu to the facet list
            getFacets().put(MONTH_MENU_ID, monthMenu);
        } else {
            monthMenu = (DropDown) comp;
        }
        if (DEBUG) {
            log("getMonthMenu() - END");
        }
        return monthMenu;
    }

    /**
     * Get the JumpDropDown menu instance to use for the
     * CalendarMonth's year menu.
     *
     * @return The JumpDropDown instance to use for the year menu
     */
    public DropDown getYearMenu() {
        if (DEBUG) {
            log("getYearMenu()");
        }

        DropDown yearMenu = (DropDown) getFacets().get(YEAR_MENU_ID);
        if (yearMenu == null) {
            yearMenu = new DropDown();
            yearMenu.setSubmitForm(true);
            yearMenu.setId(YEAR_MENU_ID);
            yearMenu.setConverter(new IntegerConverter());

            // The year menu is controlled by JavaScript when
            // this component is shown in popup mode. When used
            // in the Scheduler, we need to do the following
            // to control the behaviour on submit
            if (!isPopup()) {
                yearMenu.setImmediate(true);
                //yearMenu.addValueChangeListener(new YearListener());
            }

            // add the year menu to the facet list
            getFacets().put(YEAR_MENU_ID, yearMenu);
        }

        return yearMenu;
    }

    /**
     * Get the IconHyperlink instance to use for the previous year
     * link.
     * @return The IconHyperlink instance to use for the previous year link
     */
    public IconHyperlink getPreviousMonthLink() {
        IconHyperlink link = (IconHyperlink) getFacets()
                .get(PREVIOUS_MONTH_LINK_ID);

        if (link == null) {
            link = new IconHyperlink();
            link.setId(PREVIOUS_MONTH_LINK_ID);
            link.setIcon(ThemeImages.SCHEDULER_BACKWARD);
            link.setBorder(0);

            // The link is controlled by JavaScript when
            // this component is shown in popup mode. When used
            // in the Scheduler, we need to do the following
            // to control the behaviour on submit
            if (!isPopup()) {
                link.setImmediate(true);
                link.addActionListener(new PreviousMonthListener());
            }
            getFacets().put(PREVIOUS_MONTH_LINK_ID, link);
        }
        return (IconHyperlink) link;
    }

    /**
     * Get the IconHyperlink instance to use for the next year
     * link.
     *
     * @return The IconHyperlink instance to use for the next year link
     */
    public IconHyperlink getNextMonthLink() {
        IconHyperlink link = (IconHyperlink) getFacets()
                .get(NEXT_MONTH_LINK_ID);
        if (link == null) {
            link = new IconHyperlink();
            link.setId(NEXT_MONTH_LINK_ID);
            link.setIcon(ThemeImages.SCHEDULER_FORWARD);
            link.setBorder(0);
            // The link is controlled by JavaScript when
            // this component is shown in popup mode. When used
            // in the Scheduler, we need to do the following
            // to control the behaviour on submit
            if (!isPopup()) {
                link.addActionListener(new NextMonthListener());
                link.setImmediate(true);
            }

            getFacets().put(NEXT_MONTH_LINK_ID, link);
        }
        return link;
    }

    /**
     * Short-hand to get the current Theme.
     * @return Theme
     */
    protected Theme getTheme() {
        return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }

    /**
     * Initialize the calendar controls.
     * @param jsName JS module name
     */
    public void initCalendarControls(final String jsName) {
        if (DEBUG) {
            log("initCalendarControls()");
        }
        StringBuffer js = new StringBuffer()
                .append(jsName).append(".decreaseMonth(); return false;");

        // Don't set Javascript as the URL -- bugtraq #6306848.
        ImageHyperlink link = getPreviousMonthLink();
        link.setIcon(ThemeImages.CALENDAR_BACKWARD);
        link.setOnClick(js.toString());

        js = new StringBuffer()
                .append(jsName).append(".increaseMonth(); return false;");

        // Don't set Javascript as the URL -- bugtraq #6306848.
        link = getNextMonthLink();
        link.setIcon(ThemeImages.CALENDAR_FORWARD);
        link.setOnClick(js.toString());

        getMonthMenu().setOnChange(jsName
                .concat(".redrawCalendar(false); return false;"));
        getYearMenu().setOnChange(jsName
                .concat(".redrawCalendar(false); return false;"));
    }

    /**
     * Show the next month.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public void showNextMonth() {
        if (DEBUG) {
            log("showNextMonth");
        }
        Integer month = getCurrentMonth();
        if (DEBUG) {
            log("Current month is " + month.toString());
        }
        DropDown monthMenu = getMonthMenu();
        if (month < 12) {
            if (DEBUG) {
                log("Month is not December");
            }
            int newMonth = month + 1;
            monthMenu.setSubmittedValue(new String[]{
                String.valueOf(newMonth)
            });
        } else if (showNextYear()) {
            if (DEBUG) {
                log("Month is December");
            }
            monthMenu.setSubmittedValue(new String[]{"1"});
        }
    }

    /**
     * Show the previous month.
     */
    public void showPreviousMonth() {
        Integer month = getCurrentMonth();
        DropDown monthMenu = getMonthMenu();
        if (month > 1) {
            int newMonth = month - 1;
            monthMenu.setSubmittedValue(new String[]{
                String.valueOf(newMonth)
            });
        } else if (showPreviousYear()) {
            monthMenu.setSubmittedValue(new String[]{
                "12"
            });
        }
    }

    /**
     * Show the next year.
     * @return {@code true} if next year is shown, {@code false} otherwise
     */
    private boolean showNextYear() {
        if (DEBUG) {
            log("showNextYear");
        }
        DropDown yearMenu = getYearMenu();
        int year = getCurrentYear();
        year++;
        Option[] options = yearMenu.getOptions();
        Integer lastYear = (Integer) (options[options.length - 1].getValue());
        if (lastYear >= year) {
            yearMenu.setSubmittedValue(new String[]{
                String.valueOf(year)
            });
            return true;
        }
        return false;
    }

    /**
     * Show the previous year.
     *
     * @return {@code true} if showing the previous year, {@code false}
     * otherwise
     */
    private boolean showPreviousYear() {
        DropDown yearMenu = getYearMenu();
        int year = getCurrentYear();
        year--;
        Option[] options = yearMenu.getOptions();
        Integer firstYear = (Integer) (options[0].getValue());
        if (firstYear <= year) {
            yearMenu.setSubmittedValue(new String[]{String.valueOf(year)});
            return true;
        }
        return false;
    }

    /**
     * Get the current month.
     * @return Integer
     */
    public Integer getCurrentMonth() {
        DropDown monthMenu = getMonthMenu();
        Object value = monthMenu.getSubmittedValue();
        Integer month = null;
        if (value != null) {
            try {
                String[] vals = (String[]) value;
                month = Integer.decode(vals[0]);
            } catch (NumberFormatException ex) {
                // do nothing
            }
        } else {
            value = monthMenu.getValue();
            if (value != null && value instanceof Integer) {
                month = ((Integer) value);
            }
        }
        return month;
    }

    /**
     * Get the current year.
     * @return Integer
     */
    public Integer getCurrentYear() {
        DropDown yearMenu = getYearMenu();
        Object value = yearMenu.getSubmittedValue();
        Integer year = null;
        if (value != null) {
            try {
                String[] vals = (String[]) value;
                year = Integer.decode(vals[0]);
            } catch (NumberFormatException ex) {
                // do nothing
            }
        } else {
            value = yearMenu.getValue();
            if (value != null && value instanceof Integer) {
                year = ((Integer) value);
            }
        }
        return year;
    }

    /**
     * Getter for property javaScriptObject.
     * @return Value of property javaScriptObject.
     */
    public String getJavaScriptObjectName() {
        return this.javaScriptObjectName;
    }

    /**
     * Setter for property javaScriptObject.
     * @param jsObjectName New value of property javaScriptObject.
     */
    public void setJavaScriptObjectName(final String jsObjectName) {
        this.javaScriptObjectName = jsObjectName;
    }

    /**
     * Return the locale dependent date format pattern. If the parent
     * DateManager has a non null dateFormatPattern attribute, return that
     * value. If not, derive the date format pattern from a SimpleDateFormat
     * instance using the "SHORT" pattern. The returned pattern will have the
     * form of {@code "MM/dd/yyyy"} where the order of the tokens and the
     * separators will be locale dependent. Note that the characters in the
     * pattern are not localized.
     * @return String
     */
    @Property(name = "dateFormatPattern", category = "Appearance")
    public String getDateFormatPattern() {
        return getDateFormatPattern(null);
    }

    /**
     * Return the locale dependent date format pattern. If dateFormat is not
     * null, use that DateFormat to obtain the date format pattern. If the
     * parent DateManager has a non null dateFormatPattern attribute, return
     * that value. If not, derive the date format pattern from the
     * SimpleDateFormat parameter. The returned pattern will have the form of
     * {@code "MM/dd/yyyy"} where the order of the tokens and the separators
     * will be locale dependent. Note that the characters in the pattern are not
     * localized.
     *
     * @param dateFormat date format
     * @return String
     */
    public String getDateFormatPattern(final SimpleDateFormat dateFormat) {

        if (DEBUG) {
            log("getDateFormatPattern()");
        }
        SimpleDateFormat format = dateFormat;
        // If dateFormat is null, always derive the dateFormatPattern
        if (format == null) {
            // It's not clear if storing the derived date format pattern
            // will prevent the date format pattern changing dynamically
            // due to locale changes.
            //
            // For creator, don't store the derived date format pattern,
            // i.e. "Beans.isDesignTime == true"
            Object dfp = getAttributes().get(DATE_FORMAT_PATTERN_ATTR);
            String pattern = null;
            if (dfp != null && dfp instanceof String) {
                return (String) dfp;
            }
            format = (SimpleDateFormat) SimpleDateFormat.getDateInstance(
                    DateFormat.SHORT, getLocale());
        }

        // Derive the date format pattern.
        String pattern = null;
        UIComponent parent = getParent();
        if (parent != null && parent instanceof DateManager) {
            pattern = ((DateManager) parent).getDateFormatPattern();
        }
        if (pattern == null) {
            pattern = format.toPattern();
            if (DEBUG) {
                log("Default pattern " + pattern);
            }

            if (!pattern.contains("yyyy")) {
                pattern = pattern.replaceFirst("yy", "yyyy");
            }
            if (!pattern.contains("MM")) {
                pattern = pattern.replaceFirst("M", "MM");
            }
            if (!pattern.contains("dd")) {
                pattern = pattern.replaceFirst("d", "dd");
            }
        }

        format.applyPattern(pattern);
        pattern = format.toPattern();
        if (!Beans.isDesignTime()) {
            getAttributes().put(DATE_FORMAT_PATTERN_ATTR, pattern);
        }
        return pattern;
    }

    /**
     * Clear the cached dateFormatPattern.
     * @param dateFormatPattern date format pattern
     */
    public void setDateFormatPattern(final String dateFormatPattern) {
        // Clear the cached dateFormatPattern
        // and the DATE_FORMAT_ATTR
        getAttributes().remove(DATE_FORMAT_PATTERN_ATTR);
        getAttributes().remove(DATE_FORMAT_ATTR);
    }

    /**
     * Cause the month display to move to the current value, not what the use
     * was looking at last time.
     */
    public void displayValue() {
        if (DEBUG) {
            log("displayValue()");
        }
        DropDown monthMenu = getMonthMenu();
        DropDown yearMenu = getYearMenu();
        Object value = getValue();
        if (value == null) {
            if (DEBUG) {
                log("Value is null");
            }
            monthMenu.setValue(null);
            yearMenu.setValue(null);
        } else if (value instanceof Date) {
            if (DEBUG) {
                log("Value is date");
            }
            Calendar cal = getCalendar();
            cal.setTime((Date) value);
            int newMonth = cal.get(Calendar.MONTH) + 1;
            if (DEBUG) {
                log("new month value " + String.valueOf(newMonth));
            }
            monthMenu.setValue(newMonth);

            int newYear = cal.get(Calendar.YEAR);
            if (DEBUG) {
                log("new year value " + String.valueOf(newYear));
            }
            yearMenu.setValue(newYear);
        } else if (value instanceof ScheduledEvent) {
            if (DEBUG) {
                log("Value is ScheduledEvent");
            }
            Date date = ((ScheduledEvent) value).getStartTime();
            if (date != null) {
                Calendar cal = getCalendar();
                cal.setTime(date);
                int newMonth = cal.get(Calendar.MONTH) + 1;
                if (DEBUG) {
                    log("new month value " + String.valueOf(newMonth));
                }
                monthMenu.setValue(newMonth);
                int newYear = cal.get(Calendar.YEAR);
                if (DEBUG) {
                    log("new year value " + String.valueOf(newYear));
                }
                yearMenu.setValue(newYear);
            } else {
                if (DEBUG) {
                    log("Value is null");
                }
                monthMenu.setValue(null);
                yearMenu.setValue(null);
            }
        }
        monthMenu.setSubmittedValue(null);
        yearMenu.setSubmittedValue(null);
    }

    /**
     * Flag determining whether the component should be rendered in its
     * popup version (as used by Calendar), or in the
     * inline version used by Scheduler.
     * @return {@code boolean}
     */
    public boolean isPopup() {
        if (this.popupSet) {
            return this.popup;
        }
        ValueExpression vb = getValueExpression("popup");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return false;
    }

    /**
     * Flag determining whether the component should be rendered in its
     * popup version (as used by Calendar), or in the
     * inline version used by Scheduler.
     * @see #isPopup()
     * @param newPop popup
     */
    public void setPopup(final boolean newPop) {
        this.popup = newPop;
        this.popupSet = true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.popup = ((Boolean) values[1]);
        this.popupSet = ((Boolean) values[2]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[3];
        values[0] = super.saveState(context);
        if (this.popup) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.popupSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        return values;
    }

    /**
     * Convenience function to return the locale of the current context.
     *
     * @return Locale
     */
    private static Locale getLocale() {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getViewRoot().getLocale();
    }

    /**
     * Log a message to the standard out.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(CalendarMonth.class.getName() + "::" + msg);
    }

    /**
     * Previous month listener.
     */
    private static final class PreviousMonthListener
            implements ActionListener, Serializable {

        /**
         * Serialization UID.
         */
        private static final long serialVersionUID = 214378156127977786L;

        @Override
        public void processAction(final ActionEvent event) {
            FacesContext.getCurrentInstance().renderResponse();
            UIComponent comp = event.getComponent();
            comp = comp.getParent();
            if (comp instanceof CalendarMonth) {
                ((CalendarMonth) comp).showPreviousMonth();
            }
        }
    }

    /**
     * Next month listener.
     */
    private static final  class NextMonthListener
            implements ActionListener, Serializable {

        /**
         * Serialization UID.
         */
        private static final long serialVersionUID = -6726256680698229686L;

        @Override
        public void processAction(final ActionEvent event) {
            FacesContext.getCurrentInstance().renderResponse();
            UIComponent comp = event.getComponent();
            comp = comp.getParent();
            if (comp instanceof CalendarMonth) {
                ((CalendarMonth) comp).showNextMonth();
            }
        }
    }
}
