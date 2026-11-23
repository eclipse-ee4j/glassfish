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

 /*
 * $Id: LabelRenderer.java,v 1.1.6.1 2009-12-29 04:52:46 jyeary Exp $
 */
package com.sun.webui.jsf.renderkit.html;

import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.ComplexComponent;
import com.sun.webui.jsf.component.Label;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.MessageUtil;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.FacesException;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIParameter;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Renderer for a {@link Label} component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.Label"))
public final class LabelRenderer extends jakarta.faces.render.Renderer {

    /**
     * The set of additional String pass-through attributes to be rendered if we
     * actually create a {@code &lt;label&gt;} element.
     */
    private static final String[] EVENT_NAMES = {
        "onClick",
        "onMouseDown",
        "onMouseUp",
        "onMouseOver",
        "onMouseMove",
        "onMouseOut",
    };

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {
    }

    /**
     * Render the appropriate element attributes, followed by the label content,
     * depending on whether the {@code for} property is set or not.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code EditableValueHolder} component whose
     * submitted value is to be stored
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        if (!(component instanceof Label)) {
            Object[] params = {
                component.toString(),
                this.getClass().getName(),
                Label.class.getName(),
            };
            String message = MessageUtil.getMessage(
                    "com.sun.webui.jsf.resources.LogMessages",
                    "Label.renderer", params);
            throw new FacesException(message);
        }

        Label label = (Label) component;

        if (LogUtil.fineEnabled(LabelRenderer.class)) {
            LogUtil.fine(LabelRenderer.class, "Label.renderAttributes",
                    new Object[]{label.getId(), label.getFor()});
        }

        EditableValueHolder comp = label.getLabeledComponent();
        boolean requiredFlag = label.isRequiredIndicator();
        boolean errorFlag = false;

        if (!label.isHideIndicators() && comp != null) {
            Object o = ((UIComponent) comp).getAttributes().get("readOnly");
            if (o != null && o instanceof Boolean && o.equals(Boolean.TRUE)) {
                requiredFlag = false;
                errorFlag = false;
            } else {
                requiredFlag = comp.isRequired();
                errorFlag = !comp.isValid();
            }
        }

        Theme theme = ThemeUtilities.getTheme(context);
        String styleClass = getThemeStyleClass(label, theme, errorFlag);
        ResponseWriter writer = context.getResponseWriter();

        String id = getLabeledElementId(context, label);
        startElement(context, label, styleClass, id, writer);
        if (errorFlag) {
            writer.write("\n");
            RenderingUtilities.renderComponent(
                    label.getErrorIcon(theme, context, false), context);
        }

        // Render the label text
        String value = formatLabelText(context, label);
        if (value != null) {
            writer.write("\n");
            writer.writeText(value, "text");
            writer.writeText("\n", null);
        }

        // Render the required indicator flag
        if (requiredFlag) {
            writer.write("\n");
            RenderingUtilities.renderComponent(
                    label.getRequiredIcon(theme, context), context);
        }

        // Note: the for attribute has been set, so we render the end of
        // the label tag *before* we render the children. Otherwise we
        // will inadvertently set the font for the child components.
        writer.endElement("label");

        Iterator children = label.getChildren().iterator();
        while (children.hasNext()) {
            RenderingUtilities.renderComponent((UIComponent) children.next(),
                    context);
            writer.writeText("\n", null);
        }

        if (LogUtil.finestEnabled(LabelRenderer.class)) {
            LogUtil.finest(LabelRenderer.class, "Label.renderEnd");
        }
    }

    /**
     * Render the start element.
     * @param context faces context
     * @param label label component
     * @param styleClass style class
     * @param forId component id
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    private void startElement(final FacesContext context, final Label label,
            final String styleClass, final String forId,
            final ResponseWriter writer) throws IOException {

        // There used to be a call to Label.getElement() here to either
        // render a label or a span element depending on whether the
        // "for" attribute was set or not. That has changed so that
        // a label element be rendered in all cases.
        writer.startElement("label", label);
        writer.writeAttribute("id", label.getClientId(context), "id");

        if (forId != null && forId.length() != 0) {
            writer.writeAttribute("for", forId, "for");
        }

        RenderingUtilities.renderStyleClass(context, writer, label, styleClass);

        if (label.getStyle() != null) {
            writer.writeAttribute("style", label.getStyle(), "style");
        }
        // Render the "toolTip" properties
        String toolTip = label.getToolTip();
        if (toolTip != null) {
            writer.writeAttribute("title", toolTip, "toolTip");
        }
        writeEvents(label, writer);
    }

    /**
     * Returns an id suitable for the HTML label element's "for" attribute. This
     * implementation uses the following heuristic to obtain a suitable id.
     *
     * <ul>
     * <li>If {@code label.getFor} returns null, return the value of
     * {@code getLabeledChildId}.
     * </li>
     * <li>If {@code label.getFor} is not null, and the value is not an
     * absolute id (does not contain a
     * {@code NamingContainer.SEPARATOR_CHAR} try to resolve the id to a
     * component instance as if it were a sibling of the label. If a component
     * is found, and it is an instance of {@code ComplexComponent} return
     * the value of {@code component.getLabeledElementId} else
     * {@code component.getClientId}.
     * </li>
     * <li>If {@code label.getFor} returns an absolute id i.e. contains a
     * {@code NamingContainer.SEPARATOR_CHAR} then return the value of
     * {@code RenderingUtilities.getLabeledElementId}.
     * </li>
     * </ul>
     *
     * @param context The faces context
     * @param label The label component.
     *
     * @return A suitable id for the label element's "for" attribute.
     * @throws IOException if an IO error occurs
     */
    protected String getLabeledElementId(final FacesContext context,
            final Label label) throws IOException {

        String id = label.getFor();
        if (id == null) {
            id = getLabeledChildId(context, label);
        } else if (id.indexOf(
                UINamingContainer.getSeparatorChar(context)) == -1) {
            // The id may be a relative id.
            // This does not prove conclusively that the id is a
            // relative id. A relative id could contain a
            // NamingContainer.SEPARATOR_CHAR.
            // Assume that the component's id is given as the value of
            // for attribute. Get the label's parent and try to find the
            // client id of a sibling component.
            //
            UIComponent comp = label.getParent();
            if (comp != null) {
                comp = comp.findComponent(id);
                if (comp != null) {
                    if (comp instanceof ComplexComponent) {
                        id = ((ComplexComponent) comp)
                                .getLabeledElementId(context);
                    } else {
                        id = comp.getClientId(context);
                    }
                }
            }
        } else {
            id = RenderingUtilities.getLabeledElementId(context, id);
        }
        return id;
    }

