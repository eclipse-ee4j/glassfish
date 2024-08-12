/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.extras.grizzly;

import com.sun.logging.LogDomains;

import java.util.Collection;
import java.util.logging.Level;

import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.grizzly.http.server.HttpHandler;

/**
 * Deployed grizzly application.
 *
 * @author Jerome Dochez
 */
public class GrizzlyApp implements ApplicationContainer {

    final ClassLoader cl;
    final Collection<Adapter> modules;
    final RequestDispatcher dispatcher;

    public static final class Adapter {
        final HttpHandler service;
        final String contextRoot;
        public Adapter(String contextRoot, HttpHandler adapter) {
            this.service = adapter;
            this.contextRoot = contextRoot;
        }
    }

    public GrizzlyApp(Collection<Adapter> adapters, RequestDispatcher dispatcher, ClassLoader cl) {
        this.modules = adapters;
        this.dispatcher = dispatcher;
        this.cl = cl;
    }

    public Object getDescriptor() {
        return null;
    }

    public boolean start(ApplicationContext startupContext) throws Exception {
        for (Adapter module : modules) {
            dispatcher.registerEndpoint(module.contextRoot, module.service, this);
        }
        return true;
    }

    public boolean stop(ApplicationContext stopContext) {
        boolean success = true;
        for (Adapter module : modules) {
            try {
                dispatcher.unregisterEndpoint(module.contextRoot);
            } catch (EndpointRegistrationException e) {
                LogDomains.getLogger(getClass(), LogDomains.DPL_LOGGER).log(
                        Level.SEVERE, "Exception while unregistering adapter at " + module.contextRoot, e);
                success = false;
            }
        }
        return success;
    }

    public boolean suspend() {
        return false;
    }

    public boolean resume() throws Exception {
        return false;
    }

    public ClassLoader getClassLoader() {
        return cl;
    }
}
