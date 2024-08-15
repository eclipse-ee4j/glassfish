/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * JAXWSServletModule.java
 *
 * Created on June 19, 2007, 5:51 PM
 * @author Mike Grogan
 */

package org.glassfish.webservices;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.ServletModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of JAX-WS ServletModule SPI used by WSIT WS-MetadataExchange.
 * In the current 109 design, each endpoint has a unique JAXWSContainer.  On
 * the other hand, the requirements imposed by WSIT WS-MetadataExchange
 * require that all endpoints sharing a context root share a ServletMoule.
 * Therefore, in general, multiple JAXWSContainers will share a JAXWSServletModule,
 * so JAXWSContainer must use a lookup in the static
 * <code>JAXWSServletModule.modules</code> to find its associatiated module.
 */

public class JAXWSServletModule extends ServletModule {

    //Map of context-roots to JAXWSServletModules
    private final static Map<String, JAXWSServletModule> modules =
            new ConcurrentHashMap<String, JAXWSServletModule>();

    //Map of uri->BoundEndpoint used to implement getBoundEndpoint.  Map is rather
    //than Set, so that when a new endpoint is redeployed at a given uri, the old
    //endpoint will be replaced by the new endpoint.  The values() method of the
    //field is returned by <code>getBoundEndpoints</code>.
     private final Map<String, BoundEndpoint> endpoints =
             new ConcurrentHashMap<String, BoundEndpoint>();

    //the context-root for endpoints belonging to this module.
    private final String contextPath;


    public static synchronized JAXWSServletModule getServletModule(String contextPath) {

        JAXWSServletModule ret = modules.get(contextPath);
        if (ret == null) {
            ret = new JAXWSServletModule(contextPath);
            modules.put(contextPath, ret);
        }
        return ret;
    }

    public static void destroy(String contextPath) {
        modules.remove(contextPath);
    }

    private JAXWSServletModule(String contextPath) {
            this.contextPath = contextPath;
    }

    public void addEndpoint(String uri, ServletAdapter adapter) {
        endpoints.put(uri, adapter);
    }

    @Override
    public @NotNull List<BoundEndpoint> getBoundEndpoints() {
            return new ArrayList(endpoints.values());
    }

    @Override
    public @NotNull String getContextPath() {
        return contextPath;
    }
}
