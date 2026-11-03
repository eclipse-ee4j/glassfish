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
import com.sun.webui.jsf.component.RadioButtonGroup;
import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * The {@code RadioButtonRenderer} renders a {@code RadioButtonGroup}
 * component as set of radio buttons. The {@code RadioButtonGroupRenderer}
 * creates an instance of {@code RadioButton} for each {@code Option}
 * instance in the {@code Array}, {@code Map}, or
 * {@code Collection} returned by the {@code RadioButtonGroup}
 * {@code getItems()} method and renders them. It also creates a
 * {@code Label} component and renders it as the label for the group.
 * <p>
 * Only one radio button may be selected at any time and one radio button must
 * always be selected. The value of the {@code RadioButtonGroup} will
 * determine which radio button shall be initially selected. Subsequently, the
 * {@code RadioButtonGroup}'s value holds the currently selected radio
 * button value.
 * </p>
 * <p>
 * The radio buttons are rendered as a single column or some number of rows and
 * columns. The rows and columns are rendered as a table as defined by the
 * {@link com.sun.webui.jsf.renderkit.html.RowColumnRenderer} superclass. The
 * elements that make up the radio button occupy a cell in the table. The style
 * class selector for the group elements is identified by a java constants
 * defined in the {@link com.sun.webui.jsf.theme.ThemeStyles} class.
 * </p>
 * <ul>
 * <li>RADIOBUTTON_GROUP for the TABLE element.</li>
 * <li>RADIOBUTTON_GROUP_CAPTION for the TD element containing the group
 * label</li>
 * <li>RADIOBUTTON_GROUP_LABEL for the LABEL element used as the CAPTION</li>
 * <li>RADIOBUTTON_GROUP_LABEL_DISABLED for the LABEL used as the CAPTION if the
 * group is disabled</li>
 * <li>RADIOBUTTON_GROUP_ROW_EVEN for even TR elements</li>
 * <li>RADIOBUTTON_GROUP_ROW_ODD for odd TR elements</li>
 * <li>RADIOBUTTON_GROUP_CELL_EVEN for even TD elements</li>
 * <li>RADIOBUTTON_GROUP_CELL_ODD for odd TD elements</li>
 * <li>RADIOBUTTON for the INPUT element</li>
 * <li>RADIOBUTTON_DISABLED for the INPUT element for disabled radio button
 * </li>
 * <li>RADIOBUTTON_LABEL for a LABEL element of a radio button</li>
 * <li>RADIOBUTTON_LABEL_DISABLED for a LABEL element of a disabled radio
 * button</li>
 * <li>RADIOBUTTON_IMAGE for an IMG element of a radio button</li>
 * <li>RADIOBUTTON_IMAGE_DISABLED for an IMG element of a disabled radio
 * button</li>
 * </ul>
 * <p>
 * The {@code name} property of each radio button is the component id of
 * the {@code RadioButtonGroup} instance. The id of a
 * {@code RadioButton} component is <em>rbgrpid_N</em> where
 * <em>rbgrpid</em> is the id of the {@code RadioButtonGroup} instance and
 * <em>_N</em> is the nth radio button.
 * </p>
 * <p>
 * The {@code RadioButtonGroup} is decoded by identifying the
 * {@code RadioButtonGroup} instance component id which is returned as a
 * request parameter. It represents the name attribute of the selected radio
 * button's &lt;input&gt; element. The value of the identified request parameter
 * is assigned as the submitted value of the {@code RadioButtonGroup}
 * component.
 * </p>
 * <p>
 * If the items property of the {@code RadioButtonGroup} is null or zero
 * length no output is produced.
 * </p>
 *
 */
@Renderer(@Renderer.Renders(
                componentFamily = "com.sun.webui.jsf.RadioButtonGroup"))
//FIXME check about making SelectGroupRenderer a public abstract class
public final class RadioButtonGroupRenderer extends SelectorGroupRenderer {

    /**
     * Error message when wrong component type is being rendered.
     */
    private static final String MSG_COMPONENT_NOT_RADIOBUTTONGROUP
            = "RadioButtonGroupRender only renders RadioButtonGroup"
            + " components.";

