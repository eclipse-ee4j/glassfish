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

import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.TableColumn;
import com.sun.webui.jsf.component.TableHeader;
import com.sun.webui.jsf.component.TableRowGroup;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.FocusManager;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Iterator;

/**
 * This class renders TableHeader components.
 * <p>
 * <pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.webui.jsf.renderkit.html.TableHeaderRenderer.level = FINE
 * </pre>
 * </p>
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.TableHeader"))
public final class TableHeaderRenderer extends jakarta.faces.render.Renderer {

    /**
     * The set of String pass-through attributes to be rendered.
     * <p>
     * Note: The WIDTH, HEIGHT, and BGCOLOR attributes are all deprecated (in
     * the HTML 4.0 spec) in favor of style sheets. In addition, the DIR and
     * LANG attributes are not currently supported.
     * </p>
     */
    private static final String[] STRING_ATTRIBUTES = {
        "abbr",
        "align",
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
        "scope",
        "style",
        "valign",
        "width"
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

        TableHeader header = (TableHeader) component;
        ResponseWriter writer = context.getResponseWriter();
        renderEnclosingTagStart(context, header, writer);
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

        TableHeader header = (TableHeader) component;
        ResponseWriter writer = context.getResponseWriter();

        // Render headers.
        if (header.isGroupHeader()) {
            renderGroupHeader(context, header, writer);
        } else if (header.isSelectHeader()) {
            renderSelectHeader(context, header, writer);
        } else if (header.isSortHeader()) {
            renderSortHeader(context, header, writer);
        } else {
            renderColumnHeader(context, header, writer);
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

        TableHeader header = (TableHeader) component;
        ResponseWriter writer = context.getResponseWriter();
        renderEnclosingTagEnd(context, header, writer);
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Render column headers for TableHeader components.
     *
     * @param context FacesContext for the current request.
     * @param component TableHeader to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderColumnHeader(final FacesContext context,
            final TableHeader component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderColumnHeader",
                    "Cannot render column header, TableHeader is null");
            return;
        }

        // Get facet.
        TableColumn col = component.getTableColumnAncestor();
        UIComponent facet;
        if (col != null) {
            facet = col.getFacet(TableColumn.HEADER_FACET);
        } else {
            facet = null;
        }
        if (facet != null && facet.isRendered()) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            // Get style for nested column headers.
            TableColumn parent;
            if  (col != null) {
                parent = col.getTableColumnAncestor();
            } else {
                parent = null;
            }
            Iterator kids;
            if (col != null) {
                kids = col.getTableColumnChildren();
            } else {
                kids = null;
            }
            String styleClass;
            if (kids != null && kids.hasNext()) {
                styleClass = ThemeStyles.TABLE_MULTIPLE_HEADER_TEXT;
            } else {
                styleClass = ThemeStyles.TABLE_HEADER_TEXT;
            }
            //  If TableColumn has a parent, then it is a leaf node.
            if (parent != null) {
                writer.writeText("\n", null);
                writer.startElement("table", component);
                writer.writeAttribute("class",
                        getTheme().getStyleClass(
                                ThemeStyles.TABLE_HEADER_TABLE), null);
                writer.writeAttribute("border", "0", null);
                writer.writeAttribute("cellpadding", "0", "cellPadding");
                writer.writeAttribute("cellspacing", "0", "cellSpacing");
                writer.writeText("\n", null);
                writer.startElement("tr", component);
                writer.writeText("\n", null);
                writer.startElement("td", component);
                writer.startElement("span", component);
                writer.writeAttribute("class",
                        getTheme().getStyleClass(styleClass), null);

                // Render header text.
                if (col != null && col.getHeaderText() != null) {
                    writer.writeText(col.getHeaderText(), null);
                }
                writer.endElement("span");
                writer.endElement("td");
                writer.endElement("tr");
                writer.endElement("table");
            } else {
                writer.startElement("span", component);
                writer.writeAttribute("class",
                        getTheme().getStyleClass(styleClass), null);

                // Render header text.
                if (col != null && col.getHeaderText() != null) {
                    writer.writeText(col.getHeaderText(), null);
                }
                writer.endElement("span");
            }
        }
    }

    /**
     * Render select column headers for TableHeader components.
     *
     * @param context FacesContext for the current request.
     * @param component TableHeader to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderSelectHeader(final FacesContext context,
            final TableHeader component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderSelectHeader",
                    "Cannot render select header, TableHeader is null");
            return;
        }

        // Get facet.
        TableColumn col = component.getTableColumnAncestor();
        UIComponent facet;
        if (col != null) {
            facet = col.getFacet(TableColumn.HEADER_FACET);
        } else {
            facet = null;
        }
        if (facet != null && facet.isRendered()) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            // Render layout table.
            writer.writeText("\n", null);
            writer.startElement("table", component);
            writer.writeAttribute("class",
                    getTheme().getStyleClass(ThemeStyles.TABLE_HEADER_TABLE),
                    null);
            writer.writeAttribute("border", "0", null);
            writer.writeAttribute("cellpadding", "0", "cellPadding");
            writer.writeAttribute("cellspacing", "0", "cellSpacing");
            writer.writeText("\n", null);
            writer.startElement("tr", component);

            // Render sort title.
            writer.writeText("\n", null);
            writer.startElement("td", component);
            writer.writeAttribute("align", component.getAlign(), null);
            RenderingUtilities.renderComponent(
                    component.getSelectSortButton(), context);
            writer.endElement("td");

            // Get sort button.
            UIComponent child = null;
            if (component.getSortLevel() > 0) {
                child = component.getToggleSortButton();
            } else if (component.getSortCount() > 0) {
                // Render add button when any sort has been applied.
                child = component.getAddSortButton();
            }

            // Render sort button.
            if (child != null) {
                writer.writeText("\n", null);
                writer.startElement("td", component);
                RenderingUtilities.renderComponent(child, context);
                writer.endElement("td");

                // Set the focus to the current sort control.
                setSortFocus(context, component, child);
            }
            writer.endElement("tr");
            writer.endElement("table");

        }
    }

    /**
     * Render sort column headers for TableHeader components.
     *
     * @param context FacesContext for the current request.
     * @param component TableHeader to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderSortHeader(final FacesContext context,
            final TableHeader component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderSortHeader",
                    "Cannot render sort header, TableHeader is null");
            return;
        }

        // Get facet.
        TableColumn col = component.getTableColumnAncestor();
        UIComponent facet;
        if (col != null) {
            facet = col.getFacet(TableColumn.HEADER_FACET);
        } else {
            facet = null;
        }
        if (facet != null && facet.isRendered()) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            // Render layout table so sort link/button may wrap properly.
            writer.writeText("\n", null);
            writer.startElement("table", component);
            writer.writeAttribute("class",
                    getTheme().getStyleClass(ThemeStyles.TABLE_HEADER_TABLE),
                    null);
            writer.writeAttribute("border", "0", null);
            writer.writeAttribute("cellpadding", "0", "cellPadding");
            writer.writeAttribute("cellspacing", "0", "cellSpacing");
            writer.writeText("\n", null);
            writer.startElement("tr", component);

            // Render sort link.
            if (col != null && col.getHeaderText() != null) {
                writer.writeText("\n", null);
                writer.startElement("td", component);
                writer.writeAttribute("align",
                        component.getAlign(), null);
                RenderingUtilities.renderComponent(
                        component.getPrimarySortLink(), context);
                writer.endElement("td");
            }

            // Get sort button.
            UIComponent child;
            if (component.getSortLevel() > 0) {
                child = component.getToggleSortButton();
            } else if (component.getSortCount() > 0) {
                // Render add button when any sort has been applied.
                child = component.getAddSortButton();
            } else {
                // Render primary sort button when no sorting has been applied.
                child = component.getPrimarySortButton();
            }
            if (child != null) {
                // Set the focus to the current sort control.
                setSortFocus(context, component, child);
            }

            // Render sort button.
            writer.writeText("\n", null);
            writer.startElement("td", component);
            RenderingUtilities.renderComponent(child, context);
            writer.endElement("td");
            writer.endElement("tr");
            writer.endElement("table");
        }
    }

    /**
     * Render group header for TableHeader components.
     *
     * @param context FacesContext for the current request.
     * @param component TableHeader to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderGroupHeader(final FacesContext context,
            final TableHeader component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderGroupHeader",
                    "Cannot render group header, TableHeader is null");
            return;
        }

        TableRowGroup group = component.getTableRowGroupAncestor();
        if (group == null) {
            log("renderGroupHeader",
                    "Cannot render group header, TableRowGroup is null");
            return;
        }

        // Header facet should override header text.
        UIComponent facet = group.getFacet(TableRowGroup.HEADER_FACET);
        boolean renderHeader = facet != null
                && facet.isRendered();
        boolean renderText = group.getHeaderText() != null
                && !renderHeader;

        // Don't render controls for an empty group.
        boolean renderTableRowGroupControls = !isEmptyGroup(component)
                && (group.isSelectMultipleToggleButton()
                || group.isGroupToggleButton());

        if (renderText || renderTableRowGroupControls) {
            // Render alignment span.
            Theme theme = getTheme();
            writer.writeText("\n", null);
            writer.startElement("span", component);
            writer.writeAttribute("class",
                    theme.getStyleClass(ThemeStyles.TABLE_GROUP_HEADER_LEFT),
                    null);

            // Render table row group controls.
            if (renderTableRowGroupControls) {
                // Render select multiple toggle button.
                if (group.isSelectMultipleToggleButton()) {
                    writer.writeText("\n", null);
                    RenderingUtilities.renderComponent(
                            component.getSelectMultipleToggleButton(), context);
                }

                // Do not render warning icon unless selectId is used.
                if (getSelectId(component) != null) {
                    writer.writeText("\n", null);
                    writer.startElement("span", component);
                    writer.writeAttribute("class",
                            theme.getStyleClass(
                                    ThemeStyles.TABLE_GROUP_HEADER_IMAGE),
                            null);
                    writer.writeText("\n", null);
                    RenderingUtilities.renderComponent(
                            component.getWarningIcon(), context);
                    writer.endElement("span");
                }

                // Render group toggle button.
                if (group.isGroupToggleButton()) {
                    writer.writeText("\n", null);
                    RenderingUtilities.renderComponent(
                            component.getGroupPanelToggleButton(), context);

                    // Render hidden field for collapsed property.
                    RenderingUtilities.renderComponent(
                            component.getCollapsedHiddenField(), context);
                }

                // Add space between controls and facet text.
                if (!renderText) {
                    writer.write("&nbsp;");
                }
            }

            // Render text.
            if (renderText) {
                writer.writeText("\n", null);
                writer.startElement("span", component);
                writer.writeAttribute("class",
                        theme.getStyleClass(
                                ThemeStyles.TABLE_GROUP_HEADER_TEXT),
                        null);
                writer.writeText(group.getHeaderText(), null);
                writer.endElement("span");
            }
            writer.endElement("span");
        } else {
            log("renderGroupHeader",
                    "Group controls not rendered, empty group found");
        }

        // Render facet.
        if (renderHeader) {
            RenderingUtilities.renderComponent(facet, context);
        }
    }

    /**
     * Render enclosing tag for TableHeader components.
     *
     * @param context FacesContext for the current request.
     * @param component TableHeader to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagStart(final FacesContext context,
            final TableHeader component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderEnclosingTagStart",
                    "Cannot render enclosing tag, TableHeader is null");
            return;
        }

        writer.writeText("\n", null);
        writer.startElement("th", component);
        writer.writeAttribute("id", component.getClientId(context), null);

        // Render style class.
        String extraHtml = RenderingUtilities.renderStyleClass(context, writer,
                component, getStyleClass(component), component.getExtraHtml());

        // Render colspan.
        if (component.getColSpan() > -1
                && (extraHtml == null || !extraHtml.contains("colspan="))) {
            writer.writeAttribute("colspan",
                    Integer.toString(component.getColSpan()), null);
        }

        // Render rowspan.
        if (component.getRowSpan() > -1
                && (extraHtml == null || !extraHtml.contains("rowspan="))) {
            writer.writeAttribute("rowspan",
                    Integer.toString(component.getRowSpan()), null);
        }

        // Render nowrap.
        if (component.isNoWrap()
                && (extraHtml == null || !extraHtml.contains("nowrap="))) {
            writer.writeAttribute("nowrap", "nowrap", null);
        }

        // Render tooltip.
        if (component.getToolTip() != null
                && (extraHtml == null || !extraHtml.contains("title="))) {
            writer.writeAttribute("title", component.getToolTip(), "toolTip");
        }

        // Render pass through attributes.
        RenderingUtilities.writeStringAttributes(component, writer,
                STRING_ATTRIBUTES, extraHtml);
    }

    /**
     * Render enclosing tag for TableHeader components.
     *
     * @param context FacesContext for the current request.
     * @param component TableHeader to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagEnd(final FacesContext context,
            final TableHeader component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderEnclosingTagEnd",
                    "Cannot render enclosing tag, TableHeader is null");
            return;
        }
        writer.endElement("th");
    }

    /**
     * Helper method to get the selectId from TableRowGroup components.
     *
     * @param component The TableHeader component to be rendered.
     * @return The first selectId property found.
     */
    private String getSelectId(final TableHeader component) {

        String selectId = null;
        TableRowGroup group;
        if (component != null) {
            group = component.getTableRowGroupAncestor();
        } else {
            group = null;
        }
        if (group == null) {
            log("getSelectId",
                    "Cannot obtain select Id, TableRowGroup is null");
            return selectId;
        }

        Iterator kids = group.getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn col = (TableColumn) kids.next();
            if (!col.isRendered()) {
                continue;
            }
            selectId = getSelectId(col);
            if (selectId != null) {
                break;
            }
        }
        return selectId;
    }

    /**
     * Get the selectId from nested TableColumn components.
     *
     * @param component TableColumn to be rendered.
     * @return The first selectId property found.
     */
    private String getSelectId(final TableColumn component) {
        if (component == null) {
            log("getSelectId",
                    "Cannot obtain select Id, TableColumn is null");
            return null;
        }

        // Render nested TableColumn children.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                String selectId = getSelectId(col);
                if (selectId != null) {
                    return selectId;
                }
            }
        }
        return component.getSelectId();
    }

    /**
     * Helper method to get style class for TableHeader components.
     *
     * @param component TableHeader to be rendered.
     * @return The style class.
     */
    private String getStyleClass(final TableHeader component) {
        String styleClass = null;
        if (component == null) {
            log("getStyleClass",
                    "Cannot obtain style class, TableHeader is null");
            return styleClass;
        }

        // Get style class.
        if (component.isGroupHeader()) {
            styleClass = ThemeStyles.TABLE_GROUP_HEADER;
        } else {
            TableColumn col = component.getTableColumnAncestor();
            if (col != null && col.isSpacerColumn()) {
                styleClass = ThemeStyles.TABLE_TD_SPACER;
            } else {
                // Test for nested column headers.
                TableColumn parent;
                if (col != null) {
                    parent = col.getTableColumnAncestor();
                } else {
                    parent = null;
                }
                Iterator kids;
                if (col != null) {
                    kids = col.getTableColumnChildren();
                } else {
                    kids = null;
                }
                if (kids != null && kids.hasNext()) {
                    // If TableColumn has kids, then it is a root node.
                    styleClass = ThemeStyles.TABLE_MULTIPLE_HEADER_ROOT;
                } else if (component.getSortLevel() == 1) {
                    if (parent != null) {
                        // If TableColumn has a parent, then it is a leaf node.
                        styleClass = ThemeStyles.TABLE_MULTIPLE_HEADER_SORT;
                    } else if (component.isSelectHeader()) {
                        styleClass = ThemeStyles.TABLE_HEADER_SELECTCOL_SORT;
                    } else {
                        styleClass = ThemeStyles.TABLE_HEADER_SORT;
                    }
                } else {
                    if (parent != null) {
                        // If TableColumn has a parent, then it is a leaf node.
                        styleClass = ThemeStyles.TABLE_MULTIPLE_HEADER;
                    } else if (component.isSelectHeader()) {
                        styleClass = ThemeStyles.TABLE_HEADER_SELECTCOL;
                    } else {
                        styleClass = ThemeStyles.TABLE_HEADER;
                    }
                }
            }
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
     * Helper method to determine if group is empty.
     * @param component TableHeader component
     * @return true if empty, else false.
     */
    private boolean isEmptyGroup(final TableHeader component) {
        boolean result = false;
        if (component == null) {
            log("isEmptyGroup",
                    "Cannot determine if group is empty, TableHeader is null");
            return result;
        }

        TableRowGroup group = component.getTableRowGroupAncestor();
        if (group != null) {
            result = (group.getRowCount() == 0);
        } else {
            log("isEmptyGroup",
                    "Cannot determine if group is empty, TableRowGroup is"
                            + " null");
        }
        return result;
    }

    /**
     * Log fine messages.
     * @param method method to log
     * @param msg message to log
     */
    private void log(final String method, final String msg) {
        // Get class.
        Class clazz = TableActionsRenderer.class;
        if (LogUtil.fineEnabled(clazz)) {
            // Log method name and message.
            LogUtil.fine(clazz, clazz.getName() + "." + method + ": " + msg);
        }
    }

    /**
     * Set focus when sort buttons are displayed.
     * @param context faces context
     * @param tableHeader TableHeader component
     * @param sortControl sort control component
     */
    private void setSortFocus(final FacesContext context,
            final TableHeader tableHeader, final UIComponent sortControl) {

        // Get the client ID of the last component to have focus.
        // If it's null do nothing.
        //
        String id = FocusManager.getRequestFocusElementId(context);
        if (id == null) {
            return;
        }

        // Get prefix for all IDs.
        String prefix = tableHeader.getClientId(context)
                + UINamingContainer.getSeparatorChar(context);

        // Set component focus if any match was found. Don't include select
        // sort button here as that component does not change.
        if (id.equals(prefix + TableHeader.ADD_SORT_BUTTON_ID)
                || id.equals(prefix + TableHeader.PRIMARY_SORT_BUTTON_ID)
                || id.equals(prefix + TableHeader.TOGGLE_SORT_BUTTON_ID)) {
            FocusManager.setRequestFocusElementId(context,
                    RenderingUtilities.getFocusElementId(context, sortControl));
        }
    }
}
