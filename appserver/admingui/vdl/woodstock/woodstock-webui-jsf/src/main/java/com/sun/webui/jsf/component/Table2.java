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

import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Component that represents a table.
 */
@Component(type = "com.sun.webui.jsf.Table2",
        family = "com.sun.webui.jsf.Table2",
        tagRendererType = "com.sun.webui.jsf.widget.Table2",
        // Remove isTag to run
        displayName = "Table2", tagName = "table2", isTag = false)
public final class Table2 extends Table implements NamingContainer, Widget {

    /**
     * The facet name for the actions area.
     */
    public static final String ACTIONS_FACET = "actions";

    /**
     * The facet name for the title area.
     */
    public static final String TABLE2_TITLE_FACET = "title";

    /**
     * A List containing Table2RowGroup children.
     */
    private List<Table2RowGroup> table2RowGroupChildren;

    /**
     * Default constructor.
     */
    public Table2() {
        super();
        setRendererType("com.sun.webui.jsf.widget.Table2");
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
        return "com.sun.webui.jsf.Table2";
    }

    @Override
    public String getWidgetType() {
        return "table2";
    }

    /**
     * Get an Iterator over the Table2RowGroup children found for this
     * component.
     *
     * @return An Iterator over the Table2RowGroup children.
     */
    public Iterator getTable2RowGroupChildren() {
        // Get TableRowGroup children.
        if (table2RowGroupChildren == null) {
            table2RowGroupChildren = new ArrayList<Table2RowGroup>();
            for (UIComponent kid : getChildren()) {
                if ((kid instanceof Table2RowGroup)) {
                    table2RowGroupChildren.add((Table2RowGroup) kid);
                }
            }
        }
        return table2RowGroupChildren.iterator();
    }
}
