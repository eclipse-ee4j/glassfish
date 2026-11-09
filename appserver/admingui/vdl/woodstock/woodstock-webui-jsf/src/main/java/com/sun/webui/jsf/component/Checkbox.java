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

import java.util.ArrayList;
import java.util.Map;

/**
 * The Checkbox component is used to display a checkbox input element.
 * <p>
 * The {@code Checkbox} can be used as a single checkbox or one checkbox
 * among a group of check-boxes. A group of check-boxes represents a multiple
 * selection list which can have any number of check-boxes selected, or none
 * selected. A checkbox can represent a {@code Boolean} value, a
 * {@code String} value, or a developer defined {@code Object} value.
 * </p>
 * <h2>Detecting a selected checkbox</h2>
 * <p>
 * The {@code Checkbox} uses both the {@code selected} and
 * {@code selectedValue} properties to pass information about the
 * check-box selection status. The {@code selected} property is used to
 * indicate that the checkbox is selected. The {@code selectedValue}
 * property is used to pass a data value, a string by default, for the checkbox.
 * A checkbox is considered to be selected when the value of the
 * {@code selected} property is equal to the value of the
 * {@code selectedValue} property. A checkbox can be iniitally selected by
 * assigning the same value to the {@code selectedValue} and the
 * {@code  selected} properties. {@code isChecked} is called to
 * determine if this {@code Checkbox} is selected.
 * </p>
 * <p>
 * If the {@code selectedValue} property is not specified or its value is
 * {@code null} then the checkbox behaves like a boolean control. If the
 * checkbox is selected, the value of the {@code selected} property is a
 * true {@code Boolean} instance. If the checkbox is not selected, the
 * value of the {@code selected} property will be a false
 * {@code Boolean} instance.
 * </p>
 * <p>
 * <em>Note that a value binding expression that evaluates to a primitive
 * boolean value can be assigned to the {@code selected} property. Proper
 * type coercion from {@code Boolean} to {@code boolean} occurs.</em>
 * </p>
 * <p>
 * When check-boxes are part of a group, an {@code ArrayList} of selected
 * check-boxes is maintained. If any check-boxes within a group are selected, a
 * request attribute whose name is the value of the {@code name} property
 * is created and added to the {@code RequestMap}. The request attribute
 * value is an {@code ArrayList} containing the value of the
 * {@code selectedValue} property of each selected checkbox. If no
 * check-boxes are selected, no request attribute is created. The
 * {@code selected} property of each selected checkbox within the group
 * will also contain the value of the {@code selectedValue} property of the
 * respective selected checkbox.
 * </p>
 * <h2>Using a {@code checkbox} tag as a boolean control</h2>
 * <p>
 * If the {@code selectedValue} property is not specified or its value is
 * {@code null} then the checkbox behaves like a boolean control.
 * </p>
 * <p>
 * To use the {@code Checkbox} as a boolean control, do not specify a value
 * for the {@code selectedValue} property. The checkbox is selected if the
 * {@code selected} property is not null and has the value of a Boolean
 * instance with a {@code true} value. If the checkbox is not selected,
 * then the value of the {@code selected} property is a false
 * {@code Boolean} instance.
 * </p>
 * <p>
 * <em>Note that using a boolean checkbox in a group and referencing the request
 * property for the selected check-boxes is not useful, since the value of the
 * request property will be an {@code ArrayList
 * } of indistinguishable {@code true} values.</em>
 * </p>
 * <h2>Using a {@code Checkbox} to represent a developer defined value</h2>
 * <p>
 * The {@code selectedValue} property can be assigned a developer defined
 * object value to represent the value of a selected checkbox. If the checkbox
 * is selected, the value of the {@code selected} property is assigned the
 * value of the {@code selectedValue} property.
 * </p>
 * <p>
 * If the value of the {@code selectedValue} property is a developer
 * defined object, a {@code Converter} must be registered to convert to and
 * from a {@code String} value.<br>
 * In addition the object must support an {@code equals} method that
 * returns {@code true} when the value of the {@code selectedValue}
 * property is compared to the {@code selected} property value in order to
 * detect a selected checkbox.
 * </p>
 * <h2>Using a {@code Checkbox} as one control in a group</h2>
 * <p>
 * The {@code name} property determines whether a checkbox is part of a
 * group. A checkbox is treated as part of a group of check-boxes if the
 * {@code name} property of the checkbox is assigned a value equal to the
 * {@code name} property of the other check-boxes in the group. In other
 * words, all check-boxes of a group have the same {@code name} property
 * value. The group behaves like a multiple selection list, where zero or more
 * check-boxes can be selected. The value of the name property must be unique
 * within the scope of the Form parent containing the check-boxes.
 * </p>
 * <h2>Facets</h2>
 * <p>
 * The following facets are supported:
 * </p>
 * <ul>
 * <li><em>image</em> If the image facet exists, it replaces the
 * {@link com.sun.webui.jsf.component.ImageComponent} sub-component normally
 * created for the image associated with the checkbox if the
 * {@code imageURL} property is not null.</li>
 * <li><em>label</em> If the label facet exists, it replaces the
 * {@link com.sun.webui.jsf.component.Label} sub-component normally created for
 * the label associated with the checkbox, if the label property is not
 * null.</li>
 * </ul>
 * <p>
 * Add an image or label facet to the {@code Checkbox} if more control over
 * the properties of the sub-components is needed.
 * </p>
 * <p>
 * <em>Note that if a facet is exists, {@code Checkbox} properties that
 * would normally be assigned to the created sub-component, will not be assigned
 * to the facet</em>
 * </p>
 * <p>
 * <em>Note that unexpected layout of the {@code Checkbox} may occur if the
 * component specified by the facet is not a
 * {@link com.sun.webui.jsf.component.ImageComponent} for the image facet or
 * {@link com.sun.webui.jsf.component.Label} for the label facet.</em>
 * </p>
 * <h2>ImageComponent and Label sub-components</h2>
 * <p>
 * An image and a label may be associated with the {@code Checkbox}.<br/>
 * If the {@code imageURL} property is not null and an image facet does not
 * exist then a {@link com.sun.webui.jsf.component.ImageComponent} component is
 * created.<br/>
 * If the {@code label} property is not null and a label facet does not
 * exist then a {@link com.sun.webui.jsf.component.Label} component is created.
 * </p>
 * <p>
 * The following {@code Checkbox} properties are assigned to the
 * sub-components only if a facet does not exist.<br/>
 * For the {@link com.sun.webui.jsf.component.ImageComponent} sub-component
 * <ul>
 * <li>this.getId() + "_image" is assigned to the {@code id} property.</li>
 * <li>this.getImageURL() is assigned to the {@code url} property.</li>
 * <li>this.getToolTip() is assigned to the {@code toolTip} property.</li>
 * <li>this.getToolTip() is assigned to the {@code alt} property.</li>
 * <li>this.isVisible() is assigned to the {@code visible} property.</li>
 * <li>this.isRendered() is assigned to the {@code renderer} property.</li>
 * </ul>
 *
 * <p>
 * For the {@link com.sun.webui.jsf.component.Label} sub-component
 * <ul>
 * <li>this.getId() + "_label" is assigned to the {@code id} property.</li>
 * <li>this.getClientId() is assigned to the {@code for} property.</li>
 * <li>this.getLabel() is assigned to the {@code text} property.</li>
 * <li>this.getLabelLevel is assigned to the {@code labelLevel}
 * property.</li>
 * <li>this.getToolTip is assigned to the {@code toolTip} property.</li>
 * <li>this.isVisible is assigned to the {@code visible} property.</li>
 * <li>this.isRendered is assigned to the {@code renderer} property.</li>
 * </ul>
 * <em>Note that if a value binding exists for one of the {@code Checkbox}
 * properties mentioned above, the value binding is set on the sub-component for
 * that property.</em>
 */
