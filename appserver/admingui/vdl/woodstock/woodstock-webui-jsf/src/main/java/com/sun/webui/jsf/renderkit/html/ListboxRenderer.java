/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
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
import com.sun.webui.jsf.component.ListSelector;
import com.sun.webui.jsf.component.Listbox;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.theme.Theme;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.beans.Beans;
import java.io.IOException;

import static com.sun.webui.jsf.util.JavaScriptUtilities.renderCall;
import static com.sun.webui.jsf.util.JavaScriptUtilities.renderCalls;
import static com.sun.webui.jsf.util.ThemeUtilities.getTheme;

/**
 * Renderer for a {@link com.sun.webui.jsf.component.Listbox} component.
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.Listbox"))
public final class ListboxRenderer extends ListRendererBase {

    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        log("encodeEnd()");

        if (component == null) {
            return;
        }
        if (component instanceof ListSelector) {

            ListSelector selector = (ListSelector) component;
            if (!Beans.isDesignTime()) {
                selector.checkSelectionModel(context);
            }

            boolean useMonospace = false;
            if (selector instanceof Listbox) {
                useMonospace = ((Listbox) selector).isMonospace();
            }
            renderListComponent(selector, context,
                    getStyles(context, component, useMonospace));
        } else {
            String message = "Component " + component.toString()
                    + " has been associated with a ListboxRenderer. "
                    + " This renderer can only be used by components "
                    + " that extend com.sun.webui.jsf.component.Selector.";
            throw new FacesException(message);
        }
    }

    /**
     * Render the appropriate element end, depending on the value of the
     * {@code type} property.
     *
     * @param context {@code FacesContext} for the current request
     * @param component UI component
     * @param monospace if true, use the mono space
     * styles to render the list.
     * @return String[]
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private String[] getStyles(final FacesContext context,
            final UIComponent component, final boolean monospace) {

        log("getStyles()");

        Theme theme = getTheme(context);

        String[] styles = new String[10];
        styles[0] = renderCalls(((Listbox) component).getOnChange(),
                // ws_changed
                renderCall("changed", "listbox",
                        component.getClientId(context)));
        if (monospace) {
            styles[1] = theme.getStyleClass(ThemeStyles.LIST_MONOSPACE);
            styles[2] =
                    theme.getStyleClass(ThemeStyles.LIST_MONOSPACE_DISABLED);
        } else {
            styles[1] = theme.getStyleClass(ThemeStyles.LIST);
            styles[2] = theme.getStyleClass(ThemeStyles.LIST_DISABLED);
        }
        styles[3] = theme.getStyleClass(ThemeStyles.LIST_OPTION);
        styles[4] = theme.getStyleClass(ThemeStyles.LIST_OPTION_DISABLED);
        styles[5] = theme.getStyleClass(ThemeStyles.LIST_OPTION_SELECTED);
        styles[6] = theme.getStyleClass(ThemeStyles.LIST_OPTION_GROUP);
        styles[7] = theme.getStyleClass(ThemeStyles.LIST_OPTION_SEPARATOR);
        styles[8] = theme.getStyleClass(ThemeStyles.HIDDEN);
        styles[9] = theme.getStyleClass(ThemeStyles.LIST_ALIGN);
        return styles;
    }

    /**
     * Log an error - only used during development time.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(ListboxRenderer.class.getName() + "::" + msg);
    }
}
