/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.ws.rs.core.Feature;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.glassfish.admin.rest.JavadocWadlGeneratorConfig;
import org.glassfish.admin.rest.RestLogging;
import org.glassfish.admin.rest.RestResource;
import org.glassfish.admin.rest.generator.ASMResourcesGenerator;
import org.glassfish.admin.rest.generator.ResourcesGenerator;
import org.glassfish.admin.rest.resources.GeneratorResource;
import org.glassfish.admin.rest.resources.StatusGenerator;
import org.glassfish.admin.rest.resources.custom.ManagementProxyResource;
import org.glassfish.admin.restconnector.Constants;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.jvnet.hk2.config.Dom;

/**
 * Responsible for providing ReST resources for management operations.
 */
public class RestManagementResourceProvider extends AbstractRestResourceProvider {

    @Override
    public Feature getJsonFeature() {
        RestLogging.restLogger.log(Level.SEVERE, "Hey, you... FIX ME!!! {0}", RestManagementResourceProvider.class.getName());
        return super.getJsonFeature();
        //        return new JacksonFeature();
    }

    @Override
    public String getContextRoot() {
        return Constants.REST_MANAGEMENT_CONTEXT_ROOT;
    }

    @Override
    public ResourceConfig getResourceConfig(Set<Class<?>> classes, final ServerContext serverContext,
        final ServiceLocator serviceLocator, final Set<? extends Binder> additionalBinders)
        throws EndpointRegistrationException {
        ResourceConfig rc = super.getResourceConfig(classes, serverContext, serviceLocator, additionalBinders);
        RestLogging.restLogger.log(Level.FINEST, () -> "Extending binding configuration with " + this);

        registerExtendedWadlConfig(classes, rc, serviceLocator);
        rc.register(ExceptionFilter.class);
        rc.property(ServerProperties.RESOURCE_VALIDATION_DISABLE, Boolean.TRUE);
        return rc;
    }

    @Override
    public Set<Class<?>> getResourceClasses(ServiceLocator serviceLocator) {
        //         return getLazyJersey().getResourcesConfigForManagement(locatorBridge);

        generateASM(serviceLocator);
        Class<?> domainResourceClass = null;
        try {
            domainResourceClass = Class.forName("org.glassfish.admin.rest.resources.generatedASM.DomainResource");
        } catch (ClassNotFoundException ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        }

        final Set<Class<?>> r = new HashSet<>();
        addCompositeResources(r, serviceLocator);

        // uncomment if you need to run the generator:
        if ("true".equals(System.getenv("REST_DEBUG"))) {
            r.add(GeneratorResource.class);
        }
        r.add(StatusGenerator.class);
        //        r.add(ClientGenerator.class);
        //        r.add(ModelResource.class);
        //r.add(ActionReportResource.class);

        r.add(domainResourceClass);
        //        r.add(DomainResource.class);
        r.add(ManagementProxyResource.class);
        r.add(org.glassfish.admin.rest.resources.SessionsResource.class);

        // TODO this needs to be added to all rest adapters that want to be secured.
        //      Decide on it after the discussion to unify RestAdapter is concluded
        r.add(org.glassfish.admin.rest.resources.StaticResource.class);

        //body readers, not in META-INF/services anymore
        r.add(org.glassfish.admin.rest.readers.RestModelReader.class);
        r.add(org.glassfish.admin.rest.readers.RestModelListReader.class);
        r.add(org.glassfish.admin.rest.readers.FormReader.class);
        r.add(org.glassfish.admin.rest.readers.ParameterMapFormReader.class);
        r.add(org.glassfish.admin.rest.readers.JsonHashMapProvider.class);
        r.add(org.glassfish.admin.rest.readers.JsonPropertyListReader.class);
        r.add(org.glassfish.admin.rest.readers.JsonParameterMapProvider.class);

        r.add(org.glassfish.admin.rest.readers.XmlHashMapProvider.class);
        r.add(org.glassfish.admin.rest.readers.XmlPropertyListReader.class);

        //body writers
        r.add(org.glassfish.admin.rest.provider.ActionReportResultHtmlProvider.class);
        r.add(org.glassfish.admin.rest.provider.ActionReportResultJsonProvider.class);
        r.add(org.glassfish.admin.rest.provider.ActionReportResultXmlProvider.class);

        r.add(org.glassfish.admin.rest.provider.CollectionWriter.class);
        r.add(org.glassfish.admin.rest.provider.MapWriter.class);
        r.add(org.glassfish.admin.rest.provider.ResponseBodyWriter.class);
        r.add(org.glassfish.admin.rest.provider.RestCollectionProvider.class);
        r.add(org.glassfish.admin.rest.provider.RestModelWriter.class);
        //        r.add(ProxyMessageBodyWriter.class);

        r.add(org.glassfish.admin.rest.provider.FormWriter.class);

        r.add(org.glassfish.admin.rest.provider.GetResultListHtmlProvider.class);
        r.add(org.glassfish.admin.rest.provider.GetResultListJsonProvider.class);
        r.add(org.glassfish.admin.rest.provider.GetResultListXmlProvider.class);

        // override JSON-B when returning application/json as String
        r.add(org.glassfish.admin.rest.provider.JsonStringReaderWriter.class);

        r.add(org.glassfish.admin.rest.provider.OptionsResultJsonProvider.class);
        r.add(org.glassfish.admin.rest.provider.OptionsResultXmlProvider.class);

        r.add(SseFeature.class);
        r.add(org.glassfish.admin.rest.provider.AdminCommandStateJsonProvider.class);
        r.add(org.glassfish.admin.rest.provider.ProgressStatusJsonProvider.class);
        r.add(org.glassfish.admin.rest.provider.ProgressStatusEventJsonProvider.class);

        return r;
    }

    @SuppressWarnings("unchecked")
    private void registerExtendedWadlConfig(Set<Class<?>> classes, ResourceConfig rc, ServiceLocator serviceLocator) {
        List<ServiceHandle<JavadocWadlGeneratorConfig>> handles = serviceLocator.getAllServiceHandles(JavadocWadlGeneratorConfig.class);
        for (ServiceHandle<JavadocWadlGeneratorConfig> handle : handles) {
            ActiveDescriptor<JavadocWadlGeneratorConfig> ad = handle.getActiveDescriptor();
            if (!ad.isReified()) {
                ad = (ActiveDescriptor<JavadocWadlGeneratorConfig>) serviceLocator.reifyDescriptor(ad);
            }
            final Class<?> implementationClass = ad.getImplementationClass();
            rc.property(ServerProperties.WADL_GENERATOR_CONFIG, implementationClass.getName());
            classes.add(implementationClass);
        }

    }

    private void generateASM(ServiceLocator habitat) {
        try {
            Domain entity = habitat.getService(Domain.class);
            Dom dom = Dom.unwrap(entity);

            ResourcesGenerator resourcesGenerator = new ASMResourcesGenerator(habitat);
            resourcesGenerator.generateSingle(dom.document.getRoot().model, dom.document);
            resourcesGenerator.endGeneration();
        } catch (Exception ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private void addCompositeResources(Set<Class<?>> r, ServiceLocator locator) {
        List<ServiceHandle<RestResource>> handles = locator.getAllServiceHandles(RestResource.class);
        for (ServiceHandle<RestResource> handle : handles) {
            ActiveDescriptor<RestResource> ad = handle.getActiveDescriptor();
            if (!ad.isReified()) {
                ad = (ActiveDescriptor<RestResource>) locator.reifyDescriptor(ad);
            }
            r.add(ad.getImplementationClass());
        }
    }
}