@Component(type = "com.sun.webui.jsf.Checkbox",
        family = "com.sun.webui.jsf.Checkbox",
        displayName = "Checkbox",
        tagName = "checkbox",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_checkbox",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_checkbox_props")
        //CHECKSTYLE:ON
public final class Checkbox extends RbCbSelector {

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
    private boolean labelLevelSet;

    /**
     * Constructor for a {@code Checkbox}.
     */
    public Checkbox() {
        super();
        // When used in a group you can choose multiple
        // and this behavior is provided, but the single
        // implementation of Checkbox vs. CheckboxGroup
        // does not need Multiple to be explicit.
        setMultiple(false);
        setRendererType("com.sun.webui.jsf.Checkbox");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Checkbox";
    }

    /**
     * Return an {@code ArrayList} containing the value of the
     * {@code selectedValue} property of each selected checkbox in the group of
     * check-boxes identified by the {@code name} parameter. A {@code Checkbox}
     * is one of a group of check-boxes if more than on checkbox has the same
     * value for the {@code name} property. When one of the check-boxes among
     * that group is selected, the value of its {@code selectedValue} property
     * is maintained within an {@code ArrayList} that is stored in a request
     * attribute identified by the value of its {@code name} property.
     *
     * @param name the value a Checkbox name property.
     * @return ArrayList
     */
    public static ArrayList getSelected(final String name) {

        Map rm = FacesContext.getCurrentInstance().getExternalContext().
                getRequestMap();
        if (name != null) {
            return (ArrayList) rm.get(name);
        } else {
            return null;
        }
    }

