/*
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

package org.glassfish.admin.rest;

import java.beans.PropertyChangeEvent;

import org.glassfish.admin.rest.adapter.Reloader;
import org.glassfish.admin.rest.utils.ResourceUtil;
import org.glassfish.admin.restconnector.RestConfig;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * This test listen to a property change event only injecting the parent containing the property.
 *
 * @author Ludovic Champenois
 */
// TODO: Delete this once we're sure it's no longer needed
@Deprecated
public class RestConfigChangeListener implements ConfigListener {
    //    private Reloader r;
    private ServerContext sc;

    public RestConfigChangeListener(ServiceLocator habitat, Reloader reload, ResourceConfig rc, ServerContext sc) {
        //        this.r = reload;
        this.sc = sc;

        RestConfig target = ResourceUtil.getRestConfig(habitat);

        if (target != null) {
            ((ObservableBean) ConfigSupport.getImpl(target)).addListener(this);
        }

        /// ((ObservableBean) ConfigSupport.getImpl(target)).removeListener(this);
    }

    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader apiClassLoader = sc.getCommonClassLoader();
            Thread.currentThread().setContextClassLoader(apiClassLoader);

            // TODO - JERSEY2
            //            rc.getContainerResponseFilters().clear();
            //            rc.getContainerRequestFilters().clear();
            //            rc.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, Boolean.FALSE);

            //            RestConfig restConf = ResourceUtil.getRestConfig(habitat);
            //            if (restConf != null) {
            //                if (restConf.getLogOutput().equalsIgnoreCase("true")) { //enable output logging
            //                    rc.getContainerResponseFilters().add(LoggingFilter.class);
            //                }
            //                if (restConf.getLogInput().equalsIgnoreCase("true")) { //enable input logging
            //                    rc.getContainerRequestFilters().add(LoggingFilter.class);
            //                }
            //                if (restConf.getWadlGeneration().equalsIgnoreCase("false")) { //disable WADL
            //                    rc.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, Boolean.TRUE);
            //                }
            //            }

            //            r.reload();
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }
        return null;
    }
}
