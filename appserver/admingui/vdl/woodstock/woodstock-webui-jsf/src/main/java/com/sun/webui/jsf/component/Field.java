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
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.LogUtil;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * Represents an input field whose content will be included when the surrounding
 * form is submitted.
 */
@Component(type = "com.sun.webui.jsf.Field",
        family = "com.sun.webui.jsf.Field",
        displayName = "Field", isTag = false,
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_field",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_field_props")
        //CHECKSTYLE:ON
public class Field extends HiddenField
        implements ComplexComponent, NamingContainer {

    /**
     * Read-only id.
     */
    public static final String READONLY_ID = "_readOnly";

    /**
     * Label id.
     */
    public static final String LABEL_ID = "_label";

    /**
     * Input id.
     */
    public static final String INPUT_ID = "_field";

    /**
     * Read-only facet.
     */
    public static final String READONLY_FACET = "readOnly";

    /**
     * Label facet.
     */
    public static final String LABEL_FACET = "label";

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Number of character columns used to render this field. The default is 20.
     */
    @Property(name = "columns",
            displayName = "Columns",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int columns = Integer.MIN_VALUE;

    /**
     * columnsSet flag.
     */
    private boolean columnsSet;

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
     * disabled set flag.
     */
    private boolean disabledSet;

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
     * The maximum number of characters that can be entered for this field.
     */
    @Property(name = "maxLength",
            displayName = "Maximum Length",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int maxLength = Integer.MIN_VALUE;

    /**
     * maxLength set flag.
     */
    private boolean maxLengthSet;

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
     * Scripting code executed when a mouse click occurs over this
     * component.
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
     * Flag indicating that modification of this component by the user is not
     * currently permitted, but that it will be included when the form is
     * submitted.
     */
    @Property(name = "readOnly",
            displayName = "Read Only",
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
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
            //CHECKSTYLE:ON
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
     * Flag indicating that any leading and trailing blanks will be trimmed
     * prior to conversion to the destination data type. Default value is true.
     */
    @Property(name = "trim",
            displayName = "Trim",
            category = "Behavior")
    private boolean trim;

    /**
     * trim set flag.
     */
    private boolean trimSet;

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page.
     */
    @Property(name = "visible",
            displayName = "Visible",
            category = "Behavior")
    private boolean visible;

    /**
     * visible set flag.
     */
    private boolean visibleSet;

    /**
     * Creates a new instance of FieldBase.
     */
    public Field() {
        super();
        setRendererType("com.sun.webui.jsf.Field");
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.Field"}.
     * @return String.
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Field";
    }

    /**
     * Flag indicating that an input value for this field is mandatory, and
     * failure to provide one will trigger a validation error.
     * This implementation invokes {@code super.isRequired}
     * @return {@code boolean}
     */
    @Property(name = "required",
            isHidden = false,
            isAttribute = true,
            category = "Data")
    @Override
    public boolean isRequired() {
        return super.isRequired();
    }

    // Hide value
    /**
     * This implementation invokes {@code super.getValue}.
     * @return Object
     */
    @Property(name = "value",
            isHidden = true,
            isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * Literal value to be rendered in this input field. If this property is
     * specified by a value binding expression, the corresponding value will be
     * updated if validation succeeds.
     *
     * @return Object
     */
    @Property(name = "text",
            displayName = "Text",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
    //CHECKSTYLE:ON
    @Override
    public Object getText() {
        return getValue();
    }

    /**
     * Return a component that implements a label for this {@code Field}. If a
     * facet named {@code label} is found that component is returned. If a facet
     * is not found a {@code Label} component instance is created and returned
     * with the id {@code getId() + "_label"}. The {@code Label} instance is
     * initialized with the following values
     *
     * <ul>
     * <li>{@code getLabelLevel()}</li>
     * <li>{@code style} parameter</li>
     * <li>{@code getLabel()}</li>
     * <li>{@code setLabeledComponent(this)}</li>
     * </ul>
     *
     * <p>
     * If a facet is not defined then the returned {@code Label} component is
     * created every time this method is called.
     * </p>
     *
     * @param context faces context
     * @param labelStyle style
     * @return label facet or a Label instance
     */
    public UIComponent getLabelComponent(final FacesContext context,
            final String labelStyle) {

        if (DEBUG) {
            log("getLabelComponent()");
        }

        // Check if the page author has defined a label facet
        UIComponent labelComponent = getFacet(LABEL_FACET);
        if (labelComponent != null) {
            if (DEBUG) {
                log("\tFound facet.");
            }
            return labelComponent;
        }

        // If the page author has not defined a label facet,
        // Create one every time.
        String fieldLabel = getLabel();
        labelComponent = createLabel(fieldLabel, labelStyle, context);
        return labelComponent;
    }

    /**
     * Return a component that implements a read only version of of this
     * {@code Field}. If a facet named {@code readOnly} is found that component
     * is returned. If a facet is not found a {@code StaticText} component
     * instance is created and returned with the id
     * {@code getId() + "_alertImage"}. The {@code StaticText} instance is
     * initialized with the component's value as a {@code String}.
     * <p>
     * If a facet is not defined then the returned {@code StaticText} component
     * is created every time this method is called.
     * </p>
     *
     * @param context faces context
     * @return alertImage facet or an Icon instance
     */
    public UIComponent getReadOnlyComponent(final FacesContext context) {

        if (DEBUG) {
            log("getReadOnlyComponent()");
        }

        // Check if the page author has defined a label facet
        UIComponent textComponent = getFacet(READONLY_FACET);
        if (textComponent != null) {
            if (DEBUG) {
                log("\tFound facet.");
            }
            return textComponent;
        }

        // If the page author has not defined a readOnly facet,
        // create a static text component
        textComponent = createText(getReadOnlyValueString(context));
        return textComponent;
    }

    /**
     * Create a Label component every time unless labelString is null or the
     * empty string.
     *
     * @param labelString label
     * @param labelStyle style
     * @param context faces context
     * @return UIComponent
     */
    private UIComponent createLabel(final String labelString,
            final String labelStyle, final FacesContext context) {

        if (DEBUG) {
            log("createLabel()");
        }

        // If we find a label, define a component and add it as a
        // private facet
        //
        // We need to allow an empty string label since this
        // could mean that there is value binding and a
        // message bundle hasn't loaded yet, but there
        // is a value binding since the jakarta.el never returns
        // null for a String binding.
        if (labelString == null /*|| labelString.length() < 1*/) {
            if (DEBUG) {
                log("\tNo label");
            }
            // Remove any previously created one.
            ComponentUtilities.removePrivateFacet(this, LABEL_FACET);
            return null;
        }

        Label fieldLabel = (Label) ComponentUtilities
                .getPrivateFacet(this, LABEL_FACET, true);
        if (fieldLabel == null) {
            fieldLabel = new Label();
            fieldLabel.setId(ComponentUtilities
                    .createPrivateFacetId(this, LABEL_FACET));
            ComponentUtilities.putPrivateFacet(this, LABEL_FACET, fieldLabel);
        }
        fieldLabel.setLabelLevel(getLabelLevel());
        fieldLabel.setStyleClass(labelStyle);
        fieldLabel.setText(labelString);
        if (!isReadOnly()) {
            fieldLabel.setFor(getClientId(context));
        }
        return fieldLabel;
    }

    /**
     * Create a StaticText component every time and do not save it in the facet
     * map.
     * @param content text content
     * @return UIComponent
     */
    private UIComponent createText(final String content) {

        if (DEBUG) {
            log("createText()");
        }

        StaticText text = new StaticText();

        // If we find a label, define a component and add it to the
        // children, unless it has been added in a previous cycle
        // (the component is being redisplayed).
        if (content == null || content.length() < 1) {
            // TODO - maybe print a default?
            text.setText("");
        } else {
            text.setText(content);
        }
        text.setId(
                ComponentUtilities.createPrivateFacetId(this, READONLY_FACET));
        text.setParent(this);

        return text;
    }

    /**
     * Log an error - only used during development time.
     *
     * @param msg message to log
     */
    @Override
    protected void log(final String msg) {
        LogUtil.finest(Field.class.getName() + "::" + msg);
    }

    /**
     * Returns the absolute ID of an HTML element suitable for use as the value
     * of an HTML LABEL element's {@code for} attribute. If the
     * {@code ComplexComponent} has sub-components, and one of the
     * sub-components is the target of a label, if that sub-component is a
     * {@code ComplexComponent}, then {@code getLabeledElementId} must called on
     * the sub-component and the value returned. The value returned by this
     * method call may or may not resolve to a component instance.
     * <p>
     * This implementation returns {@code null} if {@code isReadOnly} returns
     * true.
     * </p>
     *
     * @param context The FacesContext used for the request
     * @return An absolute id suitable for the value of an HTML LABEL element's
     * {@code for} attribute.
     */
    @Override
    public String getLabeledElementId(final FacesContext context) {

        // If this component has a label either as a facet or
        // an attribute, return the id of the input element
        // that will have the "INPUT_ID" suffix. IF there is no
        // label, then the input element id will be the component's
        // client id.
        //
        // If it is read only then return null
        if (isReadOnly()) {
            return null;
        }

        // To ensure we get the right answer call getLabelComponent.
        // This checks for a developer facet or the private label facet.
        // It also checks the label attribute. This is better than
        // relying on "getLabeledComponent" having been called
        // like this method used to do.
        String clntId = this.getClientId(context);
        UIComponent labelComp = getLabelComponent(context, null);
        if (labelComp == null) {
            return clntId;
        } else {
            return clntId.concat(Field.INPUT_ID);
        }
    }

    /**
     * Returns the id of an HTML element suitable to receive the focus. If the
     * {@code ComplexComponent} has sub-components, and one of the
     * sub-components is to reveive the focus, if that sub-component is a
     * {@code ComplexComponent}, then {@code getFocusElementId} must called on
     * the sub-component and the value returned. The value returned by this
     * method call may or may not resolve to a component instance.
     * <p>
     * This implementation returns the value {@code getLabeledElementId}
     * </p>
     *
     * @param context The FacesContext used for the request
     */
    @Override
    public String getFocusElementId(final FacesContext context) {
        return getLabeledElementId(context);
    }

    /**
     * This implementaiton returns the DOM ID of the HTML element
     * which should receive focus when the component receives focus, and to
     * which a component label should apply. Usually, this is the first element
     * that accepts input.
     *
     * @param context The FacesContext for the request
     * @return The client id, also the JavaScript element id
     *
     * @deprecated
     * @see #getLabeledElementId
     * @see #getFocusElementId
     */
    @Override
    public String getPrimaryElementID(final FacesContext context) {
        // In case callers can't handle null when this component
        // is read only. don't return getLabeledElementId here.
        String clntId = this.getClientId(context);
        UIComponent labelComp = getLabelComponent(context, null);
        if (labelComp == null) {
            return clntId;
        } else {
            return clntId.concat(Field.INPUT_ID);
        }
    }

    /**
     * Return the {@code ValueExpression} stored for the specified name (if
     * any), respecting any property aliases.
     * This implementation aliases {@code "text"} with {@code "value"}
     * and delegates the call to {@code UIInput.getValueExpression}.
     *
     * @param name Name of value binding expression to retrieve
     * @return ValueExpression
     */
    @Override
    public ValueExpression getValueExpression(final String name) {
        if (name.equals("text")) {
            return super.getValueExpression("value");
        }
        return super.getValueExpression(name);
    }

    /**
     * Set the {@code ValueExpression} stored for the specified name (if any),
     * respecting any property aliases.
     *
     * @param name Name of value binding to set
     * @param binding ValueExpression to set, or null to remove
     */
    @Override
    public void setValueExpression(final String name,
            final ValueExpression binding) {

        if (name.equals("text")) {
            super.setValueExpression("value", binding);
            return;
        }
        super.setValueExpression(name, binding);
    }

    /**
     * Number of character columns used to render this field. The default is
     * 20.
     * @return int
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public int getColumns() {
        int cols = 20;
        if (this.columnsSet) {
            cols = this.columns;
        } else {
            ValueExpression vb = getValueExpression("columns");
            if (vb != null) {
                Object result = vb.getValue(getFacesContext().getELContext());
                if (result == null) {
                    cols = Integer.MIN_VALUE;
                } else {
                    cols = ((Integer) result);
                }
            }
        }
        if (cols < 1) {
            cols = 20;
            setColumns(20);
        }
        return cols;
    }

    /**
     * Number of character columns used to render this field. The default is
     * 20.
     *
     * @see #getColumns()
     * @param newColumns columns
     */
    public void setColumns(final int newColumns) {
        this.columns = newColumns;
        this.columnsSet = true;
    }

    /**
     * Flag indicating that the user is not permitted to activate this
     * component, and that the component's value will not be submitted with the
     * form.
     * This implementation evaluates the expression for {@code "disabled"}.
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
     * @see #isDisabled()
     * @param newDisabled disabled
     */
    @Override
    public void setDisabled(final boolean newDisabled) {
        this.disabled = newDisabled;
        this.disabledSet = true;
    }

    /**
     * If set, a label is rendered adjacent to the component with the value of
     * this attribute as the label text.
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
     * @see #getLabel()
     * @param newLabel label
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
    public int getLabelLevel() {
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
     * attribute has been set. Valid values are 1 (largest), 2 and 3 (smallest).
     * The default value is 2.
     *
     * @see #getLabelLevel()
     * @param newLabelLevel labelLevel
     */
    public void setLabelLevel(final int newLabelLevel) {
        this.labelLevel = newLabelLevel;
        this.labelLevelSet = true;
    }

    /**
     * The maximum number of characters that can be entered for this field.
     * @return int
     */
    public int getMaxLength() {
        if (this.maxLengthSet) {
            return this.maxLength;
        }
        ValueExpression vb = getValueExpression("maxLength");
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
     * The maximum number of characters that can be entered for this field.
     *
     * @see #getMaxLength()
     * @param newMaxLength maxLength
     */
    public void setMaxLength(final int newMaxLength) {
        this.maxLength = newMaxLength;
        this.maxLengthSet = true;
    }

    /**
     * Scripting code executed when this element loses focus.
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
     * Scripting code executed when the element value of this component is
     * changed.
     * @return String
     */
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
     * @see #getOnChange()
     * @param newOnChange onChange
     */
    public void setOnChange(final String newOnChange) {
        this.onChange = newOnChange;
    }

    /**
     * Scripting code executed when a mouse click occurs over this
     * component.
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
     * Scripting code executed when a mouse click occurs over this
     * component.
     *
     * @see #getOnClick()
     * @param newOnClick onClick
     */
    public void setOnClick(final String newOnClick) {
        this.onClick = newOnClick;
    }

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
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
     * @see #getOnDblClick()
     * @param newOnDblClick onDblClick
     */
    public void setOnDblClick(final String newOnDblClick) {
        this.onDblClick = newOnDblClick;
    }

    /**
     * Scripting code executed when this component receives focus. An element
     * receives focus when the user selects the element by pressing the tab key
     * or clicking the mouse.
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
     * @see #getOnFocus()
     * @param newOnFocus onFocus
     */
    public void setOnFocus(final String newOnFocus) {
        this.onFocus = newOnFocus;
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
     * @see #getOnKeyDown()
     * @param newOnKeyDown onKeyDown
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
     *
     * @see #getOnKeyPress()
     * @param newOnKeyPress onKeyPress
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
     * @see #getOnKeyUp()
     * @param newOnKeyUp onKeyUp
     */
    public void setOnKeyUp(final String newOnKeyUp) {
        this.onKeyUp = newOnKeyUp;
    }

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
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
     * @see #getOnMouseDown()
     * @param newOnMouseDown onMouseDown
     */
    public void setOnMouseDown(final String newOnMouseDown) {
        this.onMouseDown = newOnMouseDown;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
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
     * @see #getOnMouseMove()
     * @param newOnMouseMove onMouseMove
     */
    public void setOnMouseMove(final String newOnMouseMove) {
        this.onMouseMove = newOnMouseMove;
    }

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
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
     * @see #getOnMouseOut()
     * @param newOnMouseOut onMouseOut
     */
    public void setOnMouseOut(final String newOnMouseOut) {
        this.onMouseOut = newOnMouseOut;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
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
     * @param newOnMouseOver onMouseOver
     *
     * @see #getOnMouseOver()
     */
    public void setOnMouseOver(final String newOnMouseOver) {
        this.onMouseOver = newOnMouseOver;
    }

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
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
     * @see #getOnMouseUp()
     * @param newOnMouseUp onMouseUp
     */
    public void setOnMouseUp(final String newOnMouseUp) {
        this.onMouseUp = newOnMouseUp;
    }

    /**
     * Scripting code executed when some text in this component value is
     * selected.
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
     * @see #getOnSelect()
     * @param newOnSelect onSelect
     */
    public void setOnSelect(final String newOnSelect) {
        this.onSelect = newOnSelect;
    }

    /**
     * Flag indicating that modification of this component by the user is not
     * currently permitted, but that it will be included when the form is
     * submitted.
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
     * Flag indicating that modification of this component by the user is not
     * currently permitted, but that it will be included when the form is
     * submitted.
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
     * Literal value to be rendered in this input field. If this property is
     * specified by a value binding expression, the corresponding value will be
     * updated if validation succeeds.
     * This implementation invokes {@code setValue(newText)}.
     *
     * @see #getText()
     * @param newText text
     */
    @Override
    public void setText(final Object newText) {
        setValue(newText);
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
     * @see #getToolTip()
     * @param newToolTip tool tip
     */
    public void setToolTip(final String newToolTip) {
        this.toolTip = newToolTip;
    }

    /**
     * Flag indicating that any leading and trailing blanks will be trimmed
     * prior to conversion to the destination data type. Default value is
     * true.
     * @return {@code boolean}
     */
    public boolean isTrim() {
        if (this.trimSet) {
            return this.trim;
        }
        ValueExpression vb = getValueExpression("trim");
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
     * Flag indicating that any leading and trailing blanks will be trimmed
     * prior to conversion to the destination data type. Default value is
     * true.
     *
     * @see #isTrim()
     * @param newTrim trim
     */
    public void setTrim(final boolean newTrim) {
        this.trim = newTrim;
        this.trimSet = true;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page.
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
     * viewable by the user in the rendered HTML page.
     *
     * @see #isVisible()
     * @param newVisible visible
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
        this.columns = ((Integer) values[1]);
        this.columnsSet = ((Boolean) values[2]);
        this.disabled = ((Boolean) values[3]);
        this.disabledSet = ((Boolean) values[4]);
        this.label = (String) values[5];
        this.labelLevel = ((Integer) values[6]);
        this.labelLevelSet = ((Boolean) values[7]);
        this.maxLength = ((Integer) values[8]);
        this.maxLengthSet = ((Boolean) values[9]);
        this.onBlur = (String) values[10];
        this.onChange = (String) values[11];
        this.onClick = (String) values[12];
        this.onDblClick = (String) values[13];
        this.onFocus = (String) values[14];
        this.onKeyDown = (String) values[15];
        this.onKeyPress = (String) values[16];
        this.onKeyUp = (String) values[17];
        this.onMouseDown = (String) values[18];
        this.onMouseMove = (String) values[19];
        this.onMouseOut = (String) values[20];
        this.onMouseOver = (String) values[21];
        this.onMouseUp = (String) values[22];
        this.onSelect = (String) values[23];
        this.readOnly = ((Boolean) values[24]);
        this.readOnlySet = ((Boolean) values[25]);
        this.style = (String) values[26];
        this.styleClass = (String) values[27];
        this.tabIndex = ((Integer) values[28]);
        this.tabIndexSet = ((Boolean) values[29]);
        this.toolTip = (String) values[30];
        this.trim = ((Boolean) values[31]);
        this.trimSet = ((Boolean) values[32]);
        this.visible = ((Boolean) values[33]);
        this.visibleSet = ((Boolean) values[34]);
    }

    /**
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[35];
        values[0] = super.saveState(context);
        values[1] = this.columns;
        if (this.columnsSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.disabled) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.disabledSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        values[5] = this.label;
        values[6] = this.labelLevel;
        if (this.labelLevelSet) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        values[8] = this.maxLength;
        if (this.maxLengthSet) {
            values[9] = Boolean.TRUE;
        } else {
            values[9] = Boolean.FALSE;
        }
        values[10] = this.onBlur;
        values[11] = this.onChange;
        values[12] = this.onClick;
        values[13] = this.onDblClick;
        values[14] = this.onFocus;
        values[15] = this.onKeyDown;
        values[16] = this.onKeyPress;
        values[17] = this.onKeyUp;
        values[18] = this.onMouseDown;
        values[19] = this.onMouseMove;
        values[20] = this.onMouseOut;
        values[21] = this.onMouseOver;
        values[22] = this.onMouseUp;
        values[23] = this.onSelect;
        if (this.readOnly) {
            values[24] = Boolean.TRUE;
        } else {
            values[24] = Boolean.FALSE;
        }
        if (this.readOnlySet) {
            values[25] = Boolean.TRUE;
        } else {
            values[25] = Boolean.FALSE;
        }
        values[26] = this.style;
        values[27] = this.styleClass;
        values[28] = this.tabIndex;
        if (this.tabIndexSet) {
            values[29] = Boolean.TRUE;
        } else {
            values[29] = Boolean.FALSE;
        }
        values[30] = this.toolTip;
        if (this.trim) {
            values[31] = Boolean.TRUE;
        } else {
            values[31] = Boolean.FALSE;
        }
        if (this.trimSet) {
            values[32] = Boolean.TRUE;
        } else {
            values[32] = Boolean.FALSE;
        }
        if (this.visible) {
            values[33] = Boolean.TRUE;
        } else {
            values[33] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[34] = Boolean.TRUE;
        } else {
            values[34] = Boolean.FALSE;
        }
        return values;
    }
}
