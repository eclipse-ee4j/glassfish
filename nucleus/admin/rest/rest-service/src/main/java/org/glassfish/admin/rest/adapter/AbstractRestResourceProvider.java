/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.admin.rest.resources.ReloadResource;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.common.util.admin.RestSessionManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.MessageProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.CsrfProtectionFilter;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.MediaType;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base class for various REST resource providers
 */
public abstract class AbstractRestResourceProvider implements RestResourceProvider, Serializable {
    // content of this class has been copied from RestAdapter.java
    protected Map<String, MediaType> mappings;

    protected AbstractRestResourceProvider() {
    }

    @Override
    public boolean enableModifAccessToInstances() {
        return false;
    }

    protected Map<String, MediaType> getMimeMappings() {
        if (mappings == null) {
            mappings = new HashMap<String, MediaType>();
            mappings.put("xml", MediaType.APPLICATION_XML_TYPE);
            mappings.put("json", MediaType.APPLICATION_JSON_TYPE);
            mappings.put("html", MediaType.TEXT_HTML_TYPE);
            mappings.put("js", new MediaType("text", "javascript"));
        }
        return mappings;
    }

    protected Feature getJsonFeature() {
        return new JettisonFeature();
    }

    @Override
    public ResourceConfig getResourceConfig(Set<Class<?>> classes, final ServerContext sc, final ServiceLocator habitat,
            final Set<? extends Binder> additionalBinders) throws EndpointRegistrationException {
        final Reloader r = new Reloader();

        ResourceConfig rc = new ResourceConfig(classes);
        rc.property(ServerProperties.MEDIA_TYPE_MAPPINGS, getMimeMappings());
        rc.register(CsrfProtectionFilter.class);

        //        TODO - JERSEY2
        //        RestConfig restConf = ResourceUtil.getRestConfig(habitat);
        //        if (restConf != null) {
        //            if (restConf.getLogOutput().equalsIgnoreCase("true")) { //enable output logging
        //                rc.getContainerResponseFilters().add(LoggingFilter.class);
        //            }
        //            if (restConf.getLogInput().equalsIgnoreCase("true")) { //enable input logging
        //                rc.getContainerRequestFilters().add(LoggingFilter.class);
        //            }
        //            if (restConf.getWadlGeneration().equalsIgnoreCase("false")) { //disable WADL
        //                rc.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, Boolean.TRUE);
        //            }
        //        }
        //        else {
        //                 rc.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, Boolean.TRUE);
        //        }
        //
        rc.register(r);
        rc.register(ReloadResource.class);
        rc.register(new MultiPartFeature());
        //rc.register(getJsonFeature());
        rc.register(new AbstractBinder() {

            @Override
            protected void configure() {
                AbstractActiveDescriptor<Reloader> descriptor = BuilderHelper.createConstantDescriptor(r);
                descriptor.addContractType(Reloader.class);
                bind(descriptor);

                AbstractActiveDescriptor<ServerContext> scDescriptor = BuilderHelper.createConstantDescriptor(sc);
                scDescriptor.addContractType(ServerContext.class);
                bind(scDescriptor);

                LocatorBridge locatorBridge = new LocatorBridge(habitat);
                AbstractActiveDescriptor<LocatorBridge> hDescriptor = BuilderHelper.createConstantDescriptor(locatorBridge);
                bind(hDescriptor);

                RestSessionManager rsm = habitat.getService(RestSessionManager.class);
                AbstractActiveDescriptor<RestSessionManager> rmDescriptor = BuilderHelper.createConstantDescriptor(rsm);
                bind(rmDescriptor);
            }
        });

        for (Binder b : additionalBinders) {
            rc.register(b);
        }

        rc.property(MessageProperties.LEGACY_WORKERS_ORDERING, true);
        return rc;
    }
}
