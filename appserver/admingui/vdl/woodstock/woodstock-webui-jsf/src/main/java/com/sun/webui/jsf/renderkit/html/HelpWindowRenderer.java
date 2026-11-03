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
import com.sun.webui.jsf.component.HelpWindow;
import com.sun.webui.jsf.util.LogUtil;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This class is responsible for rendering the {@link HelpWindow} component for
 * the HTML Render Kit.
 * <p>
 * The {@link HelpWindow} component can be used as a link which when clicked
 * opens up the a popup window that displays help data.
 * </p>
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.HelpWindow"))
public final class HelpWindowRenderer extends HyperlinkRenderer {

    /**
     * This method returns the most appropriate URL under the circumstances. In
     * some cases {@code viewhandler.getActionURL()} needs to be invoked while
     * in other cases {@code viewhandler.getResourceURL()} needs to be invoked.
     * The hyperlink renderer, by default, will always use latter while
     * generating the complete URL. Subclasses of this renderer can do it their
     * own way.
     *
     * @param context faces context
     * @param component UI component
     * @url URL
     * @return String
     */
    @Override
    protected String getCorrectURL(final FacesContext context,
            final UIComponent component, final String url) {

        if (!(component instanceof HelpWindow)) {
            return null;
        }

        if (url == null) {
            return null;
        }

        StringBuffer urlBuffer = new StringBuffer();
        urlBuffer.append(context
                .getApplication()
                .getViewHandler()
                .getActionURL(context, url))
                .append("?");

        HelpWindow hw = (HelpWindow) component;
        try {
            if (hw.getWindowTitle() != null) {
                addParameter(urlBuffer, "windowTitle", hw.getWindowTitle());
            }
            if (hw.getHelpFile() != null) {
                addParameter(urlBuffer, "helpFile", hw.getHelpFile());
            }
        } catch (UnsupportedEncodingException e) {
            LogUtil.warning("Encoding error: ", e);
        }
        return urlBuffer.toString();
    }

    /**
     * Helper method to append encoded request parameters to the URL in the
     * appropriate character encoding.
     *
     * @param buffer buffer
     * @param name parameter name
     * @param value parameter value
     * @throws UnsupportedEncodingException if an encoding error occurs
     */
    private void addParameter(final StringBuffer buffer, final String name,
            final String value) throws UnsupportedEncodingException {

        if (buffer == null || name == null || value == null) {
            return;
        }
        buffer.append("&")
                .append(name)
                .append("=")
                .append(URLEncoder.encode(value, "UTF-8"));
    }
}
