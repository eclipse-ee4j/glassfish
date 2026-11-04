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
import com.sun.webui.jsf.component.CheckboxGroup;
import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * The {@code CheckboxGroupRenderer} renders a {@code CheckboxGroup}
 * component as set of checkboxes. The {@code CheckboxGroupRenderer}
 * creates an instance of {@code Checkbox} for each
 * {@code Option} instance in the {@code Array}, {@code Map}, or
 * {@code Collection} returned by the {@code CheckboxGroup}
 * {@code getItems()} method and renders them. It also creates
 * a
 * {@code Label} component and renders it as the label for the group.
 * <p>
 * Zero or more checkboxes may be selected. The value of the
 * {@code CheckboxGroup} will determine which checkboxes shall be initially
 * selected and subsequetly hold the current selections.
 * </p>
 * <p>
 * The checkboxes are rendered as a single column or some number of rows and
 * columns. The rows and columns are rendered as a table as defined by the
 * {@link com.sun.webui.jsf.renderkit.html.RowColumnRenderer} superclass. The
 * elements that make up the checkbox occupy a cell in the table. The style
 * class selector for the group elements is identified by a java
 * constants defined in the {@link com.sun.webui.jsf.theme.ThemeStyles} class.
 * </p>
 * <ul>
 * <li>CHECKBOX_GROUP for the TABLE element.</li>
 * <li>CHECKBOX_GROUP_CAPTION for the TD element containing the group label</li>
 * <li>CHECKBOX_GROUP_LABEL for the LABEL element used as the CAPTION</li>
 * <li>CHECKBOX_GROUP_LABEL_DISABLED for the LABEL used as the CAPTION if the
 * group is disabled</li>
 * <li>CHECKBOX_GROUP_ROW_EVEN for even TR elements</li>
 * <li>CHECKBOX_GROUP_ROW_ODD for odd TR elements</li>
 * <li>CHECKBOX_GROUP_CELL_EVEN for even TD elements</li>
 * <li>CHECKBOX_GROUP_CELL_ODD for odd TD elements</li>
 * <li>CHECKBOX for the INPUT element</li>
 * <li>CHECKBOX_DISABLED for the INPUT element for disabled checkbox</li>
 * <li>CHECKBOX_LABEL for a LABEL element of a checkbox</li>
 * <li>CHECKBOX_LABEL_DISABLED for a LABEL element of a disabled checkbox</li>
 * <li>CHECKBOX_IMAGE for an IMG element of a checkbox</li>
 * <li>CHECKBOX_IMAGE_DISABLED for an IMG element of a disabled checkbox</li>
 * </ul>
 * <p>
 * The {@code name} property of each checkbox is the component id of the
 * {@code CheckboxGroup} instance. The id of a {@code Checkbox}
 * component is <em>cbgrpid_N</em> where <em>cbgrpid</em> is the id of the
 * {@code CheckboxGroup} instance and <em>_N</em> is the nth checkbox.
 * </p>
 * <p>
 * The {@code CheckboxGroup} is decoded by identifying the
 * {@code CheckboxGroup} instance component id which is
 * returned as a request parameter. It represents the name attribute
 * of the selected checkbox's &lt;input&gt; element. The values of the
 * identified request parameter are assigned as the submitted value of the
 * {@code CheckboxGroup} component.
 * </p>
 * <p>
 * If the items property of the {@code CheckboxGroup} is null or
 * zero length, no output is produced.
 * </p>
 */
@Renderer(@Renderer.Renders(
        componentFamily = "com.sun.webui.jsf.CheckboxGroup"))
//FIXME check about making SelectGroupRenderer a public abstract class
public class CheckboxGroupRenderer extends SelectorGroupRenderer {

    /**
     * Error message for component not a check-box group type.
     */
    private static final String MSG_COMPONENT_NOT_CHECKBOXGROUP =
     "CheckboxGroupRenderer only renders CheckboxGroup components.";

    /**
     * The style constants defined in
     * {@link com.sun.webui.jsf.theme.ThemeStyles} mapped to the value of
     * constants defined in
     * {@link com.sun.webui.jsf.renderkit.html.SelectorGroupRenderer}.
     */
    private static final String[] STYLES = {
        ThemeStyles.CHECKBOX_GROUP, /* GRP */
        ThemeStyles.CHECKBOX_GROUP_CAPTION, /* GRP_CAPTION */
        ThemeStyles.CHECKBOX_GROUP_LABEL, /* GRP_LABEL */
        ThemeStyles.CHECKBOX_GROUP_LABEL_DISABLED, /* GRP_LABEL_DIS */
        ThemeStyles.CHECKBOX_GROUP_ROW_EVEN, /* GRP_ROW_EVEN */
        ThemeStyles.CHECKBOX_GROUP_ROW_ODD, /* GRP_ROW_EVEN */
        ThemeStyles.CHECKBOX_GROUP_CELL_EVEN, /* GRP_CELL_EVEN */
        ThemeStyles.CHECKBOX_GROUP_CELL_ODD, /* GRP_CELL_ODD */
        ThemeStyles.CHECKBOX, /* INPUT */
        ThemeStyles.CHECKBOX_DISABLED, /* INPUT_DIS */
        ThemeStyles.CHECKBOX_LABEL, /* LABEL */
        ThemeStyles.CHECKBOX_LABEL_DISABLED, /* LABEL_DIS */
        ThemeStyles.CHECKBOX_IMAGE, /* IMAGE */
        ThemeStyles.CHECKBOX_IMAGE_DISABLED, /* IMAGE_DIS */
        ThemeStyles.LABEL_LEVEL_ONE_TEXT, /* LABEL_LVL1 */
        ThemeStyles.LABEL_LEVEL_TWO_TEXT, /* LABEL_LVL2 */
        ThemeStyles.LABEL_LEVEL_THREE_TEXT /* LABLE_LVL3 */
    };

