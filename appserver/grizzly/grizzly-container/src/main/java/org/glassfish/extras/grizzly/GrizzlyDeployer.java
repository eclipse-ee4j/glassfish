/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.extras.grizzly.GrizzlyModuleDescriptor.GrizzlyProperty;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Jerome Dochez
 */
@Service(name="grizzly")
public class GrizzlyDeployer implements Deployer<GrizzlyContainer, GrizzlyApp> {

    @Inject
    RequestDispatcher dispatcher;

    @Override
    public MetaData getMetaData() {
        return new MetaData(false, new Class[] { GrizzlyModuleDescriptor.class}, null);
    }

    @Override
    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return type.cast(new GrizzlyModuleDescriptor(context.getSource(), context.getLogger()));
    }

    @Override
    public boolean prepare(DeploymentContext context) {
        return true;
    }

    /**
     * Deploy a {@link GrizzlyApp.Adapter}.
     *
     * @param container
     * @param context
     * @return GrizzlyApp
     */
    @Override
    public GrizzlyApp load(GrizzlyContainer container, DeploymentContext context) {

        GrizzlyModuleDescriptor configs = context.getModuleMetaData(GrizzlyModuleDescriptor.class);

        LinkedList<GrizzlyApp.Adapter> modules = new LinkedList<>();


        Map<String,ArrayList<GrizzlyProperty>>
                        properties = configs.getProperties();
        for (Map.Entry<String, String> config : configs.getAdapters().entrySet()) {
            HttpHandler httpHandler;
            try {
                Class<?> adapterClass = context.getClassLoader().loadClass(config.getValue());
                httpHandler = HttpHandler.class.cast(adapterClass.getDeclaredConstructor().newInstance());
                ArrayList<GrizzlyProperty> list = properties.get(config.getValue());
                for (GrizzlyProperty p : list) {
                    IntrospectionUtils.setProperty(httpHandler, p.name, p.value);
                }
                httpHandler.start();
            } catch(Exception e) {
                context.getLogger().log(Level.SEVERE, e.getMessage(),e);
                return null;
            }
            modules.add(new GrizzlyApp.Adapter(config.getKey(), httpHandler));
        }
        return new GrizzlyApp(modules, dispatcher, context.getClassLoader());

    }

    @Override
    public void unload(GrizzlyApp appContainer, DeploymentContext context) {
    }

    @Override
    public void clean(DeploymentContext context) {
    }
}
