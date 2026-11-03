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
package com.sun.webui.jsf.renderkit.widget;

import com.sun.webui.jsf.component.Widget;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.Renderer;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import static com.sun.webui.jsf.util.JavaScriptUtilities.renderCall;
import static com.sun.webui.jsf.util.JavaScriptUtilities.renderScripTag;
import static com.sun.webui.jsf.util.JsonUtilities.jsonValueOf;

/**
 * This class provides common methods for widget renderers.
 *
 * Renderers extending this class are expected to output JSON containing
 * key-value pairs of component properties. The JSON object given to the widget
 * may contain further key-value pairs of properties or an HTML string. This
 * base class shall ensure that all properties are obtained and rendered at the
 * appropriate time.
 */
public abstract class RendererBase extends Renderer {

    @Override
    public void encodeBegin(final FacesContext context,
            final UIComponent component) throws IOException {
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {

        // Children are rendered when obtaining component
        // properties via the encodeBegin method.
    }

    @Override
    public final void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        if (context == null || component == null) {
            throw new NullPointerException();
        }
        if (!component.isRendered()) {
            return;
        }

        // Get writer.
        ResponseWriter writer = context.getResponseWriter();
        String widgetType = ((Widget) component).getWidgetType();
        boolean isSubComponent = component.getParent() instanceof Widget;

        // Render for JSF facets, but not subcomponents.
        if (!isSubComponent && widgetType != null) {

            // declare widget
            StringBuilder widget = new StringBuilder();
            widget.append("<div data-dojo-type=\"webui/suntheme/widget/")
                    .append(widgetType)
                    .append("\" ")
                    .append(renderWidgetProp("id",
                            component.getClientId(context)));
            JsonObject json = getProperties(context, component).build();
            for (Entry<String, JsonValue> entry : json.entrySet()) {
                widget.append(renderWidgetProp(entry.getKey(),
                        entry.getValue().toString()));
            }
            widget.append("\n");
            renderNestedContent(context, component);
            writer.append("</div>\n")
                    .append(widget.toString());
        }

        // Render for JSF facets, but not subcomponents.
        if (!isSubComponent && widgetType != null) {
            // Render enclosing tag.
            writer.write(");\n");
            writer.endElement("script");
            writer.write("\n");
        }

        renderScripTag(writer,
                // ws_widget_parse
                renderCall("widget_parse",
                        Arrays.asList(getModuleNames(component))));
    }

    /**
     * This implementation always return {@code true}.
     * @return {@code boolean}
     */
    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Get the DOJO modules required to instantiate the widget.
     * @param component UI component
     * @return String[]
     */
    protected abstract String[] getModuleNames(UIComponent component);

    /**
     * Get the widget properties.
     *
     * @param context FacesContext for the current request.
     * @param component UIComponent to be rendered.
     * @return JsonObjectBuilder
     * @throws java.io.IOException if an IO error occurs
     */
    protected abstract JsonObjectBuilder getProperties(FacesContext context,
            UIComponent component) throws IOException;

    /**
     * Get the widget nested facets.
     * @param context faces context
     * @param component UI component
     * @throws java.io.IOException if an error occurs while rendering
     */
    protected abstract void renderNestedContent(FacesContext context,
            UIComponent component) throws IOException;

    /**
     * This method may be used to add attribute name/value pairs to the given
     * JSONObject.
     *
     * @param names Array of attribute names to be passed through.
     * @param component UIComponent to be rendered.
     * @param properties Map to add name/value pairs to.
     */
    protected static void addAttributeProperties(final String[] names,
            final UIComponent component, final JsonObjectBuilder properties) {

        if (names == null) {
            return;
        }
        Map attributes = component.getAttributes();
        for (String name : names) {
            Object value = attributes.get(name);
            if (value == null) {
                continue;
            }
            if (value instanceof Integer
                    && ((Integer) value) == Integer.MIN_VALUE) {
                    continue;
            }
            properties.add(name, jsonValueOf(value));
        }
    }

    /**
     * Create a {@code StringBuilder} for a widget property name/value pair.
     * @param name property name
     * @param value property value
     * @return rendered widget property
     */
    private static StringBuilder renderWidgetProp(final String name,
            final String value) {

        return new StringBuilder()
                .append("data-dojo-props=\"")
                .append(name)
                .append(":'")
                .append(value)
                .append("'\" ");
    }
}