    /**
     * CSS styles.
     */
    private static final String[] STYLES = {
        ThemeStyles.RADIOBUTTON_GROUP, /* GRP */
        ThemeStyles.RADIOBUTTON_GROUP_CAPTION, /* GRP_CAPTION */
        ThemeStyles.RADIOBUTTON_GROUP_LABEL, /* GRP_LABEL */
        ThemeStyles.RADIOBUTTON_GROUP_LABEL_DISABLED, /* GRP_CAPTION_DIS */
        ThemeStyles.RADIOBUTTON_GROUP_ROW_EVEN, /* GRP_ROW_EVEN */
        ThemeStyles.RADIOBUTTON_GROUP_ROW_ODD, /* GRP_ROW_EVEN */
        ThemeStyles.RADIOBUTTON_GROUP_CELL_EVEN, /* GRP_CELL_EVEN */
        ThemeStyles.RADIOBUTTON_GROUP_CELL_ODD, /* GRP_CELL_ODD */
        ThemeStyles.RADIOBUTTON, /* INPUT */
        ThemeStyles.RADIOBUTTON_DISABLED, /* INPUT_DIS */
        ThemeStyles.RADIOBUTTON_LABEL, /* LABEL */
        ThemeStyles.RADIOBUTTON_LABEL_DISABLED, /* LABEL_DIS */
        ThemeStyles.RADIOBUTTON_IMAGE, /* IMAGE */
        ThemeStyles.RADIOBUTTON_IMAGE_DISABLED, /* IMAGE_DIS */
        ThemeStyles.LABEL_LEVEL_ONE_TEXT, /* LABEL_LVL1 */
        ThemeStyles.LABEL_LEVEL_TWO_TEXT, /* LABEL_LVL2 */
        ThemeStyles.LABEL_LEVEL_THREE_TEXT /* LABLE_LVL3 */
    };

    /**
     * Creates a new instance of RadioButtonGroupRenderer.
     */
    public RadioButtonGroupRenderer() {
        super();
    }

    @Override
    public void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // Bail out if the component is not a RadioButtonGroup component.
        if (!(component instanceof RadioButtonGroup)) {
            throw new IllegalArgumentException(
                    MSG_COMPONENT_NOT_RADIOBUTTONGROUP);
        }
    }

    @Override
    public void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // Use only the cols value. If not valid render a single column.
        // If there are more items than columns, render additional rows.
        RadioButtonGroup rbgrp = (RadioButtonGroup) component;
        Theme theme = ThemeUtilities.getTheme(context);
        renderSelectorGroup(context, component, theme,
                writer, rbgrp.getColumns());
    }

    @Override
    protected UIComponent getSelectorComponent(final FacesContext context,
            final UIComponent component, final Theme theme, final String id,
            final Option option) {

        RadioButtonGroup rbgrp = (RadioButtonGroup) component;
        RadioButton rb = new RadioButton();
        rb.setId(id);
        rb.setParent(component);
        rb.setName(rbgrp.getClientId(context));
        rb.setToolTip(option.getTooltip());
        rb.setImageURL(option.getImage());
        rb.setSelectedValue(option.getValue());
        rb.setLabel(option.getLabel());
        rb.setDisabled(rbgrp.isDisabled());
        rb.setReadOnly(rbgrp.isReadOnly());
        rb.setTabIndex(rbgrp.getTabIndex());

        // mbohm 6300361,6300362
        // transfer event attributes from cbgrp to cb
        // see RowColumnRenderer.renderRowColumnLayout
        transferEventAttributes(rbgrp, rb);

        // Default to not selected
        rb.setSelected(null);

        // Need to check the submittedValue for immediate condition
        String[] subValue = (String[]) rbgrp.getSubmittedValue();
        if (subValue == null) {
            if (isSelected(option, rbgrp.getSelected())) {
                rb.setSelected(rb.getSelectedValue());
            }
        } else if (subValue.length != 0) {
            Object selectedValue = rb.getSelectedValue();
            String selectedValueAsString
                    = ConversionUtilities.convertValueToString(component,
                            selectedValue);
            if (subValue[0] != null
                    && subValue[0].equals(selectedValueAsString)) {
                rb.setSelected(rb.getSelectedValue());
            }
        }
        return rb;
    }

    /**
     * Return true if the {@code item} argument is the currently selected
     * radio button. Equality is determined by the {@code equals} method of
     * the object instance stored as the {@code value} of
     * {@code item}. Return false otherwise.
     *
     * @param item the current radio button being rendered.
     * @param currentValue the value of the current selected radio button.
     * @return {@code boolean}
     */
    private boolean isSelected(final Option item, final Object currentValue) {
        return currentValue != null
                && item.getValue() != null
                && item.getValue().equals(currentValue);
    }

    @Override
    protected String[] getStyles() {
        return STYLES;
    }
}
