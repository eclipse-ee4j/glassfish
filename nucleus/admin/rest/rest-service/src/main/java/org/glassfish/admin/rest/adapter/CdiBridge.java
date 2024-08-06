/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.admin.rest.adapter;

import java.util.logging.Level;

import org.glassfish.admin.rest.RestLogging;
import org.glassfish.common.util.admin.RestSessionManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.internal.api.ServerContext;

class CdiBridge extends AbstractBinder {

    private final ServiceLocator serviceLocator;
    private final Reloader reloader;
    private final ServerContext serverContext;

    CdiBridge(ServiceLocator serviceLocator, Reloader reloader, ServerContext serverContext) {
        this.serviceLocator = serviceLocator;
        this.reloader = reloader;
        this.serverContext = serverContext;
    }

    @Override
    protected void configure() {
        RestLogging.restLogger.log(Level.FINEST, "CdiBridge.configure()");
        AbstractActiveDescriptor<Reloader> descriptor = BuilderHelper.createConstantDescriptor(reloader);
        descriptor.addContractType(Reloader.class);
        bind(descriptor);

        AbstractActiveDescriptor<ServerContext> scDescriptor = BuilderHelper.createConstantDescriptor(serverContext);
        scDescriptor.addContractType(ServerContext.class);
        bind(scDescriptor);

        LocatorBridge locatorBridge = new LocatorBridge(serviceLocator);
        AbstractActiveDescriptor<LocatorBridge> hDescriptor = BuilderHelper.createConstantDescriptor(locatorBridge);
        bind(hDescriptor);

        RestSessionManager rsm = serviceLocator.getService(RestSessionManager.class);
        AbstractActiveDescriptor<RestSessionManager> rmDescriptor = BuilderHelper.createConstantDescriptor(rsm);
        bind(rmDescriptor);
    }

}
