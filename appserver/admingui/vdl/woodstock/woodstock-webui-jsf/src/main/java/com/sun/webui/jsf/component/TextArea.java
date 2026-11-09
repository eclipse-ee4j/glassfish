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
 * The TextArea component is used to create a multiple-line input field for
 * text.
 */
@Component(type = "com.sun.webui.jsf.TextArea",
        family = "com.sun.webui.jsf.TextArea",
        displayName = "Text Area",
        instanceName = "textArea",
        tagName = "textArea",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_text_area",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_text_area_props")
        //CHECKSTYLE:ON
public final class TextArea extends Field {

    /**
     * Number of rows used to render the textarea. You should set a value for
     * this attribute to ensure that it is rendered correctly in all browsers.
     * Browsers vary in the default number of rows used for textarea fields.
     */
    @Property(name = "rows",
            displayName = "Rows",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int rows = Integer.MIN_VALUE;

    /**
     * rows set flag.
     */
    private boolean rowsSet;

    /**
     * Default constructor.
     */
    public TextArea() {
        super();
        setRendererType("com.sun.webui.jsf.TextArea");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.TextArea";
    }

    /**
     * The maximum number of characters that can be entered for this field.
     * @return int
     */
    @Property(name = "maxLength", isHidden = true, isAttribute = true)
    @Override
    public int getMaxLength() {
        return super.getMaxLength();
    }

    /**
     * Number of rows used to render the textarea. You should set a value for
     * this attribute to ensure that it is rendered correctly in all browsers.
     * Browsers vary in the default number of rows used for textarea fields.
     * @return int
     */
    public int getRows() {
        if (this.rowsSet) {
            return this.rows;
        }
        ValueExpression vb = getValueExpression("rows");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return 2;
    }

    /**
     * Number of rows used to render the textarea. You should set a value for
     * this attribute to ensure that it is rendered correctly in all browsers.
     * Browsers vary in the default number of rows used for textarea fields.
     *
     * @see #getRows()
     * @param newRows rows
     */
    public void setRows(final int newRows) {
        this.rows = newRows;
        this.rowsSet = true;
    }

    @Override
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.rows = ((Integer) values[1]);
        this.rowsSet = ((Boolean) values[2]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[3];
        values[0] = super.saveState(context);
        values[1] = this.rows;
        if (this.rowsSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        return values;
    }
}
