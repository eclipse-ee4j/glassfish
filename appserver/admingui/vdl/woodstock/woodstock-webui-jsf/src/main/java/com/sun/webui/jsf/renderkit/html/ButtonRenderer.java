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
import com.sun.webui.jsf.component.Button;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ActionEvent;
import jakarta.json.JsonObject;

import java.io.IOException;
import java.util.Map;

import static com.sun.webui.jsf.util.ConversionUtilities.convertValueToString;
import static com.sun.webui.jsf.util.JavaScriptUtilities.renderEventCall;
import static com.sun.webui.jsf.util.JavaScriptUtilities.renderInitScriptTag;
import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;
import static com.sun.webui.jsf.util.RenderingUtilities.renderStyleClass;
import static com.sun.webui.jsf.util.RenderingUtilities.renderURLAttribute;
import static com.sun.webui.jsf.util.ThemeUtilities.getTheme;

/**
 * Renderer for a {@link Button} component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.Button"))
public final class ButtonRenderer extends AbstractRenderer {

    /**
     * The set of integer pass-through attributes to be rendered.
     */
    private static final String[] INT_ATTRIBUTES = {
        "tabIndex"
    };

    /**
     * The set of String pass-through attributes to be rendered.
     */
    private static final String[] STRING_ATTRIBUTES = {
        "dir",
        "lang",
        "onClick",
        "onDblClick",
        "onKeyDown",
        "onKeyPress",
        "onKeyUp",
        "onMouseDown",
        "onMouseUp",
        "onMouseMove",
        "style"
    };

    /**
     * The set of pass-through attributes rendered for input elements.
     */
    private static final String[] INPUT_ATTRIBUTES = {
        "alt",
        "align"
    };

    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        // Enforce NPE requirements in the Javadocs
        if (context == null || component == null) {
            throw new NullPointerException();
        }

        Button button = (Button) component;

        // Do not process disabled or reset components.
        if (button.isReset()) {
            return;
        }

        // Was our command the one that caused this submission?
        String clientId = button.getClientId(context);
        Map map = context.getExternalContext().getRequestParameterMap();

        if (map.containsKey(clientId)
                || (map.containsKey(clientId + ".x")
                && map.containsKey(clientId + ".y"))) {
            button.queueEvent(new ActionEvent(button));
        }
    }

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Button button = (Button) component;

        // Start the appropriate element.
        if (button.isEscape()) {
            writer.startElement("input", button);
        } else {
            writer.startElement("button", button);
        }
    }

    @Override
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Button button = (Button) component;

        // Render client id and name.
        // Note: Null is used when output is different than the original value
        // of the component.
        writer.writeAttribute("id", button.getClientId(context), null);
        writer.writeAttribute("name", button.getClientId(context), null);

        // Render style classes.
        String style = getStyle(button, getTheme(context));
        renderStyleClass(context, writer, button, style);

        writer.writeAttribute("onblur",
                renderEventCall("button", button.getOnBlur(),
                        "onblur"),
                "onBlur");

        writer.writeAttribute("onfocus",
                renderEventCall("button", button.getOnFocus(),
                        "onfocus"),
                "onFocus");

        writer.writeAttribute("onmouseout",
                renderEventCall("button", button.getOnMouseOut(),
                        "onmouseout"),
                "onMouseOut");

        writer.writeAttribute("onmouseover",
                renderEventCall("button", button.getOnMouseOver(),
                        "onmouseover"),
                "onMouseOver");

        // Render tooltip.
        if (button.getToolTip() != null) {
            writer.writeAttribute("title", button.getToolTip(), "toolTip");
        }

        // Render disabled attribute.
        if (button.isDisabled()) {
            writer.writeAttribute("disabled", "disabled", null);
        }

        // Render pass through attributes.
        addIntegerAttributes(context, component, writer, INT_ATTRIBUTES);
        addStringAttributes(context, component, writer, STRING_ATTRIBUTES);

        // Render pass through attributes for input elements.
        if (button.isEscape()) {
            addStringAttributes(context, component, writer, INPUT_ATTRIBUTES);
        }

        // Note: Text attributes must be assigned last because the starting
        // element may be closed here -- see bugtraq #6315893.
        String imageURL = button.getImageURL();
        String icon = button.getIcon();
        if (imageURL != null) {
            renderImageURLAttributes(context, component, writer, imageURL);
        } else if (icon != null) {
            renderIconAttributes(context, component, writer, icon);
        } else {
            renderTextAttributes(component, writer);
        }
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Button button = (Button) component;

        // End the appropriate element.
        if (button.isEscape()) {
            writer.endElement("input");
        } else {
            writer.endElement("button");
        }

        // Append button properties.
        boolean isIcon = button.getImageURL() != null
                || button.getIcon() != null;
        JsonObject initProps = JSON_BUILDER_FACTORY
                .createObjectBuilder()
                .add("id", button.getClientId(context))
                .add("mini", button.isMini())
                .add("disabled", button.isDisabled())
                .add("secondary", !button.isPrimary())
                .add("icon", isIcon)
                .build();

        // Render JavaScript.
        renderInitScriptTag(writer, "button", initProps);
    }

    /**
     * Render the appropriate element attributes for an icon button.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code UIComponent} to be rendered
     * @param writer {@code ResponseWriter} to which the element attributes
     * should be rendered
     * @param icon The identifier of the desired theme image.
     *
     * @exception IOException if an input/output error occurs
     */
    private static void renderIconAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer,
            final String icon) throws IOException {

        // Get themed image.
        String imagePath = getTheme(context).getImagePath(icon);

        // Render type and source attributes.
        writer.writeAttribute("type", "image", null);
        renderURLAttribute(context, writer, component, "src", imagePath,
                "icon");
    }

    /**
     * Render the appropriate element attributes for an image URL button.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code UIComponent} to be rendered
     * @param writer {@code ResponseWriter} to which the element attributes
     * should be rendered
     * @param url The image URL
     *
     * @exception IOException if an input/output error occurs
     */
    private static void renderImageURLAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer,
            final String url) throws IOException {

        // Append context path to relative URLs -- bugtraq #6333069 & 6306727.
        String zUrl = context.getApplication().getViewHandler().
                getResourceURL(context, url);

        // Render type and source attributes.
        writer.writeAttribute("type", "image", null);
        renderURLAttribute(context, writer, component, "src", zUrl, "imageURL");
    }

    /**
     * Render the appropriate element attributes for a text button.
     *
     * @param component {@code UIComponent} to be rendered
     * @param writer {@code ResponseWriter} to which the element attributes
     * should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static void renderTextAttributes(final UIComponent component,
            final ResponseWriter writer) throws IOException {
        Button button = (Button) component;

        // Set the type of button.
        //
        // Note: The "button" type is not supported for usability (isReset and
        // isButton values would conflict).
        if (button.isReset()) {
            writer.writeAttribute("type", "reset", null);
        } else {
            writer.writeAttribute("type", "submit", null);
        }

        // Get the textual label of the button.
        String text = convertValueToString(button,
                button.getValue());
        if (text == null || text.trim().length() == 0) {
            return;
        }

        // Pad the text if needed.
        //
        // Note: This code appears in the UI guidelines, but it may have been
        // for Netscape 4.x. We may be able to do this with styles instead.
        if (!button.isNoTextPadding()) {
            if (text.trim().length() <= 3) {
                text = "  " + text + "  ";
            } else if (text.trim().length() == 4) {
                text = " " + text + " ";
            }
        }

        // Render button text.
        if (button.isEscape()) {
            writer.writeAttribute("value", text, "text");
        } else {
            // Note: This will close the starting tag -- see bugtraq #6315893.
            writer.write(text);
        }
    }

    /**
     * Get {@code onblur} style class.
     *
     * @param button {@code Button} to be rendered
     * @param theme {@code Theme}> for the component
     * @return String
     */
    private static String getOnBlurStyle(final Button button,
            final Theme theme) {

        String style;
        if (button.getImageURL() != null || button.getIcon() != null) {
            style = theme.getStyleClass(ThemeStyles.BUTTON3);
        } else if (button.isMini() && !button.isPrimary()) {
            style = theme.getStyleClass(ThemeStyles.BUTTON2_MINI);
        } else if (button.isMini()) {
            style = theme.getStyleClass(ThemeStyles.BUTTON1_MINI);
        } else if (!button.isPrimary()) {
            style = theme.getStyleClass(ThemeStyles.BUTTON2);
        } else {
            style = theme.getStyleClass(ThemeStyles.BUTTON1);
        }
        return style;
    }

    /**
     * Get {@code onfocus} style class.
     *
     * @param button {@code Button} to be rendered
     * @param theme {@code Theme}> for the component
     * @return String
     */
    private static String getOnFocusStyle(final Button button,
            final Theme theme) {

        String style;
        if (button.getImageURL() != null || button.getIcon() != null) {
            style = theme.getStyleClass(ThemeStyles.BUTTON3_HOVER);
        } else if (button.isMini() && !button.isPrimary()) {
            style = theme.getStyleClass(ThemeStyles.BUTTON2_MINI_HOVER);
        } else if (button.isMini()) {
            style = theme.getStyleClass(ThemeStyles.BUTTON1_MINI_HOVER);
        } else if (!button.isPrimary()) {
            style = theme.getStyleClass(ThemeStyles.BUTTON2_HOVER);
        } else {
            style = theme.getStyleClass(ThemeStyles.BUTTON1_HOVER);
        }
        return style;
    }

    /**
     * Get {@code onmouseover} style class.
     *
     * @param button {@code Button} to be rendered
     * @param theme {@code Theme}> for the component
     * @return String
     */
    private static String getOnMouseOverStyle(final Button button,
            final Theme theme) {

        // The getOnfocusStyle method shares the same style classes.
        return getOnFocusStyle(button, theme);
    }

    /**
     * Get {@code onmouseout} style class.
     *
     * @param button {@code Button} to be rendered
     * @param theme {@code Theme}> for the component
     * @return String
     */
    private static String getOnMouseOutStyle(final Button button,
            final Theme theme) {

        // The getOnblurStyle method shares the same style classes.
        return getOnBlurStyle(button, theme);
    }

    /**
     * Get style class.
     *
     * @param button {@code Button} to be rendered
     * @param theme {@code Theme}> for the component
     * @return String
     */
    private static String getStyle(final Button button, final Theme theme) {

        // styles should always be appended
        String style;  // button style from theme.
        if (button.getImageURL() != null || button.getIcon() != null) {
            style = theme.getStyleClass(ThemeStyles.BUTTON3);
        } else if (button.isMini() && !button.isPrimary()) {
            if (button.isDisabled()) {
                style = theme.getStyleClass(ThemeStyles.BUTTON2_MINI_DISABLED);
            } else {
                style = theme.getStyleClass(ThemeStyles.BUTTON2_MINI);
            }
        } else if (button.isMini()) {
            if (button.isDisabled()) {
                style = theme.getStyleClass(ThemeStyles.BUTTON1_MINI_DISABLED);
            } else {
                style = theme.getStyleClass(ThemeStyles.BUTTON1_MINI);
            }
        } else if (!button.isPrimary()) {
            if (button.isDisabled()) {
                style = theme.getStyleClass(ThemeStyles.BUTTON2_DISABLED);
            } else {
                style = theme.getStyleClass(ThemeStyles.BUTTON2);
            }
        } else {
            if (button.isDisabled()) {
                style = theme.getStyleClass(ThemeStyles.BUTTON1_DISABLED);
            } else {
                style = theme.getStyleClass(ThemeStyles.BUTTON1);
            }
        }
        return style;
    }
}
