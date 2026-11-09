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
import com.sun.webui.jsf.component.RadioButton;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Map;

/**
 * <p>
 * The {@code RadioButtonRenderer} renders a
 * {@link com.sun.webui.jsf.component.RadioButton} component.
 * </p>
 * <h2>Encoding</h2>
 * <p>
 * The {@code RadioButtonRenderer} renders a {@code RadioButton} as:
 * <ul>
 * <li> An INPUT element of type radio for each radio button.
 * </li>
 * <li> An optional image. The component rendered for this feature is obtained
 * from a call to {@code getImageComponent()} on the component being
 * rendered. </li>
 * <li> An optional label. The component rendered for this feature is obtained
 * from a call to {@code getLabelComponent()} on the component being
 * rendered. </li>
 * </ul>
 *
 * <p>
 * The CSS selectors for the elements and components that comprise a radio
 * button are identified by java constants defined in the {@link ThemeStyles}
 * class.
 * </p>
 * <ul>
 * <li>RADIOBUTTON for the INPUT element</li>
 * <li>RADIOBUTTON_DISABLED for the INPUT element of a disabled radio
 * button</li>
 * <li>RADIOBUTTON_LABEL for label component if a label is rendered</li>
 * <li>RADIOBUTTON_LABEL_DISABLED for a label component of a disabled radio
 * button, if a label is rendered</li>
 * <li>RADIOBUTTON_IMAGE for an image component if an image is rendered</li>
 * <li>RADIOBUTTON_IMAGE_DISABLED for an image component of a disabled radio
 * button if an image is rendered.</li>
 * </ul>
 * <em>Note that these selectors are appended to any existing selectors that may
 * already exist on the {@code styleClass} property of the
 * {@code RadioButton} component and the optional image and label
 * components.</em>
 * <p>
 * For more details on the encoding the {@code RadioButton} component see
 * the super class {@link com.sun.webui.jsf.renderkit.html.RbCbRendererBase}
 * </p>
 *
 * <h2>Decoding</h2>
 * <p>
 * If the INPUT element representing a radio button is selected on the the
 * client, the submitted request will contain a request parameter whose name is
 * the value of the name attribute of the selected HTML INPUT element. The value
 * of the request parameter will be the value of the value attribute of the
 * selected HTML INPUT element.
 * </p>
 * <p>
 * The component being decoded is selected if the component's
 * {@code isDisabled} and {@code isReadOnly} methods return false and:
 * </p>
 * <ul>
 * <li>a request parameter exists that is equal to its {@code name}
 * property. If the {@code name} property is null, then a request parameter
 * exists that is equal to its {@code clientId} property.
 * </ul>
 * <p>
 * And
 * </p>
 * <ul>
 * <li>the request parameter's value is {@code String.equal} to the the
 * component's {@code selectedValue} property, after conversion to a
 * {@code String}, by calling
 * {@code ConversionUtilities.convertValueToString}. If the component was
 * encoded as a boolean control, then the request parameter's value must be
 * equal to the component's {@code clientId} property.
 * </li>
 * </ul>
 * <p>
 * If selected, a {@code String[1]} array is assigned as the component's
 * submitted value where the single array element is the {@code String}
 * version of the {@code selectedValue} property or "true" if the component
 * was encoded as a boolean control.
 * If not selected, a {@code String[0]} array is assigned as the
 * component's submitted value or a {@code String[1]} array where the
 * single array element is "false" if the component was encoded as a boolean
 * control.
 * </p>
 * <p>
 * If the component's {@code isDisabled} or {@code isReadOnly} methods
 * return true no submitted value is assigned to the component, and results in a
 * null submitted value implying the component was not submitted, and the state
 * of the component is unchanged.
 * <p>
 * Since the {@code RadioButtonRenderer} only renders a single
 * {@code RadioButton} component it cannot enforce that at least one radio
 * button should be selected among a group of {@code RadioButton}
 * components with the same {@code name} property.
 * </p>
 * <p>
 * If the {@code RadioButton} is selected, the {@code selected} property will be
 * the same value as the {@code selectedValue} property. If more than one
 * {@code RadioButton} component is encoded with the same {@code name} property
 * and more than one {@code RadioButton}'s is selected, the last selected
 * {@code RadioButton} component that is encoded will be appear as checked in
 * the HTML page. Subsequently during the next submit, only the checked
 * {@code RadioButton} component will be selected.
 * </p>
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.RadioButton"))
//FIXME check about making RbCbRendererBase a public abstract class
public final class RadioButtonRenderer extends RbCbRendererBase {

    /**
     * Error message when wrong component type is being rendered.
     */
    private static final String MSG_COMPONENT_NOT_RADIOBUTTON
            = "RadioButtonRenderer only renders RadioButton components.";

    /**
     * CSS styles.
     */
    private static final String[] STYLES = {
        ThemeStyles.RADIOBUTTON, /* INPUT */
        ThemeStyles.RADIOBUTTON_DISABLED, /* INPUT_DIS */
        ThemeStyles.RADIOBUTTON_LABEL, /* LABEL */
        ThemeStyles.RADIOBUTTON_LABEL_DISABLED, /* LABEL_DIS */
        ThemeStyles.RADIOBUTTON_IMAGE, /* IMAGE */
        ThemeStyles.RADIOBUTTON_IMAGE_DISABLED, /* IMAGE_DIS */
        ThemeStyles.RADIOBUTTON_SPAN, /* SPAN */
        ThemeStyles.RADIOBUTTON_SPAN_DISABLED /* SPAN_DIS */
    };

    /**
     * Creates a new instance of RadioButtonRenderer.
     */
    public RadioButtonRenderer() {
        super();
    }

    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        // We need to know the last state of the component before decoding
        // this radio button. This disabled check is not to determine
        // if the radio button was disabled on the client.
        // We assume that the disabled state is in the same state as it was
        // when this radio button was last rendered.
        // If the radio button was disabled then it can not have changed on
        // the client. We ignore the case that it might have been
        // enabled in javascript on the client.
        // This allows us to distinguish that no radio button was selected.
        // No radio buttons are selected when "isDisabled || isReadOnly -> false
        // and no request parameters match the name attribute if part of a
        // group or the clientId if a single radio button.
        if (isDisabled(component) || isReadOnly(component)) {
            return;
        }
        // If there is a request parameter that that matches the
        // name property, this component is one of the possible
        // selections. We need to match the value of the parameter to the
        // the component's value to see if this is the selected component.
        RadioButton radioButton = (RadioButton) component;
        String name = radioButton.getName();
        boolean inGroup = name != null;

        // If name is null use the clientId.
        if (name == null) {
            name = component.getClientId(context);
        }

        Map requestParameterMap = context.getExternalContext().
                getRequestParameterMap();

        // The request parameter map contains the INPUT element
        // name attribute value as a parameter. The value is the
        // the "selectedValue" value of the RadioButton component.
        if (requestParameterMap.containsKey(name)) {

            String newValue = (String) requestParameterMap.get(name);

            // We need to discern the case where the radio button
            // is part of a group and it is a boolean radio button.
            // If the radio button is part of a group and it is a
            // boolean radio button then the submitted value contains the
            // value of "component.getClientId()". If
            // the value was not a unique value within the group
            // of boolean radio buttons, then all will appear selected,
            // since name will be the same for all the radio buttons
            // and the submitted value would always be "true" and then
            // every radio button component in the group would decode
            // as selected. Due to the HTML implementation of radio
            // buttons, only the last radio button will appear selected.
            Object selectedValue = radioButton.getSelectedValue();
            String selectedValueAsString;
            if (inGroup && selectedValue instanceof Boolean) {
                selectedValueAsString = component.getClientId(context);
                // Use the toString value of selectedValue even if
                // it is a Boolean control, in case the application
                // wants "FALSE == FALSE" to mean checked.
                if (selectedValueAsString.equals(newValue)) {
                    ((UIInput) component).setSubmittedValue(
                            new String[]{selectedValue.toString()});
                    return;
                }
            } else {
                selectedValueAsString
                        = ConversionUtilities.convertValueToString(component,
                                selectedValue);
                if (selectedValueAsString.equals(newValue)) {
                    ((UIInput) component).setSubmittedValue(
                            new String[]{newValue});
                    return;
                }
            }
            // Not selected possibly deselected.
            ((UIInput) component).setSubmittedValue(new String[0]);
        }
    }

    @Override
    public void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // Bail out if the component is not a RadioButton component.
        // This message should be logged.
        if (!(component instanceof RadioButton)) {
            throw new IllegalArgumentException(MSG_COMPONENT_NOT_RADIOBUTTON);
        }
    }

    @Override
    public void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Theme theme = ThemeUtilities.getTheme(context);
        renderSelection(context, component, theme, writer, "radio");
    }

    @Override
    protected boolean isSelected(final FacesContext context,
            final UIComponent component) {

        return ((RadioButton) component).isChecked();
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