    /**
     * <p>
     * Update the request parameter that holds the value of the
     * {@code selectedValue} property of the selected check box.
     * </p>
     * If the {@code name} property has been set a request attribute is
     * created. The value of the {@code name} property will be used for the
     * request attribute name and the value of the request attribute will be an
     * {@code ArrayList} containing the value of the
     * {@code selectedValue} property of the selected check boxes that have
     * the same {@code name} property value. If no check box is selected
     * then no request attribute will be created.
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

        // If not valid or in a group don't add the checkbox to
        // the request map.
        if (!isValid()) {
            return;
        }
        String groupName = getName();
        if (groupName == null) {
            return;
        }

        // If the submitted value is valid and the
        // checkbox is selected add it to the
        // request map array list. To check if the component
        // is selected, can't call "isChecked" or getValue()
        // cause if there is a value binding, it will return the previously
        // selected state and not the state of this submit
        // so use getLocalValue() which is set if isValid is true.
        Object selected = getLocalValue();
        if (!getSelectedValue().equals(selected)) {
            return;
        }
        addToRequestMap(context, groupName, selected);
    }

    /**
     * Add to request.
     * @param context faces context
     * @param groupName group name
     * @param selected selected value
     */
    @SuppressWarnings("unchecked")
    protected void addToRequestMap(final FacesContext context,
            final String groupName, final Object selected) {

        Map<String, Object> requestMap = context.getExternalContext()
                .getRequestMap();
        ArrayList selectedCB = (ArrayList) requestMap.get(groupName);
        if (selectedCB == null) {
            selectedCB = new ArrayList();
            requestMap.put(groupName, selectedCB);
        }
        if (!selectedCB.contains(selected)) {
            selectedCB.add(selected);
        }
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

    // Hide value
    @Property(name = "value", isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * Sets the style level for the generated label, provided the label
     * attribute has been set. Valid values are 1 (largest), 2 and 3 (smallest).
     * The default value is 3.
     * @return int
     */
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

    /**
     * Sets the style level for the generated label, provided the label
     * attribute has been set. Valid values are 1 (largest), 2 and 3 (smallest).
     * The default value is 3.
     *
     * @see #getLabelLevel()
     * @param newLabelLevel labelLevel
     */
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
