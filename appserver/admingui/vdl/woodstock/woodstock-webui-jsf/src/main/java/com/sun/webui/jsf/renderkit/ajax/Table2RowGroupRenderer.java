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

package com.sun.webui.jsf.renderkit.ajax;

import com.sun.data.provider.RowKey;
import com.sun.faces.annotation.Renderer;
import com.sun.faces.extensions.avatar.lifecycle.AsyncResponse;
import com.sun.webui.jsf.component.Table2Column;
import com.sun.webui.jsf.component.Table2RowGroup;
import com.sun.webui.jsf.util.WidgetUtilities;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

import java.io.IOException;
import java.util.Iterator;

import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;
import static com.sun.webui.jsf.util.JsonUtilities.parseJsonObject;
import static com.sun.webui.jsf.util.JsonUtilities.writeJsonObject;

/**
 * This class renders Table2RowGroup components.
 */
@Renderer(@Renderer.Renders(
        rendererType = "com.sun.webui.jsf.ajax.Table2RowGroup",
        componentFamily = "com.sun.webui.jsf.Table2RowGroup"))
public final class Table2RowGroupRenderer extends jakarta.faces.render.Renderer {

    @Override
    public void encodeBegin(final FacesContext context,
            final UIComponent component) {

        // Do nothing...
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {

        if (context == null || component == null) {
            throw new NullPointerException();
        }

        // Get first and max rows parameters.
        String xjson = (String) context.getExternalContext().
                getRequestHeaderMap().get(AsyncResponse.XJSON_HEADER);
        if (xjson == null) {
            return;
        }

        JsonObject json = parseJsonObject(xjson);
        Table2RowGroup group = (Table2RowGroup) component;

        // Set first and max rows.
        if (json != null) {
            Integer first = json.getInt("first", -1);
            if (first >= 0) {
                // To do: move to decode method.
                group.setFirst(first);
            }
        }

        // FIXME: Need algorithm to retrieve rows
        int maxRows = group.getRows();
        group.setRows(maxRows * 2);
        JsonArray rows = getRows(context, group);
        group.setRows(maxRows);

        if (rows != null) {
            writeJsonObject(json, context.getResponseWriter());
        }
    }

    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) {

        // Do nothing...
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Helper method to render rows.
     *
     * @param context FacesContext for the current request.
     * @param component UIComponent to be rendered.
     * @return JsonArray
     * @throws IOException if an IO error occurs
     */
    private JsonArray getRows(final FacesContext context,
            final Table2RowGroup component) throws IOException {

        if (context == null || component == null) {
            throw new NullPointerException();
        }
        if (!component.isRendered()) {
            return null;
        }

        // Render empty data message.
        if (component.getRowCount() == 0) {
            return null;
        }

        // Get rendered row keys.
        RowKey[] rowKeys = component.getRenderedRowKeys();
        if (rowKeys == null) {
            return null;
        }

        JsonArrayBuilder rowBuilder = JSON_BUILDER_FACTORY
                .createArrayBuilder();

        // Iterate over the rendered RowKey objects.
        for (RowKey rowKey : rowKeys) {
            component.setRowKey(rowKey);
            if (!component.isRowAvailable()) {
                break;
            }
            // Render Table2Column components.
            JsonArrayBuilder cols = JSON_BUILDER_FACTORY
                    .createArrayBuilder();
            Iterator kids = component.getTable2ColumnChildren();
            while (kids.hasNext()) {
                Table2Column col = (Table2Column) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                // Render Table2Column children.
                Iterator grandKids = col.getChildren().iterator();
                while (grandKids.hasNext()) {
                    cols.add(WidgetUtilities.renderComponent(context,
                            (UIComponent) grandKids.next()));
                }
            }
            rowBuilder.add(cols);
        }
            component.setRowKey(null); // Clean up.
        return rowBuilder.build();
    }
}
