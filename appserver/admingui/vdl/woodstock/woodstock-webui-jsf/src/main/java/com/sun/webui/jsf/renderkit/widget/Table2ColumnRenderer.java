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

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.json.JsonObjectBuilder;

import java.io.IOException;

import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;
import static com.sun.webui.jsf.util.WidgetUtilities.renderComponent;

/**
 * This class renders Table2Column components.
 */
@Renderer(@Renderer.Renders(
        rendererType = "com.sun.webui.jsf.widget.Table2Column",
        componentFamily = "com.sun.webui.jsf.Table2Column"))
public final class Table2ColumnRenderer extends RendererBase {

    /**
     * The set of pass-through attributes to be rendered.
     * <p>
     * Note: The WIDTH, HEIGHT, and BGCOLOR attributes are all deprecated (in
     * the HTML 4.0 spec) in favor of style sheets. In addition, the DIR and
     * LANG attributes are not currently supported.
     * </p>
     */
    private static final String[] ATTRIBUTES = {
        "abbr",
        "axis",
        "bgColor",
        "char",
        "charOff",
        "dir",
        "headers",
        "height",
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
        "width"
    };

    @Override
    protected String[] getModuleNames(final UIComponent component) {
        return new String[]{
            "table2RowGroup"
        };
    }

    @Override
    protected JsonObjectBuilder getProperties(final FacesContext context,
            final UIComponent component) throws IOException {

        Table2Column col = (Table2Column) component;
        JsonObjectBuilder jsonBuilder = JSON_BUILDER_FACTORY
                .createObjectBuilder();

        // Add properties.
        addAttributeProperties(ATTRIBUTES, col, jsonBuilder);
        setFooterProperties(context, col, jsonBuilder);
        setHeaderProperties(context, col, jsonBuilder);

        return jsonBuilder;
    }

    @Override
    protected void renderNestedContent(final FacesContext context,
            final UIComponent component) throws IOException {
    }

    /**
     * Helper method to obtain footer properties.
     *
     * @param context FacesContext for the current request.
     * @param component Table2Column to be rendered.
     * @param jsonBuilder JSONObject to assign properties to.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void setFooterProperties(final FacesContext context,
            final Table2Column component, final JsonObjectBuilder jsonBuilder)
            throws IOException {

        // Get footer facet.
        UIComponent facet = component.getFacet(Table2Column.FOOTER_FACET);
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
     * @param component Table2Column to be rendered.
     * @param jsonBuilder JSONObject to assign properties to.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void setHeaderProperties(final FacesContext context,
            final Table2Column component, final JsonObjectBuilder jsonBuilder)
            throws IOException {

        // Get header facet.
        UIComponent facet = component.getFacet(Table2Column.HEADER_FACET);
        if (facet != null && facet.isRendered()) {
            jsonBuilder.add("headerText", renderComponent(context, facet));
        } else {
            // Add header text.
            jsonBuilder.add("headerText", component.getHeaderText());
        }
    }
}
