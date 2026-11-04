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
package com.sun.webui.jsf.renderkit.html;

import com.sun.data.provider.RowKey;
import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.Table;
import com.sun.webui.jsf.component.TableColumn;
import com.sun.webui.jsf.component.TableRowGroup;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class renders TableRowGroup components.
 * <p>
 * The TableRowGroup component provides a layout mechanism for displaying rows
 * of data. UI guidelines describe specific behavior that can applied to the
 * rows and columns of data such as sorting, filtering, pagination, selection,
 * and custom user actions. In addition, UI guidelines also define sections of
 * the table that can be used for titles, row group headers, and placement of
 * pre-defined and user defined actions.
 * </p><p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p>
 * <p>
 * <pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.webui.jsf.renderkit.html.TableRowGroupRenderer.level = FINE
 * </pre>
 * </p>
 * <p>
 * See TLD docs for more information.
 * </p>
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.TableRowGroup"))
public final class TableRowGroupRenderer extends jakarta.faces.render.Renderer {

    /**
     * The set of String pass-through attributes to be rendered.
     * <p>
     * Note: The BGCOLOR attribute is deprecated (in the HTML 4.0 spec) in favor
     * of style sheets. In addition, the DIR and LANG attributes are not
     * currently supported.
     * </p>
     */
    private static final String[] STRING_ATTRIBUTES = {
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
        "valign"
    };

