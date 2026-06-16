/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout.descriptors;

import com.sun.jsftemplating.component.ComponentUtil;
import com.sun.jsftemplating.layout.descriptors.handler.Handler;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerDefinition;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <p>
 * This class defines a LayoutMarkup. A LayoutMarkup provides a means to start a markup tag and associate the current
 * UIComponent with it for tool support. It also has the benefit of properly closing the markup tag for you.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutMarkup extends LayoutElementBase implements LayoutElement {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public LayoutMarkup(LayoutElement parent, String tag, String type) {
        super(parent, tag);
        _tag = tag;
        _type = type;

        // Add "afterEncode" handler to close the tag (if there is a close tag)
        if (!type.equals(TYPE_OPEN)) {
            ArrayList<Handler> handlers = new ArrayList<>();
            handlers.add(afterEncodeHandler);
            setHandlers(AFTER_ENCODE, handlers);
        }
    }

    /**
     *
     */
    public String getTag() {
        return _tag;
    }

    /**
     *
     */
    public String getType() {
        return _type;
    }

    /**
     * <p>
     * This method displays the text described by this component. If the text includes an EL expression, it will be
     * evaluated. It returns true to render children.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param component The <code>UIComponent</code>
     *
     * @return false
     */
    @Override
    protected boolean encodeThis(FacesContext context, UIComponent component) throws IOException {
        if (getType().equals(TYPE_CLOSE)) {
            return true;
        }

        // Get the ResponseWriter
        ResponseWriter writer = context.getResponseWriter();

        // Render...
        Object value = resolveValue(context, component, getTag());
        if (value != null) {
            writer.startElement(value.toString(), component);
        }

        // Always render children
        return true;
    }

    /**
     * <p>
     * This handler takes care of closing the tag.
     * </p>
     *
     * @param context The HandlerContext.
     */
    public static void afterEncodeHandler(HandlerContext context) throws IOException {
        FacesContext ctx = context.getFacesContext();
        ResponseWriter writer = ctx.getResponseWriter();
        LayoutMarkup markup = (LayoutMarkup) context.getLayoutElement();
        Object value = ComponentUtil.getInstance(ctx).resolveValue(ctx, markup, (UIComponent) context.getEventObject().getSource(), markup.getTag());
        if (value != null) {
            writer.endElement(value.toString());
        }
    }

    /**
     *
     */
    public static final HandlerDefinition afterEncodeHandlerDef = new HandlerDefinition("_markupAfterEncode");

    /**
     *
     */
    public static final Handler afterEncodeHandler = new Handler(afterEncodeHandlerDef);

    static {
        afterEncodeHandlerDef.setHandlerMethod(LayoutMarkup.class.getName(), "afterEncodeHandler");
    }

    /**
     * <p>
     * This markup type writes out both the opening and closing tags.
     * </p>
     */
    public static final String TYPE_BOTH = "both";

    /**
     * <p>
     * This markup type writes out the closing tag.
     * </p>
     */
    public static final String TYPE_CLOSE = "close";

    /**
     * <p>
     * This markup type writes out the opening tag.
     * </p>
     */
    public static final String TYPE_OPEN = "open";

    private String _tag = null;
    private String _type = null;
}
