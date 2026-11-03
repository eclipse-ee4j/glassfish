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
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.Label;
import com.sun.webui.jsf.component.Table;
import com.sun.webui.jsf.component.TablePanels;
import com.sun.webui.jsf.model.Option;
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

/**
 * This class renders TablePanels components.
 * <p>
 * <pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.webui.jsf.renderkit.html.TablePanelsRenderer.level = FINE
 * </pre>
 * </p>
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.TablePanels"))
public final class TablePanelsRenderer extends jakarta.faces.render.Renderer {

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

        TablePanels panel = (TablePanels) component;
        ResponseWriter writer = context.getResponseWriter();
        renderEnclosingTagStart(context, panel, writer);
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

        TablePanels panel = (TablePanels) component;
        ResponseWriter writer = context.getResponseWriter();

        // Render panels.
        if (panel.isFilterPanel()) {
            renderFilterPanel(context, panel, writer);
        }
        if (panel.isPreferencesPanel()) {
            renderPreferencesPanel(context, panel, writer);
        }
        renderSortPanel(context, panel, writer);
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

        TablePanels panel = (TablePanels) component;
        ResponseWriter writer = context.getResponseWriter();
        renderEnclosingTagEnd(context, panel, writer);
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Render filter panel for TablePanels components.
     *
     * @param context FacesContext for the current request.
     * @param component TablePanels to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderFilterPanel(final FacesContext context,
            final TablePanels component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderFilterPanel",
                    "Cannot render filter panel, TablePanels is null");
            return;
        }

        // Get facet.
        Table table = component.getTableAncestor();
        UIComponent facet;
        if (table != null) {
            facet = table.getFacet(Table.FILTER_PANEL_FACET);
        } else {
            facet = null;
        }
        if (!(facet != null && facet.isRendered())) {
            log("renderFilterPanel",
                    "Filter panel not rendered, nothing to display");
            return;
        }

        // Render filter panel.
        renderPanelStart(context, component, writer,
                TablePanels.FILTER_PANEL_ID, getTheme().getMessage(
                        "table.panel.filterTitle"), null);
        RenderingUtilities.renderComponent(facet, context);
        renderPanelEnd(writer);
    }

    /**
     * Render preferences panel for TablePanels components.
     *
     * @param context FacesContext for the current request.
     * @param component TablePanels to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderPreferencesPanel(final FacesContext context,
            final TablePanels component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderPreferencesPanel",
                    "Cannot render preferences panel, TablePanels is null");
            return;
        }

        // Get facet.
        Table table = component.getTableAncestor();
        UIComponent facet;
        if (table != null) {
            facet = table.getFacet(Table.PREFERENCES_PANEL_FACET);
        } else {
            facet = null;
        }
        if (!(facet != null && facet.isRendered())) {
            log("renderPreferencesPanel",
                    "Preferences panel not rendered, nothing to display");
            return;
        }

        // Render filter panel.
        renderPanelStart(context, component, writer,
                TablePanels.PREFERENCES_PANEL_ID,
                getTheme().getMessage("table.panel.preferencesTitle"), null);
        RenderingUtilities.renderComponent(facet, context);
        renderPanelEnd(writer);
    }

    /**
     * Render sort panel for TablePanels components.
     *
     * @param context FacesContext for the current request.
     * @param component TablePanels to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderSortPanel(final FacesContext context,
            final TablePanels component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderSortPanel",
                    "Cannot render sort panel, TablePanels is null");
            return;
        }

        // Render filter panel
        Theme theme = getTheme();
        renderPanelStart(context, component, writer, TablePanels.SORT_PANEL_ID,
                theme.getMessage("table.panel.sortTitle"),
                theme.getMessage("table.panel.help"));

        // Get facet.
        Table table = component.getTableAncestor();
        UIComponent facet;
        if (table != null) {
            facet = table.getFacet(Table.SORT_PANEL_FACET);
        } else {
            facet = null;
        }
        if (facet != null && facet.isRendered()) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            renderSortPanelLayout(context, component, writer);
        }
        renderPanelEnd(writer);
    }

    /**
     * Render enclosing tag for TablePanels components.
     *
     * @param context FacesContext for the current request.
     * @param component TablePanels to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagStart(final FacesContext context,
            final TablePanels component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderEnclosingTagStart",
                    "Cannot render enclosing tag, TablePanels is null");
            return;
        }

        Theme theme = getTheme();
        writer.writeText("\n", null);
        writer.startElement("td", component);
        writer.writeAttribute("id", component.getClientId(context), null);

        // Render style class.
        String extraHtml = RenderingUtilities.renderStyleClass(context, writer,
                component, theme.getStyleClass(ThemeStyles.TABLE_PANEL_TD),
                component.getExtraHtml());

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
     * Render enclosing tag for TablePanels components.
     *
     * @param context FacesContext for the current request.
     * @param component TablePanels to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagEnd(final FacesContext context,
            final TablePanels component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderEnclosingTagEnd",
                    "Cannot render enclosing tag, TablePanels is null");
            return;
        }
        writer.endElement("td");
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
     * Log fine messages.
     * @param method method to log
     * @param msg message to log
     */
    private static void log(final String method, final String msg) {
        // Get class.
        Class clazz = TablePanelsRenderer.class;
        if (LogUtil.fineEnabled(clazz)) {
            // Log method name and message.
            LogUtil.fine(clazz, clazz.getName() + "." + method + ": " + msg);
        }
    }

    /**
     * Helper method to render embedded panel for TablePanels components.
     *
     * @param context FacesContext for the current request.
     * @param component TablePanels to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     * @param id The identifier for the component.
     * @param title The title for the panel.
     * @param tip The help tip for the panel.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderPanelStart(final FacesContext context,
            final TablePanels component, final ResponseWriter writer,
            final String id, final String title, final String tip)
            throws IOException {

        if (component == null) {
            log("renderPanelStart", "Cannot render panel, TablePanels is null");
            return;
        }

        Theme theme = getTheme();
        writer.writeText("\n", null);
        writer.startElement("div", component);

        // Render client id.
        writer.writeAttribute("id", getId(component, id), null);

        // Render style (i.e., div is initially hidden).
        writer.writeAttribute("class", theme.getStyleClass(ThemeStyles.HIDDEN),
                null);

        // Render div used to create drop shadow effect.
        writer.writeText("\n", null);
        writer.startElement("div", component);

        // Render style class.
        writer.writeAttribute("class", theme.getStyleClass(
                ThemeStyles.TABLE_PANEL_SHADOW3_DIV), null);

        // Render div used to create drop shadow effect.
        writer.writeText("\n", null);
        writer.startElement("div", component);

        // Render style class.
        writer.writeAttribute("class", theme.getStyleClass(
                ThemeStyles.TABLE_PANEL_SHADOW2_DIV), null);

        // Render div used to create drop shadow effect.
        writer.writeText("\n", null);
        writer.startElement("div", component);

        // Render style class.
        writer.writeAttribute("class", theme.getStyleClass(
                ThemeStyles.TABLE_PANEL_SHADOW1_DIV), null);

        // Render div used to create the yellow box itself.
        writer.writeText("\n", null);
        writer.startElement("div", component);

        // Render style class.
        writer.writeAttribute("class", theme.getStyleClass(
                ThemeStyles.TABLE_PANEL_DIV), null);

        // Render div used to create help tip.
        if (tip != null) {
            writer.writeText("\n", null);
            writer.startElement("div", component);
            writer.writeAttribute("class", theme.getStyleClass(
                    ThemeStyles.TABLE_PANEL_HELP_TEXT), null);
            writer.writeText(tip, null);
            writer.endElement("div");
        }

        // Render div used to format the title in the box.
        writer.writeText("\n", null);
        writer.startElement("div", component);

        // Render style class.
        writer.writeAttribute("class", theme.getStyleClass(
                ThemeStyles.TABLE_PANEL_TITLE), null);

        // Render title.
        writer.writeText(title, null);
        writer.endElement("div");

        // Render div for whatever content goes in the box.
        writer.writeText("\n", null);
        writer.startElement("div", component);

        // Render style class.
        writer.writeAttribute("class", theme.getStyleClass(
                ThemeStyles.TABLE_PANEL_CONTENT), null);
    }

    /**
     * Helper method to render embedded panel for Table components.
     *
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderPanelEnd(final ResponseWriter writer)
            throws IOException {

        writer.endElement("div");
        writer.endElement("div");
        writer.endElement("div");
        writer.endElement("div");
        writer.endElement("div");
        writer.endElement("div");
    }

    /**
     * Helper method to render sort panel layout for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component TablePanels to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void renderSortPanelLayout(final FacesContext context,
            final TablePanels component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            log("renderSortPanelLayout",
                    "Cannot render sort panel layout, TablePanels is null");
            return;
        }

        // Render panel layout table.
        Theme theme = getTheme();
        writer.writeText("\n", null);
        writer.startElement("table", component);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.TABLE_PANEL_TABLE), null);
        writer.writeAttribute("border", "0", null);
        writer.writeAttribute("cellpadding", "0", null);
        writer.writeAttribute("cellspacing", "0", null);

        // Render primary sort column menu.
        renderSortPanelRow(context, component, writer,
                component.getPrimarySortColumnMenuLabel(),
                component.getPrimarySortColumnMenu(),
                component.getPrimarySortOrderMenu());

        // Render secondary sort column menu if there are more than 2 choices,
        // including the none selected option.
        UIComponent menu = component.getSecondarySortColumnMenu();
        Option[] options;
        if (menu instanceof DropDown) {
            options = (Option[]) ((DropDown) menu).getItems();
        } else {
            options = null;
        }
        if (options != null && options.length > 2) {
            renderSortPanelRow(context, component, writer,
                    component.getSecondarySortColumnMenuLabel(), menu,
                    component.getSecondarySortOrderMenu());
        } else {
            log("renderSortPanelLayout",
                    "Secondary sort column menu not rendered");
        }

        // Render tertiary sort column menu if there are more than 3 choices,
        // including the none selected option.
        menu = component.getTertiarySortColumnMenu();
        if (menu instanceof DropDown) {
            options = (Option[]) ((DropDown) menu).getItems();
        } else {
            options = null;
        }
        if (options != null && options.length > 3) {
            renderSortPanelRow(context, component, writer,
                    component.getTertiarySortColumnMenuLabel(), menu,
                    component.getTertiarySortOrderMenu());
        } else {
            log("renderSortPanelLayout",
                    "Tertiary sort column menu not rendered");
        }

        writer.endElement("table");
        writer.writeText("\n", null);
        writer.startElement("div", component);
        writer.writeAttribute("class", theme.getStyleClass(
                ThemeStyles.TABLE_PANEL_BUTTON_DIV), null);

        // Render ok and cancel buttons.
        RenderingUtilities.renderComponent(
                component.getSortPanelSubmitButton(), context);
        RenderingUtilities.renderComponent(
                component.getSortPanelCancelButton(), context);

        writer.endElement("div");
    }

    /**
     * Helper method to render sort panel row for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component TablePanels to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     * @param label The sort label.
     * @param columnMenu The sort column menu.
     * @param orderMenu The sort order menu.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderSortPanelRow(final FacesContext context,
            final TablePanels component, final ResponseWriter writer,
            final UIComponent label, final UIComponent columnMenu,
            final UIComponent orderMenu) throws IOException {

        if (component == null) {
            log("renderSortPanelRow",
                    "Cannot render sort panel row, TablePanels is null");
            return;
        }

        writer.writeText("\n", null);
        writer.startElement("tr", component);
        writer.writeText("\n", null);
        writer.startElement("td", component);

        // Render primary sort column menu label.
        if (label instanceof Label) {
            ((Label) label).setFor(columnMenu.getClientId(context));
            RenderingUtilities.renderComponent(label, context);
        } else {
            log("renderSortPanelRow",
                    "Cannot render label, not Label instance");
        }

        writer.endElement("td");
        writer.writeText("\n", null);
        writer.startElement("td", component);

        // Render primary sort column menu.
        RenderingUtilities.renderComponent(columnMenu, context);

        writer.endElement("td");
        writer.writeText("\n", null);
        writer.startElement("td", component);

        // Render primary sort order menu.
        RenderingUtilities.renderComponent(orderMenu, context);

        writer.endElement("td");
        writer.endElement("tr");
    }
}
