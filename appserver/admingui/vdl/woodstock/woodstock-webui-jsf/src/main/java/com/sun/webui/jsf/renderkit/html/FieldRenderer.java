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
import com.sun.webui.jsf.component.ComplexComponent;
import com.sun.webui.jsf.component.Field;
import com.sun.webui.jsf.component.Icon;
import com.sun.webui.jsf.component.TextArea;
import com.sun.webui.jsf.component.Upload;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.MessageUtil;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * Renderer for {@link com.sun.webui.jsf.component.TextField} components.
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.Field"))
public class FieldRenderer extends jakarta.faces.render.Renderer {

    /**
     * The list of attribute names in the HTML 4.01 Specification that
     * correspond to the entity type <em>%events;</em>.
     */
    public static final String[] STRING_ATTRIBUTES = {
        "onBlur",
        "onChange",
        "onClick",
        "onDblClick",
        "onFocus",
        "onMouseDown",
        "onMouseUp",
        "onMouseOver",
        "onMouseMove",
        "onMouseOut",
        "onKeyDown",
        "onKeyPress",
        "onKeyUp",
        "onSelect",
    };

    /**
     * Spacer id.
     */
    public static final String SPACER_ID = "_spacer";

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

        if (!(component instanceof Field)) {
            Object[] params = {
                component.toString(),
                this.getClass().getName(),
                Field.class.getName(),
            };
            String message = MessageUtil
                    .getMessage("com.sun.webui.jsf.resources.LogMessages",
                    "Renderer.component", params);
            throw new FacesException(message);
        }

