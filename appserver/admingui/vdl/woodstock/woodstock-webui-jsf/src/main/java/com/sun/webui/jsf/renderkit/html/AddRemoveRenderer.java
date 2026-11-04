/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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
import com.sun.webui.html.HTMLAttributes;
import com.sun.webui.html.HTMLElements;
import com.sun.webui.jsf.component.AddRemove;
import com.sun.webui.jsf.component.ComplexComponent;
import com.sun.webui.jsf.component.ListSelector;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.JavaScriptUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.json.JsonObject;

import java.io.IOException;

import static com.sun.webui.jsf.util.JavaScriptUtilities.getDomNode;
import static com.sun.webui.jsf.util.JavaScriptUtilities.renderCall;
import static com.sun.webui.jsf.util.JavaScriptUtilities.renderInitScriptTag;
import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;
import static com.sun.webui.jsf.util.RenderingUtilities.renderComponent;
import static com.sun.webui.jsf.util.RenderingUtilities.renderHiddenField;
import static com.sun.webui.jsf.util.RenderingUtilities.writeStringAttributes;

/**
 * Renderer for a {@link com.sun.webui.jsf.component.AddRemove} component.
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.AddRemove"))
public final class AddRemoveRenderer extends ListRendererBase {

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Items id.
     */
    private static final String ITEMS_ID = "_item_list";

    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        if (DEBUG) {
            log("encodeEnd()");
        }

        if (component == null) {
            return;
        }

        if (component instanceof AddRemove) {
            renderListComponent((AddRemove) component, context,
                    getStyles(context));
        } else {
            String message = "Component " + component.toString()
                    + " has been associated with a ListboxRenderer. "
                    + " This renderer can only be used by components "
                    + " that extend com.sun.webui.jsf.component.Selector.";
            throw new FacesException(message);
        }
    }

    /**
     * This method determines whether the component should be
     * rendered as a standalone list, or laid out together with a
     * label that was defined as part of the component.
     *
     * <p>A label will be rendered if either of the following is
     * true:</p>
     * <ul>
     * <li>The page author defined a label facet; or</li>
     * <li>The page author specified text in the label attribute.</li>
     * </ul>
     * @param component The component associated with the
     * renderer. Must be a subclass of ListSelector.
     * @param context The FacesContext of the request
     * @param styles A String array of styles used to render the
     * component. The first item of the array is the name of the
     * JavaScript method that handles change event. The second item is
     * the style used when the list is enabled. The third style is the
     * one to use when the list is disabled. The fourth item is the
     * style to use for an item that is enabled, the fifth to use for
     * an item that is disabled, and the sixth to use when the item is
     * selected.
     * @throws java.io.IOException if the renderer fails to write to
     * the response
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static void renderListComponent(final AddRemove component,
            final FacesContext context, final String[] styles)
            throws IOException {

        log("renderListComponent()");

        if (component.isReadOnly()) {
            UIComponent label = component.getReadOnlyLabelComponent();
            renderReadOnlyList(component, label, context, styles[19]);
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        renderOpenEncloser(component, context, HTMLElements.DIV, styles[19]);

        if (component.isVertical()) {
            renderVerticalAddRemove(component, context, writer, styles);
        } else {
            renderHorizontalAddRemove(component, context, writer, styles);
        }

        UIComponent footerComponent =
                component.getFacet(AddRemove.FOOTER_FACET);
        if (footerComponent != null) {
            writer.startElement(HTMLElements.DIV, component);
            writer.writeText("\n", null);
            renderComponent(footerComponent, context);
            writer.writeText("\n", null);
            writer.endElement(HTMLElements.DIV);
            writer.writeText("\n", null);
        }

        String id = component.getClientId(context);
        renderHiddenField(component, writer, id.concat(ITEMS_ID),
                component.getAllValues());
        writer.writeText("\n", null);

        // Value field
        renderHiddenValue(component, context, writer, styles[19]);

        writer.writeText("\n", null);
        renderDivEnd(writer);

        JsonObject initProps = JSON_BUILDER_FACTORY
                .createObjectBuilder()
                .add("id", id)
                .add("separator", component.getSeparator())
                .add("sort", component.isSorted())
                .build();

        renderInitScriptTag(writer, "addRemove", initProps,
            // ws_update_buttons
            renderCall("update_buttons", "addRemove",
                    component.getClientId(context)));
    }

    /**
     * Render horizontal add remove.
     * @param component UI component
     * @param context faces context
     * @param writer writer to use
     * @param styles CSS styles
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static void renderHorizontalAddRemove(final AddRemove component,
            final FacesContext context, final ResponseWriter writer,
            final String[] styles) throws IOException {

        // Put the columns into a table so that they can be
        // displayed horizontally
        // in a table cell as in portlet. See bug 6299233

        writer.startElement(HTMLElements.TABLE, component);
        writer.writeText("\n", null);
        writer.startElement(HTMLElements.TR, component);
        writer.writeText("\n", null);

        // If the component has a label, render it first...
        UIComponent headerComponent = component.getHeaderComponent();
        if (headerComponent != null) {
            writer.startElement(HTMLElements.TD, component);
            if (!component.isLabelOnTop()) {
                writer.writeAttribute(HTMLAttributes.VALIGN, "top", null);
            } else {
                writer.writeAttribute(HTMLAttributes.COLSPAN, "3", null);
            }
            writer.writeText("\n", null);
            renderDivBegin(component, writer, styles[21]);
            renderComponent(headerComponent, context);
            renderDivEnd(writer);
            renderTableCellEnd(writer);

            if (component.isLabelOnTop()) {
                writer.endElement(HTMLElements.TR);
                writer.writeText("\n", null);
                writer.startElement(HTMLElements.TR, component);
                writer.writeText("\n", null);
            }
        }

        // Available items column
        renderTableCellStart(component, writer);
        renderDivBegin(component, writer, styles[17]);

        renderComponent(component.getAvailableLabelComponent(), context);
        renderAvailableList(component, context, styles);

        renderDivEnd(writer);
        renderTableCellEnd(writer);

        // Buttons column
        renderTableCellStart(component, writer);
        renderDivBegin(component, writer, styles[17]);

        renderButtons(component, context, writer, styles);

        renderDivEnd(writer);
        renderTableCellEnd(writer);

        // Selected list column
        renderTableCellStart(component, writer);
        renderDivBegin(component, writer, styles[17]);

        renderComponent(component.getSelectedLabelComponent(), context);
        renderSelectedList(component, context, styles);

        renderDivEnd(writer);
        renderTableCellEnd(writer);

        // Close the table
        writer.endElement(HTMLElements.TR);
        writer.writeText("\n", null);
        writer.endElement(HTMLElements.TABLE);
        writer.writeText("\n", null);

        writer.startElement(HTMLElements.DIV, component);
        writer.writeAttribute(HTMLAttributes.CLASS, styles[18], null);
        writer.endElement(HTMLElements.DIV);
    }

    /**
     * Render table cell start.
     * @param component UI component
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private static void renderTableCellStart(final AddRemove component,
            final ResponseWriter writer) throws IOException {

        writer.startElement(HTMLElements.TD, component);
        writer.writeAttribute(HTMLAttributes.VALIGN, "top", null);
        writer.writeText("\n", null);
    }

    /**
     * Render table cell end.
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private static void renderTableCellEnd(final ResponseWriter writer)
            throws IOException {

        writer.endElement(HTMLElements.TD);
        writer.writeText("\n", null);
    }

    /**
     * Render vertical add remove.
     * @param component UI component
     * @param context faces context
     * @param writer writer to use
     * @param styles CSS styles
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static void renderVerticalAddRemove(final AddRemove component,
            final FacesContext context, final ResponseWriter writer,
            final String[] styles) throws IOException {

        // Render the header and list for available items in a table
        writer.startElement(HTMLElements.TABLE, component);
        writer.writeText("\n", null);
        writer.startElement(HTMLElements.TR, component);
        writer.writeText("\n", null);
        // If the component has a label, render it first...
        UIComponent headerComponent = component.getHeaderComponent();
        if (headerComponent != null) {
            writer.startElement(HTMLElements.TD, component);

            writer.writeText("\n", null);
            renderDivBegin(component, writer, styles[21]);
            RenderingUtilities.renderComponent(headerComponent, context);
            renderDivEnd(writer);
            renderTableCellEnd(writer);

            writer.endElement(HTMLElements.TR);
            writer.writeText("\n", null);
            writer.startElement(HTMLElements.TR, component);
            writer.writeText("\n", null);

        }
        writer.startElement(HTMLElements.TD, component);
        writer.writeText("\n", null);
        renderDivBegin(component, writer, styles[21]);
        renderComponent(component.getAvailableLabelComponent(), context);
        renderAvailableList(component, context, styles);
        renderDivEnd(writer);
        renderTableCellEnd(writer);
        writer.endElement(HTMLElements.TR);
        writer.writeText("\n", null);
        writer.endElement(HTMLElements.TABLE);
        writer.writeText("\n", null);

        // Render add/remove buttons
        renderDivBegin(component, writer, styles[21]);
        renderAddButtonRow(component, context, writer, styles);
        renderDivEnd(writer);

        // open a div
        renderDivBegin(component, writer, styles[22]);

        // render the header and list for selected items in a table
        writer.startElement(HTMLElements.TABLE, component);
        writer.writeText("\n", null);
        writer.startElement(HTMLElements.TR, component);
        writer.writeText("\n", null);
        writer.startElement(HTMLElements.TD, component);
        writer.writeText("\n", null);
        renderDivBegin(component, writer, styles[21]);
        renderComponent(component.getSelectedLabelComponent(), context);
        renderSelectedList(component, context, styles);
        renderDivEnd(writer);
        renderTableCellEnd(writer);
        writer.endElement(HTMLElements.TR);
        writer.writeText("\n", null);
        writer.endElement(HTMLElements.TABLE);
        writer.writeText("\n", null);

        // render move up/down buttons
        if (component.isMoveButtons()) {
            renderDivBegin(component, writer, styles[21]);
            renderMoveButtonRow(component, context, writer, styles);
            renderDivEnd(writer);
        }

        // Don't want elements below here affected by floated elements
        // in the component, so cancel the float property.
        renderDivBegin(component, writer, styles[22]);
        renderDivEnd(writer);

        // close the div
        writer.writeText("\n", null);
    }

    /**
     * Render div begin.
     * @param addRemove add remove component
     * @param writer writer to use
     * @param style CSS style
     * @throws IOException if an IO error occurs
     */
    private static void renderDivBegin(final AddRemove addRemove,
            final ResponseWriter writer, final String style)
            throws IOException {

        writer.startElement(HTMLElements.DIV, addRemove);
        writer.writeAttribute(HTMLAttributes.CLASS, style, null);
        writer.writeText("\n", null);
    }

    /**
     * Render div end.
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private static void renderDivEnd(final ResponseWriter writer)
            throws IOException {

        writer.writeText("\n", null);
        writer.endElement(HTMLElements.DIV);
        writer.writeText("\n", null);
    }

    /**
     * Render buttons.
     * @param component UI component
     * @param context faces context
     * @param writer writer to use
     * @param styles CSS styles
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static void renderButtons(final AddRemove component,
            final FacesContext context, final ResponseWriter writer,
            final String[] styles) throws IOException {

        writer.startElement(HTMLElements.TABLE, component);
        writer.writeAttribute(HTMLAttributes.CLASS, styles[10], null);
        writer.writeText("\n", null);
        writer.startElement(HTMLElements.TR, component);
        writer.writeText("\n", null);
        writer.startElement(HTMLElements.TD, component);
        writer.writeAttribute(HTMLAttributes.ALIGN, "center", null);
        writer.writeAttribute(HTMLAttributes.WIDTH, "125px", null);
        writer.writeText("\n", null);

        // Render the add button
        renderButton(component, component.getAddButtonComponent(),
                styles[14], writer, context);
        writer.writeText("\n", null);

        if (component.isSelectAll()) {
            renderButton(component, component.getAddAllButtonComponent(),
                    styles[14], writer, context);
        }
        String buttonStyle;
        if (component.isSelectAll()) {
            buttonStyle = styles[15];
        } else {
            buttonStyle = styles[14];
        }
        renderButton(component, component.getRemoveButtonComponent(),
                buttonStyle, writer, context);
        if (component.isSelectAll()) {
            renderButton(component, component.getRemoveAllButtonComponent(),
                    styles[14], writer, context);
        }

        if (component.isMoveButtons()) {
            renderButton(component, component.getMoveUpButtonComponent(),
                    styles[15], writer, context);
            renderButton(component, component.getMoveDownButtonComponent(),
                    styles[14], writer, context);
        }

        writer.endElement(HTMLElements.TD);
        writer.writeText("\n", null);
        writer.endElement(HTMLElements.TR);
        writer.writeText("\n", null);
        writer.endElement(HTMLElements.TABLE);
    }

    /**
     * Render add button row.
     * @param component UI component
     * @param context faces context
     * @param writer writer to use
     * @param styles CSS styles
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static void renderAddButtonRow(final AddRemove component,
            final FacesContext context, final ResponseWriter writer,
            final String[] styles) throws IOException {

        renderButtonVertical(component,
                component.getAddButtonComponent(context), styles[11], writer,
                context);

        if (component.isSelectAll()) {
            renderButtonVertical(component,
                    component.getAddAllButtonComponent(), styles[12], writer,
                    context);
        }
        renderButtonVertical(component, component.getRemoveButtonComponent(),
                styles[13], writer, context);

        if (component.isSelectAll()) {
            renderButtonVertical(component,
                    component.getRemoveAllButtonComponent(), styles[12],
                    writer, context);
        }
    }

    /**
     * Render move button row.
     * @param component UI component
     * @param context faces context
     * @param writer writer to use
     * @param styles CSS styles
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static void renderMoveButtonRow(final AddRemove component,
            final FacesContext context, final ResponseWriter writer,
            final String[] styles) throws IOException {

        renderButtonVertical(component, component.getMoveUpButtonComponent(),
                styles[11], writer, context);
        renderButtonVertical(component, component.getMoveDownButtonComponent(),
                styles[12], writer, context);
    }

    /**
     * Render button.
     * @param addRemove add remove component
     * @param comp UI component
     * @param style CSS style
     * @param writer writer to use
     * @param context faces context
     * @throws IOException if an IO error occurs
     */
    private static void renderButton(final AddRemove addRemove,
            final UIComponent comp, final String style,
            final ResponseWriter writer, final FacesContext context)
            throws IOException {

        if (comp == null) {
            return;
        }

        renderDivBegin(addRemove, writer, style);
        RenderingUtilities.renderComponent(comp, context);
        writer.writeText("\n", null);
        renderDivEnd(writer);
    }

    /**
     * Render button vertical.
     * @param addRemove add remove component
     * @param comp UI component
     * @param style CSS style
     * @param writer writer to use
     * @param context faces context
     * @throws IOException if an IO error occurs
     */
    private static void renderButtonVertical(final AddRemove addRemove,
            final UIComponent comp, final String style,
            final ResponseWriter writer, final FacesContext context)
            throws IOException {

        if (comp == null) {
            return;
        }

        renderDivBegin(addRemove, writer, style);
        RenderingUtilities.renderComponent(comp, context);
        writer.writeText("\n", null);
        renderDivEnd(writer);
    }

    /**
     * This is the base method for rendering a HTML select
     * element. This method is based on the functionality of the RI
     * version, so it invokes a method renderSelectItems which in term
     * invokes renderSelectItem. Currently, this renderer requires for
     * the options to be specified using the JSF SelectItem construct,
     * but this should be replaced with a Lockhart version, because
     * the JSF version lacks the ability to associate an id with the
     * list item. I'm not sure whether it should be possible to use
     * SelectItem as well yet.
     * @param component The UI Component associated with the
     * renderer.
     * @param context The FacesContext of the request
     * @param styles A String array of styles used to render the
     * component. The first item of the array is the name of the
     * JavaScript method that handles change event. The second item is
     * the style used when the list is enabled. The third style is the
     * one to use when the list is disabled. The fourth item is the
     * style to use for an item that is enabled, the fifth to use for
     * an item that is disabled, and the sixth to use when the item is
     * selected.
     * @throws java.io.IOException if the renderer fails to write to
     * the response
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static void renderAvailableList(final AddRemove component,
            final FacesContext context, final String[] styles)
            throws IOException {

        if (component == null) {
            return;
        }

        String id;
        if (component instanceof ComplexComponent) {
            id = component.getLabeledElementId(context);
        } else {
            id = component.getClientId(context);
        }

        // Set the style class
        String styleClass = styles[1];
        if (component.isDisabled()) {
            styleClass = styles[2];
        }

        ResponseWriter writer = context.getResponseWriter();
        writer.startElement(HTMLElements.SELECT, component);
        writer.writeAttribute(HTMLAttributes.CLASS, styleClass, null);
        writer.writeAttribute(HTMLAttributes.ID, id, null);
        writer.writeAttribute(HTMLAttributes.NAME, id, null);

        String jsObject = getDomNode(context, component);
        StringBuffer jsBuffer = new StringBuffer(256);
        jsBuffer.append(jsObject)
                .append(AddRemove.ADD_FUNCTION)
                .append(AddRemove.RETURN);
        writer.writeAttribute(HTMLAttributes.ONDBLCLICK, jsBuffer.toString(),
                null);

        jsBuffer = new StringBuffer(200);
        jsBuffer.append(jsObject)
                .append(AddRemove.AVAILABLE_ONCHANGE_FUNCTION)
                .append(AddRemove.RETURN);
        writer.writeAttribute(HTMLAttributes.ONCHANGE, jsBuffer.toString(),
                null);

        int size = component.getRows();
        writer.writeAttribute(HTMLAttributes.SIZE, String.valueOf(size),
                null);
        writer.writeAttribute(HTMLAttributes.MULTIPLE, "multiple",
                null);

        if (component.isDisabled()) {
            writer.writeAttribute(HTMLAttributes.DISABLED,
                    "disabled",
                    "disabled");
        }

        String tooltip = component.getToolTip();
        if (tooltip != null) {
            writer.writeAttribute(HTMLAttributes.TITLE, tooltip, null);
        }

        if (DEBUG) {
            log("Setting onchange event handler");
        }
        //writer.writeAttribute("onchange", script, null);

        int tabindex = component.getTabIndex();
        if (tabindex > 0 && tabindex < 32767) {
            writer.writeAttribute(HTMLAttributes.TABINDEX,
                    String.valueOf(tabindex),
                    "tabindex");
        }

        writeStringAttributes(component, writer, STRING_ATTRIBUTES);
        writer.writeText("\n", null);

        renderListOptions(component, component.getListItems(context, true),
                writer, styles);

        writer.endElement(HTMLElements.SELECT);
        writer.writeText("\n", null);
    }

    /**
     * This is the base method for rendering a HTML select
     * element. This method is based on the functionality of the RI
     * version, so it invokes a method renderSelectItems which in term
     * invokes renderSelectItem. Currently, this renderer requires for
     * the options to be specified using the JSF SelectItem construct,
     * but this should be replaced with a Lockhart version, because
     * the JSF version lacks the ability to associate an id with the
     * list item. I'm not sure whether it should be possible to use
     * SelectItem as well yet.
     * @param component The UI Component associated with the
     * renderer.
     * @param context The FacesContext of the request
     * @param styles A String array of styles used to render the
     * component. The first item of the array is the name of the
     * JavaScript method that handles change event. The second item is
     * the style used when the list is enabled. The third style is the
     * one to use when the list is disabled. The fourth item is the
     * style to use for an item that is enabled, the fifth to use for
     * an item that is disabled, and the sixth to use when the item is
     * selected.
     * @throws java.io.IOException if the renderer fails to write to
     * the response
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static void renderSelectedList(final AddRemove component,
            final FacesContext context, final String[] styles)
            throws IOException {

        String id =
                component.getClientId(context).concat(AddRemove.SELECTED_ID);

        // Set the style class
        String styleClass = styles[1];
        if (component.isDisabled()) {
            styleClass = styles[2];
        }

        ResponseWriter writer = context.getResponseWriter();
        writer.startElement(HTMLElements.SELECT, component);
        writer.writeAttribute(HTMLAttributes.CLASS, styleClass, null);
        writer.writeAttribute(HTMLAttributes.ID, id, null);
        writer.writeAttribute(HTMLAttributes.SIZE,
                String.valueOf(component.getRows()), null);
        writer.writeAttribute(HTMLAttributes.MULTIPLE, "multiple",
                null);

        String jsObject = JavaScriptUtilities.getDomNode(context, component);

        StringBuffer jsBuffer = new StringBuffer(256);
        jsBuffer.append(jsObject);
        jsBuffer.append(AddRemove.REMOVE_FUNCTION);
        jsBuffer.append(AddRemove.RETURN);
        writer.writeAttribute(HTMLAttributes.ONDBLCLICK, jsBuffer.toString(),
                null);

        jsBuffer = new StringBuffer(200);
        jsBuffer.append(jsObject);
        jsBuffer.append(AddRemove.SELECTED_ONCHANGE_FUNCTION);
        jsBuffer.append(AddRemove.RETURN);
        writer.writeAttribute(HTMLAttributes.ONCHANGE, jsBuffer.toString(),
                null);

        if (component.isDisabled()) {
            writer.writeAttribute(HTMLAttributes.DISABLED, "disabled",
                    "disabled");
        }

        if (DEBUG) {
            log("Setting onchange event handler");
        }

        int tabindex = component.getTabIndex();
        if (tabindex > 0 && tabindex < 32767) {
            writer.writeAttribute(HTMLAttributes.TABINDEX,
                    String.valueOf(tabindex),
                    "tabindex");
        }

        writeStringAttributes(component, writer, STRING_ATTRIBUTES);
        writer.writeText("\n", null);
        renderListOptions(component, component.getSelectedListItems(),
                writer, styles);
        writer.endElement(HTMLElements.SELECT);
        writer.writeText("\n", null);
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws java.io.IOException {
    }

    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        if (DEBUG) {
            log("decode()");
        }

        if (component == null) {
            return;
        }

        decode(context, component,
                component.getClientId(context)
                        .concat(ListSelector.VALUE_ID));
    }

    /**
     * Render the appropriate element end, depending on the value of the
     * {@code type} property.
     *
     * @param context {@code FacesContext} for the current request
     * styles to render the list.
     * @return String[]
     * @exception IOException if an input/output error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static String[] getStyles(final FacesContext context) {

        if (DEBUG) {
            log("getStyles()");
        }

        Theme theme = ThemeUtilities.getTheme(context);

        String[] styles = new String[23];
        styles[0] = JavaScriptUtilities.getModuleName("listbox.changed");
        styles[1] = theme.getStyleClass(ThemeStyles.LIST);
        styles[2] = theme.getStyleClass(ThemeStyles.LIST_DISABLED);
        styles[3] = theme.getStyleClass(ThemeStyles.LIST_OPTION);
        styles[4] = theme.getStyleClass(ThemeStyles.LIST_OPTION_DISABLED);
        styles[5] = theme.getStyleClass(ThemeStyles.LIST_OPTION_SELECTED);
        styles[6] = theme.getStyleClass(ThemeStyles.LIST_OPTION_GROUP);
        styles[7] = theme.getStyleClass(ThemeStyles.LIST_OPTION_SEPARATOR);
        styles[8] = theme.getStyleClass(ThemeStyles.ADDREMOVE_LABEL);
        styles[9] = theme.getStyleClass(ThemeStyles.ADDREMOVE_LABEL2);
        styles[10] = theme.getStyleClass(ThemeStyles.ADDREMOVE_BUTTON_TABLE);
        styles[11] = theme.getStyleClass(ThemeStyles.ADDREMOVE_VERTICAL_FIRST);
        styles[12] = theme.getStyleClass(ThemeStyles.ADDREMOVE_VERTICAL_WITHIN);
        styles[13] = theme.getStyleClass(
                ThemeStyles.ADDREMOVE_VERTICAL_BETWEEN);
        styles[14] = theme.getStyleClass(
                ThemeStyles.ADDREMOVE_HORIZONTAL_WITHIN);
        styles[15] = theme.getStyleClass(
                ThemeStyles.ADDREMOVE_HORIZONTAL_BETWEEN);
        styles[16] = null;
        styles[17] = theme.getStyleClass(
                ThemeStyles.ADDREMOVE_HORIZONTAL_ALIGN);
        styles[18] = theme.getStyleClass(ThemeStyles.ADDREMOVE_HORIZONTAL_LAST);
        styles[19] = theme.getStyleClass(ThemeStyles.HIDDEN);
        styles[20] = theme.getStyleClass(ThemeStyles.ADDREMOVE_VERTICAL_BUTTON);
        styles[21] = theme.getStyleClass(ThemeStyles.ADDREMOVE_VERTICAL_DIV);
        styles[22] = theme.getStyleClass(ThemeStyles.ADDREMOVE_VERTICAL_CLEAR);
        return styles;
    }

    /**
     * Log an error - only used during development time.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(ListRendererBase.class.getName() + "::" + msg);
    }
}
