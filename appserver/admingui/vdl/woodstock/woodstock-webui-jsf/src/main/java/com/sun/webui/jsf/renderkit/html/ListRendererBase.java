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

import com.sun.webui.jsf.component.ListManager;
import com.sun.webui.jsf.component.ListSelector;
import com.sun.webui.jsf.model.OptionTitle;
import com.sun.webui.jsf.model.Separator;
import com.sun.webui.jsf.model.list.EndGroup;
import com.sun.webui.jsf.model.list.ListItem;
import com.sun.webui.jsf.model.list.StartGroup;
import com.sun.webui.jsf.util.LogUtil;

import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.Renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import static com.sun.webui.jsf.util.ConversionUtilities.setRenderedValue;
import static com.sun.webui.jsf.util.RenderingUtilities.renderComponent;
import static com.sun.webui.jsf.util.RenderingUtilities.writeStringAttributes;

/**
 * The ListRendererBase is the base class for the list box renderer
 * (Drop-down Menu and Selectable List). These are both rendered using
 * the same HTML tag (select) so a lot of the rendering functionality
 * is shared.
 */
public abstract class ListRendererBase extends Renderer {

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * The list of attribute names in the HTML 4.01 Specification that
     * correspond to the entity type <em>%events;</em>.
     */
    public static final String[] STRING_ATTRIBUTES = {
        "onBlur",
        "onClick",
        "onDblClick",
        "onFocus",
        "onMouseDown",
        "onMouseUp",
        "onMouseOver",
        "onMouseMove",
        "onMouseOut",
        "onKeyPress",
        "onKeyDown",
        "onKeyUp",
        "onSelect",
    };

    /**
     * Separator character.
     */
    protected static final String SEPARATOR = "|";

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
     * <p>If there is a label, the component will be laid out using a
     * HTML table. If not, the component will be rendered as a
     * standalone HTML <code>select</code> element.</p>
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
    protected static void renderListComponent(final ListSelector component,
            final FacesContext context, final String[] styles)
            throws IOException {

        if (DEBUG) {
            log("renderListComponent()");
        }

        boolean readonly = component.isReadOnly();
        if (readonly) {
            if (DEBUG) {
                log("\t component is readonly");
            }
            // We don't want to accidentally mark any label as
            // required in this case...
            component.setRequired(false);
        }

        UIComponent label = component.getLabelComponent();
        ResponseWriter writer = context.getResponseWriter();
        String id = component.getClientId(context);
        boolean spanRendered = false;

        if (label != null) {
            renderOpenEncloser(component, context, "span", styles[8]);
            spanRendered = true;
            writer.writeText("\n", null);
            if (!component.isLabelOnTop() && component.getRows() > 1) {
                Map<String, Object> attributes = label.getAttributes();
                Object styleClass = attributes.get("styleClass");
                if (styleClass == null) {
                    attributes.put("styleClass", styles[9]);
                } else if (!styleClass.toString().contains(styles[9])) {
                    attributes.put("styleClass", styleClass + " " + styles[9]);
                }
            }

            renderComponent(label, context);
            writer.writeText("\n", null);
            if (component.isLabelOnTop()) {

                writer.startElement("br", component);
                writer.endElement("br");
                writer.writeText("\n", null);
            }

            writer.writeText("\n", null);
            id = id.concat(ListSelector.LIST_ID);
        }

        if (readonly) {
            UIComponent value = component.getReadOnlyValueComponent();
            if (label == null) {
                value.getAttributes().put("style", component.getStyle());
                value.getAttributes().put("styleClass",
                        component.getStyleClass());
            }
            renderComponent(value, context);
        } else {
            //renderHiddenValue(component, context, writer, styles[8]);
            //writer.writeText("\n", null);

            // Because renderHiddenValue is commented out this needs
            // to be called for supporting DB NULL values.
            // If it becomes uncommented remove this call.
            recordRenderedValue(component);
            renderList(component, id, context, styles, label == null);
        }
        if (label != null) {
            context.getResponseWriter().endElement("span");
        }
    }

    /**
     * This implementation is empty.
     * @param context faces context
     * @param component UI component
     * @throws java.io.IOException if an IO error occurs
     */
    @Override
    public void encodeChildren(final jakarta.faces.context.FacesContext context,
            final jakarta.faces.component.UIComponent component)
            throws java.io.IOException {
    }

