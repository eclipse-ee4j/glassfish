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
import com.sun.webui.jsf.model.ClockTime;
import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.IntegerConverter;
import jakarta.faces.event.ValueChangeEvent;

import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This component is for internal use only.
 */
@Component(type = "com.sun.webui.jsf.Time",
        family = "com.sun.webui.jsf.Time",
        displayName = "Time",
        isTag = false,
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_time",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_time_props")
        //CHECKSTYLE:ON
public final class Time extends WebuiInput implements NamingContainer {

    /**
     * The hour menu facet name.
     */
    public static final String HOUR_FACET = "hour";

    /**
     * The minutes menu facet name.
     */
    public static final String MINUTES_FACET = "minutes";

    /**
     * Time submitted.
     */
    private static final String TIME_SUBMITTED
            = "com.sun.webui.jsf.TimeSubmitted";

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Standard HTML attribute which determines whether the web application user
     * can change the the value of this component.
     */
    @Property(name = "disabled", displayName = "Disabled")
    private boolean disabled = false;

    /**
     * disabled set flag.
     */
    private boolean disabledSet = false;

    /**
     * If this attribute is set to true, the value of the component is rendered
     * as text, preceded by the label if one was defined.
     */
    @Property(name = "readOnly", displayName = "Read-only")
    private boolean readOnly = false;

