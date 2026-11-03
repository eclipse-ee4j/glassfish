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
import com.sun.webui.jsf.component.TimeStamp;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Renders an instance of the TimeStamp component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.TimeStamp"))
public final class TimeStampRenderer extends AbstractRenderer {

    /**
     * Creates a new instance of TimeStampRenderer.
     */
    public TimeStampRenderer() {
    }

    /**
     * Core attributes that are simple pass through.
     */
    private static final String[] CORE_ATTRIBUTES = {
        "style",
        "title"
    };

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        TimeStamp timeStamp = (TimeStamp) component;
        Theme theme = ThemeUtilities.getTheme(context);

        if (!timeStamp.isRendered()) {
            return;
        }

        String textStyle = theme.getStyleClass(ThemeStyles.TIMESTAMP_TEXT);

        StringBuilder sb = new StringBuilder(timeStamp.getClientId(context));
        int idlen = sb.length();

        writer.startElement("span", timeStamp);
        writer.writeAttribute("id", sb.toString(), "id");
        writer.startElement("span", timeStamp);

        writer.writeAttribute("id", sb.append("_span1"), "id");
        //Reset the length
        sb.setLength(idlen);

        RenderingUtilities.renderStyleClass(context, writer, component,
                textStyle);
        addStringAttributes(context, component, writer, CORE_ATTRIBUTES);

        String message = timeStamp.getText();
        if (message == null) {
            // use the default "Last updated:" message
            message = theme.getMessage("TimeStamp.lastUpdate");
        }

        writer.write(message);
        writer.endElement("span");
        writer.write("&nbsp;");
        writer.startElement("span", timeStamp);

        writer.writeAttribute("id", sb.append("_span2"), "id");

        RenderingUtilities.renderStyleClass(context, writer, component,
                textStyle);
        addStringAttributes(context, component, writer, CORE_ATTRIBUTES);

        Locale locale
                = FacesContext.getCurrentInstance().getViewRoot().getLocale();

        DateFormat dateFormat = DateFormat.getDateTimeInstance(
                Integer.parseInt(theme.getMessage("TimeStamp.dateStyle")),
                Integer.parseInt(theme.getMessage("TimeStamp.timeStyle")),
                locale);

        writer.write(dateFormat.format(new Date()));

        writer.endElement("span");
        writer.endElement("span");
    }
}
