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
import com.sun.webui.jsf.component.ComplexComponent;
import com.sun.webui.jsf.component.EditableList;
import com.sun.webui.jsf.component.ListSelector;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.theme.Theme;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.json.JsonObject;

import java.io.IOException;
import java.util.Map;

import static com.sun.webui.jsf.util.JavaScriptUtilities.renderCall;
import static com.sun.webui.jsf.util.JavaScriptUtilities.renderCalls;
import static com.sun.webui.jsf.util.JavaScriptUtilities.renderInitScriptTag;
import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;
import static com.sun.webui.jsf.util.RenderingUtilities.renderComponent;
import static com.sun.webui.jsf.util.ThemeUtilities.getTheme;

/**
 * Renderer for a {@link EditableList} component.
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.EditableList"))
public class EditableListRenderer extends ListRendererBase {

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * This implementation renders the component.
     * @param context faces context
     * @param component UI component
     * @throws IOException if an IO error occurs
     */
    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        if (DEBUG) {
            log("encodeEnd()");
        }
        if (component == null) {
            return;
        }
        if (component instanceof EditableList) {
            renderListComponent((EditableList) component,
                    context,
                    getStyles(component, context));
        } else {
            String message = "Component " + component.toString()
                    + " has been associated with an EditableListRenderer. "
                    + " This renderer can only be used by components "
                    + " that extend com.sun.webui.jsf.component.Selector.";
            throw new FacesException(message);
        }
    }

    /**
     * This implementation decodes user input and delegates to
     * {@link ListRendererBase#decode}.
     * @param context faces context
     * @param component UI component
     */
    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        if (DEBUG) {
            log("decode()");
        }
        if (component == null) {
            return;
        }

        EditableList list = (EditableList) component;
        if (list.isReadOnly()) {
            if (DEBUG) {
                log("component is readonly...");
            }
            return;
        }

        String id = list.getClientId(context);
        Map params =
                context.getExternalContext().getRequestParameterValuesMap();

        // The id for the editable list content or "select" element.
        // If there is a parameter matching listID then
        // there have been selections made. These are only
        // respected when the action is remove.
        // Do it every time, since we cannot know which action
        // has taken place, an add or a remove.
        String listID;
        if (list instanceof ComplexComponent) {
            listID = list.getLabeledElementId(context);
        } else {
            listID = list.getClientId(context);
        }
        String[] selections;
        Object parameters = params.get(listID);
        if (parameters == null) {
            selections = new String[0];
        } else if (parameters instanceof String[]) {
            selections = (String[]) parameters;
        } else {
            selections = new String[1];
            selections[0] = parameters.toString();
        }
        // These are the values to remove.
        // The submitted value for the component will be the
        // current contents of the select element.
        // It is important to not reference the values to
        // remove during an add action.
        list.setValuesToRemove(selections);

        // Always decode the list contents.
        // This is the only place where the current values of the
        // list are kept. This is a result of having both the
        // add and remove buttons set with immediate == true.
        // With immediate true, update model values does not
        // occur. In the listbox case, validation does not occur
        // either therefore the local value of the EditableList
        // which is the contents of the list, is not updated
        // either. The current value is stored only in the
        // submittedValue during the lifecycle of a request
        // initiated by the add or remove button.
        // The update model values will only occur when a submit
        // of the page has occurred.
        // The decoded field is the hidden field rendered
        // by the ListRenderer containing the current list contents.

        String valueID = id.concat(ListSelector.VALUE_ID);
        decode(context, component, valueID);
    }

    /**
     * This method determines whether the component should be
     * rendered as a standalone list, or laid out together with a
     * label that was defined as part of the component.
     *
     * <p>A label will be rendered if either of the following is true:
     * </p>
     * <ul>
     * <li>The page author defined a label facet; or</li>
     * <li>The page author specified text in the label attribute.</li>
     * </ul>
     * <p>If there is a label, the component will be laid out using a
     * HTML table. If not, the component will be rendered as a
     * standalone HTML <tt>select</tt> element.</p>
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
    void renderListComponent(final EditableList component,
            final FacesContext context, final String[] styles)
            throws IOException {

        log("renderListComponent()");
        if (component.isReadOnly()) {
            UIComponent label = component.getListLabelComponent();
            renderReadOnlyList(component, label, context, styles[8]);
            return;
        }

        UIComponent headerComponent =
                component.getFacet(EditableList.HEADER_FACET);
        UIComponent footerComponent =
                component.getFacet(EditableList.FOOTER_FACET);

        boolean gotHeaderOrFooter =
                (headerComponent != null) || (footerComponent != null);

        ResponseWriter writer = context.getResponseWriter();
        renderOpenEncloser(component, context, "div", styles[8]);

        if (DEBUG) {
            log("layout the component");
        }

        if (gotHeaderOrFooter) {

            writer.startElement("table", component);
            writer.writeText("\n", null);

            if (headerComponent != null) {
                addComponentSingleRow(component, headerComponent, context);
            }

            // New table row for the list
            writer.startElement("tr", component);
            writer.writeText("\n", null);
            writer.startElement("td", component);
            writer.writeText("\n", null);
        }

        writer.startElement("table", component);
        writer.writeAttribute("class", styles[9], null);
        writer.writeText("\n", null);

        boolean listOnTop = component.isListOnTop();

        if (listOnTop) {
            addListRow(component, context, styles);
            addFieldRow(component, context, styles);
        } else {
            addFieldRow(component, context, styles);
            addListRow(component, context, styles);
        }
        writer.endElement("table");
        writer.writeText("\n", null);

        if (gotHeaderOrFooter) {
            writer.endElement("td");
            writer.writeText("\n", null);
            writer.endElement("tr");
            writer.writeText("\n", null);

            if (footerComponent != null) {
                addComponentSingleRow(component, footerComponent, context);
            }

            writer.endElement("table");
            writer.writeText("\n", null);
        }
        writer.endElement("div");
        renderJavaScript(component, context, writer);
    }

    /**
     * This implementation is empty.
     * @param context faces context
     * @param component UI component
     * @throws java.io.IOException if an error occurs
     */
    @Override
    public void encodeChildren(final jakarta.faces.context.FacesContext context,
            final jakarta.faces.component.UIComponent component)
            throws java.io.IOException {
    }

    /**
     * Renders a component in a table row.
     * @param list list component
     * @param component The component
     * @param context The FacesContext of the request
     * @throws java.io.IOException if the renderer fails to write to
     * the response
     */
    private void addComponentSingleRow(final EditableList list,
            final UIComponent component, final FacesContext context)
            throws IOException {

        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("tr", list);
        writer.writeText("\n", null);
        writer.startElement("td", list);
        renderComponent(component, context);
        writer.writeText("\n", null);
        // Perhaps this should depend on the dir?
        // writer.writeAttribute("align", "left", null);
        writer.endElement("td");
        writer.writeText("\n", null);
        writer.endElement("tr");
        writer.writeText("\n", null);
    }

    /**
     * Renders the list row.
     * @param component The component
     * @param context The FacesContext of the request
     * @param styles CSS styles
     * @throws java.io.IOException if the renderer fails to write to
     * the response
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void addListRow(final EditableList component,
            final FacesContext context, final String[] styles)
            throws IOException {

        // Perhaps this should depend on the dir?
        // writer.writeAttribute("align", "left", null);
        UIComponent listLabelComponent =
                component.getListLabelComponent();
        UIComponent removeButtonComponent =
                component.getRemoveButtonComponent();

        ResponseWriter writer = context.getResponseWriter();

        // Begin new row for the list label, list & remove button
        writer.startElement("tr", component);
        writer.writeText("\n", null);

        // Render the label in a new table cell
        writer.startElement("td", component);
        writer.writeAttribute("class", styles[13], null);
        writer.writeAttribute("valign", "top", null);
        writer.writeText("\n", null);
        renderComponent(listLabelComponent, context);
        writer.endElement("td");
        writer.writeText("\n", null);

        // Render the textfield in a new table cell
        writer.startElement("td", component);
        writer.writeAttribute("class", styles[14], null);
        writer.writeAttribute("valign", "top", null);
        writer.writeText("\n", null);
        renderHiddenValue(component, context, writer, styles[8]);
        writer.writeText("\n", null);
        String id = component.getClientId(context).concat(ListSelector.LIST_ID);
        renderList(component, id, context, styles);
        writer.endElement("td");
        writer.writeText("\n", null);

        // Render the button in a new table cell

        // Create another table cell for the list and the remove buttons...
        writer.startElement("td", component);
        writer.writeAttribute("class", styles[15], null);
        writer.writeAttribute("valign", "top", null);
        writer.writeText("\n", null);
        renderComponent(removeButtonComponent, context);
        writer.endElement("td");
        writer.writeText("\n", null);

        // End the row
        writer.endElement("tr");
        writer.writeText("\n", null);
    }

    /**
     * Renders the list row.
     * @param component The component
     * @param context The FacesContext of the request
     * @param styles CSS styles
     * @throws java.io.IOException if the renderer fails to write to
     * the response
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void addFieldRow(final EditableList component,
            final FacesContext context, final String[] styles)
            throws IOException {

        // Perhaps this should depend on the dir?
        // writer.writeAttribute("align", "left", null);

        UIComponent textfieldLabelComponent =
                component.getFieldLabelComponent();
        UIComponent textfieldComponent =
                component.getFieldComponent();
        UIComponent addButtonComponent =
                component.getAddButtonComponent();
        UIComponent searchButtonComponent =
                component.getFacet(EditableList.SEARCH_FACET);

        ResponseWriter writer = context.getResponseWriter();

        // Begin new row
        writer.startElement("tr", component);
        writer.writeText("\n", null);

        // Render the label
        writer.startElement("td", component);
        writer.writeAttribute("class", styles[10], null);
        writer.writeText("\n", null);
        renderComponent(textfieldLabelComponent, context);
        writer.writeText("\n", null);
        writer.endElement("td");
        writer.writeText("\n", null);

        // Create another table cell for the text field, the add
        // button and the search button...
        writer.startElement("td", component);
        writer.writeAttribute("class", styles[11], null);
        writer.writeAttribute("valign", "top", null);
        writer.writeText("\n", null);
        renderComponent(textfieldComponent, context);
        writer.writeText("\n", null);
        writer.endElement("td");
        writer.writeText("\n", null);

        // Create another table cell for the text field, the add
        // button and the search button...
        writer.startElement("td", component);
        writer.writeAttribute("class", styles[12], null);
        writer.writeAttribute("valign", "top", null);
        writer.writeText("\n", null);
        renderComponent(addButtonComponent, context);
        writer.writeText("\n", null);
        writer.endElement("td");
        writer.writeText("\n", null);

        if (searchButtonComponent != null) {
            writer.startElement("td", component);
            writer.writeAttribute("class", styles[12], null);
            writer.writeAttribute("valign", "top", null);
            writer.writeText("\n", null);
            renderComponent(searchButtonComponent, context);
            writer.writeText("\n", null);
            writer.endElement("td");
            writer.writeText("\n", null);
        }

        // End the row
        writer.endElement("tr");
        writer.writeText("\n", null);
    }

    /**
     * Render the JS.
     * @param component UI component
     * @param context faces context
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void renderJavaScript(final UIComponent component,
            final FacesContext context, final ResponseWriter writer)
            throws IOException {

        JsonObject initProps = JSON_BUILDER_FACTORY.createObjectBuilder()
                .add("id", component.getClientId(context))
                .build();

        renderInitScriptTag(writer, "editableList", initProps,
                // ws_update_buttons
                renderCall("update_buttons", "editableList",
                        component.getClientId(context)));
    }

    /**
     * Render the appropriate element end, depending on the value of the
     * {@code type} property.
     *
     * @param component UI component
     * @param context {@code FacesContext}for the current request
     * @return String[]
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static String[] getStyles(final UIComponent component,
            final FacesContext context) {

        if (DEBUG) {
            log("getStyles()");
        }
        Theme theme = getTheme(context);

        String[] styles = new String[17];
        styles[0] = renderCalls(((EditableList) component).getOnChange(),
                renderCall("changed", "listbox",
                        component.getClientId(context)));
        styles[1] = theme.getStyleClass(ThemeStyles.LIST);
        styles[2] = theme.getStyleClass(ThemeStyles.LIST_DISABLED);
        styles[3] = theme.getStyleClass(ThemeStyles.LIST_OPTION);
        styles[4] = theme.getStyleClass(ThemeStyles.LIST_OPTION_DISABLED);
        styles[5] = theme.getStyleClass(ThemeStyles.LIST_OPTION_SELECTED);
        styles[6] = theme.getStyleClass(ThemeStyles.LIST_OPTION_GROUP);
        styles[7] = theme.getStyleClass(ThemeStyles.LIST_OPTION_SEPARATOR);
        styles[8] = theme.getStyleClass(ThemeStyles.HIDDEN);
        styles[9] = theme.getStyleClass(ThemeStyles.EDITABLELIST_TABLE);
        styles[10] = theme.getStyleClass(ThemeStyles.EDITABLELIST_FIELD_LABEL);
        styles[11] = theme.getStyleClass(ThemeStyles.EDITABLELIST_FIELD);
        styles[12] = theme.getStyleClass(ThemeStyles.EDITABLELIST_ADD_BUTTON);
        styles[13] = theme.getStyleClass(ThemeStyles.EDITABLELIST_LIST_LABEL);
        styles[14] = theme.getStyleClass(ThemeStyles.EDITABLELIST_LIST);
        styles[15] = theme.getStyleClass(
                ThemeStyles.EDITABLELIST_REMOVE_BUTTON);
        styles[16] = null;
        return styles;
    }

    /**
     * Log an error - only used during development time.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(EditableListRenderer.class.getName() + "::" + msg);
    }
}
