/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.webservices;

import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.Module;
import com.sun.xml.ws.api.server.ResourceInjector;
import com.sun.xml.ws.api.server.ServerPipelineHook;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;

import jakarta.servlet.ServletContext;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;

public class JAXWSContainer extends Container {

    private final ServletContext servletContext;
    private final WebServiceEndpoint endpoint;
    private final JAXWSServletModule module;

    public JAXWSContainer(ServletContext servletContext, WebServiceEndpoint ep) {
        this.servletContext = servletContext;
        this.endpoint = ep;

        if (servletContext != null) {
            this.module = JAXWSServletModule.getServletModule(servletContext.getContextPath());
        } else {
            this.module = null;
        }
    }

    public void addEndpoint(ServletAdapter adapter) {
        if (module != null) {
            module.addEndpoint(endpoint.getEndpointAddressUri(), adapter);
        }
    }

    public <T> T getSPI(Class<T> spiType) {
        if (ServletContext.class.isAssignableFrom(spiType)) {
            return (T) servletContext;
        }

        if (ServerPipelineHook.class.isAssignableFrom(spiType)) {
            ServiceLocator h = Globals.getDefaultHabitat();
            ServerPipeCreator s = h.getService(ServerPipeCreator.class);
            s.init(endpoint);
            return ((T) s);
        }

        if (ResourceInjector.class.isAssignableFrom(spiType)) {
            // Give control of injection time only for servlet endpoints
            if (endpoint.implementedByWebComponent()) {
                return (T) new ResourceInjectorImpl(endpoint);
            }
        }

        if (Module.class.isAssignableFrom(spiType)) {
            if (module != null) {
                return ((T) spiType.cast(module));
            }
        }

        return null;
    }
}
