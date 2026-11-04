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
import jakarta.faces.context.FacesContext;

/**
 * Component that represents a table column.
 */
@Component(type = "com.sun.webui.jsf.table2Column",
        family = "com.sun.webui.jsf.table2Column",
        tagRendererType = "com.sun.webui.jsf.widget.Table2Column",
        // Remove isTag to run
        displayName = "Table2Column", tagName = "table2Column", isTag = false)
public final class Table2Column extends TableColumn
        implements NamingContainer, Widget {

    /**
     * Default constructor.
     */
    public Table2Column() {
        super();
        setRendererType("com.sun.webui.jsf.widget.Table2Column");
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
        return "com.sun.webui.jsf.Table2Column";
    }

    @Override
    public String getWidgetType() {
        return "table2";
    }
}
