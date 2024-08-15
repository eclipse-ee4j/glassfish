/*
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

package org.glassfish.admin.restconnector;

import com.sun.enterprise.config.serverbeans.Config;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.container.Adapter;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Implementation of {@link Adapter} for rest based management. It extends from {@link AbstractProxyRestAdapter} that
 * uses a handle-body idiom. The handle implements methods that are metadata/configuration based. The body implements
 * methods that require REST subsystem.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
public class ProxyRestManagementAdapter extends AbstractProxyRestAdapter {

    @Inject
    ServiceLocator services;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    @Override
    protected ServiceLocator getServices() {
        return services;
    }

    @Override
    protected Config getConfig() {
        return config;
    }

    @Override
    protected String getName() {
        return Constants.REST_MANAGEMENT_ADAPTER;
    }

    @Override
    public String getContextRoot() {
        return Constants.REST_MANAGEMENT_CONTEXT_ROOT;
    }
}
