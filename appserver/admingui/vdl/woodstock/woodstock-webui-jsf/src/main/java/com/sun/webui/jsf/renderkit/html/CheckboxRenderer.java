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

package com.sun.webui.jsf.renderkit.html;

import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.Checkbox;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.MessageUtil;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Map;

import static com.sun.webui.jsf.util.ThemeUtilities.getTheme;

/**
 * The {@code CheckboxRenderer} renders a
 * {@link com.sun.webui.jsf.component.Checkbox} component.
 *
 * <h3>Encoding</h3>
 * <p>
 * The {@code CheckboxRenderer} renders a {@code Checkbox} as:
 * <ul>
 * <li> An INPUT element of type checkbox for each checkbox.
 * </li>
 * <li> An optional image. The component rendered for this feature is obtained
 * from a call to {@code getImageComponent()} on the component being
 * rendered. </li>
 * <li> An optional label. The component rendered for this feature is obtained
 * from a call to {@code getLabelComponent()} on the component being
 * rendered. </li>
 * </ul>
 * </p>
 * <p>
 * The CSS selectors for the elements and components that comprise a
 * checkbox are identified by java
 * constants defined in the {@link ThemeStyles} class.
 * </p>
 * <ul>
 * <li>CHECKBOX for the INPUT element</li>
 * <li>CHECKBOX_DISABLED for the INPUT element of a disabled radio
 * button</li>
 * <li>CHECKBOX_LABEL for label component if a label is rendered</li>
 * <li>CHECKBOX_LABEL_DISABLED for a label component of a disabled
 * radio button, if a label is rendered</li>
 * <li>CHECKBOX_IMAGE for an image component if an image is rendered</li>
 * <li>CHECKBOX_IMAGE_DISABLED for an image component of a disabled
 * radio button if an image is rendered.</li>
 * </ul>
 * <em>Note that these selectors are appended to any existing selectors
 * that may already exist on the {@code styleClass} property of the
 * {@code Checkbox} component and the optional image and label
 * components.</em>
 * <p>
 * For more details on the logic for actual renderering of HTML for
 * a {@code Checkbox} component see the super class
 * {@link com.sun.webui.jsf.renderkit.html.RbCbRendererBase}
 * </p>
 * <p>

 * <h3>Decoding</h3>
 * <p>
 * If the INPUT element representing a checkbox is selected on the
 * the client, the submitted request will contain a request parameter
 * whose name is the value of the name attribute of the selected
 * HTML INPUT element. The value of the request parameter will be the
 * value of the value attribute of the selected HTML INPUT element.
 * If more than one checkbox INPUT element has the same value for
 * the name attribute, then the value of the request parameter will
 * be an array of those INPUT element values.
 * </p>
 * <p>
 * The component being decoded is selected if the component's
 * {@code isDisabled} and {@code isReadOnly} methods
 * return false and:
 * </p>
 * <ul>
 * <li>a request parameter exists that is equal to its {@code name}
 * property. If the {@code name} property is null, then a
 * request parameter exists that is equal to its {@code clientId}
 * property.
 * <li/>
 * </ul>
 * <p>
 * And
 * </p>
 * <ul>
 * <li> the request parameter's value is an array that contains an element
 * that is {@code String.equal} to the
 * the component's {@code selectedValue} property, after conversion
 * to a {@code String}, by calling
 * {@code ConversionUtilities.convertValueToString}. If the component
 * was encoded as a boolean control, then an element the request parameter's
 * array value must be equal to the component's {@code clientId} property.
 * </li>
 * </ul>
 * <p>
 * If selected, a {@code String[1]} array is assigned as the component's
 * submitted value where the single array element is the {@code String}
 * version of the {@code selectedValue} property or "true" if the
 * component was encoded as a boolean control.
 * If not selected, a {@code String[0]} array is assigned as the
 * component's submitted value or a {@code String[1]} array where the
 * single array element is "false" if the component was encoded as a
 * boolean control.
 * </p>
 * <p>
 * If the component's {@code isDisabled} or {@code isReadOnly}
 * methods return true no submitted value is assigned to the component,
 * and results in a null submitted value implying the component
 * was not submitted, and the state of the component is unchanged.
 * </p>
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.Checkbox"))
//FIXME check about making SelectGroupRenderer a public abstract class
public final class CheckboxRenderer extends RbCbRendererBase {

    /**
     * Error message for component not a check-box type.
     */
    private static final String MSG_COMPONENT_NOT_CHECKBOX =
            "CheckboxRenderer only renders Checkbox components.";

    /**
     * All checkbox styles.
     */
    private static final String[] STYLES = {
        ThemeStyles.CHECKBOX, // INPUT
        ThemeStyles.CHECKBOX_DISABLED, // INPUT_DIS
        ThemeStyles.CHECKBOX_LABEL, // LABEL
        ThemeStyles.CHECKBOX_LABEL_DISABLED, // LABEL_DIS
        ThemeStyles.CHECKBOX_IMAGE, // IMAGE
        ThemeStyles.CHECKBOX_IMAGE_DISABLED, // IMAGE_DIS
        ThemeStyles.CHECKBOX_SPAN, // SPAN
        ThemeStyles.CHECKBOX_SPAN_DISABLED, // SPAN_DIS
    };

    /**
     * Creates a new instance of CheckboxRenderer.
     */
    public CheckboxRenderer() {
        super();
    }

    /**
     * <p>Decode the {@code Checkbox} selection.</p>
     * <p>
     * If the component's {@code isDisabled} and {@code isReadOnly}
     * methods return false,
     * If the value of the component's {@code name} property
     * has been set, the value is used to match a request parameter.
     * If it has not been set the component clientId is used to match
     * a request parameter. If a match is found, and the value of the
     * of the request parameter matches the {@code String} value of the
     * component's {@code selectedValue} property, the
     * radio button is selected. The component's submitted value is
     * assigned a {@code String[1]} array where the single array
     * element is the matching parameter value.
     * </p>
     * <p>
     * If no matching request parameter or value is found, an instance of
     * {@code String[0]} is assigned as the submitted value,
     * meaning that this is a component was not selected.
     * </p>
     * <p>
     * If the component was encoded as a boolean control the
     * value of the matching request attribute will be the component's
     * {@code clientId} property if selected. If selected the
     * submitted value is {@code new String[] { "true" }}
     * and {@code new String[] { "false" }} if not selected.
     * </p>
     * <p>
     * It is the developer's responsibility to ensure the validity of the
     * {@code name} property (the name attribute on the
     * INPUT element) by ensuring that it is unique to the radio buttons
     * for a given group within a form.
     * </p>
     *
     * @param context FacesContext for the request we are processing.
     * @param component The {@code Checkbox}
     * component to be decoded.
     */
    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        // We need to know if the last state of the component before decoding
        // this checkbox. This disabled check is not to determine
        // if the checkbox was disabled on the client.
        // We assume that the disabled state is in the same state as it was
        // when this checkbox was last rendered.
        // If the checkbox was disabled then it can not have changed on
        // the client. We ignore the case that it might have been
        // enabled in javascript on the client.
        // This allows us to distinguish that no checkbox was selected.
        // No checkboxes are selected when "isDisabled || isReadOnly -> false
        // and no request parameters match the name attribute if part of a
        // group or the clientId, if a single checkbox.
        //
        if (isDisabled(component) || isReadOnly(component)) {
            return;
        }

        // If there is a request parameter that that matches the
        // name property, this component is one of the possible
        // selections. We need to match the value of the parameter to the
        // the component's value to see if this is the selected component,
        // unless it is a group of Boolean checkboxes.
        //
        Checkbox checkbox = (Checkbox) component;
        String name = checkbox.getName();
        boolean inGroup = name != null;

        // If name not set look for clientId.
        // Boolean checkboxes decode correctly when they are not
        // in a group, since the submitted attribute
        // value in the clientId and is unique for each check box.
        //
        if (name == null) {
            name = component.getClientId(context);
        }

        Map requestParameterValuesMap = context.getExternalContext().
                getRequestParameterValuesMap();

        // If a parameter with key == name does not exist, the component
        // was not submitted. This only means that it is
        // unchecked, since we already know that is it not in the
        // map because is was readonly or disabled. (based on the
        // server side state)
        //
        if (requestParameterValuesMap.containsKey(name)) {

            String[] newValues = (String[]) requestParameterValuesMap.get(name);

            if (newValues != null || newValues.length != 0) {

                String selectedValueAsString;
                Object selectedValue = checkbox.getSelectedValue();

                // We need to discern the case where the checkbox
                // is part of a group and it is a boolean checkbox.
                // If the checkbox is part of a group and it is a
                // boolean checkbox then the submitted value contains the
                // value of "component.getClientId()". If
                // the value was not a unique value within the group
                // of boolean checkboxes, then all will appear selected,
                // since name will be the same for all the checkboxes
                // and the submitted value would always be "true" and then
                // every checkbox component in the group would decode
                // as selected.
                if (inGroup && selectedValue instanceof Boolean) {
                    selectedValueAsString = component.getClientId(context);
                    // See if one of the values of the attribute
                    // is equal to the component id.
                    //
                    // Use the toString value of selectedValue even if
                    // Boolean in case it is FALSE and the application
                    // wants checked to be "FALSE == FALSE"
                    //
                    for (int i = 0; i < newValues.length; ++i) {
                        if (selectedValueAsString.equals(newValues[i])) {
                            ((UIInput) component).setSubmittedValue(
                                    new String[]{selectedValue.toString()});

                            return;
                        }
                    }
                } else {
                    selectedValueAsString =
                            ConversionUtilities.convertValueToString(
                            component, selectedValue);

                    for (int i = 0; i < newValues.length; ++i) {
                        if (selectedValueAsString.equals(newValues[i])) {
                            ((UIInput) component).setSubmittedValue(
                                    new String[]{newValues[i]});

                            return;
                        }
                    }
                }
                // Not selected.
                // But this results in an update to the model object
                // of every checkbox, even if the value is the same.
                // However only those that experience a state change issue
                // a ValueChangeEvent.
                ((UIInput) component).setSubmittedValue(new String[0]);
                return;
            }
        }
        // Not disabled and this checkbox is not selected.
        //
        ((UIInput) component).setSubmittedValue(new String[0]);
    }

    /**
     * Ensure that the component to be rendered is a Checkbox instance.
     * Actual rendering occurs during {@code renderEnd}
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     * @throws IOException if an IO error occurs
     */
    @Override
    public void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // Bail out if the component is not a Checkbox component.
        if (!(component instanceof Checkbox)) {
            throw new IllegalArgumentException(
                    MessageUtil.getMessage(context,
                    BUNDLE, MSG_COMPONENT_NOT_CHECKBOX));
        }
    }

    @Override
    public void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Theme theme = getTheme(context);
        renderSelection(context, component, theme, writer, "checkbox");
    }

    @Override
    protected boolean isSelected(final FacesContext context,
            final UIComponent component) {

        return ((Checkbox) component).isChecked();
    }

    @Override
    protected String getStyle(final Theme theme, final int styleCode) {
        String style = null;
        try {
            style = theme.getStyleClass(STYLES[styleCode]);
        } catch (Exception e) {
            // Don't care
        }
        return style;
    }
}
