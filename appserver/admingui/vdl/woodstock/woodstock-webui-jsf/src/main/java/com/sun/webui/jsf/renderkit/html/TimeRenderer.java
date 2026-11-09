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
import com.sun.webui.jsf.component.Time;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.LogUtil;
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
 * Time renderer.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.Time"))
public final class TimeRenderer extends jakarta.faces.render.Renderer {

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        if (DEBUG) {
            log("encodeEnd() START");
        }

        if (!(component instanceof Time)) {
            Object[] params = {
                component.toString(),
                this.getClass().getName(),
                Time.class.getName(),
            };
            String message = MessageUtil.getMessage(
                    "com.sun.webui.jsf.resources.LogMessages",
                    "Renderer.component", params);
            throw new FacesException(message);
        }
        Theme theme = ThemeUtilities.getTheme(context);
        Time time = (Time) component;
        DropDown hourMenu = time.getHourMenu();
        DropDown minuteMenu = time.getMinutesMenu();

        ResponseWriter writer = context.getResponseWriter();

        writer.startElement("table", time);
        writer.writeAttribute("cellspacing", "0", null);
        writer.writeAttribute("cellpadding", "0", null);
        writer.writeText("\n", null);
        writer.startElement("tr", time);
        writer.writeText("\n", null);

        // hour menu
        writer.startElement("td", time);
        writer.writeText("\n", null);
        RenderingUtilities.renderComponent(hourMenu, context);
        writer.writeText("\n", null);
        writer.endElement("td");
        writer.writeText("\n", null);

        // colon
        writer.startElement("td", time);
        writer.write(":");
        writer.endElement("td");
        writer.writeText("\n", null);

        // minutes menu
        writer.startElement("td", time);
        writer.writeText("\n", null);
        RenderingUtilities.renderComponent(minuteMenu, context);
        writer.writeText("\n", null);

        // Should use another cell for this, and a width attribute instead
        //writer.write("&nbsp;");
        writer.startElement("span", time);

        String string
                = theme.getStyleClass(ThemeStyles.DATE_TIME_ZONE_TEXT);
        writer.writeAttribute("class", string, null);
        writer.writeText(theme.getMessage("Time.gmt"), null);
        writer.writeText(time.getOffset(), null);
        writer.endElement("span");
        writer.writeText("\n", null);

        writer.endElement("td");
        writer.writeText("\n", null);
        writer.endElement("tr");
        writer.writeText("\n", null);
        writer.endElement("table");
        writer.writeText("\n", null);

        if (DEBUG) {
            log("encodeEnd() END");
        }
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) {
    }

    @Override
    public void encodeBegin(final FacesContext context,
            final UIComponent component) {
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Log a message to the standard log.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(TimeRenderer.class.getName() + "::" + msg);
    }
}
