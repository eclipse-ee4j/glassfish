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

package com.sun.jsftemplating.util;

import com.sun.jsftemplating.layout.LayoutDefinitionManager;
import com.sun.jsftemplating.layout.descriptors.LayoutElement;
import com.sun.jsftemplating.layout.descriptors.handler.Handler;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContextImpl;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerDefinition;

import jakarta.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * <p>
 * This class is for {@link Handler} utility methods.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class HandlerUtil {

    /**
     * <p>
     * This method invokes the {@link Handler} identified by the given id.
     * </p>
     *
     * @param handlerId The id of the globally defined {@link Handler}.
     * @param elt The {@link LayoutElement} to associate with the {@link Handler}, it does not need to reference the
     * {@link Handler}.
     * @param args <code>Object[]</code> that represent the arguments to pass into the {@link Handler}.
     *
     * @return The value returned from the {@link Handler} if any.
     */
    public static Object dispatchHandler(String handlerId, LayoutElement elt, Object... args) {
        // Get the Handler
        HandlerDefinition def = elt.getLayoutDefinition().getHandlerDefinition(handlerId);
        if (def == null) {
            def = LayoutDefinitionManager.getGlobalHandlerDefinition(handlerId);
        }
        if (def == null) {
            throw new IllegalArgumentException("Unable to locate handler definition for '" + handlerId + "'!");
        }
        Handler handler = new Handler(def);
        if (args != null) {
            // Basic check to make sure we have valid arguments
            int size = args.length;
            if (size % 2 == 1) {
                throw new IllegalArgumentException("Arguments to " + "dispatchHandler must be paired: name1, value1, "
                        + "name2, value2.  An odd number was received which " + "is invalid.");
            }

            // Set all the input values
            String name = null;
            Object value = null;
            for (int count = 0; count < size; count += 2) {
                name = (String) args[count];
                value = args[count + 1];
                handler.setInputValue(name, value);
            }
        }

        // Put it in a List
        List<Handler> handlers = new ArrayList<>();
        handlers.add(handler);

        // Create a HandlerContext...
        HandlerContext handlerCtx = new HandlerContextImpl(FacesContext.getCurrentInstance(), elt, new EventObject(elt), "none");

        return elt.dispatchHandlers(handlerCtx, handlers);
    }
}