    /**
     * readOnly set flag.
     */
    private boolean readOnlySet = false;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style", displayName = "CSS Style(s)")
    private String style = null;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "styleClass",
            displayName = "CSS Style Class(es)")
    private String styleClass = null;

    /**
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     */
    @Property(name = "tabIndex", displayName = "Tab Index")
    private int tabIndex = Integer.MIN_VALUE;

    /**
     * tabIndex set flag.
     */
    private boolean tabIndexSet = false;

    /**
     * A binding to a Time Zone instance to use for this Scheduler. If none is
     * specified, the Scheduler uses the default TimeZone from the Schedulers
     * locale.
     */
    @Property(name = "timeZone", displayName = "Time Zone")
    private java.util.TimeZone timeZone = null;

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     *
     * @return {@code boolean}
     */
    @Property(name = "visible", displayName = "Visible")
    private boolean visible = false;

    /**
     * visible set flag.
     */
    private boolean visibleSet = false;

    /**
     * Holds value of property hourTooltipKey.
     */
    private String hourTooltipKey;

    /**
     * Holds value of property minutesTooltipKey.
     */
    private String minutesTooltipKey;

    /**
     * Default constructor.
     */
    public Time() {
        super();
        setRendererType("com.sun.webui.jsf.Time");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Time";
    }

    /**
     * Return a DropDown component that implements an hour menu. If
     * {@code ComponentUtilities.getPrivateFacet()} returns a facet named
     * {@code hour} that component is initialized every time this method is
     * called and returned.
     * <p>
     * <em>This is a private facet.</em>
     * </p>
     * Otherwise a {@code DropDown} component is created and initialized.
     * It is assigned the id
     * {@code getId() + "_hour"} and added to the facets map as a private
     * facet.
     *
     * @return an hour menu DropDown component.
     */
    public DropDown getHourMenu() {
        return getMenu(HOUR_FACET, getHourItems());
    }

    /**
     * Return a DropDown component that implements a minutes menu. If
     * {@code ComponentUtilities.getPrivateFacet()} returns a facet named
     * {@code minutes} that component is initialized every time this method
     * is called and returned.
     * <p>
     * <em>This is a private facet.</em>
     * </p>
     * Otherwise a {@code DropDown} component is created and initialized.
     * It is assigned the id
     * {@code getId() + "_minutes"} and added to the facets map as a
     * private facet.
     *
     * @return a minutes menu DropDown component.
     */
    public DropDown getMinutesMenu() {
        return getMenu(MINUTES_FACET, getMinuteItems());
    }

    /**
     * Return a DropDown component for a menu. If
     * {@code ComponentUtilities.getPrivateFacet()} returns a facet named
     * {@code facet} that component is initialized every time this method
     * is called and returned.
     * <p>
     * <em>This method returns a private facet.</em>
     * </p>
     * Otherwise a {@code DropDown} component is created and initialized.
     * It is assigned the id
     * {@code getId() + "_" + facet} and added to the facets map as a
     * private facet.
     *
     * @param facet the facet name
     * @param options the menu options
     *
     * @return a DropDown menu component.
     */
    private DropDown getMenu(final String facet, final Option[] options) {

        if (DEBUG) {
            log("getMenu() for facet " + facet);
        }
        // Support only a private facet.
        //
        DropDown menu = (DropDown) ComponentUtilities.getPrivateFacet(this,
                facet, true);
        if (menu == null) {
            if (DEBUG) {
                log("createDropDown() for facet " + facet);
            }
            menu = new DropDown();
            menu.setId(ComponentUtilities.createPrivateFacetId(this, facet));
            // Doesn't change
            menu.setItems(options);
            menu.setConverter(new IntegerConverter());
            ComponentUtilities.putPrivateFacet(this, facet, menu);
        }

        // Probably should be doing tooltips here as well.
        // However, I was reluctant to change the logic
        // regarding setting tooltips at this time but
        // it should be revisited. See encodeEnd in this file and
        // Scheduler.
        int tindex = getTabIndex();
        if (tindex > 0) {
            menu.setTabIndex(tindex);
        }
        menu.setDisabled(isDisabled());
        menu.setRequired(isRequired());
        return menu;
    }

    /**
     * Convenience method to return at Option[] with all of the hours defined in
     * 24 hourObject format.
     *
     * @return An Option[] containing all the hours
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private Option[] getHourItems() {
        Option[] hours = new Option[25];
        hours[0] = new Option(-1, " ");
        int counter = 0;
        while (counter < 10) {
            hours[counter + 1] = new Option(counter, "0" + counter);
            ++counter;
        }
        while (counter < 24) {
            hours[counter + 1] = new Option(counter,
                    String.valueOf(counter));
            ++counter;
        }
        return hours;
    }

    /**
     * Convenience method to return at Option[] with all of the minutes (in 5
     * minuteObject increments) for an hourObject.
     *
     * @return An Option[] containing all the minutes
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private Option[] getMinuteItems() {
        Option[] minutes = new Option[13];
        minutes[0] = new Option(-1, " ");
        minutes[1] = new Option(0, "00");
        minutes[2] = new Option(5, "05");
        for (int i = 2; i < 12; i++) {
            minutes[i + 1] = new Option(5 * i, String.valueOf(5 * i));
        }
        return minutes;
    }

    /**
     * Get the time-zone as a string.
     * @return String
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public String getOffset() {
        java.util.Calendar calendar = getCalendar();
        TimeZone tz = calendar.getTimeZone();

        StringBuilder gmtTimeZone = new StringBuilder(8);

        int value = calendar.get(java.util.Calendar.ZONE_OFFSET)
                + calendar.get(java.util.Calendar.DST_OFFSET);

        if (value < 0) {
            // GMT - hh:mm
            gmtTimeZone.append('-');
            value = -value;
        } else {
            // GMT + hh:mm
            gmtTimeZone.append('+');
        }

        // determine the offset hours
        int num = value / (1000 * 60 * 60);
        if (num < 10) {
            // display offset as GMT + 0h:mm
            gmtTimeZone.append("0");
        }

        // add the hh: part
        gmtTimeZone.append(num).append(":");

        // determine the offset minutes
        num = (value % (1000 * 60 * 60)) / (1000 * 60);
        if (num < 10) {
            // display as hh:0m
            gmtTimeZone.append("0");
        }

        // append the minutes
        gmtTimeZone.append(num);
        return gmtTimeZone.toString();
    }

    /**
     * Returns a new Calendar instance corresponding to the user's current
     * locale and the developer specified time zone (if any).
     *
     * @return {@code java.util.Calendar} A new Calendar instance with the
     * correct locale and time zone.
     */
    public java.util.Calendar getCalendar() {
        java.util.Calendar calendar;
        Locale locale = FacesContext.getCurrentInstance().getViewRoot()
                .getLocale();
        if (locale == null) {
            locale = Locale.getDefault();
        }
        TimeZone tz = getTimeZone();
        if (tz == null) {
            calendar = java.util.Calendar.getInstance(locale);
        } else {
            calendar = java.util.Calendar.getInstance(tz, locale);
        }
        return calendar;
    }

    /**
     * Getter for property hourTooltipKey.
     *
     * @return Value of property hourTooltipKey.
     */
    public String getHourTooltipKey() {
        return this.hourTooltipKey;
    }

    /**
     * Setter for property hourTooltipKey.
     *
     * @param newHourTooltipKey New value of property hourTooltipKey.
     */
    public void setHourTooltipKey(final String newHourTooltipKey) {
        this.hourTooltipKey = newHourTooltipKey;
    }

    /**
     * Getter for property minutesTooltipKey.
     *
     * @return Value of property minutesTooltipKey.
     */
    public String getMinutesTooltipKey() {
        return this.minutesTooltipKey;
    }

    /**
     * Setter for property minutesTooltipKey.
     *
     * @param newMinutesTooltipKey New value of property minutesTooltipKey.
     */
    public void setMinutesTooltipKey(final String newMinutesTooltipKey) {
        this.minutesTooltipKey = newMinutesTooltipKey;
    }

    @Override
    public void processDecodes(final FacesContext context) {
        if (DEBUG) {
            log("processDecodes");
        }

        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        setValid(true);

        // There is no public facet. Only the private facet.
        // Get the private facet but don't validate the id since
        // we want the facet that was rendered.
        //
        // The assumption is that the facets will already exist since the
        // component was rendered. And if it wasn't rendered
        // this method should be executed.
        ComponentUtilities.getPrivateFacet(this, HOUR_FACET, false).
                processDecodes(context);
        ComponentUtilities.getPrivateFacet(this, MINUTES_FACET, false).
                processDecodes(context);
        setSubmittedValue(TIME_SUBMITTED);

        // There is nothing to decode other than the facets
        if (isImmediate()) {
            if (DEBUG) {
                log("Time is immediate");
            }
            runValidation(context);
        }
    }

    @Override
    public void validate(final FacesContext context) {

        if (DEBUG) {
            log("validate()");
        }
        if (context == null) {
            throw new NullPointerException();
        }

        // There is no public facet. Only the private facet.
        // Get the private facet but don't validate the id since
        // we want the facet that was rendered. And don't call
        // getHourMenu or getMinutesMenu since we don't want to reinitialize.
        //
        // The assumption is that the facets will already exist since the
        // component was rendered. And if it wasn't rendered
        // this method should be executed.
        Object hourValue = ((EditableValueHolder) ComponentUtilities
                .getPrivateFacet(this, HOUR_FACET, false)).getValue();

        if (DEBUG) {
            log("Hour value is " + String.valueOf(hourValue));
        }
        Object minuteValue = ((EditableValueHolder) ComponentUtilities
                .getPrivateFacet(this, MINUTES_FACET, false)).getValue();
        if (DEBUG) {
            log("Minute value is " + String.valueOf(minuteValue));
        }
        ClockTime newValue = null;

        try {
            newValue = createClockTime(hourValue, minuteValue, context);
            if (DEBUG) {
                log("Created ClockTime");
            }
        } catch (ConverterException ce) {
            FacesMessage message = ce.getFacesMessage();
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            context.addMessage(getClientId(context), message);
            setValid(false);
            context.renderResponse();
        } catch (Exception ex) {
            // TODO - message
            FacesMessage message = new FacesMessage("Invalid input");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            context.addMessage(getClientId(context), message);
            setValid(false);
            context.renderResponse();
        }

        // If our value is valid, store the new value, erase the
        // "submitted" value, and emit a ValueChangeEvent if appropriate
        if (isValid()) {
            if (DEBUG) {
                log("\tComponent is valid");
            }
            Object previous = getValue();
            setValue(newValue);
            if (DEBUG) {
                log("\tNew value: " + String.valueOf(newValue));
            }
            setSubmittedValue(null);
            if (compareValues(previous, newValue)) {
                queueEvent(new ValueChangeEvent(this, previous, newValue));
            }
        }
    }

    /**
     * Run the component validation.
     * @param context faces context
     */
    private void runValidation(final FacesContext context) {

        if (DEBUG) {
            log("runValidation()");
        }
        try {
            validate(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }

        if (!isValid()) {
            if (DEBUG) {
                log("\tnot valid");
            }
            context.renderResponse();
        }
    }

    /**
     * Create clock time.
     * @param hourObject hour
     * @param minuteObject minute
     * @param context faces context
     * @return ClockTime
     */
    private ClockTime createClockTime(final Object hourObject,
            final Object minuteObject, final FacesContext context) {

        if (DEBUG) {
            log("CreateClockTime()");
        }
        String messageKey = null;
        ClockTime time = null;

        if (hourObject instanceof Integer
                && minuteObject instanceof Integer) {
            if (DEBUG) {
                log("Found integers");
            }
            int hour = ((Integer) hourObject);
            int minute = ((Integer) minuteObject);
            if (hour == -1 && minute == -1) {
                if (DEBUG) {
                    log("No selections made");
                }
                if (isRequired()) {
                    messageKey = "Time.required";
                } else {
                    return null;
                }
            } else if (hour == -1) {
                messageKey = "Time.enterHour";
            } else if (minute == -1) {
                messageKey = "Time.enterMinute";
            } else {
                time = new ClockTime();
                try {
                    if (DEBUG) {
                        log("Hour is " + hour);
                    }
                    if (DEBUG) {
                        log("Minute is " + minute);
                    }
                    time.setHour(hour);
                    time.setMinute(minute);
                } catch (Exception ex) {
                    if (DEBUG) {
                        LogUtil.finest("invaliddata", ex);
                    }
                    messageKey = "Time.invalidData";
                }
            }
        } else {
            if (isRequired()) {
                messageKey = "Time.required";
            } else {
                return null;
            }

        }

        if (messageKey != null) {
            if (DEBUG) {
                log("Invalid input");
            }
            String message = ThemeUtilities
                    .getTheme(context).getMessage(messageKey);
            throw new ConverterException(new FacesMessage(message));
        }
        return time;
    }

    /**
     * Log a message to the standard out.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(Time.class.getName() + "::" + msg);
    }

    // Take this from TimeRenderer. A Renderer shouldn't be
    // initializing or updating a component. Ideally the state should
    // be current before this point is reached thereby giving the
    // application an opportunity to set the state in
    // INVOKE_APPLICATION_PHASE.
    //
    @Override
    public void encodeEnd(final FacesContext context) throws IOException {
        if (context == null) {
            throw new NullPointerException();
        }
        if (!isRendered()) {
            return;
        }

        DropDown hourMenu = getHourMenu();
        DropDown minuteMenu = getMinutesMenu();

        // If there is no submitted value, set the values of
        // the DropDowns to the actual value... If we have
        // a submitted value, the DropDown will remember it
        // so we do nothing in that case.
        // FIXME: have to round this to the nearest five minutes!
        if (getSubmittedValue() == null) {

            if (DEBUG) {
                log("No submitted value");
            }
            Object object = getValue();
            if (DEBUG) {
                log("Got the ClockTime");
            }
            ClockTime value = null;
            if (object != null && object instanceof ClockTime) {
                value = (ClockTime) object;
                if (DEBUG) {
                    log("\tValue is " + String.valueOf(value));
                }
            }
            if (value != null) {
                hourMenu.setValue(value.getHour());
            } else {
                hourMenu.setValue(-1);
            }

            if (value != null) {
                minuteMenu.setValue(value.getMinute());
            } else {
                minuteMenu.setValue(-1);
            }
        } else if (DEBUG) {
            log("Found submitted value");
        }
        Theme theme = ThemeUtilities.getTheme(context);

        String key = getHourTooltipKey();
        if (key != null) {
            hourMenu.setToolTip(theme.getMessage(key));
        }

        key = getMinutesTooltipKey();
        if (key != null) {
            minuteMenu.setToolTip(theme.getMessage(key));
        }

        String rendererType = getRendererType();
        if (rendererType != null) {
            getRenderer(context).encodeEnd(context, this);
        }
    }

    /**
     * Standard HTML attribute which determines whether the web application user
     * can change the the value of this component.
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
     * can change the the value of this component.
     *
     * @see #isDisabled()
     * @param newDisabled disabled
     */
    public void setDisabled(final boolean newDisabled) {
        this.disabled = newDisabled;
        this.disabledSet = true;
    }

    /**
     * If this attribute is set to true, the value of the component is rendered
     * as text, preceded by the label if one was defined.
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
     * as text, preceded by the label if one was defined.
     *
     * @see #isReadOnly()
     * @param newReadOnly readOnly
     */
    public void setReadOnly(final boolean newReadOnly) {
        this.readOnly = newReadOnly;
        this.readOnlySet = true;
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
     * A binding to a Time Zone instance to use for this Scheduler. If none is
     * specified, the Scheduler uses the default TimeZone from the Schedulers
     * locale.
     * @return {@code java.util.TimeZone}
     */
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
     * A binding to a Time Zone instance to use for this Scheduler. If none is
     * specified, the Scheduler uses the default TimeZone from the Schedulers
     * locale.
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
        this.disabled = ((Boolean) values[1]);
        this.disabledSet = ((Boolean) values[2]);
        this.readOnly = ((Boolean) values[3]);
        this.readOnlySet = ((Boolean) values[4]);
        this.style = (String) values[5];
        this.styleClass = (String) values[6];
        this.tabIndex = ((Integer) values[7]);
        this.tabIndexSet = ((Boolean) values[8]);
        this.timeZone = (java.util.TimeZone) values[9];
        this.visible = ((Boolean) values[10]);
        this.visibleSet = ((Boolean) values[11]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[12];
        values[0] = super.saveState(context);
        if (this.disabled) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.disabledSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.readOnly) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.readOnlySet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        values[5] = this.style;
        values[6] = this.styleClass;
        values[7] = this.tabIndex;
        if (this.tabIndexSet) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        values[9] = this.timeZone;
        if (this.visible) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[11] = Boolean.TRUE;
        } else {
            values[11] = Boolean.FALSE;
        }
        return values;
    }
}
