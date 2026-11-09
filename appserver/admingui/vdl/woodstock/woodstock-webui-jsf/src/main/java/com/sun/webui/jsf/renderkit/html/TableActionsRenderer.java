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
import com.sun.webui.jsf.component.TableActions;
import com.sun.webui.jsf.component.TableRowGroup;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.FocusManager;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * This class renders TableActions components.
 *
 * <pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.webui.jsf.renderkit.html.TableActionsRenderer.level = FINE
 * </pre>
 *
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.TableActions"))
public final class TableActionsRenderer extends jakarta.faces.render.Renderer {

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

        TableActions action = (TableActions) component;
        ResponseWriter writer = context.getResponseWriter();
        renderEnclosingTagStart(context, action, writer);
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

        TableActions action = (TableActions) component;
        ResponseWriter writer = context.getResponseWriter();

        // Render actions.
        if (action.isActionsBottom()) {
            renderActionsBottom(context, action, writer);
        } else {
            renderActionsTop(context, action, writer);
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

        TableActions action = (TableActions) component;
        ResponseWriter writer = context.getResponseWriter();
        renderEnclosingTagEnd(context, action, writer);
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Render the top actions for TableActions components.
     *
     * @param context FacesContext for the current request.
     * @param component TableActions to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    @SuppressWarnings("checkstyle:methodlength")
    protected void renderActionsTop(final FacesContext context,
            final TableActions component, final ResponseWriter writer)
            throws IOException {

        Table table;
        if (component != null) {
            table = component.getTableAncestor();
        } else {
            table = null;
        }
        if (table == null) {
            log("renderActionsTop", "Cannot render actions bar, Table is null");
            return;
        }

        // We must determine if all TableRowGroup components are empty. Controls
        // are only hidden when all row groups are empty. Likewise, pagination
        // controls are only hidden when all groups fit on a single page.
        int totalRows = table.getRowCount();
        boolean emptyTable = (totalRows == 0);
        boolean singleRow = (totalRows == 1);
        boolean singlePage = (totalRows <= table.getRows());

        // Get facets.
        UIComponent actions = table.getFacet(Table.ACTIONS_TOP_FACET);
        UIComponent filter = table.getFacet(Table.FILTER_FACET);
        UIComponent sort = table.getFacet(Table.SORT_PANEL_FACET);
        UIComponent prefs = table.getFacet(Table.PREFERENCES_PANEL_FACET);

        // Flags indicating which facets to render.
        boolean renderActions = actions != null
                && actions.isRendered();
        boolean renderFilter = filter != null
                && filter.isRendered();
        boolean renderSort = sort != null
                && sort.isRendered();
        boolean renderPrefs = prefs != null
                && prefs.isRendered();

        // Hide sorting and pagination controls for an empty table or when there
        // is only a single row.
        boolean renderSelectMultipleButton = !emptyTable
                && table.isSelectMultipleButton();
        boolean renderDeselectMultipleButton = !emptyTable
                && table.isDeselectMultipleButton();
        boolean renderDeselectSingleButton = !emptyTable
                && table.isDeselectSingleButton();
        boolean renderClearTableSortButton = !emptyTable
                && !singleRow
                && table.isClearSortButton();
        boolean renderTableSortPanelToggleButton = !emptyTable
                && !singleRow
                && (table.isSortPanelToggleButton()
                || renderSort);
        boolean renderPaginateButton = !emptyTable
                && !singlePage
                && table.isPaginateButton();

        // Return if nothing is rendered.
        if (!(renderActions
                || renderFilter
                || renderPrefs
                || renderSelectMultipleButton
                || renderDeselectMultipleButton
                || renderDeselectSingleButton
                || renderClearTableSortButton
                || renderTableSortPanelToggleButton
                || renderPaginateButton)) {
            log("renderActionsTop",
                    "Actions bar not rendered, nothing to display");
            return;
        }

        // Render select multiple button.
        if (renderSelectMultipleButton) {
            writer.writeText("\n", null);
            RenderingUtilities.renderComponent(
                    component.getSelectMultipleButton(), context);
        }

        // Render deselect multiple button.
        if (renderDeselectMultipleButton) {
            writer.writeText("\n", null);
            RenderingUtilities.renderComponent(
                    component.getDeselectMultipleButton(), context);
        }

        // Render deselect single button.
        if (renderDeselectSingleButton) {
            writer.writeText("\n", null);
            RenderingUtilities.renderComponent(
                    component.getDeselectSingleButton(), context);
        }

        // Render actions facet.
        if (renderActions) {
            // Render action separator.
            if (renderSelectMultipleButton
                    || renderDeselectMultipleButton
                    || renderDeselectSingleButton) {
                writer.writeText("\n", null);
                RenderingUtilities.renderComponent(
                        component.getActionsSeparatorIcon(), context);
            }
            writer.writeText("\n", null);
            RenderingUtilities.renderComponent(actions, context);
        }

        // Render filter facet.
        if (renderFilter) {
            // Render filter separator.
            if (renderActions
                    || renderSelectMultipleButton
                    || renderDeselectMultipleButton
                    || renderDeselectSingleButton) {
                writer.writeText("\n", null);
                RenderingUtilities.renderComponent(
                        component.getFilterSeparatorIcon(), context);
            }

            writer.writeText("\n", null);
            RenderingUtilities.renderComponent(component.getFilterLabel(),
                    context);
            writer.writeText("\n", null);
            RenderingUtilities.renderComponent(filter, context);
        }

        // Render view action separator.
        if ((renderActions
                || renderFilter
                || renderSelectMultipleButton
                || renderDeselectMultipleButton
                || renderDeselectSingleButton)
                && (renderPrefs
                || renderClearTableSortButton
                || renderTableSortPanelToggleButton)) {
            writer.writeText("\n", null);
            RenderingUtilities.renderComponent(
                    component.getViewActionsSeparatorIcon(), context);
        }

        // Render table sort panel toggle button.
        if (renderTableSortPanelToggleButton) {
            writer.writeText("\n", null);
            UIComponent child = component.getSortPanelToggleButton();
            RenderingUtilities.renderComponent(child, context);
        }

        // Render clear sort button.
        if (renderClearTableSortButton) {
            writer.writeText("\n", null);
            RenderingUtilities.renderComponent(
                    component.getClearSortButton(), context);
        }

        // Render table preferences panel toggle button.
        if (renderPrefs) {
            writer.writeText("\n", null);
            RenderingUtilities.renderComponent(
                    component.getPreferencesPanelToggleButton(), context);
        }

        // Render paginate button.
        if (renderPaginateButton) {
            // Render separator.
            if (renderActions
                    || renderFilter
                    || renderPrefs
                    || renderSelectMultipleButton
                    || renderDeselectMultipleButton
                    || renderDeselectSingleButton
                    || renderClearTableSortButton
                    || renderTableSortPanelToggleButton) {
                writer.writeText("\n", null);
                RenderingUtilities.renderComponent(
                        component.getPaginateSeparatorIcon(), context);
            }
            writer.writeText("\n", null);
            RenderingUtilities.renderComponent(component.getPaginateButton(),
                    context);
        }
    }

    /**
     * Render the bottom actions for TableActions components.
     *
     * @param context FacesContext for the current request.
     * @param component TableActions to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderActionsBottom(final FacesContext context,
            final TableActions component, final ResponseWriter writer)
            throws IOException {

        Table table;
        if (component != null) {
            table = component.getTableAncestor();
        } else {
            table = null;
        }
        if (table == null) {
            log("renderActionsBottom",
                    "Cannot render actions bar, Table is null");
            return;
        }

        // We must determine if all TableRowGroup components are empty. Controls
        // are only hidden when all row groups are empty. Likewise, pagination
        // controls are only hidden when all groups fit on a single page.
        int totalRows = table.getRowCount();
        boolean emptyTable = (totalRows == 0);
        boolean singleRow = (totalRows == 1);
        boolean singlePage = (totalRows <= table.getRows());

        // Get facets.
        UIComponent actions = table.getFacet(Table.ACTIONS_BOTTOM_FACET);

        // Get flag indicating which facets to render.
        boolean renderActions = !emptyTable
                && !singleRow
                && actions != null
                && actions.isRendered();

        // Hide pagination controls when all rows fit on a page.
        boolean renderPaginationControls = !emptyTable
                && !singlePage
                && table.isPaginationControls();

        // Hide paginate button for a single row.
        boolean renderPaginateButton = !emptyTable
                && !singlePage
                && table.isPaginateButton();

        // Render table actions facet.
        if (renderActions) {
            writer.writeText("\n", null);
            RenderingUtilities.renderComponent(actions, context);
        }

        // Render actions separator.
        if (renderPaginationControls || renderPaginateButton) {
            // Render actions separator.
            if (renderActions) {
                writer.writeText("\n", null);
                RenderingUtilities.renderComponent(
                        component.getActionsSeparatorIcon(), context);
            }
        }

        // Render pagination controls.
        if (renderPaginationControls) {
            // Get TableRowGroup component.
            TableRowGroup group = table.getTableRowGroupChild();
            boolean paginated;
            if (group != null) {
                paginated = group.isPaginated();
            } else {
                paginated = false;
            }

            // Do not display controls while in scroll mode.
            if (paginated) {
                renderPagination(context, component, writer);
            }
        }

        // Render paginate button.
        if (renderPaginateButton) {
            writer.writeText("\n", null);
            RenderingUtilities.renderComponent(
                    component.getPaginateButton(), context);
        }
    }

    /**
     * Render enclosing tag for TableActions components.
     *
     * @param context FacesContext for the current request.
     * @param component TableActions to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagStart(final FacesContext context,
            final TableActions component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderEnclosingTagStart",
                    "Cannot render enclosing tag, TableActions is null");
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
     * Render enclosing tag for TableActions components.
     *
     * @param context FacesContext for the current request.
     * @param component TableActions to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagEnd(final FacesContext context,
            final TableActions component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderEnclosingTagEnd",
                    "Cannot render enclosing tag, TableActions is null");
            return;
        }
        writer.endElement("td");
    }

    /**
     * Helper method to get style class for TableActions components.
     *
     * @param component TableActions to be rendered
     * @return The style class.
     */
    private String getStyleClass(final TableActions component) {
        String styleClass = null;
        if (component == null) {
            log("getStyleClass",
                    "Cannot obtain style class, TableActions is null");
            return styleClass;
        }

        // Get style class.
        if (component.isActionsBottom()) {
            styleClass = ThemeStyles.TABLE_ACTION_TD_LASTROW;
        } else {
            styleClass = ThemeStyles.TABLE_ACTION_TD;
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
        Class clazz = TableActionsRenderer.class;
        if (LogUtil.fineEnabled(clazz)) {
            // Log method name and message.
            LogUtil.fine(clazz, clazz.getName() + "." + method + ": " + msg);
        }
    }

    /**
     * Render the pagination controls for TableActions components. This does not
     * include the paginate button.
     *
     * @param context FacesContext for the current request.
     * @param component TableActions to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderPagination(final FacesContext context,
            final TableActions component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderPagination",
                    "Cannot render pagination controls, TableActions is null");
            return;
        }

        Theme theme = getTheme();

        // Used to deal with setting up focus when pagination controls
        // are disabled.
        UIComponent paginationControl;
        UIComponent paginationPageField = component.getPaginationPageField();

        // Render span for left-side buttons.
        writer.writeText("\n", null);
        writer.startElement("span", component);
        writer.writeAttribute("class", theme.getStyleClass(
                ThemeStyles.TABLE_PAGINATION_LEFT_BUTTON), null);

        // If the focus is on a diasbled pagination control
        // place the focus on the paginationPageField instead
        // Do this for each of the pagination controls, first, prev,
        // next, and last.
        //
        paginationControl = component.getPaginationFirstButton();
        setPaginationFocus(context, paginationControl,
                paginationPageField);

        // Render first button.
        writer.writeText("\n", null);
        RenderingUtilities.renderComponent(paginationControl, context);

        paginationControl = component.getPaginationPrevButton();
        setPaginationFocus(context, paginationControl, paginationPageField);

        // Render prev button.
        writer.writeText("\n", null);
        RenderingUtilities.renderComponent(paginationControl, context);
        writer.endElement("span");

        // Render span for label.
        writer.writeText("\n", null);
        writer.startElement("span", component);
        writer.writeAttribute("class", theme.getStyleClass(
                ThemeStyles.TABLE_PAGINATION_TEXT_BOLD), null);

        // Render page field.
        writer.writeText("\n", null);
        RenderingUtilities.renderComponent(paginationPageField, context);
        writer.endElement("span");

        // Render total pages text.
        writer.writeText("\n", null);
        RenderingUtilities.renderComponent(component.getPaginationPagesText(),
                context);

        // Render span for submit button.
        writer.writeText("\n", null);
        writer.startElement("span", component);
        writer.writeAttribute("class", theme.getStyleClass(
                ThemeStyles.TABLE_PAGINATION_SUBMIT_BUTTON), null);

        // Render submit button.
        writer.writeText("\n", null);
        RenderingUtilities.renderComponent(
                component.getPaginationSubmitButton(), context);
        writer.endElement("span");

        // Render span for right-side buttons.
        writer.writeText("\n", null);
        writer.startElement("span", component);
        writer.writeAttribute("class", theme.getStyleClass(
                ThemeStyles.TABLE_PAGINATION_RIGHT_BUTTON), null);

        paginationControl = component.getPaginationNextButton();
        setPaginationFocus(context, paginationControl, paginationPageField);

        // Render next button.
        writer.writeText("\n", null);
        RenderingUtilities.renderComponent(paginationControl, context);

        paginationControl = component.getPaginationLastButton();
        setPaginationFocus(context, paginationControl, paginationPageField);

        // Render last button.
        writer.writeText("\n", null);
        RenderingUtilities.renderComponent(paginationControl, context);
        writer.endElement("span");
    }

    /**
     * Set focus when pagination buttons are disabled.
     * @param context faces context
     * @param paginationControl pagination control
     * @param paginationPageField pagination page field
     */
    private void setPaginationFocus(final FacesContext context,
            final UIComponent paginationControl,
            final UIComponent paginationPageField) {

        // If there is not current focus, or no pagination control
        // or pagination page field, do nothing.
        String focusId = FocusManager.getRequestFocusElementId(context);
        if (focusId == null || paginationControl == null
                || paginationPageField == null) {
            return;
        }

        // If the pagination control does not have a disabled attribute
        // or it does and is not disabled, do nothing.
        //
        Boolean disabled = (Boolean) paginationControl.getAttributes()
                .get("disabled");
        if (disabled == null || !disabled) {
            return;
        }

        // If the current focus element is not the pagination control
        // do nothing.
        String id = RenderingUtilities.getFocusElementId(context,
                paginationControl);
        if (!focusId.equals(id)) {
            return;
        }

        // The pagination control is disabled and is the
        // current element to receive the focus.
        //
        // Get the focus element id for the pagination page field
        // and set it to receive the focus.
        FocusManager.setRequestFocusElementId(context,
                RenderingUtilities.getFocusElementId(context,
                        paginationPageField));
    }
}