    /**
     * Renders the opening div tag.
     * @param component The component associated with the
     * renderer. Must implement ListManager
     * @param context The FacesContext of the request
     * @param element One of "span" or "div"
     * @param hiddenStyle hidden style
     * @throws java.io.IOException if the renderer fails to write to
     * the response
     */
    protected static void renderOpenEncloser(final ListManager component,
            final FacesContext context, final String element,
            final String hiddenStyle) throws IOException {

        String id = component.getClientId(context);
        ResponseWriter writer = context.getResponseWriter();
        writer.writeText("\n", null);
        writer.startElement(element, (UIComponent) component);
        writer.writeAttribute("id", id, "id");

        String style = component.getStyle();
        if (style != null && style.length() > 0) {
            writer.writeAttribute("style", style, "style");
        }
        style = component.getStyleClass();
        if (component.isVisible()) {
            if (style != null && style.length() > 0) {
                writer.writeAttribute("class", style, "class");
            }
        } else {
            if (style == null) {
                style = hiddenStyle;
            } else {
                style = style + " " + hiddenStyle;
            }
            writer.writeAttribute("class", style, "class");
        }
        writer.writeText("\n", null);
    }

    /**
     * Renders a hidden value.
     * @param component UI component
     * @param context faces context
     * @param writer writer to use
     * @param hiddenStyle CSS style
     * @throws IOException if an IO error occurs
     */
    protected static void renderHiddenValue(final UIComponent component,
            final FacesContext context, final ResponseWriter writer,
            final String hiddenStyle) throws IOException {

        ListManager listManager = (ListManager) component;
        recordRenderedValue(component);

        String hiddenID = component.getClientId(context)
                .concat(ListSelector.VALUE_ID);
        String hiddenLabelID = component.getClientId(context)
                .concat(ListSelector.VALUE_LABEL_ID);

        String[] values = listManager.getValueAsStringArray(context);
        if (DEBUG) {
            log("Values are: ");
            for (int i = 0; i < values.length; ++i) {
                log(String.valueOf(values[i]));
            }
        }

        // Write a hidden label to pacify a11y checkers.
        writer.startElement("label", component);
        writer.writeAttribute("id", hiddenLabelID, null);
        writer.writeAttribute("for", hiddenID, "for");
        writer.writeAttribute("class", hiddenStyle, null);
        writer.endElement("label");
        // Write the hidden <select>

        writer.startElement("select", component);
        writer.writeAttribute("id", hiddenID, null);
        writer.writeAttribute("name", hiddenID, null);
        writer.writeAttribute("multiple", "true", null);
        writer.writeAttribute("class", hiddenStyle, null);
        writer.writeText("\n", null);
        for (int counter = 0; counter < values.length; ++counter) {
            writer.startElement("option", component);
            writer.writeAttribute("selected", "selected", null);
            writer.writeAttribute("value", values[counter], null);
            writer.writeText(values[counter], null);
            writer.endElement("option");
            writer.writeText("\n", null);
        }
        writer.endElement("select");
    }

    /**
     * This is the base method for rendering a HTML select element.This method
     * is based on the functionality of the RI version, so it invokes a method
     * renderSelectItems which in term invokes renderSelectItem. Currently, this
     * renderer requires for the options to be specified using the JSF
     * SelectItem construct, but this should be replaced with a Lockhart
     * version, because the JSF version lacks the ability to associate an id
     * with the list item. I'm not sure whether it should be possible to use
     * SelectItem as well yet.
     *
     * @param component The UI Component associated with the renderer.
     * @param id component id
     * @param context The FacesContext of the request
     * @param styles A String array of styles used to render the component. The
     * first item of the array is the name of the JavaScript method that handles
     * change event. The second item is the style used when the list is enabled.
     * The third style is the one to use when the list is disabled. The fourth
     * item is the style to use for an item that is enabled, the fifth to use
     * for an item that is disabled, and the sixth to use when the item is
     * selected.
     * @throws java.io.IOException if the renderer fails to write to the
     * response
     */
    protected static void renderList(final ListManager component,
            final String id, final FacesContext context,
            final String[] styles) throws IOException {

        renderList(component, id, context, styles, false);
    }

