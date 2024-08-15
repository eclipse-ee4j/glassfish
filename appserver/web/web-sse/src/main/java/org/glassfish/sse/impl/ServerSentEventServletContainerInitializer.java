/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.sse.impl;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.annotation.HandlesTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.sse.api.ServerSentEvent;
import org.glassfish.sse.api.ServerSentEventHandler;

/**
 * Registers a context listener to get ServletContext
 *
 * Registers a servlet dynamically if there are ServerSentEventHandlers in an application.
 *
 * @author Jitendra Kotamraju
 */
@HandlesTypes(ServerSentEvent.class)
public class ServerSentEventServletContainerInitializer implements ServletContainerInitializer {

    public void onStartup(Set<Class<?>> set, ServletContext ctx) throws ServletException {
        if (set == null || set.isEmpty()) {
            return;
        }

        // Check if there is already a servlet for server sent events
        Map<String, ? extends ServletRegistration> registrations = ctx.getServletRegistrations();
        for (ServletRegistration reg : registrations.values()) {
            if (reg.getClass().equals(ServerSentEventServlet.class)) {
                return;
            }
        }

        // Collect all the url patterns for server sent event handlers
        List<String> urlPatternList = new ArrayList<String>();

        for (Class<?> clazz : set) {
            if (ServerSentEventHandler.class.isAssignableFrom(clazz)) {
                ServerSentEvent handler = clazz.getAnnotation(ServerSentEvent.class);
                if (handler == null) {
                    throw new RuntimeException("ServerSentEventHandler Class " + clazz + " doesn't have WebHandler annotation");
                }
                urlPatternList.add(handler.value());
            }
        }

        // Register a servlet for all the url patterns of server sent event handler
        if (!urlPatternList.isEmpty()) {
            ServletRegistration.Dynamic reg = ctx.addServlet("sse servlet", ServerSentEventServlet.class);
            reg.setAsyncSupported(true);
            reg.addMapping(urlPatternList.toArray(new String[urlPatternList.size()]));
        }
    }

}
