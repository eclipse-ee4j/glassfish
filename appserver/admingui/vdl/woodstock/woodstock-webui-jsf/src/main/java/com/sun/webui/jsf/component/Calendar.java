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
package com.sun.webui.jsf.component;

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.converter.DateConverter;
import com.sun.webui.jsf.util.JavaScriptUtilities;
import com.sun.webui.jsf.validator.DateInRangeValidator;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.validator.Validator;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * The Calendar component is used to allow a user to select a date.
 */
@Component(type = "com.sun.webui.jsf.Calendar",
        family = "com.sun.webui.jsf.Calendar",
        displayName = "Calendar",
        tagName = "calendar",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_calendar",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_calendar_props")
        //CHECKSTYLE:ON
public final class Calendar extends Field
        implements DateManager, NamingContainer {

    /**
     * Date picker link.
     */
    private static final String DATE_PICKER_LINK_FACET = "datePickerLink";

    /**
     * Date picker link id.
     */
    private static final String DATE_PICKER_LINK_ID = "_datePickerLink";

    /**
     * Date picker facet.
     */
    private static final String DATE_PICKER_FACET = "datePicker";

    /**
     * Date picker id.
     */
    private static final String DATE_PICKER_ID = "_datePicker";

    /**
     * Pattern id.
     */
    public static final String PATTERN_ID = "_pattern";

    /**
     * Date converter.
     */
    private DateConverter dateConverter;

    /**
     * The date format pattern to use (i.e. {@code yyyy-MM-dd}). The component
     * uses an instance of {@code java.text.SimpleDateFormat} and you may
     * specify a pattern to be used by this component, with the following
     * restriction: the format pattern must include {@code yyyy} (not
     * {@code yy}), {@code MM}, and {@code dd}; and no other parts of time may
     * be displayed. If a pattern is not specified, a locale-specific default is
     * used.
     * <p>
     * If you change the date format pattern, you may also need to change the
     * {@code dateFormatPatternHelp} attribute. See the documentation for that
     * attribute.
     * </p>
     */
    @Property(name = "dateFormatPattern",
            displayName = "Date Format Pattern",
            category = "Appearance",
            //CHECKSTYLE:OFF
            shortDescription = "The date format pattern to use (e.g., yyyy-MM-dd).",
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.DateFormatPatternsEditor")
            //CHECKSTYLE:ON
    private String dateFormatPattern;

    /**
     * A message below the text field for the date, indicating the string format
     * to use when entering a date as text into the text field.
     *
     * <p>
     * The component internally relies on an instance of
     * {@code java.text.SimpleDateFormat} to produce the hint. The default
     * hint is constructed by invoking the {@code toLocalizedPattern()}
     * method on the {@code SimpleDateFormat} instance and converting this
     * String to lower case.</p>
     *
     * <p>
     * Due to a bug in {@code SimpleDateFormat},
     * {@code toLocalizedPattern()} does not actually produce
     * locale-appropriate strings for most locales (it works for German, but not
     * for other locales). If the default value for the
     * {@code dateFormtPattern} is used, the component takes care of the
     * localization itself, but if the default is overridden, you may need to
     * override the hint on a per-locale basis too. </p>
     */
    @Property(name = "dateFormatPatternHelp",
            displayName = "Date Format Pattern Help",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String dateFormatPatternHelp;

    /**
     * A {@code java.util.Date} object representing the last selectable day. The
     * default value is 200 years after the {@code minDate} (which is evaluated
     * first).
     * <p>
     * The value of this attribute is reflected in the years that are available
     * for selection in the month display. In future releases of this component,
     * web application users will also not be able to view months after this
     * date, or select days that follow this date. At present such dates can be
     * selected, but will not be validated when the form is submitted.</p>
     */
    @Property(name = "maxDate",
            displayName = "Last selectable date",
            category = "Data",
            shortDescription = "The last selectable date.",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor")
            //CHECKSTYLE:ON
    private java.util.Date maxDate;

    /**
     * A {@code java.util.Date} object representing the first selectable day.
     * The default value is 100 years prior to today's date.
     * <p>
     * The value of this attribute is reflected in the years that are available
     * for selection in the month display. In future releases of this component,
     * web application users will also not be able to view months before this
     * date, or select days that precede this date. At present such dates can be
     * selected, but will not be validated when the form is submitted.</p>
     */
    @Property(name = "minDate",
            displayName = "First selectable date",
            category = "Data",
            shortDescription = "The first selectable date.",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor")
            //CHECKSTYLE:ON
    private java.util.Date minDate;

    /**
     * The {@code java.util.TimeZone} used with this component. Unless set, the
     * default TimeZone for the locale in
     * {@code jakarta.faces.component.UIViewRoot} is used.
     */
    @Property(name = "timeZone",
            displayName = "Time Zone",
            category = "Appearance",
            isHidden = true)
    private java.util.TimeZone timeZone;

    /**
     * Creates a new instance of Calendar.
     */
    public Calendar() {
        super();
        setRendererType("com.sun.webui.jsf.Calendar");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Calendar";
    }

    /**
     * This method returns the ImageHyperlink that serves as the "button" to
     * show or hide the calendar date picker display.
     *
     * @param context The current FacesContext.
     * @return The ImageHyperlink to show or hide the calendar date picker.
     */
    public ImageHyperlink getDatePickerLink(final FacesContext context) {

        UIComponent component = getFacet(DATE_PICKER_LINK_FACET);

        ImageHyperlink datePickerLink;
        if (component instanceof ImageHyperlink) {
            datePickerLink = (ImageHyperlink) component;
        } else {
            datePickerLink = new ImageHyperlink();
            getFacets().put(DATE_PICKER_LINK_FACET, datePickerLink);
        }

        datePickerLink.setId(DATE_PICKER_LINK_ID);
        datePickerLink.setAlign("middle");

        // render the image hyperlink to show/hide the calendar
        StringBuilder js = new StringBuilder();
        js.append("javascript: ").append(getJavaScriptObjectName(context))
                .append(".toggle(); return false;");

        // Don't set Javascript as the URL -- bugtraq #6306848.
        datePickerLink.setOnClick(js.toString());

        // We should do this, but unfortunately the component can't be enabled
        // from the client-side yet.
        //component.getAttributes().put("disabled", new Boolean(isDisabled()));
        return datePickerLink;
    }

    /**
     * Get the date picker.
     * @return CalendarMonth
     */
    public CalendarMonth getDatePicker() {
        UIComponent comp = getFacet(DATE_PICKER_FACET);
        if (comp == null || !(comp instanceof CalendarMonth)) {
            CalendarMonth datePicker = new CalendarMonth();
            datePicker.setPopup(true);
            datePicker.setId(DATE_PICKER_ID);
            getFacets().put(DATE_PICKER_FACET, datePicker);
            comp = datePicker;
        }
        ((CalendarMonth) comp).setJavaScriptObjectName(
                getJavaScriptObjectName(FacesContext.getCurrentInstance()));
        return (CalendarMonth) comp;
    }

    /**
     * Get the JS object name.
     * @param context faces context
     * @return String
     */
    public String getJavaScriptObjectName(final FacesContext context) {
        return JavaScriptUtilities.getDomNode(getFacesContext(), this);
    }

    @Override
    public Converter getConverter() {
        // We add the validator at this point, if needed...
        Validator[] validators = getValidators();
        int len = validators.length;
        boolean found = false;
        for (int i = 0; i < len; ++i) {
            if (validators[i] instanceof DateInRangeValidator) {
                found = true;
                break;
            }
        }
        if (!found) {
            addValidator(new DateInRangeValidator());
        }
        Converter converter = super.getConverter();

        if (converter == null) {
            if (dateConverter == null) {
                dateConverter = new DateConverter();
            }
            converter = dateConverter;
        }
        return converter;
    }

    @Override
    public String getReadOnlyValueString(final FacesContext context) {
        if (getValue() == null) {
            return "-";
        } else {
            return super.getReadOnlyValueString(context);
        }
    }

    @Override
    public DateFormat getDateFormat() {
        return getDatePicker().getDateFormat();
    }

    // Since the value of the minDate attribute could change, we can't
    // cache this in an attribute.
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Date getFirstAvailableDate() {
        Date min = getMinDate();
        if (min == null) {
            java.util.Calendar calendar = getDatePicker().getCalendar();
            calendar.add(java.util.Calendar.YEAR, -100);
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calendar.set(java.util.Calendar.MINUTE, 0);
            calendar.set(java.util.Calendar.SECOND, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            min = calendar.getTime();
        }
        return min;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Date getLastAvailableDate() {
        Date max = getMaxDate();
        if (max == null) {
            Date min = getFirstAvailableDate();
            java.util.Calendar calendar = getDatePicker().getCalendar();
            calendar.setTime(min);
            calendar.add(java.util.Calendar.YEAR, 200);
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
            calendar.set(java.util.Calendar.MINUTE, 59);
            calendar.set(java.util.Calendar.SECOND, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            max = calendar.getTime();
        }
        return max;
    }

    @Override
    public String getPrimaryElementID(final FacesContext context) {
        return getLabeledElementId(context);
    }

    @Override
    public String getLabeledElementId(final FacesContext context) {
        // The id of the calendar component is assigned to the
        // the HTML table element that lays out an optional
        // specified label, an input text field and an icon button
        // to launch the calendar.
        //
        // The element id returned is the text field since this is the
        // element that the label, that typically calls this method,
        // is interested in so that it can render the required and
        // invalid icons appropriately.
        //
        // Input id is defined in the Field super class of CalendarBase.
        // The field component is responsible for the input text field
        // as well.
        //
        return this.getClientId(context).concat(INPUT_ID);
    }

    @Override
    public String getFocusElementId(final FacesContext context) {
        return getLabeledElementId(context);
    }

    /**
     * Update the datePicker with an explicitly set date format pattern.
     * @param newDateFormatPattern dateFormatPattern
     */
    public void setDateFormatPattern(final String newDateFormatPattern) {
        this.dateFormatPattern = newDateFormatPattern;
        CalendarMonth dp = getDatePicker();
        dp.setDateFormatPattern(newDateFormatPattern);
    }

    /**
     * Update the datePicker with an explicitly set time zone.
     * @param newTimeZone timeZone
     */
    public void setTimeZone(final TimeZone newTimeZone) {
        this.timeZone = newTimeZone;
        CalendarMonth dp = getDatePicker();
        dp.setTimeZone(newTimeZone);
    }

    /**
     * Flag indicating that an input value for this field is mandatory, and
     * failure to provide one will trigger a validation error.
     */
    @Property(name = "required")
    @Override
    public void setRequired(final boolean required) {
        super.setRequired(required);
    }

    // Hide maxLength
    @Property(name = "maxLength", isHidden = true, isAttribute = false)
    @Override
    public int getMaxLength() {
        return super.getMaxLength();
    }

    // Hide text
    @Property(name = "text", isHidden = true, isAttribute = false)
    @Override
    public Object getText() {
        return super.getText();
    }

    // Hide trim
    @Property(name = "trim", isHidden = true, isAttribute = false)
    @Override
    public boolean isTrim() {
        return super.isTrim();
    }

    @Override
    public ValueExpression getValueExpression(final String name) {
        if (name.equals("selectedDate")) {
            return super.getValueExpression("value");
        }
        return super.getValueExpression(name);
    }

    @Override
    public void setValueExpression(final String name,
            final ValueExpression binding) {

        if (name.equals("selectedDate")) {
            super.setValueExpression("value", binding);
            return;
        }
        super.setValueExpression(name, binding);
    }

    @Override
    public String getDateFormatPattern() {
        if (this.dateFormatPattern != null) {
            return this.dateFormatPattern;
        }
        ValueExpression vb = getValueExpression("dateFormatPattern");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * A message below the textfield for the date, indicating the string format
     * to use when entering a date as text into the textfield.
     *
     * <p>
     * The component internally relies on an instance of
     * {@code java.text.SimpleDateFormat} to produce the hint. The default
     * hint is constructed by invoking the {@code toLocalizedPattern()}
     * method on the {@code SimpleDateFormat} instance and converting this
     * String to lower case.</p>
     *
     * <p>
     * Due to a bug in {@code SimpleDateFormat},
     * {@code toLocalizedPattern()} does not actually produce
     * locale-appropriate strings for most locales (it works for German, but not
     * for other locales). If the default value for the
     * {@code dateFormtPattern} is used, the component takes care of the
     * localization itself, but if the default is overridden, you may need to
     * override the hint on a per-locale basis too. </p>
     * @return String
     */
    public String getDateFormatPatternHelp() {
        if (this.dateFormatPatternHelp != null) {
            return this.dateFormatPatternHelp;
        }
        ValueExpression vb = getValueExpression("dateFormatPatternHelp");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * A message below the textfield for the date, indicating the string format
     * to use when entering a date as text into the textfield.
     *
     * <p>
     * The component internally relies on an instance of
     * {@code java.text.SimpleDateFormat} to produce the hint. The default
     * hint is constructed by invoking the {@code toLocalizedPattern()}
     * method on the {@code SimpleDateFormat} instance and converting this
     * String to lower case.</p>
     *
     * <p>
     * Due to a bug in {@code SimpleDateFormat},
     * {@code toLocalizedPattern()} does not actually produce
     * locale-appropriate strings for most locales (it works for German, but not
     * for other locales). If the default value for the
     * {@code dateFormtPattern} is used, the component takes care of the
     * localization itself, but if the default is overridden, you may need to
     * override the hint on a per-locale basis too. </p>
     *
     * @see #getDateFormatPatternHelp()
     * @param newDateFormatPatternHelp dateFormatPatternHelp
     */
    public void setDateFormatPatternHelp(
            final String newDateFormatPatternHelp) {

        this.dateFormatPatternHelp = newDateFormatPatternHelp;
    }

    /**
     * A {@code java.util.Date} object representing the last selectable
     * day. The default value is 200 years after the {@code minDate} (which
     * is evaluated first).
     * <p>
     * The value of this attribute is reflected in the years that are available
     * for selection in the month display. In future releases of this component,
     * web application users will also not be able to view months after this
     * date, or select days that follow this date. At present such dates can be
     * selected, but will not be validated when the form is submitted.</p>
     * @return {@code java.util.Date}
     */
    public java.util.Date getMaxDate() {
        if (this.maxDate != null) {
            return this.maxDate;
        }
        ValueExpression vb = getValueExpression("maxDate");
        if (vb != null) {
            return (java.util.Date) vb.getValue(getFacesContext()
                    .getELContext());
        }
        return null;
    }

    /**
     * A {@code java.util.Date} object representing the last selectable
     * day. The default value is 200 years after the {@code minDate} (which
     * is evaluated first).
     * <p>
     * The value of this attribute is reflected in the years that are available
     * for selection in the month display. In future releases of this component,
     * web application users will also not be able to view months after this
     * date, or select days that follow this date. At present such dates can be
     * selected, but will not be validated when the form is submitted.</p>
     *
     * @see #getMaxDate()
     * @param newMaxDate maxDate
     */
    public void setMaxDate(final java.util.Date newMaxDate) {
        this.maxDate = newMaxDate;
    }

    /**
     * A {@code java.util.Date} object representing the first selectable
     * day. The default value is 100 years prior to today's date.
     * <p>
     * The value of this attribute is reflected in the years that are available
     * for selection in the month display. In future releases of this component,
     * web application users will also not be able to view months before this
     * date, or select days that precede this date. At present such dates can be
     * selected, but will not be validated when the form is submitted.</p>
     * @return {@code java.util.Date}
     */
    public java.util.Date getMinDate() {
        if (this.minDate != null) {
            return this.minDate;
        }
        ValueExpression vb = getValueExpression("minDate");
        if (vb != null) {
            return (java.util.Date) vb.getValue(getFacesContext()
                    .getELContext());
        }
        return null;
    }

    /**
     * A {@code java.util.Date} object representing the first selectable
     * day. The default value is 100 years prior to today's date.
     * <p>
     * The value of this attribute is reflected in the years that are available
     * for selection in the month display. In future releases of this component,
     * web application users will also not be able to view months before this
     * date, or select days that precede this date. At present such dates can be
     * selected, but will not be validated when the form is submitted.</p>
     *
     * @see #getMinDate()
     * @param newMinDate minDate
     */
    public void setMinDate(final java.util.Date newMinDate) {
        this.minDate = newMinDate;
    }

    /**
     * A {@code java.util.Date} object representing the currently selected
     * calendar date.
     * @return {@code java.util.Date}
     */
    @Property(name = "selectedDate",
            displayName = "Selected Date",
            category = "Data",
            shortDescription = "The date currently selected.",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor")
            //CHECKSTYLE:ON
    public java.util.Date getSelectedDate() {
        return (java.util.Date) getValue();
    }

    /**
     * A {@code java.util.Date} object representing the currently selected
     * calendar date.
     *
     * @param selectedDate selected date
     * @see #getSelectedDate()
     */
    public void setSelectedDate(final java.util.Date selectedDate) {
        setValue((Object) selectedDate);
    }

    @Override
    public java.util.TimeZone getTimeZone() {
        if (this.timeZone != null) {
            return this.timeZone;
        }
        ValueExpression vb = getValueExpression("timeZone");
        if (vb != null) {
            return (java.util.TimeZone) vb.getValue(getFacesContext()
                    .getELContext());
        }
        return null;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.dateFormatPattern = (String) values[1];
        this.dateFormatPatternHelp = (String) values[2];
        this.maxDate = (java.util.Date) values[3];
        this.minDate = (java.util.Date) values[4];
        this.timeZone = (java.util.TimeZone) values[5];
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @Override
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[6];
        values[0] = super.saveState(context);
        values[1] = this.dateFormatPattern;
        values[2] = this.dateFormatPatternHelp;
        values[3] = this.maxDate;
        values[4] = this.minDate;
        values[5] = this.timeZone;
        return values;
    }
}