    /**
     * Render the list.
     * @param listManager list manager
     * @param id component id
     * @param context faces context
     * @param styles CSS styles
     * @param renderUserStyles flag for rendering user styles
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static void renderList(final ListManager listManager,
            final String id, final FacesContext context, final String[] styles,
            final boolean renderUserStyles) throws IOException {

        // Set the style class
        String styleClass = styles[1];
        if (listManager.isDisabled()) {
            styleClass = styles[2];
        }

        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("select", (UIComponent) listManager);

        if (renderUserStyles) {
            String style = listManager.getStyle();
            if (style != null && style.length() > 0) {
                writer.writeAttribute("style", style, null);
            }
            String compStyleClass = getStyleClass(listManager,
                    styles[8]);

            if (compStyleClass != null && compStyleClass.length() > 0) {
                styleClass = compStyleClass + " " + styleClass;
            }
        }

        writer.writeAttribute("class", styleClass, null);
        writer.writeAttribute("id", id, null);
        if (listManager.mainListSubmits()) {
            writer.writeAttribute("name", id, null);
        }
        int size = listManager.getRows();
        if (size < 1) {
            size = 12;
        }
        writer.writeAttribute("size", String.valueOf(size), null);

        if (listManager.isMultiple()) {
            writer.writeAttribute("multiple", "multiple", null);
        }

        if (listManager.isDisabled()) {
            writer.writeAttribute("disabled", "disabled", "disabled");
        }

        String tooltip = listManager.getToolTip();
        if (tooltip != null) {
            writer.writeAttribute("title", tooltip, null);
        }

        if (DEBUG) {
            log("Setting onchange event handler");
        }
        writer.writeAttribute("onchange", styles[0], null);

        int tabindex = listManager.getTabIndex();
        if (tabindex > 0 && tabindex < 32767) {
            writer.writeAttribute("tabindex",
                    String.valueOf(tabindex),
                    "tabindex");
        }

        writeStringAttributes((UIComponent) listManager, writer,
                STRING_ATTRIBUTES);

        writer.writeText("\n", null);

        renderListOptions((UIComponent) listManager,
                listManager.getListItems(context, true),
                writer, styles);

        writer.endElement("select");
        writer.writeText("\n", null);
    }

    /**
     * This is the method responsible for rendering the options of a HTML select
     * element.This method is based on the corresponding method from the JSF RI,
     * so the options to be specified using the JSF SelectItem construct.This
     * will have to be replaced - see the renderList method for details.<i>Note
     * - option groups are not yet implemented w.r.t. any styles specified by
     * the HCI guidelines.</i>
     *
     * @param component The UI Component associated with the renderer
     * @param optionsIterator options iterator
     * @param styles A String array of styles used to render the component. The
     * first item of the array is the name of the JavaScript method that handles
     * change event. The second item is the style used when the list is enabled.
     * The third style is the one to use when the list is disabled. The fourth
     * item is the style to use for an item that is enabled, the fifth to use
     * for an item that is disabled, and the sixth to use when the item is
     * selected.
     * @param writer writer to use
     * @throws java.io.IOException if the renderer fails to write to the
     * response
     */
    @SuppressWarnings("checkstyle:magicnumber")
    protected static void renderListOptions(final UIComponent component,
            final Iterator optionsIterator, final ResponseWriter writer,
            final String[] styles) throws IOException {

        if (DEBUG) {
            log("renderListOptions() START");
        }

        Object option;
        boolean noSeparator = true;

        while (optionsIterator.hasNext()) {
            option = optionsIterator.next();
            if (option instanceof Separator) {
                if (DEBUG) {
                    log("\tFound separator");
                }
                renderSeparator(component, writer, styles[7]);
            } else if (option instanceof StartGroup) {
                if (DEBUG) {
                    log("\tFound option group");
                }
                StartGroup group = (StartGroup) option;
                if (DEBUG) {
                    log("\tThis is the start of a group");
                    log("\tLabel is" + group.getLabel());
                }

                if (!noSeparator) {
                    renderSeparator(component, writer, styles[7]);
                }
                writer.startElement("optgroup", component);
                writer.writeAttribute("label", group.getLabel(), null);
                writer.writeAttribute("class", styles[6], null);
                writer.write("\n");
                noSeparator = true;
            } else if (option instanceof EndGroup) {
                if (DEBUG) {
                    log("\tThis is the end of a group");
                }
                writer.endElement("optgroup");
                writer.write("\n");
                if (optionsIterator.hasNext()) {
                    renderSeparator(component, writer, styles[7]);
                }
                noSeparator = true;
            } else {
                renderListOption(component, (ListItem) option, writer, styles);
                noSeparator = false;
            }
        }
        if (DEBUG) {
            log("renderListOptions() END");
        }
    }

