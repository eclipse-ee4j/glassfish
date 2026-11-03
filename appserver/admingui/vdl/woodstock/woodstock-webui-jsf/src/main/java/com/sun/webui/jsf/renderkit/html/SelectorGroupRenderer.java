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

import com.sun.webui.jsf.component.Label;
import com.sun.webui.jsf.component.RbCbSelector;
import com.sun.webui.jsf.component.Selector;
import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * The {@code SelectorGroupRenderer} is the abstract base class for
 * {@link RadioButtonGroupRenderer} and {@link CheckboxGroupRenderer}. It
 * provides common behavior for driving the {@link RowColumnRenderer}
 * superclass. Its {@code decode} method provides decoding for both
 * {@code CheckboxGroup} and {@code RadioButtonGroup} components. It
 * decodes the value as a {@code String[]} for both components. For
 * {@code CheckboxGroup} components this an array of possibly 0 or more
 * elements. For the {@code RadioButtonGroup} component the value is
 * decoded as a {@code String[]} of at least one element.
 */
//FIXME this should probably be a public or protected abstract class
abstract class SelectorGroupRenderer extends RowColumnRenderer {

    /**
     * The define constant indicating the style class for the top level TABLE
     * element.
     */
    protected static final int GRP = 0;

    /**
     * The define constant indicating the style class for the CSS table CAPTION
     * (a CELL element).
     */
    protected static final int GRP_CAPTION = 1;

    /**
     * The define constant indicating the style class for a disabled CSS table
     * CAPTION (a LABEL) element.
     */
    protected static final int GRP_LABEL = 2;

    /**
     * The define constant indicating the style class for a disabled CSS table
     * CAPTION (a LABEL) element.
     */
    protected static final int GRP_LABEL_DIS = 3;

    /**
     * The define constant indicating the style class for the even rows.
     */
    protected static final int GRP_ROW_EVEN = 4;

    /**
     * The define constant indicating the style class for the odd rows.
     */
    protected static final int GRP_ROW_ODD = 5;

    /**
     * The define constant indicating the style class for the even cells.
     */
    protected static final int GRP_CELL_EVEN = 6;

    /**
     * The define constant indicating the style class for the odd cells.
     */
    protected static final int GRP_CELL_ODD = 7;

    /**
     * The define constant indicating the style class for an INPUT element.
     */
    protected static final int INPUT = 8;

    /**
     * The define constant indicating the style class for a disabled INPUT
     * element.
     */
    protected static final int INPUT_DIS = 9;

    /**
     * The define constant indicating the style class for the LABEL element.
     */
    protected static final int LABEL = 10;

    /**
     * The define constant indicating the style class for a disabled LABEL
     * element.
     */
    protected static final int LABEL_DIS = 11;

    /**
     * The define constant indicating the style class for the IMG element.
     */
    protected static final int IMAGE = 12;

    /**
     * The define constant indicating the style class for a disabled IMG
     * element.
     */
    protected static final int IMAGE_DIS = 13;

    /**
     * The define constant indicating the label level style class for a LABEL
     * element.
     */
    protected static final int LABEL_LVL1 = 14;

    /**
     * The define constant indicating the label level style class for a LABEL
     * element.
     */
    protected static final int LABEL_LVL2 = 15;

    /**
     * The define constant indicating the label level style class for a LABEL
     * element.
     */
    protected static final int LABEL_LVL3 = 16;

    /**
     * The define constant indicating the default label level style class for a
     * LABEL element.
     */
    protected static final int LABEL_LVL_DEF = 0;

    /**
     * Structural styles.
     */
    private static final int[] ROW_COL_STYLE = {
        GRP, GRP_CAPTION, GRP_ROW_EVEN, GRP_ROW_ODD,
        GRP_CELL_EVEN, GRP_CELL_ODD
    };

    /**
     * Creates a new instance of SelectorGroupRenderer.
     */
    SelectorGroupRenderer() {
        super();
    }

    /**
     * Return style constants for the controls in the group. The getStyles
     * method is implemented by subclasses to return a {@code String[]} of
     * style constants as defined in {@code ThemeStyles} in an order
     * defined by the constants in this class.
     * @return String[]
     */
    protected abstract String[] getStyles();

    /**
     * Implemented in the subclass to return the UIComponent for a control in
     * the group.
     *
     * @param context FacesContext for the request we are processing.
     * @param component The RadioButtonGroup component to be decoded.
     * @param theme Theme for the request we are processing.
     * @param id the new component's id.
     * @param option the {@code Option} being rendered.
     * @return UIComponent
     */
    protected abstract UIComponent getSelectorComponent(FacesContext context,
            UIComponent component, Theme theme, String id, Option option);

    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        if (context == null || component == null) {
            throw new NullPointerException();
        }