        renderField(context, (Field) component, "text", getStyles(context));
    }

    /**
     * Render the TextField depending on the value of the
     * {@code type} property.
     * @param context {@code FacesContext} for the current request
     * @param component {@code UIComponent} to be rendered
     * @param type field type
     * @param styles CSS styles
     * @return {@code boolean}
     * @exception IOException if an input/output error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public boolean renderField(final FacesContext context,
            final Field component, final String type, final String[] styles)
            throws IOException {

        String id = component.getClientId(context);
        ResponseWriter writer = context.getResponseWriter();
        UIComponent label = component.getLabelComponent(context, styles[3]);
        boolean spanRendered = false;

        if (label != null) {
            renderOpeningSpan(component, id, styles[2], writer);
            spanRendered = true;
            writer.writeText("\n", null);
            RenderingUtilities.renderComponent(label, context);
            writer.writeText("\n", null);

            // Add a 10 px spacer image after the label text so that the
            // label has the right distance (10px) from the field, since
            // a developer has no control over the layout in this case.
            Theme theme = ThemeUtilities.getTheme(context);
            Icon icon = ThemeUtilities.getIcon(theme, ThemeImages.DOT);
            icon.setId(component.getId().concat(SPACER_ID));
            icon.setHeight(1);
            icon.setWidth(10);
            RenderingUtilities.renderComponent(icon, context);
            writer.writeText("\n", null);

            // Set the ID to use on the inner component
            if (component instanceof Upload) {
                id = ((Upload) component).getLabeledElementId(context);
            } else if (component instanceof ComplexComponent) {
                id = component.getLabeledElementId(context);
            } else {
                id = component.getClientId(context);
            }
        }
        if (component.isReadOnly()) {
            UIComponent text = component.getReadOnlyComponent(context);
            if (label == null) {
                text.getAttributes().put("style", component.getStyle());
                text.getAttributes().put("styleClass",
                        component.getStyleClass());
            }
            RenderingUtilities.renderComponent(text, context);
        } else {
            renderInput(component, type, id, label == null, styles, context,
                    writer);
        }
        if (label != null) {
            writer.writeText("\n", null);
            writer.endElement("span");
        }
        return spanRendered;
    }

    /**
     * Render the input.
     * @param component UI component
     * @param type type
     * @param id id
     * @param renderUserStyles user styles
     * @param styles CSS styles
     * @param context faces context
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    protected void renderInput(final Field component, final String type,
            final String id, final boolean renderUserStyles,
            final String[] styles, final FacesContext context,
            final ResponseWriter writer) throws IOException {

        if (component instanceof TextArea) {
            writer.startElement("textarea", component);
            int rows = ((TextArea) component).getRows();
            if (rows > 0) {
                writer.writeAttribute("rows", String.valueOf(rows), "rows");
            }
            int columns = component.getColumns();

            if (columns > 0) {
                writer.writeAttribute("cols",
                        String.valueOf(columns),
                        "columns");
            }
        } else {
            writer.startElement("input", component);
            writer.writeAttribute("type", type, null);

            int columns = component.getColumns();
            if (columns > 0) {
                writer.writeAttribute("size",
                        String.valueOf(columns),
                        "columns");
            }
        }

        writer.writeAttribute("id", id, null);
        writer.writeAttribute("name", id, null);

        if (component.isDisabled()) {
            writer.writeAttribute("disabled", "disabled", null);
        }

        int maxlength = component.getMaxLength();
        if (maxlength > 0) {
            writer.writeAttribute("maxlength",
                    String.valueOf(maxlength),
                    "maxLength");
        }

        // Render attributes based on the "toolTip" properties
        String toolTip = component.getToolTip();
        if (toolTip != null && toolTip.length() > 0) {
            writer.writeAttribute("title", toolTip, "toolTip");
        }

        int tabIndex = component.getTabIndex();
        if (tabIndex > 0) {
            writer.writeAttribute("tabindex",
                    String.valueOf(tabIndex),
                    "tabIndex");
        }

        RenderingUtilities.writeStringAttributes(component, writer,
                STRING_ATTRIBUTES);

        String styleClass;
        if (component.isDisabled()) {
            styleClass = styles[1];
        } else {
            styleClass = styles[0];
        }

        String style = null;
        if (renderUserStyles) {

            String compStyleClass = getStyleClass(component, styles[2]);
            if (compStyleClass != null) {
                styleClass = compStyleClass + " " + styleClass;
            }

            style = component.getStyle();
            if (style != null && style.length() == 0) {
                style = null;
            }
        }

        writer.writeAttribute("class", styleClass, null);
        if (style != null) {
            writer.writeAttribute("style", style, null);
        }

        // Record the value that is rendered.
        // Note that getValueAsString conforms to JSF conventions
        // for NULL values, in that it returns "" if the component
        // value is NULL. This value cannot be trusted since
        // the fidelity of the data must be preserved, i.e. if the
        // value is null, it must remain null if the component is unchanged
        // by the user..
        //
        // If submittedValue is not null then the component's
        // model value or local value has not been updated
        // therefore assume that this is an immediate or premature
        // render response. Therefore just assume that if the rendered
        // value was null, the saved information is still valid.
        if (component.getSubmittedValue() == null) {
            ConversionUtilities.setRenderedValue(component,
                    component.getValue());
        }

        // Still call the component's getValueAsString method
        // in order to render it.
        String value = component.getValueAsString(context);
        if (component instanceof TextArea) {
            writer.writeText(value, "value");
            writer.endElement("textarea");
        } else {
            if (value != null) {
                writer.writeAttribute("value", value, "value");
            }
            writer.endElement("input");
        }
    }

    /**
     * Prepend the hidden style to the user's added style if necessary.
     * @param component UI component
     * @param hiddenStyle hidden style
     * @return String
     */
    protected String getStyleClass(final Field component,
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
     * Decode the component.
     * @param context The FacesContext associated with this request
     * @param component The TextField component to decode
     */
    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        HiddenFieldRenderer.decodeInput(context, component);
    }

    /**
     * This implementation is empty.
     *
     * @param context {@link FacesContext} for the response we are creating
     * @param component {@link UIComponent} whose children are to be rendered
     */
    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) {
    }

    /**
     * This implementation is empty.
     *
     * @param context {@link FacesContext} for the request we are processing
     * @param component {@link UIComponent} to be rendered
     */
    @Override
    public void encodeBegin(final FacesContext context,
            final UIComponent component) {
    }

    /**
     * This implementation returns {@code true}, meaning that the FieldRenderer
     * is responsible for rendering the children the component it is asked to
     * render.
     *
     * @return {@code boolean}
     */
    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Get the CSS styles.
     * @param context faces context
     * @return String[]
     */
    @SuppressWarnings("checkstyle:magicnumber")
    protected String[] getStyles(final FacesContext context) {
        Theme theme = ThemeUtilities.getTheme(context);
        String[] styles = new String[4];
        styles[0] = theme.getStyleClass(ThemeStyles.TEXT_FIELD);
        styles[1] = theme.getStyleClass(ThemeStyles.TEXT_FIELD_DISABLED);
        styles[2] = theme.getStyleClass(ThemeStyles.HIDDEN);
        styles[3] = "";
        return styles;
    }

    /**
     * Render the TextField depending on the value of the
     * {@code type} property.
     * @param component {@code UIComponent} to be rendered
     * @param id component id
     * @param hiddenStyle hidden style
     * @param writer writer to use
     * @exception IOException if an input/output error occurs
     */
    private void renderOpeningSpan(final Field component, final String id,
            final String hiddenStyle, final ResponseWriter writer)
            throws IOException {

        writer.startElement("span", component);
        writer.writeAttribute("id", id, "id");

        String style = component.getStyle();
        if (style != null && style.length() > 0) {
            writer.writeAttribute("style", style, "style");
        }

        style = getStyleClass(component, hiddenStyle);
        if (style != null) {
            writer.writeAttribute("class", style, "class");
        }
    }
}