    /**
     * Creates a new instance of CheckboxGroupRenderer.
     */
    public CheckboxGroupRenderer() {
        super();
    }

    /**
     * Ensure that the component to be rendered is a CheckboxGroup instance.
     * Actual rendering occurs during the {@code renderEnd} method.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     * @throws IOException if an IO error occurs
     */
    @Override
    public void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // Bail out if the component is not a CheckboxGroup component.
        if (!(component instanceof CheckboxGroup)) {
            throw new IllegalArgumentException(
                    MSG_COMPONENT_NOT_CHECKBOXGROUP);
        }
    }

    /**
     * CheckboxGroupRenderer renders the entire CheckboxGroup
     * component within the renderEnd method.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     * @throws IOException if an IO error occurs
     */
    @Override
    public void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // Use only the cols value. If not valid render a single column.
        // If there are more items than columns, render additional rows.
        CheckboxGroup cbgrp = (CheckboxGroup) component;

        Theme theme = ThemeUtilities.getTheme(context);
        renderSelectorGroup(context, component, theme,
                writer, cbgrp.getColumns());
    }

    /**
     * Return a Checkbox component to render.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code CheckboxGroup} component rendered
     * @param theme {@code Theme} for the component
     * @param id component id
     * @param option the {@code Option} being rendered.
     * @return UIComponent
     */
    @Override
    protected UIComponent getSelectorComponent(final FacesContext context,
            final UIComponent component, final Theme theme, final String id,
            final Option option) {

        CheckboxGroup cbgrp = (CheckboxGroup) component;

        Checkbox cb = new Checkbox();
        cb.setId(id);
        cb.setParent(cbgrp);

        cb.setName(cbgrp.getClientId(context));
        cb.setToolTip(option.getTooltip());
        cb.setImageURL(option.getImage());
        cb.setSelectedValue(option.getValue());
        cb.setLabel(option.getLabel());
        cb.setDisabled(cbgrp.isDisabled());
        cb.setReadOnly(cbgrp.isReadOnly());
        cb.setTabIndex(cbgrp.getTabIndex());

        // mbohm 6300361,6300362
        // transfer event attributes from cbgrp to cb
        // see RowColumnRenderer.renderRowColumnLayout
        transferEventAttributes(cbgrp, cb);

        // Default to not selected
        //
        cb.setSelected(null);

        // Need to check the submittedValue for immediate condition
        //
        String[] subValue = (String[]) cbgrp.getSubmittedValue();
        if (subValue == null) {
            if (isSelected(option, cbgrp.getSelected())) {
                cb.setSelected(cb.getSelectedValue());
            }
        } else if (subValue.length != 0) {
            Object selectedValue = cb.getSelectedValue();
            String selectedValueAsString =
                    ConversionUtilities.convertValueToString(component,
                    selectedValue);
            for (int i = 0; i < subValue.length; ++i) {
                if (subValue[i] != null
                        && subValue[i].equals(selectedValueAsString)) {
                    cb.setSelected(cb.getSelectedValue());
                    break;
                }
            }
        }

        return cb;
    }

    /**
     * Return true if the {@code item} argument is the currently
     * selected checkbox. Equality is determined by the {@code equals}
     * method of the object instance stored as the {@code value} of
     * {@code item}. Return false otherwise.
     *
     * @param item the current checkbox being rendered.
     * @param currentValue the value of the current selected checkbox.
     * @return {@code boolean}
     */
    private boolean isSelected(final Option item, final Object currentValue) {
        // How is the selected value determined ?
        // Is it the Selection value on CheckboxGroup or
        // the boolean value on the current Selection being processed ?
        //
        Object value = item.getValue();
        if (value == null || currentValue == null) {
            return false;
        }
        if (currentValue instanceof Map) {
            return ((Map) currentValue).containsValue(value);
        } else if (currentValue instanceof Collection) {
            return ((Collection) currentValue).contains(value);
        } else if (currentValue instanceof Object[]) {
            Object[] selectedValues = (Object[]) currentValue;
            for (int i = 0; i < selectedValues.length; ++i) {
                if (value.equals(selectedValues[i])) {
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * Return style constants for a {@code Checkbox} component.
     * @return String[]
     */
    @Override
    protected String[] getStyles() {
        return STYLES;
    }
}
