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

import com.sun.data.provider.SortCriteria;
import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.Alarm;
import com.sun.webui.jsf.component.TableColumn;
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
import java.util.Iterator;

/**
 * This class renders TableColumn components.
 * <p>
 * The tableColumn component provides a layout mechanism for displaying columns
 * of data. UI guidelines describe specific behavior that can applied to the
 * rows and columns of data such as sorting, filtering, pagination, selection,
 * and custom user actions. In addition, UI guidelines also define sections of
 * the table that can be used for titles, row group headers, and placement of
 * pre-defined and user defined actions.
 * </p>
 * <p>
 * <pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.webui.jsf.renderkit.html.TableColumnRenderer.level = FINE
 * </pre>
 * </p>
 * <p>
 * See TLD docs for more information.
 * </p>
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.TableColumn"))
public final class TableColumnRenderer extends jakarta.faces.render.Renderer {

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

        // Don't render TableColumn parents.
        TableColumn col = (TableColumn) component;
        Iterator kids = col.getTableColumnChildren();
        if (!kids.hasNext()) {
            ResponseWriter writer = context.getResponseWriter();
            renderEnclosingTagStart(context, col, writer);
        }
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

        // Don't render TableColumn parents.
        TableColumn col = (TableColumn) component;
        Iterator kids = col.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn kid = (TableColumn) kids.next();
                if (!kid.isRendered()) {
                    log("encodeChildren",
                            "TableColumn not rendered, nothing to display");
                    continue;
                }
                RenderingUtilities.renderComponent(kid, context);
            }
        } else {
            // Render empty cell image. Don't do this automatically because we
            // cannot reconize truly null values, such as an empty alarm cell
            // or a comment field which is blank, neither of which should have
            // the dash image. Further, the dash image is not used for cells
            // that contain user interface elements such as checkboxes or
            // drop-down lists when these elements are not applicable.
            // Instead, the elements are simply not displayed.
            if (col.isEmptyCell()) {
                RenderingUtilities.renderComponent(col.getEmptyCellIcon(),
                        context);
            } else {
                // Render children.
                kids = col.getChildren().iterator();
                while (kids.hasNext()) {
                    UIComponent kid = (UIComponent) kids.next();
                    if (!kid.isRendered()) {
                        log("encodeChildren",
                                "TableColumn child not rendered, nothing to"
                                        + " display");
                        continue;
                    }
                    RenderingUtilities.renderComponent(kid, context);

                    // Render a separator bar between each child component.
                    if (kids.hasNext() && col.isEmbeddedActions()) {
                        RenderingUtilities.renderComponent(
                                col.getEmbeddedActionSeparatorIcon(), context);
                        ResponseWriter writer = context.getResponseWriter();
                        writer.writeText(" ", null);
                    }
                }
            }
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

        // Don't render TableColumn parents.
        TableColumn col = (TableColumn) component;
        Iterator kids = col.getTableColumnChildren();
        if (!kids.hasNext()) {
            ResponseWriter writer = context.getResponseWriter();
            renderEnclosingTagEnd(context, col, writer);
        }
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Render enclosing tag for TableColumn components.
     *
     * @param context FacesContext for the current request.
     * @param component TableColumn to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagStart(final FacesContext context,
            final TableColumn component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderEnclosingTagStart",
                    "Cannot render enclosing tag, UIComponent is null");
            return;
        }

        // Note: Row header is not valid for select column.
        boolean isRowHeader = component.isRowHeader()
                && component.getSelectId() == null;

        writer.writeText("\n", null);
        if (isRowHeader) {
            writer.startElement("th", component);
        } else {
            writer.startElement("td", component);
        }

        // Render client id.
        writer.writeAttribute("id", component.getClientId(context), null);

        // Render style class.
        RenderingUtilities.renderStyleClass(context, writer, component,
                getStyleClass(component));

        // Render align.
        if (component.getAlign() != null) {
            writer.writeAttribute("align", component.getAlign(), null);
        }

        // Render scope.
        if (isRowHeader) {
            writer.writeAttribute("scope", "row", null);
        } else if (component.getScope() != null) {
            writer.writeAttribute("scope", component.getScope(), null);
        }

        // Render colspan.
        if (component.getColSpan() > -1) {
            writer.writeAttribute("colspan",
                    Integer.toString(component.getColSpan()), null);
        }

        // Render rowspan.
        if (component.getRowSpan() > -1) {
            writer.writeAttribute("rowspan",
                    Integer.toString(component.getRowSpan()), null);
        }

        // Render nowrap.
        if (component.isNoWrap()) {
            writer.writeAttribute("nowrap", "nowrap", null);
        }

        // Render tooltip.
        if (component.getToolTip() != null) {
            writer.writeAttribute("title", component.getToolTip(), "toolTip");
        }

        // Render pass through attributes.
        RenderingUtilities.writeStringAttributes(component, writer,
                STRING_ATTRIBUTES);
    }

    /**
     * Render enclosing tag for TableColumn components.
     *
     * @param context FacesContext for the current request.
     * @param component TableColumn to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagEnd(final FacesContext context,
            final TableColumn component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderEnclosingTagEnd",
                    "Cannot render enclosing tag, UIComponent is null");
            return;
        }

        // Note: Row header is not valid for select column.
        if (component.isRowHeader() && component.getSelectId() == null) {
            writer.endElement("th");
        } else {
            writer.endElement("td");
        }
    }

    /**
     * Helper method to get column style class for TableColumn components.
     *
     * @param component TableColumn to be rendered
     *
     * @return The style class.
     */
    private String getStyleClass(final TableColumn component) {
        String styleClass = null;
        if (component == null) {
            log("getStyleClass",
                    "Cannot obtain style class, TableColumn is null");
            return styleClass;
        }

        // Get sort level.
        TableRowGroup group = component.getTableRowGroupAncestor();
        SortCriteria criteria = component.getSortCriteria();
        int level;
        if (group != null) {
            level = group.getSortLevel(criteria);
        } else {
            level = -1;
        }

        // Get style class.
        if (component.isSpacerColumn()) {
            styleClass = ThemeStyles.TABLE_TD_SPACER;
        } else if (component.getSeverity() != null
                && !component.getSeverity().equals(Alarm.SEVERITY_OK)) {
            styleClass = ThemeStyles.TABLE_TD_ALARM;
        } else if (level == 1) {
            if (component.getSelectId() != null) {
                styleClass = ThemeStyles.TABLE_TD_SELECTCOL_SORT;
            } else {
                styleClass = ThemeStyles.TABLE_TD_SORT;
            }
        } else {
            if (component.getSelectId() != null) {
                styleClass = ThemeStyles.TABLE_TD_SELECTCOL;
            } else {
                styleClass = ThemeStyles.TABLE_TD_LAYOUT;
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
        Class clazz = TableColumnRenderer.class;
        if (LogUtil.fineEnabled(clazz)) {
            // Log method name and message.
            LogUtil.fine(clazz, clazz.getName() + "." + method + ": " + msg);
        }
    }
}
