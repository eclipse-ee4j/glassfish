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
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;

/**
 * The RadioButtonGroup component is used to display two or more radio buttons
 * in a grid layout in the rendered HTML page.
 */
@Component(type = "com.sun.webui.jsf.RadioButtonGroup",
        family = "com.sun.webui.jsf.RadioButtonGroup",
        displayName = "Radio Button Group",
        tagName = "radioButtonGroup",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_radiobutton_group",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_radio_button_group_props")
        //CHECKSTYLE:ON
public final class RadioButtonGroup extends Selector
        implements NamingContainer, ComplexComponent {

    /**
     * Defines how many columns may be used to layout the radio buttons. The
     * value must be greater than or equal to one. The default value is one.
     * Invalid values are ignored and the value is set to one.
     */
    @Property(name = "columns",
            displayName = "Columns",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int columns = Integer.MIN_VALUE;

    /**
     * Columns set flag.
     */
    private boolean columnsSet = false;

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
    private boolean visible = false;

    /**
     * visible set flag.
     */
    private boolean visibleSet = false;

    /**
     * Default constructor.
     */
    public RadioButtonGroup() {
        super();
        setMultiple(false);
        setRendererType("com.sun.webui.jsf.RadioButtonGroup");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.RadioButtonGroup";
    }

    @Override
    public String getPrimaryElementID(final FacesContext context) {
        return this.getClientId(context);
    }

    @Override
    public String getLabeledElementId(final FacesContext context) {
        // Return the first radio button id. We don't support children
        // yet and the Renderer creates the radio buttons on the fly
        // But we know what the id  that the renderer creates
        // for the first radio button, hack, certainly.
        //
        return getFirstRbId(context);
    }

    @Override
    public String getFocusElementId(final FacesContext context) {
        // Return the first radio button id. We don't support children
        // yet and the Renderer creates the radio buttons on the fly
        // But we know what the id  that the renderer creates
        // for the first radio button, hack, certainly.
        //
        return getFirstRbId(context);
    }

    /**
     * Get the first radio button id.
     * @param context faces context
     * @return String
     */
    private String getFirstRbId(final FacesContext context) {
        String sb = getClientId(context)
                + String.valueOf(UINamingContainer.getSeparatorChar(context))
                + getId() + "_0";
        return sb;
    }

    // Hide onBlur
    @Property(name = "onBlur", isHidden = true, isAttribute = false)
    @Override
    public String getOnBlur() {
        return super.getOnBlur();
    }

    // Hide onChange
    @Property(name = "onChange", isHidden = true, isAttribute = false)
    @Override
    public String getOnChange() {
        return super.getOnChange();
    }

    // Hide onFocus
    @Property(name = "onFocus", isHidden = true, isAttribute = false)
    @Override
    public String getOnFocus() {
        return super.getOnFocus();
    }

    // Hide onSelect
    @Property(name = "onSelect", isHidden = true, isAttribute = false)
    @Override
    public String getOnSelect() {
        return super.getOnSelect();
    }

    // Hide value
    @Property(name = "value", isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * Defines how many columns may be used to layout the radio buttons. The
     * value must be greater than or equal to one. The default value is one.
     * Invalid values are ignored and the value is set to one.
     * @return int
     */
    public int getColumns() {
        if (this.columnsSet) {
            return this.columns;
        }
        ValueExpression vb = getValueExpression("columns");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return 1;
    }

    /**
     * Defines how many columns may be used to layout the radio buttons. The
     * value must be greater than or equal to one. The default value is one.
     * Invalid values are ignored and the value is set to one.
     *
     * @see #getColumns()
     * @param newColumns columns
     */
    public void setColumns(final int newColumns) {
        this.columns = newColumns;
        this.columnsSet = true;
    }

    @Override
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

    @Override
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.columns = ((Integer) values[1]);
        this.columnsSet = ((Boolean) values[2]);
        this.visible = ((Boolean) values[3]);
        this.visibleSet = ((Boolean) values[4]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[5];
        values[0] = super.saveState(context);
        values[1] = this.columns;
        if (this.columnsSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.visible) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        return values;
    }
}