    /**
     * Return the text to be rendered for this label. This will be either the
     * literal value of the {@code text} property, or the use of that value
     * as a {@code MessageFormat} string, using nested
     * {@code UIParameter} children as the source of replacement
     * values.
     *
     * @param context {@code FacesContext} for the current request
     * @param label {@code Label} we are rendering
     * @return String
     */
    private String formatLabelText(final FacesContext context,
            final Label label) {

        String text = ConversionUtilities.convertValueToString(label,
                label.getValue());
        text = text.concat(" ");
        if (label.getChildCount() == 0) {
            return text;
        }
        List<Object> list = new ArrayList<Object>();
        for (UIComponent kid : label.getChildren()) {
            if (kid instanceof UIParameter) {
                list.add(((UIParameter) kid).getValue());
            }
        }
        if (list.isEmpty()) {
            return text;
        }
        return MessageFormat.format(text,
                list.toArray(new Object[list.size()]));
    }

    /**
     * Get the theme style class.
     * @param label label component
     * @param theme the current theme
     * @param errorFlag error flag
     * @return String
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private String getThemeStyleClass(final Label label, final Theme theme,
            final boolean errorFlag) {

        String style = null;
        int level = label.getLabelLevel();

        if (errorFlag) {
            style = theme.getStyleClass(ThemeStyles.CONTENT_ERROR_LABEL_TEXT);
        } else if (level == 1) {
            style = theme.getStyleClass(ThemeStyles.LABEL_LEVEL_ONE_TEXT);
        } else if (level == 2) {
            style = theme.getStyleClass(ThemeStyles.LABEL_LEVEL_TWO_TEXT);
        } else if (level == 3) {
            style = theme.getStyleClass(ThemeStyles.LABEL_LEVEL_THREE_TEXT);
        }
        return style;
    }

    /**
     * Write the events.
     * @param label label component
     * @param writer writer
     * @throws IOException if an IO error occurs
     */
    private void writeEvents(final Label label, final ResponseWriter writer)
            throws IOException {

        Map attributes = label.getAttributes();
        Object value;
        int length = EVENT_NAMES.length;
        for (int i = 0; i < length; i++) {
            value = attributes.get(EVENT_NAMES[i]);
            if (value != null) {
                if (value instanceof String) {
                    writer.writeAttribute(EVENT_NAMES[i].toLowerCase(),
                            (String) value, EVENT_NAMES[i]);
                } else {
                    writer.writeAttribute(EVENT_NAMES[i].toLowerCase(),
                            value.toString(), EVENT_NAMES[i]);
                }
            }
        }
    }

    /**
     * Returns the client id of the first child of the label component. If there
     * are no children, {@code null} is returned. If the first child is a
     * {@code ComplexComponent} return the value of the
     * {@code getLabeledElementId} instance method, else the value of
     * {@code getClientId}.
     * <p>
     * Note that, no recursive search is made to find a suitable component to
     * label, if the child has more than one child or the child is a grouping
     * component buy not a {@code ComplexComponent}. In such cases, it is
     * advisable to explicitly set the "for" attribute for the label to the
     * desired component contained by the grouping component or non first child
     * of the {@code Label}.
     *
     * @param context The faces context instance
     * @param component The label component.
     * @return String
     */
    private String getLabeledChildId(final FacesContext context,
            final UIComponent component) {

        if (component.getChildCount() == 0) {
            if (LogUtil.fineEnabled(LabelRenderer.class)) {
                LogUtil.fine(LabelRenderer.class,
                        "No children available");
            }
            return null;
        }
        UIComponent child = (UIComponent) component.getChildren().get(0);
        if (child instanceof ComplexComponent) {
            return ((ComplexComponent) child).getLabeledElementId(context);
        } else {
            return child.getClientId(context);
        }
    }
}
