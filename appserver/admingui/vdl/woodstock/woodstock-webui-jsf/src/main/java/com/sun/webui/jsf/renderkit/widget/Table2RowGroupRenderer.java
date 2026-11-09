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
import com.sun.webui.jsf.component.Table2Column;
import com.sun.webui.jsf.component.Table2RowGroup;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

import java.io.IOException;
import java.util.Iterator;

import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;
import static com.sun.webui.jsf.util.WidgetUtilities.renderComponent;

/**
 * This class renders Table2RowGroup components.
 */
@Renderer(@Renderer.Renders(
        rendererType = "com.sun.webui.jsf.widget.Table2RowGroup",
        componentFamily = "com.sun.webui.jsf.Table2RowGroup"))
public final class Table2RowGroupRenderer extends RendererBase {

    /**
     * The set of pass-through attributes to be rendered.
     * Note: The BGCOLOR attribute is deprecated (in the HTML 4.0 spec) in favor
     * of style sheets. In addition, the DIR and LANG attributes are not
     * currently supported.
     */
    private static final String[] ATTRIBUTES = {
        "align",
        "bgColor",
        "char",
        "charOff",
        "dir",
        "lang",
        "onClick",
        "onDblClick",
        "onKeyDown",
        "onKeyPress",
        "onKeyUp",
        "onMouseDown",
        "onMouseUp",
        "onMouseMove",
        "onMouseOut",
        "onMouseOver",
        "style",
        "valign",
    };

    @Override
    protected String[] getModuleNames(final UIComponent component) {
        Table2RowGroup group = (Table2RowGroup) component;
        if (group.isAjaxify()) {
            return new String[]{
                "table2RowGroup",
                "jsfx/table2RowGroup",
            };
        } else {
            return new String[]{
                "table2RowGroup",
            };
        }
    }

    @Override
    protected JsonObjectBuilder getProperties(final FacesContext context,
            final UIComponent component) throws IOException {

        Table2RowGroup group = (Table2RowGroup) component;

        JsonObjectBuilder jsonBuilder = JSON_BUILDER_FACTORY
                .createObjectBuilder();
        jsonBuilder.add("first", group.getFirst())
                .add("maxRows", group.getRows())
                .add("totalRows", group.getRowCount());

        // Add properties.
        addAttributeProperties(ATTRIBUTES, group, jsonBuilder);
        setColumnProperties(context, group, jsonBuilder);
        setFooterProperties(context, group, jsonBuilder);
        setHeaderProperties(context, group, jsonBuilder);
        return jsonBuilder;
    }

    @Override
    protected void renderNestedContent(final FacesContext context,
            final UIComponent component) throws IOException {
    }

    /**
     * Helper method to obtain column properties.
     *
     * @param context FacesContext for the current request.
     * @param component Table2RowGroup to be rendered.
     * @param jsonBuilder JSONObject to assign properties to.
     * @throws java.io.IOException if an IO error occurs
     */
    private static void setColumnProperties(final FacesContext context,
            final Table2RowGroup component, final JsonObjectBuilder jsonBuilder)
            throws IOException {

        JsonArrayBuilder jsonArrayBuilder = JSON_BUILDER_FACTORY
                .createArrayBuilder();

        // Add properties for each Table2Column child.
        Iterator kids = component.getTable2ColumnChildren();
        while (kids.hasNext()) {
            Table2Column col = (Table2Column) kids.next();
            if (col.isRendered()) {
                jsonArrayBuilder.add(renderComponent(context, col));
            }
        }
        jsonBuilder.add("columns", jsonArrayBuilder);
    }

    /**
     * Helper method to obtain footer properties.
     *
     * @param context FacesContext for the current request.
     * @param component Table2RowGroup to be rendered.
     * @param jsonBuilder JSONObject to assign properties to.
     * @throws java.io.IOException if an IO error occurs
     */
    private static void setFooterProperties(final FacesContext context,
            final Table2RowGroup component, final JsonObjectBuilder jsonBuilder)
            throws IOException {

        // Get footer facet.
        UIComponent facet = component.getFacet(Table2RowGroup.FOOTER_FACET);
        if (facet != null && facet.isRendered()) {
            jsonBuilder.add("footerText", renderComponent(context, facet));
        } else {
            // Add footer text.
            jsonBuilder.add("footerText", component.getFooterText());
        }
    }

    /**
     * Helper method to obtain header properties.
     *
     * @param context FacesContext for the current request.
     * @param component Table2RowGroup to be rendered.
     * @param jsonBuilder JSONObject to assign properties to.
     * @throws java.io.IOException if an IO error occurs
     */
    private static void setHeaderProperties(final FacesContext context,
            final Table2RowGroup component, final JsonObjectBuilder jsonBuilder)
            throws IOException {

        // Get header facet.
        UIComponent facet = component.getFacet(Table2RowGroup.HEADER_FACET);
        if (facet != null && facet.isRendered()) {
            jsonBuilder.add("headerText", renderComponent(context, facet));
        } else {
            // Add header text.
            jsonBuilder.add("headerText", component.getHeaderText());
        }
    }
}
