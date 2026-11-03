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
import com.sun.webui.jsf.converter.DateConverter;
import com.sun.webui.jsf.event.IntervalListener;
import com.sun.webui.jsf.event.SchedulerPreviewListener;
import com.sun.webui.jsf.model.ClockTime;
import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.model.ScheduledEvent;
import com.sun.webui.jsf.model.scheduler.RepeatInterval;
import com.sun.webui.jsf.model.scheduler.RepeatIntervalConverter;
import com.sun.webui.jsf.model.scheduler.RepeatIntervalOption;
import com.sun.webui.jsf.model.scheduler.RepeatUnit;
import com.sun.webui.jsf.model.scheduler.RepeatUnitConverter;
import com.sun.webui.jsf.model.scheduler.RepeatUnitOption;
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.JavaScriptUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.jsf.validator.DateInRangeValidator;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.IntegerConverter;
import jakarta.faces.validator.DoubleRangeValidator;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * The Scheduler component is used to display a calendar and the input controls
 * that are used for selecting a date and time.
 */
@Component(type = "com.sun.webui.jsf.Scheduler",
        family = "com.sun.webui.jsf.Scheduler",
        displayName = "Scheduler", tagName = "scheduler",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_scheduler",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_scheduler_props")
        //CHECKSTYLE:ON
