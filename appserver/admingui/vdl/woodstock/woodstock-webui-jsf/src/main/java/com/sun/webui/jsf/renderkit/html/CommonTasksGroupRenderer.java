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
import com.sun.webui.html.HTMLAttributes;
import com.sun.webui.html.HTMLElements;
import com.sun.webui.jsf.component.CommonTasksGroup;
import com.sun.webui.jsf.component.CommonTasksSection;
import com.sun.webui.jsf.component.StaticText;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.json.JsonObject;

import java.io.IOException;
import java.util.Iterator;

import static com.sun.webui.jsf.util.JavaScriptUtilities.renderInitScriptTag;
import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;
import static com.sun.webui.jsf.util.RenderingUtilities.getStyleClasses;
import static com.sun.webui.jsf.util.RenderingUtilities.renderComponent;
import static com.sun.webui.jsf.util.ThemeUtilities.getTheme;

/**
 * Renderer for a {@link com.sun.webui.jsf.component.CommonTasksGroup}
 * component.
 */
@Renderer(
        @Renderer.Renders(
                componentFamily = "com.sun.webui.jsf.CommonTasksGroup"))
public class CommonTasksGroupRenderer extends AbstractRenderer {

    /**
     *Append this string for the id of span for the group header's title.
     */
    private static final String TITLE_SPAN = "_groupTitle";

    /**
     * Group title property.
     */
    public static final String GROUP_TITLE = "commonTasks.groupTitle";

    /**
     * Skip group property.
     */
    public static final String SKIP_GROUP = "commonTasks.skipTagAltText";

    /**
     * Skip a complete common tasks group.
     */
    private static final String SKIP_TASKSGROUP = "skipGroup";

    /**
     * Creates a new instance of CommonTaskGroupRenderer.
     */
    public CommonTasksGroupRenderer() {
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {

        // purposefully don't want to do anything here!
    }

    /**
     * Render a common tasks group.
     *
     * @param context The current FacesContext
     * @param component The CommonTasksGroup object to render
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        if (context == null || component == null) {
            throw new NullPointerException();
        }
        CommonTasksGroup ctg = (CommonTasksGroup) component;

        if (!ctg.isRendered()) {
            return;
        }
        Theme theme = getTheme(context);
        String title = ctg.getTitle();
        if (title == null) {
            title = theme.getMessage(GROUP_TITLE);
            ctg.setTitle(title);
        }

        writer.startElement(HTMLElements.DIV, ctg);
        writer.writeAttribute(HTMLAttributes.ID, ctg.getClientId(context),
                HTMLAttributes.ID);

        String styles = getStyleClasses(context, ctg,
                theme.getStyleClass(ThemeStyles.CTS_GROUP));

        if (styles != null) {
            writer.writeAttribute(HTMLAttributes.CLASS, styles,
                    HTMLAttributes.CLASS);
        }

        if (ctg.getStyle() != null) {
            writer.writeAttribute(HTMLAttributes.STYLE, ctg.getStyle(),
                    HTMLAttributes.STYLE);
        }

        StringBuilder jsBuffer = new StringBuilder();
        renderTaskGroupHeader(title, writer, context, ctg, theme);

        if (!(ctg.getParent() instanceof CommonTasksSection)) {
            renderJavascriptImages(theme, writer, ctg, context);
        }

        Iterator it = ctg.getChildren().iterator();
        UIComponent comp;
        while (it.hasNext()) {
            comp = (UIComponent) it.next();
            renderComponent(comp, context);
        }

        writer.endElement(HTMLElements.DIV);
    }

    /**
     * Renders the JS necessary to precache the "i" images.
     *
     * @param theme The current theme
     * @param writer The ResponseWriter object
     * @param component The commonTasksSection component
     * @param context faces context
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderJavascriptImages(final Theme theme,
            final ResponseWriter writer, final UIComponent component,
            final FacesContext context) throws IOException {

        JsonObject initProps = getJSONProperties(context, theme, component);

        // Render JavaScript.
        renderInitScriptTag(writer, "commonTasksSection", initProps);
    }

    /**
     * Get the JSON properties.
     * @param context faces context
     * @param theme theme to use
     * @param component component
     * @return JsonObject
     * @throws IOException if an IO error occurs
     */
    protected JsonObject getJSONProperties(final FacesContext context,
            final Theme theme, final UIComponent component) throws IOException {

        return JSON_BUILDER_FACTORY.createObjectBuilder()
                .add("id", component.getClientId(context))
                .add("pic1URL", theme.getImagePath(
                        ThemeImages.CTS_RIGHT_TOGGLE_SELECTED))
                .add("pic2URL", theme.getImagePath(
                        ThemeImages.CTS_RIGHT_TOGGLE_OVER))
                .add("pic3URL", theme.getImagePath(
                        ThemeImages.CTS_RIGHT_TOGGLE))
                .build();

    }

    /**
     * Render a common tasks group.
     *
     * @param context The current FacesContext
     * @param component The CommonTasksGroup object to render
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {
    }

    /**
     * Renders the task group header.
     * @param header The header text to be rendered
     * @param writer The current ResponseWriter object
     * @param component The CommonTasksGroup object
     * @param theme The current theme.
     * @param context The current FacesContext
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderTaskGroupHeader(final String header,
            final ResponseWriter writer, final FacesContext context,
            final UIComponent component, final Theme theme)
            throws IOException {

        StaticText staticText = new StaticText();
        staticText.setParent(component);
        staticText.setId(TITLE_SPAN);
        staticText.setStyleClass(theme.getStyleClass(ThemeStyles.CTS_HEADER));
        staticText.setText(header);
        renderComponent(staticText, context);
    }

    /**
     * This implementation returns {@code true}.
     * @return {@code boolean}
     */
    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
