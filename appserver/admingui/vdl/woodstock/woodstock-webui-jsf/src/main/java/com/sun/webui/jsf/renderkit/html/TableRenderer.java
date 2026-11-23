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
import com.sun.data.provider.SortCriteria;
import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.Table;
import com.sun.webui.jsf.component.TableActions;
import com.sun.webui.jsf.component.TableColumn;
import com.sun.webui.jsf.component.TableHeader;
import com.sun.webui.jsf.component.TablePanels;
import com.sun.webui.jsf.component.TableRowGroup;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import java.io.IOException;
import java.util.Iterator;

import static com.sun.webui.jsf.util.JavaScriptUtilities.renderInitScriptTag;
import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;
import static com.sun.webui.jsf.util.RenderingUtilities.renderComponent;
import static com.sun.webui.jsf.util.RenderingUtilities.renderExtraHtmlAttributes;
import static com.sun.webui.jsf.util.RenderingUtilities.renderStyleClass;
import static com.sun.webui.jsf.util.RenderingUtilities.writeStringAttributes;

/**
 * This class renders Table components.
 * <p>
 * The table component provides a layout mechanism for displaying table actions.
 * UI guidelines describe specific behavior that can applied to the rows and
 * columns of data such as sorting, filtering, pagination, selection, and custom
 * user actions. In addition, UI guidelines also define sections of the table
 * that can be used for titles, row group headers, and placement of pre-defined
 * and user defined actions.
 * </p>
 * <p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p>
 *
 * <pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.webui.jsf.renderkit.html.TableRenderer.level = FINE
 * </pre>
 *
 * <p>
 * See TLD docs for more information.
 * </p>
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.Table"))
public final class TableRenderer extends jakarta.faces.render.Renderer {

    /**
     * JS object name.
     */
    private static final String JAVASCRIPT_OBJECT_CLASS = "Table";

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

        Table table = (Table) component;
        ResponseWriter writer = context.getResponseWriter();
        renderEnclosingTagStart(context, table, writer);
        renderTitle(context, table, writer);
        renderActionsTop(context, table, writer);
        renderEmbeddedPanels(context, table, writer);
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

        Table table = (Table) component;
        ResponseWriter writer = context.getResponseWriter();

        // Render TableRowGroup children.
        Iterator kids = table.getTableRowGroupChildren();
        while (kids.hasNext()) {
            TableRowGroup group = (TableRowGroup) kids.next();
            renderComponent(group, context);
        }
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

        Table table = (Table) component;
        ResponseWriter writer = context.getResponseWriter();
        renderActionsBottom(context, table, writer);
        renderTableFooter(context, table, writer);
        renderEnclosingTagEnd(writer);
        renderJavascript(context, table, writer);
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Render the bottom actions for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderActionsBottom(final FacesContext context,
            final Table component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderActionsBottom",
                    "Cannot render actions bar, Table is null");
            return;
        }

        // Get panel component.
        UIComponent actions = component.getTableActionsBottom();
        if (!(actions != null && actions.isRendered())) {
            log("renderActionsBottom",
                    "Actions bar not rendered, nothing to display");
            return;
        }

        writer.writeText("\n", null);
        writer.startElement("tr", component);
        writer.writeAttribute("id", getId(component,
                Table.TABLE_ACTIONS_BOTTOM_BAR_ID), null);

        // Render embedded panels.
        renderComponent(actions, context);
        writer.endElement("tr");
    }

    /**
     * Render the top actions for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderActionsTop(final FacesContext context,
            final Table component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderActionsTop",
                    "Cannot render actions bar, Table is null");
            return;
        }

        // Get panel component.
        UIComponent actions = component.getTableActionsTop();
        if (!(actions != null && actions.isRendered())) {
            log("renderActionsTop",
                    "Actions bar not rendered, nothing to display");
            return;
        }

        writer.writeText("\n", null);
        writer.startElement("tr", component);
        writer.writeAttribute("id", getId(component,
                Table.TABLE_ACTIONS_TOP_BAR_ID), null);

        // Render embedded panels.
        renderComponent(actions, context);
        writer.endElement("tr");
    }

    /**
     * Render embedded panels for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEmbeddedPanels(final FacesContext context,
            final Table component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderEmbeddedPanels",
                    "Cannot render embedded panels, Table is null");
            return;
        }

        // Get panel component.
        UIComponent panels = component.getEmbeddedPanels();
        if (!(panels != null && panels.isRendered())) {
            log("renderEmbeddedPanels",
                    "Embedded panels not rendered, nothing to display");
            return;
        }

        writer.writeText("\n", null);
        writer.startElement("tr", component);
        writer.writeAttribute("id", getId(component,
                Table.EMBEDDED_PANELS_BAR_ID), null);

        // Render embedded panels.
        renderComponent(panels, context);
        writer.endElement("tr");
    }

    /**
     * Render table footer for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderTableFooter(final FacesContext context,
            final Table component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderTableFooter",
                    "Cannot render table foter, Table is null");
            return;
        }

        // Get footer.
        UIComponent footer = component.getTableFooter();
        if (!(footer != null && footer.isRendered())) {
            log("renderTableFooter",
                    "Table footer not rendered, nothing to display");
            return;
        }

        Theme theme = getTheme();
        writer.writeText("\n", null);
        writer.startElement("tr", component);
        writer.writeAttribute("id", getId(component, Table.TABLE_FOOTER_BAR_ID),
                null);

        // Render footer.
        renderComponent(footer, context);
        writer.endElement("tr");
    }

    /**
     * Render title for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderTitle(final FacesContext context,
            final Table component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderTitle", "Cannot render title, Table is null");
            return;
        }

        // Render facet.
        UIComponent facet = component.getFacet(Table.TITLE_FACET);
        if (facet != null) {
            renderTitleStart(context, component, writer);
            renderComponent(facet, context);
            renderTitleEnd(context, writer);
            return;
        }

        // Render default title.
        if (component.getTitle() == null) {
            log("renderTitle", "Title is null, nothing to display");
            return;
        }

        // Get filter augment.
        Theme theme = getTheme();
        String filter;
        if (component.getFilterText() != null) {
            filter = theme.getMessage("table.title.filterApplied",
                        new String[]{
                            component.getFilterText(),
                        });
        } else {
            filter = "";
        }
        // Get TableRowGroup component.
        TableRowGroup group = component.getTableRowGroupChild();
        boolean paginated;
        if (group != null) {
            paginated = group.isPaginated();
        } else {
            paginated = false;
        }

        // Initialize values.
        int totalRows = component.getRowCount();
        boolean emptyTable = (totalRows == 0);
        boolean singlePage = (totalRows < component.getRows());

        // Render title (e.g., "Title (25 - 50 of 1000): [Filter]").
        String title = component.getTitle();
        if (component.isAugmentTitle()) {
            if (!emptyTable && !singlePage && paginated) {
                // Get max values for paginated group table.
                int maxRows = component.getRows();
                int maxFirst = component.getFirst();

                // Get first and last rows augment.
                String first = Integer.toString(maxFirst + 1);
                String last = Integer.toString(Math.min(maxFirst + maxRows,
                        totalRows));

                if (component.getItemsText() != null) {
                    title = theme.getMessage("table.title.paginatedItems",
                            new String[]{
                                component.getTitle(),
                                first,
                                last,
                                Integer.toString(totalRows),
                                component.getItemsText(), filter,
                            });
                } else {
                    title = theme.getMessage("table.title.paginated",
                            new String[]{
                                component.getTitle(),
                                first,
                                last,
                                Integer.toString(totalRows),
                                filter,
                            });
                }
            } else {
                if (component.getItemsText() != null) {
                    title = theme.getMessage("table.title.scrollItems",
                            new String[]{
                                component.getTitle(),
                                Integer.toString(totalRows),
                                component.getItemsText(),
                                filter,
                            });
                } else {
                    title = theme.getMessage("table.title.scroll",
                            new String[]{
                                component.getTitle(),
                                Integer.toString(totalRows),
                                filter,
                            });
                }
            }
        } else {
            log("renderTitle",
                    "Title not augmented, itemsText & filterText not"
                            + " displayed");
        }

        renderTitleStart(context, component, writer);

        // Render title and hidden rows text.
        if (component.isHiddenSelectedRows()) {
            writer.startElement("span", component);
            writer.writeAttribute("class",
                    theme.getStyleClass(ThemeStyles.TABLE_TITLE_TEXT_SPAN),
                    null);
            writer.writeText(title, null);
            writer.endElement("span");
            writer.startElement("span", component);
            writer.writeAttribute("class",
                    theme.getStyleClass(ThemeStyles.TABLE_TITLE_MESSAGE_SPAN),
                    null);
            writer.writeText(theme.getMessage("table.hiddenSelections",
                    new String[]{
                        Integer.toString(component.getHiddenSelectedRowsCount()),
                    }), null);
            writer.endElement("span");
        } else {
            // Render default title text.
            writer.writeText(title, null);
        }
        renderTitleEnd(context, writer);
    }

    /**
     * Render title for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component The table component being rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderTitleStart(final FacesContext context,
            final Table component, final ResponseWriter writer)
            throws IOException {

        writer.writeText("\n", null);
        writer.startElement("caption", component);
        writer.writeAttribute("id", getId(component, Table.TITLE_BAR_ID),
                null);
        writer.writeAttribute("class",
                getTheme().getStyleClass(ThemeStyles.TABLE_TITLE_TEXT), null);

        // Render extra HTML.
        if (component.getExtraTitleHtml() != null) {
            renderExtraHtmlAttributes(writer, component.getExtraTitleHtml());
        }
    }

    /**
     * Render title for Table components.
     *
     * @param context FacesContext for the current request.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderTitleEnd(final FacesContext context,
            final ResponseWriter writer) throws IOException {

        writer.endElement("caption");
    }

    /**
     * Render enclosing tag for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagStart(final FacesContext context,
            final Table component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderEnclosingTagStart",
                    "Cannot render enclosing tag, Table is null");
            return;
        }

        Theme theme = getTheme();

        // Render div used to set style and class properties
        // -- bugtraq #6316179.
        writer.writeText("\n", null);
        writer.startElement("div", component);
        writer.writeAttribute("id", component.getClientId(context), null);

        // Render style.
        String style = component.getStyle();
        if (style != null) {
            writer.writeAttribute("style", style, null);
        }

        // Render style class.
        renderStyleClass(context, writer, component, null);

        // Render div used to set width.
        writer.writeText("\n", null);
        writer.startElement("div", component);

        // Render width.
        String width = component.getWidth();
        if (width != null) {
            // If not a percentage, units are in pixels.
            if (!width.contains("%")) {
                width += "px";
            }
            writer.writeAttribute("style", "width:" + width, null);
        } else {
            writer.writeAttribute("style", "width:100%", null);
        }

        // Render table.
        writer.writeText("\n", null);
        writer.startElement("table", component);
        writer.writeAttribute("id", getId(component, Table.TABLE_ID), null);

        // Get style class.
        String styleClass = theme.getStyleClass(ThemeStyles.TABLE);
        if (component.isLite()) {
            styleClass += " "
                    + theme.getStyleClass(ThemeStyles.TABLE_LITE);
        }

        // Render style class.
        writer.writeAttribute("class", styleClass, null);

        // Render width using 100% to ensure consistent right margins.
        writer.writeAttribute("width", "100%", null);

        // Render height for Creator which sets property via style.
        if (style != null) {
            int first = style.indexOf("height:");
            if (first > -1) {
                int last = style.indexOf(";", first);
                if (last > -1) {
                    writer.writeAttribute("style",
                            style.substring(first, last + 1), null);
                } else {
                    writer.writeAttribute("style",
                            style.substring(first), null);
                }
            }
        }
        renderTableAttributes(context, component, writer);
    }

    /**
     * This implementation renders {@code border}, {@code cellpadding},
     * {@code cellspacing}, tool tip and string attributes.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     * @throws IOException if an input/output error occurs.
     */
    protected void renderTableAttributes(final FacesContext context,
            final Table component, final ResponseWriter writer)
            throws IOException {

        // Render border.
        int border = component.getBorder();
        if (border > -1) {
            writer.writeAttribute("border",
                    Integer.toString(border), null);
        } else {
            writer.writeAttribute("border", "0", null);
        }

        // Render cellpadding.
        String value = component.getCellPadding();
        if (value != null) {
            writer.writeAttribute("cellpadding", value, null);
        } else {
            writer.writeAttribute("cellpadding", "0", null);
        }

        // Render cellspacing.
        value = component.getCellSpacing();
        if (value != null) {
            writer.writeAttribute("cellspacing", value, null);
        } else {
            writer.writeAttribute("cellspacing", "0", null);
        }

        // Render tooltip.
        value = component.getToolTip();
        if (value != null) {
            writer.writeAttribute("title", value, "toolTip");
        }

        // Render pass through attributes.
        writeStringAttributes(component, writer, STRING_ATTRIBUTES);
    }

    /**
     * Render enclosing tag for Table components.
     *
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagEnd(final ResponseWriter writer)
            throws IOException {

        writer.endElement("table");
        writer.endElement("div");
        writer.endElement("div");
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
     * Helper method to get the column ID and selectId from nested TableColumn
     * components, used in JS functions (e.g., de/select all button
     * functionality).
     *
     * @param context FacesContext for the current request.
     * @param component TableColumn to be rendered.
     * @return The first selectId property found.
     */
    private String getSelectId(final FacesContext context,
            final TableColumn component) {

        String selectId = null;
        if (component == null) {
            log("getSelectId", "Cannot obtain select Id, TableColumn is null");
            return selectId;
        }

        // Render nested TableColumn children.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                selectId = getSelectId(context, col);
                if (selectId != null) {
                    break;
                }
            }
        } else {
            // Get selectId for possible nested TableColumn components.
            if (component.getSelectId() != null) {
                // Get TableRowGroup ancestor.
                TableRowGroup group = component.getTableRowGroupAncestor();
                if (group != null) {
                    // Get column and group id.
                    String colId = component.getClientId(context);
                    String groupId = group.getClientId(context)
                            + UINamingContainer.getSeparatorChar(context);
                    try {
                        selectId = colId.substring(groupId.length(),
                                colId.length())
                                + UINamingContainer.getSeparatorChar(context)
                                + component.getSelectId();
                    } catch (IndexOutOfBoundsException e) {
                        // Do nothing.
                    }
                }
            }
        }
        return selectId;
    }

    /**
     * Helper method to get the sort menu option value for the select column.
     *
     * @param component Table to be rendered.
     *
     * @return The select option value.
     */
    private String getSelectSortMenuOptionValue(final Table component) {
        TableRowGroup group = component.getTableRowGroupChild();

        // Get first select column found.
        if (group != null) {
            Iterator kids = group.getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered() || col.getSelectId() == null) {
                    continue;
                }
                String value = getSelectSortMenuOptionValue(col);
                if (value != null) {
                    return value;
                }
            }
        } else {
            log("getSelectSortMenuOptionValue",
                    "Cannot obtain select sort menu option value,"
                    + " TableRowGroup is null");
        }
        return null;
    }

    /**
     * Helper method to get the sort menu option value for the select column.
     *
     * @param component TableColumn to be rendered.
     *
     * @return The select option value.
     */
    private String getSelectSortMenuOptionValue(final TableColumn component) {
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered() || col.getSelectId() == null) {
                    continue;
                }
                String value = getSelectSortMenuOptionValue(col);
                if (value != null) {
                    return value;
                }
            }
        }

        // Return sort criteria key.
        SortCriteria criteria = component.getSortCriteria();
        if (criteria != null) {
            return criteria.getCriteriaKey();
        }
        return null;
    }

    /**
     * Helper method to get JS array containing tool tips used for sort order
     * menus.
     * @param component Table to be rendered.
     * @param descending flag indicating descending tool tips.
     * @return A JS array containing tool tips.
     */
    private JsonArray getSortToolTipJavascript(final Table component,
            final boolean descending) {

        // Get undetermined tooltip.
        String tooltip;
        if (descending) {
            tooltip = "table.sort.augment.undeterminedDescending";
        } else {
            tooltip = "table.sort.augment.undeterminedAscending";
        }

        // Append array of ascending sort order tooltips.
        JsonArrayBuilder jsonBuilder = JSON_BUILDER_FACTORY
                .createArrayBuilder()
                .add(getTheme().getMessage(tooltip));

        // Use the first TableRowGroup child to obtain sort tool tip.
        TableRowGroup group = component.getTableRowGroupChild();
        if (group != null) {
            // For each TableColumn component, get the sort tool tip augment
            // based on the value for the align property of TableColumn.
            Iterator kids = group.getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                // Get tool tip augment.
                jsonBuilder.add(col.getSortToolTipAugment(descending));
            }
        } else {
            log("getSortToolTipJavascript",
                    "Cannot obtain Javascript array of sort tool tips,"
                    + " TableRowGroup is null");
        }
        return jsonBuilder.build();
    }

    /**
     * Helper method to get table column footer style class for TableColumn
     * components.
     *
     * @param component TableColumn to be rendered.
     * @param level The current sort level.
     * @return The style class for the table column footer.
     */
    private String getTableColumnFooterStyleClass(final TableColumn component,
            final int level) {

        String styleClass;

        // Get appropriate style class.
        if (component.isSpacerColumn()) {
            styleClass = ThemeStyles.TABLE_COL_FOOTER_SPACER;
        } else if (level == 1) {
            styleClass = ThemeStyles.TABLE_COL_FOOTER_SORT;
        } else {
            styleClass = ThemeStyles.TABLE_COL_FOOTER;
        }
        return getTheme().getStyleClass(styleClass);
    }

    /**
     * Helper method to get Theme objects.
     * @return Theme
     */
    private Theme getTheme() {
        return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }

    /**
     * Log fine messages.
     * @param method method to log
     * @param msg message to log
     */
    private static void log(final String method, final String msg) {
        // Get class.
        Class clazz = TableRenderer.class;
        if (LogUtil.fineEnabled(clazz)) {
            // Log method name and message.
            LogUtil.fine(clazz, clazz.getName() + "." + method
                    + ": " + msg);
        }
    }

    /**
     * Helper method to render JS to Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderJavascript(final FacesContext context,
            final Table component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderJavascript", "Cannot render Javascript, Table is null");
            return;
        }

        // Append properties.
        JsonObjectBuilder initProps = JSON_BUILDER_FACTORY
                .createObjectBuilder()
                .add("id", component.getClientId(context));

        appendPanelProperties(context, component, initProps);
        appendFilterProperties(context, component, initProps);
        appendSortPanelProperties(context, component, initProps);
        appendGroupProperties(context, component, initProps);
        appendGroupPanelProperties(context, component, initProps);

        // Render JavaScript.
        renderInitScriptTag(writer, "table", initProps.build());
    }

    /**
     * Helper method to append panel properties.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param jsonBuilder jsonBuilder to add the properties to
     *
     * @exception IOException if an input/output error occurs.
     */
    private void appendPanelProperties(final FacesContext context,
            final Table component, final JsonObjectBuilder jsonBuilder)
            throws IOException {

        if (component == null) {
            log("appendPanelProperties",
                    "Cannot obtain properties, Table is null");
            return;
        }

        // Don't invoke component.getEmbeddedPanels() here because it will
        // create new component instances which do not work with the action
        // listeners assigned to the rendered components.
        UIComponent panels = component.getFacet(Table.EMBEDDED_PANELS_ID);
        if (panels == null) {
            log("appendPanelProperties",
                    "Cannot obtain panel properties, embedded panels"
                            + " facet is null");
            return;
        }

        Theme theme = getTheme();
        String prefix = panels.getClientId(context)
                + UINamingContainer.getSeparatorChar(context);

        // Append array of panel Ids.
        JsonArray ary1 = JSON_BUILDER_FACTORY.createArrayBuilder()
                .add(prefix + TablePanels.SORT_PANEL_ID)
                .add(prefix + TablePanels.PREFERENCES_PANEL_ID)
                .add(prefix + TablePanels.FILTER_PANEL_ID)
                .build();
        jsonBuilder.add("panelIds", ary1);

        // Don't invoke component.getTableActionsTop() here because it will
        // create new component instances which do not work with the action
        // listeners assigned to the rendered components.
        UIComponent actions = component.getFacet(Table.TABLE_ACTIONS_TOP_ID);
        if (actions == null) {
            log("appendPanelProperties",
                    "Cannot obtain properties, facet is null");
            return;
        }

        // Append array of focus Ids.
        JsonArrayBuilder ary2 = JSON_BUILDER_FACTORY.createArrayBuilder();
        if (component.getSortPanelFocusId() != null) {
            ary2.add(component.getSortPanelFocusId());
        } else {
            ary2.add(prefix + TablePanels.PRIMARY_SORT_COLUMN_MENU_ID);
        }
        if (component.getPreferencesPanelFocusId() != null) {
            ary2.add(component.getPreferencesPanelFocusId());
        } else {
            ary2.add(JsonObject.NULL);
        }
        if (component.getFilterPanelFocusId() != null) {
            ary2.add(component.getFilterPanelFocusId());
        } else {
            ary2.add(JsonObject.NULL);
        }
        jsonBuilder.add("panelFocusIds", ary2);

        prefix = actions.getClientId(context)
                + UINamingContainer.getSeparatorChar(context);

        // Append array of panel toggle Ids.
        JsonArrayBuilder ary3 = JSON_BUILDER_FACTORY.createArrayBuilder();
        ary3.add(prefix + TableActions.SORT_PANEL_TOGGLE_BUTTON_ID)
                .add(prefix + TableActions.PREFERENCES_PANEL_TOGGLE_BUTTON_ID);
        if (component.getFilterId() != null) {
            ary3.add(component.getFilterId());
        } else {
            ary3.add(JsonObject.NULL);
        }
        jsonBuilder.add("panelToggleIds", ary3);

        // Append array of toggle icons for open panels.
        JsonArray ary4 = JSON_BUILDER_FACTORY.createArrayBuilder()
                .add(theme.getImagePath(ThemeImages.TABLE_SORT_PANEL_FLIP))
                .add(theme.getImagePath(
                        ThemeImages.TABLE_PREFERENCES_PANEL_FLIP))
                .add(JsonObject.NULL)
                .build();
        jsonBuilder.add("panelToggleIconsOpen", ary4);

        // Append array of toggle icons for closed panels.
        JsonArray ary5 = JSON_BUILDER_FACTORY.createArrayBuilder()
                .add(theme.getImagePath(ThemeImages.TABLE_SORT_PANEL))
                .add(theme.getImagePath(ThemeImages.TABLE_PREFERENCES_PANEL))
                .add(JsonObject.NULL)
                .build();
        jsonBuilder.add("panelToggleIconsClose", ary5);
    }

    /**
     * Helper method to append filter properties.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param jsonBuilder jsonBuilder to add the properties to
     *
     * @exception IOException if an input/output error occurs.
     */
    private void appendFilterProperties(final FacesContext context,
            final Table component, final JsonObjectBuilder jsonBuilder)
            throws IOException {

        if (component == null) {
            log("apppendFilterProperties",
                    "Cannot obtain properties, Table is null");
            return;
        }

        Theme theme = getTheme();
        jsonBuilder.add("basicFilterStyleClass",
                theme.getStyleClass(ThemeStyles.MENU_JUMP))
                .add("customFilterStyleClass", theme.getStyleClass(
                        ThemeStyles.TABLE_CUSTOM_FILTER_MENU))
                .add("customFilterOptionValue", Table.CUSTOM_FILTER)
                .add("customFilterAppliedOptionValue",
                        Table.CUSTOM_FILTER_APPLIED);
    }

    /**
     * Helper method to append group properties.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param jsonBuilder jsonBuilder to add the properties to
     *
     * @exception IOException if an input/output error occurs.
     */
    private void appendGroupProperties(final FacesContext context,
            final Table component, final JsonObjectBuilder jsonBuilder)
            throws IOException {

        if (component == null) {
            log("appendGroupProperties",
                    "Cannot obtain properties, Table is null");
            return;
        }

        Theme theme = getTheme();
        jsonBuilder.add("selectRowStyleClass",
                theme.getStyleClass(ThemeStyles.TABLE_SELECT_ROW));

        // Append array of select IDs.
        JsonArrayBuilder ary1 = JSON_BUILDER_FACTORY.createArrayBuilder();
        Iterator kids = component.getTableRowGroupChildren();
        while (kids.hasNext()) {
            TableRowGroup group = (TableRowGroup) kids.next();

            // Iterate over each TableColumn chlid to find selectId.
            String selectId = null;
            Iterator grandkids = group.getTableColumnChildren();
            while (grandkids.hasNext()) {
                TableColumn col = (TableColumn) grandkids.next();
                if (!col.isRendered()) {
                    continue;
                }
                selectId = getSelectId(context, col);
                if (selectId != null) {
                    break;
                }
            }
            // Append selectId, if applicable.
            if (selectId != null) {
                ary1.add(selectId);
            } else {
                ary1.add(JsonObject.NULL);
            }
        }
        jsonBuilder.add("selectIds", ary1);

        // Append array of TableRowGroup IDs.
        JsonArrayBuilder ary2 = JSON_BUILDER_FACTORY.createArrayBuilder();
        kids = component.getTableRowGroupChildren();
        while (kids.hasNext()) {
            TableRowGroup group = (TableRowGroup) kids.next();
            ary2.add(group.getClientId(context));
        }
        jsonBuilder.add("groupIds", ary2);

        // Append array of row IDs.
        JsonArrayBuilder ary3 = JSON_BUILDER_FACTORY.createArrayBuilder();
        kids = component.getTableRowGroupChildren();
        while (kids.hasNext()) {
            TableRowGroup group = (TableRowGroup) kids.next();
            // Only rendered rows.
            RowKey[] rowKeys = group.getRenderedRowKeys();

            // Append an array of row ids for each TableRowGroup child.
            JsonArrayBuilder tmp = JSON_BUILDER_FACTORY.createArrayBuilder();
            if (rowKeys != null) {
                for (RowKey rowKey : rowKeys) {
                    tmp.add(rowKey.getRowId());
                }
            } else {
                // TableRowGroup may have been empty.
                tmp.add(JsonObject.NULL);
            }
            ary3.add(tmp);
        }
        jsonBuilder.add("rowIds", ary3);

        // Append array of hidden selected row counts.
        JsonArrayBuilder ary4 = JSON_BUILDER_FACTORY.createArrayBuilder();
        kids = component.getTableRowGroupChildren();
        while (kids.hasNext()) {
            TableRowGroup group = (TableRowGroup) kids.next();

            // Don't bother with calculations if this property is not set.
            if (component.isHiddenSelectedRows()) {
                ary4.add(group.getHiddenSelectedRowsCount());
            } else {
                ary4.add(0);
            }
        }
        jsonBuilder.add("hiddenSelectedRowCounts", ary4)
                .add("hiddenSelectionsMsg",
                        theme.getMessage("table.confirm.hiddenSelections"))
                .add("totalSelectionsMsg",
                        theme.getMessage("table.confirm.totalSelections"))
                .add("deleteSelectionsMsg",
                        theme.getMessage("table.confirm.deleteSelections"));
    }

    /**
     * Helper method to append group panel properties.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param jsonBuilder jsonBuilder to add the properties to
     *
     * @exception IOException if an input/output error occurs.
     */
    private void appendGroupPanelProperties(final FacesContext context,
            final Table component, final JsonObjectBuilder jsonBuilder)
            throws IOException {

        if (component == null) {
            log("appendGroupPanelProperties",
                    "Cannot obtain properties, Table is null");
            return;
        }

        // Get ID prefix for TableHeader components.
        String prefix = TableRowGroup.GROUP_HEADER_ID
                + UINamingContainer.getSeparatorChar(context);

        Theme theme = getTheme();
        jsonBuilder.add("columnFooterId", TableRowGroup.COLUMN_FOOTER_BAR_ID)
                .add("columnHeaderId", TableRowGroup.COLUMN_HEADER_BAR_ID)
                .add("tableColumnFooterId",
                        TableRowGroup.TABLE_COLUMN_FOOTER_BAR_ID)
                .add("groupFooterId", TableRowGroup.GROUP_FOOTER_BAR_ID)
                .add("groupPanelToggleButtonId", prefix
                        + TableHeader.GROUP_PANEL_TOGGLE_BUTTON_ID)
                .add("groupPanelToggleButtonToolTipOpen",
                        theme.getMessage("table.group.collapse"))
                .add("groupPanelToggleButtonToolTipClose",
                        theme.getMessage("table.group.expand"))
                .add("groupPanelToggleIconOpen", theme.getImagePath(
                        ThemeImages.TABLE_GROUP_PANEL_FLIP))
                .add("groupPanelToggleIconClose", theme.getImagePath(
                        ThemeImages.TABLE_GROUP_PANEL))
                .add("warningIconId", prefix + TableHeader.WARNING_ICON_ID)
                .add("warningIconOpen", theme.getImagePath(ThemeImages.DOT))
                .add("warningIconClose", theme.getImagePath(
                        ThemeImages.ALERT_WARNING_SMALL))
                // No tooltip for place holder icon.
                .add("warningIconToolTipOpen", JsonObject.NULL)
                .add("warningIconToolTipClose",
                        theme.getMessage("table.group.warning"))
                .add("collapsedHiddenFieldId", prefix
                        + TableHeader.COLLAPSED_HIDDEN_FIELD_ID)
                .add("selectMultipleToggleButtonId", prefix
                        + TableHeader.SELECT_MULTIPLE_TOGGLE_BUTTON_ID)
                .add("selectMultipleToggleButtonToolTip",
                        theme.getMessage("table.group.selectMultiple"))
                .add("selectMultipleToggleButtonToolTipSelected",
                        theme.getMessage("table.group.deselectMultiple"));
    }

    /**
     * Helper method to append sort panel properties.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param jsonBuilder jsonBuilder to add the properties to
     *
     * @exception IOException if an input/output error occurs.
     */
    private void appendSortPanelProperties(final FacesContext context,
            final Table component, final JsonObjectBuilder jsonBuilder)
            throws IOException {

        if (component == null) {
            log("appendSortPanelProperties",
                    "Cannot obtain properties, Table is null");
            return;
        }

        // Don't invoke component.getEmbeddedPanels() here because it will
        // create new component instances which do not work with the action
        // listeners assigned to the rendered components.
        UIComponent panels = component.getFacet(Table.EMBEDDED_PANELS_ID);
        if (panels == null) {
            log("appendSortPanelProperties",
                    "Cannot obtain properties, Embedded panels facet is null");
            return;
        }

        Theme theme = getTheme();
        String prefix = panels.getClientId(context)
                + UINamingContainer.getSeparatorChar(context);

        // Append array of sort column menu Ids.
        JsonArray ary1 = JSON_BUILDER_FACTORY.createArrayBuilder()
                .add(prefix + TablePanels.PRIMARY_SORT_COLUMN_MENU_ID)
                .add(prefix + TablePanels.SECONDARY_SORT_COLUMN_MENU_ID)
                .add(prefix + TablePanels.TERTIARY_SORT_COLUMN_MENU_ID)
                .build();
        jsonBuilder.add("sortColumnMenuIds", ary1);

        // Append array of sort order menu Ids.
        JsonArray ary2 = JSON_BUILDER_FACTORY.createArrayBuilder()
                .add(prefix + TablePanels.PRIMARY_SORT_ORDER_MENU_ID)
                .add(prefix + TablePanels.SECONDARY_SORT_ORDER_MENU_ID)
                .add(prefix + TablePanels.TERTIARY_SORT_ORDER_MENU_ID)
                .build();
        jsonBuilder.add("sortOrderMenuIds", ary2);

        // Append array of sort order tooltips.
        JsonArray ary3 = JSON_BUILDER_FACTORY.createArrayBuilder()
                .add(theme.getMessage("table.panel.primarySortOrder"))
                .add(theme.getMessage("table.panel.secondarySortOrder"))
                .add(theme.getMessage("table.panel.tertiarySortOrder"))
                .build();
        jsonBuilder.add("sortOrderToolTips", ary3);

        // Append sort menu option value for select column and paginated flag.
        String value = getSelectSortMenuOptionValue(component);
        TableRowGroup group = component.getTableRowGroupChild();
        jsonBuilder.add("sortOrderToolTipsAscending",
                getSortToolTipJavascript(component, false))
                .add("sortOrderToolTipsDescending",
                        getSortToolTipJavascript(component, true))
                .add("duplicateSelectionMsg",
                        theme.getMessage("table.panel.duplicateSelectionError"))
                .add("missingSelectionMsg",
                        theme.getMessage("table.panel.missingSelectionError"));
        if (value != null) {
            jsonBuilder.add("selectSortMenuOptionValue", value);
        } else {
            jsonBuilder.add("selectSortMenuOptionValue", JsonObject.NULL);
        }
        jsonBuilder.add("hiddenSelectedRows", component.isHiddenSelectedRows());
        if (group != null) {
            jsonBuilder.add("paginated", group.isPaginated());
        } else {
            jsonBuilder.add("paginated", false);
        }
    }
}