public final class Scheduler extends WebuiInput
        implements ComplexComponent, DateManager, NamingContainer {

    /**
     * The date picker facet name.
     */
    public static final String DATE_PICKER_FACET = "datePicker";

    /**
     * The date facet name.
     */
    public static final String DATE_FACET = "date";

    /**
     * The date label facet name.
     */
    public static final String DATE_LABEL_FACET = "dateLabel";

    /**
     * The start date label text.
     */
    public static final String START_DATE_TEXT_KEY
            = "Scheduler.startDate";

    /**
     * The start time facet name.
     */
    public static final String START_TIME_FACET = "startTime";

    /**
     * The start time facet name.
     */
    public static final String START_TIME_LABEL_FACET
            = "startTimeLabel";

    /**
     * The start time label text.
     */
    public static final String START_TIME_TEXT_KEY
            = "Scheduler.startTime";

    /**
     * The end time facet name.
     */
    public static final String END_TIME_FACET = "endTime";

    /**
     * The end time facet name.
     */
    public static final String END_TIME_LABEL_FACET = "endTimeLabel";

    /**
     * The end time label text.
     */
    public static final String END_TIME_TEXT_KEY = "Scheduler.endTime";

    /**
     * The repeat unit facet name.
     */
    public static final String REPEAT_LIMIT_UNIT_FACET

            = "repeatLimitUnit";
    /**
     * The repeat unit descriptions text key.
     */
    public static final String REPEAT_UNIT_DESCRIPTION_TEXT_KEY

            = "Scheduler.repeatUnitDesc";
    /**
     * The repeat limit facet name.
     */
    public static final String REPEAT_LIMIT_FACET = "repeatLimit";

    /**
     * The repeat limit label facet name.
     */
    public static final String REPEAT_LIMIT_LABEL_FACET
            = "repeatLimitLabel";

    /**
     * The repeat limit label text.
     */
    public static final String REPEAT_LIMIT_TEXT_KEY
            = "Scheduler.repeatLimit";

    /**
     * The repeat interval facet name.
     */
    public static final String REPEAT_INTERVAL_FACET
            = "repeatInterval";

    /**
     * The repeat interval label facet name.
     */
    public static final String REPEAT_INTERVAL_LABEL_FACET
            = "repeatIntervalLabel";

    /**
     * The repeat interval label text.
     */
    public static final String REPEAT_INTERVAL_TEXT_KEY
            = "Scheduler.repeatInterval";

    /**
     * The repeat interval descriptions text key.
     */
    public static final String REPEAT_INTERVAL_DESCRIPTION_TEXT_KEY
            = "Scheduler.repeatIntervalDesc";

    /**
     * The preview button facet name.
     */
    public static final String PREVIEW_BUTTON_FACET = "previewButton";

    /**
     * The preview button text key.
     */
    public static final String PREVIEW_BUTTON_TEXT_KEY
            = "Scheduler.preview";

    /**
     * The start hour title text key.
     */
    public static final String START_HOUR_TITLE_TEXT_KEY
            = "Scheduler.startHourTitle";

    /**
     * The start minute title text key.
     */
    public static final String START_MINUTE_TITLE_TEXT_KEY
            = "Scheduler.startMinuteTitle";

    /**
     * The end hour title text key.
     */
    public static final String END_HOUR_TITLE_TEXT_KEY
            = "Scheduler.endHourTitle";

    /**
     * The end minute title text key.
     */
    public static final String END_MINUTE_TITLE_TEXT_KEY
            = "Scheduler.endMinuteTitle";

    /**
     * The end before start error text key.
     */
    private static final String END_BEFORE_START_TEXT_KEY
            = "Scheduler.endBeforeStart";

    /**
     * The invalid input text key.
     */
    private static final String INVALID_INPUT_TEXT_KEY
            = "Scheduler.invalidInput";

    /**
     * The invalid date text key.
     */
    private static final String INVALID_DATE_TEXT_KEY
            = "Scheduler.invalidDate";

    /**
     * The invalid start time text key.
     */
    private static final String INVALID_START_TIME_TEXT_KEY
            = "Scheduler.invalidStartTime";

    /**
     * The invalid end time text key.
     */
    private static final String INVALID_END_TIME_TEXT_KEY
            = "Scheduler.invalidEndTime";

    /**
     * Icon id.
     */
    public static final String ICON_ID = "_icon";

    /**
     * Value submitted.
     */
    private static final String VALUE_SUBMITTED
            = "com.sun.webui.jsf.SchedulerSubmitted";

    /**
     * First available date.
     */
    private static final String FIRST_AVAILABLE_DATE
            = "com.sun.webui.jsf.FirstAvailable";

    /**
     * Last available date.
     */
    private static final String LAST_AVAILABLE_DATE
            = "com.sun.webui.jsf.LastAvailable";

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

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
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.DateFormatPatternsEditor",
            shortDescription = "The date format pattern to use (e.g., {@code yyyy-MM-dd}).")
            //CHECKSTYLE:ON
    private String dateFormatPattern = null;

    /**
     * A message below the text field for the date, indicating the string format
     * to use when entering a date as text into the Start Date text field.
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
    private String dateFormatPatternHelp = null;

        /**
     * This text replaces the "Start Date" label.
     */
    @Property(name = "dateLabel",
            displayName = "Date Label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String dateLabel = null;

    /**
     * Standard HTML attribute which determines whether the web application user
     * can change the the value of this component. NOT YET IMPLEMENTED.
     */
    @Property(name = "disabled",
            displayName = "Disabled",
            isHidden = true,
            isAttribute = false)
    private boolean disabled = false;

    /**
     * disabled set flag.
     */
    private boolean disabledSet = false;

    /**
     * Flag indicating that an input field for the end time should be shown. The
     * default value is true.
     */
    @Property(name = "endTime",
            displayName = "Show End Time",
            category = "Appearance")
    private boolean endTime = false;

    /**
     * endTime set flag.
     */
    private boolean endTimeSet = false;

    /**
     * This text replaces the "End Time" label.
     */
    @Property(name = "endTimeLabel",
            displayName = "End Time Label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String endTimeLabel = null;

    /**
     * Flag indicating that a control for setting a limit for repeating events
     * should be shown. The default value is true.
     */
    @Property(name = "limitRepeating",
            displayName = "Limit Repeating Events",
            category = "Appearance")
    private boolean limitRepeating = false;

    /**
     * limit repeating set flag.
     */
    private boolean limitRepeatingSet = false;

    /**
     * A {@code java.util.Date} object representing the last selectable day. The
     * default value is four years after the {@code minDate} (which is evaluated
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
    private java.util.Date maxDate = null;

    /**
     * A {@code java.util.Date} object representing the first selectable day.
     * The default value is today's date.
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
    private java.util.Date minDate = null;

    /**
     * Flag indicating that the "Preview in Browser" button should be displayed
     * - default value is true.
     */
    @Property(name = "previewButton",
            displayName = "Preview Button",
            category = "Appearance")
    private boolean previewButton = false;

    /**
     * previewButton set flag.
     */
    private boolean previewButtonSet = false;

    /**
     * If this attribute is set to true, the value of the component is rendered
     * as text, preceded by the label if one was defined. NOT YET IMPLEMENTED.
     */
    @Property(name = "readOnly",
            displayName = "Read-only",
            category = "Behavior")
    private boolean readOnly = false;

    /**
     * readOnly set flag.
     */
    private boolean readOnlySet = false;

    /**
     * Override the items that appear on the repeat interval menu. The value
     * must be one of an array, Map or Collection whose members are all
     * subclasses of
     * {@code com.sun.webui.jsf.model.scheduler.RepeatIntervalOption}, whose
     * values must be one of the member classes of
     * {@code com.sun.webui.jsf.model.scheduler.RepeatInterval}, for example
     * {@code com.sun.webui.jsf.model.scheduler.RepeatInterval.ONETIME} or
     * {@code com.sun.webui.jsf.model.scheduler.RepeatInterval.HOURLY}. If this
     * attribute is not specified, default options of "One Time", "Hourly",
     * "Weekly", "Monthly" will be shown.
     */
    @Property(name = "repeatIntervalItems",
            displayName = "Repeat Interval Menu Items",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.RepeatIntervalEditor")
            //CHECKSTYLE:ON
    private Object repeatIntervalItems = null;

    /**
     * Override the default value of the label for the repeat interval menu.
     */
    @Property(name = "repeatIntervalLabel",
            displayName = "Repeat Interval Label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String repeatIntervalLabel = null;

    /**
     * Override the default value of the label for the repeat limit menu.
     */
    @Property(name = "repeatLimitLabel",
            displayName = "Repeat Limit Label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String repeatLimitLabel = null;

    /**
     * Override the items that appear on the repeat limit unit menu. The value
     * must be one of an array, Map or Collection whose members are all
     * subclasses of {@code com.sun.webui.jsf.model.Option}, and the value of
     * the options must implement the {@code com.sun.webui.jsf.model.RepeatUnit}
     * interface. The default value is to show a menu with values "Hours",
     * "Days", "Weeks", "Months".
     */
    @Property(name = "repeatUnitItems",
            displayName = "Repeat Limit Unit Items",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.RepeatUnitEditor")
            //CHECKSTYLE:ON
    private Object repeatUnitItems = null;

    /**
     * Flag indicating that a control for scheduling a repeated event should be
     * shown. The default value is true.
     */
    @Property(name = "repeating",
            displayName = "Repeating Events",
            category = "Appearance")
    private boolean repeating = false;

    /**
     * repeating set flag.
     */
    private boolean repeatingSet = false;

    /**
     * Flag indicating that the user must enter a value for this Scheduler.
     * Default value is true.
     */
    @Property(name = "required",
            displayName = "Required",
            isHidden = true,
            isAttribute = false)
    private boolean required = false;

    /**
     * required set flag.
     */
    private boolean requiredSet = false;

    /**
     * Flag indicating if the "* indicates required field" message should be
     * displayed - default value is true.
     */
    @Property(name = "requiredLegend",
            displayName = "Required Legend",
            category = "Appearance")
    private boolean requiredLegend = false;

    /**
     * requireLegend set flag.
     */
    private boolean requiredLegendSet = false;

    /**
     * Flag indicating that an input field for the start time should be shown.
     * The default value is true.
     */
    @Property(name = "startTime",
            displayName = "Show Start Time",
            category = "Appearance")
    private boolean startTime = false;

    /**
     * startTime set flag.
     */
    private boolean startTimeSet = false;

    /**
     * This text replaces the "Start Time" label.
     */
    @Property(name = "startTimeLabel",
            displayName = "Start Time Label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String startTimeLabel = null;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
    private String style = null;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "styleClass",
            displayName = "CSS Style Class(es)",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StyleClassPropertyEditor")
            //CHECKSTYLE:ON
    private String styleClass = null;

    /**
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     */
    @Property(name = "tabIndex",
            displayName = "Tab Index",
            category = "Accessibility",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int tabIndex = Integer.MIN_VALUE;

    /**
     * tabIndex set flag.
     */
    private boolean tabIndexSet = false;

    /**
     * The {@code java.util.TimeZone} used with this component. Unless set, the
     * default TimeZone for the locale in
     * {@code jakarta.faces.component.UIViewRoot} is used.
     */
    @Property(name = "timeZone",
            displayName = "Time Zone",
            isHidden = true)
    private java.util.TimeZone timeZone = null;

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     */
    @Property(name = "visible",
            displayName = "Visible",
            category = "Behavior")
    private boolean visible = false;

    /**
     * visible set flag.
     */
    private boolean visibleSet = false;

    /**
     * Default constructor.
     */
    public Scheduler() {
        super();
        setRendererType("com.sun.webui.jsf.Scheduler");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Scheduler";
    }

    /**
     * The current value of this component.
     *
     * @param value value
     */
    @Property(name = "value")
    @Override
    public void setValue(final Object value) {
        super.setValue(value);
    }

    // Hide converter
    @Property(name = "converter", isHidden = true, isAttribute = false)
    @Override
    public Converter getConverter() {
        return super.getConverter();
    }

    /**
     * Return a {@code CalendarMonth} component that implements the
     * calendar for the Scheduler.
     * <p>
     * <em>This is a private facet.</em>
     * </p>
     *
     * @return a CalendarMonth component.
     */
    public CalendarMonth getDatePicker() {

        CalendarMonth dp = (CalendarMonth) ComponentUtilities
                .getPrivateFacet(this, DATE_PICKER_FACET, true);
        if (dp == null) {
            dp = new CalendarMonth();
            dp.setPopup(false);
            dp.setId(ComponentUtilities.createPrivateFacetId(this,
                    DATE_PICKER_FACET));
            ComponentUtilities.putPrivateFacet(this, DATE_PICKER_FACET, dp);
        }
        dp.setJavaScriptObjectName(getJavaScriptObjectName(
                FacesContext.getCurrentInstance()));

        return dp;
    }

    /**
     * Return a component that implements a label for the date component. If a
     * facet named {@code dateLabel} is found that component is returned.
     * Otherwise a {@code Label} component is returned. It is assigned the
     * id
     * {@code getId() + "_dateLabel"}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @param theme theme to use
     * @return a date label facet component
     */
    // Passing Theme is unlike most all other "getXXComponent" methods
    // defined in  other components with facets
    public UIComponent getDateLabelComponent(final Theme theme) {

        // This criteria is different than other label facets
        // Other label facets check for "" as well
        // as null and in those cases, uses the default.
        String label = getDateLabel();
        if (label == null) {
            label = theme.getMessage(START_DATE_TEXT_KEY);
        }

        // Really need to optimize calling "getXXXComponent" methods.
        // Many times these reinitialize the component. Depending
        // on the lifecycle phase, this may or may not be appropriate.
        UIComponent comp = getDateComponent();
        String compId;
        if (comp != null) {
            compId = comp.getClientId(getFacesContext());
        } else {
            compId = null;
        }
        return getLabelFacet(DATE_LABEL_FACET, label, compId);
    }

    /**
     * Return a component that implements a date input field. If a facet named
     * {@code date} is found that component is returned. Otherwise a
     * {@code TextField} component is returned. It is assigned the id
     * {@code getId() + "_date"}
     * <p>
     * If the facet is not defined then the returned {@code TextField}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return a date input field facet component
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public UIComponent getDateComponent() {
        if (DEBUG) {
            log("getDateComponent()");
        }
        // Check if the page author has defined the facet
        UIComponent fieldComponent = getFacet(DATE_FACET);
        if (fieldComponent != null) {
            if (DEBUG) {
                log("\tFound facet");
            }
            return fieldComponent;
        }

        // Return the private facet or create one, but initialize
        // it every time
        //
        // We know it's a TextField
        TextField field = (TextField) ComponentUtilities.getPrivateFacet(this,
                DATE_FACET, true);
        if (field == null) {
            if (DEBUG) {
                log("create Field");
            }
            field = new TextField();
            field.setId(ComponentUtilities.createPrivateFacetId(this,
                    DATE_FACET));
            ((EditableValueHolder) field).setConverter(new DateConverter());
            ((EditableValueHolder) field).addValidator(
                    new DateInRangeValidator());
            ComponentUtilities.putPrivateFacet(this, DATE_FACET, field);
        }
        // FIXME: 12 should be part of Theme.
        field.setDisabled(isDisabled());
        initFieldFacet(field, 12, isRequired());
        return field;
    }

    /**
     * Return a component that implements a label for the start time component.
     * If a facet named {@code startTimeLabel} is found that component is
     * returned. Otherwise a {@code Label} component is returned. It is
     * assigned the id
     * {@code getId() + "_startTimeLabel"}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @param theme theme to use
     * @return a start time label facet component
     */
    // Passing Theme is unlike most all other "getXXComponent" methods
    // defined in  other components with facets
    public UIComponent getStartTimeLabelComponent(final Theme theme) {

        // This criteria is different that other label facets
        // Other label facets have checked for "" as well
        // as null and in those cases, uses the default.
        String label = getStartTimeLabel();
        if (label == null) {
            label = theme.getMessage(START_TIME_TEXT_KEY);
        }
        // Really need to optimize calling "getXXXComponent" methods.
        // Many times these reinitialize the component. Depending
        // on the lifecycle phase, this may or may not be appropriate.
        UIComponent comp = getStartTimeComponent();
        String compId;
        if (comp != null) {
            compId = comp.getClientId(getFacesContext());
        } else {
            compId = null;
        }
        return getLabelFacet(START_TIME_LABEL_FACET, label, compId);
    }

    /**
     * Return a Time component that implements the start time. If
     * {@code ComponentUtilities.getPrivateFacet()} returns a facet named
     * {@code startTime} that component is initialized every time this
     * method is called and returned.
     * <p>
     * <em>This is a private facet.</em>
     * </p>
     * Otherwise a {@code Time} component is created and initialized. It is
     * assigned the id
     * {@code getId() + "_startTime"} and added to the facets map as a
     * private facet.
     *
     * @return a start time Time component.
     */
    public Time getStartTimeComponent() {
        // Note that tooltip keys are passed here.
        // This is unlike many other facets. The keys are
        // resolved in Time.endcodeEnd.
        return getTimeFacet(START_TIME_FACET, isRequired(),
                START_HOUR_TITLE_TEXT_KEY, START_MINUTE_TITLE_TEXT_KEY);
    }

    /**
     * Return a component that implements a label for end time component. If a
     * facet named {@code endTimeLabel} is found that component is
     * returned. Otherwise a {@code Label} component is returned. It is
     * assigned the id
     * {@code getId() + "_endTimeLabel"}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @param theme theme to use
     * @return a end time label facet component
     */
    // Passing Theme is unlike most all other "getXXComponent" methods
    // defined in  other components with facets
    //
    public UIComponent getEndTimeLabelComponent(final Theme theme) {

        // This criteria is different that other label facets
        // Other label facets have checked for "" as well
        // as null and in those cases, uses the default.
        String label = getEndTimeLabel();
        if (label == null) {
            label = theme.getMessage(END_TIME_TEXT_KEY);
        }
        // Really need to optimize calling "getXXXComponent" methods.
        // Many times these reinitialize the component. Depending
        // on the lifecycle phase, this may or may not be appropriate.
        UIComponent comp = getEndTimeComponent();
        String compId;
        if (comp != null) {
            compId = comp.getClientId(getFacesContext());
        } else {
            compId = null;
        }
        return getLabelFacet(END_TIME_LABEL_FACET, label, compId);

    }

    /**
     * Return a Time component that implements the end time. If
     * {@code ComponentUtilities.getPrivateFacet()} returns a facet named
     * {@code endTime} that component is initialized every time this method
     * is called and returned.
     * <p>
     * <em>This is a private facet.</em>
     * </p>
     * Otherwise a {@code Time} component is created and initialized. It is
     * assigned the id
     * {@code getId() + "_endTime"} and added to the facets map as a
     * private facet.
     *
     * @return an end time Time component.
     */
    public Time getEndTimeComponent() {
        // Note that tooltip keys are passed here.
        // This is unlike many other facets. The keys are
        // resolved in Time.endcodeEnd.
        return getTimeFacet(END_TIME_FACET, false,
                END_HOUR_TITLE_TEXT_KEY, END_MINUTE_TITLE_TEXT_KEY);
    }

    /**
     * Return a component that implements a label for repeat interval component.
     * If a facet named {@code repeatIntervalLabel} is found that component
     * is returned. Otherwise a {@code Label} component is returned. It is
     * assigned the id
     * {@code getId() + "_repeatIntervalLabel"}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return a repeat interval label facet component
     */
    public UIComponent getRepeatIntervalLabelComponent() {

        // This criteria is different that other components with
        // label facets. Other label facets check for "" as well
        // as null and then, uses the default.
        String label = getRepeatIntervalLabel();
        if (label == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            label = ThemeUtilities.getTheme(context).getMessage(
                    REPEAT_INTERVAL_TEXT_KEY);
        }
        // Really need to optimize calling "getXXXComponent" methods.
        // Many times these reinitialize the component. Depending
        // on the lifecycle phase, this may or may not be appropriate.
        UIComponent comp = getRepeatIntervalComponent();
        String compId;
        if (comp != null) {
            compId = comp.getClientId(getFacesContext());
        } else {
            compId = null;
        }
        return getLabelFacet(REPEAT_INTERVAL_LABEL_FACET, label, compId);
    }

    /**
     * Return a DropDown component that implements a repeat interval menu. If
     * {@code ComponentUtilities.getPrivateFacet()} returns a facet named
     * {@code repeatInterval} that component is initialized every time this
     * method is called and returned.
     * <p>
     * <em>This is a private facet.</em>
     * </p>
     * Otherwise a {@code DropDown} component is created and initialized.
     * It is assigned the id
     * {@code getId() + "_repeatInterval"} and added to the facets map as a
     * private facet.
     *
     * @return a repeat interval DropDown component.
     */
    public DropDown getRepeatIntervalComponent() {
        if (DEBUG) {
            log("getRepeatIntervalComponent");
        }
        // Support only a private facet.
        DropDown dropDown = (DropDown) ComponentUtilities
                .getPrivateFacet(this, REPEAT_INTERVAL_FACET,
                true);
        if (dropDown == null) {
            if (DEBUG) {
                log("createDropDown");
            }
            dropDown = new DropDown();
            dropDown.setId(ComponentUtilities.createPrivateFacetId(this,
                    REPEAT_INTERVAL_FACET));
            dropDown.setSubmitForm(true);
            dropDown.setConverter(new RepeatIntervalConverter());
            dropDown.setImmediate(true);
            dropDown.addActionListener(new IntervalListener());

            ComponentUtilities.putPrivateFacet(this, REPEAT_INTERVAL_FACET,
                    dropDown);
        }

        dropDown.setItems(getRepeatIntervalItems());
        Theme theme = ThemeUtilities
                .getTheme(FacesContext.getCurrentInstance());
        dropDown.setToolTip(theme
                .getMessage(REPEAT_INTERVAL_DESCRIPTION_TEXT_KEY));
        return dropDown;
    }

    /**
     * Return a component that implements a label for the repeat limit
     * component. If a facet named {@code repeatLimitLabel} is found that
     * component is returned. Otherwise a {@code Label} component is
     * returned. It is assigned the id
     * {@code getId() + "_repeatLimitLabel"}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return a repeat limit label facet component
     */
    public UIComponent getRepeatLimitLabelComponent() {

        // This criteria is different that other label facets
        // Other label facets have checked for "" as well
        // as null and in those cases, uses the default.
        String label = getRepeatLimitLabel();
        if (label == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            label = ThemeUtilities.getTheme(context).getMessage(
                    REPEAT_LIMIT_TEXT_KEY);
        }
        // Really need to optimize calling "getXXXComponent" methods.
        // Many times these reinitialize the component. Depending
        // on the lifecycle phase, this may or may not be appropriate.
        UIComponent comp = getRepeatingFieldComponent();
        String compId;
        if (comp != null) {
            compId = comp.getClientId(getFacesContext());
        } else {
            compId = null;
        }
        return getLabelFacet(REPEAT_LIMIT_LABEL_FACET, label, compId);
    }

    /**
     * Return a component that implements a repeating limit input field. If a
     * facet named {@code repeatLimit} is found that component is returned.
     * Otherwise a {@code TextField} component is returned. It is assigned
     * the id
     * {@code getId() + "_repeatLimit"}
     * <p>
     * If the facet is not defined then the returned {@code TextField}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return a repeat limit input field facet component
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public UIComponent getRepeatingFieldComponent() {

        if (DEBUG) {
            log("getRepeatingFieldComponent()");
        }
        // Check if the page author has defined the facet
        UIComponent fieldComponent = getFacet(REPEAT_LIMIT_FACET);
        if (fieldComponent != null) {
            if (DEBUG) {
                log("\tFound facet");
            }
            return fieldComponent;
        }

        // Return the private facet or create one, but initialize
        // it every time
        //
        // We know it's a TextField
        TextField field = (TextField) ComponentUtilities.getPrivateFacet(this,
                REPEAT_LIMIT_FACET, true);
        if (field == null) {
            if (DEBUG) {
                log("create Field");
            }
            field = new TextField();
            field.setId(ComponentUtilities.createPrivateFacetId(this,
                    REPEAT_LIMIT_FACET));
            IntegerConverter converter = new IntegerConverter();
            field.setConverter(converter);
            DoubleRangeValidator drv = new DoubleRangeValidator();
            drv.setMinimum(1);
            field.addValidator(drv);
            ComponentUtilities.putPrivateFacet(this, REPEAT_LIMIT_FACET, field);
        }

        // Ideally this facet should be disabled or enabled here
        // based on the state of the Scheduler. Currently it is
        // disabled/enabled in disableRepeatLimitComponents
        // and enableRepeatLimitComponents, which are called from
        // updateRepeatUnitMenu, which is called from the IntervalListener.
        // initializeValues also explicitly enables/disables this
        // facet. It is called during encodeEnd if the validation
        // phase is run, i.e. Scheduler.submittedValue == null.
        // FIXME: 3 should be part of Theme.
        initFieldFacet(field, 3, false);
        return field;
    }

    /**
     * Return a DropDown component that implements a repeat unit menu. If
     * {@code ComponentUtilities.getPrivateFacet()} returns a facet named
     * {@code repeatLimitUnit} that component is initialized every time
     * this method is called and returned.
     * <p>
     * <em>This is a private facet.</em>
     * </p>
     * Otherwise a {@code DropDown} component is created and initialized.
     * It is assigned the id
     * {@code getId() + "_repeatLimitUnit"} and added to the facets map as
     * a private facet.
     *
     * @return a repeat unit DropDown component.
     */
    public DropDown getRepeatUnitComponent() {

        if (DEBUG) {
            log("getRepeatUnitComponent()");
        }
        // Support only a private facet.
        DropDown dropDown = (DropDown) ComponentUtilities
                .getPrivateFacet(this, REPEAT_LIMIT_UNIT_FACET,
                true);
        if (dropDown == null) {
            if (DEBUG) {
                log("createDropDown");
            }
            dropDown = new DropDown();
            dropDown.setId(ComponentUtilities.createPrivateFacetId(this,
                    REPEAT_LIMIT_UNIT_FACET));
            dropDown.setConverter(new RepeatUnitConverter());
            ComponentUtilities.putPrivateFacet(this, REPEAT_LIMIT_UNIT_FACET,
                    dropDown);
        }

        // Ideally this facet should be disabled or enabled here
        // based on the state of the Scheduler. Currently it is
        // disabled/enabled in disableRepeatLimitComponents
        // and enableRepeatLimitComponents, which are called from
        // updateRepeatUnitMenu, which is called from the IntervalListener.
        // initializeValues also explicitly enables/disables this
        // facet. It is called during encodeEnd if the validation
        // phase is run, i.e. Scheduler.submittedValue == null.
        dropDown.setItems(getRepeatUnitItems());
        Theme theme = ThemeUtilities
                .getTheme(FacesContext.getCurrentInstance());
        dropDown.setToolTip(theme.getMessage(REPEAT_UNIT_DESCRIPTION_TEXT_KEY));

        return dropDown;
    }

    /**
     * Return a component that implements a preview button facet. If a facet
     * named {@code previewButton} is found that component is returned.
     * Otherwise a {@code Button} component is returned. It is assigned the
     * id
     * {@code getId() + "_previewButton"}
     * <p>
     * If the facet is not defined then the returned {@code Button}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return a preview button facet component
     */
    public UIComponent getPreviewButtonComponent() {

        if (DEBUG) {
            log("getPreviewButtonComponent()");
        }
        // Check if the page author has defined the facet
        UIComponent buttonComponent = getFacet(PREVIEW_BUTTON_FACET);
        if (buttonComponent != null) {
            if (DEBUG) {
                log("\tFound facet");
            }
            return buttonComponent;
        }

        // Return the private facet or create one, but initialize
        // it every time
        //
        // We know it's a Button
        Button button = (Button) ComponentUtilities.getPrivateFacet(this,
                PREVIEW_BUTTON_FACET, true);
        if (button == null) {
            if (DEBUG) {
                log("create Button");
            }
            button = new Button();
            button.setId(ComponentUtilities.createPrivateFacetId(this,
                    PREVIEW_BUTTON_FACET));
            button.setMini(true);
            button.setPrimary(false);
            button.setImmediate(true);
            button.addActionListener(new SchedulerPreviewListener());
            ComponentUtilities.putPrivateFacet(this, PREVIEW_BUTTON_FACET,
                    button);
        }
        button.setText(ThemeUtilities.getTheme(
                FacesContext.getCurrentInstance()).getMessage(
                PREVIEW_BUTTON_TEXT_KEY));

        return button;
    }

    // Called in IntervalListener
    /**
     * Called from IntervalListener, enable or disable dependent facets. If the
     * REPEAT_INTERVAL_FACET exists, has a non null value that is an instance of
     * {@code RepeatInterval} and is
     * }RepeatInterval.ONETIME} then disable the REPEAT_LIMIT_FACET
     * and REPEAT_INTERVAL_FACET facets. This includes setting the values of the
     * facets to null.
     * Otherwise enable both facets.
     * If the value is null or not an instance of {@code RepeatInterval}
     * then disable the REPEAT_LIMIT_FACET and REPEAT_INTERVAL_FACET facets as
     * described above.
     */
    public void updateRepeatUnitMenu() {

        if (DEBUG) {
            log("updateRepeatUnitMenu");
        }
        // We only need to do something if the fields that limit
        // repetition are shown
        if (!isLimitRepeating()) {
            if (DEBUG) {
                log("repeat limit fields not shown - return");
            }
            return;
        }
        // Get the private facets directly, don't initialize here.
        // unless it doesn't exist but it should exist.
        DropDown repeatIntervalComp
                = (DropDown) getPrivateFacet(REPEAT_INTERVAL_FACET);

        Object value = repeatIntervalComp.getValue();

        if (value == null || !(value instanceof RepeatInterval)) {
            if (DEBUG) {
                log("value is null - disable everything");
            }
            disableRepeatLimitComponents();
        } else {
            RepeatInterval ri = (RepeatInterval) value;
            if (DEBUG) {
                log("RI value is " + ri.getKey());
            }
            if (ri.getRepresentation().equals(RepeatInterval.ONETIME)) {
                if (DEBUG) {
                    log("value is ONE TIME - disable everything");
                }
                disableRepeatLimitComponents();

            } else {
                if (DEBUG) {
                    log("Repeat specified, enable everything: "
                            + ((RepeatInterval) value).getKey());
                }
                enableRepeatLimitComponents(ri);
            }
        }
    }

    @Override
    public String getPrimaryElementID(final FacesContext context) {
        return getLabeledElementId(context);
    }

    @Override
    public String getLabeledElementId(final FacesContext context) {
        UIComponent comp = getDateComponent();
        if (comp == null) {
            return null;
        }
        if (comp instanceof ComplexComponent) {
            return ((ComplexComponent) comp).getLabeledElementId(context);
        } else {
            return comp.getClientId(context);
        }
    }

    @Override
    public String getFocusElementId(final FacesContext context) {
        // For return the labeled component
        return getLabeledElementId(context);
    }

    /**
     * If the developer has not provided repeat interval items, return an
     * {@code Options} array of {@code RepeatIntervalOption} elements
     * representing the following intervals.
     * <p>
     * <ul>
     * <li>one time</li>
     * <li>hourly</li>
     * <li>daily</li>
     * <li>weekly</li>
     * <li>monthly</li>
     * </ul>
     * </p>
     * @return Object
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public Object getRepeatIntervalItems() {
        Object optionsObject = doGetRepeatIntervalItems();
        if (optionsObject == null) {
            Option[] options = new Option[5];
            options[0] = new RepeatIntervalOption(
                    RepeatInterval.getInstance(RepeatInterval.ONETIME));
            options[1] = new RepeatIntervalOption(
                    RepeatInterval.getInstance(RepeatInterval.HOURLY));
            options[2] = new RepeatIntervalOption(
                    RepeatInterval.getInstance(RepeatInterval.DAILY));
            options[3] = new RepeatIntervalOption(
                    RepeatInterval.getInstance(RepeatInterval.WEEKLY));
            options[4] = new RepeatIntervalOption(
                    RepeatInterval.getInstance(RepeatInterval.MONTHLY));
            optionsObject = options;

            // This should not be done.
            // This changes a developers bound value if there
            // is a setter. Unlikely, but should check for a value binding.
            setRepeatIntervalItems(options);
        }
        return optionsObject;
    }

    /**
     * If the developer has not provided repeat unit items, return an
     * {@code Options} array of {@code RepeatUnitOption} elements
     * representing the following units.
     * <p>
     * <ul>
     * <li>hours</li>
     * <li>days</li>
     * <li>weeks</li>
     * <li>months</li>
     * </ul>
     * </p>
     * @return Object
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public Object getRepeatUnitItems() {
        Object optionsObject = doGetRepeatUnitItems();
        if (optionsObject == null) {
            Option[] options = new Option[4];
            options[0] = new RepeatUnitOption(
                    RepeatUnit.getInstance(RepeatUnit.HOURS));
            options[1] = new RepeatUnitOption(
                    RepeatUnit.getInstance(RepeatUnit.DAYS));
            options[2] = new RepeatUnitOption(
                    RepeatUnit.getInstance(RepeatUnit.WEEKS));
            options[3] = new RepeatUnitOption(
                    RepeatUnit.getInstance(RepeatUnit.MONTHS));
            optionsObject = options;

            // This should not be done.
            // This changes a developers bound value if there
            // is a setter. Unlikely, but should check for a value binding.
            setRepeatUnitItems(options);
        }
        return optionsObject;
    }

    @Override
    public DateFormat getDateFormat() {
        return getDatePicker().getDateFormat();
    }

    /**
     * Get the JS object name.
     * @param context faces context
     * @return String
     */
    public String getJavaScriptObjectName(final FacesContext context) {
        return JavaScriptUtilities.getDomNode(context, this);
    }

    /**
     * Specialized decode behavior on top of that provided by the superclass. In
     * addition to the standard {@code processDecodes} behavior inherited
     * from {@link
     * UIComponentBase}, calls {@code validate()} if the the
     * {@code immediate} property is true; if the component is invalid
     * afterwards or a {@code RuntimeException} is thrown, calls
     * {@link FacesContext#renderResponse}.
     *
     * @exception NullPointerException
     */
    @Override
    public void processDecodes(final FacesContext context) {

        if (DEBUG) {
            log("processDecodes()");
        }
        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        setValid(true);

        // Obtain facets with the state that they had when the
        // facet was rendered. For the private facets which must
        // exist since component was rendered, use getPrivateFacet.
        // If that returns null, call the getXXXComponent method.
        // Only the DATE_FACET, and PREVIEW_BUTTON_FACET
        // facet have public alternatives. For these call the
        // getXXXComponent methods.
        UIComponent facet = getPrivateFacet(DATE_PICKER_FACET);
        facet.processDecodes(context);

        // never returns null;
        // might be developer defined facets.
        //
        getPreviewButtonComponent().processDecodes(context);
        getDateComponent().processDecodes(context);

        if (isStartTime()) {
            facet = getPrivateFacet(START_TIME_FACET);
            facet.processDecodes(context);
        }
        if (isEndTime()) {
            facet = getPrivateFacet(END_TIME_FACET);
            facet.processDecodes(context);
        }
        if (isRepeating()) {
            facet = getPrivateFacet(REPEAT_INTERVAL_FACET);
            facet.processDecodes(context);
            if (isLimitRepeating()) {
                facet = getPrivateFacet(REPEAT_LIMIT_FACET);
                facet.processDecodes(context);
                facet = getPrivateFacet(REPEAT_LIMIT_UNIT_FACET);
                facet.processDecodes(context);
            }
        }

        setSubmittedValue(VALUE_SUBMITTED);

        // There is nothing to decode other than the facets
        if (isImmediate()) {
            if (DEBUG) {
                log("Scheduler is immediate");
            }
            runValidation(context);
        }
    }

    /**
     * Perform the following algorithm to validate the local value of this
     * {@link UIInput}.<ul>
     * <li>Retrieve the submitted value with {@code getSubmittedValue()}.If this
     * returns null, exit without further processing. (This indicates that no
     * value was submitted for this component.)</li>
     *
     * <li> Convert the submitted value into a "local value" of the appropriate
     * data type by calling {@link #getConvertedValue}.</li>
     *
     * <li>Validate the property by calling {@link #validateValue}.</li>
     *
     * <li>If the {@code valid} property of this component is still
     * {@code true}, retrieve the previous value of the component (with
     * {@code getValue()}), store the new local value using {@code setValue()},
     * and reset the submitted value to null. If the local value is different
     * from the previous value of this component, fire a
     * {@link ValueChangeEvent} to be broadcast to all interested
     * listeners.</li>
     * </ul>
     *
     * <p>
     * Application components implementing {@link UIInput} that wish to perform
     * validation with logic embedded in the component should perform their own
     * correctness checks, and then call the {@code super.validate()} method to
     * perform the standard processing described above.</p>
     *
     * @param context The {@link FacesContext} for the current request
     * @param submittedValue submitted value
     * @return Object
     */
    // Note that the getXXCompoent methods are called here. The problem is
    // that getConvertedValue is called in encodeEnd when an immediate
    // request action occurs. It may not be appropriate to use the
    // component in its last rendered state, but it current
    // reinitialized state. But it's not clear which is best.
    // Ideally getConvertedValue is relegated to being called
    // onlyc during request processing and not during rendering
    // but that requires that facets and Scheduler always have the
    // apporpiate state by the time rendering occurs.
    // That is not the case currently.
    //
    @Override
    public Object getConvertedValue(final FacesContext context,
            final Object submittedValue) throws ConverterException {

        if (DEBUG) {
            log("getConvertedValue()");
        }

        if (context == null) {
            throw new NullPointerException();
        }

        // Process all the facets and children of this component
        Iterator kids = getFacetsAndChildren();
        UIComponent kid;

        while (kids.hasNext()) {
            kid = (UIComponent) kids.next();
            if (kid instanceof EditableValueHolder
                    && !(((EditableValueHolder) kid).isValid())) {
                return null;
            }
        }

        // We ran the ordinary validation process in processValidators, so
        // we use getValue() to get the value from the children - they
        // will have been processed at this point.
        Object dateValue = ((EditableValueHolder) getDateComponent())
                .getValue();
        Object startTimeValue = null;
        Object endTimeValue = null;

        if (DEBUG) {
            log("Date value is " + String.valueOf(dateValue));
        }

        if (isStartTime()) {
            startTimeValue = getStartTimeComponent().getValue();
            if (DEBUG) {
                log("Date value is " + String.valueOf(startTimeValue));
            }
        }
        if (isEndTime()) {
            endTimeValue = getEndTimeComponent().getValue();
            if (DEBUG) {
                log("Date value is " + String.valueOf(endTimeValue));
            }
        }

        ScheduledEvent newValue = createScheduledEvent(dateValue,
                startTimeValue, endTimeValue, context);
        if (isRepeating()) {
            boolean valueRepeat = false;
            Object repeatFrequency = getRepeatIntervalComponent().getValue();
            if (repeatFrequency != null
                    && repeatFrequency instanceof RepeatInterval) {
                RepeatInterval freq = (RepeatInterval) repeatFrequency;
                if (freq.getCalendarField() > -1) {
                    valueRepeat = true;
                }
            }
            if (valueRepeat) {
                newValue.setRepeatingEvent(true);
                try {
                    newValue.setRepeatInterval(
                            (RepeatInterval) repeatFrequency);
                    if (DEBUG) {
                        log("Repeat frequency value is " + String.valueOf(
                                        ((RepeatInterval) repeatFrequency)
                                                .getCalendarField()));
                    }

                    if (isLimitRepeating()) {
                        Object repeatUnit = getRepeatUnitComponent().getValue();
                        if (DEBUG) {
                            log("Repeat unit is " + String.valueOf(repeatUnit));
                        }
                        if (repeatUnit instanceof RepeatUnit) {
                            newValue.setDurationUnit((RepeatUnit) repeatUnit);
                        } else {
                            newValue.setDurationUnit(null);
                        }
                        Object repeatLimit = ((EditableValueHolder)
                                getRepeatingFieldComponent()).getValue();
                        if (DEBUG) {
                            log("Repeat unit is "
                                    + String.valueOf(repeatLimit));
                        }
                        if (repeatLimit instanceof Integer) {
                            newValue.setDuration((Integer) repeatLimit);
                        } else {
                            newValue.setDuration(null);
                        }
                    } else {
                        newValue.setDurationUnit(null);
                        newValue.setDuration(null);
                    }
                } catch (Exception ce) {
                    throw new ConverterException();
                }
            } else {
                newValue.setRepeatingEvent(false);
                newValue.setDurationUnit(null);
                newValue.setDuration(null);
            }
        } else {
            newValue.setRepeatingEvent(false);
            newValue.setDurationUnit(null);
            newValue.setDuration(null);
        }
        getDatePicker().setValue(newValue);
        return newValue;
    }

    /**
     * Return a component that implements a label for the facetName role. If a
     * facet named facetName is found that component is returned. Otherwise a
     * {@code Label} component is returned. It is assigned the id
     * {@code getId() + "_"  + facetName}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @param facetName the name of the facet to return or create
     * @param labelText the text for the label
     * @param labeledComponentId the absolute id to use for the label's
     * {@code for} attribute.
     *
     * @return a label facet component
     */
    private UIComponent getLabelFacet(final String facetName,
            final String labelText, final String labeledComponentId) {

        if (DEBUG) {
            log("getLabelFacet() " + facetName);
        }
        // Check if the page author has defined a label facet
        UIComponent labelFacet = getFacet(facetName);
        if (labelFacet != null) {
            return labelFacet;
        }

        // No longer modify a developer defined facet. Not even for the
        // "for" attribute, even though it may not be straightforward
        // to know what the labeled component is. Err on the side
        // of consistently not modifying a developer defined facet.
        //
        // Return the private facet or create one, but initialize
        // it every time
        //
        // We know it's a Label
        Label label = (Label) ComponentUtilities.getPrivateFacet(this,
                facetName, true);
        if (label == null) {
            label = new Label();
            label.setId(ComponentUtilities.createPrivateFacetId(this,
                    facetName));

            // Note that in the original code labels were not put
            // into the facet map. I'm not sure why. It's not clear
            // if the label will always be able find its "for"
            // component otherwise. Also the parent wasn't set either
            // which means the label will not have a clientId
            // renedered. Adding it to the facet map here.
            ComponentUtilities.putPrivateFacet(this, facetName, label);
        }

        label.setText(labelText);
        label.setFor(labeledComponentId);

        // FIXME: Should be part of Theme
        label.setLabelLevel(2);

        return label;
    }

    /**
     * Initialize a field facet.
     *
     * @param textField the TextField instance
     * @param columns the number of characters
     * @param requiredFlag if true the field is required.
     */
    private void initFieldFacet(final TextField textField, final int columns,
            final boolean requiredFlag) {

        if (DEBUG) {
            log("initFieldFacet()");
        }
        textField.setColumns(columns);
        textField.setTrim(true);
        textField.setRequired(requiredFlag);
        int tindex = getTabIndex();
        if (tindex > 0) {
            textField.setTabIndex(tindex);
        }
    }

    /**
     * Return a Time facet component. If
     * {@code ComponentUtilities.getPrivateFacet()} returns a facet named
     * {@code facetName} that component is initialized every time this
     * method is called and returned.
     * <p>
     * <em>This is a private facet.</em>
     * </p>
     * Otherwise a {@code Time} component is created and initialized. It is
     * assigned the id
     * {@code getId() + "_" + facetName} and added to the facets map as a
     * private facet.
     *
     * @param facetName facet name
     * @param requiredFlag required flag
     * @param hourKey hour key
     * @param minutesKey minutes key
     * @return a Time component.
     */
    private Time getTimeFacet(final String facetName,
            final boolean requiredFlag, final String hourKey,
            final String minutesKey) {

        if (DEBUG) {
            log("getTimeFacet() " + facetName);
        }
        // Not a public facet
        Time time = (Time) ComponentUtilities.getPrivateFacet(this,
                facetName, true);
        if (time == null) {
            if (DEBUG) {
                log("Create new Time Component");
            }
            time = new Time();
            time.setId(ComponentUtilities.createPrivateFacetId(this,
                    facetName));
            ComponentUtilities.putPrivateFacet(this, facetName, time);
        }

        time.setRequired(requiredFlag);
        time.setTimeZone(getTimeZone());
        time.setHourTooltipKey(hourKey);
        time.setMinutesTooltipKey(minutesKey);

        return time;
    }

    /**
     * Invoke {@code UIInput.validate}.
     * @param context faces context
     */
    private void runValidation(final FacesContext context) {
        try {
            validate(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }

        if (!isValid()) {
            context.renderResponse();
        }
    }

    /**
     * Note that this method can throw exception when getConvertedValue is
     * called during updateDatePicker in encodeEnd when immediate request
     * actions occur and some of the facet's have null values. As a side effect
     * the method can also set the isValid(false) on the getEndTimeComponent();
     *
     * @param dateObject date
     * @param startTimeObject start time
     * @param endTimeObject end time
     * @param context faces context
     * @return ScheduledEvent
     */
    private ScheduledEvent createScheduledEvent(final Object dateObject,
            final Object startTimeObject, final Object endTimeObject,
            final FacesContext context) {

        ScheduledEvent event = null;
        String messageKey = null;

        if (!(dateObject instanceof Date)) {
            messageKey = INVALID_DATE_TEXT_KEY;
        }
        if (!(startTimeObject instanceof ClockTime)) {
            messageKey = INVALID_START_TIME_TEXT_KEY;
        }
        if (endTimeObject != null && !(endTimeObject instanceof ClockTime)) {
            messageKey = INVALID_END_TIME_TEXT_KEY;
        }
        if (messageKey == null) {

            event = new ScheduledEvent();

            Date date = (Date) dateObject;
            ClockTime start = (ClockTime) startTimeObject;

            if (DEBUG) {
                log("Base date is " + date.toString());
            }
            event.setStartTime(calculateDate(date, start));

            if (endTimeObject != null) {
                ClockTime end = (ClockTime) endTimeObject;
                event.setEndTime(calculateDate(date, end));
                if (event.getEndTime().before(event.getStartTime())) {
                    messageKey = END_BEFORE_START_TEXT_KEY;
                    // This should not be happening here.
                    // This should be done during validation.
                    // Currently this can occur during a call to
                    // getConvertedValue within normal request processing
                    // or during encodeEnd in the event of an immediate
                    // request action, in updateDatePicker.
                    getEndTimeComponent().setValid(false);
                }
            }
        }

        if (messageKey != null) {
            String message
                    = ThemeUtilities.getTheme(context).getMessage(messageKey);
            throw new ConverterException(new FacesMessage(message));
        }
        return event;
    }

    /**
     * Compute a combined date with a specified date and time.
     * @param date date
     * @param time time
     * @return Date
     */
    private Date calculateDate(final Date date, final ClockTime time) {

        java.util.Calendar calendar = (java.util.Calendar) (getDatePicker()
                .getCalendar().clone());
        calendar.setTime(date);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, time.getHour());
        calendar.set(java.util.Calendar.MINUTE, time.getMinute());
        return calendar.getTime();
    }

    /**
     * Disable repeat limit component.
     */
    private void disableRepeatLimitComponents() {
        DropDown dd = getRepeatUnitComponent();
        UIComponent comp = getRepeatingFieldComponent();
        dd.setDisabled(true);
        dd.setSubmittedValue(null);
        dd.setValue(null);
        comp.getAttributes().put("disabled", Boolean.TRUE);
        ((EditableValueHolder) comp).setValue(null);
        ((EditableValueHolder) comp).setSubmittedValue(null);
    }

    /**
     * Enable repeat limit components.
     * @param ri repeat interval
     */
    private void enableRepeatLimitComponents(final RepeatInterval ri) {
        DropDown dd = getRepeatUnitComponent();
        UIComponent comp = getRepeatingFieldComponent();
        dd.setValue(ri.getDefaultRepeatUnit());
        dd.setSubmittedValue(null);
        dd.setDisabled(false);
        comp.getAttributes().put("disabled", Boolean.FALSE);
    }

    /**
     * Log a message to the standard out.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(Scheduler.class.getName() + "::" + msg);
    }

    /**
     * Return a private facet. This method is used during request processing to
     * obtain a private facet in its last rendered state. The facet should
     * exist, since the idea is that it was at least rendered once. If it
     * doesn't exist return the facet from the associated getXXXComponent
     * method. This method only supports the private facets DATE_PICKER_FACET,
     * START_TIME_FACET, END_TIME_FACET, REPEAT_LIMIT_UNIT_FACET,
     * REPEAT_INTERVAL_FACET.
     * @param facetName facet name
     * @return UIComponent
     */
    private UIComponent getPrivateFacet(final String facetName) {

        // Return the private facet if it exists
        UIComponent facet = ComponentUtilities.getPrivateFacet(this,
                facetName, false);
        if (facet != null) {
            return facet;
        }

        if (facetName.equals(DATE_PICKER_FACET)) {
            facet = getDatePicker();
        } else if (facetName.equals(START_TIME_FACET)) {
            facet = getStartTimeComponent();
        } else if (facetName.equals(END_TIME_FACET)) {
            facet = getEndTimeComponent();
        } else if (facetName.equals(REPEAT_LIMIT_UNIT_FACET)) {
            facet = getRepeatUnitComponent();
        } else if (facetName.equals(REPEAT_INTERVAL_FACET)) {
            facet = getRepeatIntervalComponent();
        }
        return facet;
    }

    // Take this from SchedulerRenderer. A Renderer shouldn't be
    // initializing or updating a component. Ideally the state should
    // be current before this point is reached thereby giving the
    // application an opportunity to affect the state in
    // INVOKE_APPLICATION_PHASE. But until these state changes
    // occur during request processing exlusively, the application
    // still does not have exclusive rights to affect the state
    // in INVOKE_APPLICATION_PHASE.
    @Override
    public void encodeEnd(final FacesContext context) throws IOException {

        if (context == null) {
            throw new NullPointerException();
        }
        if (!isRendered()) {
            return;
        }

        if (getSubmittedValue() != null) {
            if (DEBUG) {
                log("Found submitted value");
            }
            updateDatePicker(context);
        } else {
            if (DEBUG) {
                log("No submitted value");
            }
            initializeValues();
        }

        String rendererType = getRendererType();
        if (rendererType != null) {
            getRenderer(context).encodeEnd(context, this);
        }
    }

    /**
     * Most fields manage themselves when the component re-renders itself with a
     * submitted value. There are a couple of exceptions however, where the
     * value of one field affects another... This method is called if
     * Scheduler.getSubmittedValue returns non null. If the submitted
     * value is not null then on of the following is true.
     *
     * - An immediate action via, preview, or the repeat interval facet has
     * occurred - Validation has failed for Scheduler or one of the
     * sub-components.
     *
     * @param context faces context
     */
    private void updateDatePicker(final FacesContext context) {

        if (DEBUG) {
            log("updateDatePicker()");
        }
        try {
            Object value = getConvertedValue(context, null);

            // This happens IN getConvertedValue
            // when there is no exception
            getDatePicker().setValue(value);
        } catch (ConverterException ex) {

            // Why doesn't getConvertedValue do this ?
            UIComponent comp = getDateComponent();
            Object value = ((EditableValueHolder) comp).getSubmittedValue();
            if (value != null) {
                try {
                    Object dO = ConversionUtilities.convertValueToObject(comp,
                            (String) value, context);
                    getDatePicker().setValue(dO);
                } catch (ConverterException ex2) {
                    // do nothing
                }
            }
        }
    }

    /**
     * Need to figure out if this is the right thing to do? These components do
     * not retain their values unless managed this way. For the time component
     * the problem is that the submitted value can't be used to set the value,
     * so to speak.
     */
    private void initializeValues() {

        if (DEBUG) {
            log("initializeValue()");
        }
        CalendarMonth dp = getDatePicker();

        // Note that the repeating field component's disabled state
        // will NOT be modfied if
        //
        // value == null || value ! instanceof ScheduledEvent &&
        // isRepeating == false || (isRepeating == true &&
        //  isLimitRepeating == false)
        //
        // Or
        //
        // value != null && value instanceof ScheduledEvent &&
        // isRepeating == false || (isRepeating == true &&
        //  isLimitRepeating == false)
        //
        // So what does this mean ? Similarly getRepeatUnitComponent's
        // disabled state may also not be modfied under the same
        // conditions.
        Object value = getValue();
        if (value != null && value instanceof ScheduledEvent) {

            if (DEBUG) {
                log("Found scheduled event");
            }
            ScheduledEvent event = (ScheduledEvent) value;

            // Set the value of the date picker
            dp.setValue(event);
            dp.displayValue();

            // Initialize the start date field
            ((EditableValueHolder) getDateComponent())
                    .setValue(event.getStartTime());

            // Isn't this redundant ?
            //
            //dp.setValue(event);
            if (isStartTime()) {
                if (DEBUG) {
                    log("Setting the start time");
                }
                getStartTimeComponent().setValue(
                        getClockTime(dp, event.getStartTime()));
            }

            // Initialize the end date field
            if (isEndTime()) {
                getEndTimeComponent().setValue(
                        getClockTime(dp, event.getEndTime()));
            }

            // Initialize the fields that configure repetition
            if (isRepeating()) {
                RepeatInterval ri = event.getRepeatInterval();
                getRepeatIntervalComponent().setValue(ri);
                if (isLimitRepeating()) {
                    DropDown unitDropDown = getRepeatUnitComponent();
                    UIComponent comp = getRepeatingFieldComponent();
                    if (ri == null || ri.getRepresentation().equals(
                            RepeatInterval.ONETIME)) {
                        unitDropDown.setDisabled(true);
                        unitDropDown.setValue(null);
                        // Will trash a developer facet
                        //
                        ((EditableValueHolder) comp).setValue(null);
                        comp.getAttributes().put("disabled",
                                Boolean.TRUE);
                    } else {
                        unitDropDown.setDisabled(false);
                        unitDropDown.setValue(event.getDurationUnit());

                        // Will trash a developer defined facet
                        //
                        ((EditableValueHolder) comp).setValue(
                                event.getDuration());
                        comp.getAttributes().put("disabled",
                                Boolean.FALSE);
                    }
                }
            }

        } else {

            // Set the value of the date picker
            dp.setValue(null);

            ((EditableValueHolder) getDateComponent()).setValue(null);

            if (isStartTime()) {
                getStartTimeComponent().setValue(null);
            }
            if (isEndTime()) {
                getEndTimeComponent().setValue(null);
            }
            if (isRepeating()) {
                // This one handles itself - it's immediate...
                getRepeatIntervalComponent().setValue(RepeatInterval.ONETIME);
                if (isLimitRepeating()) {

                    DropDown ru = getRepeatUnitComponent();
                    ru.setValue(null);
                    ru.setDisabled(true);

                    EditableValueHolder rf = (EditableValueHolder)
                            getRepeatingFieldComponent();

                    // Will trash a developer defined facet
                    //
                    rf.setValue(null);
                    ((UIComponent) rf).getAttributes().put("disabled",
                            Boolean.TRUE);
                }
            }
        }
    }

    /**
     * Re-implement this as a converter for Time, and have time take Date too...
     *
     * @param datePicker date picker
     * @param date date
     * @return ClockTime
     */
    private ClockTime getClockTime(final CalendarMonth datePicker,
            final Date date) {

        if (DEBUG) {
            log("getClockTime()");
        }
        if (date == null) {
            if (DEBUG) {
                log("\tdate is null");
            }
            return null;
        }
        if (DEBUG) {
            log("date is " + date.toString());
        }
        java.util.Calendar calendar = (java.util.Calendar)
                (datePicker.getCalendar());
        calendar.setTime(date);

        ClockTime clockTime = new ClockTime();
        clockTime.setHour(calendar.get(
                java.util.Calendar.HOUR_OF_DAY));
        clockTime.setMinute(calendar.get(
                java.util.Calendar.MINUTE));

        if (DEBUG) {
            log("Hour is " + clockTime.getHour().toString());
        }
        if (DEBUG) {
            log("Hour is " + clockTime.getMinute().toString());
        }
        return clockTime;
    }

    // Since the value of the minDate attribute could change, we can't
    // cache this in an attribute.
    @Override
    public Date getFirstAvailableDate() {
        Date min = getMinDate();
        if (min == null) {
            java.util.Calendar calendar = getDatePicker().getCalendar();
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
            calendar.add(java.util.Calendar.YEAR, 4);
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
            calendar.set(java.util.Calendar.MINUTE, 59);
            calendar.set(java.util.Calendar.SECOND, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            max = calendar.getTime();
        }
        return max;
    }

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
     * <p>
     * The date format pattern to use (i.e. {@code yyyy-MM-dd}). The component
     * uses an instance of {@code java.text.SimpleDateFormat} and you may
     * specify a pattern to be used by this component, with the following
     * restriction: the format pattern must include {@code yyyy} (not
     * {@code yy}), {@code MM}, and {@code dd}; and no other parts of time may
     * be displayed. If a pattern is not specified, a locale-specific default is
     * used.</p>
     * <p>
     * If you change the date format pattern, you may also need to change the
     * {@code dateFormatPatternHelp} attribute. See the documentation for that
     * attribute.
     * </p>
     *
     * @see #getDateFormatPattern()
     * @param newDateFormatPattern dateFormatPattern
     */
    public void setDateFormatPattern(final String newDateFormatPattern) {
        this.dateFormatPattern = newDateFormatPattern;
    }

    /**
     * A message below the text field for the date, indicating the string format
     * to use when entering a date as text into the Start Date text field.
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
     * A message below the text field for the date, indicating the string format
     * to use when entering a date as text into the Start Date text field.
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
     * This text replaces the "Start Date" label.
     * @return String
     */
    public String getDateLabel() {
        if (this.dateLabel != null) {
            return this.dateLabel;
        }
        ValueExpression vb = getValueExpression("dateLabel");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * This text replaces the "Start Date" label.
     *
     * @see #getDateLabel()
     * @param newDateLabel dateLabel
     */
    public void setDateLabel(final String newDateLabel) {
        this.dateLabel = newDateLabel;
    }

    /**
     * Standard HTML attribute which determines whether the web application user
     * can change the the value of this component. NOT YET IMPLEMENTED.
     * @return {@code boolean}
     */
    public boolean isDisabled() {
        if (this.disabledSet) {
            return this.disabled;
        }
        ValueExpression vb = getValueExpression("disabled");
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
     * Standard HTML attribute which determines whether the web application user
     * can change the the value of this component. NOT YET IMPLEMENTED.
     *
     * @see #isDisabled()
     * @param newDisabled disabled
     */
    public void setDisabled(final boolean newDisabled) {
        this.disabled = newDisabled;
        this.disabledSet = true;
    }

    /**
     * Flag indicating that an input field for the end time should be shown. The
     * default value is true.
     * @return {@code boolean}
     */
    public boolean isEndTime() {
        if (this.endTimeSet) {
            return this.endTime;
        }
        ValueExpression vb = getValueExpression("endTime");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return true;
    }

    /**
     * Flag indicating that an input field for the end time should be shown. The
     * default value is true.
     *
     * @see #isEndTime()
     * @param newEndTime endTime
     */
    public void setEndTime(final boolean newEndTime) {
        this.endTime = newEndTime;
        this.endTimeSet = true;
    }

    /**
     * This text replaces the "End Time" label.
     * @return String
     */
    public String getEndTimeLabel() {
        if (this.endTimeLabel != null) {
            return this.endTimeLabel;
        }
        ValueExpression vb = getValueExpression("endTimeLabel");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * This text replaces the "End Time" label.
     *
     * @see #getEndTimeLabel()
     * @param newEndTimeLabel endTimeLabel
     */
    public void setEndTimeLabel(final String newEndTimeLabel) {
        this.endTimeLabel = newEndTimeLabel;
    }

    /**
     * Flag indicating that a control for setting a limit for repeating events
     * should be shown. The default value is true.
     * @return {@code boolean}
     */
    public boolean isLimitRepeating() {
        if (this.limitRepeatingSet) {
            return this.limitRepeating;
        }
        ValueExpression vb = getValueExpression("limitRepeating");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return true;
    }

    /**
     * Flag indicating that a control for setting a limit for repeating events
     * should be shown. The default value is true.
     *
     * @see #isLimitRepeating()
     * @param newLimitRepeating limitRepeating
     */
    public void setLimitRepeating(final boolean newLimitRepeating) {
        this.limitRepeating = newLimitRepeating;
        this.limitRepeatingSet = true;
    }

    /**
     * A {@code java.util.Date} object representing the last selectable
     * day. The default value is four years after the {@code minDate}
     * (which is evaluated first).
     * <p>
     * The value of this attribute is reflected in the years that are available
     * for selection in the month display. In future releases of this component,
     * web application users will also not be able to view months after this
     * date, or select days that follow this date. At present such dates can be
     * selected, but will not be validated when the form is submitted.</p>
     * @return java.util.Date
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
     * day. The default value is four years after the {@code minDate}
     * (which is evaluated first).
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
     * day. The default value is today's date.
     * <p>
     * The value of this attribute is reflected in the years that are available
     * for selection in the month display. In future releases of this component,
     * web application users will also not be able to view months before this
     * date, or select days that precede this date. At present such dates can be
     * selected, but will not be validated when the form is submitted.</p>
     * @return java.util.Date
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
     * day. The default value is today's date.
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
     * Flag indicating that the "Preview in Browser" button should be displayed
     * - default value is true.
     * @return {@code boolean}
     */
    public boolean isPreviewButton() {
        if (this.previewButtonSet) {
            return this.previewButton;
        }
        ValueExpression vb = getValueExpression("previewButton");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return true;
    }

    /**
     * Flag indicating that the "Preview in Browser" button should be displayed
     * - default value is true.
     *
     * @see #isPreviewButton()
     * @param newPreviewButton previewButton
     */
    public void setPreviewButton(final boolean newPreviewButton) {
        this.previewButton = newPreviewButton;
        this.previewButtonSet = true;
    }

    /**
     * If this attribute is set to true, the value of the component is rendered
     * as text, preceded by the label if one was defined. NOT YET
     * IMPLEMENTED.
     * @return {@code boolean}
     */
    public boolean isReadOnly() {
        if (this.readOnlySet) {
            return this.readOnly;
        }
        ValueExpression vb = getValueExpression("readOnly");
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
     * If this attribute is set to true, the value of the component is rendered
     * as text, preceded by the label if one was defined. NOT YET
     * IMPLEMENTED.
     *
     * @see #isReadOnly()
     * @param newReadOnly readOnly
     */
    public void setReadOnly(final boolean newReadOnly) {
        this.readOnly = newReadOnly;
        this.readOnlySet = true;
    }

    /**
     * Override the items that appear on the repeat interval menu. The value
     * must be one of an array, Map or Collection whose members are all
     * subclasses of
     * {@code com.sun.webui.jsf.model.scheduler.RepeatIntervalOption},
     * whose values must be one of the member classes of
     * {@code com.sun.webui.jsf.model.scheduler.RepeatInterval}, for
     * example
     * {@code com.sun.webui.jsf.model.scheduler.RepeatInterval.ONETIME} or
     * {@code com.sun.webui.jsf.model.scheduler.RepeatInterval.HOURLY}. If
     * this attribute is not specified, default options of "One Time", "Hourly",
     * "Weekly", "Monthly" will be shown.
     * @return Object
     */
    private Object doGetRepeatIntervalItems() {
        if (this.repeatIntervalItems != null) {
            return this.repeatIntervalItems;
        }
        ValueExpression vb = getValueExpression("repeatIntervalItems");
        if (vb != null) {
            return (Object) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Override the items that appear on the repeat interval menu. The value
     * must be one of an array, Map or Collection whose members are all
     * subclasses of
     * {@code com.sun.webui.jsf.model.scheduler.RepeatIntervalOption},
     * whose values must be one of the member classes of
     * {@code com.sun.webui.jsf.model.scheduler.RepeatInterval}, for
     * example
     * {@code com.sun.webui.jsf.model.scheduler.RepeatInterval.ONETIME} or
     * {@code com.sun.webui.jsf.model.scheduler.RepeatInterval.HOURLY}. If
     * this attribute is not specified, default options of "One Time", "Hourly",
     * "Weekly", "Monthtly" will be shown.
     *
     * @see #getRepeatIntervalItems()
     * @param newRepeatIntervalItems repeatIntervalItems
     */
    public void setRepeatIntervalItems(final Object newRepeatIntervalItems) {
        this.repeatIntervalItems = newRepeatIntervalItems;
    }

    /**
     * Override the default value of the label for the repeat interval menu.
     * @return String
     */
    public String getRepeatIntervalLabel() {
        if (this.repeatIntervalLabel != null) {
            return this.repeatIntervalLabel;
        }
        ValueExpression vb = getValueExpression("repeatIntervalLabel");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Override the default value of the label for the repeat interval menu.
     *
     * @see #getRepeatIntervalLabel()
     * @param newRepeatIntervalLabel repeatIntervalLabel
     */
    public void setRepeatIntervalLabel(final String newRepeatIntervalLabel) {
        this.repeatIntervalLabel = newRepeatIntervalLabel;
    }

    /**
     * Override the default value of the label for the repeat limit menu.
     * @return String
     */
    public String getRepeatLimitLabel() {
        if (this.repeatLimitLabel != null) {
            return this.repeatLimitLabel;
        }
        ValueExpression vb = getValueExpression("repeatLimitLabel");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Override the default value of the label for the repeat limit menu.
     *
     * @see #getRepeatLimitLabel()
     * @param newRepeatLimitLabel repeatLimitLabel
     */
    public void setRepeatLimitLabel(final String newRepeatLimitLabel) {
        this.repeatLimitLabel = newRepeatLimitLabel;
    }

    /**
     * Override the items that appear on the repeat limit unit menu. The value
     * must be one of an array, Map or Collection whose members are all
     * subclasses of {@code com.sun.webui.jsf.model.Option}, and the value
     * of the options must implement the
     * {@code com.sun.webui.jsf.model.RepeatUnit} interface. The default
     * value is to show a menu with values "Hours", "Days", "Weeks",
     * "Months".
     * @return Object
     */
    private Object doGetRepeatUnitItems() {
        if (this.repeatUnitItems != null) {
            return this.repeatUnitItems;
        }
        ValueExpression vb = getValueExpression("repeatUnitItems");
        if (vb != null) {
            return (Object) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Override the items that appear on the repeat limit unit menu. The value
     * must be one of an array, Map or Collection whose members are all
     * subclasses of {@code com.sun.webui.jsf.model.Option}, and the value
     * of the options must implement the
     * {@code com.sun.webui.jsf.model.RepeatUnit} interface. The default
     * value is to show a menu with values "Hours", "Days", "Weeks",
     * "Months".
     *
     * @see #getRepeatUnitItems()
     * @param newRepeatUnitItems repeatUnitItems
     */
    public void setRepeatUnitItems(final Object newRepeatUnitItems) {
        this.repeatUnitItems = newRepeatUnitItems;
    }

    /**
     * Flag indicating that a control for scheduling a repeated event should be
     * shown. The default value is true.
     * @return {@code boolean}
     */
    public boolean isRepeating() {
        if (this.repeatingSet) {
            return this.repeating;
        }
        ValueExpression vb = getValueExpression("repeating");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return true;
    }

    /**
     * Flag indicating that a control for scheduling a repeated event should be
     * shown. The default value is true.
     *
     * @see #isRepeating()
     * @param newRepeating repeating
     */
    public void setRepeating(final boolean newRepeating) {
        this.repeating = newRepeating;
        this.repeatingSet = true;
    }

    /**
     * Flag indicating that the user must enter a value for this Scheduler.
     * Default value is true.
     * @return {@code boolean}
     */
    @Override
    public boolean isRequired() {
        if (this.requiredSet) {
            return this.required;
        }
        ValueExpression vb = getValueExpression("required");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return true;
    }

    /**
     * Flag indicating that the user must enter a value for this Scheduler.
     * Default value is true.
     *
     * @see #isRequired()
     * @param newRequired required
     */
    @Override
    public void setRequired(final boolean newRequired) {
        this.required = newRequired;
        this.requiredSet = true;
    }

    /**
     * Flag indicating if the "* indicates required field" message should be
     * displayed - default value is true.
     * @return {@code boolean}
     */
    public boolean isRequiredLegend() {
        if (this.requiredLegendSet) {
            return this.requiredLegend;
        }
        ValueExpression vb = getValueExpression("requiredLegend");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return true;
    }

    /**
     * Flag indicating if the "* indicates required field" message should be
     * displayed - default value is true.
     *
     * @see #isRequiredLegend()
     * @param newRequiredLegend requiredLegend
     */
    public void setRequiredLegend(final boolean newRequiredLegend) {
        this.requiredLegend = newRequiredLegend;
        this.requiredLegendSet = true;
    }

    /**
     * Flag indicating that an input field for the start time should be shown.
     * The default value is true.
     * @return {@code boolean}
     */
    public boolean isStartTime() {
        if (this.startTimeSet) {
            return this.startTime;
        }
        ValueExpression vb = getValueExpression("startTime");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return true;
    }

    /**
     * Flag indicating that an input field for the start time should be shown.
     * The default value is true.
     *
     * @see #isStartTime()
     * @param newStartTime startTime
     */
    public void setStartTime(final boolean newStartTime) {
        this.startTime = newStartTime;
        this.startTimeSet = true;
    }

    /**
     * This text replaces the "Start Time" label.
     * @return String
     */
    public String getStartTimeLabel() {
        if (this.startTimeLabel != null) {
            return this.startTimeLabel;
        }
        ValueExpression vb = getValueExpression("startTimeLabel");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * This text replaces the "Start Time" label.
     *
     * @see #getStartTimeLabel()
     * @param newStartTimeLabel startTimeLabel
     */
    public void setStartTimeLabel(final String newStartTimeLabel) {
        this.startTimeLabel = newStartTimeLabel;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     * @return String
     */
    public String getStyle() {
        if (this.style != null) {
            return this.style;
        }
        ValueExpression vb = getValueExpression("style");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @see #getStyle()
     * @param newStyle style
     */
    public void setStyle(final String newStyle) {
        this.style = newStyle;
    }

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     * @return String
     */
    public String getStyleClass() {
        if (this.styleClass != null) {
            return this.styleClass;
        }
        ValueExpression vb = getValueExpression("styleClass");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @see #getStyleClass()
     * @param newStyleClass styleClass
     */
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

    /**
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     * @return int
     */
    public int getTabIndex() {
        if (this.tabIndexSet) {
            return this.tabIndex;
        }
        ValueExpression vb = getValueExpression("tabIndex");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     *
     * @see #getTabIndex()
     * @param newTabIndex tabIndex
     */
    public void setTabIndex(final int newTabIndex) {
        this.tabIndex = newTabIndex;
        this.tabIndexSet = true;
    }

    /**
     * The {@code java.util.TimeZone} used with this component. Unless set,
     * the default TimeZone for the locale in
     * {@code jakarta.faces.component.UIViewRoot} is used.
     * @return TimeZone
     */
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

    /**
     * The {@code java.util.TimeZone} used with this component. Unless set,
     * the default TimeZone for the locale in
     * {@code jakarta.faces.component.UIViewRoot} is used.
     *
     * @see #getTimeZone()
     * @param newTimeZone timeZone
     */
    public void setTimeZone(final java.util.TimeZone newTimeZone) {
        this.timeZone = newTimeZone;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     * @return {@code boolean}
     */
    public boolean isVisible() {
        if (this.visibleSet) {
            return this.visible;
        }
        ValueExpression vb = getValueExpression("visible");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return true;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     *
     * @see #isVisible()
     * @param newVisible visible
     */
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.dateFormatPattern = (String) values[1];
        this.dateFormatPatternHelp = (String) values[2];
        this.dateLabel = (String) values[3];
        this.disabled = ((Boolean) values[4]);
        this.disabledSet = ((Boolean) values[5]);
        this.endTime = ((Boolean) values[6]);
        this.endTimeSet = ((Boolean) values[7]);
        this.endTimeLabel = (String) values[8];
        this.limitRepeating = ((Boolean) values[9]);
        this.limitRepeatingSet = ((Boolean) values[10]);
        this.maxDate = (java.util.Date) values[11];
        this.minDate = (java.util.Date) values[12];
        this.previewButton = ((Boolean) values[13]);
        this.previewButtonSet = ((Boolean) values[14]);
        this.readOnly = ((Boolean) values[15]);
        this.readOnlySet = ((Boolean) values[16]);
        this.repeatIntervalItems = (Object) values[17];
        this.repeatIntervalLabel = (String) values[18];
        this.repeatLimitLabel = (String) values[19];
        this.repeatUnitItems = (Object) values[20];
        this.repeating = ((Boolean) values[21]);
        this.repeatingSet = ((Boolean) values[22]);
        this.required = ((Boolean) values[23]);
        this.requiredSet = ((Boolean) values[24]);
        this.requiredLegend = ((Boolean) values[25]);
        this.requiredLegendSet = ((Boolean) values[26]);
        this.startTime = ((Boolean) values[27]);
        this.startTimeSet = ((Boolean) values[28]);
        this.startTimeLabel = (String) values[29];
        this.style = (String) values[30];
        this.styleClass = (String) values[31];
        this.tabIndex = ((Integer) values[32]);
        this.tabIndexSet = ((Boolean) values[33]);
        this.timeZone = (java.util.TimeZone) values[34];
        this.visible = ((Boolean) values[35]);
        this.visibleSet = ((Boolean) values[36]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[37];
        values[0] = super.saveState(context);
        values[1] = this.dateFormatPattern;
        values[2] = this.dateFormatPatternHelp;
        values[3] = this.dateLabel;
        if (disabled) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        if (disabledSet) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        if (endTime) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        if (endTimeSet) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        values[8] = this.endTimeLabel;
        if (limitRepeating) {
            values[9] = Boolean.TRUE;
        } else {
            values[9] = Boolean.FALSE;
        }
        if (limitRepeatingSet) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        values[11] = this.maxDate;
        values[12] = this.minDate;
        if (previewButton) {
            values[13] = Boolean.TRUE;
        } else {
            values[13] = Boolean.FALSE;
        }
        if (previewButtonSet) {
            values[14] = Boolean.TRUE;
        } else {
            values[14] = Boolean.FALSE;
        }
        if (readOnly) {
            values[15] = Boolean.TRUE;
        } else {
            values[15] = Boolean.FALSE;
        }
        if (readOnlySet) {
            values[16] = Boolean.TRUE;
        } else {
            values[16] = Boolean.FALSE;
        }
        values[17] = this.repeatIntervalItems;
        values[18] = this.repeatIntervalLabel;
        values[19] = this.repeatLimitLabel;
        values[20] = this.repeatUnitItems;
        if (repeating) {
            values[21] = Boolean.TRUE;
        } else {
            values[21] = Boolean.FALSE;
        }
        if (repeatingSet) {
            values[22] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (required) {
            values[23] = Boolean.TRUE;
        } else {
            values[23] = Boolean.FALSE;
        }
        if (requiredSet) {
            values[24] = Boolean.TRUE;
        } else {
            values[24] = Boolean.FALSE;
        }
        if (requiredLegend) {
            values[25] = Boolean.TRUE;
        } else {
            values[25] = Boolean.FALSE;
        }
        if (requiredLegendSet) {
            values[26] = Boolean.TRUE;
        } else {
            values[26] = Boolean.FALSE;
        }
        if (startTime) {
            values[27] = Boolean.TRUE;
        } else {
            values[27] = Boolean.FALSE;
        }
        if (startTimeSet) {
            values[28] = Boolean.TRUE;
        } else {
            values[28] = Boolean.FALSE;
        }
        values[29] = this.startTimeLabel;
        values[30] = this.style;
        values[31] = this.styleClass;
        values[32] = this.tabIndex;
        if (tabIndexSet) {
            values[33] = Boolean.TRUE;
        } else {
            values[33] = Boolean.FALSE;
        }
        values[34] = this.timeZone;
        if (visible) {
            values[35] = Boolean.TRUE;
        } else {
            values[35] = Boolean.FALSE;
        }
        if (visibleSet) {
            values[36] = Boolean.TRUE;
        } else {
            values[36] = Boolean.FALSE;
        }
        return values;
    }
}
