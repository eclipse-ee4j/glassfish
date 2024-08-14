/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.naming.impl;

import jakarta.inject.Inject;

import org.glassfish.api.naming.ClientNamingConfigurator;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;



/**
 * This is the manager that handles all naming operations including
 * publishObject as well as binding environment props, resource and ejb
 * references in the namespace.
 */

@Service
public final class  ClientNamingConfiguratorImpl
        implements ClientNamingConfigurator, PostConstruct {

    @Inject
    private ServiceLocator defaultServices;


    public void postConstruct() {

        SerialInitContextFactory.setDefaultServices(defaultServices);

    }

    public void setDefaultHost(String host) {
         SerialInitContextFactory.setDefaultHost(host);
    }

    public void setDefaultPort(String port) {
         SerialInitContextFactory.setDefaultPort(port);
    }


}