    /**
     *
     * This is the method responsible for rendering an individual
     * option for a HTML select element.
     *
     * @param list UI component
     * @param listItem The ListItem to render
     * @param writer The ResponseWriter used to render the option
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
    protected static void renderListOption(final UIComponent list,
            final ListItem listItem, final ResponseWriter writer,
            final String[] styles) throws IOException {

        if (DEBUG) {
            log("renderListOption() - START");
        }

        // By default, we use the basic option style
        String styleClass = styles[3];

        // CR 6317842. Regardless if the option is currently selected or
        // not, the disabled option style should be used when the option
        // is disabled. So, check for the disabled item first.
        if (listItem.isDisabled()) {
            if (DEBUG) {
                log("\tItem is disabled");
            }
            // Use "disabled" option style
            styleClass = styles[4];
        } else if (listItem.isSelected()) {
            if (DEBUG) {
                log("\tItem is selected");
            }
            // Use "selected" option style
            styleClass = styles[5];
            if (DEBUG) {
                log("\tStyleclass is " + styleClass);
            }
        } else if (DEBUG) {
            if (DEBUG) {
                log("\tNormal item");
            }
        }

        writer.writeText("\t", null);
        writer.startElement("option", list);
        writer.writeAttribute("class", styleClass, null);
        String itemValue = listItem.getValue();

        // Note that there is no distinction made between an
        // itemValue that is null or an empty string.
        // This is important since the results may be indistinguishable
        // on the client and therefore indistinguishable in the response.
        //
        // However Option which inherits from SelectItem does not
        // allow null item values.
        if (itemValue != null) {
            if (DEBUG) {
                log("Item value is not null");
            }
            writer.writeAttribute("value", itemValue, null);
        }
        if (listItem.isDisabled()) {
            writer.writeAttribute("disabled", "disabled", null);
        }
        if (listItem.isSelected()) {
            if (DEBUG) {
                log("\tWriting selected attribute");
            }
            writer.writeAttribute("selected", "selected", null);
        }

        boolean title = listItem.isTitle();
        if (title) {
            writer.write("&#8212; ");
        }
        writer.write(listItem.getLabel());
        if (title) {
            writer.write(" &#8212;");
        }
        writer.endElement("option");
        writer.writeText("\n", null);
        if (DEBUG) {
            log("\trenderListOption() - END");
        }
    }

    /**
     *
     * This method is responsible for rendering a separator for an
     * option group.
     *
     * @param component The component for which we render the separator
     * @param writer The ResponseWriter used to render the separator
     * @param style The style to use when rendering the option.
     * @throws java.io.IOException if the renderer fails to write to
     * the response
     */
    protected static void renderSeparator(final UIComponent component,
            final ResponseWriter writer, final String style)
            throws IOException {

        if (!(component instanceof ListSelector)) {
            return;
        }

        ListSelector selector = (ListSelector) component;
        if (!selector.isSeparators()) {
            return;
        }

        writer.writeText("\t", null);
        writer.startElement("option", component);
        writer.writeAttribute("class", style, null);
        writer.writeAttribute("disabled", "disabled", null);

        int numEms = selector.getSeparatorLength();
        StringBuilder labelBuffer = new StringBuilder();
        for (int em = 0; em < numEms; ++em) {
            labelBuffer.append("-");
        }

        writer.writeText(labelBuffer.toString(), null);
        writer.endElement("option");
        writer.writeText("\n", null);
    }

    /**
     * * This method is used by some of the renderers that extend ListRenderBase
     * (not ListBox and DropDown).I should probably refactor things so that we
     * always know what the hidden style is instead - then renderListComponent
     * would do.
     *
     * @param component UI component
     * @param label label component
     * @param context faces context
     * @param hiddenStyle hidden style
     * @throws java.io.IOException if an IO error occurs
     */
    protected static void renderReadOnlyList(final ListManager component,
            final UIComponent label, final FacesContext context,
            final String hiddenStyle) throws IOException {

        UIComponent value = component.getReadOnlyValueComponent();
        renderOpenEncloser(component, context, "span", hiddenStyle);
        if (label != null) {
            renderComponent(label, context);
        }
        renderComponent(value, context);
        context.getResponseWriter().endElement("span");
    }

    /**
     * Prepend the hidden style to the user's added style if necessary.
     * @param component UI component
     * @param hiddenStyle hidden style
     * @return String
     */
    private static String getStyleClass(final ListManager component,
            final String hiddenStyle) {

        String style = component.getStyleClass();
        if (style != null && style.length() == 0) {
            style = null;
        }

        if (!component.isVisible()) {
            if (style == null) {
                style = hiddenStyle;
            } else {
                style = style + " " + hiddenStyle;
            }
        }
        return style;
    }

    /**
     * This implementation decodes the user input.
     * @param context faces context
     * @param component UI component
     */
    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        if (DEBUG) {
            log("decode()");
        }
        String id = component.getClientId(context);

