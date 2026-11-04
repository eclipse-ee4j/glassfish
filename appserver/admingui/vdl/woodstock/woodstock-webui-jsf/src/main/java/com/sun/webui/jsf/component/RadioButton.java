/*
 * Copyright 2023 Contributors to the Eclipse Foundation.
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

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * A component that represents a radio button.</p>
 * <p>
 * The {@code RadioButton} can be used as a single radio button or one radio
 * button among a group of radio button. A group of radio button represents a
 * multiple selection list which can have any number of radio button selected,
 * or none selected. A radio button can represent a {@code Boolean} value, a
 * {@code String} value, or a developer defined {@code Object} value.
 * </p>
 * <h3>Detecting a selected radio button</h3>
 * <p>
 * The {@code RadioButton} uses both the {@code selected} and
 * {@code selectedValue} properties to pass information about the radio button's
 * selection status. The {@code selected} property is used to indicate that the
 * radio button is selected. The {@code selectedValue} property is used to pass
 * a data value, a string by default, for the radio button. A radio button is
 * considered to be selected when the value of the {@code selected} property is
 * equal to the value of the {@code selectedValue} property. A radio button can
 * be initially selected by assigning the same value to the
 * {@code selectedValue} and the {@code  selected} properties. {@code isChecked}
 * is called to determine if this {@code RadioButton} is selected.
 * </p>
 * <p>
 * If the {@code selectedValue} property is not specified or its value is
 * {@code null} then the radio button behaves like a boolean control. If the
 * radio button is selected, the value of the {@code selected} property is a
 * true {@code Boolean} instance. If the radio button is not selected, the value
 * of the {@code selected} property will be a false {@code Boolean} instance.
 * </p>
 * <p>
 * <em>Note that a value binding expression that evaluates to a primitive
 * boolean value can be assigned to the {@code selected} property. Proper type
 * coercion from {@code Boolean} to {@code boolean} occurs.</em>
 * </p>
 * <p>
 * When a radio button is part of a group, the selected radio button is
 * maintained as a request attribute in the {@code RequestMap}. The name of the
 * attribute is the value of the radio button's {@code name} property. The
 * request attribute value is the value of the {@code selectedValue} property of
 * the selected radio button. The {@code selected} property of the selected
 * radio button within the group, will also contain the value of the
 * {@code selectedValue} property of the respective selected radio button. If no
 * radio buttons are selected, no request attribute is created, however at least
 * one radio button must be selected.
 * </p>
 * <p>
 * <em>
 * Note that the {@code RadioButton} does not enforce the requirement that at
 * least one radio button must be selected. The application should ensure that
 * this requirement is met.
 * </em></p>
 * <h3>Using a {@code radio button} tag as a boolean control</h3>
 * <p>
 * If the {@code selectedValue} property is not specified or its value is
 * {@code null} then the radio button behaves like a boolean control.
 * </p>
 * <p>
 * To use the {@code RadioButton} as a boolean control, do not specify a value
 * for the {@code selectedValue} property. The radio button is selected if the
 * {@code selected} property is not null and has the value of a Boolean instance
 * with a {@code true} value. If the radio button is not selected, then the
 * value of the {@code selected} property is a false {@code Boolean} instance.
 * </p>
 * <p>
 * <em>Note that using a boolean radio button in a group and referencing the
 * request property for the selected radio button is not useful, since the value
 * of the request property will be an indistinguishable {@code true} value.</em>
 * </p>
 * <h3>Using a {@code RadioButton} to represent a developer defined value</h3>
 * <p>
 * The {@code selectedValue} property can be assigned a developer defined object
 * value to represent the value of a selected radio button. If the radio button
 * is selected, the value of the {@code selected} property is assigned the value
 * of the {@code selectedValue} property.
 * </p>
 * <p>
 * If the value of the {@code selectedValue} property is a developer defined
 * object, a {@code Converter} must be registered to convert to and from a
 * {@code String} value.<br>
 * In addition the object must support an {@code equals} method that returns
 * {@code true} when the value of the {@code selectedValue} property is compared
 * to the {@code selected} property value in order to detect a selected radio
 * button.
 * </p>
 * <h3>Using a {@code RadioButton} as one control in a group</h3>
 * <p>
 * The {@code name} property determines whether a radio button is part of a
 * group. A radio button is treated as part of a group of radio buttons if the
 * {@code name} property of the radio button is assigned a value equal to the
 * {@code name} property of the other radio buttons in the group. In other
 * words, all radio button of a group have the same {@code name} property value.
 * The group behaves like a single selection list, where only one radio button
 * can be selected. The value of the name property must be unique within the
 * scope of the Form parent containing the radio buttons.
 * </p>
 * <h3>Facets</h3>
 * <p>
 * The following facets are supported:
 * </p>
 * <ul>
 * <li><em>image</em> If the image facet exists, it replaces the
 * {@link com.sun.webui.jsf.component.ImageComponent} sub-component normally
 * created for the image associated with the radio button, if the
 * {@code imageURL} property is not null.</li>
 * <li><em>label</em> If the label facet exists, it replaces the
 * {@link com.sun.webui.jsf.component.Label} sub-component normally created for
 * the label associated with the radio button, if the label property is not
 * null.</li>
 * </ul>
 * <p>
 * Add an image or label facet to the {@code RadioButton} if more control over
 * the properties of the sub-components is needed.
 * </p>
 * <p>
 * <em>Note that if a facet is exists, {@code RadioButton} properties that would
 * normally be assigned to the created sub-component, will not be assigned to
 * the facet</em>
 * </p>
 * <p>
 * <em>Note that unexpected layout of the {@code RadioButton} may occur if the
 * component specified by the facet is not a
 * {@link com.sun.webui.jsf.component.ImageComponent} for the image facet or
 * {@link com.sun.webui.jsf.component.Label} for the label facet.</em>
 * </p>
 * <h3>ImageComponent and Label sub-components</h3>
 * <p>
 * An image and a label may be associated with the {@code RadioButton}. If the
 * {@code imageURL} property is not null and an image facet does not exist then
 * a {@link com.sun.webui.jsf.component.ImageComponent} component is created. If
 * the {@code label} property is not null and a label facet does not exist then
 * a {@link com.sun.webui.jsf.component.Label} component is created.
 * </p>
 * <p>
 * The following {@code RadioButton} properties are assigned to the
 * sub-components only if a facet does not exist. For the
 * {@link com.sun.webui.jsf.component.ImageComponent} sub-component
 * <ul>
 * <li>this.getId() + "_image" is assigned to the {@code id} property.</li>
 * <li>this.getImageURL() is assigned to the {@code url} property.</li>
 * <li>this.getToolTip() is assigned to the {@code toolTip} property.</li>
 * <li>this.getToolTip() is assigned to the {@code alt} property.</li>
 * <li>this.isVisible() is assigned to the {@code visible} property.</li>
 * <li>this.isRendered() is assigned to the {@code renderer} property.</li>
 * </ul>
 * </p>
 * <p>
 * For the {@link com.sun.webui.jsf.component.Label} sub-component
 * <ul>
 * <li>this.getId() + "_label" is assigned to the {@code id} property.</li>
 * <li>this.getClientId() is assigned to the {@code for} property.</li>
 * <li>this.getLabel() is assigned to the {@code text} property.</li>
 * <li>this.getLabelLevel is assigned to the {@code labelLevel} property.</li>
 * <li>this.getToolTip is assigned to the {@code toolTip} property.</li>
 * <li>this.isVisible is assigned to the {@code visible} property.</li>
 * <li>this.isRendered is assigned to the {@code renderer} property.</li>
 * </ul>
 * </p>
 * <em>Note that if a value binding exists for one of the {@code RadioButton}
 * properties mentioned above, the value binding is set on the sub-component for
 * that property.</em>
 */
