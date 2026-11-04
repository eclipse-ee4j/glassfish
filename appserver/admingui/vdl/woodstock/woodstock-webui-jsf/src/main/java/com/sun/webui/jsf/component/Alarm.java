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

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.util.Comparator;

/**
 * The Alarm component is used to display a theme-specific image to indicate the
 * condition of an object.
 */
@Component(type = "com.sun.webui.jsf.Alarm",
        family = "com.sun.webui.jsf.Alarm",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_alarm",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_alarm_props")
        //CHECKSTYLE:ON
public final class Alarm extends ImageComponent implements Comparator {

    /**
     * Down alarm severity.
     */
    public static final String SEVERITY_DOWN = "down";

    /**
     * Critical alarm severity.
     */
    public static final String SEVERITY_CRITICAL = "critical";

    /**
     * Major alarm severity.
     */
    public static final String SEVERITY_MAJOR = "major";

    /**
     * Minor alarm severity.
     */
    public static final String SEVERITY_MINOR = "minor";

    /**
     * OK alarm severity.
     */
    public static final String SEVERITY_OK = "ok";

    /**
     * Default severity, SEVERITY_OK.
     */
    public static final String DEFAULT_SEVERITY = SEVERITY_OK;

    /**
     * Down severity level.
     */
    private static final int SEVERITY_LEVEL_DOWN = 1;

    /**
     * Critical severity level.
     */
    private static final int SEVERITY_LEVEL_CRITICAL = 2;

    /**
     * Major severity level.
     */
    private static final int SEVERITY_LEVEL_MAJOR = 3;

    /**
     * Minor severity level.
     */
    private static final int SEVERITY_LEVEL_MINOR = 4;

    /**
     * OK severity level.
     */
    private static final int SEVERITY_LEVEL_OK = 5;

