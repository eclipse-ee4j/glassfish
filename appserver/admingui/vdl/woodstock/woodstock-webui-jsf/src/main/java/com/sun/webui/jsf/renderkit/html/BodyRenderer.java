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
import com.sun.webui.jsf.component.Body;
import com.sun.webui.jsf.component.ComplexComponent;
import com.sun.webui.jsf.util.CookieUtils;
import com.sun.webui.jsf.util.FocusManager;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.MessageUtil;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

import static com.sun.webui.jsf.util.JavaScriptUtilities.renderCall;
import static com.sun.webui.jsf.util.JavaScriptUtilities.renderScripTag;
import static com.sun.webui.jsf.util.RenderingUtilities.decodeHiddenField;
import static com.sun.webui.jsf.util.RenderingUtilities.isPortlet;

/**
 * Renderer for a {@link Body} component.
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.Body"))
public final class BodyRenderer extends AbstractRenderer {

    /**
     * The set of String pass-through attributes to be rendered.
     */
    private static final String[] STRING_ATTRIBUTES = {
        "onClick",
        "onDblClick",
        "onMouseDown",
        "onMouseUp",
        "onMouseOver",
        "onMouseMove",
        "onMouseOut",
        "onKeyPress",
        "onKeyDown",
        "onKeyUp",
        "onFocus",
        "onBlur"
    };

    /**
     * The set of integer pass-through attributes to be rendered.
     */
    private static final String[] INT_ATTRIBUTES = {
        "tabIndex"
    };

    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        // Enforce NPE requirements in the Javadocs
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }
        if (!component.isRendered()) {
            return;
        }

        // If we are not preserving the focus do not update the
        // FocusManager.
        if (component instanceof Body
                && !((Body) component).isPreserveFocus()) {
            return;
        }

        String id = decodeHiddenField(context, FocusManager.FOCUS_FIELD_ID);
        if (id != null) {
            id = id.trim();
            if (id.length() != 0) {
                FocusManager.setRequestFocusElementId(context, id);
            }
        }
    }

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // Start the appropriate element
        if (isPortlet(context) || component == null) {
            return;
        }

        if (!(component instanceof Body)) {
            Object[] params = {
                component.toString(),
                this.getClass().getName(),
                Body.class.getName()
            };
            String message = MessageUtil.getMessage(
                    "com.sun.webui.jsf.resources.LogMessages",
                    "Renderer.component", params);
            throw new FacesException(message);
        }

        writer.startElement("body", component);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        if (isPortlet(context)) {
            return;
        }

        Body body = (Body) component;

        addCoreAttributes(context, component, writer, null);
        addStringAttributes(context, component, writer, STRING_ATTRIBUTES);

        // onload is a special case;
        String onload = body.getOnLoad();
        StringBuffer sb = new StringBuffer(256);
        if (onload != null) {
            sb.append(onload);
            sb.append("; ");
        }
        writer.writeAttribute("onload", sb.toString(), null);

        // Apply a background image
        String imageUrl = body.getImageURL();
        if (imageUrl != null && imageUrl.length() > 0) {
            String resourceUrl = context.getApplication().
                    getViewHandler().getResourceURL(context, imageUrl);
            writer.writeAttribute("background", resourceUrl, null);
        }

        // unload is a special case;
        String onUnload = body.getOnUnload();
        sb = new StringBuffer(256);
        if (onUnload != null) {
            sb.append(onUnload);
            sb.append("; ");
        }
        writer.writeAttribute("onunload", sb.toString(), null);
        addIntegerAttributes(context, component, writer, INT_ATTRIBUTES);
        writer.write("\n");
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        if (isPortlet(context)) {
            return;
        }

        Body body = (Body) component;
        String id = body.getClientId(context);

         // Scroll cookie name.
        String viewId = context.getViewRoot().getViewId();
        // Cookie string.
        String urlString = context.getApplication().getViewHandler().
                getActionURL(context, viewId);
        // Get this after we calculate the urlString...
        viewId = CookieUtils.getValidCookieName(viewId);

        // Pass the developer specified focus id. This will be the
        // default focus id, if the "dynamic" focus element cannot
        // receive the focus. This is the "defaultFocusElementId"
        // javascript argument.
        String defaultFocusElementId = getFocusElementId(context,
                body.getFocus());
        if (defaultFocusElementId != null
                && defaultFocusElementId.isEmpty()) {
            defaultFocusElementId = null;
        }

        // Pass the id of the element that should receive the initial focus.
        // for this response.
        // This has been set during decode by the body, or by a
        // component during the lifecycle processing. It is assumed to be
        // a client id. If its null pass javascript null and not 'null'.
        // This is the "focusElementId" javascript argument.
        String focusElementId = FocusManager.getRequestFocusElementId(context);
        if (focusElementId != null && focusElementId.isEmpty()) {
            focusElementId = null;
        }

        // pass the id of the hidden field that holds the
        // focus element id
        // This is the "focusElementFieldId" argument.
        String focusElementFieldId = FocusManager.FOCUS_FIELD_ID;

        renderScripTag(writer,
                // ws_init_body
                renderCall("init_body", viewId, urlString,
                        defaultFocusElementId, focusElementId,
                        focusElementFieldId));

        writer.endElement("body");
        writer.write("\n");
    }

    /**
     * Helper method to obtain the id of a ComplexComponent sub component.If a
     * developer specified the focus property they may not have been able to
     * obtain the sub component that should receive the focus, since they can
     * only specify the id of the complex component and not the sub
     * component.The returned id must be the id of an HTML element in the page
     * that can receive the focus.
     *
     * @param context faces context
     * @param id element id
     * @return String
     */
    private static String getFocusElementId(final FacesContext context,
            final String id) {

        // Note that this code is duplicated in
        // Body because we don't want to
        // reference the Body.getFocusID, which is deprecated.
        if (id == null || id.length() == 0) {
            return "";
        }

        // Need absolute id.
        // Make sure it doesn't already have a leading
        // NamingContainer.SEPARATOR_CHAR
        String absid = id;
        char separatorChar = UINamingContainer.getSeparatorChar(context);
        if (id.charAt(0) != separatorChar) {
            absid = String.valueOf(separatorChar).concat(id);
        }
        try {
            // Since a developer using Body.setFocus may not be able to
            // identify a sub component of a ComplexComponent, that
            // must be done here.
            // There is an assumption here that the ComplexComponent
            // will recurse to find the appropriate sub-component id.
            // to return.
            UIComponent comp = context.getViewRoot().findComponent(absid);
            if (comp != null && comp instanceof ComplexComponent) {
                return ((ComplexComponent) comp).getFocusElementId(context);
            }
        } catch (Exception e) {
            if (LogUtil.finestEnabled()) {
                LogUtil.finest("BodyRenderer.getFocusElementId: "
                        + "couldn't find component with id " + absid
                        + " rendering focus id as " + id);
            }
        }
        return id;
    }
}