        // We used to depend on getLabelComponent() returning non-null
        // to calculate the ID to be used to retrieve parameters for the
        // component. But after the changes made to the facet management,
        // that mechanism doesn't work anymore.
        // We can simply see if we have any input for any of the
        // possible parameter names instead, similar to what field
        // does. It works, though we can technically end up looking
        // at the wrong parameter, if there was no input.
        Map params = context.getExternalContext().getRequestParameterMap();
        Object valueObject = params.get(id);

        if (valueObject == null) {
            id = id.concat(ListSelector.LIST_ID);
        }
        decode(context, component, id);
    }

    /**
     * Retrieve user input from the UI.
     * The expected format of the request parameter of interest is
     * {@code <separator>value<separator>value<separator>} ...
     * If a value is an empty string the format is
     * {@code <separator><separator>}
     * If there is no value there is a single separator.
     * @param context The FacesContext of this request
     * @param component The component associated with the renderer
     * @param id The DOM id of the select element which represents the
     * value of the list
     */
    protected static void decode(final FacesContext context,
            final UIComponent component, final String id) {

        if (DEBUG) {
            log("decode(context, component, id)");
        }
        if (DEBUG) {
            log("id is " + id);
        }
        ListManager lmComponent = (ListManager) component;

        if (lmComponent.isReadOnly()) {
            if (DEBUG) {
                log("component is readonly...");
            }
            return;
        }

        Map params = context.getExternalContext()
                .getRequestParameterValuesMap();

        String[] values;
        Object p = params.get(id);
        if (p == null) {
            values = new String[0];
        } else {
            if (DEBUG) {
                log("\tValue is string array");
            }
            values = (String[]) p;
        }

        // If we find the OptionTitle.NONESELECTED
        // value among a multiple selection list, remove it.
        // If there is only one value and it matches
        // OptionTitle.NONESELECTED then act like the
        // submit did not happen at all and leave the submitted
        // value as is. It should be null, thereby effectively
        // taking this component out of further lifecycle processing.
        if (values.length > 1) {
            // Need to remove any OptionTitle submitted values
            ArrayList<String> newParams = new ArrayList<String>();
            for (int i = 0; i < values.length; ++i) {
                if (OptionTitle.NONESELECTED.equals(values[i])) {
                    continue;
                }
                newParams.add(values[i]);
            }
            values = (String[]) newParams.toArray(new String[newParams.size()]);
        } else if (values.length == 1
                && OptionTitle.NONESELECTED.equals(values[0])) {
            return;
        }

        if (DEBUG) {
            log("\tNumber of Selected values "
                    + String.valueOf(values.length));
            for (int counter = 0; counter < values.length; ++counter) {
                log("\tvalue: " + values[counter]);
            }
        }

        //CR 6455533/6447372
        //if component is disabled, don't set empty array as submitted value
        //so only set if values.length > 0, or if it's == 0 and the component
        //is not disabled
        if (values.length > 0 || !lmComponent.isDisabled()) {
            lmComponent.setSubmittedValue(values);
        }
    }

    /**
     * The list is not responsible for rendering any child components, so this
     * method returns false.(This is not intuitive, but it causes the right
     * behavior). I need to understand this better.
     *
     * @return {@code true}
     */
    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Retrieves the selected values as Strings.
     * @param context The FacesContext of this request
     * @param component The component associated with the renderer
     * @return String[]
     */
    protected static String[] getUserInput(final FacesContext context,
            final UIComponent component) {

        if (DEBUG) {
            log("::getUserSelectedValues()");
        }
        String id = component.getClientId(context);
        // Util.doAssert(id != null);
        Map params =
                context.getExternalContext().getRequestParameterValuesMap();

        String[] values = null;
        if (params.containsKey(id)) {
            values = (String[]) params.get(id);
        }
        if (values == null) {
            values = new String[0];
        }
        if (DEBUG) {
            log("\tNumber of Selected values "
                    + String.valueOf(values.length));
            for (int counter = 0; counter < values.length; ++counter) {
                log("\t" + values[counter]);
            }
        }
        return values;
    }

    /**
     * This must be called where the value is about to be rendered
     * for DB Null value support.
     * @param component UI component
     */
    private static void recordRenderedValue(final UIComponent component) {

        if (component instanceof EditableValueHolder
                && ((EditableValueHolder) component)
                        .getSubmittedValue() == null) {
            setRenderedValue(component, ((EditableValueHolder) component)
                    .getValue());
        }
    }

    /**
     * Log an error - only used during development time.
     *
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(ListRendererBase.class.getName() + "::" + msg);
    }
}
