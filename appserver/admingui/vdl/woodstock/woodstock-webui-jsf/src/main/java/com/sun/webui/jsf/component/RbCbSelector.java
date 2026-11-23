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

import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.ConversionUtilities;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.render.Renderer;

import java.io.IOException;

/**
 * This renderer meta-data is not mapped one to one with a component. A renderer
 * of this name does exist as the super class of the RadioButton and Checkbox
 * renderers.
 */
public class RbCbSelector extends Selector implements NamingContainer {

    /**
     * Image facet name.
     */
    public static final String IMAGE_FACET = "image";

    /**
     * Label facet name.
     */
    public static final String LABEL_FACET = "label";

    /**
     * This is the default value for selectedValue. Because of the generation
     * and alias of "items" (Need to reconsider this inheritance) its not
     * possible to set up a default value. If selectedValue is not set, then
     * allow this component to behave as a {@code boolean} control. The
     * component is selected if both "selected" and "selectedValue" are "true".
     */
    private static final Boolean TRUE_SELECTED_VALUE = Boolean.TRUE;

    /**
     * A context relative path of an image to be displayed with the control. If
     * you want to be able to specify attributes for the image, specify an
     * {@code image} facet instead of the {@code imageURL}
     * attribute.
     */
    @Property(name = "imageURL",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.ImageUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String imageURL;

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
     * Identifies the control as participating as part of a group. The
     * {@code RadioButton} and {@code Checkbox} classes determine the behavior
     * of the group, that are assigned the same value to the {@code name}
     * property. The value of this property must be unique for components in the
     * group, within the scope of the {@code Form} parent component containing
     * the grouped components.
     */
    @Property(name = "name",
            displayName = "Group Name",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String name;

    /**
     * Default constructor.
     */
    public RbCbSelector() {
        super();
        setRendererType("com.sun.webui.jsf.RbCbSelectorRenderer");
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.RbCbSelector"}.
     * @return String
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.RbCbSelector";
    }

    /**
     * Implemented by subclasses in order to reflect the selection state of this
     * component id part of a group. This method is called if the component is
     * part of a group.
     * This implementation does nothing.
     *
     * @param context the context for this request.
     * @param groupName the value of the {@code name} property.
     */
    protected void addToRequestMap(final FacesContext context,
            final String groupName) {
    }

    /**
     * Encode the component.
     * <p>
     * If this component is part of a group, ensure that the initial state is
     * reflected in the request map by calling {@code addToRequestMap}.
     * </p>
     *
     * @param context the context for this request.
     * @throws IOException if an IO error occurs
     */
    // Implement this here to initialize the RequestMap ArrayList
    // of selected grouped checkboxes, so that initially selected
    // check boxes are available on the first render cycle
    //
    @Override
    public void encodeBegin(final FacesContext context) throws IOException {

        if (context == null) {
            throw new NullPointerException();
        }
        if (!isRendered()) {
            return;
        }

        // If the checkbox or radio button isn't valid, or
        // not in a group or not
        // selected, don't put it in the RequestMap.
        String groupName = getName();
        if (groupName == null || !isValid() || !isChecked()) {
            return;
        }
        addToRequestMap(context, groupName);
        String rendererType = getRendererType();
        if (rendererType != null) {
            getRenderer(context).encodeBegin(context, this);
        }
    }

    /**
     * Convert the {@code submittedValue} argument.
     * <p>
     * If there is a renderer for this component, its
     * {@code getConvertedValue()} method is called and the value returned by
     * that method is returned.
     * </p>
     * <p>
     * If there is no renderer, and {@code submittedValue} is not an instance of
     * {@code String[]} or {@code String} a {@code ConverterException} is
     * thrown.
     * </p>
     * <p>
     * The {@code submittedValue} indicates selected if it is
     * {@code String[1].length() != 0} or {@code String.length() != 0}.
     * </p>
     * <p>
     * If not selected and {@code getSelectedValue()} returns an instance of
     * {@code Boolean}, {@code Boolean.FALSE} is returned.
     * </p>
     * <p>
     * If not selected and it's not a {@code boolean} control then an unselected
     * value is returned appropriate for the type of the {@code selected}
     * property. If the type of the {@code selected} property evaluates to a
     * primitive type by virtue of a value binding the appropriate
     * {@code MIN_VALUE} constant is returned. For example if the type is
     * {@code int}, {@code new Integer(Integer.MIN_VALUE)} is returned. If the
     * type is not a primitive value {@code ""} is returned.
     * </p>
     * <p>
     * If the control is selected
     * {@code ConversionUtilities.convertValueToObject()} is called to convert
     * {@code submittedValue}.
     * </p>
     * <p>
     * If {@code ConversionUtilities.convertValueToObject()} returns
     * {@code submittedValue}, the value of the {@code getSelectedValue()}
     * property is returned, else the value returned by
     * {@code ConversionUtilities.convertValueToObject()} is returned.
     * </p>
     *
     * @param context the context of this request.
     * @param submittedValue the submitted String value of this component.
     */
    @Override
    public Object getConvertedValue(final FacesContext context,
            final Object submittedValue) throws ConverterException {

        // First defer to the renderer.
        Renderer renderer = getRenderer(context);
        if (renderer != null) {
            return renderer.getConvertedValue(context, this, submittedValue);
        }
        return getConvertedValue(context, this, submittedValue);
    }

    /**
     * Convert the {@code submittedValue} argument.
     * <p>
     * If {@code submittedValue} is not an instance of {@code String[]} or
     * {@code String} a {@code ConverterException} is thrown.
     * </p>
     * <p>
     * The {@code submittedValue} indicates selected if it is
     * {@code String[1].length() != 0} or {@code String.length() != 0}.
     * </p>
     * <p>
     * If not selected and {@code getSelectedValue()} returns an instance of
     * {@code Boolean}, {@code Boolean.FALSE} is returned.
     * </p>
     * <p>
     * If not selected and it's not a {@code boolean} control then an unselected
     * value is returned appropriate for the type of the {@code selected}
     * property. If the type of the {@code selected} property evaluates to a
     * primitive type by virtue of a value binding the appropriate
     * {@code MIN_VALUE} constant is returned. For example if the type is
     * {@code int}, {@code new Integer(Integer.MIN_VALUE)} is returned. If the
     * type is not a primitive value {@code ""} is returned.
     * </p>
     * <p>
     * If the control is selected
     * {@code ConversionUtilities.convertValueToObject()} is called to convert
     * {@code submittedValue}.
     * </p>
     * <p>
     * If {@code ConversionUtilities.convertValueToObject()} returns
     * {@code submittedValue}, the value of the {@code getSelectedValue()}
     * property is returned, else the value returned by
     * {@code ConversionUtilities.convertValueToObject()} is returned.
     * </p>
     *
     * @param context the context of this request.
     * @param component an RbCbSelector instance.
     * @param submittedValue the submitted String value of this component.
     * @return Object
     * @throws ConverterException if a conversion error occurs
     */
    public Object getConvertedValue(final FacesContext context,
            final RbCbSelector component, final Object submittedValue)
            throws ConverterException {

        // This would indicate minimally not selected
        if (submittedValue == null) {
            throw new ConverterException(
                    "The submitted value is null. "
                    + "The submitted value must be a String or String array.");
        }

        // Expect a String or String[]
        // Should be made to be just String.
        boolean isStringArray = submittedValue instanceof String[];
        boolean isString = submittedValue instanceof String;
        if (!(isStringArray || isString)) {
            throw new ConverterException(
                    "The submitted value must be a String or String array.");
        }

        String rawValue = null;
        if (isStringArray) {
            if (((String[]) submittedValue).length > 0) {
                rawValue = ((String[]) submittedValue)[0];
            }
        } else if (isString) {
            rawValue = (String) submittedValue;
        }

        // Need to determine if the submitted value is not checked
        // and unchanged. If it is unchecked, rawValue == null or
        // rawValue == "". Compare with the rendered value. If the
        // rendered value is "" or null, then the component is unchanged
        // and if the rendered value was not null, try and convert it.
        boolean unselected = rawValue == null || rawValue.length() == 0;

        // If the component was unselected then we need to know if it
        // was rendered unselected due to a value that was an empty
        // string or null. If it is was submitted as unselected
        // and rendered as unselected, we need the rendered value that
        // implied unselected, since it may not null, just different
        // than "selectedValue"
        Object newValue;
        Object selectedValue = getSelectedValue();
        if (unselected) {
            newValue = ConversionUtilities
                    .convertRenderedValue(context, rawValue, this);
            // Determine the unselected value for Boolean controls
            // if the converted value is null but the the component
            // value wasn't rendered as null.
            // For example if the control rendered as null, and is
            // still unselected, then we don't want to return FALSE
            // for a Boolean control, since it is unchanged.
            // But if it has changed and is unselected then return
            // the unselected value of FALSE.
            if (!ConversionUtilities.renderedNull(component)
                    && selectedValue instanceof Boolean
                    && newValue == null) {

                // return the complement of the selectedValue
                // Boolean value.
                if (((Boolean) selectedValue)) {
                    newValue = Boolean.FALSE;
                } else {
                    newValue = Boolean.TRUE;
                }
            }
            return getUnselectedValue(context, component, newValue);
        } else {
            newValue = ConversionUtilities
                    .convertValueToObject(component, rawValue, context);
            if (newValue == rawValue) {
                return selectedValue;
            }
            return newValue;
        }
    }

    /**
     * Get the unselected value.
     * @param context faces context
     * @param component UI component
     * @param noValue no value
     * @return Object
     */
    private Object getUnselectedValue(final FacesContext context,
            final UIComponent component, final Object noValue) {

        // Determine the type of the component's value object
        ValueExpression valueExpr
                = component.getValueExpression("value");

        // If there's no value binding we don't care
        // since the local value is an object and can support null or ""
        if (valueExpr == null) {
            return noValue;
        }
        // We have found a valuebinding.
        Class clazz = valueExpr.getType(context.getELContext());

        // Null class
        if (clazz == null) {
            return noValue;
        }
        // Pass noValue for use in primitive boolean case.
        // If the "selectedValue" was Boolean.FALSE, unselected
        // will be Boolean.TRUE.
        if (clazz.isPrimitive()) {
            return getPrimitiveUnselectedValue(clazz, noValue);
        }
        // bail out
        return noValue;
    }

    /**
     * Get the primitive unselected value.
     * @param clazz the class
     * @param booleanUnselectedValue unselected flag
     * @return Object
     */
    private Object getPrimitiveUnselectedValue(final Class clazz,
            final Object booleanUnselectedValue) {

        // it MUST be at least one of these
        if (clazz.equals(Boolean.TYPE)) {
            return booleanUnselectedValue;
        } else if (clazz.equals(Byte.TYPE)) {
            return new Integer(Byte.MIN_VALUE);
        } else if (clazz.equals(Double.TYPE)) {
            return Double.MIN_VALUE;
        } else if (clazz.equals(Float.TYPE)) {
            return Float.MIN_VALUE;
        } else if (clazz.equals(Integer.TYPE)) {
            return Integer.MIN_VALUE;
        } else if (clazz.equals(Character.TYPE)) {
            return Character.MIN_VALUE;
        } else if (clazz.equals(Short.TYPE)) {
            return Short.MIN_VALUE;
        } else {
            // if (clazz.equals(Long.TYPE))
            return Long.MIN_VALUE;
        }
    }

    /**
     * The value of the component when it is selected. The value of this
     * property is assigned to the {@code selected} property when the component
     * is selected. The component is selected when the {@code selected} property
     * is equal to this value. This attribute can be bound to a {@code String},
     * or {@code
     * Object} value. If this property is not assigned a value, the component
     * behaves as a {@code boolean} component. A {@code boolean} component is
     * selected when the {@code selected} property is equal to a true
     * {@code Boolean} instance.<br>
     * If a {@code boolean} component is not selected, the {@code selected}
     * property value is a false {@code Boolean} instance.
     * @return Object
     */
    @Property(name = "selectedValue",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    public Object getSelectedValue() {
        Object sv = getItems();
        if (sv == null) {
            return TRUE_SELECTED_VALUE;
        }
        return sv;
    }

    // Hack to overcome introspection of "isSelected"
    /**
     * Return {@code true} if the control is checked. A control is checked
     * when the {@code selectedValue} property is equal to the
     * {@code selected} property.
     * @return {@code boolean}
     */
    public boolean isChecked() {
        Object selectedValue = getSelectedValue();
        Object selected = getSelected();
        if (selectedValue == null || selected == null) {
            return false;
        }
        // Need to support "selected" set to a constant String
        // such as "true" or "false" when it is a boolean control.
        // This does not include when selected is bound to a String
        if (getValueExpression("selected") == null
                && selected instanceof String
                && selectedValue instanceof Boolean) {
            return selectedValue.equals(Boolean.valueOf((String) selected));
        } else {
            return selected.equals(selectedValue);
        }
    }

    /**
     * Return a component that implements an image. If a facet named
     * {@code image} is found that component is returned.
     * If a facet is not found and {@code getImageURL()} returns a non null
     * value an {@code ImageComponent} component instance is returned with
     * the id
     * {@code getId() + "_image"}.
     * The {@code ImageComponent} instance is initialized with the values
     * from
     *
     * <ul>
     * <li>{@code getImageURL()}</li>
     * <li>{@code getToolTip()} for the toolTip and alt property</li>
     * </ul>
     *
     * <p>
     * If a facet is not defined then the returned {@code ImageComponent}
     * component is created every time this method is called.
     * </p>
     *
     * @return - the image facet or an ImageComponent instance or null
     */
    public UIComponent getImageComponent() {
        UIComponent imageComponent = getFacet(IMAGE_FACET);
        if (imageComponent != null) {
            return imageComponent;
        }
        return (createImageComponent());
    }

    /**
     * Return a component that implements a label. If a facet named
     * {@code label} is found that component is returned.
     * If a facet is not found and {@code getLabel()} returns a non null
     * value, a {@code Label} component instance is returned with the
     * id
     * {@code getId() + "_label"}.
     * The {@code Label} instance is initialized with the values from
     *
     * <ul>
     * <li>{@code getLabel()}</li>
     * <li>{@code getLabelLevel()}</li>
     * </ul>
     *
     * <p>
     * If a facet is not defined then the returned {@code Label} component
     * is created every time this method is called.
     * </p>
     *
     * @return - the label facet or a Label instance or null
     */
    public UIComponent getLabelComponent() {
        UIComponent labelComponent = getFacet(LABEL_FACET);
        if (labelComponent != null) {
            return labelComponent;
        }
        return createLabelComponent();
    }

    /**
     * Create a {@code com.sun.webui.jsf.component.Label} to implement a
     * label for this component. If {@code getLabel()} returns null, null
     * is returned else a {@code Label} component is created each time this
     * method is called.
     * @return UIComponent
     */
    private UIComponent createLabelComponent() {

        // This diverges from previous behavior if a subsequent
        // request yields a null label. Previously if a private
        // facet was created, and getLabel() returned null
        // the previous facet was returned.
        String label = getLabel();
        if (label == null) {
            return null;
        }

        Label flabel = new Label();
        flabel.setId(ComponentUtilities
                .createPrivateFacetId(this, LABEL_FACET));
        flabel.setFor(getClientId(getFacesContext()));
        flabel.setText(label);
        flabel.setLabelLevel(getLabelLevel());
        flabel.setToolTip(getToolTip());
        flabel.setParent(this);
        return flabel;
    }

    /**
     * Create a {@code com.sun.webui.jsf.component.ImageComponent} to
     * represent the image for this component.
     * @return UIComponent
     */
    private UIComponent createImageComponent() {

        String iurl = getImageURL();
        if (iurl == null) {
            return null;
        }

        ImageComponent image = new ImageComponent();
        image.setId(ComponentUtilities
                .createPrivateFacetId(this, IMAGE_FACET));
        image.setUrl(getImageURL());
        image.setToolTip(getToolTip());
        image.setAlt(getToolTip());
        image.setParent(this);
        return image;
    }

    /**
     * This implementation aliases "selected" with "value" and "selectedValue"
     * width "items" and invokes {@code super.getValueExpression}.
     * @param exprName value expression name
     * @return ValueExpression
     */
    @Override
    public ValueExpression getValueExpression(final String exprName) {
        if (exprName.equals("selected")) {
            return super.getValueExpression("value");
        }
        if (exprName.equals("selectedValue")) {
            return super.getValueExpression("items");
        }
        return super.getValueExpression(exprName);
    }

    /**
     * This implementation aliases "selected" with "value" and "selectedValue"
     * width "items" and invokes {@code super.setValueExpression}.
     * @param exprName value expression name
     * @param binding value expression
     */
    @Override
    public void setValueExpression(final String exprName,
            final ValueExpression binding) {

        if (exprName.equals("selected")) {
            super.setValueExpression("value", binding);
            return;
        }
        if (exprName.equals("selectedValue")) {
            super.setValueExpression("items", binding);
            return;
        }
        super.setValueExpression(exprName, binding);
    }

    /**
     * A context relative path of an image to be displayed with the control. If
     * you want to be able to specify attributes for the image, specify an
     * {@code image} facet instead of the {@code imageURL}
     * attribute.
     * @return String
     */
    public String getImageURL() {
        if (this.imageURL != null) {
            return this.imageURL;
        }
        ValueExpression vb = getValueExpression("imageURL");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * A context relative path of an image to be displayed with the control. If
     * you want to be able to specify attributes for the image, specify an
     * {@code image} facet instead of the {@code imageURL}
     * attribute.
     *
     * @see #getImageURL()
     * @param newImageURL imageURL
     */
    public void setImageURL(final String newImageURL) {
        this.imageURL = newImageURL;
    }

    /**
     * Specifies the options that the web application user can choose from. The
     * value must be one of an array, Map or Collection whose members are all
     * subclasses of{@code com.sun.webui.jsf.model.Option}.
     * @return Object
     */
    @Override
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
     * subclasses of{@code com.sun.webui.jsf.model.Option}.
     *
     * @see #getItems()
     * @param newItems items
     */
    @Override
    public void setItems(final Object newItems) {
        this.items = newItems;
    }

    /**
     * Identifies the control as participating as part of a group. The
     * {@code RadioButton} and {@code Checkbox} classes determine the
     * behavior of the group, that are assigned the same value to the
     * {@code name} property. The value of this property must be unique for
     * components in the group, within the scope of the {@code Form} parent
     * component containing the grouped components.
     * @return String
     */
    public String getName() {
        if (this.name != null) {
            return this.name;
        }
        ValueExpression vb = getValueExpression("name");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Identifies the control as participating as part of a group. The
     * {@code RadioButton} and {@code Checkbox} classes determine the
     * behavior of the group, that are assigned the same value to the
     * {@code name} property. The value of this property must be unique for
     * components in the group, within the scope of the {@code Form} parent
     * component containing the grouped components.
     *
     * @see #getName()
     * @param newName name
     */
    public void setName(final String newName) {
        this.name = newName;
    }

    /**
     * The object that represents the selections made from the available
     * options. If multiple selections are allowed, this must be bound to
     * ArrayList, an Object array, or an array of primitives.
     * @return Object
     */
    @Property(name = "selected",
            displayName = "Selected",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.RbCbSelectedPropertyEditor")
            //CHECKSTYLE:ON
    @Override
    public Object getSelected() {
        return getValue();
    }

    /**
     * The object that represents the selections made from the available
     * options. If multiple selections are allowed, this must be bound to
     * ArrayList, an Object array, or an array of primitives.
     *
     * @see #getSelected()
     * @param selected selected
     */
    @Override
    public void setSelected(final Object selected) {
        setValue(selected);
    }

    /**
     * The value of the component when it is selected. The value of this
     * property is assigned to the {@code selected} property when the component
     * is selected. The component is selected when the {@code selected} property
     * is equal to this value.
     * This attribute can be bound to a {@code String}, or {@code
     * Object} value.
     * If this property is not assigned a value, the component behaves as a
     * {@code boolean} component. A {@code boolean} component is selected when
     * the {@code selected} property is equal to a true {@code Boolean}
     * instance.<br>
     * If a {@code boolean} component is not selected, the {@code selected}
     * property value is a false {@code Boolean} instance.
     *
     * @see #getSelectedValue()
     * @param selectedValue selectedValue
     */
    public void setSelectedValue(final Object selectedValue) {
        setItems(selectedValue);
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
        this.imageURL = (String) values[1];
        this.items = (Object) values[2];
        this.name = (String) values[3];
    }

    /**
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[4];
        values[0] = super.saveState(context);
        values[1] = this.imageURL;
        values[2] = this.items;
        values[3] = this.name;
        return values;
    }
}
