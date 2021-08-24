/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Server;
import org.glassfish.config.support.GlassFishDocument;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.config.DomDocument;

import static org.glassfish.hk2.utilities.BuilderHelper.createConstantDescriptor;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.glassfish.api.admin.ServerEnvironment;

/**
 * User: Jerome Dochez
 * Date: Mar 25, 2008
 * Time: 12:38:30 PM
 */
public abstract class ConfigApiTest extends org.glassfish.tests.utils.ConfigApiTest {

    @Override
    public DomDocument getDocument(ServiceLocator habitat) {
        DomDocument doc = habitat.getService(GlassFishDocument.class);
        if (doc == null) {
            return new GlassFishDocument(habitat, Executors.newCachedThreadPool(new ThreadFactory() {

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = Executors.defaultThreadFactory().newThread(r);
                    t.setDaemon(true);
                    return t;
                }

            }));
        }
        return doc;
    }


    @Override
    public void decorate(ServiceLocator locator) {
        Server server = locator.getService(Server.class, "server");
        if (server == null) {
            return;
        }
        ActiveDescriptor<Server> serverDescriptor
            = createConstantDescriptor(server, ServerEnvironment.DEFAULT_INSTANCE_NAME, Server.class);
        ServiceLocatorUtilities.addOneDescriptor(locator, serverDescriptor);

        server.getConfig().addIndex(locator, ServerEnvironment.DEFAULT_INSTANCE_NAME);

        Cluster c = server.getCluster();
        if (c != null) {
            ActiveDescriptor<Cluster> clusterDescriptor
                = createConstantDescriptor(c, ServerEnvironment.DEFAULT_INSTANCE_NAME, Cluster.class);
            ServiceLocatorUtilities.addOneDescriptor(locator, clusterDescriptor);
        }
    }
}
