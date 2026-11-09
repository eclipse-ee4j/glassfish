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
import com.sun.faces.extensions.avatar.lifecycle.AsyncResponse;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Component that represents a group of table rows.
 */
@Component(type = "com.sun.webui.jsf.Table2RowGroup",
        family = "com.sun.webui.jsf.Table2RowGroup",
        tagRendererType = "com.sun.webui.jsf.widget.Table2RowGroup",
        // Remove isTag to run
        displayName = "Table2RowGroup",
        tagName = "table2RowGroup",
        isTag = false)
public final class Table2RowGroup extends TableRowGroup
        implements NamingContainer, Widget {

    /**
     * A List containing Table2Column children.
     */
    private List<Table2Column> table2ColumnChildren;

    /**
     * Flag indicating to turn off default Ajax functionality. Set
     * {@code ajaxify} to false when providing a different Ajax implementation.
     */
    @Property(name = "ajaxify",
            displayName = "Ajaxify",
            category = "Javascript")
    private boolean ajaxify = true;

    /**
     * ajaxify set flag.
     */
    private boolean ajaxifySet;

    /**
     * Default constructor.
     */
    public Table2RowGroup() {
        super();
        setRendererType("com.sun.webui.jsf.widget.Table2RowGroup");
    }

    /**
     * Get the faces context.
     * @return FacesContext
     */
    public FacesContext getContext() {
        return getFacesContext();
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Table2RowGroup";
    }

    @Override
    public String getRendererType() {
        if (AsyncResponse.isAjaxRequest()) {
            return "com.sun.webui.jsf.ajax.Table2RowGroup";
        } else {
            return super.getRendererType();
        }
    }

    @Override
    public String getWidgetType() {
        return "table2RowGroup";
    }

    /**
     * Get an Iterator over the Table2Column children found for this component.
     *
     * @return An Iterator over the Table2Column children.
     */
    public Iterator getTable2ColumnChildren() {
        if (table2ColumnChildren == null) {
            table2ColumnChildren = new ArrayList<Table2Column>();
            for (UIComponent kid : getChildren()) {
                if ((kid instanceof Table2Column)) {
                    table2ColumnChildren.add((Table2Column) kid);
                }
            }
        }
        return table2ColumnChildren.iterator();
    }

    /**
     * Get the number of rows to be displayed for a paginated table.
     * <p>
     * Note: UI guidelines recommend a default value of 25 rows per page.
     * </p>
     *
     * @return The number of rows to be displayed for a paginated table.
     */
    @Override
    public int getRows() {
        setPaginated(true);
        return Math.max(1, super.getRows());
    }

    /**
     * Test if default Ajax functionality should be turned off.
     *
     * @return {@code boolean}
     */
    public boolean isAjaxify() {
        if (this.ajaxifySet) {
            return this.ajaxify;
        }
        ValueExpression vb = getValueExpression("ajaxify");
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

    /**
     * Set flag indicating to turn off default Ajax functionality.
     *
     * @param newAjaxify ajaxify
     */
    public void setAjaxify(final boolean newAjaxify) {
        this.ajaxify = newAjaxify;
        this.ajaxifySet = true;
    }
}