        if (isDisabled(component) || isReadOnly(component)) {
            return;
        }

        setSubmittedValues(context, component);
    }

    /**
     * Set the submitted values.
     * @param context faces context
     * @param component UI component
     */
    private void setSubmittedValues(final FacesContext context,
            final UIComponent component) {

        String clientId = component.getClientId(context);

        Map requestParameterValuesMap = context.getExternalContext().
                getRequestParameterValuesMap();

        // If the clientId is found some controls are checked
        //
        if (requestParameterValuesMap.containsKey(clientId)) {
            String[] newValues = (String[])
                    requestParameterValuesMap.get(clientId);

            ((UIInput) component).setSubmittedValue(newValues);
            return;
        }
        // Return if there are no disabledCheckedValues and there
        // were no controls checked
        ((UIInput) component).setSubmittedValue(new String[0]);
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {
    }

    /**
     * Called from the renderEnd method of the subclass to begin rendering the
     * component.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     * @param theme the current theme
     * @param writer {@code ResponseWriter} to which the HTML will be output
     * @param columns the number of columns to use when rendering the controls
     * @throws IOException if an IO error occurs
     */
    protected void renderSelectorGroup(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final int columns)
            throws IOException {

        // If there are more items than columns, render additional rows.
        Selector selector = (Selector) component;

        // See if we are rendering null for nothing selected
        Object selected = selector.getSelected();

        // If the submittedValue is null record the rendered value
        // If the submittedValue is not null then it contains the
        // String values of the selected controls.
        // If the submittedValue is not null but zero in length
        // then nothing is selected. Assume that the component still
        // has the appropriate rendered state.
        if (selector.getSubmittedValue() == null) {
            ConversionUtilities.setRenderedValue(component, selected);
        }

        // If there aren't any items don't render anything
        Option[] items = getItems((Selector) component);
        if (items == null) {
            return;
        }
        int length = items.length;
        if (length == 0) {
            return;
        }

        int cols;
        if (columns <= 0) {
            cols = 1;
        } else if (columns > length) {
            cols = length;
        } else {
            cols = columns;
        }
        int rows = (length + (cols - 1)) / cols;

        // Render the table layout
        renderRowColumnLayout(context, component, theme,
                writer, rows, cols);
    }

    /**
     * Get the items.
     * @param selector selector
     * @return Option[]
     */
    @SuppressWarnings("unchecked")
    protected Option[] getItems(final Selector selector) {
        Object items = selector.getItems();
        if (items == null) {
            return null;
        } else if (items instanceof Option[]) {
            return (Option[]) items;
        } else if (items instanceof Map) {
            int size = ((Map) items).size();
            return (Option[]) ((Map) items).values().toArray(new Option[size]);
        } else if (items instanceof Collection) {
            int size = ((Collection) items).size();
            return (Option[]) ((Collection) items).toArray(new Option[size]);
        } else {
            throw new IllegalArgumentException(
                    "Selector.items is not Option[], Map, or Collection");
        }
    }

    @Override
    protected void renderCellContent(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final int itemN) throws IOException {

        Option[] items = getItems((Selector) component);
        if (itemN >= items.length) {
            renderEmptyCell(context, component, theme, writer);
            return;
        }

        String id = component.getId().concat("_") + itemN;
        UIComponent content = getSelectorComponent(context, component,
                theme, id, items[itemN]);
        RenderingUtilities.renderComponent(content, context);
    }

    @Override
    protected void renderCaption(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        UIComponent captionComponent = getCaptionComponent(context,
                component, theme, component.getId().concat("_caption"));
        if (captionComponent != null) {
            RenderingUtilities.renderComponent(captionComponent, context);
        }
    }

    /**
     * Get caption component.
     * @param context faces context
     * @param component UI component
     * @param theme the current theme
     * @param captionId caption id
     * @return UIComponent
     * @throws IOException if an IO error occurs
     */
    private UIComponent getCaptionComponent(final FacesContext context,
            final UIComponent component, final Theme theme,
            final String captionId) throws IOException {

        // Check if the page author has defined a label facet
        //
        // What if the component is readonly ? Do we need to modify
        // the facet to be readonly, disabled, or required ?
        // What about styles, etc.
        UIComponent labelComponent = component.getFacet("label");
        if (labelComponent != null) {
            return labelComponent;
        }

        // If we find a label, define a label component
        String attrvalue
                = (String) component.getAttributes().get("label");
        if (attrvalue == null || attrvalue.length() <= 0) {
            return null;
        }

        // This code should be in the component.
        // But it is more complicated than that since the argument is
        // "UIComponent" and not RadioButtonGroup or CheckboxGroup.
        // Too much needs to be done so leave this way for now until
        // we fix all renderers with similar problems.
        Label label = (Label) ComponentUtilities.getPrivateFacet(component,
                "label", true);
        if (label == null) {
            label = new Label();
            label.setId(ComponentUtilities.createPrivateFacetId(
                    component, "label"));
            ComponentUtilities.putPrivateFacet(component, "label",
                    label);
        }

        label.setText(attrvalue);

        // Set the for attribute. This will eventually resolve to the
        // the first control.
        label.setFor(component.getClientId(context));

        // Give the group's tooltip to the group label
        attrvalue
                = (String) component.getAttributes().get("toolTip");
        if (attrvalue != null) {
            label.setToolTip(attrvalue);
        }

        Integer lblLvl = (Integer) component.getAttributes().get("labelLevel");

        // Need to synch up defaults
        if (lblLvl == null) {
            lblLvl = 2;
        }
        label.setLabelLevel(lblLvl);

        int styleCode = GRP_LABEL;
        Boolean disabled = (Boolean) component.getAttributes().get("disabled");
        if (disabled != null && disabled) {
            styleCode = GRP_LABEL_DIS;
        }

        String captionStyle = getStyle(theme, styleCode);
        if (captionStyle != null) {
            label.setStyleClass(captionStyle);
        }
        return label;
    }

    /**
     * Called from the {@code renderCellContent} method implemented in the
     * sub-class when there are no more controls to render.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     * @param theme Theme for the request we are processing.
     * @param writer {@code ResponseWriter} to which the HTML will be
     * output
     * @throws IOException if an IO exception occurs
     */
    protected void renderEmptyCell(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

    }

    /**
     * This implementation passes on the style request from the
     * {@link com.sun.webui.jsf.renderkit.html.RowColumnRenderer} to the
     * {@code SelectorGroupRenderer} subclass.
     *
     * @param theme Theme for the request we are processing.
     * @param styleCode the desired style class constant
     * @return String
     */
    @Override
    protected final String getRowColumnStyle(final Theme theme,
            final int styleCode) {

        return getStyle(theme, ROW_COL_STYLE[styleCode]);
    }

    /**
     * Return the style class name for the structural element indicated by
     * {@code styleCode}.
     *
     * @param theme the current theme4
     * @param styleCode identifies the style class for the element about to be
     * rendered.
     * @return String
     */
    private String getStyle(final Theme theme, final int styleCode) {
        String style = null;
        try {
            style = theme.getStyleClass(getStyles()[styleCode]);
        } catch (Exception e) {
            // Don't care
        }
        return style;
    }

    /**
     * Return the style class name and level for the structural element
     * indicated by {@code styleCode}.
     *
     * @param styleCode identifies the style class for the element about to be
     * rendered.
     * @param styleLevelCode identifies the style class level for the element
     * about to be rendered.
     * @param theme the current theme
     * @return String
     */
    protected String getStyle(final Theme theme, final int styleCode,
            final int styleLevelCode) {

        String style = getStyle(theme, styleCode);
        if (style == null) {
            return null;
        }

        StringBuilder styleBuf = new StringBuilder(style);
        String styleLevel = null;
        if (styleLevelCode != LABEL_LVL_DEF) {
            styleLevel = getStyle(theme, styleLevelCode);
        }

        // No style code for the desired one, get the default
        if (styleLevel != null) {
            if (styleBuf.length() != 0) {
                styleBuf.append(" ");
            }
            styleBuf.append(styleLevel);
        } else {
            switch (styleCode) {
                case GRP_CAPTION:
                    style = getStyle(theme, LABEL_LVL2);
                    break;
                case LABEL:
                    style = getStyle(theme, LABEL_LVL3);
                    break;
                default:
                    style = null;
            }
            if (style != null) {
                if (styleBuf.length() != 0) {
                    styleBuf.append(" ");
                }
                styleBuf.append(style);
            }
        }
        return styleBuf.toString();
    }

    /**
     * Transfer event attributes from a radio-button-group / check-box-group to
     * a radio-button / check-box.
     *
     * @param group group component
     * @param rbcb selector component
     */
    protected void transferEventAttributes(final Selector group,
            final RbCbSelector rbcb) {

        Map<String, Object> groupAttributes = group.getAttributes();
        Map<String, Object> rbcbAttributes = rbcb.getAttributes();
        final String[] eventAttributeNames
                = AbstractRenderer.EVENTS_ATTRIBUTES;
        for (String eventAttributeName : eventAttributeNames) {
            Object eventAttributeValue =
                    groupAttributes.get(eventAttributeName);
            if (eventAttributeValue != null) {
                rbcbAttributes.put(eventAttributeName, eventAttributeValue);
            }
        }
    }
}
