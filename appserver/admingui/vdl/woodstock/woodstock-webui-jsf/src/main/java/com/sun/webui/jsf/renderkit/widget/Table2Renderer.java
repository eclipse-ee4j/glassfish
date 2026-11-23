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

package com.sun.webui.jsf.renderkit.widget;

import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.Table2;
import com.sun.webui.jsf.component.Table2RowGroup;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

import java.io.IOException;
import java.util.Iterator;

import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;
import static com.sun.webui.jsf.util.ThemeUtilities.getTheme;
import static com.sun.webui.jsf.util.WidgetUtilities.renderComponent;

/**
 * This class renders Table2 components.
 */
@Renderer(@Renderer.Renders(rendererType = "com.sun.webui.jsf.widget.Table2",
componentFamily = "com.sun.webui.jsf.Table2"))
public final class Table2Renderer extends RendererBase {

    /**
     * The set of pass-through attributes to be rendered.
     * <p>
     * Note: The BGCOLOR attribute is deprecated (in the HTML 4.0 spec) in favor
     * of style sheets. In addition, the DIR and LANG attributes are not
     * currently supported.
     * </p>
     */
    private static final String[] ATTRIBUTES = {
        "align",
        "bgColor",
        "dir",
        "frame",
        "lang",
        "onClick",
        "onDblClick",
        "onKeyDown",
        "onKeyPress",
        "onKeyUp",
        "onMouseDown",
        "onMouseMove",
        "onMouseOut",
        "onMouseOver",
        "onMouseUp",
        "rules",
        "summary",
    };

    @Override
    protected String[] getModuleNames(final UIComponent component) {
        return new String[]{
            "table2",
        };
    }

    @Override
    protected JsonObjectBuilder getProperties(final FacesContext context,
            final UIComponent component) throws IOException {

        Table2 table = (Table2) component;

        JsonObjectBuilder jsonBuilder = JSON_BUILDER_FACTORY
                .createObjectBuilder();

        // Add properties.
        addAttributeProperties(ATTRIBUTES, table, jsonBuilder);
        setRowGroupProperties(context, table, jsonBuilder);
        setActionsProperties(context, table, jsonBuilder);
        setTitleProperties(context, table, jsonBuilder);
        return jsonBuilder;
    }

    @Override
    protected void renderNestedContent(final FacesContext context,
            final UIComponent component) throws IOException {
    }

    /**
     * Helper method to obtain actions properties.
     *
     * @param context FacesContext for the current request.
     * @param component Table2 to be rendered.
     * @param jsonBuilder JSONObject to assign properties to.
     * @throws java.io.IOException if an IO error occurs
     */
    private static void setActionsProperties(final FacesContext context,
            final Table2 component, final JsonObjectBuilder jsonBuilder)
            throws IOException {

        // Get actions facet.
        UIComponent facet = component.getFacet(Table2.ACTIONS_TOP_FACET);
        if (facet != null && facet.isRendered()) {
            jsonBuilder.add("actions", renderComponent(context, facet));
        }
    }

    /**
     * Helper method to obtain row group properties.
     *
     * @param context FacesContext for the current request.
     * @param component Table2 to be rendered.
     * @param jsonBuilder JSONObject to assign properties to.
     * @throws java.io.IOException if an IO error occurs
     */
    private static void setRowGroupProperties(final FacesContext context,
            final Table2 component, final JsonObjectBuilder jsonBuilder)
            throws IOException {

        // Add properties for each Table2RowGroup child.
        JsonArrayBuilder jsonArrayBuilder = JSON_BUILDER_FACTORY
                .createArrayBuilder();
        Iterator kids = component.getTable2RowGroupChildren();
        while (kids.hasNext()) {
            Table2RowGroup group = (Table2RowGroup) kids.next();
            if (group.isRendered()) {
                jsonArrayBuilder.add(renderComponent(context, group));
            }
        }
        jsonBuilder.add("rowGroups", jsonArrayBuilder);
    }

    /**
     * Helper method to obtain title properties.
     *
     * @param context FacesContext for the current request.
     * @param component Table2 to be rendered.
     * @throws java.io.IOException if an IO error occurs
     * @param jsonBuilder JSONObject to assign properties to.
     */
    private static void setTitleProperties(final FacesContext context,
            final Table2 component, final JsonObjectBuilder jsonBuilder)
            throws IOException {

        // Get facet.
        UIComponent facet = component.getFacet(Table2.TABLE2_TITLE_FACET);
        if (facet != null) {
            jsonBuilder.add("title", renderComponent(context, facet));
            return;
        }

        // Get filter argument.
        JsonValue filterText;
        if (component.getFilterText() != null) {
            filterText = Json.createValue(getTheme(context)
                    .getMessage("table.title.filterApplied",
                            new String[]{
                                component.getFilterText(),
                            }));
        } else {
            filterText = JsonValue.NULL;
        }

        // Append component properties.
        jsonBuilder.add("title", component.getTitle())
                .add("filterText", filterText);
    }
}
