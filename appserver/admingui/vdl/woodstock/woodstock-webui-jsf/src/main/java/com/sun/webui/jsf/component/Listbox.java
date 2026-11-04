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

/**
 * The Listbox component allows users to select one or more items from a list.
 */
@Component(type = "com.sun.webui.jsf.Listbox",
        family = "com.sun.webui.jsf.Listbox",
        displayName = "Listbox", tagName = "listbox",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_listbox",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_listbox_props")
        //CHECKSTYLE:ON
public final class Listbox extends ListSelector {

    /**
     * When set to true, this attribute causes the list items to be rendered in
     * a monospace font.
     */
    @Property(name = "monospace",
            displayName = "Use Monospace Space",
            category = "Appearance")
    private boolean monospace = false;

    /**
     * monospace set flag.
     */
    private boolean monospaceSet = false;

    /**
     * Flag indicating that the application user can make select more than one
     * option at a time from the list-box.
     */
    @Property(name = "multiple", displayName = "Multiple", category = "Data")
    private boolean multiple = false;

    /**
     * multiple set flag.
     */
    private boolean multipleSet = false;

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool-tip if the mouse cursor hovers over the HTML
     * element.
     */
    @Property(name = "toolTip",
            displayName = "Tool Tip",
            category = "Behavior")
    private String toolTip = null;

    /**
     * Default constructor.
     */
    public Listbox() {
        super();
        setRendererType("com.sun.webui.jsf.Listbox");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Listbox";
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int getRows() {
        int rows = super.getRows();
        if (rows < 1) {
            rows = 12;
            super.setRows(rows);
        }
        return rows;
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
     * Get the monospace flag value.
     * @return {@code boolean}
     */
    public boolean isMonospace() {
        if (this.monospaceSet) {
            return this.monospace;
        }
        ValueExpression vb = getValueExpression("monospace");
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
     * When set to true, this attribute causes the list items to be rendered in
     * a monospace font.
     *
     * @see #isMonospace()
     * @param newMonospace monospace
     */
    public void setMonospace(final boolean newMonospace) {
        this.monospace = newMonospace;
        this.monospaceSet = true;
    }

    @Override
    public boolean isMultiple() {
        if (this.multipleSet) {
            return this.multiple;
        }
        ValueExpression vb = getValueExpression("multiple");
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

    @Override
    public void setMultiple(final boolean newMultiple) {
        this.multiple = newMultiple;
        this.multipleSet = true;
    }

    @Override
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

    @Override
    public void setToolTip(final String newToolTip) {
        this.toolTip = newToolTip;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.monospace = ((Boolean) values[1]);
        this.monospaceSet = ((Boolean) values[2]);
        this.multiple = ((Boolean) values[3]);
        this.multipleSet = ((Boolean) values[4]);
        this.toolTip = (String) values[5];
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[6];
        values[0] = super.saveState(context);
        if (this.monospace) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.monospaceSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.multiple) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.multipleSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        values[5] = this.toolTip;
        return values;
    }
}
