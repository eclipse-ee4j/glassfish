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
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.ListSelector;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.MessageUtil;
import com.sun.webui.theme.Theme;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

import static com.sun.webui.jsf.util.JavaScriptUtilities.renderCall;
import static com.sun.webui.jsf.util.JavaScriptUtilities.renderCalls;
import static com.sun.webui.jsf.util.RenderingUtilities.renderHiddenField;
import static com.sun.webui.jsf.util.ThemeUtilities.getTheme;

/**
 * Renderer for a {@link DropDown} component.
 */
@Renderer(@Renderer.Renders(
        componentFamily = "com.sun.webui.jsf.DropDown"))
public class DropDownRenderer extends ListRendererBase {

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
        if (!(component instanceof DropDown)) {
            Object[] params = {
                component.toString(),
                this.getClass().getName(),
                DropDown.class.getName(),
            };
            String message = MessageUtil.getMessage(
                    "com.sun.webui.jsf.resources.LogMessages",
                    "Renderer.component", params);
            throw new FacesException(message);
        }

        DropDown dropDown = (DropDown) component;
        if (dropDown.isForgetValue()) {
            dropDown.setValue(null);
        }

        // Render the element and attributes for this component
        String[] styles;
        if (dropDown.isSubmitForm()) {
            styles = getJumpDropDownStyles(dropDown, context);
        } else {
            styles = getDropDownStyles(dropDown, context);
        }

        renderListComponent((ListSelector) dropDown, context, styles);
        ResponseWriter writer = context.getResponseWriter();
        if (dropDown.isSubmitForm()) {
            String id = dropDown.getClientId(context).concat(DropDown.SUBMIT);
            renderHiddenField(dropDown, writer, id, "false");
        }
    }

    /**
     * Render the appropriate element end, depending on the value of the
     * {@code type} property.
     *
     * @param component UI component
     * @param context {@code FacesContext} for the current request
     * @return String[]
     * @exception IOException if an input/output error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private String[] getDropDownStyles(final DropDown component,
            final FacesContext context) {

        Theme theme = getTheme(context);
        String[] styles = new String[10];

        styles[0] = renderCalls(component.getOnChange(),
                // ws_changed
                renderCall("changed", "dropDown",
                        component.getClientId(context)));
        styles[1] = theme.getStyleClass(ThemeStyles.MENU_STANDARD);
        styles[2] = theme.getStyleClass(ThemeStyles.MENU_STANDARD_DISABLED);
        styles[3] = theme.getStyleClass(ThemeStyles.MENU_STANDARD_OPTION);
        styles[4] = theme.getStyleClass(
                ThemeStyles.MENU_STANDARD_OPTION_DISABLED);
        styles[5] = theme.getStyleClass(
                ThemeStyles.MENU_STANDARD_OPTION_SELECTED);
        styles[6] = theme.getStyleClass(
                ThemeStyles.MENU_STANDARD_OPTION_GROUP);
        styles[7] = theme.getStyleClass(
                ThemeStyles.MENU_STANDARD_OPTION_SEPARATOR);
        styles[8] = theme.getStyleClass(ThemeStyles.HIDDEN);
        return styles;
    }

    /**
     * Helper function to get the theme specific styles for this drop down given
     * the current context.
     * @param component drop-down component
     * @param context faces context
     * @return String[]
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private String[] getJumpDropDownStyles(final DropDown component,
            final FacesContext context) {

        Theme theme = getTheme(context);
        String[] styles = new String[10];
        styles[0] = renderCalls(component.getOnChange(),
                // ws_dropdown_changed
                renderCall("dropdown_changed",
                component.getClientId(context)));
        styles[1] = theme.getStyleClass(ThemeStyles.MENU_JUMP);
        styles[2] = ""; // jumpMENU can't be disabled
        styles[3] = theme.getStyleClass(ThemeStyles.MENU_JUMP_OPTION);
        styles[4] = theme.getStyleClass(
                ThemeStyles.MENU_JUMP_OPTION_DISABLED);
        styles[5] = theme.getStyleClass(ThemeStyles.MENU_JUMP_OPTION_SELECTED);
        styles[6] = theme.getStyleClass(ThemeStyles.MENU_JUMP_OPTION_GROUP);
        styles[7] = theme.getStyleClass(ThemeStyles.MENU_JUMP_OPTION_SEPARATOR);
        styles[8] = theme.getStyleClass(ThemeStyles.HIDDEN);
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