@Component(type = "com.sun.webui.jsf.RadioButton",
        family = "com.sun.webui.jsf.RadioButton",
        displayName = "Radio Button",
        tagName = "radioButton",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_radiobutton",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_radio_button_props")
        //CHECKSTYLE:ON
public final class RadioButton extends RbCbSelector {

    /**
     * Sets the style level for the generated label, provided the label
     * attribute has been set. Valid values are 1 (largest), 2 and 3 (smallest).
     * The default value is 3.
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
    private boolean labelLevelSet = false;

    /**
     * Default constructor.
     */
    public RadioButton() {
        super();
        setMultiple(false);
        setRendererType("com.sun.webui.jsf.RadioButton");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.RadioButton";
    }

    /**
     * Return the value of the {@code selectedValue} property of the selected
     * radio button in the group of radio buttons identified by the {@code name}
     * parameter. A {@code RadioButton} is one of a group of radio buttons if
     * more than on radio button has the same value for the {@code name}
     * property.
     * When one of the radio buttons among that group is selected, the value of
     * its {@code selectedValue} property is maintained in a request attribute
     * identified by the value of its {@code name} property.
     *
     * @param name the value a RadioButton name property.
     * @return Object
     */
    public static Object getSelected(final String name) {
        Map rm = FacesContext.getCurrentInstance().getExternalContext().
                getRequestMap();

        if (name != null) {
            return rm.get(name);
        } else {
            return null;
        }
    }

    @Override
    public void updateModel(final FacesContext context) {
        if (Objects.equals(getSelected(getName()), getValue())) {
            super.updateModel(context);
        }
    }

    /**
     * <p>
     * Update the request parameter that holds the value of the
     * {@code selectedValue} property of the selected radio button.
     * </p>
     * If the {@code name} property has been set a request attribute is created.
     * The value of the {@code name} property will be used for the request
     * attribute name and the value of the request attribute will be the value
     * of the {@code selectedValue} property.
     * <p>
     * The request attribute described above is available during a
     * {@code ValueChangeEvent}.
     * </p>
     *
     * @param context The context of this request.
     */
    @Override
    public void validate(final FacesContext context) {
        super.validate(context);
        if (!(isValid() && isChecked())) {
            return;
        }
        String groupName = getName();
        if (groupName == null) {
            return;
        }
        addToRequestMap(context, groupName);
    }

    @Override
    protected void addToRequestMap(final FacesContext context,
            final String groupName) {

        Map<String, Object> requestMap = context.getExternalContext()
                .getRequestMap();
        requestMap.put(groupName, getValue());
    }

    // Hide items
    @Property(name = "items", isHidden = true, isAttribute = false)
    @Override
    public Object getItems() {
        return super.getItems();
    }

    // Hide required
    @Property(name = "required", isHidden = true, isAttribute = false)
    @Override
    public boolean isRequired() {
        return super.isRequired();
    }

    // Hide hidden
    @Property(isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    // Hide onSelect
    @Property(isHidden = true, isAttribute = false)
    @Override
    public String getOnSelect() {
        return super.getOnSelect();
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
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
        return 3;
    }

    @Override
    public void setLabelLevel(final int newLabelLevel) {
        this.labelLevel = newLabelLevel;
        this.labelLevelSet = true;
    }

    @Override
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.labelLevel = ((Integer) values[1]);
        this.labelLevelSet = ((Boolean) values[2]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[3];
        values[0] = super.saveState(context);
        values[1] = this.labelLevel;
        if (this.labelLevelSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        return values;
    }
}
