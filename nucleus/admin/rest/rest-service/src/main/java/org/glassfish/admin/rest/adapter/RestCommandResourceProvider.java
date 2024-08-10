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

import jakarta.ws.rs.core.MediaType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.glassfish.admin.rest.provider.ActionReportJson2Provider;
import org.glassfish.admin.rest.provider.AdminCommandStateJsonProvider;
import org.glassfish.admin.rest.provider.CommandModelStaxProvider;
import org.glassfish.admin.rest.provider.ParamsWithPayloadMultipartWriter;
import org.glassfish.admin.rest.provider.ProgressStatusEventJsonProvider;
import org.glassfish.admin.rest.provider.ProgressStatusJsonProvider;
import org.glassfish.admin.rest.readers.JsonParameterMapProvider;
import org.glassfish.admin.rest.readers.MultipartFDPayloadReader;
import org.glassfish.admin.rest.readers.ParameterMapFormReader;
import org.glassfish.admin.rest.resources.admin.CommandResource;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.message.MessageProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.CsrfProtectionFilter;
import org.glassfish.jersey.server.filter.UriConnegFilter;

/**
 * Responsible for providing ReST resources for {@code asadmin} and {@code cadmin} communication.
 *
 * @author mmares
 * @author sanjeeb.sahoo@oracle.com
 */
public class RestCommandResourceProvider extends AbstractRestResourceProvider {

    public RestCommandResourceProvider() {
        super();
    }

    @Override
    public boolean enableModifAccessToInstances() {
        return true;
    }

    @Override
    public Map<String, MediaType> getMimeMappings() {
        if (mappings == null) {
            mappings = new HashMap<String, MediaType>();
            mappings.put("json", MediaType.APPLICATION_JSON_TYPE);
            mappings.put("txt", MediaType.TEXT_PLAIN_TYPE);
            mappings.put("multi", new MediaType("multipart", null));
            mappings.put("sse", new MediaType("text", "event-stream"));
        }

        return mappings;
    }

    public static Set<Class<?>> getResourceClasses() {
        final Set<Class<?>> r = new HashSet<Class<?>>();
        r.add(CommandResource.class);
        //ActionReport - providers
        r.add(ActionReportJson2Provider.class);
        //CommandModel - providers
        r.add(CommandModelStaxProvider.class);
        //Parameters
        r.add(ParameterMapFormReader.class);
        r.add(JsonParameterMapProvider.class);
        //Multipart
        //r.add(PayloadPartProvider.class);
        r.add(MultipartFDPayloadReader.class);
        r.add(ParamsWithPayloadMultipartWriter.class);
        //SSE data
        r.add(SseFeature.class);
        r.add(AdminCommandStateJsonProvider.class);
        //ProgressStatus
        r.add(ProgressStatusJsonProvider.class);
        r.add(ProgressStatusEventJsonProvider.class);
        //        //Debuging filters
        //        r.add(LoggingFilter.class);
        return r;
    }

    @Override
    public Set<Class<?>> getResourceClasses(ServiceLocator habitat) {
        return getResourceClasses();
    }

    @Override
    public String getContextRoot() {
        return org.glassfish.admin.restconnector.Constants.REST_COMMAND_CONTEXT_ROOT;
    }

    @Override
    public ResourceConfig getResourceConfig(Set<Class<?>> classes, final ServerContext sc, final ServiceLocator habitat,
            final Set<? extends Binder> additionalBinders) throws EndpointRegistrationException {
        ResourceConfig rc = new ResourceConfig(classes);
        rc.property(ServerProperties.MEDIA_TYPE_MAPPINGS, getMimeMappings());
        rc.register(CsrfProtectionFilter.class);
        rc.register(UriConnegFilter.class);
        for (Binder b : additionalBinders) {
            rc.register(b);
        }
        rc.property(MessageProperties.LEGACY_WORKERS_ORDERING, true);
        //Disable as much as possible
        rc.property(ServerProperties.JSON_PROCESSING_FEATURE_DISABLE, true);
        rc.property(ServerProperties.WADL_FEATURE_DISABLE, true);
        rc.property(ServerProperties.BV_FEATURE_DISABLE, true);
        rc.property(ServerProperties.RESOURCE_VALIDATION_DISABLE, true);
        return rc;
    }

}
