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
import com.sun.webui.jsf.model.OptionTitle;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.MessageUtil;
import com.sun.webui.jsf.util.ValueType;
import com.sun.webui.jsf.util.ValueTypeEvaluator;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.ConverterException;

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * Base component for UI components that allow the user to make a selection from
 * a set of options.
 */
@Component(type = "com.sun.webui.jsf.Selector",
        family = "com.sun.webui.jsf.Selector",
        displayName = "Selector",
        isTag = false,
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_selector",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_selector_props")
        //CHECKSTYLE:ON
public class Selector extends WebuiInput implements SelectorManager {

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Read only separator string.
     */
    private static final String READ_ONLY_SEPARATOR = ", ";

    /**
     * Multiple flag.
     */
    private boolean multiple;

    /**
     * Holds the ValueType of this component.
     */
    private ValueTypeEvaluator valueTypeEvaluator;

    /**
     * If set, a label is rendered adjacent to the component with the value of
     * this attribute as the label text.
     */
    @Property(name = "label",
            displayName = "Label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String label;

    /**
     * Sets the style level for the generated label, provided the label
     * attribute has been set. Valid values are 1 (largest), 2 and 3 (smallest).
     * The default value is 2.
     */
    @Property(name = "labelLevel",
            displayName = "Label Level",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.LabelLevelsEditor")
            //CHECKSTYLE:ON
    private int labelLevel = Integer.MIN_VALUE;

    /**
     * labelLevel set flag.
     */
    private boolean labelLevelSet;

    /**
     * Scripting code executed when this element loses focus.
     */
    @Property(name = "onBlur",
            displayName = "Blur Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onBlur;

    /**
     * Scripting code executed when the element value of this component is
     * changed.
     */
    @Property(name = "onChange",
            displayName = "Value Change Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onChange;

    /**
     * Specifies the options that the web application user can choose from. The
     * value must be one of an array, Map or Collection whose members are all
     * subclasses of{@code com.sun.webui.jsf.model.Option}.
     */
    @Property(name = "items",
            displayName = "Items",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor")
            //CHECKSTYLE:ON
    private Object items;

    /**
     * Flag indicating that the user is not permitted to activate this
     * component, and that the component's value will not be submitted with the
     * form.
     */
    @Property(name = "disabled",
            displayName = "Disabled",
            category = "Behavior")
    private boolean disabled;

    /**
     * Disabled set flag.
     */
    private boolean disabledSet;

    /**
     * Scripting code executed when a mouse click occurs over this component.
     */
    @Property(name = "onClick",
            displayName = "Click Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onClick;

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
    private String onDblClick;

    /**
     * Scripting code executed when this component receives focus. An element
     * receives focus when the user selects the element by pressing the tab key
     * or clicking the mouse.
     */
    @Property(name = "onFocus",
            displayName = "Focus Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onFocus;

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
    private String onKeyDown;

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
    private String onKeyPress;

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
    private String onKeyUp;

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
    private String onMouseDown;

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
    private String onMouseMove;

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
    private String onMouseOut;

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
    private String onMouseOver;

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
    private String onMouseUp;

    /**
     * Scripting code executed when some text in this component value is
     * selected.
     */
    @Property(name = "onSelect",
            displayName = "Text Selected Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onSelect;

    /**
     * If this attribute is set to true, the value of the component is rendered
     * as text, preceded by the label if one was defined.
     */
    @Property(name = "readOnly",
            displayName = "Read-only",
            category = "Behavior")
    private boolean readOnly;

    /**
     * readOnly set flag.
     */
    private boolean readOnlySet;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
    private String style;

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
    private String styleClass;

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
    private boolean tabIndexSet;

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
    private String toolTip;

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
    private boolean visible;

    /**
     * Visible set flag.
     */
    private boolean visibleSet;

    /**
     * Create a new instance.
     */
    public Selector() {
        valueTypeEvaluator = new ValueTypeEvaluator(this);
        setRendererType("com.sun.webui.jsf.Selector");
    }

    /**
     * Set the value type evaluator.
     * @param newValueTypeEvaluator value type evaluator
     */
    protected void setValueTypeEvaluator(
            final ValueTypeEvaluator newValueTypeEvaluator) {

        this.valueTypeEvaluator = newValueTypeEvaluator;
    }

    /**
     * Get the value type evaluator.
     * @return ValueTypeEvaluator
     */
    protected ValueTypeEvaluator getValueTypeEvaluator() {
        return valueTypeEvaluator;
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.Selector"}.
     * @return String
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Selector";
    }

    /**
     * This implementation returns {@code true}.
     *
     * @return {@code boolean}
     */
    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * This implementation uses
     * {@code ConversionUtilities.convertValueToObject}.
     * @param context faces context
     * @param submittedValue raw value
     * @return Object
     * @throws ConverterException if a conversion error occurs
     */
    @Override
    public Object getConvertedValue(final FacesContext context,
            final Object submittedValue) throws ConverterException {

        return getConvertedValue(this, valueTypeEvaluator, context,
                submittedValue);
    }

    /**
     * Retrieve the value of this component (the "selected" property) as an
     * object. This method is invoked by the JSF engine during the validation
     * phase. The JSF default behavior is for components to defer the conversion
     * and validation to the renderer, but for the Selector based components,
     * the renderers do not share as much functionality as the components do, so
     * it is more efficient to do it here.
     *
     * @param component The component whose value to convert
     * @param evaluator value type evaluator
     * @param context The FacesContext of the request
     * @param submittedValue The submitted value of the component
     * @return Object
     * @throws ConverterException if a conversion error occurs
     */
    private Object getConvertedValue(final UIComponent component,
            final ValueTypeEvaluator evaluator,
            final FacesContext context, final Object submittedValue)
            throws ConverterException {

        if (DEBUG) {
            log("getConvertedValue()", component);
        }

        if (!(submittedValue instanceof String[])) {
            Object[] args = {
                component.getClass().getName(),
            };
            String msg = MessageUtil.getMessage(
                    "com.sun.webui.jsf.resources.LogMessages",
                    "Selector.invalidSubmittedValue", args);
            throw new ConverterException(msg);
        }

        String[] rawValues = (String[]) submittedValue;

        // This should never happen
        if (rawValues.length == 1
                && OptionTitle.NONESELECTED.equals(rawValues[0])) {

            Object[] args = {OptionTitle.NONESELECTED};
            String msg = MessageUtil.getMessage(
                    "com.sun.webui.jsf.resources.LogMessages",
                    "Selector.invalidSubmittedValue", args);
            throw new ConverterException(msg);
        }

        // If there are no elements in rawValue nothing was submitted.
        // If null was rendered, return null
        if (rawValues.length == 0) {
            if (DEBUG) {
                log("\t no values submitted, we return null", component);
            }
            if (ConversionUtilities.renderedNull(component)) {
                return null;
            }
        }

        // Why does getAttributes.get("multiple") not work?
        if (((SelectorManager) component).isMultiple()) {
            if (DEBUG) {
                log("\tComponent accepts multiple values", component);
            }
            if (evaluator.getValueType() == ValueType.ARRAY) {
                if (DEBUG) {
                    log("\tComponent value is an array", component);
                }
                return ConversionUtilities
                        .convertValueToArray(component, rawValues, context);
            } else if (evaluator.getValueType() == ValueType.LIST) {
                // This case is not supported yet!
                if (DEBUG) {
                    log("\tComponent value is a list", component);
                }
                throw new jakarta.faces.FacesException(
                        "List is not a supported value.");
            } else {
                if (DEBUG) {
                    log("\tMultiple selection enabled for non-array value",
                            component);
                }
                Object[] params = {component.getClass().getName()};
                String msg = MessageUtil.getMessage(
                        "com.sun.webui.jsf.resources.LogMessages",
                        "Selector.multipleError", params);
                throw new ConverterException(msg);
            }
        }

        if (DEBUG) {
            log("\tComponent value is an object", component);
        }

        String cv;
        if (rawValues.length == 0) {
            cv = "";
        } else {
            cv = rawValues[0];
        }

        if (evaluator.getValueType() == ValueType.NONE) {
            if (DEBUG) {
                log("\t valuetype == none, return rawValue", component);
            }
            return cv;
        }

        if (DEBUG) {
            log("\t Convert the thing...", component);
        }
        return ConversionUtilities.convertValueToObject(component, cv, context);
    }

    /**
     * Return a string suitable for displaying the value in read only mode.The
     * default is to separate the list values with a comma.
     *
     * @param context The FacesContext
     * @return String
     * @throws jakarta.faces.FacesException If the list items cannot be processed
     */
    // AVK - instead of doing this here, I think we
    // should set the value to be displayed when we get the readOnly
    // child component. It would be a good idea to separate the listItems
    // processing for the renderer - where we have to reprocess the items
    // every time, from other times, when this may not be necessary.
    // I note that although this code has been refactored by Rick, my
    // original code already did this so the fault is wtih me.
    protected String getValueAsReadOnly(final FacesContext context) {

        // The comma format READ_ONLY_SEPARATOR should be part of the theme
        // and/or configurable by the application
        return getValueAsString(context, READ_ONLY_SEPARATOR, true);
    }

    /**
     * Get the value (the object representing the selection(s)) of this
     * component as a String. If the component allows multiple selections, the
     * strings corresponding to the individual options are separated by the
     * separator argument. If readOnly is true, leading and and trailing
     * separators are omitted. If readOnly is false the formatted String is
     * suitable for decoding by ListRendererBase.decode.
     *
     * @param context The FacesContext of the request
     * @param separator A String separator between the values
     * @param isReadOnly A read-only formatted String, no leading or trailing
     * separator string.
     * @return String
     */
    private String getValueAsString(final FacesContext context,
            final String separator, final boolean isReadOnly) {

        // Need to distinguish null value from an empty string
        // value. See the end of this method for empty string
        // value formatting
        Object value = getValue();
        if (value == null) {
            return new String();
        }

        if (valueTypeEvaluator.getValueType() == ValueType.NONE) {
            return new String();
        }

        if (valueTypeEvaluator.getValueType() == ValueType.INVALID) {
            return new String();
        }

        // Multiple selections
        //
        // The format should be the same as that returned
        // from the javascript which always has a leading
        // and terminating separator. And suitable for decoding
        // by ListRendererBase.decode
        if (valueTypeEvaluator.getValueType() == ValueType.LIST) {

            StringBuilder valueBuffer = new StringBuilder();

            java.util.List list = (java.util.List) value;
            Iterator valueIterator = ((java.util.List) value).iterator();
            String valueString;

            // Leading delimiter
            if (!isReadOnly && valueIterator.hasNext()) {
                valueBuffer.append(separator);
            }

            while (valueIterator.hasNext()) {
                valueString = ConversionUtilities
                        .convertValueToString(this, valueIterator.next());
                valueBuffer.append(valueString);
                // Add terminating delimiter
                //
                if (!isReadOnly || (isReadOnly && valueIterator.hasNext())) {
                    valueBuffer.append(separator);
                }
            }
            return valueBuffer.toString();
        }

        if (valueTypeEvaluator.getValueType() == ValueType.ARRAY) {

            StringBuilder valueBuffer = new StringBuilder();
            int length = Array.getLength(value);
            Object valueObject;
            String valueString;

            if (!isReadOnly && length != 0) {
                valueBuffer.append(separator);
            }
            for (int counter = 0; counter < length; ++counter) {
                valueObject = Array.get(value, counter);
                valueString
                        = ConversionUtilities.convertValueToString(this,
                                valueObject);
                valueBuffer.append(valueString);
                // Add terminating delimiter
                if (!isReadOnly || (isReadOnly && counter < length - 1)) {
                    valueBuffer.append(separator);
                }
            }
            return valueBuffer.toString();
        }

        // Empty string looks like '<sep><sep>' or if separator == "|"
        // it'll be "||"
        String cv = ConversionUtilities.convertValueToString(this, value);
        if (isReadOnly) {
            return cv;
        } else {
            StringBuilder sb = new StringBuilder();
            return sb.append(separator).append(cv).append(separator).toString();
        }
    }

    /**
     * This implementation evaluates the expression {@code "labelLevel"}
     * if not already done, and sets the label level, then returns the level
     * value.
     * @return int
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public int getLabelLevel() {
        int level = doGetLabelLevel();
        if (level < 1 || level > 3) {
            level = 2;
            setLabelLevel(level);
        }
        return level;
    }

    /**
     * This implementation returns the value of the {@code multiple attribute}.
     * @return {@code boolean}
     */
    @Override
    public boolean isMultiple() {
        return this.multiple;
    }

    /**
     * Setter for property multiple.
     *
     * @param newMultiple New value of property multiple.
     */
    public void setMultiple(final boolean newMultiple) {
        if (this.multiple != newMultiple) {
            valueTypeEvaluator.reset();
            this.multiple = newMultiple;
        }
    }

    /**
     * This implementation returns the instance class name.
     * @return String
     */
    @Override
    public String toString() {
        String string = this.getClass().getName();
        return string;
    }

    /**
     * Return {@code true} if the new value is different from the previous
     * value.
     *
     * This only implements a compareValues for value if it is an Array. If
     * value is not an Array, defer to super.compareValues. The assumption is
     * that the ordering of the elements between the previous value and the new
     * value is determined in the same manner.
     *
     * Another assumption is that the two object arguments are of the same type,
     * both arrays of both not arrays.
     *
     * @param previous old value of this component (if any)
     * @param value new value of this component (if any)
     * @return {@code boolean}
     */
    @Override
    protected boolean compareValues(final Object previous, final Object value) {

        // Let super take care of null cases
        if (previous == null || value == null) {
            return super.compareValues(previous, value);
        }
        if (value instanceof Object[]) {
            // If the lengths aren't equal return true
            //
            int length = Array.getLength(value);
            if (Array.getLength(previous) != length) {
                return true;
            }
            // Each element at index "i" in previous must be equal to the
            // elementa at index "i" in value.
            for (int i = 0; i < length; ++i) {

                Object newValue = Array.get(value, i);
                Object prevValue = Array.get(previous, i);

                // This is probably not necessary since
                // an Option's value cannot be null
                if (newValue == null) {
                    if (prevValue == null) {
                        continue;
                    } else {
                        return true;
                    }
                }
                if (prevValue == null) {
                    return true;
                }

                if (!prevValue.equals(newValue)) {
                    return true;
                }
            }
            return false;
        }
        return super.compareValues(previous, value);
    }

    /**
     * This implementation invokes {@code UIInput.setValue}.
     * @param selected object to mark selected
     */
    public void setSelected(final Object selected) {
        doSetSelected(selected);
        valueTypeEvaluator.reset();
    }

    /**
     * This implementation aliases {@code "selected"} with {@code "value"}
     * and delegates the call to {@code UIInput.getValueExpression}.
     * @param name property name
     * @return ValueExpression
     */
    @Override
    public ValueExpression getValueExpression(final String name) {
        if (name.equals("selected")) {
            return super.getValueExpression("value");
        }
        return super.getValueExpression(name);
    }

    /**
     * This implementation aliases {@code "selected"} with {@code "value"}
     * and delegates the call to {@code UIInput.setValueExpression}.
     * @param name property name
     * @param binding the value expression to set
     */
    @Override
    public void setValueExpression(final String name,
            final ValueExpression binding) {

        if (name.equals("selected")) {
            super.setValueExpression("value", binding);
            return;
        }
        super.setValueExpression(name, binding);
    }

    /**
     * This implementation evaluates the {@code "disabled"} value expression
     *  if not already done.
     * @return {@code boolean}
     */
    @Override
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
     * Flag indicating that the user is not permitted to activate this
     * component, and that the component's value will not be submitted with the
     * form.
     *
     * @param newDisabled disabled flag
     * @see #isDisabled()
     */
    public void setDisabled(final boolean newDisabled) {
        this.disabled = newDisabled;
        this.disabledSet = true;
    }

    /**
     * Specifies the options that the web application user can choose from.The
     * value must be one of an array, Map or Collection whose members are all
     * subclasses of{@code com.sun.webui.jsf.model.Option}.
     *
     * @return Object
     */
    public Object getItems() {
        if (this.items != null) {
            return this.items;
        }
        ValueExpression vb = getValueExpression("items");
        if (vb != null) {
            return (Object) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Specifies the options that the web application user can choose from. The
     * value must be one of an array, Map or Collection whose members are all
     * subclasses of {@code com.sun.webui.jsf.model.Option}.
     *
     * @param newItems new items
     * @see #getItems()
     */
    public void setItems(final Object newItems) {
        this.items = newItems;
    }

    /**
     * If set, a label is rendered adjacent to the component with the value of
     * this attribute as the label text.
     *
     * @return String
     */
    public String getLabel() {
        if (this.label != null) {
            return this.label;
        }
        ValueExpression vb = getValueExpression("label");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * If set, a label is rendered adjacent to the component with the value of
     * this attribute as the label text.
     *
     * @param newLabel label
     * @see #getLabel()
     */
    public void setLabel(final String newLabel) {
        this.label = newLabel;
    }

    /**
     * Sets the style level for the generated label, provided the label
     * attribute has been set. Valid values are 1 (largest), 2 and 3 (smallest).
     * The default value is 2.
     * @return int
     */
    private int doGetLabelLevel() {
        if (this.labelLevelSet) {
            return this.labelLevel;
        }
        ValueExpression vb = getValueExpression("labelLevel");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return 2;
    }

    /**
     * Sets the style level for the generated label, provided the label
     * attribute has been set.Valid values are 1 (largest), 2 and 3 (smallest).
     * The default value is 2.
     *
     * @param newLabelLevel level
     * @see #getLabelLevel()
     */
    public void setLabelLevel(final int newLabelLevel) {
        this.labelLevel = newLabelLevel;
        this.labelLevelSet = true;
    }

    /**
     * Scripting code executed when this element loses focus.
     *
     * @return String
     */
    public String getOnBlur() {
        if (this.onBlur != null) {
            return this.onBlur;
        }
        ValueExpression vb = getValueExpression("onBlur");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when this element loses focus.
     *
     * @see #getOnBlur()
     * @param newOnBlur onBlur
     */
    public void setOnBlur(final String newOnBlur) {
        this.onBlur = newOnBlur;
    }

    /**
     * This implementation evaluates the {@code "onChange"} expression if not
     * already done.
     * @return String
     */
    @Override
    public String getOnChange() {
        if (this.onChange != null) {
            return this.onChange;
        }
        ValueExpression vb = getValueExpression("onChange");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the element value of this component is
     * changed.
     *
     * @param newOnChange on change value
     * @see #getOnChange()
     */
    public void setOnChange(final String newOnChange) {
        this.onChange = newOnChange;
    }

    /**
     * Scripting code executed when a mouse click occurs over this component.
     *
     * @return String
     */
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

    /**
     * Scripting code executed when a mouse click occurs over this component.
     *
     * @param newOnClick onClick
     * @see #getOnClick()
     */
    public void setOnClick(final String newOnClick) {
        this.onClick = newOnClick;
    }

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     *
     * @return String
     */
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

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     *
     * @param newOnDblClick on double click
     * @see #getOnDblClick()
     */
    public void setOnDblClick(final String newOnDblClick) {
        this.onDblClick = newOnDblClick;
    }

    /**
     * Scripting code executed when this component receives focus. An element
     * receives focus when the user selects the element by pressing the tab key
     * or clicking the mouse.
     *
     * @return String
     */
    public String getOnFocus() {
        if (this.onFocus != null) {
            return this.onFocus;
        }
        ValueExpression vb = getValueExpression("onFocus");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when this component receives focus. An element
     * receives focus when the user selects the element by pressing the tab key
     * or clicking the mouse.
     *
     * @param newOnFocus onFocus
     * @see #getOnFocus()
     */
    public void setOnFocus(final String newOnFocus) {
        this.onFocus = newOnFocus;
    }

    /**
     * Scripting code executed when the user presses down on a key while the
     * component has focus.
     *
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
     *
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
     *
     * @param newOnKeyPress onKeyPress
     * @see #getOnKeyPress()
     */
    public void setOnKeyPress(final String newOnKeyPress) {
        this.onKeyPress = newOnKeyPress;
    }

    /**
     * Scripting code executed when the user releases a key while the component
     * has focus.
     *
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

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     *
     * @return String
     */
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

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     *
     * @param newOnMouseDown onMouseDown
     * @see #getOnMouseDown()
     */
    public void setOnMouseDown(final String newOnMouseDown) {
        this.onMouseDown = newOnMouseDown;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     *
     * @return String
     */
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

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     *
     * @param newOnMouseMove onMouseMove
     * @see #getOnMouseMove()
     */
    public void setOnMouseMove(final String newOnMouseMove) {
        this.onMouseMove = newOnMouseMove;
    }

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     *
     * @return String
     */
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

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     *
     * @param newOnMouseOut onMouseOut
     * @see #getOnMouseOut()
     */
    public void setOnMouseOut(final String newOnMouseOut) {
        this.onMouseOut = newOnMouseOut;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     *
     * @return String
     */
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

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     *
     * @param newOnMouseOver onMouseOver
     * @see #getOnMouseOver()
     */
    public void setOnMouseOver(final String newOnMouseOver) {
        this.onMouseOver = newOnMouseOver;
    }

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     *
     * @return String
     */
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

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     *
     * @param newOnMouseUp onMouseUp
     * @see #getOnMouseUp()
     */
    public void setOnMouseUp(final String newOnMouseUp) {
        this.onMouseUp = newOnMouseUp;
    }

    /**
     * Scripting code executed when some text in this component value is
     * selected.
     *
     * @return String
     */
    public String getOnSelect() {
        if (this.onSelect != null) {
            return this.onSelect;
        }
        ValueExpression vb = getValueExpression("onSelect");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when some text in this component value is
     * selected.
     *
     * @param newOnSelect onSelect
     * @see #getOnSelect()
     */
    public void setOnSelect(final String newOnSelect) {
        this.onSelect = newOnSelect;
    }

    /**
     * This implementation evaluates the {@code "readOnly"} expression if not
     * already done.
     * @return {@code boolean}
     */
    @Override
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
     * @param newReadOnly readOnly
     * @see #isReadOnly()
     */
    public void setReadOnly(final boolean newReadOnly) {
        this.readOnly = newReadOnly;
        this.readOnlySet = true;
    }

    /**
     * The object that represents the selections made from the available
     * options.If multiple selections are allowed, this must be bound to an
     * Object array, or an array of primitives.
     *
     * @return Object
     */
    @Property(name = "selected",
            displayName = "Selected",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor")
            //CHECKSTYLE:ON
    public Object getSelected() {
        return getValue();
    }

    /**
     * The object that represents the selections made from the available
     * options. If multiple selections are allowed, this must be bound to an
     * Object array, or an array of primitives.
     *
     * @param newSelected selected object
     * @see #getSelected()
     */
    private void doSetSelected(final Object newSelected) {
        setValue(newSelected);
    }

    /**
     * This implementation evaluates the {@code "style"} expression if not
     * already done.
     * @return String
     */
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

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @param newStyle style
     * @see #getStyle()
     */
    public void setStyle(final String newStyle) {
        this.style = newStyle;
    }

    /**
     * This implementation evaluates the {@code "styleClass"} expression if not
     * already done.
     * @return String
     */
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

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @param newStyleClass styleClass
     * @see #getStyleClass()
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
    @Override
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
     * @param newTabIndex tabIndex
     * @see #getTabIndex()
     */
    public void setTabIndex(final int newTabIndex) {
        this.tabIndex = newTabIndex;
        this.tabIndexSet = true;
    }

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     * @return String
     */
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

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     *
     * @param newToolTip tool-tip
     * @see #getToolTip()
     */
    public void setToolTip(final String newToolTip) {
        this.toolTip = newToolTip;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page.If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     *
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
     * @param newVisible visible
     * @see #isVisible()
     */
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    /**
     * This implementation restores the state of all properties.
     * @param context faces context
     * @param state state object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.disabled = ((Boolean) values[1]);
        this.disabledSet = ((Boolean) values[2]);
        this.items = (Object) values[3];
        this.label = (String) values[4];
        this.labelLevel = ((Integer) values[5]);
        this.labelLevelSet = ((Boolean) values[6]);
        this.onBlur = (String) values[7];
        this.onChange = (String) values[8];
        this.onClick = (String) values[9];
        this.onDblClick = (String) values[10];
        this.onFocus = (String) values[11];
        this.onKeyDown = (String) values[12];
        this.onKeyPress = (String) values[13];
        this.onKeyUp = (String) values[14];
        this.onMouseDown = (String) values[15];
        this.onMouseMove = (String) values[16];
        this.onMouseOut = (String) values[17];
        this.onMouseOver = (String) values[18];
        this.onMouseUp = (String) values[19];
        this.onSelect = (String) values[20];
        this.readOnly = ((Boolean) values[21]);
        this.readOnlySet = ((Boolean) values[22]);
        this.style = (String) values[23];
        this.styleClass = (String) values[24];
        this.tabIndex = ((Integer) values[25]);
        this.tabIndexSet = ((Boolean) values[26]);
        this.toolTip = (String) values[27];
        this.visible = ((Boolean) values[28]);
        this.visibleSet = ((Boolean) values[29]);
    }

    /**
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[30];
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
        values[3] = this.items;
        values[4] = this.label;
        values[5] = this.labelLevel;
        if (this.labelLevelSet) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        values[7] = this.onBlur;
        values[8] = this.onChange;
        values[9] = this.onClick;
        values[10] = this.onDblClick;
        values[11] = this.onFocus;
        values[12] = this.onKeyDown;
        values[13] = this.onKeyPress;
        values[14] = this.onKeyUp;
        values[15] = this.onMouseDown;
        values[16] = this.onMouseMove;
        values[17] = this.onMouseOut;
        values[18] = this.onMouseOver;
        values[19] = this.onMouseUp;
        values[20] = this.onSelect;
        if (this.readOnly) {
            values[21] = Boolean.TRUE;
        } else {
            values[21] = Boolean.FALSE;
        }
        if (this.readOnlySet) {
            values[22] = Boolean.TRUE;
        } else {
            values[22] = Boolean.FALSE;
        }
        values[23] = this.style;
        values[24] = this.styleClass;
        values[25] = this.tabIndex;
        if (this.tabIndexSet) {
            values[26] = Boolean.TRUE;
        } else {
            values[26] = Boolean.FALSE;
        }
        values[27] = this.toolTip;
        if (this.visible) {
            values[28] = Boolean.TRUE;
        } else {
            values[28] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[29] = Boolean.TRUE;
        } else {
            values[29] = Boolean.FALSE;
        }
        return values;
    }

    /**
     * Private method for development time error detecting.
     *
     * @param msg message to log
     * @param object object to derive the class of
     */
    private static void log(final String msg, final Object object) {
        LogUtil.finest(object.getClass().getName() + "::" + msg);
    }
}
