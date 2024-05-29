/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.glassfish.admin.rest.adapter.Reloader;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author ludo
 */
@Path("reload")
public class ReloadResource {

    @Inject
    private Reloader reloader;
    @Inject
    private ResourceConfig resourceConfig;
    @Inject
    private ServerContext serverContext;

    @POST
    public void reload() {
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(serverContext.getCommonClassLoader());
            resourceConfig.getClasses().add(org.glassfish.admin.rest.resources.StaticResource.class);
            reloader.reload();
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }
    }
}
