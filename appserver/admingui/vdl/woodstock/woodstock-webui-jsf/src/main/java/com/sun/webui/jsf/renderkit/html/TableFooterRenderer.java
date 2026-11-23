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
import com.sun.webui.jsf.component.Table;
import com.sun.webui.jsf.component.TableColumn;
import com.sun.webui.jsf.component.TableFooter;
import com.sun.webui.jsf.component.TableRowGroup;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * This class renders TableFooter components.
 *
 * <pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.webui.jsf.renderkit.html.TableFooterRenderer.level = FINE
 * </pre>
 *
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.TableFooter"))
public final class TableFooterRenderer extends jakarta.faces.render.Renderer {

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
        "style",
        "valign",
        "width"};

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

        TableFooter footer = (TableFooter) component;
        ResponseWriter writer = context.getResponseWriter();
        renderEnclosingTagStart(context, footer, writer);
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

        TableFooter footer = (TableFooter) component;
        ResponseWriter writer = context.getResponseWriter();

        // Render footers.
        if (footer.isGroupFooter()) {
            renderGroupFooter(context, footer, writer);
        } else if (footer.isTableColumnFooter()) {
            renderTableColumnFooter(context, footer, writer);
        } else if (footer.isTableFooter()) {
            renderTableFooter(context, footer, writer);
        } else {
            renderColumnFooter(context, footer, writer);
        }
    }

    @Override
    public void encodeEnd(final FacesContext context,
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

        TableFooter footer = (TableFooter) component;
        ResponseWriter writer = context.getResponseWriter();
        renderEnclosingTagEnd(context, footer, writer);
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Render column footer for TableFooter components.
     *
     * @param context FacesContext for the current request.
     * @param component TableFooter to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderColumnFooter(final FacesContext context,
            final TableFooter component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderColumnFooter",
                    "Cannot render column footer, TableFooter is null");
            return;
        }

        // Render facet.
        TableColumn col = component.getTableColumnAncestor();
        UIComponent facet;
        if (col != null) {
            facet = col.getFacet(TableColumn.FOOTER_FACET);
        } else {
            facet = null;
        }
        if (facet != null && facet.isRendered()) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            writer.startElement("span", component);
            writer.writeAttribute("class",
                    getTheme().getStyleClass(
                            ThemeStyles.TABLE_GROUP_COL_FOOTER_TEXT),
                    null);

            // Render footer text.
            if (col != null && col.getFooterText() != null) {
                writer.writeText(col.getFooterText(), null);
            }
            writer.endElement("span");
        }
    }

    /**
     * Render group footer for TableFooter components.
     *
     * @param context FacesContext for the current request.
     * @param component TableFooter to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderGroupFooter(final FacesContext context,
            final TableFooter component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderGroupFooter",
                    "Cannot render group footer, TableFooter is null");
            return;
        }

        // Render facet.
        TableRowGroup group = component.getTableRowGroupAncestor();
        UIComponent facet;
        if (group != null) {
            facet = group.getFacet(TableRowGroup.FOOTER_FACET);
        } else {
            facet = null;
        }
        if (facet != null && facet.isRendered()) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            writer.startElement("span", component);
            writer.writeAttribute("class",
                    getTheme().getStyleClass(
                            ThemeStyles.TABLE_GROUP_FOOTER_TEXT),
                    null);

            // Render text.
            if (group.getFooterText() != null) {
                writer.writeText(group.getFooterText(), null);
            }
            writer.endElement("span");
        }
    }

    /**
     * Render table column footer for TableFooter components.
     *
     * @param context FacesContext for the current request.
     * @param component TableFooter to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderTableColumnFooter(final FacesContext context,
            final TableFooter component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderTableColumnFooter",
                    "Cannot render table column footer, TableFooter is null");
            return;
        }

        // Render facet.
        TableColumn col = component.getTableColumnAncestor();
        UIComponent facet;
        if (col != null) {
            facet = col.getFacet(TableColumn.TABLE_FOOTER_FACET);
        } else {
            facet = null;
        }
        if (facet != null && facet.isRendered()) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            writer.startElement("span", component);
            writer.writeAttribute("class",
                    getTheme().getStyleClass(ThemeStyles.TABLE_COL_FOOTER_TEXT),
                    null);

            // Get TableColumn component.
            if (col != null && col.getTableFooterText() != null) {
                writer.writeText(col.getTableFooterText(), null);
            }
            writer.endElement("span");
        }
    }

    /**
     * Render table footer for TableFooter components.
     *
     * @param context FacesContext for the current request.
     * @param component TableFooter to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderTableFooter(final FacesContext context,
            final TableFooter component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderTableFooter",
                    "Cannot render table footer, TableFooter is null");
            return;
        }

        Table table = component.getTableAncestor();
        if (table == null) {
            log("renderTableFooter",
                    "Cannot render table footer, Table is null");
            return;
        }

        // Render facet.
        UIComponent facet = table.getFacet(Table.FOOTER_FACET);
        if (facet != null && facet.isRendered()) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            Theme theme = getTheme();

            // Get hidden selected rows text.
            String hiddenSelectionsText;
            if (table.isHiddenSelectedRows()) {
                hiddenSelectionsText = theme.getMessage(
                        "table.hiddenSelections",
                            new String[]{
                                Integer.toString(
                                        table.getHiddenSelectedRowsCount())
                            });
            } else {
                hiddenSelectionsText = null;
            }

            // If both footer and hidden selected rows are not null, the
            // table footer is left aigned and hidden selected rows is right
            // aligned. Otherwise, text should appear centered.
            if (hiddenSelectionsText != null
                    && table.getFooterText() != null) {
                writer.startElement("span", component);
                writer.writeAttribute("class",
                        theme.getStyleClass(ThemeStyles.TABLE_FOOTER_LEFT),
                        null);
                writer.writeText(table.getFooterText(), null);
                writer.endElement("span");
                writer.startElement("span", component);
                writer.writeAttribute("class",
                        theme.getStyleClass(
                                ThemeStyles.TABLE_FOOTER_MESSAGE_SPAN),
                        null);
                writer.writeText(hiddenSelectionsText, null);
                writer.endElement("span");
            } else {
                writer.startElement("span", component);
                writer.writeAttribute("class",
                        theme.getStyleClass(ThemeStyles.TABLE_FOOTER_TEXT),
                        null);
                if (table.getFooterText() != null) {
                    writer.writeText(table.getFooterText(), null);
                } else {
                    writer.writeText(hiddenSelectionsText, null);
                }
                writer.endElement("span");
            }
        }
    }

    /**
     * Render enclosing tag for TableFooter components.
     *
     * @param context FacesContext for the current request.
     * @param component TableFooter to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagStart(final FacesContext context,
            final TableFooter component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderEnclosingTagStart",
                    "Cannot render enclosing tag, TableFooter is null");
            return;
        }

        writer.writeText("\n", null);
        writer.startElement("td", component);
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
     * Render enclosing tag for TableFooter components.
     *
     * @param context FacesContext for the current request.
     * @param component TableFooter to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagEnd(final FacesContext context,
            final TableFooter component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderEnclosingTagEnd",
                    "Cannot render enclosing tag, TableFooter is null");
            return;
        }
        writer.endElement("td");
    }

    /**
     * Helper method to get style class for TableFooter components.
     *
     * @param component TableFooter to be rendered
     * @return The style class.
     */
    private String getStyleClass(final TableFooter component) {
        String styleClass = null;
        if (component == null) {
            log("getStyleClass",
                    "Cannot obtain style class, TableFooter is null");
            return styleClass;
        }

        // Get style class.
        if (component.isTableFooter()) {
            styleClass = ThemeStyles.TABLE_FOOTER;
        } else if (component.isGroupFooter()) {
            styleClass = ThemeStyles.TABLE_GROUP_FOOTER;
        } else {
            TableColumn col = component.getTableColumnAncestor();
            if (col != null && col.isSpacerColumn()) {
                if (component.isTableColumnFooter()) {
                    styleClass = ThemeStyles.TABLE_COL_FOOTER_SPACER;
                } else {
                    styleClass = ThemeStyles.TABLE_TD_SPACER;
                }
            } else if (component.getSortLevel() == 1) {
                if (component.isTableColumnFooter()) {
                    styleClass = ThemeStyles.TABLE_COL_FOOTER_SORT;
                } else {
                    styleClass = ThemeStyles.TABLE_GROUP_COL_FOOTER_SORT;
                }
            } else {
                if (component.isTableColumnFooter()) {
                    styleClass = ThemeStyles.TABLE_COL_FOOTER;
                } else {
                    styleClass = ThemeStyles.TABLE_GROUP_COL_FOOTER;
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
     * Log fine messages.
     * @param method method to log
     * @param msg message to log
     */
    private static void log(final String method, final String msg) {
        // Get class.
        Class clazz = TableFooterRenderer.class;
        if (LogUtil.fineEnabled(clazz)) {
            // Log method name and message.
            LogUtil.fine(clazz, clazz.getName() + "." + method + ": " + msg);
        }
    }
}