    /**
     * Alternative textual description of the image rendered by this component.
     * The alt text can be used by screen readers and in tool tips, and when
     * image display is turned off in the web browser.
     */
    @Property(name = "alt",
            displayName = "Alt Text",
            category = "Accessibility",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String alt = null;

    /**
     * Scripting code executed when a mouse click occurs over this component.
     */
    @Property(name = "onClick",
            displayName = "Click Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onClick = null;

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     */
    @Property(name = "onDblClick",
            displayName = "Double Click Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onDblClick = null;

    /**
     * Scripting code executed when the user presses down on a key while the
     * component has focus.
     */
    @Property(name = "onKeyDown",
            displayName = "Key Down Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onKeyDown = null;

    /**
     * Scripting code executed when the user presses and releases a key while
     * the component has focus.
     */
    @Property(name = "onKeyPress",
            displayName = "Key Press Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onKeyPress = null;

    /**
     * Scripting code executed when the user releases a key while the component
     * has focus.
     */
    @Property(name = "onKeyUp",
            displayName = "Key Up Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onKeyUp = null;

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseDown",
            displayName = "Mouse Down Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseDown = null;

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     */
    @Property(name = "onMouseMove",
            displayName = "Mouse Move Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseMove = null;

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     */
    @Property(name = "onMouseOut",
            displayName = "Mouse Out Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseOut = null;

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     */
    @Property(name = "onMouseOver",
            displayName = "Mouse In Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseOver = null;

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseUp",
            displayName = "Mouse Up Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseUp = null;

    /**
     * Specifies the severity of the alarm. Valid values are:
     * <ul>
     * <li>critical</li>
     * <li>major</li>
     * <li>minor</li>
     * <li>down</li>
     * <li>ok</li>
     * </ul>
     * The default value is "ok", which renders no alarm icon.
     */
    @Property(name = "severity",
            displayName = "Severity",
            category = "Appearance",
            isDefault = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.AlertTypesEditor")
            //CHECKSTYLE:ON
    private String severity = null;

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
     * The text description of the alarm.
     */
    @Property(name = "text",
            displayName = "Alarm Text")
    private String text = null;

    /**
     * Specifies where the text will be placed relative to the image. The valid
     * values currently are "right" or "left". By default, text is placed to the
     * right of the image.
     */
    @Property(name = "textPosition",
            displayName = "Text Position")
    private String textPosition = null;

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     */
    @Property(name = "toolTip",
            displayName = "Tool Tip",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String toolTip = null;

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
     * Visible set flag.
     */
    private boolean visibleSet = false;

    /**
     * Default constructor.
     */
    public Alarm() {
    }

    /**
     * Create an instance with the given severity.
     *
     * @param newSeverity severity
     */
    public Alarm(final String newSeverity) {
        setSeverity(newSeverity);
        setRendererType("com.sun.webui.jsf.Alarm");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Alarm";
    }

    /**
     * Compare the given objects for severity order.
     */
    @Override
    public int compare(final Object o1, final Object o2) {
        int s1 = getSeverityLevel((Alarm) o1);
        int s2 = getSeverityLevel((Alarm) o2);
        if (s1 > s2) {
            return -1;
        } else if (s1 == s2) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof Alarm) {
            return getSeverityLevel(this) == getSeverityLevel((Alarm) obj);
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash;
        if (this.severity != null) {
            hash = hash + this.severity.hashCode();
        }
        return hash;
    }

    /**
     * Helper method to get the severity level of an alarm.
     *
     * @param alarm alarm to get the severity level
     * @return int
     */
    private static int getSeverityLevel(final Alarm alarm) {
        int level = SEVERITY_LEVEL_OK;
        String alarmSeverity = alarm.getSeverity();
        if (alarmSeverity == null) {
            return level;
        }
        if (alarmSeverity.equals(SEVERITY_DOWN)) {
            level = SEVERITY_LEVEL_DOWN;
        } else if (alarmSeverity.equals(SEVERITY_CRITICAL)) {
            level = SEVERITY_LEVEL_CRITICAL;
        } else if (alarmSeverity.equals(SEVERITY_MAJOR)) {
            level = SEVERITY_LEVEL_MAJOR;
        } else if (alarmSeverity.equals(SEVERITY_MINOR)) {
            level = SEVERITY_LEVEL_MINOR;
        }
        return level;
    }

    // Note that this component is implemented differently than
    // other components. First its renderer extends ImageRenderer
    // and second, it does not support a call like "getImageComponent"
    // which would return an appropriately initialized image component
    // representing the severity of this alarm. Once that component is
    // obtained AlarmRenderer would just call the returned component's
    // renderer.
    @Override
    public void encodeBegin(final FacesContext context) throws IOException {
        if (context == null) {
            throw new NullPointerException();
        }
        if (!isRendered()) {
            return;
        }
        String rendererType = getRendererType();
        if (rendererType != null) {
            getRenderer(context).encodeBegin(context, this);
        }
    }

    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    // Hide value
    @Property(name = "value", isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    @Override
    public String getAlt() {
        if (this.alt != null) {
            return this.alt;
        }
        ValueExpression vb = getValueExpression("alt");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    @Override
    public void setAlt(final String newAlt) {
        this.alt = newAlt;
    }

    @Override
    public String getOnClick() {
        if (this.onClick != null) {
            return this.onClick;
        }
        ValueExpression vb = getValueExpression("onClick");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    @Override
    public void setOnClick(final String newOnClick) {
        this.onClick = newOnClick;
    }

    @Override
    public String getOnDblClick() {
        if (this.onDblClick != null) {
            return this.onDblClick;
        }
        ValueExpression vb = getValueExpression("onDblClick");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    @Override
    public void setOnDblClick(final String newOnDblClick) {
        this.onDblClick = newOnDblClick;
    }

    /**
     * Scripting code executed when the user presses down on a key while the
     * component has focus.
     * @return String
     */
    public String getOnKeyDown() {
        if (this.onKeyDown != null) {
            return this.onKeyDown;
        }
        ValueExpression vb = getValueExpression("onKeyDown");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user presses down on a key while the
     * component has focus.
     *
     * @param newOnKeyDown onKeyDown
     * @see #getOnKeyDown()
     */
    public void setOnKeyDown(final String newOnKeyDown) {
        this.onKeyDown = newOnKeyDown;
    }

    /**
     * Scripting code executed when the user presses and releases a key while
     * the component has focus.
     * @return String
     */
    public String getOnKeyPress() {
        if (this.onKeyPress != null) {
            return this.onKeyPress;
        }
        ValueExpression vb = getValueExpression("onKeyPress");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user presses and releases a key while
     * the component has focus.
     * @param newOnKeyPress onKeyPress
     * @see #getOnKeyPress()
     */
    public void setOnKeyPress(final String newOnKeyPress) {
        this.onKeyPress = newOnKeyPress;
    }

    /**
     * Scripting code executed when the user releases a key while the component
     * has focus.
     * @return String
     */
    public String getOnKeyUp() {
        if (this.onKeyUp != null) {
            return this.onKeyUp;
        }
        ValueExpression vb = getValueExpression("onKeyUp");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user releases a key while the component
     * has focus.
     *
     * @param newOnKeyUp onKeyUp
     * @see #getOnKeyUp()
     */
    public void setOnKeyUp(final String newOnKeyUp) {
        this.onKeyUp = newOnKeyUp;
    }

    @Override
    public String getOnMouseDown() {
        if (this.onMouseDown != null) {
            return this.onMouseDown;
        }
        ValueExpression vb = getValueExpression("onMouseDown");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    @Override
    public void setOnMouseDown(final String newOnMouseDown) {
        this.onMouseDown = newOnMouseDown;
    }

    @Override
    public String getOnMouseMove() {
        if (this.onMouseMove != null) {
            return this.onMouseMove;
        }
        ValueExpression vb = getValueExpression("onMouseMove");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    @Override
    public void setOnMouseMove(final String newOnMouseMove) {
        this.onMouseMove = newOnMouseMove;
    }

    @Override
    public String getOnMouseOut() {
        if (this.onMouseOut != null) {
            return this.onMouseOut;
        }
        ValueExpression vb = getValueExpression("onMouseOut");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    @Override
    public void setOnMouseOut(final String newOnMouseOut) {
        this.onMouseOut = newOnMouseOut;
    }

    @Override
    public String getOnMouseOver() {
        if (this.onMouseOver != null) {
            return this.onMouseOver;
        }
        ValueExpression vb = getValueExpression("onMouseOver");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    @Override
    public void setOnMouseOver(final String newOnMouseOver) {
        this.onMouseOver = newOnMouseOver;
    }

    @Override
    public String getOnMouseUp() {
        if (this.onMouseUp != null) {
            return this.onMouseUp;
        }
        ValueExpression vb = getValueExpression("onMouseUp");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    @Override
    public void setOnMouseUp(final String newOnMouseUp) {
        this.onMouseUp = newOnMouseUp;
    }

    /**
     * Specifies the severity of the alarm. Valid values are:
     * <ul>
     * <li>critical</li>
     * <li>major</li>
     * <li>minor</li>
     * <li>down</li>
     * <li>ok</li>
     * </ul>
     * The default value is "ok", which renders no alarm icon.
     * @return String
     */
    public String getSeverity() {
        if (this.severity != null) {
            return this.severity;
        }
        ValueExpression vb = getValueExpression("severity");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Specifies the severity of the alarm. Valid values are:
     * <ul>
     * <li>critical</li>
     * <li>major</li>
     * <li>minor</li>
     * <li>down</li>
     * <li>ok</li>
     * </ul>
     * The default value is "ok", which renders no alarm icon.
     *
     * @see #getSeverity()
     * @param newSeverity severity
     */
    public void setSeverity(final String newSeverity) {
        this.severity = newSeverity;
    }

    @Override
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

    @Override
    public void setStyle(final String newStyle) {
        this.style = newStyle;
    }

    @Override
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

    @Override
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

    /**
     * The text description of the alarm.
     * @return String
     */
    public String getText() {
        if (this.text != null) {
            return this.text;
        }
        ValueExpression vb = getValueExpression("text");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The text description of the alarm.
     * @param newText text
     * @see #getText()
     */
    public void setText(final String newText) {
        this.text = newText;
    }

    /**
     * Specifies where the text will be placed relative to the image. The valid
     * values currently are "right" or "left". By default, text is placed to the
     * right of the image.
     * @return String
     */
    public String getTextPosition() {
        if (this.textPosition != null) {
            return this.textPosition;
        }
        ValueExpression vb = getValueExpression("textPosition");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return "right";
    }

    /**
     * Specifies where the text will be placed relative to the image. The valid
     * values currently are "right" or "left". By default, text is placed to the
     * right of the image.
     * @param newTextPosition text position
     * @see #getTextPosition()
     */
    public void setTextPosition(final String newTextPosition) {
        this.textPosition = newTextPosition;
    }

    @Override
    public String getToolTip() {
        if (this.toolTip != null) {
            return this.toolTip;
        }
        ValueExpression vb = getValueExpression("toolTip");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    @Override
    public void setToolTip(final String newToolTip) {
        this.toolTip = newToolTip;
    }

    @Override
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

    @Override
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.alt = (String) values[1];
        this.onClick = (String) values[2];
        this.onDblClick = (String) values[3];
        this.onKeyDown = (String) values[4];
        this.onKeyPress = (String) values[5];
        this.onKeyUp = (String) values[6];
        this.onMouseDown = (String) values[7];
        this.onMouseMove = (String) values[8];
        this.onMouseOut = (String) values[9];
        this.onMouseOver = (String) values[10];
        this.onMouseUp = (String) values[11];
        this.severity = (String) values[12];
        this.style = (String) values[13];
        this.styleClass = (String) values[14];
        this.text = (String) values[15];
        this.textPosition = (String) values[16];
        this.toolTip = (String) values[17];
        this.visible = ((Boolean) values[18]);
        this.visibleSet = ((Boolean) values[19]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[20];
        values[0] = super.saveState(context);
        values[1] = this.alt;
        values[2] = this.onClick;
        values[3] = this.onDblClick;
        values[4] = this.onKeyDown;
        values[5] = this.onKeyPress;
        values[6] = this.onKeyUp;
        values[7] = this.onMouseDown;
        values[8] = this.onMouseMove;
        values[9] = this.onMouseOut;
        values[10] = this.onMouseOver;
        values[11] = this.onMouseUp;
        values[12] = this.severity;
        values[13] = this.style;
        values[14] = this.styleClass;
        values[15] = this.text;
        values[16] = this.textPosition;
        values[17] = this.toolTip;
        if (this.visible) {
            values[18] = Boolean.TRUE;
        } else {
            values[18] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[19] = Boolean.TRUE;
        } else {
            values[19] = Boolean.FALSE;
        }
        return values;
    }
}
