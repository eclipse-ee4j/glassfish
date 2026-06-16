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

package com.sun.jsftemplating.layout.descriptors.handler;

import com.sun.jsftemplating.el.PageSessionResolver;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 * This class implements the OutputType interface to provide a way to get/set Output values from the Page attribute Map
 * (see {@link PageSessionResolver}).
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class PageAttributeOutputType implements OutputType {

    /**
     * <p>
     * This method is responsible for retrieving the value of the Output from a Page Session Attribute. 'key' may be null,
     * if this occurs, a default name will be provided. That name will follow the following format:
     * </p>
     *
     * <p>
     * [handler-id]:[output-name]
     * </p>
     *
     * @param context The HandlerContext
     *
     * @param outDesc The {@link IODescriptor} for this Output value in which to obtain the value
     *
     * @param key The optional 'key' to use when retrieving the value from the Page Session Attribute Map.
     *
     * @return The requested value, <code>null</code> if not found.
     */
    @Override
    public Object getValue(HandlerContext context, IODescriptor outDesc, String key) {
        if (key == null) {
            // Provide a reasonably unique default
            key = context.getHandlerDefinition().getId() + ':' + outDesc.getName();
        }

        // Get the Page Session Map
        Map<String, Serializable> map = PageSessionResolver.getPageSession(context.getFacesContext(), (UIViewRoot) null);

        // Get the value to return
        Serializable value = null;
        if (map != null) {
            value = map.get(key);
        }

        // Return it...
        return value;
    }

    /**
     * <p>
     * This method is responsible for setting the value of the Output to a Page Session Attribute. 'key' may be null, in
     * this case, a default name will be provided. That name will follow the following format:
     * </p>
     *
     * <p>
     * [handler-id]:[output-name]
     * </p>
     *
     * @param context The {@link HandlerContext}
     *
     * @param outDesc The {@link IODescriptor} for this Output value in which to obtain the value
     *
     * @param key The optional 'key' to use when setting the value into the Page Session Attribute Map
     *
     * @param value The value to set
     */
    @Override
    public void setValue(HandlerContext context, IODescriptor outDesc, String key, Object value) {
        // Ensure we have a key...
        if (key == null) {
            // We don't, provide a reasonably unique default
            key = context.getHandlerDefinition().getId() + ':' + outDesc.getName();
        }

        // Get the Page Session Map
        FacesContext ctx = context.getFacesContext();
        Map<String, Serializable> map = PageSessionResolver.getPageSession(ctx, (UIViewRoot) null);
        if (map == null) {
            map = PageSessionResolver.createPageSession(ctx, (UIViewRoot) null);
        }

        // Set the Page Session Attribute Map
        map.put(key, (Serializable) value);
    }
}