    @Override
    public void encodeBegin(final FacesContext context,
            final UIComponent component) throws IOException {

        if (context == null || component == null) {
            log("encodeBegin",
                    "Cannot render, FacesContext or UIComponent is null");
            throw new NullPointerException();
        }
        if (!component.isRendered()) {
            log("encodeBegin", "Component not rendered, nothing to display");
            return;
        }

        TableRowGroup group = (TableRowGroup) component;
        ResponseWriter writer = context.getResponseWriter();

        // Render group and column headers.
        if (group.isAboveColumnHeader()) {
            renderGroupHeader(context, group, writer);
            renderColumnHeaders(context, group, writer);
        } else {
            renderColumnHeaders(context, group, writer);
            renderGroupHeader(context, group, writer);
        }
        // Clean up.
        group.setRowKey(null);
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {

        if (context == null || component == null) {
            log("encodeChildren",
                    "Cannot render, FacesContext or UIComponent is null");
            throw new NullPointerException();
        }
        if (!component.isRendered()) {
            log("encodeChildren", "Component not rendered, nothing to display");
            return;
        }

        TableRowGroup group = (TableRowGroup) component;
        ResponseWriter writer = context.getResponseWriter();

        // Render empty data message.
        if (group.getRowCount() == 0) {
            log("encodeChildren", "Cannot render data, row count is zero");
            renderEmptyDataColumn(context, group, writer);
            return;
        }

        // Get rendered row keys.
        RowKey[] rowKeys = group.getRenderedRowKeys();
        if (rowKeys == null) {
            log("encodeChildren", "Cannot render data, RowKey array is null");
            return;
        }

        // Iterate over the rendered RowKey objects.
        for (int i = 0; i < rowKeys.length; i++) {
            group.setRowKey(rowKeys[i]);
            if (!group.isRowAvailable()) {
                log("encodeChildren", "Cannot render data, row not available");
                break;
            }

            // Render row.
            renderEnclosingTagStart(context, group, writer, i);

            // Render children.
            Iterator kids = group.getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    log("encodeChildren",
                            "TableColumn not rendered, nothing to display");
                    continue;
                }
                // Render column.
                RenderingUtilities.renderComponent(col, context);
            }
            renderEnclosingTagEnd(writer);
        }
        group.setRowKey(null); // Clean up.
    }

    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        if (context == null || component == null) {
            log("encodeEnd",
                    "Cannot render, FacesContext or UIComponent is null");
            throw new NullPointerException();
        }
        if (!component.isRendered()) {
            log("encodeEnd", "Component not rendered, nothing to display");
            return;
        }

        TableRowGroup group = (TableRowGroup) component;
        ResponseWriter writer = context.getResponseWriter();

        // Do not render footers for an empty table.
        if (group.getRowCount() == 0) {
            log("encodeEnd",
                    "Column, group, and table footers not rendered, row count"
                            + " is zero");
            return;
        }

        // Render group and column footers.
        if (group.isAboveColumnFooter()) {
            renderGroupFooter(context, group, writer);
            renderColumnFooters(context, group, writer);
        } else {
            renderColumnFooters(context, group, writer);
            renderGroupFooter(context, group, writer);
        }

        // Do not render table footers for an empty table.
        Table table = group.getTableAncestor();
        if (table.getRowCount() > 0) {
            renderTableColumnFooters(context, group, writer);
        } else {
            log("encodeEnd",
                    "Table column footers not rendered, row count is zero");
        }

        // Clean up.
        group.setRowKey(null);
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Render empty data message for TableRowGroup components.
     *
     * @param context FacesContext for the current request.
     * @param component TableRowGroup to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEmptyDataColumn(final FacesContext context,
            final TableRowGroup component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderEmptyDataColumn",
                    "Cannot render empty data column, TableRowGroup is null");
            return;
        }
        // Render row start.
        renderEnclosingTagStart(context, component, writer, -1);

        // Render empty data column.
        writer.writeText("\n", null);
        RenderingUtilities.renderComponent(component.getEmptyDataColumn(),
                context);
        renderEnclosingTagEnd(writer);
    }

    /**
     * Render column footers for TableRowGroup components.
     * <p>
     * Note: Although not currently a requirement, nested TableColumn children
     * could render column footers that look like:
     * </p><pre>
     *
     * |   | 1 | 2 |   | 3 | 4 | 5 | 6 |   |
     * | A |   B   | C |       D       | E |
     *
     * </pre><p>
     * In this case, components would be rendered on separate rows. For example,
     * the HTML would look like:
     * </p><pre>
     *
     * <table border="1">
     * <tr>
     * <th rowspan="2">A</th>
     * <th>1</th>
     * <th>2</th>
     * <th rowspan="2">C</th>
     * <th>3</th>
     * <th>4</th>
     * <th>5</th>
     * <th>6</th>
     * <th rowspan="2">E</th>
     * </tr>
     * <tr>
     * <th colspan="2">B</th>
     * <th colspan="2">D</th>
     * </tr>
     * </table>
     *
     * </pre><p>
     * However, the current implementation will render only the first row, which
     * would look like:
     * </p><pre>
     *
     * | A | 1 | 2 | C | 3 | 4 | 5 | 6 | E |
     *
     * </pre>
     *
     * @param context FacesContext for the current request.
     * @param component TableRowGroup to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderColumnFooters(final FacesContext context,
            final TableRowGroup component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderColumnFooters",
                    "Cannot render column footers, TableRowGroup is null");
            return;
        }

        // Get Map of List objects containing nested TableColumn children.
        Map map = getColumnFooterMap(component);

        // Render nested TableColumn children on separate rows.
        Theme theme = getTheme();
        Table table = component.getTableAncestor();
        for (int c = 0; c < map.size(); c++) {
            // The default is to show one level only.
            if (c > 0 && !component.isMultipleColumnFooters()) {
                log("renderColumnFooters",
                        "Multiple column footers not rendered, nothing to"
                                + " display");
                break;
            }

            // Flag to keep from rendering empty tag when no headers are
            // displayed.
            boolean renderStartElement = true;

            // Get List of nested TableColumn children.
            List list = (List) map.get(c);
            for (int i = 0; i < list.size(); i++) {
                TableColumn col = (TableColumn) list.get(i);
                if (!col.isRendered()) {
                    log("renderColumnFooters",
                            "TableColumn not rendered, nothing to display");
                    continue;
                }

                // Get group footer.
                UIComponent footer = col.getColumnFooter();
                if (!(footer != null && footer.isRendered())) {
                    log("renderColumnFooters",
                            "Column footer not rendered, nothing to display");
                    continue;
                }

                // Render start element.
                if (renderStartElement) {
                    renderStartElement = false;
                    writer.writeText("\n", null);
                    writer.startElement("tr", component);
                    writer.writeAttribute("id", getId(component,
                            TableRowGroup.COLUMN_FOOTER_BAR_ID
                            + UINamingContainer.getSeparatorChar(context) + c),
                            null);

                    // Render style class.
                    if (component.isCollapsed()) {
                        writer.writeAttribute("class",
                                theme.getStyleClass(ThemeStyles.HIDDEN), null);
                    }
                }
                // Render footer.
                RenderingUtilities.renderComponent(footer, context);
            }

            // If start element was rendered, this value will be false.
            if (!renderStartElement) {
                writer.endElement("tr");
            }
        }
    }

    /**
     * Render column headers for TableRowGroup components.
     * <p>
     * Note: Although not typical, nested TableColumn children may render column
     * headers that look like:
     * </p><pre>
     *
     * | A |   B   | C |       D       | E |
     * |   | 1 | 2 |   | 3 | 4 | 5 | 6 |   |
     *
     * </pre><p>
     * In this case, components would be rendered on separate rows. For example,
     * the HTML would look like:
     * </p><pre>
     *
     * <table border="1">
     * <tr>
     * <th rowspan="2">A</th>
     * <th colspan="2">B</th>
     * <th rowspan="2">C</th>
     * <th colspan="4">D</th>
     * <th rowspan="2">E</th>
     * </tr>
     * <tr>
     * <th>1</th>
     * <th>2</th>
     * <th>3</th>
     * <th>4</th>
     * <th>5</th>
     * <th>6</th>
     * </tr>
     * </table>
     *
     * </pre>
     *
     * @param context FacesContext for the current request.
     * @param component TableRowGroup to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderColumnHeaders(final FacesContext context,
            final TableRowGroup component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderColumnHeaders",
                    "Cannot render column headers, TableRowGroup is null");
            return;
        }

        // Get Map of List objects containing nested TableColumn children.
        Map<Integer, List<TableColumn>> map = getColumnHeaderMap(component);

        // Render nested TableColumn children on separate rows.
        Theme theme = getTheme();
        Table table = component.getTableAncestor();
        for (int c = 0; c < map.size(); c++) {
            // Flag to keep from rendering empty tag when no headers are
            // displayed.
            boolean renderStartElement = true;

            // Get List of nested TableColumn children.
            List<TableColumn> list = map.get(c);
            for (int i = 0; i < list.size(); i++) {
                TableColumn col = (TableColumn) list.get(i);
                if (!col.isRendered()) {
                    log("renderColumnHeaders",
                            "TableColumn not rendered, nothing to display");
                    continue;
                }

                // Get group header.
                UIComponent header = col.getColumnHeader();
                if (!(header != null && header.isRendered())) {
                    log("renderColumnHeaders",
                            "Column header not rendered, nothing to display");
                    continue;
                }

                // Render start element.
                if (renderStartElement) {
                    renderStartElement = false;
                    writer.writeText("\n", null);
                    writer.startElement("tr", component);
                    writer.writeAttribute("id", getId(component,
                            TableRowGroup.COLUMN_HEADER_BAR_ID
                            + UINamingContainer.getSeparatorChar(context) + c),
                            null);

                    // Render style class.
                    //
                    // Note: We must determine if column headers are available
                    // for all or individual TableRowGroup components. That is,
                    // there could be a single column header for all row groups
                    // or one for each group. Thus, headers may only be hidden
                    // when there is more than one column header.
                    if (component.isCollapsed()
                            && table != null
                            && table.getColumnHeadersCount() > 1) {
                        writer.writeAttribute("class",
                                theme.getStyleClass(ThemeStyles.HIDDEN), null);
                    }
                }
                // Render header.
                RenderingUtilities.renderComponent(header, context);
            }

            // If start element was rendered, this value will be false.
            if (!renderStartElement) {
                writer.endElement("tr");
            }
        }
    }

    /**
     * Render table column footers for TableRowGroup components.
     * <p>
     * Note: Although not currently a requirement, nested TableColumn children
     * could render column footers that look like:
     * </p><pre>
     *
     * |   | 1 | 2 |   | 3 | 4 | 5 | 6 |   |
     * | A |   B   | C |       D       | E |
     *
     * </pre><p>
     * In this case, components would be rendered on separate rows. For example,
     * the HTML would look like:
     * </p><pre>
     *
     * <table border="1">
     * <tr>
     * <th rowspan="2">A</th>
     * <th>1</th>
     * <th>2</th>
     * <th rowspan="2">C</th>
     * <th>3</th>
     * <th>4</th>
     * <th>5</th>
     * <th>6</th>
     * <th rowspan="2">E</th>
     * </tr>
     * <tr>
     * <th colspan="2">B</th>
     * <th colspan="2">D</th>
     * </tr>
     * </table>
     *
     * </pre><p>
     * However, the current implementation will render only the first row, which
     * would look like:
     * </p><pre>
     *
     * | A | 1 | 2 | C | 3 | 4 | 5 | 6 | E |
     *
     * </pre>
     *
     * @param context FacesContext for the current request.
     * @param component TableRowGroup to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderTableColumnFooters(final FacesContext context,
            final TableRowGroup component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderTableColumnFooters",
                    "Cannot render table column footers, TableRowGroup"
                            + " is null");
            return;
        }

        // Get Map of List objects containing nested TableColumn children.
        Map map = getColumnFooterMap(component);

        // Render nested TableColumn children on separate rows.
        Theme theme = getTheme();
        Table table = component.getTableAncestor();
        for (int c = 0; c < map.size(); c++) {
            // The default is to show one level only.
            if (c > 0 && table != null
                    && !component.isMultipleTableColumnFooters()) {
                log("renderTableColumnFooters",
                        "Multiple table column footers not rendered, nothing"
                                + " to display");
                break;
            }

            // Flag to keep from rendering empty tag when no headers are
            // displayed.
            boolean renderStartElement = true;

            // Get List of nested TableColumn children.
            List list = (List) map.get(c);
            for (int i = 0; i < list.size(); i++) {
                TableColumn col = (TableColumn) list.get(i);
                if (!col.isRendered()) {
                    log("renderTableColumnFooters",
                            "TableColumn not rendered, nothing to display");
                    continue;
                }

                // Get group footer.
                UIComponent footer = col.getTableColumnFooter();
                if (!(footer != null && footer.isRendered())) {
                    log("renderTableColumnFooters",
                            "Table column footer not rendered, nothing"
                                    + " to display");
                    continue;
                }

                // Render start element.
                if (renderStartElement) {
                    renderStartElement = false;
                    writer.writeText("\n", null);
                    writer.startElement("tr", component);
                    writer.writeAttribute("id", getId(component,
                            TableRowGroup.TABLE_COLUMN_FOOTER_BAR_ID
                            + UINamingContainer.getSeparatorChar(context) + c),
                            null);

                    // Render style class.
                    //
                    // Note: We must determine if column footers are available
                    // for all or individual TableRowGroup components. That is,
                    // there could be a single column footer for all row groups
                    // or one for each group. Thus, footers may only be hidden
                    // when there is more than one column footer.
                    if (component.isCollapsed()
                            && table.getColumnHeadersCount() > 1) {
                        writer.writeAttribute("class",
                                theme.getStyleClass(ThemeStyles.HIDDEN), null);
                    }
                }
                // Render header.
                RenderingUtilities.renderComponent(footer, context);
            }

            // If start element was rendered, this value will be false.
            if (!renderStartElement) {
                writer.endElement("tr");
            }
        }
    }

    /**
     * Render group footer for TableRowGroup components.
     *
     * @param context FacesContext for the current request.
     * @param component TableRowGroup to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderGroupFooter(final FacesContext context,
            final TableRowGroup component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderGroupFooter",
                    "Cannot render group footer, TableRowGroup is null");
            return;
        }

        // Get group footer.
        UIComponent footer = component.getGroupFooter();
        if (!(footer != null && footer.isRendered())) {
            log("renderGroupFooter",
                    "Group footer not rendered, nothing to display");
            return;
        }

        Theme theme = getTheme();
        writer.writeText("\n", null);
        writer.startElement("tr", component);
        writer.writeAttribute("id", getId(component,
                TableRowGroup.GROUP_FOOTER_BAR_ID), null);

        // Render style class.
        if (component.isCollapsed()) {
            writer.writeAttribute("class",
                    theme.getStyleClass(ThemeStyles.HIDDEN), null);
        }

        // Render footer.
        RenderingUtilities.renderComponent(footer, context);
        writer.endElement("tr");
    }

    /**
     * Render group header for TableRowGroup components.
     *
     * @param context FacesContext for the current request.
     * @param component TableRowGroup to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderGroupHeader(final FacesContext context,
            final TableRowGroup component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderGroupHeader",
                    "Cannot render group header, TableRowGroup is null");
            return;
        }

        // Get group header.
        UIComponent header = component.getGroupHeader();
        if (!(header != null && header.isRendered())) {
            log("renderGroupHeader",
                    "Group header not rendered, nothing to display");
            return;
        }

        Theme theme = getTheme();
        writer.writeText("\n", null);
        writer.startElement("tr", component);
        writer.writeAttribute("id", getId(component,
                TableRowGroup.GROUP_HEADER_BAR_ID), null);

        // Render header.
        RenderingUtilities.renderComponent(header, context);
        writer.endElement("tr");
    }

    /**
     * Render enclosing tag for TableRowGroup components.
     *
     * @param context FacesContext for the current request.
     * @param component TableRowGroup to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     * @param index The current row index.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagStart(final FacesContext context,
            final TableRowGroup component, final ResponseWriter writer,
            final int index) throws IOException {

        if (component == null) {
            log("renderEnclosingTagStart",
                    "Cannot render enclosing tag, TableRowGroup is null");
            return;
        }

        Theme theme = getTheme();
        writer.writeText("\n", null);
        writer.startElement("tr", component);
        writer.writeAttribute("id", component.getClientId(context), null);

        // Get style class for nonempty table.
        String[] styleClasses = getRowStyleClasses(component);
        String styleClass;
        if (index > -1 && styleClasses.length > 0) {
            styleClass = styleClasses[index % styleClasses.length];
        } else {
            styleClass = null;
        }

        // Get selected style class.
        if (component.isSelected()) {
            String s = theme.getStyleClass(ThemeStyles.TABLE_SELECT_ROW);
            if (styleClass != null) {
                styleClass = styleClass + " " + s;
            } else {
                styleClass = s;
            }
        }

        // Get collapsed style class.
        if (component.isCollapsed()) {
            String s = theme.getStyleClass(ThemeStyles.HIDDEN);
            if (styleClass != null) {
                styleClass = styleClass + " " + s;
            } else {
                styleClass = s;
            }
        }

        // Render style class.
        RenderingUtilities.renderStyleClass(context, writer, component,
                styleClass);

        // Render tooltip.
        if (component.getToolTip() != null) {
            writer.writeAttribute("title", component.getToolTip(), "toolTip");
        }

        // Render pass through attributes.
        RenderingUtilities.writeStringAttributes(component, writer,
                STRING_ATTRIBUTES);
    }

    /**
     * Render enclosing tag for TableRowGroup components.
     *
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagEnd(final ResponseWriter writer)
            throws IOException {
        writer.endElement("tr");
    }

    /**
     * Helper method to get Map of List objects containing nested TableColumn
     * children.
     * <p>
     * Note: Although not currently a requirement, nested TableColumn children
     * could render column footers that look like:
     * </p><pre>
     *
     * |   | 1 | 2 |   | 3 | 4 | 5 | 6 |   |
     * | A |   B   | C |       D       | E |
     *
     * </pre><p>
     * In this case, components would be rendered on separate rows. Thus, we
     * need a map that contains List objects like so:
     * </p><pre>
     *
     * Key (row) 0: A, 1, 2, C, 3, 4, 5, 6, E
     * Key (row) 1: B, D
     *
     * </pre><p>
     * Obtaining the List for key 0 tells the renderer that A, 1, 2, C, 3, 4, 5,
     * 6, and E should be rendered for the first row. Obtaining the List for key
     * 1, tells the renderer that B and D should be rendered for the next row.
     * And so on...
     * </p>
     *
     * @param component TableRowGroup to be rendered.
     * @return A Map of nested TableColumn children.
     */
    private Map getColumnFooterMap(final TableRowGroup component) {

        // Start with header map.
        Map<Integer, List<TableColumn>> map = getColumnHeaderMap(component);
        if (map.isEmpty()) {
            log("getColumnFooterMap", "Cannot obtain column footer map");
            return map;
        }

        // Invert map.
        HashMap<Integer, List<TableColumn>> newMap =
                new HashMap<Integer, List<TableColumn>>();
        for (int i = 0; i < map.size(); i++) {
            newMap.put(i, map.get(map.size() - i - 1));
        }

        // Move all non-nested components to the top row.
        List<TableColumn> newList = newMap.get(0);
        // Top row is set already.
        for (int c = 1; c < newMap.size(); c++) {
            List list = (List) newMap.get(c);
            // Start with last component.
            for (int i = list.size() - 1; i >= 0; i--) {
                TableColumn col = (TableColumn) list.get(i);
                if (col.getTableColumnChildren().hasNext()) {
                    // Do not move TableColumn components with nested children.
                    continue;
                }

                // Get colspan of all previous components.
                int colspan = 0;
                for (int k = i - 1; k >= 0; k--) {
                    TableColumn prevCol = (TableColumn) list.get(k);
                    if (prevCol.getTableColumnChildren().hasNext()) {
                        // Count only nested TableColumn components. Other
                        // components have not been moved to the to row, yet.
                        colspan += prevCol.getColumnCount();
                    }
                }

                // Set new position in the top row.
                newList.add(colspan, col);
                list.remove(i);
            }
        }
        return newMap;
    }

    /**
     * Helper method to get Map of List objects containing nested TableColumn
     * children.
     * <p>
     * Note: Nested TableColumn children may be rendered on separate rows. For
     * example, to render column footers that look like the following:
     * </p><pre>
     *
     * | A |   B   | C |       D       | E |
     * |   | 1 | 2 |   | 3 | 4 | 5 | 6 |   |
     *
     * </pre><p>
     * In this case, components would be rendered on separate rows. Thus, we
     * need a map that contains List objects like so:
     * </p><pre>
     *
     * Key (row) 0: A, B, C, D, E
     * Key (row) 1: 1, 2, 3, 4, 5, 6
     *
     * </pre><p>
     * Obtaining the List for key 0 tells the renderer that A, B, C, D, and E
     * should be rendered for the first row. Obtaining the List for key 1, tells
     * the renderer that 1, 2, 3, 4, 5, and 6 should be rendered for the next
     * row. And so on...
     * </p>
     *
     * @param component TableRowGroup to be rendered.
     * @return A Map of nested TableColumn children.
     */
    private Map<Integer, List<TableColumn>> getColumnHeaderMap(
            final TableRowGroup component) {

        HashMap<Integer, List<TableColumn>> map =
                new HashMap<Integer, List<TableColumn>>();
        if (component == null) {
            log("getColumnHeaderMap",
                    "Cannot obtain column header map, TableRowGroup is null");
            return map;
        }
        Iterator<TableColumn> kids = component.getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn col = kids.next();
            initColumnHeaderMap(col, map, 0);
        }
        return map;
    }

    /**
     * Get component id.
     *
     * @param component The parent UIComponent component.
     * @param id The id of the the component to be rendered.
     * @return String
     */
    private String getId(final UIComponent component, final String id) {
        FacesContext context = FacesContext.getCurrentInstance();
        String clientId = component.getClientId(context);
        return clientId + UINamingContainer.getSeparatorChar(context) + id;
    }

    /**
     * Helper method to get Theme objects.
     * @return Theme
     */
    private Theme getTheme() {
        return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }

    /**
     * Helper method to get an array of style-sheet classes to be applied to
     * each row, in the order specified.
     * <p>
     * Note: This is a comma-delimited list of CSS style classes that will be
     * applied to the rows of this table. A space separated list of classes may
     * also be specified for any individual row. These styles are applied, in
     * turn, to each row in the table. For example, if the list has two
     * elements, the first style class in the list is applied to the first row,
     * the second to the second row, the first to the third row, the second to
     * the fourth row, etc. In other words, we keep iterating through the list
     * until we reach the end, and then we start at the beginning again.
     * </p>
     *
     * @param component TableRowGroup component being rendered.
     * @return An array of style-sheet classes.
     */
    private String[] getRowStyleClasses(final TableRowGroup component) {
        String values;
        if (component != null) {
            values = component.getStyleClasses();
        } else {
            values = null;
        }
        if (values == null) {
            return new String[0];
        }

        values = values.trim();
        ArrayList<String> list = new ArrayList<String>();

        while (values.length() > 0) {
            int comma = values.indexOf(",");
            if (comma >= 0) {
                list.add(values.substring(0, comma).trim());
                values = values.substring(comma + 1);
            } else {
                list.add(values.trim());
                values = "";
            }
        }

        String[] results = new String[list.size()];
        return ((String[]) list.toArray(results));
    }

    /**
     * Helper method to initialize Map of List objects containing nested
     * TableColumn children.
     *
     * @param component TableColumn to be rendered.
     * @param map Map to save component List.
     * @param level The current level of the component tree.
     */
    private void initColumnHeaderMap(final TableColumn component,
            final Map<Integer, List<TableColumn>> map, final int level) {

        if (component == null) {
            log("initColumnHeaderMap",
                    "Cannot initialize column header map, TableColumn is null");
            return;
        }

        // Get new List for nested TableColumn children.
        Iterator<TableColumn> kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                initColumnHeaderMap(col, map, level + 1);
            }
        }

        // Create a new List if needed.
        List<TableColumn> list = map.get(level);
        if (list == null) {
            list = new ArrayList<TableColumn>();
        }
        // Save component in List.
        list.add(component);
        // Save List in map.
        map.put(level, list);
    }

    /**
     * Log fine messages.
     * @param method method to log
     * @param msg message to log
     */
    private static void log(final String method, final String msg) {
        // Get class.
        Class clazz = TableRowGroupRenderer.class;
        if (LogUtil.fineEnabled(clazz)) {
            // Log method name and message.
            LogUtil.fine(clazz, clazz.getName() + "." + method + ": " + msg);
        }
    }
}
