/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.v3.common.PropsFileActionReporter;

import jakarta.inject.Inject;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import javax.security.auth.Subject;

import org.glassfish.admin.rest.RestLogging;
import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.extras.ExtrasUtilities;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.internal.api.InternalSystemAdministrator;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.inject.hk2.Hk2ReferencingFactory;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author martinmares
 */
@Service
@StartupRunLevel
public class JerseyContainerCommandService implements PostConstruct {

    @Inject
    private ServiceLocator serviceLocator;

    @Inject
    private InternalSystemAdministrator kernelIdentity;

    private Future<JerseyContainer> future;

    @Override
    public void postConstruct() {
        if (Boolean.parseBoolean(System.getenv("AS_INIT_REST_EAGER"))) {
            ExecutorService executor = Executors.newFixedThreadPool(8);
            this.future = executor.submit(() -> {
                JerseyContainer result = exposeContext();
                return result;
            });
            executor.execute(() -> {
                CommandRunner cr = serviceLocator.getService(CommandRunner.class);
                final CommandRunner.CommandInvocation invocation = cr.getCommandInvocation("uptime",
                    new PropsFileActionReporter(), kernelIdentity.getSubject());
                invocation.parameters(new ParameterMap());
                invocation.execute();
            });
            executor.shutdown();
        }
    }

    public JerseyContainer getJerseyContainer() throws EndpointRegistrationException {
        try {
            if (future == null) {
                return exposeContext();
            }
            return future.get();
        } catch (InterruptedException ex) {
            return exposeContext();
        } catch (ExecutionException ex) {
            Throwable orig = ex.getCause();
            if (orig instanceof EndpointRegistrationException) {
                throw (EndpointRegistrationException) orig;
            }
            RestLogging.restLogger.log(Level.SEVERE, RestLogging.INIT_FAILED, orig);
            return null;
        }
    }

    private ServerContext getServerContext() {
        return serviceLocator.getService(ServerContext.class);
    }

    private JerseyContainer exposeContext() throws EndpointRegistrationException {
        Set<Class<?>> classes = RestCommandResourceProvider.getResourceClasses();
        // Use common classloader. Jersey artifacts are not visible through
        // module classloader. Actually there is a more important reason to use CommonClassLoader.
        // jax-rs API called RuntimeDelegate makes stupid class loading assumption and throws LinkageError
        // when it finds an implementation of RuntimeDelegate that's part of WLS system class loader.
        // So, we force it to restrict its search space using common class loader.
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ServerContext serverContext = getServerContext();
            ClassLoader apiClassLoader = serverContext.getCommonClassLoader();
            Thread.currentThread().setContextClassLoader(apiClassLoader);
            ResourceConfig rc = new RestCommandResourceProvider().getResourceConfig(classes, serverContext,
                serviceLocator, getAdditionalBinders());
            return getJerseyContainer(rc);
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }
    }

    private JerseyContainer getJerseyContainer(ResourceConfig config) {
        AdminJerseyServiceIteratorProvider iteratorProvider = new AdminJerseyServiceIteratorProvider();
        try {
            ServiceFinder.setIteratorProvider(iteratorProvider);
            RestLogging.restLogger.log(Level.FINEST,
                () -> this + ": Creating Jersey container for " + HttpHandler.class + " and " + config);
            final GrizzlyHttpContainer httpHandler = ContainerFactory.createContainer(GrizzlyHttpContainer.class, config);
            final ServiceLocator jerseyLocator = httpHandler.getApplicationHandler().getInjectionManager().getInstance(ServiceLocator.class);
            ExtrasUtilities.enableTopicDistribution(jerseyLocator);
            return new JerseyContainer() {
                @Override
                public void service(Request request, Response response) throws Exception {
                    httpHandler.service(request, response);
                }
            };
        } finally {
            iteratorProvider.disable();
        }
    }

    private Set<? extends Binder> getAdditionalBinders() {
        return Collections.singleton(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(RestAdapter.SubjectReferenceFactory.class).to(new TypeLiteral<Ref<Subject>>() {
                }).in(PerLookup.class);
                bindFactory(Hk2ReferencingFactory.<Subject>referenceFactory()).to(new TypeLiteral<Ref<Subject>>() {
                }).in(RequestScoped.class);
            }
        });
    }

}
